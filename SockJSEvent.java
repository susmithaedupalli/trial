package trial;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.core.sockjs.SockJSSocket;
import org.vertx.java.platform.Verticle;

import eventbusbridge.ServerHook;

public class SockJSEvent extends Verticle {
	Logger logger;

	public void start() {
		logger = container.logger();

		HttpServer server = vertx.createHttpServer();

		server.requestHandler(new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				if (req.path().equals("/"))
					req.response().sendFile("trial/index.html"); // Serve the
																	// index.html
				if (req.path().endsWith("vertxbus.js"))
					req.response().sendFile("trial/vertxbus.js"); // Serve the
																	// js
			}
		});

		ServerHook hook = new ServerHook(logger);
		SockJSServer sockJSServer = vertx.createSockJSServer(server);
		sockJSServer.setHook(hook);
		sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus"),
				new JsonArray(), new JsonArray());

		final EventBus eb = vertx.eventBus();

		// eb.send("ping-address", "pong!");

		eb.registerHandler("ping-address", new Handler<Message<String>>() {

			public void handle(Message<String> message) {
				// Reply to it
				eb.send("ping-address", "pong!");
				System.out.println("Received message: " + message.body());
				// message.reply("pong!");
			}
		});

		server.listen(8080);

		System.out.println("Server created ------------- ");

	}

}