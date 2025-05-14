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
    version_number INT   NOT NULL,
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
            RAISE EXCEPTION 'Pierwsza wersja modelu musi mieć numer 1. Podano: %', NEW.version_number;
        END IF;
    ELSE
        IF NEW.version_number <> max_version + 1 THEN
            RAISE EXCEPTION 'Numer wersji modelu % dla modelu % musi być % (następny po %), a podano %',
                NEW.version_number, NEW.model_id, max_version + 1, max_version, NEW.version_number;
        END IF;
    END IF;

    IF NEW.version_number <= 0 THEN
        RAISE EXCEPTION 'Numer wersji modelu musi być większy od 0.';
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
VALUES ('Polska'),
       ('Niemcy'),
       ('USA'),
       ('Wielka Brytania');

INSERT INTO categories (name)
VALUES ('Rozpoznawanie Obrazów'),
       ('Przetwarzanie Języka Naturalnego'),
       ('Analiza Szeregów Czasowych'),
       ('Systemy Rekomendacyjne');

INSERT INTO optimizers (name)
VALUES ('Adam'),
       ('SGD'),
       ('RMSprop'),
       ('Adagrad');

INSERT INTO loss_functions (name)
VALUES ('Cross-Entropy'),
       ('Mean Squared Error'),
       ('Harmonic Loss'),
       ('Binary Cross-Entropy');

INSERT INTO events (name, is_income)
VALUES ('Początkowe Tokeny', TRUE),
       ('Bonus za wysoką pozycję w leaderboardzie', TRUE),
       ('Koszt Utworzenia Modelu', FALSE),
       ('Koszt Uruchomienia Treningu', FALSE),
       ('Koszt Uploadu Zestawu Danych', FALSE);

INSERT INTO statuses (name, description)
VALUES ('Oczekujący', 'Trening czeka na rozpoczęcie.'),
       ('W Trakcie', 'Trening jest aktualnie uruchomiony.'),
       ('Ukończony', 'Trening zakończył się pomyślnie.'),
       ('Niepowodzenie', 'Trening zakończył się błędem.'),
       ('Anulowany', 'Trening został anulowany przez użytkownika lub system.');

INSERT INTO roles (name, initial_tokens)
VALUES ('Użytkownik Podstawowy', 1000),
       ('Użytkownik Premium', 5000),
       ('Administrator', 99999);

WITH inserted_users AS (
    INSERT INTO users (username, first_name, last_name, email, password_hash, country_id,
                       birth_date) VALUES ('jan_kowalski', 'Jan', 'Kowalski', 'jan.kowalski@example.com',
                                           'hashed_password_jan', 1, '1990-05-15'),
                                          ('anna_nowak', 'Anna', 'Nowak', 'anna.nowak@example.com',
                                           'hashed_password_anna', 1, '1988-11-30'),
                                          ('piotr_zielinski', 'Piotr', 'Zielinski', 'piotr.zielinski@example.com',
                                           'hashed_password_piotr', 2, '1995-07-22'),
                                          ('admin_system', 'Admin', 'Systemowy', 'admin@company.com',
                                           'hashed_password_admin', 3, '1975-01-01')
        RETURNING id, username)

INSERT
INTO user_roles (user_id, role_id, is_active)
VALUES ((SELECT id FROM inserted_users WHERE username = 'jan_kowalski'),
        (SELECT id FROM roles WHERE name = 'Użytkownik Podstawowy'), TRUE),
       ((SELECT id FROM inserted_users WHERE username = 'anna_nowak'),
        (SELECT id FROM roles WHERE name = 'Użytkownik Podstawowy'), TRUE),
       ((SELECT id FROM inserted_users WHERE username = 'piotr_zielinski'),
        (SELECT id FROM roles WHERE name = 'Użytkownik Premium'), TRUE),
       ((SELECT id FROM inserted_users WHERE username = 'admin_system'),
        (SELECT id FROM roles WHERE name = 'Administrator'), TRUE),
       ((SELECT id FROM inserted_users WHERE username = 'anna_nowak'),
        (SELECT id FROM roles WHERE name = 'Użytkownik Premium'), FALSE); -- Przykład nieaktywnej roli

WITH inserted_models AS (
    INSERT INTO models (user_id, name) VALUES ((SELECT id FROM users WHERE username = 'jan_kowalski'),
                                               'Mój Pierwszy Konwolucyjny'),
                                              ((SELECT id FROM users WHERE username = 'anna_nowak'),
                                               'Model NLP do Analizy Sentymenów'),
                                              ((SELECT id FROM users WHERE username = 'piotr_zielinski'),
                                               'Model Regresji Czasowej')
        RETURNING id, name, user_id)

INSERT
INTO model_versions (model_id, version_number, architecture)
VALUES ((SELECT id
         FROM inserted_models
         WHERE name = 'Mój Pierwszy Konwolucyjny'
           AND user_id = (SELECT id FROM users WHERE username = 'jan_kowalski')), 1, '{
  "type": "CNN",
  "layers": [
    {
      "layer": "conv2d",
      "filters": 32
    },
    {
      "layer": "relu"
    },
    {
      "layer": "maxpool"
    }
  ]
}'::jsonb),
       ((SELECT id
         FROM inserted_models
         WHERE name = 'Mój Pierwszy Konwolucyjny'
           AND user_id = (SELECT id FROM users WHERE username = 'jan_kowalski')), 2, '{
         "type": "CNN",
         "layers": [
           {
             "layer": "conv2d",
             "filters": 64
           },
           {
             "layer": "relu"
           },
           {
             "layer": "conv2d",
             "filters": 64
           },
           {
             "layer": "relu"
           },
           {
             "layer": "maxpool"
           }
         ]
       }'::jsonb),
       ((SELECT id
         FROM inserted_models
         WHERE name = 'Model NLP do Analizy Sentymenów'
           AND user_id = (SELECT id FROM users WHERE username = 'anna_nowak')), 1, '{
         "type": "Transformer",
         "encoder_layers": 6,
         "decoder_layers": 6,
         "attention_heads": 8
       }'::jsonb),
       ((SELECT id
         FROM inserted_models
         WHERE name = 'Model Regresji Czasowej'
           AND user_id = (SELECT id FROM users WHERE username = 'piotr_zielinski')), 1, '{
         "type": "LSTM",
         "units": 128,
         "return_sequences": true
       }'::jsonb);

WITH inserted_datasets AS (
    INSERT INTO datasets (name, description, category_id) VALUES ('CIFAR-10',
                                                                  'Zestaw danych obrazów 32x32 do klasyfikacji na 10 klas.',
                                                                  (SELECT id FROM categories WHERE name = 'Rozpoznawanie Obrazów')),
                                                                 ('IMDB Reviews',
                                                                  'Duży binarny zestaw danych do analizy sentymentu.',
                                                                  (SELECT id
                                                                   FROM categories
                                                                   WHERE name = 'Przetwarzanie Języka Naturalnego')),
                                                                 ('Monthly Milk Production',
                                                                  'Miesięczna produkcja mleka w USA od 1962 do 1975.',
                                                                  (SELECT id FROM categories WHERE name = 'Analiza Szeregów Czasowych'))
        RETURNING id, name)

INSERT
INTO token_history (user_id, amount, event_type, description)
VALUES ((SELECT id FROM users WHERE username = 'jan_kowalski'),
        (SELECT initial_tokens FROM roles WHERE name = 'Użytkownik Podstawowy'), 1,
        'Początkowe tokeny za założenie konta'),
       ((SELECT id FROM users WHERE username = 'anna_nowak'),
        (SELECT initial_tokens FROM roles WHERE name = 'Użytkownik Podstawowy'), 1,
        'Początkowe tokeny za założenie konta'),
       ((SELECT id FROM users WHERE username = 'piotr_zielinski'),
        (SELECT initial_tokens FROM roles WHERE name = 'Użytkownik Premium'), 1,
        'Początkowe tokeny za założenie konta premium'),
       ((SELECT id FROM users WHERE username = 'admin_system'),
        (SELECT initial_tokens FROM roles WHERE name = 'Administrator'), 1, 'Początkowe tokeny dla administratora'),

       ((SELECT id FROM users WHERE username = 'jan_kowalski'), -50, 3,
        'Koszt utworzenia modelu: Mój Pierwszy Konwolucyjny'),
       ((SELECT id FROM users WHERE username = 'anna_nowak'), -75, 3,
        'Koszt utworzenia modelu: Model NLP do Analizy Sentymenów'),
       ((SELECT id FROM users WHERE username = 'piotr_zielinski'), -60, 3,
        'Koszt utworzenia modelu: Model Regresji Czasowej'),

       ((SELECT id FROM users WHERE username = 'jan_kowalski'), -10, 5, 'Koszt użycia zestawu danych: CIFAR-10'),
       ((SELECT id FROM users WHERE username = 'anna_nowak'), -12, 5, 'Koszt użycia zestawu danych: IMDB Reviews');


INSERT INTO trainings (model_version_id, dataset_id, learning_rate, optimizer, loss_function, status, started_at,
                       finished_at)
VALUES ((SELECT id
         FROM model_versions
         WHERE model_id = (SELECT id
                           FROM models
                           WHERE name = 'Mój Pierwszy Konwolucyjny'
                             AND user_id = (SELECT id FROM users WHERE username = 'jan_kowalski'))
           AND version_number = 1),
        (SELECT id FROM datasets WHERE name = 'CIFAR-10'),
        1,
        (SELECT id FROM optimizers WHERE name = 'Adam'),
        (SELECT id FROM loss_functions WHERE name = 'Cross-Entropy'),
        3,
        now() - INTERVAL '2 days',
        now() - INTERVAL '1 day'),
       ((SELECT id
         FROM model_versions
         WHERE model_id = (SELECT id
                           FROM models
                           WHERE name = 'Model NLP do Analizy Sentymenów'
                             AND user_id = (SELECT id FROM users WHERE username = 'anna_nowak'))
           AND version_number = 1),
        (SELECT id FROM datasets WHERE name = 'IMDB Reviews'),
        5,
        (SELECT id FROM optimizers WHERE name = 'SGD'),
        (SELECT id FROM loss_functions WHERE name = 'Binary Cross-Entropy'),
        2,
        now() - INTERVAL '6 hours',
        NULL -- jeszcze się nie zakończył
       ),
       ((SELECT id
         FROM model_versions
         WHERE model_id = (SELECT id
                           FROM models
                           WHERE name = 'Mój Pierwszy Konwolucyjny'
                             AND user_id = (SELECT id FROM users WHERE username = 'jan_kowalski'))
           AND version_number = 2),
        (SELECT id FROM datasets WHERE name = 'CIFAR-10'),
        3,
        (SELECT id FROM optimizers WHERE name = 'RMSprop'),
        (SELECT id FROM loss_functions WHERE name = 'Cross-Entropy'),
        3,
        now() - INTERVAL '1 day',
        now() - INTERVAL '23 hours'),
       ((SELECT id
         FROM model_versions
         WHERE model_id = (SELECT id
                           FROM models
                           WHERE name = 'Model Regresji Czasowej'
                             AND user_id = (SELECT id FROM users WHERE username = 'piotr_zielinski'))
           AND version_number = 1),
        (SELECT id FROM datasets WHERE name = 'Monthly Milk Production'),
        1,
        (SELECT id FROM optimizers WHERE name = 'Adam'),
        (SELECT id FROM loss_functions WHERE name = 'Mean Squared Error'),
        1,
        now() + INTERVAL '1 hour',
        NULL);


INSERT INTO training_metrics (training_id, epoch, loss, accuracy)
SELECT T.id,
       metrics.epoch,
       metrics.loss,
       metrics.accuracy
FROM trainings T
         JOIN
     (VALUES ((SELECT id
               FROM model_versions
               WHERE model_id = (SELECT id
                                 FROM models
                                 WHERE name = 'Mój Pierwszy Konwolucyjny'
                                   AND user_id = (SELECT id FROM users WHERE username = 'jan_kowalski'))
                 AND version_number = 1), (SELECT id FROM datasets WHERE name = 'CIFAR-10'), 0, 1.5, 0.15),
             ((SELECT id
               FROM model_versions
               WHERE model_id = (SELECT id
                                 FROM models
                                 WHERE name = 'Mój Pierwszy Konwolucyjny'
                                   AND user_id = (SELECT id FROM users WHERE username = 'jan_kowalski'))
                 AND version_number = 1), (SELECT id FROM datasets WHERE name = 'CIFAR-10'), 5, 0.8, 0.65),
             ((SELECT id
               FROM model_versions
               WHERE model_id = (SELECT id
                                 FROM models
                                 WHERE name = 'Mój Pierwszy Konwolucyjny'
                                   AND user_id = (SELECT id FROM users WHERE username = 'jan_kowalski'))
                 AND version_number = 1), (SELECT id FROM datasets WHERE name = 'CIFAR-10'), 10, 0.4, 0.85),
             ((SELECT id
               FROM model_versions
               WHERE model_id = (SELECT id
                                 FROM models
                                 WHERE name = 'Model NLP do Analizy Sentymenów'
                                   AND user_id = (SELECT id FROM users WHERE username = 'anna_nowak'))
                 AND version_number = 1), (SELECT id FROM datasets WHERE name = 'IMDB Reviews'), 0, 0.7, 0.5),
             ((SELECT id
               FROM model_versions
               WHERE model_id = (SELECT id
                                 FROM models
                                 WHERE name = 'Model NLP do Analizy Sentymenów'
                                   AND user_id = (SELECT id FROM users WHERE username = 'anna_nowak'))
                 AND version_number = 1), (SELECT id FROM datasets WHERE name = 'IMDB Reviews'), 2, 0.6, 0.6),
             ((SELECT id
               FROM model_versions
               WHERE model_id = (SELECT id
                                 FROM models
                                 WHERE name = 'Mój Pierwszy Konwolucyjny'
                                   AND user_id = (SELECT id FROM users WHERE username = 'jan_kowalski'))
                 AND version_number = 2), (SELECT id FROM datasets WHERE name = 'CIFAR-10'), 0, 1.6, 0.1),
             ((SELECT id
               FROM model_versions
               WHERE model_id = (SELECT id
                                 FROM models
                                 WHERE name = 'Mój Pierwszy Konwolucyjny'
                                   AND user_id = (SELECT id FROM users WHERE username = 'jan_kowalski'))
                 AND version_number = 2), (SELECT id FROM datasets WHERE name = 'CIFAR-10'), 1, 2.5, 0.05))
         AS metrics (model_version_id, dataset_id, epoch, loss, accuracy)
     ON T.model_version_id = metrics.model_version_id AND T.dataset_id = metrics.dataset_id
WHERE T.status != 1;

INSERT INTO public_results (training_id, accuracy, loss)
SELECT T.id,
       metrics.accuracy,
       metrics.loss
FROM trainings T
         JOIN (VALUES ((SELECT id
                        FROM model_versions
                        WHERE model_id =
                              (SELECT id
                               FROM models
                               WHERE name = 'Mój Pierwszy Konwolucyjny'
                                 AND user_id =
                                     (SELECT id
                                      FROM users
                                      WHERE username = 'jan_kowalski'))
                          AND version_number = 1),
                       (SELECT id
                        FROM datasets
                        WHERE name = 'CIFAR-10'),
                       0.85, 0.4)) AS metrics (model_version_id, dataset_id, accuracy, loss)
              ON T.model_version_id = metrics.model_version_id AND T.dataset_id = metrics.dataset_id
WHERE T.status = 3;

INSERT INTO token_history (user_id, amount, event_type, description, timestamp)
SELECT U.id                                                                                       AS user_id,
       -10                                                                                        AS amount,
       3                                                                                          AS event_type,
       'Koszt uruchomienia treningu: ' || M.name || ' v' || MV.version_number || ' na ' || D.name AS description,
       T.started_at
FROM trainings T
         JOIN model_versions MV ON T.model_version_id = MV.id
         JOIN models M ON MV.model_id = M.id
         JOIN users U ON M.user_id = U.id
         JOIN datasets D ON T.dataset_id = D.id
WHERE T.status != 1;

ALTER TABLE datasets
    ADD COLUMN path TEXT;
UPDATE datasets
SET path = 'datasets/' || name;

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