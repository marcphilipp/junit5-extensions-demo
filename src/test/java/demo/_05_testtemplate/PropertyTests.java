package demo._05_testtemplate;

import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertyTests {

	@DisplayName("element added => size increases")
	@Property
	void addingElementIncreasesListSize(List<String> items, String newItem) {
		int previousSize = items.size();
		items.add(newItem);
		assertEquals(previousSize + 1, items.size());
	}

	@DisplayName("element removed => size decreases")
	@Property
	void removingElementDecreasesListSize(List<String> items) {
		int previousSize = items.size();
		items.remove(items.get(0));
		assertEquals(previousSize - 1, items.size());
	}
}
