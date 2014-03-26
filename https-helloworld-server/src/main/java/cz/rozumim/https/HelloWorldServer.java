package cz.rozumim.https;

import static spark.Spark.get;

import org.apache.log4j.Logger;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * Server, that listens at https://localhost:4567/helloworld
 * 
 * @author Petr Stepanek
 */
public class HelloWorldServer {

	static final transient Logger log = Logger
			.getLogger(HelloWorldServer.class);

	private static final String KEYSTORE_PATH = "./src/main/resources/keystore.jks";
	private static final String KEYSTORE_PASSWORD = "password";

	public static void main(String[] args) {

		Spark.setSecure(KEYSTORE_PATH, KEYSTORE_PASSWORD, null,
				null);

		get(new Route("/") {

			@Override
			public Object handle(Request request, Response response) {
				
				log.info("-> / is not supported");
				
				halt(404);
				
				return null;
			}
		});

		get(new Route("/helloworld") {

			@Override
			public Object handle(Request request, Response response) {

				log.info("-> /helloworld");

				return "hello world";
			}
		});
	}
}
