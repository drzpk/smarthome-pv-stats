create schema if not exists pv_stats default character set utf8;

create user if not exists ${PV_STATS_DB_USER}@`%` identified by '${PV_STATS_DB_PASSWORD}';
grant all privileges on pv_stats.* to ${PV_STATS_DB_USER}@`%`;