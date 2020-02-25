package com.example.controller;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.service.ReferenceService;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

@RestController
public class Testontroller {
	
	@Autowired
	private ReferenceService referenceService;
	
	 @PostMapping("/myapp")
	    public String homeInit(@RequestBody String str) throws ParseException, InterruptedException, ExecutionException {
		 
		 	long start = System.currentTimeMillis();
	        Configuration conf = Configuration.builder().options(Option.AS_PATH_LIST).build();
	        
//	        System.out.println("jsonpath Object:"+jsonContext.jsonString());
	        List<String> pathList = JsonPath.using(conf).parse(str).read("$..reference");
	        
	        List<CompletableFuture<RequestResponse>> requestResponseListFutures =
	        pathList.stream().map(path -> referenceService.getReferenceJson(path,str)).collect(Collectors.toList());
	        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
	        		requestResponseListFutures.toArray(new CompletableFuture[requestResponseListFutures.size()])
	        );
	        CompletableFuture<List<RequestResponse>> allCompletableFuture = allFutures.thenApply(future -> {
	            return requestResponseListFutures.stream()
	                    .map(completableFuture -> completableFuture.join())
	                    .collect(Collectors.toList());
	        });
	        
	        CompletableFuture<DocumentContext> documentContextFuture = allCompletableFuture.thenApply(requestResponseList -> {
	        	DocumentContext jsonContext = JsonPath.parse(str); 
	        	for(RequestResponse requestResponse : requestResponseList) {
	        		jsonContext.set(requestResponse.getPath(),JsonPath.parse(requestResponse.getResponse()).json());
	        	}
	        	return jsonContext;
	        });
	        
	        String finaljsonstring = documentContextFuture.get().jsonString();
	       // String response = null;
//	        for(String path : pathList) {
//	            String resourceURI = jsonContext.read(path);
//            	response = getReferenceJson(resourceURI);
//            	jsonContext.set(path,JsonPath.parse(response).json());
//	        }
	        
//	        String finaljsonstring =(jsonContext.jsonString());
//	        System.out.println("printing json finally");
//	        System.out.println(finaljsonstring);
//	        return finaljsonstring;
	        long diff = System.currentTimeMillis() - start;
	        System.out.println("time for:" +diff);
	        return finaljsonstring;
	    }
	 
//	 @Async("asyncExecutor")
//	 public CompletableFuture<RequestResponse>  getReferenceJson(String path,String json)
//	 {
//		 DocumentContext jsonContext = JsonPath.parse(json);  
//		 String resourceURI = jsonContext.read(path);
//		 System.out.println(Thread.currentThread());
//		 
//		 RestTemplate restTemplate = new RestTemplate();
//		 
//		 
//		 String baseUri = "http://hapi.fhir.org/baseR4";
//		 String uri = baseUri +"/"+ resourceURI;
//		 ResponseEntity<String> response
//		  = restTemplate.getForEntity(uri , String.class);
//		 RequestResponse requestResponse = new RequestResponse();
//		 requestResponse.setPath(path);
//		 requestResponse.setResponse(response.getBody());
//		 return CompletableFuture.completedFuture(requestResponse)  ;		 
//	 }
	 
	 public static class RequestResponse{
		
		 String path;
		 public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public String getResponse() {
			return response;
		}
		public void setResponse(String response) {
			this.response = response;
		}
		String response;
	 }
}
	 
