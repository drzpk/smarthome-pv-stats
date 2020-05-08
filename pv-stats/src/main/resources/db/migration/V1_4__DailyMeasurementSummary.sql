create table energy_measurement_daily_summary
(
    id         int      not null auto_increment,
    created_at date     not null,
    total_wh   int      not null,
    delta_wh   int      not null,
    avg_power  float(3) not null,
    max_power  int      not null,
    device_id  int      not null,
    primary key (id),
    index i_energy_measurement_ds_created_at (created_at),
    constraint fk_energy_measurement_ds_device_id foreign key (device_id) references device (id)
);
