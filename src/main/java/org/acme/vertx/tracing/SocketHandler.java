package org.acme.vertx.tracing;

import static io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle.setCurrentContextSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.common.vertx.VertxContext;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

class SocketHandler implements Handler<Buffer>, Responder {

	private static final Logger LOG = LoggerFactory.getLogger(SocketHandler.class);

	private final NetSocket socket;
	private final MessageHandler messageHandler;

	SocketHandler(NetSocket socket, MessageHandler messageHandler) {
		this.socket = socket;
		this.messageHandler = enforceDuplicatedContext(messageHandler);
	}

	@Override
	public void handle(Buffer buffer) {
		String message = buffer.toString();
		LOG.info("Processing: {}", message);
		messageHandler.handle(message);
	}

	@Override
	public void respond(String response) {
		LOG.info("Responding: {}", response);
		Buffer output = Buffer.buffer(response);
		socket.write(output);
	}

	/* See https://github.com/quarkusio/quarkus/discussions/41346#discussioncomment-9836900 */
	private static MessageHandler enforceDuplicatedContext(MessageHandler delegate) {
		return new MessageHandler() {
			@Override
			public void handle(String message) {
				if (!VertxContext.isOnDuplicatedContext()) {
					Context context = VertxContext.createNewDuplicatedContext();
					context.runOnContext(new Handler<Void>() {
						@Override
						public void handle(Void x) {
							setCurrentContextSafe(true);
							delegate.handle(message);
						}
					});
				} else {
					setCurrentContextSafe(true);
					delegate.handle(message);
				}
			}
		};
	}

}