package bank.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Gives tests their own throwaway SQLite file instead of the real bank.db.
 * @Primary means: wherever the app asks for DatabaseSettings, use this one.
 */
@TestConfiguration
public class TestDatabaseConfig {

    @Bean
    @Primary
    public DatabaseSettings testDatabaseSettings() throws IOException {
        Path file = Files.createTempFile("bank-api-test-", ".db");
        file.toFile().deleteOnExit();
        return new DatabaseSettings("jdbc:sqlite:" + file + "?foreign_keys=on", null, null);
    }
}