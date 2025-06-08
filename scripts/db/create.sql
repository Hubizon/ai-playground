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
DROP FUNCTION IF EXISTS calculate_training_cost CASCADE;
DROP FUNCTION IF EXISTS calculate_event_price CASCADE;
DROP FUNCTION IF EXISTS next_model_version(integer) CASCADE;
DROP FUNCTION IF EXISTS give_basic_role() CASCADE;
DROP FUNCTION IF EXISTS get_user_token_balance(uuid) CASCADE;
DROP FUNCTION IF EXISTS check_user_tokens_before_model_creation() CASCADE;
DROP FUNCTION IF EXISTS update_token_history_afetr_model_creation() CASCADE;
DROP FUNCTION IF EXISTS check_user_tokens_before_training() CASCADE;
DROP FUNCTION IF EXISTS update_token_history_after_training() CASCADE;
DROP FUNCTION IF EXISTS update_token_history_after_public_results() CASCADE;
DROP FUNCTION IF EXISTS update_token_history_after_model_creation() CASCADE;
DROP FUNCTION IF EXISTS ordinal(integer) CASCADE;
DROP FUNCTION IF EXISTS insert_custom_event_price(text, text, numeric) CASCADE;
DROP FUNCTION IF EXISTS insert_token_history_on_new_role() CASCADE;
DROP FUNCTION IF EXISTS one_active_role() CASCADE;

CREATE TABLE currencies
(
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    conversion_rate double precision
);

CREATE TABLE countries
(
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    currency INT REFERENCES currencies DEFAULT 1
);

CREATE TABLE categories
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE optimizers
(
    id    SERIAL PRIMARY KEY,
    name  TEXT NOT NULL,
    price double precision
);

CREATE TABLE loss_functions
(
    id    SERIAL PRIMARY KEY,
    name  TEXT NOT NULL,
    price double precision
);

CREATE TABLE events
(
    id               SERIAL PRIMARY KEY,
    name             VARCHAR(50) NOT NULL,
    base_price       INTEGER,
    training_related boolean     NOT NULL,
    model_related    boolean     NOT NULL
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
    user_id     UUID    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id     INT     NOT NULL REFERENCES roles (id),
    assigned_at TIMESTAMPTZ DEFAULT now(),
    is_active   BOOLEAN NOT NULL,
    PRIMARY KEY (user_id, role_id, assigned_at)
);

CREATE TABLE models
(
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name    VARCHAR(100) NOT NULL
);

CREATE TABLE model_versions
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    model_id       UUID  NOT NULL REFERENCES models (id) ON DELETE CASCADE,
    version_number INT   NOT NULL   DEFAULT 1,
    architecture   JSONB NOT NULL,
    created_at     TIMESTAMPTZ      DEFAULT now()
);

CREATE TABLE datasets
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    category_id INT                 NOT NULL REFERENCES categories (id),
    created_at  TIMESTAMPTZ      DEFAULT now(),
    price       double precision DEFAULT 1,
    path        TEXT
);

CREATE TABLE trainings
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    model_version_id UUID                           NOT NULL REFERENCES model_versions (id) ON DELETE CASCADE,
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
    training_id UUID                           NOT NULL REFERENCES trainings (id) ON DELETE CASCADE,
    epoch       INT                            NOT NULL,
    iter        INT                            NOT NULL,
    loss        DOUBLE PRECISION               NOT NULL,
    accuracy    NUMERIC(6, 3)                  NOT NULL,
    type        TEXT                           NOT NULL,
    timestamp   TIMESTAMPTZ      DEFAULT now() NOT NULL,
    CHECK (epoch >= 0),
    CHECK (accuracy >= 0.0 AND accuracy <= 100.0)
);

CREATE TABLE token_history
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    amount      INT  NOT NULL,
    event_type  INT  NOT NULL REFERENCES events (id),
    description TEXT,
    timestamp   TIMESTAMPTZ      DEFAULT now(),
    training_id UUID REFERENCES trainings ON DELETE CASCADE,
    model_id    UUID REFERENCES models ON DELETE CASCADE
        CHECK (amount != 0)
);

CREATE TABLE public_results
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    training_id UUID NOT NULL REFERENCES trainings (id) ON DELETE CASCADE UNIQUE,
    shared_at   TIMESTAMPTZ      DEFAULT now()
);

CREATE TABLE custom_event_prices
(
    event_id INTEGER REFERENCES events ON DELETE CASCADE,
    role_id  INTEGER REFERENCES roles ON DELETE CASCADE,
    price    INTEGER
);



CREATE OR REPLACE VIEW leaderboards AS
WITH last_test_metrics AS (SELECT DISTINCT ON (training_id) training_id, epoch, accuracy, loss
                           FROM training_metrics
                           WHERE type = 'TEST'
                           ORDER BY training_id, epoch DESC),
     best_per_user_dataset AS (SELECT DISTINCT ON (u.id, d.id) u.id   AS user_id,
                                                               u.username,
                                                               c.name AS country,
                                                               d.id   AS dataset_id,
                                                               d.name AS dataset,
                                                               m.name AS model_name,
                                                               ltm.accuracy,
                                                               ltm.loss
                               FROM public_results pr
                                        JOIN trainings t ON pr.training_id = t.id
                                        JOIN datasets d ON t.dataset_id = d.id
                                        JOIN model_versions mv ON t.model_version_id = mv.id
                                        JOIN models m ON mv.model_id = m.id
                                        JOIN users u ON m.user_id = u.id
                                        JOIN countries c ON u.country_id = c.id
                                        JOIN last_test_metrics ltm ON pr.training_id = ltm.training_id
                               ORDER BY u.id, d.id, ltm.accuracy DESC, ltm.loss)
SELECT RANK() OVER (ORDER BY accuracy DESC, loss) AS position,
       username,
       country,
       dataset,
       accuracy,
       loss
FROM best_per_user_dataset;

CREATE OR REPLACE VIEW best_results AS
SELECT DISTINCT ON (th.user_id, d.id,
    CASE
        WHEN e.name LIKE '%Global' THEN 'global'
        ELSE 'country'
        END) th.user_id,
             d.id          AS dataset_id,
             CASE
                 WHEN e.name LIKE '%Global' THEN 'global'
                 ELSE 'country'
                 END       AS scope,
             th.event_type AS event_id,
             th.amount     AS max_tokens
FROM token_history th
         JOIN events e ON th.event_type = e.id
         JOIN trainings t ON th.training_id = t.id
         JOIN datasets d ON t.dataset_id = d.id
WHERE e.name IN (
                 '1st Place Global', '2nd Place Global', '3rd Place Global',
                 '1st Place Country', '2nd Place Country', '3rd Place Country'
    )
ORDER BY th.user_id, d.id, scope, th.amount DESC;

CREATE OR REPLACE FUNCTION insert_custom_event_price(
    event_name TEXT,
    role_name TEXT,
    price NUMERIC
)
    RETURNS VOID AS
$$
BEGIN
    INSERT INTO custom_event_prices (event_id, role_id, price)
    VALUES ((SELECT id FROM events WHERE name = event_name),
            (SELECT id FROM roles WHERE name = role_name),
            price);
END;
$$ LANGUAGE plpgsql;

Commit;