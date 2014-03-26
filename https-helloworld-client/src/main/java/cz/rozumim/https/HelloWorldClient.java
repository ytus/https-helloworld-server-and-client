package cz.rozumim.https;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.google.common.io.Closer;

/**
 * Client that requests GET at https://localhost:4567/helloworld
 * 
 * @author Petr Stepanek
 */
public class HelloWorldClient {

	private static final transient Logger log = Logger
			.getLogger(HelloWorldClient.class);

	// server listens at https://localhost:4567/helloworld
	private static final String SCHEME = "https";
	private static final String HOST = "localhost";
	private static final int PORT = 4567;
	private static final String PATH = "/helloworld";

	private static final String TRUSTSTORE_PATH = "./src/main/resources/truststore.jks";
	private static final String TRUSTSTORE_TYPE = "JKS";
	private static final String TRUSTSTORE_PASSWORD = "password";

	public void run() throws IOException {

		// how Closer works:
		// https://code.google.com/p/guava-libraries/wiki/ClosingResourcesExplained
		Closer closer = Closer.create();

		try {

			URI uri = new URIBuilder().setScheme(SCHEME).setHost(HOST)
					.setPort(PORT).setPath(PATH).build();

			HttpGet httpget = new HttpGet(uri);

			HttpClientContext context = prepareHttpContext();

			CloseableHttpClient httpclient = closer
					.register(prepareHttpClient());

			CloseableHttpResponse response = closer.register(httpclient
					.execute(httpget, context));

			String content = EntityUtils.toString(response.getEntity());

			if (log.isDebugEnabled()) {
				log.debug(response);
			}

			log.info("-> " + content);
		
		} catch (Throwable e) {
			throw closer.rethrow(e);
		} finally {
			closer.close();
		}
	}


	private CloseableHttpClient prepareHttpClient()
			throws NoSuchAlgorithmException, CertificateException, IOException,
			KeyManagementException, KeyStoreException {

		// for simple http (no https) use this instead:
		// return HttpClients.createDefault();

		FileInputStream fin = new FileInputStream(TRUSTSTORE_PATH);
		KeyStore ks = KeyStore.getInstance(TRUSTSTORE_TYPE);
		ks.load(fin, TRUSTSTORE_PASSWORD.toCharArray());

		SSLContext sslContext = SSLContexts.custom().useTLS()
				.loadTrustMaterial(ks).build();

		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslContext);

		return HttpClients.custom().setSSLSocketFactory(sslsf).build();
	}

	private HttpClientContext prepareHttpContext() {

		HttpClientContext context = HttpClientContext.create();

		// here u can for example add basic authentication:
		//
		// org.apache.http.client.CredentialsProvider credsProvider = new
		// BasicCredentialsProvider();
		// credsProvider.setCredentials(new AuthScope(HOST,
		// AuthScope.ANY_PORT),
		// new UsernamePasswordCredentials("user", "password"));
		// context.setCredentialsProvider(credsProvider);

		return context;
	}

	public static void main(String[] args) throws IOException {

		HelloWorldClient client = new HelloWorldClient();
		client.run();
	}
}
