
create table task_ (
    task_id_        varchar(32) not null,
    user_id_        varchar(32) not null,
    timestamp_      timestamp without time zone not null,
    title_          varchar not null,
    description_    varchar null,
    done_           boolean null
);

alter table task_ add constraint task_pk_ primary key (task_id_);

create table location_ (
    user_id_        varchar(32) not null,
    timestamp_      timestamp without time zone not null,
    latitude_       double precision not null,
    longitude_      double precision not null
);

alter table location_ add constraint location_pk_ primary key (user_id_, timestamp_);

create table location_perm_ (
    user_id_        varchar(32) not null,
    user_id_view_   varchar(32) not null
);

alter table location_perm_ add constraint location_perm_pk_ primary key (user_id_, user_id_view_);

create table user_ (
    username_       varchar(32) not null,
    password_       varchar null,
    roles_          varchar null
);

alter table user_ add constraint user_pk_ primary key (username_);
