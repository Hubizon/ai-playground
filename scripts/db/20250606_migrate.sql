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

COMMIT;