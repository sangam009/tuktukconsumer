package com.tuktukconsumer.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ConsumerService {

	public String getStatusOfKafka(HttpServletRequest req, HttpServletResponse res);

	public String addConsumerOnTopic(HttpServletRequest req, HttpServletResponse res);

}
