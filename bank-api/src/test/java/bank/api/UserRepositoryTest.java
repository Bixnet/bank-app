package bank.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest {

	// JUnit creates a fresh, empty folder for every test and deletes it afterwards
	@TempDir
	Path tempDir;

	private UserRepository repo;

	@BeforeEach
	void setUp() {
		String url = "jdbc:sqlite:" + tempDir.resolve("test.db") + "?foreign_keys=on";
		repo = new UserRepository(new DatabaseSettings(url, null, null));
		repo.createTables();
	}

	@Test
	void createdUserCanBeFoundAgain() {
		assertTrue(repo.create(new User("anna", "hash-1", User.ROLE_USER)));

		User found = repo.findByUsername("anna").orElseThrow();
		assertEquals("anna", found.username());
		assertEquals("hash-1", found.passwordHash());
		assertEquals(User.ROLE_USER, found.role());
	}

	@Test
	void unknownUserIsNotFound() {
		assertTrue(repo.findByUsername("nobody").isEmpty());
	}

	@Test
	void duplicateUsernameIsRejectedAndOriginalKept() {
		repo.create(new User("anna", "hash-1", User.ROLE_USER));

		assertFalse(repo.create(new User("anna", "hash-2", User.ROLE_ADMIN)));
		assertEquals("hash-1", repo.findByUsername("anna").orElseThrow().passwordHash());
	}

	@Test
	void ownersAreStoredPerUser() {
		repo.create(new User("anna", "hash-1", User.ROLE_USER));
		repo.create(new User("max", "hash-2", User.ROLE_USER));
		repo.addOwner("anna", "654321");
		repo.addOwner("anna", "654321"); // twice on purpose: must not create a duplicate
		repo.addOwner("max", "123456");

		assertEquals(List.of("654321"), repo.findAccountNumbersOwnedBy("anna"));
		assertEquals(List.of("123456"), repo.findAccountNumbersOwnedBy("max"));
	}

	@Test
	void ownerMustBeAnExistingUser() {
		assertThrows(IllegalStateException.class, () -> repo.addOwner("ghost", "654321"));
	}

	@Test
	void createTablesCanRunTwice() {
		repo.create(new User("anna", "hash-1", User.ROLE_USER));

		repo.createTables(); // runs on every app start, must not wipe existing data
		assertTrue(repo.findByUsername("anna").isPresent());
	}

	@Test
	void toStringHidesSecrets() {
		String text = new User("anna", "secret-hash", User.ROLE_USER).toString()
				+ new DatabaseSettings("jdbc:x", "u", "secret-pw").toString();
		assertFalse(text.contains("secret"));
	}
}