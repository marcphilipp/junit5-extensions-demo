package demo._05_testtemplate;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.support.AnnotationSupport;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class PropertyInvocationProvider implements TestTemplateInvocationContextProvider {

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		Optional<AnnotatedElement> element = context.getElement();
		return element.isPresent() && AnnotationSupport.isAnnotated(element.get(), Property.class);
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		Method testMethod = context.getRequiredTestMethod();
		Property annotation = AnnotationSupport.findAnnotation(testMethod, Property.class).orElseThrow(IllegalStateException::new);
		Random random = getRandom(context, annotation);
		int invocations = annotation.invocations() > 0 ? annotation.invocations() : 100;
		AtomicBoolean failed = new AtomicBoolean(false);
		return Stream.generate(() -> createInvocationContext(testMethod, failed, random))
				.peek(invocationContext -> {
					if (failed.get()) {
						throw new TestAbortedException("at least one invocation failed");
					}
				})
				.limit(invocations);
	}

	private TestTemplateInvocationContext createInvocationContext(Method testMethod, AtomicBoolean failed, Random random) {
		Object[] arguments = generateRandomArguments(testMethod, random);
		return new TestTemplateInvocationContext() {
			@Override
			public String getDisplayName(int invocationIndex) {
				return Arrays.toString(arguments);
			}

			@Override
			public List<Extension> getAdditionalExtensions() {
				return Arrays.asList(parameterResolver(arguments), afterEachCallback(failed));
			}
		};
	}

	private ParameterResolver parameterResolver(Object[] arguments) {
		return new ParameterResolver() {
			@Override
			public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
				return true;
			}

			@Override
			public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
				return arguments[parameterContext.getIndex()];
			}
		};
	}

	private AfterEachCallback afterEachCallback(AtomicBoolean failed) {
		return context -> {
			Optional<Throwable> exception = context.getExecutionException();
			if (exception.isPresent() && !(exception.get() instanceof TestAbortedException)) {
				failed.compareAndSet(false, true);
			}
		};
	}

	private Object[] generateRandomArguments(Method testMethod, Random random) {
		Class<?>[] parameterTypes = testMethod.getParameterTypes();
		Object[] arguments = new Object[parameterTypes.length];
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = generateRandomValue(random, parameterTypes[i]);
		}
		return arguments;
	}

	private Object generateRandomValue(Random random, Class<?> type) {
		if (String.class.equals(type)) {
			return new UUID(random.nextLong(), random.nextLong()).toString();
		}
		if (List.class.equals(type)) { // we assume List<String> here
			return IntStream.range(0, random.nextInt(5))
				.mapToObj(i -> generateRandomValue(random, String.class))
				.collect(toCollection(ArrayList::new));
		}
		// obviously, this needs to support more than just String and List
		return null;
	}

	private Random getRandom(ExtensionContext context, Property annotation) {
		long seed = annotation.seed() > 0 ? annotation.seed() : System.currentTimeMillis();
		Random random = new Random(seed);
		context.publishReportEntry("seed", String.valueOf(seed));
		return random;
	}
}
