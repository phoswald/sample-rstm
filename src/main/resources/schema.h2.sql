
create table if not exists task_ (
  task_id_          varchar(32) primary key not null,
  user_id_          varchar(32) not null,
  timestamp_        timestamp not null,
  title_            varchar not null,
  description_      varchar null,
  done_             boolean null
);

create table if not exists location_ (
  user_id_          varchar(32) not null,
  timestamp_        timestamp not null,
  latitude_         double precision not null,
  longitude_        double precision not null,
  primary key (user_id_, timestamp_)
);

create table if not exists location_perm_ (
  user_id_          varchar(32) not null,
  user_id_view_     varchar(32) not null,
  primary key (user_id_, user_id_view_)
);

create table if not exists user_ (
  username_         varchar(32) primary key not null,
  password_         varchar null,
  roles_            varchar null
);
