package demo._02_reusablefixture;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class DockerExtension implements BeforeEachCallback, AfterEachCallback {

	@Override
	public void beforeEach(ExtensionContext context) {
		context.getElement().ifPresent(annotatedElement ->
			AnnotationSupport.findAnnotation(annotatedElement, Container.class)
				.map(annotation -> startContainer(context, annotation.image(), annotation.env(), annotation.ports()))
				.ifPresent(containerId -> getStore(context).put("containerId", containerId))
		);
	}

	@Override
	public void afterEach(ExtensionContext context) {
		String containerId = getContainerId(context);
		if (containerId != null) {
			stopContainer(context, containerId);
		}
	}

	public static String getContainerId(ExtensionContext context) {
		return getStore(context).get("containerId", String.class);
	}

	private String startContainer(ExtensionContext context, String image, String[] env, String[] ports) {
		DockerClient dockerClient = getDockerClient(context);

		List<Image> results = dockerClient.listImagesCmd()
			.withImageNameFilter(image + ":latest")
			.exec();
		if (results.isEmpty()) {
			System.out.println("Pulling Docker image " + image);
			dockerClient.pullImageCmd(image).exec(new PullImageResultCallback()).awaitSuccess();
		}

		String containerId = dockerClient.createContainerCmd(image)
			.withEnv(env)
			.withPortBindings(Arrays.stream(ports).map(PortBinding::parse).collect(toList()))
			.exec()
			.getId();

		context.publishReportEntry("docker-container-id", containerId);
		dockerClient.startContainerCmd(containerId).exec();

		return containerId;
	}

	private void stopContainer(ExtensionContext context, String containerId) {
		DockerClient dockerClient = getDockerClient(context);
		dockerClient.stopContainerCmd(containerId).exec();
		dockerClient.removeContainerCmd(containerId).exec();
	}

	public static DockerClient getDockerClient(ExtensionContext context) {
		return context.getRoot().getStore(Namespace.GLOBAL)
			.getOrComputeIfAbsent(
				"DockerClient",
				key -> new DockerClientResource(),
				DockerClientResource.class).get();
	}

	private static ExtensionContext.Store getStore(ExtensionContext context) {
		return context.getStore(Namespace.create(DockerExtension.class, context.getUniqueId()));
	}

	static class DockerClientResource implements CloseableResource {
		private final DockerClient dockerClient;

		DockerClientResource() {
			var config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
			dockerClient = DockerClientBuilder.getInstance(config).build();
		}

		@Override
		public void close() throws Throwable {
			dockerClient.close();
		}

		DockerClient get() {
			return dockerClient;
		}
	}
}
