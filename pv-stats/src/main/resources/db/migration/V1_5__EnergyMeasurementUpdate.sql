alter table energy_measurement
    add column power_w int not null default 0 after delta_wh;

create table device_data
(
    id        int          not null auto_increment,
    device_id int          not null,
    property  varchar(32)  not null,
    value     varchar(128) not null,
    primary key (id),
    foreign key fk__device_cache_device_id (device_id) references device (id)
);