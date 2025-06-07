BEGIN;
CREATE OR REPLACE FUNCTION check_sequential_model_version()
    RETURNS TRIGGER AS
$$
DECLARE
    max_version INT;
BEGIN
    SELECT MAX(version_number)
    INTO max_version
    FROM model_versions
    WHERE model_id = NEW.model_id;
    IF max_version IS NULL THEN
        IF NEW.version_number <> 1 THEN
            RAISE EXCEPTION 'The first model version must be 1. Provided: %', NEW.version_number;
        END IF;
    ELSE
        IF NEW.version_number <> max_version + 1 THEN
            RAISE EXCEPTION 'Model version number % for model % must be % (next after %), but % was provided',
                NEW.version_number, NEW.model_id, max_version + 1, max_version, NEW.version_number;
        END IF;
    END IF;

    IF NEW.version_number <= 0 THEN
        RAISE EXCEPTION 'Model version number must be greater than 0.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER enforce_sequential_model_version
    BEFORE INSERT
    ON model_versions
    FOR EACH ROW
EXECUTE FUNCTION check_sequential_model_version();

CREATE OR REPLACE FUNCTION prevent_admin_deletion()
    RETURNS trigger AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM user_roles ur
                 JOIN roles r ON ur.role_id = r.id
        WHERE ur.user_id = OLD.id
          AND ur.is_active = true
          AND r.name = 'Administrator'
    ) THEN
        RAISE EXCEPTION 'Cannot delete a user with an active admin role.';
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_admin_user_deletion
    BEFORE DELETE ON users
    FOR EACH ROW
EXECUTE FUNCTION prevent_admin_deletion();



CREATE OR REPLACE FUNCTION next_model_version(version_number integer)
    returns integer
    language plpgsql
as
$$
begin
    return version_number + 1;
end;
$$;

CREATE OR REPLACE FUNCTION calculate_training_cost(rec RECORD)
    RETURNS INTEGER AS
$$
DECLARE
    loss_function_cost double precision;
    optimizer_cost     double precision;
    dataset_cost       double precision;
    model_cost         INTEGER;
BEGIN
    SELECT price INTO loss_function_cost FROM loss_functions WHERE id = rec.loss_function;
    SELECT price INTO optimizer_cost FROM optimizers WHERE id = rec.optimizer;
    SELECT price INTO dataset_cost FROM datasets WHERE id = rec.dataset_id;
    SELECT count(*)
    INTO model_cost
    FROM model_versions m, jsonb_each(m.architecture)
    WHERE m.id = rec.model_version_id;
    RETURN CEIL(sqrt(100 * loss_function_cost * optimizer_cost * dataset_cost * rec.max_epochs * model_cost));
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_user_token_balance(user_id_param UUID)
    RETURNS INTEGER AS
$$
DECLARE
    token_changes   INTEGER;
    current_balance INTEGER;
BEGIN
    SELECT COALESCE(SUM(th.amount), 0)
    INTO token_changes
    FROM token_history th
    WHERE th.user_id = user_id_param;

    current_balance := token_changes;

    --should never occur
    IF current_balance < 0 THEN
        current_balance := 0;
    END IF;

    RETURN current_balance;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION calculate_event_price(
    p_user_id UUID,
    p_event_id INTEGER
) RETURNS INTEGER AS
$$
DECLARE
    v_role_id     INTEGER;
    v_final_price INTEGER;
BEGIN
    SELECT ur.role_id
    INTO v_role_id
    FROM user_roles ur
    WHERE ur.user_id = p_user_id
      AND ur.is_active = TRUE
    ORDER BY ur.assigned_at DESC
    LIMIT 1;

    IF v_role_id IS NULL THEN
        RAISE EXCEPTION 'User % has no active role', p_user_id;
    END IF;

    SELECT COALESCE(
                   (SELECT cep.price
                    FROM custom_event_prices cep
                    WHERE cep.event_id = p_event_id
                      AND cep.role_id = v_role_id),
                   (SELECT e.base_price
                    FROM events e
                    WHERE e.id = p_event_id)
           )
    INTO v_final_price;

    IF v_final_price IS NULL THEN
        RAISE EXCEPTION 'No price found for event %', p_event_id;
    END IF;

    RETURN v_final_price;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION insert_token_history_on_new_role()
    RETURNS TRIGGER AS
$$
DECLARE
existing_count    INT;
    parallel_tokens   INT;
    existing_roles    INT;
    granted_tokens    INT;
    role_tokens       INT;
    new_role_event_id INT;
BEGIN
SELECT id
INTO new_role_event_id
FROM events
WHERE name = 'New Role Tokens';

SELECT initial_tokens
INTO role_tokens
FROM roles
WHERE id = NEW.role_id;

SELECT MAX(r2.initial_tokens)
INTO parallel_tokens
FROM user_roles u
         JOIN roles r2 ON u.role_id = r2.id
WHERE u.user_id = NEW.user_id
  AND u.assigned_at = NEW.assigned_at;

IF role_tokens < parallel_tokens THEN
        RETURN NEW;
END IF;

SELECT COUNT(*)
INTO existing_roles
FROM user_roles u
         LEFT JOIN roles r2 on u.role_id = r2.id
WHERE u.user_id = NEW.user_id
  AND u.assigned_at < NEW.assigned_at;

IF existing_roles = 0 THEN
        INSERT INTO token_history (user_id, amount, event_type, description)
        VALUES (NEW.user_id,
                role_tokens,
                new_role_event_id,
                'Granted initial tokens for new role');
RETURN NEW;
END IF;

SELECT COUNT(*)
INTO existing_count
FROM user_roles
WHERE user_id = NEW.user_id
  AND role_id = NEW.role_id
  AND assigned_at < NEW.assigned_at;

IF existing_count = 0 THEN
SELECT MAX(initial_tokens)
INTO granted_tokens
FROM user_roles
         LEFT JOIN roles r on r.id = user_roles.role_id
WHERE user_id = NEW.user_id
  AND assigned_at < NEW.assigned_at
  AND role_id != NEW.role_id;


IF role_tokens - granted_tokens > 0 THEN
            INSERT INTO token_history (user_id, amount, event_type, description)
            VALUES (NEW.user_id,
                    role_tokens - granted_tokens,
                    new_role_event_id,
                    'Granted initial tokens for new role');
END IF;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_insert_token_on_new_role
    AFTER INSERT
    ON user_roles
    FOR EACH ROW
    EXECUTE FUNCTION insert_token_history_on_new_role();

CREATE OR REPLACE FUNCTION give_basic_role() RETURNS trigger AS
$$
BEGIN
    INSERT INTO user_roles
    VALUES (NEW.id, (SELECT id FROM roles WHERE name = 'Basic User'), now(), True);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER give_basic_role
    AFTER INSERT
    ON users
    FOR EACH ROW
EXECUTE PROCEDURE give_basic_role();

CREATE OR REPLACE FUNCTION one_active_role() RETURNS trigger AS
$$
BEGIN
    UPDATE user_roles
    SET is_active = FALSE
    WHERE user_id = NEW.user_id
      AND (role_id != NEW.role_id OR assigned_at != NEW.assigned_at)
      AND is_active = TRUE;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER one_active_role
    AFTER INSERT
    ON user_roles
    FOR EACH ROW
EXECUTE PROCEDURE one_active_role();

CREATE OR REPLACE FUNCTION check_user_tokens_before_model_creation()
    RETURNS TRIGGER AS
$$
DECLARE
    user_balance            INTEGER;
    model_creation_event_id INTEGER;
    event_price             INTEGER;
BEGIN
    SELECT id INTO model_creation_event_id FROM events WHERE name = 'Model Creation';
    event_price := calculate_event_price(NEW.user_id, model_creation_event_id);


    user_balance := get_user_token_balance(NEW.user_id);

    IF user_balance < -event_price THEN --event_price<0, that is why "-"
        RAISE EXCEPTION 'Insufficient tokens to create a model. Current balance: %, required: %', user_balance, -event_price
            USING ERRCODE = 'P0001';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION update_token_history_after_model_creation()
    RETURNS TRIGGER AS
$$

DECLARE
    v_event_id INTEGER;
    v_price    INTEGER;
BEGIN
    SELECT id INTO v_event_id FROM events WHERE name = 'Model Creation';
    v_price := calculate_event_price(NEW.user_id, v_event_id);
    INSERT INTO token_history (user_id, amount, event_type, description, model_id)
    VALUES (NEW.user_id,
            v_price,
            v_event_id,
            'Model Creation: ' || NEW.name,
            NEW.id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER enforce_min_tokens_for_model_creation
    BEFORE INSERT
    ON models
    FOR EACH ROW
EXECUTE FUNCTION check_user_tokens_before_model_creation();

CREATE OR REPLACE TRIGGER update_token_history_after_model_creation
    AFTER INSERT
    ON models
    FOR EACH ROW
EXECUTE FUNCTION update_token_history_after_model_creation();


CREATE OR REPLACE FUNCTION check_user_tokens_before_training()
    RETURNS TRIGGER AS
$$
DECLARE
    user_balance      INTEGER;
    training_event_id INTEGER;
    training_cost     INTEGER;
    event_price       INTEGER;
    model_user_id     UUID;
BEGIN
    SELECT id
    INTO training_event_id
    FROM events
    WHERE name = 'Model Training';

    SELECT m.user_id
    INTO model_user_id
    FROM model_versions mv
             JOIN models m ON mv.model_id = m.id
    WHERE mv.id = NEW.model_version_id;

    event_price := calculate_event_price(model_user_id, training_event_id);

    training_cost := -calculate_training_cost(NEW) + event_price;

    user_balance := get_user_token_balance(model_user_id);

    IF user_balance < -training_cost THEN --training_cost<0, that is why "-"
        RAISE EXCEPTION 'Insufficient tokens to start training. Current balance: %, required: %',
            user_balance, -training_cost
            USING ERRCODE = 'P0002';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_token_history_after_training()
    RETURNS TRIGGER AS
$$
DECLARE
    training_event_id INTEGER;
    training_cost     INTEGER;
    event_price       INTEGER;
    total_cost        INTEGER;
    v_model_id        UUID;
    model_user_id     UUID;
    model_name        TEXT;
    version_number    TEXT;
BEGIN
    SELECT id INTO training_event_id FROM events WHERE name = 'Model Training';

    SELECT m.id,
           m.user_id,
           m.name,
           mv.version_number
    INTO
        v_model_id,
        model_user_id,
        model_name,
        version_number
    FROM model_versions mv
             JOIN models m ON mv.model_id = m.id
    WHERE mv.id = NEW.model_version_id;

    training_cost := -calculate_training_cost(NEW);
    event_price := calculate_event_price(model_user_id, training_event_id);

    total_cost := training_cost + event_price;

    INSERT INTO token_history (user_id,
                               amount,
                               event_type,
                               description,
                               training_id,
                               model_id)
    VALUES (model_user_id,
            total_cost,
            training_event_id,
            'Model Training: ' || model_name || ' version #' || version_number,
            NEW.id,
            v_model_id);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER enforce_min_tokens_for_training
    BEFORE INSERT
    ON trainings
    FOR EACH ROW
EXECUTE FUNCTION check_user_tokens_before_training();

CREATE OR REPLACE TRIGGER update_token_history_after_starting_model_training
    AFTER INSERT
    ON trainings
    FOR EACH ROW
EXECUTE FUNCTION update_token_history_after_training();


CREATE OR REPLACE FUNCTION ordinal(n int)
    RETURNS text AS
$$
BEGIN
    RETURN n || CASE
                    WHEN (n % 100) BETWEEN 11 AND 13 THEN 'th'
                    WHEN (n % 10) = 1 THEN 'st'
                    WHEN (n % 10) = 2 THEN 'nd'
                    WHEN (n % 10) = 3 THEN 'rd'
                    ELSE 'th'
        END;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_token_history_after_public_results()
    RETURNS TRIGGER AS
$$
DECLARE
    v_user_id                 UUID;
    v_country_id              INTEGER;
    v_country_rank            INTEGER;
    v_global_rank             INTEGER;
    v_event_id                INTEGER;
    v_reward_global           INTEGER;
    v_reward_country          INTEGER;
    v_reward_total            INTEGER;
    v_description             TEXT;
    v_description_global      TEXT;
    v_description_country     TEXT;
    v_dataset_id              UUID;
    v_old_best_reward_global  INTEGER;
    v_old_best_reward_country INTEGER;
    v_new_loss                DOUBLE PRECISION;
    v_new_accuracy            NUMERIC(6, 3);
BEGIN
    SELECT u.id, u.country_id
    INTO v_user_id, v_country_id
    FROM trainings t
             JOIN model_versions mv ON t.model_version_id = mv.id
             JOIN models m ON mv.model_id = m.id
             JOIN users u ON m.user_id = u.id
    WHERE t.id = NEW.training_id;

    SELECT accuracy, loss
    INTO v_new_accuracy, v_new_loss
    FROM training_metrics
    WHERE training_id = NEW.training_id
      AND type = 'TEST'
    ORDER BY epoch DESC
    LIMIT 1;

    SELECT dataset_id INTO v_dataset_id FROM trainings WHERE id = NEW.training_id;

    SELECT COALESCE((SELECT max_tokens
                     FROM best_results
                     WHERE user_id = v_user_id
                       AND dataset_id = v_dataset_id
                       AND scope = 'global'), 0)
    INTO v_old_best_reward_global;

    SELECT COALESCE((SELECT max_tokens
                     FROM best_results
                     WHERE user_id = v_user_id
                       AND dataset_id = v_dataset_id
                       AND scope = 'country'), 0)
    INTO v_old_best_reward_country;

    SELECT COUNT(*) + 1
    INTO v_global_rank
    FROM leaderboards lb
             JOIN datasets d ON lb.dataset = d.name
    WHERE d.id = v_dataset_id
      AND (lb.accuracy > v_new_accuracy
        OR (lb.accuracy = v_new_accuracy AND lb.loss < v_new_loss));

    SELECT COUNT(*) + 1
    INTO v_country_rank
    FROM leaderboards lb
             JOIN datasets d ON lb.dataset = d.name
             JOIN countries c ON lb.country = c.name
    WHERE d.id = v_dataset_id
      AND c.id = v_country_id
      AND (lb.accuracy > v_new_accuracy
        OR (lb.accuracy = v_new_accuracy AND lb.loss < v_new_loss));

    v_description_country := NULL;
    IF v_country_rank = 1 THEN
        v_event_id := (SELECT id FROM events WHERE name = '1st Place Country');
        v_reward_country := calculate_event_price(v_user_id, v_event_id);
        v_description_country := '1st place in country with accuracy: ' || v_new_accuracy;
    ELSIF v_country_rank = 2 THEN
        v_event_id := (SELECT id FROM events WHERE name = '2nd Place Country');
        v_reward_country := calculate_event_price(v_user_id, v_event_id);
        v_description_country := '2nd place in country with accuracy: ' || v_new_accuracy;
    ELSIF v_country_rank = 3 THEN
        v_event_id := (SELECT id FROM events WHERE name = '3rd Place Country');
        v_reward_country := calculate_event_price(v_user_id, v_event_id);
        v_description_country := '3rd place in country with accuracy: ' || v_new_accuracy;
    END IF;

    v_reward_country := GREATEST(0, v_reward_country - v_old_best_reward_country);
    if (v_reward_country > 0) THEN
        INSERT INTO token_history (user_id, amount, event_type, description, training_id)
        VALUES (v_user_id, v_reward_country, v_event_id, v_description_country, NEW.training_id);
    END IF;

    v_description_global := NULL;
    IF v_global_rank = 1 THEN
        v_event_id := (SELECT id FROM events WHERE name = '1st Place Global');
        v_reward_global := calculate_event_price(v_user_id, v_event_id);
        v_description_global := '1st place globally with accuracy: ' || v_new_accuracy || '%';
    ELSIF v_global_rank = 2 THEN
        v_event_id := (SELECT id FROM events WHERE name = '2nd Place Global');
        v_reward_global := calculate_event_price(v_user_id, v_event_id);
        v_description_global := '2nd place globally with accuracy: ' || v_new_accuracy || '%';
    ELSIF v_global_rank = 3 THEN
        v_event_id := (SELECT id FROM events WHERE name = '3rd Place Global');
        v_reward_global := calculate_event_price(v_user_id, v_event_id);
        v_description_global := '3rd place globally with accuracy: ' || v_new_accuracy || '%';
    END IF;

    v_reward_global := GREATEST(0, v_reward_global - v_old_best_reward_global);
    IF (v_reward_global > 0) THEN
        INSERT INTO token_history (user_id, amount, event_type, description, training_id)
        VALUES (v_user_id, v_reward_global, v_event_id, v_description_global, NEW.training_id);
    END IF;

    v_reward_total := v_reward_country + v_reward_global;
    IF v_reward_total > 0 THEN
        v_description := '';
        IF (v_description_country IS NOT NULL) THEN
            v_description := v_description_country;
        END IF;
        IF (v_description_global IS NOT NULL) THEN
            IF (v_description = '') THEN
                v_description := v_description;
            ELSE
                v_description := v_description || '\n' || v_description_global;
            END IF;
        END IF;

        PERFORM pg_notify(
                'reward_channel',
                format('Congratulations! You''ve earned %s tokens!\n%s',
                       v_reward_total, v_description)
                );
    ELSE
        PERFORM pg_notify(
                'reward_channel',
                format(
                        'Congratulations! You''ve shared the model. It currently ranks %s globally and %s in your country.',
                        ordinal(v_global_rank),
                        ordinal(v_country_rank))
                );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trigger_update_token_history_after_public_results
    AFTER INSERT
    ON public_results
    FOR EACH ROW
EXECUTE FUNCTION update_token_history_after_public_results();

COMMIT;