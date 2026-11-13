package com.nhannkl.db_backup_utility;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

import com.nhannkl.db_backup_utility.exception.BackupException;
import com.nhannkl.db_backup_utility.process.CommandResult;
import com.nhannkl.db_backup_utility.process.CommandRunner;

@SpringBootTest 
public class CommandRunnerTest {
    
    private static final String JAVA =
            Path.of(System.getProperty("java.home"), "bin", "java").toString();

    private final CommandRunner runner = new CommandRunner(Duration.ofSeconds(20));

    @Test
    void capturesStderrAndSuccessExitCode() {
        CommandResult r = runner.run(List.of(JAVA, "-version"), Map.of());

        assertThat(r.isSuccess()).isTrue();
        assertThat(r.stderr()).containsIgnoringCase("version");
        assertThat(r.stdout()).isEmpty();
    }

    @Test
    void reportsNonZeroExitCode() {
        CommandResult r = runner.run(List.of(JAVA, "--no-such-option"), Map.of());

        assertThat(r.isSuccess()).isFalse();
        assertThat(r.exitCode()).isNotZero();
    }

    @Test
    void throwsWhenBinaryDoesNotExist() {
        assertThatThrownBy(() -> runner.run(List.of("no-such-binary-xyz"), Map.of()))
                .isInstanceOf(BackupException.class)
                .hasMessageContaining("no-such-binary-xyz");
    }

    @Test
    void writesStdoutIntoGzipFile(@TempDir Path dir) throws IOException {
        Path target = dir.resolve("out.gz");

        CommandResult r = runner.runToGzipFile(List.of(JAVA, "--list-modules"), Map.of(), target);

        assertThat(r.isSuccess()).isTrue();
        try (var in = new GZIPInputStream(Files.newInputStream(target))) {
            assertThat(new String(in.readAllBytes())).contains("java.base");
        }
    }

    @Test
    void deletesPartialFileWhenCommandFails(@TempDir Path dir) {
        Path target = dir.resolve("out.gz");

        CommandResult r = runner.runToGzipFile(List.of(JAVA, "--no-such-option"), Map.of(), target);

        assertThat(r.isSuccess()).isFalse();
        assertThat(target).doesNotExist();
    }

}
