package com.hanshow.support.util;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

public class JsonUtils
{
  private static ObjectMapper objectMapper = new ObjectMapper();
  
  static
  {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }
  
  public static String slientToJsonString(Object object)
    throws JsonProcessingException
  {
    try
    {
      return objectMapper.writeValueAsString(object);
    }
    catch (JsonProcessingException ex)
    {
      throw ex;
    }
  }
  
  public static String toJsonString(Object object)
    throws JsonProcessingException
  {
    return objectMapper.writeValueAsString(object);
  }
  
  public static <T> T readJson(String jsonString, Class<T> clazz)
    throws IOException
  {
    return (T)objectMapper.readValue(jsonString, clazz);
  }
  
  @SuppressWarnings("unchecked")
public static <T> T readJson(String jsonString, TypeReference<T> clazz)
    throws IOException
  {
    return (T)objectMapper.readValue(jsonString, clazz);
  }
  
@SuppressWarnings("unchecked")
public static Map<String, String> jsonStringToMap(String str)
  {
    return (Map<String, String>)JSON.parse(str);
  }
}