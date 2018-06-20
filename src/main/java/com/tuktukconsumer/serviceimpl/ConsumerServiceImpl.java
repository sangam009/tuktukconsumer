package com.tuktukconsumer.serviceimpl;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.tuktukconsumer.services.ConsumerService;

@Service("consumerservice")
public class ConsumerServiceImpl implements ConsumerService {

	@Autowired
	AsyncKafkaService asyncKafkaService;

	@Autowired
	SupportService supportservice;

	@Override
	public String getStatusOfKafka(HttpServletRequest req, HttpServletResponse res) {

		try {
			JsonObject consumerOptions = new JsonObject();
			JsonObject producerOptions = new JsonObject();
			Consumer<String, String> consumer = supportservice.getKafkaConsumer(consumerOptions);
			consumer.subscribe(Arrays.asList("status"));
			Producer<String, String> producer = supportservice.getKafkaProducer(producerOptions);
			for (int i = 0; i < 10; i++)
				producer.send(new ProducerRecord<String, String>("status", Integer.toString(i), Integer.toString(i)));
			producer.close();
			int count = 10;
			while (count > 0) {
				ConsumerRecords<String, String> records = consumer.poll(100);
				for (ConsumerRecord<String, String> record : records)
					System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(),
							record.value());
				count--;
			}
			consumer.close();

		} catch (Exception e) {
			System.out.println(
					"Error in inserting data in kafka or eror in getting data consumed by consumer with Exception "
							+ e);
			return (e.toString());
		}
		return "kafka producer consumer flow is working on topic status";
	}

	@Override
	public String addConsumerOnTopic(HttpServletRequest req, HttpServletResponse res) {
		try {
			System.out.println("topic name is " + req.getParameter("topic"));
			asyncKafkaService.addConsumerToTopicWithoutReflection(req.getParameter("topic"),
					req.getParameter("groupname"));
			return "done";
		} catch (Exception e) {
			System.out.println(
					"Exception in adding kafka consumer to the topic " + req.getParameter("topic") + " is " + e);
			return "fail";
		}

	}

}
