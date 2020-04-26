alter table device
    add column created_at datetime not null default now() after type;