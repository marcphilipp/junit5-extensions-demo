package demo._03_parameterresolution;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.NetworkSettings;
import com.github.dockerjava.api.model.Ports.Binding;
import demo._02_reusablefixture.Container;
import org.codejargon.fluentjdbc.api.FluentJdbc;
import org.codejargon.fluentjdbc.api.FluentJdbcBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mariadb.jdbc.MariaDbDataSource;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class ContainerWithDynamicPortTests {

	private static final String MYSQL_ROOT_PASSWORD = "root";
	private static final String MYSQL_DATABASE = "demo";
	private static final String MYSQL_PORT = "3306";

	@Test
	@Container(image = "mariadb", ports = MYSQL_PORT, env = {
		"MYSQL_ROOT_PASSWORD=" + MYSQL_ROOT_PASSWORD,
		"MYSQL_DATABASE=" + MYSQL_DATABASE
	})
	@ExtendWith(DockerParameterResolver.class)
	void insertIntoTable(NetworkSettings networkSettings, TestReporter testReporter) {
		int port = getMySQLPort(networkSettings);
		testReporter.publishEntry("MySQL port", String.valueOf(port));

		FluentJdbc jdbc = connectToDatabase(port);
		jdbc.query().plainConnection(connection ->
			connection.createStatement()
				.execute("CREATE TABLE example (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) NOT NULL)")
		);

		insertIntoExampleTable(jdbc, "John Doe");
		insertIntoExampleTable(jdbc, "Jane Doe");

		List<String> names = jdbc.query()
			.select("SELECT name FROM example ORDER BY id")
			.listResult(resultSet -> resultSet.getString(1));

		assertIterableEquals(asList("John Doe", "Jane Doe"), names);
	}

	private void insertIntoExampleTable(FluentJdbc jdbc, String name) {
		jdbc.query()
			.update("INSERT INTO example (name) VALUES (?)")
			.params(name)
			.run();
	}

	private FluentJdbc connectToDatabase(int port) {
		var dataSource = new MariaDbDataSource("127.0.0.1", port, MYSQL_DATABASE);
		dataSource.setUser("root");
		dataSource.setPassword(MYSQL_ROOT_PASSWORD);
		await().atMost(30, SECONDS)
			.until(() -> {
				try (var connection = dataSource.getConnection()) {
					return connection.isValid(1000);
				} catch (Exception e) {
					return false;
				}
			});
		return new FluentJdbcBuilder()
			.connectionProvider(dataSource)
			.build();
	}

	private int getMySQLPort(NetworkSettings networkSettings) {
		Map<ExposedPort, Binding[]> bindings = networkSettings.getPorts().getBindings();
		Binding binding = bindings.get(ExposedPort.parse(MYSQL_PORT))[0];
		return Integer.parseInt(binding.getHostPortSpec());
	}

}
