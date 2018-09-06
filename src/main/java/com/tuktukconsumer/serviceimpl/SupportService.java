package com.tuktukconsumer.serviceimpl;

import javax.servlet.http.HttpServletRequest;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;

public interface SupportService {

	public Producer<String, String> getKafkaProducer(JsonObject options);

	public Consumer<String, String> getKafkaConsumer(JSONObject options) throws JSONException;

	public JsonObject getRequestObject(HttpServletRequest req);

	public String getWikiResponse(JsonObject requestObject) throws Exception;

}
