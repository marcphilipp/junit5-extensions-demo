package demo._01_conditionalexecution;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@ExtendWith(EnvironmentExecutionCondition.class)
@Retention(RUNTIME)
@Target(METHOD)
public @interface EnabledForEnvironment {

	String[] value();

}
