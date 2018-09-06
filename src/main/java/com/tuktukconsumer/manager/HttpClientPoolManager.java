package com.tuktukconsumer.manager;

import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

import javax.net.ssl.SSLContext;

import org.apache.http.Consts;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.ssl.SSLContexts;

/**
 * This example demonstrates how to customize and configure the most common
 * aspects of HTTP request execution and connection management.
 */
public class HttpClientPoolManager {

	private static CloseableHttpClient httpclient;
	private static Boolean flag = Boolean.TRUE;

	public static CloseableHttpClient getInstance() throws Exception {

		if (httpclient == null) {

			synchronized (flag) {

				if (httpclient == null) {
					httpclient = createClient();
				}
			}
		}

		return httpclient;
	}

	private static CloseableHttpClient createClient() throws Exception {

		HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory();

		/*
		 * Client HTTP connection objects when fully initialized can be bound to an
		 * arbitrary network socket. The process of network socket initialization, its
		 * connection to a remote address and binding to a local one is controlled by a
		 * connection socket factory.
		 */

		/*
		 * SSL context for secure connections can be created either based on system or
		 * application specific properties.
		 */
		SSLContext sslcontext = SSLContexts.createSystemDefault();

		/*
		 * Create a registry of custom connection socket factories for supported
		 * protocol schemes.
		 */
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslcontext)).build();

		// Use custom DNS resolver to override the system DNS resolution.
		DnsResolver dnsResolver = new SystemDefaultDnsResolver();

		// Create a connection manager with custom configuration.
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry,
				connFactory, dnsResolver);

		// Create socket configuration
		SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();

		/*
		 * Configure the connection manager to use socket configuration either by
		 * default or for a specific host.
		 */
		connManager.setDefaultSocketConfig(socketConfig);

		// Validate connections after 1 sec of inactivity
		connManager.setValidateAfterInactivity(1000);

		// Create message constraints
		MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(200)
				.setMaxLineLength(2000).build();

		// Create connection configuration
		ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE)
				.setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8)
				.setMessageConstraints(messageConstraints).build();

		/*
		 * Configure the connection manager to use connection configuration either by
		 * default or for a specific host.
		 */
		connManager.setDefaultConnectionConfig(connectionConfig);

		/*
		 * Configure total max or per route limits for persistent connections that can
		 * be kept in the pool or leased by the connection manager.
		 */
		connManager.setMaxTotal(200);
		connManager.setDefaultMaxPerRoute(200);

		// Use custom cookie store if necessary.
		CookieStore cookieStore = new BasicCookieStore();
		// Use custom credentials provider if necessary.
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		// Create global request configuration
		RequestConfig defaultRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT)
				.setConnectTimeout(15000).setSocketTimeout(15000).setExpectContinueEnabled(true)
				.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
				.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();

		// Create an HttpClient with the given custom dependencies and
		// configuration.
		CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(connManager)
				.setDefaultCookieStore(cookieStore).setDefaultCredentialsProvider(credentialsProvider)
				.setDefaultRequestConfig(defaultRequestConfig).build();

		return httpclient;
	}

}
