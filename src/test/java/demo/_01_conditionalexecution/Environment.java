package demo._01_conditionalexecution;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@ExtendWith(EnvironmentExecutionCondition.class)
@Retention(RUNTIME)
public @interface Environment {

	String[] enabledFor();

}
