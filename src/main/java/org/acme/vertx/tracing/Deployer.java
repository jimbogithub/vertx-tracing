package org.acme.vertx.tracing;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.mutiny.core.Vertx;

@ApplicationScoped
public class Deployer {

	private static final Logger LOG = LoggerFactory.getLogger(Deployer.class);

	private String deploymentID;

	void onStart(@Observes StartupEvent ev, Vertx vertx, Instance<Verticle> verticle) {
		LOG.info("#onStart");

		DeploymentOptions deploymentOptions = new DeploymentOptions().setInstances(1)
				.setThreadingModel(ThreadingModel.EVENT_LOOP);
		deploymentID = vertx.deployVerticle(verticle::get, deploymentOptions).log().await()
				.atMost(Duration.ofSeconds(10));

		LOG.info("Started {}", deploymentID);
	}

	void onStop(@Observes ShutdownEvent ev, Vertx vertx) {
		LOG.info("#onStop");

		vertx.undeploy(deploymentID).await().atMost(Duration.ofSeconds(10));

		LOG.info("Stopped {}", deploymentID);
	}

}
