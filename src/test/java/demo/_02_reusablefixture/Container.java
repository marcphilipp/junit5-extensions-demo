package demo._02_reusablefixture;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@ExtendWith(DockerExtension.class)
@Retention(RUNTIME)
public @interface Container {

	String image();

	String[] env() default {};

	String[] ports() default {};

}
