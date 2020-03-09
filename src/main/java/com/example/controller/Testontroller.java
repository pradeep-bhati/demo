package com.example.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;

@RestController
public class Testontroller {
	
	 @PostMapping("/myapp")
	    public String homeInit(@RequestBody String str)  {
		 
	        Configuration conf = Configuration.builder().options(Option.AS_PATH_LIST).build();
	        DocumentContext jsonContext = JsonPath.parse(str);  
	        JsonObject jsonObject = new JsonObject();
	        JsonParser parser = new JsonParser();
	        List<String> referencePathsInJson = new ArrayList<String>();
	        String remoteResponse = null;
//	        take out all the paths in json, having reference word.
	        try {
	        	referencePathsInJson = JsonPath.using(conf).parse(str).read("$..reference");
	        }
	        catch(PathNotFoundException pathNotFoundException) {
	        	System.out.println("continue");
	        }
	        
			JsonElement parsedOrignalJson = parser.parse(str);
	        jsonObject.add("orignaljson",parsedOrignalJson);
	        Map<String,Integer> keyMap = new HashMap<>();
	        if(referencePathsInJson!=null) {
	        for(String path : referencePathsInJson) {        	
	            String resourceUri = jsonContext.read(path);
	            String regex = ".*/.*";
	            if(resourceUri.matches(regex)) {
	            	remoteResponse = getReferenceJson(resourceUri);          	
	            	ArrayList<String> keyList = filterJsonKey(path,resourceUri);
	            	populateJsonWithRefrences(keyList,jsonObject,remoteResponse,keyMap);
	            }
	        }
	        }
	        	        
	        return jsonObject.toString();
	    }
	  
	 public JsonObject populateJsonWithRefrences(ArrayList<String> keyList,JsonObject jsonObject,String remoteResponse,
			 Map<String,Integer> keyMap) {
		 
		 JsonParser parser = new JsonParser();
		 JsonElement parsedRemoteResponse = parser.parse(remoteResponse);
		 if(keyList.size()<2) {
			 
			 keyList.stream().forEach(key -> {
				 if(keyMap.containsKey(key)) {
					 
					 Integer value = keyMap.get(key);
					 if(value == 1) {
						 JsonElement json = jsonObject.get(key);
						 jsonObject.remove(key);
						 String newkey1 = key + "_"+ value;
						 jsonObject.add(newkey1, json);
					 }
					 value = value + 1;
					 String newkey = key + "_"+ value;
					 jsonObject.add(newkey, parsedRemoteResponse);
					 keyMap.put(key, value);
					 
				 }
				 else {
					 jsonObject.add(key, parsedRemoteResponse);
					 keyMap.put(key, 1);
				 }				 
			 });
     		System.out.println("json object with  patient"+jsonObject.toString());
     	}
     	else {
     		
     			JsonObject childJson = new JsonObject();	
     			childJson.add(keyList.get(0), parsedRemoteResponse);
     			if(keyMap.containsKey(keyList.get(1))) {
     				
     				Integer value = keyMap.get(keyList.get(1));
     				if(value == 1) {
     					JsonElement json = jsonObject.get(keyList.get(1));
     					jsonObject.remove(keyList.get(1));
     					String newkey1 = keyList.get(1)+"_"+value;
     					jsonObject.add(newkey1, json);
     					
     				}
     				value = value + 1;
     				String newkey = keyList.get(1)+"_"+value;
     				jsonObject.add(newkey,childJson);
     				keyMap.put(keyList.get(1), value);
     			}
     			else {
     				jsonObject.add(keyList.get(1),childJson);
     				keyMap.put(keyList.get(1), 1);
     			}

     	}
		 return jsonObject;
	 }
	 
	 public ArrayList<String> filterJsonKey(String path,String resourceUri) {
		StringBuffer sb = new StringBuffer(path);
		 int index = resourceUri.indexOf("/");
		 String firstWord = resourceUri.substring(0, index);		 
     	sb.delete(0, 2);
     	sb.deleteCharAt(sb.length()-1);
     	String str1 = sb.toString();
     	String str2 = str1.replace("'", "");
     	String str3= str2.replace("][", ":");
     	String[] strarray = str3.split(":");
     	Arrays.asList(strarray).stream().forEach(m -> System.out.println("Array element:"+m));
     	ArrayList<String> nodeList = new ArrayList<String>();
     	Boolean notFound = true;
     	int i = strarray.length -2;
     	String regex = "\\d+";
     	while(notFound){
     		if(!(strarray[i].matches(regex))){
     			
     			if(strarray[i].equalsIgnoreCase(firstWord)) {
     				nodeList.add(strarray[i]);
     				
     			}
     			else {
     				nodeList.add(firstWord);
     				nodeList.add(strarray[i]);
     			}
     			
     			notFound = false;
     			
     		}
     		
     		 i--;
     				
     	}   	
     	return nodeList;		 
	 }
	 
	 public String getReferenceJson(String resourceURI)
	 {
		 RestTemplate restTemplate = new RestTemplate();
		 String baseUri = "http://hapi.fhir.org/baseR4";
		 String uri = baseUri +"/"+ resourceURI;
		 ResponseEntity<String> response
		  = restTemplate.getForEntity(uri , String.class);
		 return response.getBody();		 
	 }
}
	 
