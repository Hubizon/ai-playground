DO
$$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'aiplayground') THEN
            CREATE USER aiplayground WITH PASSWORD 'aiplayground';
        END IF;
    END
$$;

CREATE DATABASE aiplayground OWNER aiplayground;
GRANT ALL PRIVILEGES ON DATABASE aiplayground TO aiplayground;
