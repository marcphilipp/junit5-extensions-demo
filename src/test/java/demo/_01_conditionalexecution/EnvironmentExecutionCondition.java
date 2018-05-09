package demo._01_conditionalexecution;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

public class EnvironmentExecutionCondition implements ExecutionCondition {

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		if (!context.getTestMethod().isPresent()) {
			return enabled("@EnabledForEnvironment can only be used on test methods");
		}
		String activeEnvironment = System.getProperty("environment");
		if (activeEnvironment == null) {
			return disabled("there's no active environment");
		}
		Set<String> enabledEnvironments = getEnabledEnvironments(context);
		return enabledEnvironments.contains(activeEnvironment)
				? enabled("active environment (" + activeEnvironment + ") is enabled for this test")
				: disabled("active environment (" + activeEnvironment + ") is not enabled for this test");
	}

	private Set<String> getEnabledEnvironments(ExtensionContext context) {
		Method testMethod = context.getRequiredTestMethod();
		return AnnotationSupport.findAnnotation(testMethod, EnabledForEnvironment.class)
			.map(EnabledForEnvironment::value)
			.map(Set::of)
			.orElse(emptySet());
	}
}
