package com.tuktukconsumer.serviceimpl;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;

import com.google.gson.JsonObject;

public interface SupportService {

	public Producer<String, String> getKafkaProducer(JsonObject options);

	public Consumer<String, String> getKafkaConsumer(JsonObject options);

}
