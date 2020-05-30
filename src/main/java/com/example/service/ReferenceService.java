package com.example.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.controller.Testontroller.RequestResponse;
//import com.example.controller.Testontroller.RequestResponse;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

@Service
public class ReferenceService {
	
	 @Async("asyncExecutor")
	 public CompletableFuture<RequestResponse>  getReferenceJson(String path,String json)
	 {
		 System.out.println(Thread.currentThread());
		 DocumentContext jsonContext = JsonPath.parse(json);  
		 String resourceUri = jsonContext.read(path);
		 String regex = ".*/.*";
		 RequestResponse requestResponse = new RequestResponse();
		 if(resourceUri.matches(regex)) {
		 RestTemplate restTemplate = new RestTemplate();
		 String baseUri = "http://hapi.fhir.org/baseR4";
		 String uri = baseUri +"/"+ resourceUri;
		 ResponseEntity<String> response
		  = restTemplate.getForEntity(uri ,String.class);
		 requestResponse.setPath(path);
		 requestResponse.setResponse(response.getBody());
		 		 
	 }
		 return CompletableFuture.completedFuture(requestResponse);

	 }
}
