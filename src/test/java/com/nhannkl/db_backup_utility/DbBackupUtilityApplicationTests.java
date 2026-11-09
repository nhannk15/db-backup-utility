package com.nhannkl.db_backup_utility;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.nhannkl.db_backup_utility.model.ConnectionConfig;
import com.nhannkl.db_backup_utility.model.DatabaseType;

@SpringBootTest
class DbBackupUtilityApplicationTests {

	@Test
	void contextLoads() {
	}

    @Test 
    void appliesDefaultHostAndPort() {
        var connection = new ConnectionConfig(DatabaseType.MYSQL, null, 0, "root", "21012006", "shop");
        assertThat(connection.host()).isEqualTo("localhost");
        assertThat(connection.port()).isEqualTo(3306);
    }

    @Test
    void rejectsBlankDatabaseName() {
        assertThatThrownBy(() ->
                new ConnectionConfig(DatabaseType.MYSQL, "h", 1, "u", "p", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toStringHidesPassword() {
        var connection = new ConnectionConfig(DatabaseType.POSTGRESQL, "h", 1, "u", "supersecret", "db");
        assertThat(connection.toString()).doesNotContain("supersecret");
    }

}
