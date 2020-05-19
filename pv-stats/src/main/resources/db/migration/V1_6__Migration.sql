create table migration
(
    id                int         not null auto_increment,
    name              varchar(64) not null,
    executed_at       timestamp   not null,
    execution_time_ms int         not null,
    status            tinyint(1)  not null,
    primary key (id)
);