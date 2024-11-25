-- Step 1: Create the user if it does not exist
DO
$do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_roles WHERE rolname = 'm2m'
    ) THEN
        CREATE ROLE m2m LOGIN CREATEDB PASSWORD 'm2m';
    END IF;
END
$do$;

-- Step 2: Create the database if it does not exist
-- Note: This must be outside a DO block or transaction, hence the workaround
SELECT 'CREATE DATABASE m2mdb OWNER m2m'
WHERE NOT EXISTS (
    SELECT FROM pg_database WHERE datname = 'm2mdb'
)\gexec

-- Step 3: Connect to the database
\connect m2mdb m2m

-- Step 4: Create tables
DO
$do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'users'
    ) THEN
        CREATE TABLE Users (
            username VARCHAR(30) PRIMARY KEY,
            password BYTEA
        );
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'friends'
    ) THEN
        CREATE TABLE Friends (
            sender VARCHAR REFERENCES Users(username),
            receiver VARCHAR REFERENCES Users(username),
            state VARCHAR(10),
            PRIMARY KEY (sender, receiver)
        );
    END IF;
END
$do$;