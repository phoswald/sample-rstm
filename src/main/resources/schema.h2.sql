
CREATE TABLE IF NOT EXISTS task_ (
    task_id_        VARCHAR(32) PRIMARY KEY NOT NULL,
    user_id_        VARCHAR(32) NOT NULL,
    timestamp_      TIMESTAMP NOT NULL,
    title_          VARCHAR NOT NULL,
    description_    VARCHAR NULL,
    done_           BOOLEAN NULL
);

CREATE TABLE IF NOT EXISTS location_ (
    user_id_        VARCHAR(32) NOT NULL,
    timestamp_      TIMESTAMP NOT NULL,
    latitude_       DOUBLE PRECISION NOT NULL,
    longitude_      DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (user_id_, timestamp_)
);

CREATE TABLE IF NOT EXISTS location_perm_ (
    user_id_        VARCHAR(32) NOT NULL,
    user_id_view_   VARCHAR(32) NOT NULL,
    PRIMARY KEY (user_id_, user_id_view_)
);

CREATE TABLE IF NOT EXISTS user_ (
    username_       VARCHAR(32) PRIMARY KEY NOT NULL,
    password_       VARCHAR NULL,
    roles_          VARCHAR NULL
);
