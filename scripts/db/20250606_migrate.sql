BEGIN;

TRUNCATE custom_event_prices;

DELETE FROM events
WHERE name IN ('Saving New Model Version', 'Model Stopping', 'Application Login');

UPDATE events
SET name = 'Bought Tokens'
WHERE name = 'BoughtTokens';

ALTER TABLE token_history
    DROP CONSTRAINT token_history_event_type_fkey;

ALTER TABLE custom_event_prices
    DROP CONSTRAINT custom_event_prices_event_id_fkey;

UPDATE events
SET id = 8
WHERE id = 9;

UPDATE events
SET id = 9
WHERE id = 12;

UPDATE events
SET id = 10
WHERE id = 13;

UPDATE token_history
SET event_type = 8
WHERE event_type = 9;

UPDATE token_history
SET event_type = 9
WHERE event_type = 12;

UPDATE token_history
SET event_type = 10
WHERE event_type = 13;

UPDATE custom_event_prices
SET event_id = 8
WHERE event_id = 9;

UPDATE custom_event_prices
SET event_id = 9
WHERE event_id = 12;

UPDATE custom_event_prices
SET event_id = 10
WHERE event_id = 13;

ALTER TABLE token_history
    ADD CONSTRAINT token_history_event_type_fkey
        FOREIGN KEY (event_type) REFERENCES events(id);

ALTER TABLE custom_event_prices
    ADD CONSTRAINT custom_event_prices_event_id_fkey
        FOREIGN KEY (event_id) REFERENCES events(id);

UPDATE users
SET last_name = 'Jastrzebski'
WHERE email = 'hubizon@mail.com';

CREATE OR REPLACE FUNCTION insert_custom_event_price(
    event_name TEXT,
    role_name TEXT,
    price NUMERIC
)
    RETURNS VOID AS
$$
BEGIN
    INSERT INTO custom_event_prices (event_id, role_id, price)
    VALUES (
               (SELECT id FROM events WHERE name = event_name),
               (SELECT id FROM roles WHERE name = role_name),
               price
           );
END;
$$ LANGUAGE plpgsql;

SELECT insert_custom_event_price('Model Training', 'Administrator', 0);
SELECT insert_custom_event_price('Model Training', 'Premium User', -5);
SELECT insert_custom_event_price('1st Place Global', 'Premium User', 1500);
SELECT insert_custom_event_price('1st Place Country', 'Premium User', 700);
SELECT insert_custom_event_price('2nd Place Global', 'Premium User', 400);
SELECT insert_custom_event_price('2nd Place Country', 'Premium User', 300);
SELECT insert_custom_event_price('3rd Place Global', 'Premium User', 200);
SELECT insert_custom_event_price('3rd Place Country', 'Premium User', 100);

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

ALTER TABLE user_roles        DROP CONSTRAINT user_roles_user_id_fkey;
ALTER TABLE models            DROP CONSTRAINT models_user_id_fkey;
ALTER TABLE token_history     DROP CONSTRAINT token_history_user_id_fkey;

ALTER TABLE user_roles
    ADD CONSTRAINT user_roles_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE models
    ADD CONSTRAINT models_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE token_history
    ADD CONSTRAINT token_history_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE model_versions    DROP CONSTRAINT model_versions_model_id_fkey;
ALTER TABLE trainings         DROP CONSTRAINT trainings_model_version_id_fkey;
ALTER TABLE training_metrics  DROP CONSTRAINT training_metrics_training_id_fkey;
ALTER TABLE public_results    DROP CONSTRAINT public_results_training_id_fkey;
ALTER TABLE token_history     DROP CONSTRAINT token_history_training_id_fkey;
ALTER TABLE token_history     DROP CONSTRAINT token_history_model_id_fkey;

ALTER TABLE model_versions
    ADD CONSTRAINT model_versions_model_id_fkey
        FOREIGN KEY (model_id) REFERENCES models(id) ON DELETE CASCADE;

ALTER TABLE trainings
    ADD CONSTRAINT trainings_model_version_id_fkey
        FOREIGN KEY (model_version_id) REFERENCES model_versions(id) ON DELETE CASCADE;

ALTER TABLE training_metrics
    ADD CONSTRAINT training_metrics_training_id_fkey
        FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE;

ALTER TABLE public_results
    ADD CONSTRAINT public_results_training_id_fkey
        FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE;

ALTER TABLE token_history
    ADD CONSTRAINT token_history_training_id_fkey
        FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE;

ALTER TABLE token_history
    ADD CONSTRAINT token_history_model_id_fkey
        FOREIGN KEY (model_id) REFERENCES models(id) ON DELETE CASCADE;

COMMIT;