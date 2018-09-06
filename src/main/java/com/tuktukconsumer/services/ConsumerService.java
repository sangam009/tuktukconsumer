package com.tuktukconsumer.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ConsumerService {

	public String getStatusOfKafka(HttpServletRequest req, HttpServletResponse res);

	public String addConsumerOnTopic(HttpServletRequest req, HttpServletResponse res);

	public String getWikiResponse(HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException, IOException, Exception;

}
