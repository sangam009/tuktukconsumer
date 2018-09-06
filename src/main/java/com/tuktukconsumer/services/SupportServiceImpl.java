package com.tuktukconsumer.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tuktukconsumer.manager.HttpClientPoolManager;
import com.tuktukconsumer.serviceimpl.SupportService;

@Service("supportservice")
@PropertySource("classpath:kafka.properties")
public class SupportServiceImpl implements SupportService {

	@Value("${kafka.bootstrap.server}")
	String server;

	@Value("${kafka.acks}")
	String acks;

	@Value("${kafka.retries}")
	int retries;

	@Value("${kafka.batch.size}")
	int size;

	@Value("${kafka.linger.ms}")
	int linger;

	@Value("${kafka.buffer.memory}")
	int memory;

	@Value("${kafka.key.serializer}")
	String keyserializer;

	@Value("${kafka.value.serializer}")
	String valueserializer;

	@Value("${kafka.group.id}")
	String group;

	@Value("${kafka.enable.auto.commit}")
	Boolean commit;

	@Value("${kafka.auto.commit.interval.ms}")
	int autocommittime;

	@Value("${kafka.session.timeout.ms}")
	int sessiontimeout;

	@Value("${kafka.key.deserializer}")
	String keydeserializer;

	@Value("${kafka.value.deserializer}")
	String valuedeserializer;

	@Value("${kafka.connect.timeout.ms}")
	int connectiontimeout;

	@Override
	public Producer<String, String> getKafkaProducer(JsonObject options) {
		Properties producerprops = new Properties();
		server = options.has("server") ? options.get("server").toString() : server;
		producerprops.put("bootstrap.servers", server);
		acks = options.has("acks") ? options.get("acks").toString() : acks;
		producerprops.put("acks", acks);
		retries = options.has("retries") ? options.get("retries").getAsInt() : retries;
		producerprops.put("retries", retries);
		size = options.has("size") ? options.get("size").getAsInt() : size;
		producerprops.put("batch.size", size);
		linger = options.has("linger") ? options.get("linger").getAsInt() : linger;
		producerprops.put("linger.ms", linger);
		memory = options.has("memory") ? options.get("memory").getAsInt() : memory;
		producerprops.put("buffer.memory", memory);
		keyserializer = options.has("keyserializer") ? options.get("keyserializer").toString() : keyserializer;
		producerprops.put("key.serializer", keyserializer);
		valueserializer = options.has("valueserializer") ? options.get("valueserializer").toString() : valueserializer;
		producerprops.put("value.serializer", valueserializer);
		return new KafkaProducer<>(producerprops);
	}

	@Override
	public Consumer<String, String> getKafkaConsumer(JSONObject options) throws JSONException {
		System.out.println("group name us " + options.get("group"));
		Properties consumerprops = new Properties();
		server = options.has("server") ? options.get("server").toString() : server;
		consumerprops.put("bootstrap.servers", server);
		group = options.has("group") ? options.get("group").toString() : group;
		consumerprops.put("group.id", group);
		commit = options.has("commit") ? options.getBoolean("commit") : commit;
		consumerprops.put("enable.auto.commit", commit);
		autocommittime = options.has("autocommittime") ? options.getInt("autocommittime") : autocommittime;
		consumerprops.put("auto.commit.interval.ms", autocommittime);
		sessiontimeout = options.has("sessiontimeout") ? options.getInt("sessiontimeout") : sessiontimeout;
		consumerprops.put("session.timeout.ms", sessiontimeout);
		keydeserializer = options.has("keydeserializer") ? options.get("keydeserializer").toString() : keydeserializer;
		consumerprops.put("key.deserializer", keydeserializer);
		valuedeserializer = options.has("valuedeserializer") ? options.get("valuedeserializer").toString()
				: valuedeserializer;
		consumerprops.put("value.deserializer", valuedeserializer);
		return new KafkaConsumer<>(consumerprops);
	}

	@Override
	public JsonObject getRequestObject(HttpServletRequest req) {
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(req.getInputStream(), "UTF-8"));
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Exception is reading raw data from request is " + e);

		}
		JsonParser jsonparser = new JsonParser();
		JsonObject jsonObject = jsonparser.parse(stringBuilder.toString()).getAsJsonObject();
		return jsonObject;
	}

	@Override
	public String getWikiResponse(JsonObject requestObject) throws Exception {

		CloseableHttpClient client = HttpClientPoolManager.getInstance();

		HttpGet request = new HttpGet(
				"https://en.wikipedia.org/w/api.php");
		request.setHeader("Content-type", "application/json");
		request.setHeader("user-agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.170 Safari/537.36");
		request.setHeader("authority", "as.wikipedia.org");
		request.setHeader("accept-language", "en-US,en;q=0.9");

		HttpParams params = getHttpParams(requestObject);
		request.setParams(params);
		System.out.println(" params are " + params.toString());
		CloseableHttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		System.out.println("response is " + response.toString());
		StringBuilder strbuild = new StringBuilder();
		InputStreamReader inputStrReader = new InputStreamReader(entity.getContent());
		BufferedReader buffReader = new BufferedReader(inputStrReader);
		String line = null;
		while ((line = buffReader.readLine()) != null) {
			strbuild.append(line);
		}

		return strbuild.toString();
	}

	private HttpParams getHttpParams(JsonObject paramObject) {
		HttpParams params = new BasicHttpParams();
		for (String key : paramObject.keySet()) {
			params.setParameter(key, paramObject.get(key));
		}
		return params;
	}

}
