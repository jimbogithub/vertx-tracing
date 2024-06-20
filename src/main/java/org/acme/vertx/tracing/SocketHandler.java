package org.acme.vertx.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

class SocketHandler implements Handler<Buffer>, Responder {

	private static final Logger LOG = LoggerFactory.getLogger(SocketHandler.class);

	private final NetSocket socket;
	private final MessageHandler messageHandler;

	SocketHandler(NetSocket socket, MessageHandler messageHandler) {
		this.socket = socket;
		this.messageHandler = messageHandler;
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

}