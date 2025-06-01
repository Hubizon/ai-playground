BEGIN;
DROP TABLE IF EXISTS public_results CASCADE;
DROP TABLE IF EXISTS training_metrics CASCADE;
DROP TABLE IF EXISTS trainings CASCADE;
DROP TABLE IF EXISTS token_history CASCADE;
DROP TABLE IF EXISTS model_versions CASCADE;
DROP TABLE IF EXISTS models CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS datasets CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS statuses CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS loss_functions CASCADE;
DROP TABLE IF EXISTS optimizers CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS countries CASCADE;
DROP TABLE IF EXISTS currencies CASCADE;
DROP TABLE IF EXISTS custom_event_prices CASCADE;
DROP FUNCTION IF EXISTS check_sequential_model_version CASCADE;
DROP TRIGGER IF EXISTS enforce_sequential_model_version ON model_versions CASCADE;

CREATE TABLE currencies
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    conversion_rate double precision --ile potrzebujemy, żeby kupić jednego dolara
);

CREATE TABLE countries
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    currency INT REFERENCES currencies DEFAULT 1
);

CREATE TABLE categories
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE optimizers
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    price double precision
);

CREATE TABLE loss_functions
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    price double precision
);

CREATE TABLE events
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(50) NOT NULL,
    base_price INTEGER,
    training_related boolean NOT NULL,
    model_related boolean NOT NULL
);

CREATE TABLE statuses
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(500)
);

CREATE TABLE users
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(50) UNIQUE  NOT NULL,
    first_name    VARCHAR(100)        NOT NULL,
    last_name     VARCHAR(100),
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    country_id    INT                 NOT NULL REFERENCES countries (id),
    birth_date    DATE                NOT NULL,
    created_at    TIMESTAMPTZ      DEFAULT now(),
    CHECK (birth_date <= CURRENT_DATE)
);

CREATE TABLE roles
(
    id             SERIAL PRIMARY KEY,
    name           VARCHAR(50) UNIQUE NOT NULL,
    initial_tokens INT,
    CHECK (initial_tokens >= 0)
);

CREATE TABLE user_roles
(
    user_id     UUID    NOT NULL REFERENCES users (id),
    role_id     INT     NOT NULL REFERENCES roles (id),
    assigned_at TIMESTAMPTZ DEFAULT now(),
    is_active   BOOLEAN NOT NULL,
    PRIMARY KEY (user_id, role_id, assigned_at)
);

CREATE TABLE models
(
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID         NOT NULL REFERENCES users (id),
    name    VARCHAR(100) NOT NULL
);

CREATE TABLE model_versions
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    model_id       UUID  NOT NULL REFERENCES models (id),
    version_number INT   NOT NULL DEFAULT 1,
    architecture   JSONB NOT NULL,
    created_at     TIMESTAMPTZ      DEFAULT now()
);

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

CREATE TABLE datasets
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    category_id INT                 NOT NULL REFERENCES categories (id),
    created_at  TIMESTAMPTZ      DEFAULT now(),
    price double precision DEFAULT 1

);



CREATE TABLE trainings
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    model_version_id UUID                           NOT NULL REFERENCES model_versions (id),
    dataset_id       UUID                           NOT NULL REFERENCES datasets (id),
    learning_rate    REAL                           NOT NULL,
    optimizer        INT                            NOT NULL REFERENCES optimizers (id),
    loss_function    INT                            NOT NULL REFERENCES loss_functions (id),
    max_epochs       INT                            NOT NULL,
    batch_size       INT                            NOT NULL,
    status           INT                            NOT NULL REFERENCES statuses (id),
    started_at       TIMESTAMPTZ      DEFAULT now() NOT NULL,
    finished_at      TIMESTAMPTZ,
    CHECK (learning_rate > 0),
    CHECK (finished_at IS NULL OR finished_at >= started_at)
);

CREATE TABLE training_metrics
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    training_id UUID                           NOT NULL REFERENCES trainings (id),
    epoch       INT                            NOT NULL,
    loss        DOUBLE PRECISION               NOT NULL,
    accuracy    DOUBLE PRECISION               NOT NULL,
    timestamp   TIMESTAMPTZ      DEFAULT now() NOT NULL,
    CHECK (epoch >= 0),
    CHECK (accuracy >= 0.0 AND accuracy <= 1.0)
);

CREATE TABLE token_history
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users (id),
    amount      INT  NOT NULL,
    event_type  INT  NOT NULL REFERENCES events (id),
    description TEXT,
    timestamp   TIMESTAMPTZ      DEFAULT now(),
    training_id UUID REFERENCES trainings,
    model_id UUID REFERENCES models
        CHECK (amount != 0)
);

CREATE TABLE public_results
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    training_id UUID             NOT NULL REFERENCES trainings (id) UNIQUE,
    accuracy    DOUBLE PRECISION NOT NULL,
    loss        DOUBLE PRECISION NOT NULL,
    shared_at   TIMESTAMPTZ      DEFAULT now(),
    CHECK (accuracy >= 0.0 AND accuracy <= 1.0)
);

CREATE TABLE custom_event_prices
(
  event_id INTEGER REFERENCES events,
  role_id INTEGER REFERENCES roles,
  price double precision
);

INSERT INTO currencies (name,conversion_rate)
VALUES ('dolar',1),
    ('złoty',4),
    ('euro', 0.9),
    ('funt', 0.8),
    ('Juan', 8);

INSERT INTO countries (name, currency)
VALUES ('Poland',2),
       ('Germany',3),
       ('USA',1),
       ('United Kingdom',4),
       ('China',5),
       ('France',3);
INSERT INTO countries (name)
VALUES
       ('Canada'),
       ('Australia'),
       ('Japan'),
       ('China'),
       ('India'),
       ('Brazil'),
       ('Mexico'),
       ('Spain'),
       ('Italy'),
       ('South Korea'),
       ('Netherlands'),
       ('Sweden'),
       ('Switzerland'),
       ('Argentina'),
       ('South Africa'),
       ('Egypt'),
       ('New Zealand'),
       ('Norway'),
       ('Denmark');

INSERT INTO categories (name)
VALUES ('Image Recognition'),
       ('Natural Language Processing'),
       ('Time Series Analysis'),
       ('Recommendation Systems'),
       ('Tabular Data'),
       ('Reinforcement Learning'),
       ('Generative Models');

INSERT INTO optimizers (name,price)
VALUES ('Adam',1.5),
       ('SGD',1),
       ('RMSprop',1.2),
       ('AdaDelta',1.2),
       ('AdaGrad',1.2);

INSERT INTO loss_functions (name,price)
VALUES ('Mean Squared Error',1),
       ('Harmonic Loss',1.05),
       ('Binary Cross-Entropy',1.1),
       ('Cross-Entropy',1.2);

INSERT INTO events (name, base_price,training_related,model_related)
VALUES ('3rd Place Global', 100,TRUE, FALSE),
       ('3rd Place Country', 50,TRUE, FALSE),
       ('2nd Place Global', 200,TRUE, FALSE),
       ('2nd Place Country', 100,TRUE, FALSE),
       ('1st Place Global', 500,TRUE,FALSE),
       ('1st Place Country', 250,TRUE, FALSE),
       ('Model Creation', -50,FALSE, TRUE),
       ('Saving New Model Version', -20,FALSE, TRUE),
       ('Model Training', -10,TRUE,FALSE),
       ('Model Stopping', -5,TRUE,FALSE),
       ('Application Login', -1,FALSE, FALSE);

INSERT INTO statuses (name, description)
VALUES ('Queue', 'The training is waiting to start.'),
       ('In Progress', 'The training is currently running.'),
       ('Finished', 'The training completed successfully.'),
       ('Error', 'The training finished with an error.'),
       ('Cancelled', 'The training was cancelled by the user or system.');

INSERT INTO roles (name, initial_tokens)
VALUES ('Basic User', 1000),
       ('Premium User', 5000),
       ('Administrator', 99999);

WITH inserted_users AS (
    INSERT INTO users (username, first_name, last_name, email, password_hash, country_id,
                       birth_date) VALUES ('admin', 'admin', 'admin', 'admin@admin.com',
                                           '$2a$10$34z1aIuXDSogxnsZS090DOaA3Sgs5q.03RA4tEUP5GbVHgmiJyDRi', 1,
                                           '2000-01-01'),
                                          ('fimpro', 'Filip', 'Manijak', 'filip@example.com',
                                           '$2a$10$34z1aIuXDSogxnsZS090DOaA3Sgs5q.03RA4tEUP5GbVHgmiJyDRi', 2,
                                           '1960-01-01'),
                                          ('hubizon', 'Hubert', 'Jastrzębski', 'hubizon@mail.com',
                                           '$2a$10$34z1aIuXDSogxnsZS090DOaA3Sgs5q.03RA4tEUP5GbVHgmiJyDRi', 3,
                                           '2004-02-29'),
                                          ('Igas', 'Ignacy', 'Wojtulewicz', 'ignacy@domena.com',
                                           '$2a$10$34z1aIuXDSogxnsZS090DOaA3Sgs5q.03RA4tEUP5GbVHgmiJyDRi', 4,
                                           '2000-01-01')
        RETURNING id, username)

INSERT
INTO user_roles (user_id, role_id, is_active)
VALUES ((SELECT id FROM inserted_users WHERE username = 'admin'),
       (SELECT id FROM roles WHERE name = 'Administrator'), TRUE),
       ((SELECT id FROM inserted_users WHERE username = 'fimpro'),
       (SELECT id FROM roles WHERE name = 'Premium User'), TRUE),
       ((SELECT id FROM inserted_users WHERE username = 'hubizon'),
       (SELECT id FROM roles WHERE name = 'Premium User'), TRUE),
       ((SELECT id FROM inserted_users WHERE username = 'Igas'),
        (SELECT id FROM roles WHERE name = 'Basic User'), TRUE);

INSERT INTO custom_event_prices (event_id, role_id, price)
VALUES ((SELECT id FROM events WHERE name = 'Application Login'),
        (SELECT id FROM roles WHERE name = 'Administrator'),
        0),
       ((SELECT id FROM events WHERE name = 'Model Training'),
       (SELECT id FROM roles WHERE name = 'Premium User'),
    -5);

INSERT INTO datasets (name, description, category_id, price)
VALUES ('Iris',
        'A classic dataset for classification, containing 3 classes of 50 instances each, where each class refers to a type of iris plant.',
        (SELECT id FROM categories WHERE name = 'Tabular Data'),1),
       ('Moons',
        'A synthetic dataset for binary classification, shaped like two interleaving half-circles.',
        (SELECT id FROM categories WHERE name = 'Tabular Data'),1),
       ('Blobs',
        'A synthetic dataset for clustering, consisting of isotropic Gaussian blobs.',
        (SELECT id FROM categories WHERE name = 'Tabular Data'),1),
       ('Circles',
        'A synthetic dataset for binary classification, shaped like two concentric circles.',
        (SELECT id FROM categories WHERE name = 'Tabular Data'),1),
       ('MNIST',
        'A large database of handwritten digits commonly used for training image processing systems.',
        (SELECT id FROM categories WHERE name = 'Image Recognition'),10);

ALTER TABLE datasets
    ADD COLUMN path TEXT;
UPDATE datasets
SET path = 'datasets/' || name || '.csv';

CREATE OR REPLACE FUNCTION next_model_version(version_number integer)
    returns integer
    language plpgsql
as
$$
begin
    return version_number + 1;
end;
$$;

CREATE OR REPLACE FUNCTION give_basic_role() RETURNS trigger AS
$$
BEGIN
    INSERT INTO user_roles VALUES (NEW.id, (SELECT id FROM roles WHERE name = 'Basic User'), now(), True);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER give_basic_role AFTER INSERT ON users
    FOR EACH ROW EXECUTE PROCEDURE give_basic_role();

CREATE OR REPLACE FUNCTION calculate_training_cost(rec RECORD)
    RETURNS double precision AS $$
DECLARE
    loss_function_cost double precision;
    optimizer_cost double precision;
    dataset_cost double precision;
    model_cost INTEGER;
BEGIN
    SELECT price INTO loss_function_cost FROM loss_functions WHERE id = rec.loss_function;
    SELECT price INTO optimizer_cost FROM optimizers WHERE id = rec.optimizer;
    SELECT price INTO dataset_cost FROM datasets WHERE id = rec.dataset_id;
    SELECT count(*)
    INTO model_cost
    FROM model_versions m,
        jsonb_each(m.architecture)
    WHERE m.id = rec.model_version_id;
    RETURN sqrt(loss_function_cost * optimizer_cost * dataset_cost * rec.max_epochs * model_cost);
END;
$$ LANGUAGE plpgsql;

--COMMIT;

CREATE OR REPLACE FUNCTION get_user_token_balance(user_id_param UUID)
RETURNS INTEGER AS $$
DECLARE
initial_tokens INTEGER;
    token_changes INTEGER;
    current_balance INTEGER;
BEGIN
SELECT r.initial_tokens INTO initial_tokens
FROM user_roles ur
         JOIN roles r ON ur.role_id = r.id
WHERE ur.user_id = user_id_param AND ur.is_active = TRUE
ORDER BY ur.assigned_at DESC
    LIMIT 1;

IF initial_tokens IS NULL THEN
        RETURN 0;
END IF;

SELECT COALESCE(SUM(th.amount), 0) INTO token_changes
FROM token_history th
WHERE th.user_id = user_id_param;

current_balance := initial_tokens + token_changes;

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
) RETURNS DOUBLE PRECISION AS $$
DECLARE
v_role_id INTEGER;
    v_custom_price DOUBLE PRECISION;
    v_base_price INTEGER;
    v_final_price DOUBLE PRECISION;
BEGIN

SELECT ur.role_id INTO v_role_id
FROM user_roles ur
WHERE ur.user_id = p_user_id AND ur.is_active = TRUE
ORDER BY ur.assigned_at DESC
    LIMIT 1;

IF v_role_id IS NULL THEN
        RAISE EXCEPTION 'User % has no active role', p_user_id;
END IF;

 SELECT COALESCE(
(SELECT cep.price
FROM custom_event_prices cep
WHERE cep.event_id = p_event_id AND cep.role_id = v_role_id),
(SELECT e.base_price
FROM events e
WHERE e.id = p_event_id)
) INTO v_final_price;

IF v_final_price IS NULL THEN
        RAISE EXCEPTION 'No price found for event %', p_event_id;
END IF;

RETURN v_final_price;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION check_user_tokens_before_model_creation()
RETURNS TRIGGER AS $$
DECLARE
    user_balance INTEGER;
    model_creation_event_id INTEGER;
    event_price DOUBLE PRECISION;
BEGIN
SELECT id INTO model_creation_event_id FROM events WHERE name = 'Model Creation';
event_price := calculate_event_price(NEW.user_id, model_creation_event_id);


    user_balance := get_user_token_balance(NEW.user_id);

IF user_balance < -event_price THEN -- "-" because model creation price is negative
    RAISE EXCEPTION 'Insufficient tokens to create a model. Current balance: %, required: %', user_balance, event_price;
    RETURN NULL; --TODO: print exception message as error in MainView
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION update_token_history_afetr_model_creation()
RETURNS TRIGGER AS $$

DECLARE
v_event_id INTEGER;
v_price DOUBLE PRECISION;
BEGIN
SELECT id INTO v_event_id FROM events WHERE name = 'Model Creation';
v_price := calculate_event_price(NEW.user_id, v_event_id);
INSERT INTO token_history (user_id, amount, event_type, description, model_id)
VALUES (
           NEW.user_id,
           v_price,
           v_event_id,
           'Model Creation: ' || NEW.name,
           NEW.id
       );
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER enforce_min_tokens_for_model_creation
    BEFORE INSERT ON models
    FOR EACH ROW
    EXECUTE FUNCTION check_user_tokens_before_model_creation();

CREATE OR REPLACE TRIGGER update_token_history_after_model_creation
    AFTER INSERT ON models
    FOR EACH ROW
    EXECUTE FUNCTION update_token_history_afetr_model_creation();


CREATE OR REPLACE FUNCTION check_user_tokens_before_training()
RETURNS TRIGGER AS $$
DECLARE
    user_balance INTEGER;
    training_event_id INTEGER;
    training_cost DOUBLE PRECISION;
    event_price DOUBLE PRECISION;
    model_user_id UUID;
BEGIN
SELECT id INTO training_event_id FROM events WHERE name = 'Model Training';


SELECT m.user_id INTO model_user_id
FROM model_versions mv
         JOIN models m ON mv.model_id = m.id
WHERE mv.id = NEW.model_version_id;

event_price := calculate_event_price(model_user_id,training_event_id);

training_cost := -calculate_training_cost(NEW) + event_price;

user_balance := get_user_token_balance(model_user_id);

    IF user_balance < -training_cost THEN
        RAISE EXCEPTION 'Insufficient tokens to start training. Current balance: %, required: %',
                        user_balance, training_cost;
       RETURN NULL; --TODO: print exception message as error in MainView
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_token_history_after_training()
RETURNS TRIGGER AS $$
DECLARE
    training_event_id INTEGER;
    training_cost DOUBLE PRECISION;
    event_price DOUBLE PRECISION;
    total_cost INTEGER;
    v_model_id UUID;
    model_user_id UUID;
    model_name TEXT;
    version_number TEXT;
BEGIN
SELECT id INTO training_event_id FROM events WHERE name = 'Model Training';

SELECT
    m.id,
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

total_cost := CEIL(training_cost + event_price);

INSERT INTO token_history (
    user_id,
    amount,
    event_type,
    description,
    training_id,
    model_id
) VALUES (
             model_user_id,
             total_cost,
             training_event_id,
             'Model Training: ' || model_name || ' version #' || version_number,
             NEW.id,
             v_model_id
         );

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER enforce_min_tokens_for_training
    BEFORE INSERT ON trainings
    FOR EACH ROW
    EXECUTE FUNCTION check_user_tokens_before_training();

CREATE OR REPLACE TRIGGER update_token_history_after_starting_model_training
    AFTER INSERT ON trainings
    FOR EACH ROW
    EXECUTE FUNCTION update_token_history_after_training();


CREATE OR REPLACE FUNCTION update_token_history_after_public_results()
RETURNS TRIGGER AS $$
DECLARE
v_user_id UUID;
    v_country_id INTEGER;
    v_country_rank INTEGER;
    v_global_rank INTEGER;
    v_event_id INTEGER;
    v_price DOUBLE PRECISION;
    v_training_id UUID;
    v_description TEXT;
BEGIN
SELECT u.id, u.country_id INTO v_user_id, v_country_id
FROM trainings t
         JOIN model_versions mv ON t.model_version_id = mv.id
         JOIN models m ON mv.model_id = m.id
         JOIN users u ON m.user_id = u.id
WHERE t.id = NEW.training_id;

SELECT COUNT(*) + 1 INTO v_country_rank
FROM public_results pr
         JOIN trainings t ON pr.training_id = t.id
         JOIN model_versions mv ON t.model_version_id = mv.id
         JOIN models m ON mv.model_id = m.id
         JOIN users u ON m.user_id = u.id
WHERE u.country_id = v_country_id
  AND pr.accuracy > NEW.accuracy;

SELECT COUNT(*) + 1 INTO v_global_rank
FROM public_results pr
         JOIN trainings t ON pr.training_id = t.id
         JOIN model_versions mv ON t.model_version_id = mv.id
         JOIN models m ON mv.model_id = m.id
         JOIN users u ON m.user_id = u.id
WHERE pr.accuracy > NEW.accuracy;

-- award for country rank
IF v_country_rank = 1 THEN
        v_event_id := (SELECT id FROM events WHERE name = '1st Place Country');
        v_price := calculate_event_price(v_user_id, v_event_id);
        v_description := '1st place in country with accuracy: ' || NEW.accuracy;

INSERT INTO token_history (user_id, amount, event_type, description, training_id)
VALUES (v_user_id, v_price, v_event_id, v_description, NEW.training_id);
ELSIF v_country_rank = 2 THEN
        v_event_id := (SELECT id FROM events WHERE name = '2nd Place Country');
        v_price := calculate_event_price(v_user_id, v_event_id);
        v_description := '2nd place in country with accuracy: ' || NEW.accuracy;

INSERT INTO token_history (user_id, amount, event_type, description, training_id)
VALUES (v_user_id, v_price, v_event_id, v_description, NEW.training_id);
ELSIF v_country_rank = 3 THEN
        v_event_id := (SELECT id FROM events WHERE name = '3rd Place Country');
        v_price := calculate_event_price(v_user_id, v_event_id);
        v_description := '3rd place in country with accuracy: ' || NEW.accuracy;

INSERT INTO token_history (user_id, amount, event_type, description, training_id)
VALUES (v_user_id, v_price, v_event_id, v_description, NEW.training_id);
END IF;

-- award for global rank
    IF v_global_rank = 1 THEN
        v_event_id := (SELECT id FROM events WHERE name = '1st Place Global');
        v_price := calculate_event_price(v_user_id, v_event_id);
        v_description := '1st place globally with accuracy: ' || NEW.accuracy;

INSERT INTO token_history (user_id, amount, event_type, description, training_id)
VALUES (v_user_id, v_price, v_event_id, v_description, NEW.training_id);
ELSIF v_global_rank = 2 THEN
        v_event_id := (SELECT id FROM events WHERE name = '2nd Place Global');
        v_price := calculate_event_price(v_user_id, v_event_id);
        v_description := '2nd place globally with accuracy: ' || NEW.accuracy;

INSERT INTO token_history (user_id, amount, event_type, description, training_id)
VALUES (v_user_id, v_price, v_event_id, v_description, NEW.training_id);
ELSIF v_global_rank = 3 THEN
        v_event_id := (SELECT id FROM events WHERE name = '3rd Place Global');
        v_price := calculate_event_price(v_user_id, v_event_id);
        v_description := '3rd place globally with accuracy: ' || NEW.accuracy;

INSERT INTO token_history (user_id, amount, event_type, description, training_id)
VALUES (v_user_id, v_price, v_event_id, v_description, NEW.training_id);
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trigger_update_token_history_after_public_results
    AFTER INSERT ON public_results
    FOR EACH ROW
    EXECUTE FUNCTION update_token_history_after_public_results();

COMMIT;