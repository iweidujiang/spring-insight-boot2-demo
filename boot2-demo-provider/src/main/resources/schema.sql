CREATE TABLE IF NOT EXISTS demo_item (
    id    BIGINT PRIMARY KEY,
    name  VARCHAR(64) NOT NULL,
    stock INT NOT NULL DEFAULT 0
);

CREATE ALIAS IF NOT EXISTS SLEEP_MS FOR "io.github.iweidujiang.boot2demo.provider.jdbc.DemoH2Functions.sleepMs";
