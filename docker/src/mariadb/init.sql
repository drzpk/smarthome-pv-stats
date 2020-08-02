create schema if not exists pv_stats default character set utf8;

create user if not exists ${PV_STATS_DB_USER}@`%` identified by '${PV_STATS_DB_PASSWORD}';

revoke all privileges, grant option from ${PV_STATS_DB_USER}@`%`;
flush privileges;

grant all privileges on pv_stats.* to ${PV_STATS_DB_USER}@`%`;
grant create user on `data\_%`.* to ${PV_STATS_DB_USER}@`%`;
grant grant option on `data\_%`.* to ${PV_STATS_DB_USER}@`%`;
flush privileges;