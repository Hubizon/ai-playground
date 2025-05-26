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
DROP FUNCTION IF EXISTS check_sequential_model_version CASCADE;
DROP TRIGGER IF EXISTS enforce_sequential_model_version ON model_versions CASCADE;

CREATE TABLE countries
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE categories
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE optimizers
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE loss_functions
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE events
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(50) NOT NULL,
    is_income BOOLEAN     NOT NULL
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
    created_at  TIMESTAMPTZ      DEFAULT now()
);

CREATE TABLE token_history
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users (id),
    amount      INT  NOT NULL,
    event_type  INT  NOT NULL REFERENCES events (id),
    description TEXT,
    timestamp   TIMESTAMPTZ      DEFAULT now(),
    CHECK (amount != 0)
);

CREATE TABLE trainings
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    model_version_id UUID                           NOT NULL REFERENCES model_versions (id),
    dataset_id       UUID                           NOT NULL REFERENCES datasets (id),
    learning_rate    REAL                           NOT NULL,
    optimizer        INT                            NOT NULL REFERENCES optimizers (id),
    loss_function    INT                            NOT NULL REFERENCES loss_functions (id),
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

CREATE TABLE public_results
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    training_id UUID             NOT NULL REFERENCES trainings (id),
    accuracy    DOUBLE PRECISION NOT NULL,
    loss        DOUBLE PRECISION NOT NULL,
    shared_at   TIMESTAMPTZ      DEFAULT now(),
    CHECK (accuracy >= 0.0 AND accuracy <= 1.0)
);

INSERT INTO countries (name)
VALUES ('Poland'),
       ('Germany'),
       ('USA'),
       ('United Kingdom'),
       ('France'),
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

INSERT INTO optimizers (name)
VALUES ('Adam'),
       ('SGD'),
       ('RMSProp'),
       ('AdaDelta'),
       ('AdaGrad');

INSERT INTO loss_functions (name)
VALUES ('Mean Squared Error'),
       ('Harmonic Loss'),
       ('Binary Cross-Entropy'),
       ('Cross-Entropy');

INSERT INTO events (name, is_income)
VALUES ('3rd Place Global', TRUE),
       ('3rd Place Country', TRUE),
       ('2nd Place Global', TRUE),
       ('2nd Place Country', TRUE),
       ('1st Place Global', TRUE),
       ('1st Place Country', TRUE),
       ('Model Creation', FALSE),
       ('Saving New Model Version', FALSE),
       ('Model Training', FALSE),
       ('Model Stopping', FALSE),
       ('Application Login', FALSE);

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
                                           '2000-01-01')
        RETURNING id, username)

INSERT
INTO user_roles (user_id, role_id, is_active)
VALUES ((SELECT id FROM inserted_users WHERE username = 'admin'),
        (SELECT id FROM roles WHERE name = 'Administrator'), TRUE);

INSERT INTO datasets (name, description, category_id)
VALUES ('Iris',
        'A classic dataset for classification, containing 3 classes of 50 instances each, where each class refers to a type of iris plant.',
        (SELECT id FROM categories WHERE name = 'Tabular Data')),
       ('Moons',
        'A synthetic dataset for binary classification, shaped like two interleaving half-circles.',
        (SELECT id FROM categories WHERE name = 'Tabular Data')),
       ('Blobs',
        'A synthetic dataset for clustering, consisting of isotropic Gaussian blobs.',
        (SELECT id FROM categories WHERE name = 'Tabular Data')),
       ('Circles',
        'A synthetic dataset for binary classification, shaped like two concentric circles.',
        (SELECT id FROM categories WHERE name = 'Tabular Data')),
       ('MNIST',
        'A large database of handwritten digits commonly used for training image processing systems.',
        (SELECT id FROM categories WHERE name = 'Image Recognition'));

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

COMMIT;