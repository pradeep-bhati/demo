package com.example.controller;

import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

@RestController
public class Testontroller {
	
	 @PostMapping("/myapp")
	    public String homeInit(@RequestBody String str) throws ParseException {
		 
	        Configuration conf = Configuration.builder().options(Option.AS_PATH_LIST).build();
	        DocumentContext jsonContext = JsonPath.parse(str);  
//	        System.out.println("jsonpath Object:"+jsonContext.jsonString());
	        List<String> pathList = JsonPath.using(conf).parse(str).read("$..reference");
	        String response = null;
	        for(String path : pathList) {
	            String resourceURI = jsonContext.read(path);
            	response = getReferenceJson(resourceURI);
            	jsonContext.set(path,JsonPath.parse(response).json());
	        }
	        
	        String finaljsonstring =(jsonContext.jsonString());
//	        System.out.println("printing json finally");
//	        System.out.println(finaljsonstring);
	        return finaljsonstring;
	    }
	 
	 public String getReferenceJson(String resourceURI)
	 {
		 System.out.println(Thread.currentThread());
		 long start = System.currentTimeMillis();
		 RestTemplate restTemplate = new RestTemplate();
		 long diff = System.currentTimeMillis() - start;
		 System.out.println("time for:"+resourceURI+" :"+diff);
		 String baseUri = "http://hapi.fhir.org/baseR4";
		 String uri = baseUri +"/"+ resourceURI;
		 ResponseEntity<String> response
		  = restTemplate.getForEntity(uri , String.class);
		 return response.getBody();		 
	 }
}
	 
