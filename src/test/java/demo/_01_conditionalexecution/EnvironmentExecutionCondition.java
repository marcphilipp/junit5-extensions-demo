package demo._01_conditionalexecution;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

public class EnvironmentExecutionCondition implements ExecutionCondition {

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		String activeEnvironment = System.getProperty("environment");
		if (activeEnvironment == null) {
			return disabled("there's no active environment");
		}
		Set<String> enabledEnvironments = getEnabledEnvironments(context);
		return enabledEnvironments.contains(activeEnvironment)
				? enabled("active environment is enabled")
				: disabled("active environment is not enabled");
	}

	private Set<String> getEnabledEnvironments(ExtensionContext context) {
		Set<String> enabledEnvironments = new HashSet<>();
		context.getElement().ifPresent(element -> AnnotationSupport.findAnnotation(element, Environment.class)
					.map(Environment::enabledFor)
					.ifPresent(array -> enabledEnvironments.addAll(Arrays.asList(array)))
		);
		return enabledEnvironments;
	}
}
