package com.tuktukconsumer.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.tuktukconsumer.services.ConsumerService;

@RestController
public class PrimeController {

	@Autowired
	ConsumerService consumerservice;

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	@ResponseBody
	public String test() {
		return System.getProperty("java.class.path");
	}

	@RequestMapping(value = "/getKafkaStatus", method = RequestMethod.GET, headers = "Content-Type=application/json")
	@ResponseBody
	public String getStatusOfTopic(HttpServletRequest req, HttpServletResponse res) {

		return consumerservice.getStatusOfKafka(req, res);
	}
	
	@RequestMapping(value = "/getWikiResponse", method = RequestMethod.POST, headers = "Content-Type=application/json")
	@ResponseBody
	public String getWikiResponse(HttpServletRequest req, HttpServletResponse res) throws Exception {

		return consumerservice.getWikiResponse(req, res);
	}

	@RequestMapping(value = "/addConsumerOnTopic", method = RequestMethod.POST)
	@ResponseBody
	public String addConsumerOnTopic(HttpServletRequest req, HttpServletResponse res) {

		return consumerservice.addConsumerOnTopic(req, res);
	}

}
