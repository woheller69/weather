DROP TABLE CITIES_TO_WATCH;
CREATE TABLE CITIES_TO_WATCH (
            cities_to_watch_id INTEGER PRIMARY KEY,
            city_id INTEGER,
            rank INTEGER,
            city_name TEXT,
            country_code TEXT,
            longitude REAL NOT NULL,
            latitude REAL NOT NULL);
