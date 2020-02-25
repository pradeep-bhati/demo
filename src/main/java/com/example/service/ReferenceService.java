package com.example.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.controller.Testontroller.RequestResponse;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

@Service
public class ReferenceService {
	
	 @Async("asyncExecutor")
	 public CompletableFuture<RequestResponse>  getReferenceJson(String path,String json)
	 {
		 DocumentContext jsonContext = JsonPath.parse(json);  
		 String resourceURI = jsonContext.read(path);
		 System.out.println(Thread.currentThread());
		 long start = System.currentTimeMillis();
		 RestTemplate restTemplate = new RestTemplate();
		 long diff = System.currentTimeMillis() - start;
		 System.out.println("time for:"+resourceURI+" :"+diff);
		 String baseUri = "http://hapi.fhir.org/baseR4";
		 String uri = baseUri +"/"+ resourceURI;
		 ResponseEntity<String> response
		  = restTemplate.getForEntity(uri , String.class);
		 RequestResponse requestResponse = new RequestResponse();
		 requestResponse.setPath(path);
		 requestResponse.setResponse(response.getBody());
		 return CompletableFuture.completedFuture(requestResponse)  ;		 
	 }


}
