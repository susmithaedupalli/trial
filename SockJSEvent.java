package trial;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.platform.Verticle;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

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

		JsonArray permitted = new JsonArray();
		permitted.add(new JsonObject().putString("address", "ping-address"));
		permitted.add(new JsonObject().putString("address", "pong-address"));
		sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus"),
				permitted, permitted);

		final EventBus eb = vertx.eventBus();

		eb.registerHandler("ping-address", new Handler<Message<String>>() {
			@Override
			public void handle(Message<String> message) {
				// Reply to it
				System.out.println("Received message: " + message.body());

				eb.send("pong-address", "hi from server",
						new Handler<Message<String>>() {
							@Override
							public void handle(Message<String> reply) {
								System.out.println("Received reply: "
										+ reply.body());
							}
						});

			}
		});

		server.listen(8080);

		System.out.println("Server created ------------- ");

	}

}