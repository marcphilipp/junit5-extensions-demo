package demo._01_conditionalexecution;

import com.example.Calculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConditionalTests {

	@Test
	@EnabledForEnvironment({"Dev", "QA"})
	void add() {
		assertEquals(2, new Calculator().add(1, 1));
	}

	@Test
	// @EnabledForEnvironment({})
	void subtract() {
		assertEquals(0, new Calculator().subtract(1, 1));
	}

}
