alter table data_source
    drop foreign key fk_data_source__device_id;

alter table device
    modify id int not null auto_increment;
alter table device
    auto_increment = 10;

alter table data_source
    add constraint fk_data_source__device_id foreign key (device_id) references device (id)
