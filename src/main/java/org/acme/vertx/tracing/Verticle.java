package org.acme.vertx.tracing;

import java.net.SocketException;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

@Dependent
public class Verticle extends AbstractVerticle {

	private static final Logger LOG = LoggerFactory.getLogger(Verticle.class);

	private NetServer server;

	@ConfigProperty(name = "VT_LISTEN_PORT", defaultValue = "8684")
	int listenPort;

	@Inject
	MessageHandler messageHandler;

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		LOG.info("#start - listening on {}", listenPort);

		NetServerOptions options = new NetServerOptions().setPort(listenPort);
		server = vertx.createNetServer(options);

		server.connectHandler(socket -> {
			LOG.info("Socket Open [{}]", socket.remoteAddress().host());

			SocketHandler socketHandler = new SocketHandler(socket, messageHandler);
			messageHandler.setResponder(socketHandler);
			socket.handler(socketHandler);

			socket.closeHandler(v -> LOG.info("Socket Closed [{}]", socket.remoteAddress().host()));

			socket.exceptionHandler(ex -> {
				if (ex instanceof SocketException se && "Connection reset".equals(se.getMessage())) {
					/* Quieter logging of common boring disconnect. */
					LOG.info("Socket Exception [{}] {}", socket.remoteAddress().host(), se.getMessage());
				} else {
					LOG.error("Socket Exception [{}]", socket.remoteAddress().host(), ex);
				}
			});
		});

		server.listen().<Void>mapEmpty().onComplete(startPromise);
	}

	@Override
	public void stop(Promise<Void> stopPromise) throws Exception {
		LOG.info("#stop");

		server.close(stopPromise);
	}

}
