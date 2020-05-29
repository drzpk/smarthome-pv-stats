alter table device_data
    add column updated_at timestamp default current_timestamp() after value;