# Former storage mechanism was flawed and multiple exact pairs of (device_id, property) could be stored.
# Delete all such old duplicates.
delete
from device_data
where (device_id, property, updated_at) not in (
    select device_id, property, max(updated_at)
    from device_data
    group by device_id, property
);

alter table device_data
    drop primary key,
    drop column id,
    add primary key (device_id, property);

