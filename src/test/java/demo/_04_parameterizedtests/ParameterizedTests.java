package demo._04_parameterizedtests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ParameterizedTests {

	@ParameterizedTest
	@ValueSource(strings = {"foo", "bar"})
	void simpleParameterizedTest(String argument) {
		assertNotEquals("baz", argument);
	}

	@ParameterizedTest(name = "{0} → {1}")
	@CsvSource({"foo, 42", "bar, 23"})
	void csvParameterizedTest(String argument, int number) {
		assertNotEquals("baz", argument);
		assertNotEquals(14, number);
	}

	@ParameterizedTest
	@MethodSource("personProvider")
	void methodParameterizedTest(Person person) {
		assertNotEquals("baz", person.getName());
		assertNotEquals(14, person.getAge());
	}

	private static Stream<Person> personProvider() {
		return Stream.of(
			new Person("foo", 42),
			new Person("bar", 23)
		);
	}

	@ParameterizedTest
	@JsonSource("test-data.json")
	void customParameterizedTest(Person person) {
		assertNotEquals("baz", person.getName());
		assertNotEquals(14, person.getAge());
	}

	static class Person {

		private final String name;
		private final int age;

		@JsonCreator
		Person(@JsonProperty("name") String name, @JsonProperty("age") int age) {
			this.name = name;
			this.age = age;
		}

		String getName() {
			return name;
		}

		int getAge() {
			return age;
		}

		@Override
		public String toString() {
			return "Person(name = " + name + ", age = " + age + ")";
		}
	}

}
