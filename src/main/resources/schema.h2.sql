CREATE TABLE IF NOT EXISTS task_ (
  task_id_     VARCHAR(255) PRIMARY KEY NOT NULL,
  user_id_     VARCHAR(255),
  timestamp_   TIMESTAMP,
  title_       VARCHAR(255),
  description_ VARCHAR(255),
  done_        BOOLEAN
);
