
CREATE TABLE task_ (
    task_id_        VARCHAR(32) NOT NULL,
    user_id_        VARCHAR(32) NOT NULL,
    timestamp_      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    title_          VARCHAR NOT NULL,
    description_    VARCHAR NULL,
    done_           BOOLEAN NULL
);

ALTER TABLE task_ ADD CONSTRAINT task_pk_ PRIMARY KEY (task_id_);

CREATE TABLE location_ (
    user_id_        VARCHAR(32) NOT NULL,
    timestamp_      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    latitude_       DOUBLE PRECISION NOT NULL,
    longitude_      DOUBLE PRECISION NOT NULL
);

ALTER TABLE location_ ADD CONSTRAINT location_pk_ PRIMARY KEY (user_id_, timestamp_);

CREATE TABLE location_perm_ (
    user_id_        VARCHAR(32) NOT NULL,
    user_id_view_   VARCHAR(32) NOT NULL
);

ALTER TABLE location_perm_ ADD CONSTRAINT location_perm_pk_ PRIMARY KEY (user_id_, user_id_view_);

CREATE TABLE user_ (
    username_       VARCHAR(32) NOT NULL,
    password_       VARCHAR NULL,
    roles_          VARCHAR NULL
);

ALTER TABLE user_ ADD CONSTRAINT user_pk_ PRIMARY KEY (username_);
