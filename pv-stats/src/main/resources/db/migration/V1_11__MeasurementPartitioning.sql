alter table energy_measurement
    add partition (
        partition p2022 values less than (unix_timestamp('2023-01-01 00:00')),
        partition p2023 values less than (unix_timestamp('2024-01-01 00:00')),
        partition p2024 values less than (unix_timestamp('2025-01-01 00:00')),
        partition p2025 values less than (unix_timestamp('2026-01-01 00:00')),
        partition p2026 values less than (unix_timestamp('2027-01-01 00:00')),
        partition p2027 values less than (unix_timestamp('2028-01-01 00:00')),
        partition p2028 values less than (unix_timestamp('2029-01-01 00:00')),
        partition p2029 values less than (unix_timestamp('2030-01-01 00:00')),
        partition p2030 values less than (unix_timestamp('2031-01-01 00:00')),
        partition plast values less than (2147483647)
        );