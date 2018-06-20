package com.tuktukconsumer.services;

import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
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
	public Consumer<String, String> getKafkaConsumer(JsonObject options) {
		Properties consumerprops = new Properties();
		server = options.has("server") ? options.get("server").toString() : server;
		consumerprops.put("bootstrap.servers", server);
		group = options.has("group") ? options.get("group").toString() : group;
		consumerprops.put("group.id", group);
		commit = options.has("commit") ? options.get("commit").getAsBoolean() : commit;
		consumerprops.put("enable.auto.commit", commit);
		autocommittime = options.has("autocommittime") ? options.get("autocommittime").getAsInt() : autocommittime;
		consumerprops.put("auto.commit.interval.ms", autocommittime);
		sessiontimeout = options.has("sessiontimeout") ? options.get("sessiontimeout").getAsInt() : sessiontimeout;
		consumerprops.put("session.timeout.ms", sessiontimeout);
		keydeserializer = options.has("keydeserializer") ? options.get("keydeserializer").toString() : keydeserializer;
		consumerprops.put("key.deserializer", keydeserializer);
		valuedeserializer = options.has("valuedeserializer") ? options.get("valuedeserializer").toString()
				: valuedeserializer;
		consumerprops.put("value.deserializer", valuedeserializer);
		return new KafkaConsumer<>(consumerprops);
	}

}
