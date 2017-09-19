package demo._01_conditionalexecution;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.Calculator;
import org.junit.jupiter.api.Test;

class ConditionalTests {

	@Test
	@Environment(enabledFor = {"Dev", "QA"})
	void add() {
		assertEquals(2, new Calculator().add(1, 1));
	}

	@Test
	// @Environment(enabledFor = {})
	void subtract() {
		assertEquals(0, new Calculator().subtract(1, 1));
	}

}
