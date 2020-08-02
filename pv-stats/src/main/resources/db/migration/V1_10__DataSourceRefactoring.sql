alter table device
    add constraint uq_device__name unique (name);

# User naming schema has been changed and existing users have to be deleted
select group_concat('\'', user, '\'@\'', host, '\'')
into @usersToDelete
FROM mysql.user
WHERE user like 'viewer_%';
set @usersToDelete = concat('drop user ', @usersToDelete);
prepare stmt from @usersToDelete;
execute stmt;
flush privileges;

# Existing data isn't compatible with new table layout and needs to be recreated
truncate table data_source;

alter table data_source
    add column `schema` varchar(64) not null after `id`,
    add constraint uq_data_source__schema unique (`schema`),
    add constraint uq_data_source__user unique (`user`);