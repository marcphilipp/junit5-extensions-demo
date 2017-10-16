package demo._03_parameterresolution;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.NetworkSettings;
import demo._02_reusablefixture.DockerExtension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class DockerParameterResolver implements ParameterResolver {

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		return DockerExtension.getContainerId(extensionContext) != null
			&& parameterContext.getParameter().getType().equals(NetworkSettings.class);
	}

	@Override
	public NetworkSettings resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		DockerClient dockerClient = DockerExtension.getDockerClient(extensionContext);
		InspectContainerResponse response = dockerClient.inspectContainerCmd(DockerExtension.getContainerId(extensionContext)).exec();
		return response.getNetworkSettings();
	}

}
