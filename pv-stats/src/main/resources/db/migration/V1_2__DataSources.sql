create table data_source
(
    id                 int          not null auto_increment,
    user               varchar(64)  not null,
    password           varchar(128) not null,
    device_id          int          not null,
    created_at         datetime     not null,
    updated_at_version smallint     not null,
    primary key (id),
    constraint fk_data_source__device_id foreign key (device_id) references device (id)
);