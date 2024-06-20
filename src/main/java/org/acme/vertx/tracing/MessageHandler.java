package org.acme.vertx.tracing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.context.api.ManagedExecutorConfig;

@ApplicationScoped
class MessageHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MessageHandler.class);

	@Inject
	@ManagedExecutorConfig(maxAsync = 5)
	ManagedExecutor worker;

	@Inject
	@ManagedExecutorConfig(maxAsync = 1)
	ManagedExecutor asyncResponder;

	private Responder responder;

	protected void setResponder(Responder responder) {
		this.responder = responder;
	}

	public void handle(String msg) {
		worker.execute(() -> handleMessage(msg));
	}

	@WithSpan
	void handleMessage(String msg) {
		LOG.info("Handling: {}", msg);
		String response = prepareEcho(msg);
		Runnable task = Context.current().wrap(() -> respond(response));
		asyncResponder.execute(task);
	}

	@WithSpan
	String prepareEcho(String msg) {
		return "ECHO " + msg;
	}

	@WithSpan
	void respond(String response) {
		responder.respond(response);
	}

}