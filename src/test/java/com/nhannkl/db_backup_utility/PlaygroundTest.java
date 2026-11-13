package com.nhannkl.db_backup_utility;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.nhannkl.db_backup_utility.process.CommandResult;
import com.nhannkl.db_backup_utility.process.CommandRunner;

@SpringBootTest
public class PlaygroundTest {
    @Test
    void test() throws IOException {
        var runner = new CommandRunner(Duration.ofSeconds(60));

        // Thử 1: lệnh đơn giản
        // CommandResult r1 = runner.run(List.of("docker", "--version"), Map.of());
        // System.out.println("exit=" + r1.exitCode() + " stdout=" + r1.stdout());

        // Thử 2: lệnh sai, xem exit code và stderr
        // CommandResult r2 = runner.run(
        //         List.of("docker", "exec", "bk-mysql", "mysqldump", "-uroot", "-pWRONG", "shop"), Map.of());
        // System.out.println("exit=" + r2.exitCode() + " stderr=" + r2.stderr());

        // Thử 3: dump thật ra file .gz
        Path target = Path.of("backups", "shop-java.sql.gz");
        Files.createDirectories(target.getParent());
        CommandResult r3 = runner.runToGzipFile(
                List.of("docker", "exec", "-e", "MYSQL_PWD=21012006",
                        "bk-mysql", "mysqldump", "-uroot", "shop"),
                Map.of(), target);

        System.out.println("exit=" + r3.exitCode());
        System.out.println("stderr=" + r3.stderr());
        if (r3.isSuccess()) {
            System.out.println("size=" + Files.size(target) + " bytes");
        } else {
            System.out.println("Dump FAILED, file was deleted. Read stderr above.");
        }
    }
}
