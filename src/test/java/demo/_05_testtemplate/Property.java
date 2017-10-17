package demo._05_testtemplate;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
@ExtendWith(PropertyInvocationProvider.class)
@TestTemplate
@interface Property {

	long seed() default 0;

	int invocations() default 0;
}
