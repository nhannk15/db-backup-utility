package com.nhannkl.db_backup_utility.process;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import com.nhannkl.db_backup_utility.exception.BackupException;

public class CommandRunner {
    
    private final Duration timeout;

    public CommandRunner(Duration timeout) {
        this.timeout = timeout;
    }

    public Process start(List<String> command, Map<String, String> env) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().putAll(env);
        try {
            return processBuilder.start();
        } catch (Exception ex) {
            throw new BackupException("Cannot start " + command.get(0) + ". Is it installed on PATH?", ex);
        }
    }

    private int awaitExit(Process process) {
        try {
            boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BackupException("Command timed out after " + timeout);
            }
            return process.exitValue();
        } catch (InterruptedException exception) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new BackupException("Interrupted while waiting for command ", exception);
        }
    }

    private CompletableFuture<String> readAsync(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            try (inputStream) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
    }

    private void deleteQuietly(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            //--- If fail, nothing can go wrong.
        }
    }

    public CommandResult run(List<String> command, Map<String, String> env) {
        Process process = start(command, env);
        CompletableFuture<String> stdout = readAsync(process.getInputStream());
        CompletableFuture<String> stderr = readAsync(process.getErrorStream());
        int exitCode = awaitExit(process);
        return new CommandResult(exitCode, stdout.join(), stderr.join());
    }

    public CommandResult runToGzipFile(List<String> command, Map<String, String> env, Path filePath) {
        Process process = start(command, env);
        CompletableFuture<String> stderr = readAsync(process.getErrorStream());
        try (OutputStream outputStream = new GZIPOutputStream(new BufferedOutputStream(Files.newOutputStream(filePath)))) {
            process.getInputStream().transferTo(outputStream);
        } catch (IOException ex) {
            process.destroyForcibly();
            deleteQuietly(filePath);
            throw new BackupException("Failed while writing " + filePath, ex);
        }

        int exitCode = awaitExit(process);
        if (exitCode != 0) {
            deleteQuietly(filePath);
        }

        return new CommandResult(exitCode, "", stderr.join());
    }

}
