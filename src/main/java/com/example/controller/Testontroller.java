package com.example.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	        List<String> pathList = new ArrayList<String>();
	        try {
	        	pathList = JsonPath.using(conf).parse(str).read("$..reference");
	        }
	        catch(PathNotFoundException pathNotFoundException) {
	        	System.out.println("continue");
	        }
	        String response = null;
	        JsonObject jsonObject = new JsonObject();
	        jsonObject.addProperty("orignaljson",jsonContext.jsonString());
	        if(pathList!=null) {
	        for(String path : pathList) {
	        	
	            String resourceUri = jsonContext.read(path);
	            String regex = ".*/.*";
	            if(resourceUri.matches(regex)) {
	            response = getReferenceJson(resourceUri);
            	//String parsedResponse = JsonPath.parse(response).jsonString();
            	System.out.println("parsed response"+response);
	            ArrayList<String> keyList = filterJsonKey(path,resourceUri);
	            populateJsonWithRefrences(keyList,jsonObject,response);
	            }
	        }
	        }
	        	        
	        String preparedjson = jsonObject.toString();
	        String responsejson = removeSpecialCharacters(preparedjson);     
	        return responsejson;
	    }
	 
	 public String removeSpecialCharacters(String preparedjson) {
		 
		 return
		 preparedjson.replace("\\\"", "\"").replace("\"{","{").replace("}\"", "}").replace("\\\\\"", "\\\"")
		 .replace("\\\\\\\"", "\\\"")
 		.replace("\\\\n", "\\n")
 		.replace("\\\\\\n", "\\n");
 		
	 }
	 
	 public JsonObject populateJsonWithRefrences(ArrayList<String> keyList,JsonObject jsonObject,String responseJson) {
		 
		 JsonParser parser = new JsonParser();
		 JsonElement v1 = parser.parse(responseJson);
		 if(keyList.size()<2) {
//     		keyList.stream().forEach(key -> jsonObject.addProperty(key, responseJson));
			 keyList.stream().forEach(key -> jsonObject.add(key, v1));
     		System.out.println("json object with  patient"+jsonObject.toString());
     	}
     	else {
     		
     			JsonObject childJson = new JsonObject();
     			
     			childJson.addProperty(keyList.get(0), responseJson);
//     			String parseChildJson = childJson.toString();
     			System.out.println("child json"+childJson.toString());
     			
     			
     			String childJsonWithoutspecial = childJson.toString().replace("\\\"", "\"");
     			System.out.println("child json after replacing"+childJsonWithoutspecial);
     			if(jsonObject.has(keyList.get(1))) {
     				JsonElement value = jsonObject.get(keyList.get(1));
     				jsonObject.remove(keyList.get(1));
     				String newkey1 = keyList.get(1) + "_1";
     				
     				jsonObject.add(newkey1, value);
     				String newkey2 = keyList.get(1) + "_2";
     				jsonObject.addProperty(newkey2, childJsonWithoutspecial);
     				
     			}
     			else {
     			jsonObject.addProperty(keyList.get(1), childJsonWithoutspecial);	
     			}
     			System.out.println("final json after replacing"+jsonObject.toString());
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
	 
