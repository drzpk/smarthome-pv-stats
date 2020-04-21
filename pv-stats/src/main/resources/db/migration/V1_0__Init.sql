create table energy_measurement
(
    id        int       not null auto_increment,
    timestamp timestamp not null,
    total_wh  int       not null,
    delta_wh  int       not null,
    device_id int       not null,
    primary key (id, timestamp)
)
    partition by range (unix_timestamp(`timestamp`))
        (
        partition p0000 values less than (unix_timestamp('2020-01-01 00:00')),
        partition p2020 values less than (unix_timestamp('2021-01-01 00:00')),
        partition p2021 values less than (unix_timestamp('2022-01-01 00:00'))
        );

create table device
(
    id          int          not null,
    name        varchar(128) not null,
    description varchar(255),
    type        varchar(32)  not null,
    active      tinyint(1)   not null,
    api_url     varchar(128) not null,
    primary key (id)
);