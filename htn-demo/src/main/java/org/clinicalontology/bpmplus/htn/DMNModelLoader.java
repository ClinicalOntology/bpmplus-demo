package org.clinicalontology.bpmplus.htn;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.compiler.RuntimeTypeCheckOption;
import org.kie.dmn.core.impl.DMNRuntimeImpl;
import org.kie.internal.builder.InternalKieBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

//import static org.hamcrest.CoreMatchers.is;

public final class DMNModelLoader {

	public static final Logger LOG = LoggerFactory.getLogger(DMNModelLoader.class);

	public static DMNRuntime createRuntime(final String resourceName, final Class testClass) {
		final KieServices ks = KieServices.Factory.get();
		final KieContainer kieContainer = getKieContainer(
				ks.newReleaseId("org.kie", "dmn-test-" + UUID.randomUUID(), "1.0"),
				ks.getResources().newClassPathResource(resourceName, testClass));

		final DMNRuntime runtime = typeSafeGetKieRuntime(kieContainer);
		if(runtime == null) {
		    throw new RuntimeException("BPM runtime is null");
        }
		return runtime;
	}

	public static DMNRuntime typeSafeGetKieRuntime(final KieContainer kieContainer) {
		DMNRuntime dmnRuntime = kieContainer.newKieSession().getKieRuntime(DMNRuntime.class);
		((DMNRuntimeImpl) dmnRuntime).setOption(new RuntimeTypeCheckOption(true));
		return dmnRuntime;

	}

	public static KieContainer getKieContainer(ReleaseId releaseId, Resource... resources) {
		KieServices ks = KieServices.Factory.get();
		createAndDeployJar(ks, releaseId, resources);
		return ks.newKieContainer(releaseId);
	}

	public static KieModule createAndDeployJar(KieServices ks, ReleaseId releaseId, Resource... resources) {
		byte[] jar = createJar(ks, releaseId, resources);

		KieModule km = deployJarIntoRepository(ks, jar);
		return km;
	}
	
	public static String formatMessages(final List<DMNMessage> messages) {
        return messages.stream().map(Object::toString).peek(m -> LOG.debug(m)).collect(Collectors.joining("\n"));
    }
	
	public static byte[] createJar(KieServices ks, ReleaseId releaseId, Resource... resources) {
		KieFileSystem kfs = ks.newKieFileSystem().generateAndWritePomXML(releaseId);
		for (int i = 0; i < resources.length; i++) {
			if (resources[i] != null) {
				kfs.write(resources[i]);
			}
		}
		KieBuilder kieBuilder = ks.newKieBuilder(kfs);
		((InternalKieBuilder) kieBuilder).buildAll(o -> true);
		Results results = kieBuilder.getResults();
		if (results.hasMessages(Message.Level.ERROR)) {
			throw new IllegalStateException(results.getMessages(Message.Level.ERROR).toString());
		}
		InternalKieModule kieModule = (InternalKieModule) ks.getRepository().getKieModule(releaseId);
		byte[] jar = kieModule.getBytes();
		return jar;
	}

	public static KieModule deployJarIntoRepository(KieServices ks, byte[] jar) {
		Resource jarRes = ks.getResources().newByteArrayResource(jar);
		KieModule km = ks.getRepository().addKieModule(jarRes);
		return km;
	}
}