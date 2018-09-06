package com.tuktukconsumer.serviceimpl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Place;

@Service("asyncKafkaService")
public class AsyncKafkaService extends ClassLoader {

	@Autowired
	SupportService supportservice;

	@Async("multitasker")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addConsumerToTopicWithoutReflection(String topicname, String group) {
		System.out.println("group name here is " + group);
		try {
			JSONObject consumerOptions = new JSONObject();
			consumerOptions.put("group", group);
			Consumer<String, String> consumer = supportservice.getKafkaConsumer(consumerOptions);
			consumer.subscribe(Arrays.asList(topicname));
			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(100);
				for (ConsumerRecord<String, String> record : records) {
					JsonParser jsonparser = new JsonParser();
					JsonObject json = (JsonObject) jsonparser.parse(record.value());
					addNearBySearchToTheEnrichment(json);
				}
			}
		} catch (Exception e) {
			System.out.println("Exception in adding topic is " + e);
		}
	}
	@Async("multitasker")
	public void addNearBySearchToTheEnrichment(JsonObject geoCodeApiResponse) throws IOException, JSONException {
		GooglePlaces client = new GooglePlaces("AIzaSyCUg-jlo_6QekPQgmUT_vx6z0nHw-eJOis");
		// to get the list of the place ids of the nearby places to given lat long
		JsonObject location = geoCodeApiResponse.get("geometry").getAsJsonObject().get("location").getAsJsonObject();
		Double lat = Double.parseDouble(location.get("lat").toString());
		Double lon = Double.parseDouble(location.get("lng").toString());
		List<Place> places = client.getNearbyPlaces(lat, lon, 500.0, GooglePlaces.MAXIMUM_RESULTS);
		System.out.println("place searh value is "+places.get(0));
		for (Place place : places) {
			Place response = client.getPlaceById(place.getPlaceId());
			JSONObject responseInJson = response.getJson();
			String geometry = responseInJson.get("geometry").toString().replace("lng", "lon");
			JSONObject geometryJson = new JSONObject(geometry);
			responseInJson.put("geometry", geometryJson);
			
			System.out.println("final json to index is " + responseInJson.toString());
			indexInElasticsearch(responseInJson);
		}

	}
	
	@Async("multitasker")
	private JSONObject getDataFromWiki(JSONObject indexData) {
		
		return indexData;
	}
	
	@Async("multitasker")
	private void indexInElasticsearch(JSONObject json) {
		try {
			HttpEntity entity = new NStringEntity(json.toString(), ContentType.APPLICATION_JSON);
			RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
			JsonParser jsonp = new JsonParser();
			JsonObject location = jsonp.parse(json.get("geometry").toString()).getAsJsonObject().get("location")
					.getAsJsonObject();
			System.out.println("location value is " + location);
			Double addedValueOfLatLong = Double.parseDouble(location.get("lat").toString())
					+ Double.parseDouble(location.get("lon").toString());
			System.out.println("added value for lat long is " + addedValueOfLatLong);
			String endpointValue = "/suggestion/suggestionsdata/" + addedValueOfLatLong.toString();
			Response response = restClient.performRequest("PUT", endpointValue, Collections.<String, String>emptyMap(),
					entity);
			restClient.close();
		} catch (Exception e) {
			System.out.println("Exception in indexing is " + e);
		}
	}

	// @Async("multitasker")
	// @SuppressWarnings({ "unchecked", "rawtypes" })
	// public void addConsumerToTopic(String topicname, String group, String
	// qualifiedClassName) {
	// if (qualifiedClassName == null) {
	// System.out.println("the qualified class name is null please enter valid
	// one");
	// return;
	// }
	// try {
	// URL classUrl = new URL(
	// "file:///home/sangamdubey/Documents/workspace-sts-3.9.1.RELEASE/tuktukdataconsumer/parsingClass/ElasticsearchParser.class");
	// URL[] classUrls = { classUrl };
	// URLClassLoader ucl = new URLClassLoader(classUrls);
	// Class cls = ucl.loadClass("ElasticsearchParser");
	// Object obj = cls.newInstance();
	// Method execute = cls.getDeclaredMethod("execute", String.class);
	// JsonObject consumerOptions = new JsonObject();
	// consumerOptions.addProperty("group", group);
	// Consumer<String, String> consumer =
	// supportservice.getKafkaConsumer(consumerOptions);
	// consumer.subscribe(Arrays.asList(topicname));
	// while (true) {
	// ConsumerRecords<String, String> records = consumer.poll(100);
	// for (ConsumerRecord<String, String> record : records) {
	// System.out.println("record value is " + record.value());
	// execute.invoke(obj, record);
	// }
	// }
	// } catch (ClassNotFoundException e) {
	// System.out.println("specified class is not found");
	// e.printStackTrace();
	// return;
	// } catch (NoSuchMethodException e) {
	// System.out.println("specified method not found");
	// e.printStackTrace();
	// return;
	// } catch (SecurityException e) {
	// System.out.println("security exception issue");
	// e.printStackTrace();
	// return;
	// } catch (InstantiationException e) {
	// System.out.println("specified object instance doesn't exist");
	// e.printStackTrace();
	// return;
	// } catch (IllegalAccessException e) {
	// System.out.println("specified object has illegal access to the methods");
	// e.printStackTrace();
	// return;
	// } catch (IllegalArgumentException e) {
	// System.out.println("illegal argument in reflection method");
	// e.printStackTrace();
	// } catch (InvocationTargetException e) {
	// System.out.println("invocation taget exception in reflection");
	// e.printStackTrace();
	// } catch (MalformedURLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
}
