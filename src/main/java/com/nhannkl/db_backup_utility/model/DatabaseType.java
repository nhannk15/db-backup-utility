package com.nhannkl.db_backup_utility.model;

public enum DatabaseType {
    MYSQL(3306),
    POSTGRESQL(5432),
    MONGODB(27017),
    SQLITE(0);

    private final int defaultPort;

    DatabaseType(int defaultPort) {
        this.defaultPort = defaultPort;
    }

    public int getDefaultPort() {
        return this.defaultPort;
    }
}
