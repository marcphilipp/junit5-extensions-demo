package demo._02_reusablefixture;

import org.codejargon.fluentjdbc.api.FluentJdbc;
import org.codejargon.fluentjdbc.api.FluentJdbcBuilder;
import org.junit.jupiter.api.Test;
import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.Connection;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ContainerTests {

	private static final String MYSQL_ROOT_PASSWORD = "root";
	private static final String MYSQL_DATABASE = "demo";
	private static final int MYSQL_PORT = 3306;

	@Test
	@Container(image = "mariadb", ports = MYSQL_PORT + ":" + MYSQL_PORT, env = {
		"MYSQL_ROOT_PASSWORD=" + MYSQL_ROOT_PASSWORD,
		"MYSQL_DATABASE=" + MYSQL_DATABASE
	})
	void insertIntoTable() throws Exception {
		FluentJdbc jdbc = connectToDatabase();
		jdbc.query().plainConnection(connection ->
			connection.createStatement()
				.execute("CREATE TABLE example (id BIGINT NOT NULL, name VARCHAR(255) not null)")
		);

		executeCodeUnderTest(jdbc);

		String name = jdbc.query()
				.select("SELECT name FROM example")
				.singleResult(resultSet -> resultSet.getString(1));

		assertEquals("John Doe", name);
	}

	private void executeCodeUnderTest(FluentJdbc jdbc) {
		jdbc.query()
				.update("INSERT INTO example (id, name) VALUES (?, ?)")
				.params(42, "John Doe")
				.run();
	}

	private FluentJdbc connectToDatabase() throws Exception {
		MariaDbDataSource dataSource = new MariaDbDataSource("127.0.0.1", MYSQL_PORT, MYSQL_DATABASE);
		dataSource.setUser("root");
		dataSource.setPassword(MYSQL_ROOT_PASSWORD);
		await().atMost(30, SECONDS)
			.until(() -> {
				try (Connection connection = dataSource.getConnection()) {
					return connection.isValid(1000);
				} catch (Exception e) {
					return false;
				}
			});
		return new FluentJdbcBuilder()
				.connectionProvider(dataSource)
				.build();
	}

}
