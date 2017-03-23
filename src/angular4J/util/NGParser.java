package angular4J.util;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import angular4J.io.ByteArrayCache;

/**
 * utility class for Angular4J
 */
@SuppressWarnings("serial")
public class NGParser implements Serializable {

   private NGParser() {
      this.initJson();
   }

   private static NGParser instance;

   private transient Gson mainSerializer;
   private HttpServletRequest request;

   private static final void createInstance() {
      instance = new NGParser();
   }

   public static final synchronized NGParser getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance;
   }

   private class ByteArrayJsonAdapter implements JsonSerializer<Object> {

      public ByteArrayJsonAdapter() {}

      private String getContextPath(HttpServletRequest request) {
         StringBuilder contextPath = new StringBuilder(request.getScheme());
         contextPath.append("://");
         contextPath.append(request.getServerName());
         contextPath.append(":");
         contextPath.append(request.getServerPort());
         contextPath.append(request.getServletContext().getContextPath());
         contextPath.append("/");

         return contextPath.toString();
      }

      @Override
      public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
         if (request == null) {
            return null;
         }

         byte[] bytes = null;
         if (src instanceof NGLob) {
            bytes = ((NGLob) src).getBytes();
         } else if (src instanceof byte[]) {
            bytes = (byte[]) src;
         } else {
            return null;
         }

         String id = String.valueOf(UUID.randomUUID());
         ByteArrayCache.getInstance().getCache().put(id, bytes);

         String result = this.getContextPath(request) + "lob/" + id + "?" + Calendar.getInstance().getTimeInMillis();

         return new JsonPrimitive(result);
      }
   }

   private byte[] getBytesFromJson(JsonElement element) {
      String value = element.getAsString();
      if (CommonUtils.isStrValid(value)) {
         try {
            if (value.contains(Constants.DATA_MARK) && value.contains(Constants.BASE64_MARK)) {
               value = value.substring(value.indexOf(Constants.BASE64_MARK) + Constants.BASE64_MARK.length());
            }
            if (value.length() > 0) {
               return DatatypeConverter.parseBase64Binary(value);
            }
         }
         catch (Exception e) {}
      }
      return null;
   }

   private JsonPrimitive getBase64Json(String type, byte[] bytes) {
      if (bytes != null && bytes.length > 0) {
         if (type == null || type.trim().length() == 0) {
            type = NGBase64.FORM_DATA_TYPE;
         }

         try {
            return new JsonPrimitive(Constants.DATA_MARK + type + Constants.BASE64_MARK + Base64.getEncoder().encodeToString(bytes).trim());
         }
         catch (Exception e) {}

      }
      return null;
   }

   public String serialize(Object object) {
      return this.serialize(object, null);
   }

   public String serialize(Object object, HttpServletRequest request) {
      if (object == null) {
         return null;
      }

      this.request = request;

      return mainSerializer.toJson(object);
   }

   public JsonElement deserialize(String json) {
      return (JsonElement) this.deserialize(json, JsonElement.class);
   }

   public Object deserialize(JsonElement element, Type type) {
      return mainSerializer.fromJson(element, type);
   }

   public Object deserialize(String json, Type type) {
      return mainSerializer.fromJson(json, type);
   }

   public void initJson() {
      GsonBuilder builder = new GsonBuilder();

      builder.serializeNulls();

      builder.setExclusionStrategies(NGConfig.getGsonExclusionStrategy());

      builder.registerTypeAdapter(NGLob.class, new ByteArrayJsonAdapter());

      // ---NGBASE64---
      builder.registerTypeAdapter(NGBase64.class, new JsonSerializer<NGBase64>(){

         @Override
         public JsonElement serialize(NGBase64 src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
               return getBase64Json(src.getType(), src.getBytes());
            }
            return null;
         }
      });

      builder.registerTypeAdapter(NGBase64.class, new JsonDeserializer<NGBase64>(){

         @Override
         public NGBase64 deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {
            byte[] bytes = getBytesFromJson(element);
            if (bytes != null && bytes.length > 0) {
               return new NGBase64(bytes);
            }
            return null;
         }
      });

      // ---DATES---
      final SimpleDateFormat dateFormat = new SimpleDateFormat("'" + Constants.DATA_MARK + Constants.DATE_UTC_MARK + "'yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

      if (dateFormat != null && NGConfig.getProperty("TIME_ZONE") != null) {
         dateFormat.setTimeZone(TimeZone.getTimeZone(NGConfig.getProperty("TIME_ZONE")));
      }

      builder.registerTypeAdapter(java.sql.Date.class, new JsonSerializer<java.sql.Date>(){

         @Override
         public JsonElement serialize(java.sql.Date src, Type typeOfSrc, JsonSerializationContext context) {

            if (src != null) {
               Calendar cal = Calendar.getInstance();
               cal.setTime(src);
               cal.set(Calendar.HOUR_OF_DAY, 0);
               cal.set(Calendar.MINUTE, 0);
               cal.set(Calendar.SECOND, 0);
               cal.set(Calendar.MILLISECOND, 0);

               return new JsonPrimitive(dateFormat.format(cal.getTime()));
            }
            return null;
         }
      });

      builder.registerTypeAdapter(java.sql.Date.class, new JsonDeserializer<java.sql.Date>(){

         @Override
         public java.sql.Date deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String dateFormated = element.getAsString();
               if (CommonUtils.isStrValid(dateFormated)) {
                  Calendar cal = Calendar.getInstance();
                  cal.setTime(dateFormat.parse(dateFormated));
                  cal.set(Calendar.HOUR_OF_DAY, 0);
                  cal.set(Calendar.MINUTE, 0);
                  cal.set(Calendar.SECOND, 0);
                  cal.set(Calendar.MILLISECOND, 0);
                  return new java.sql.Date(cal.getTime().getTime());
               }
            }
            catch (Exception e) {}

            return null;
         }

      });

      builder.registerTypeAdapter(java.sql.Time.class, new JsonSerializer<java.sql.Time>(){

         @Override
         public JsonElement serialize(java.sql.Time src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
               Calendar cal = Calendar.getInstance();
               cal.setTime(src);

               return new JsonPrimitive(dateFormat.format(cal.getTime()));
            }
            return null;
         }

      });

      builder.registerTypeAdapter(java.sql.Time.class, new JsonDeserializer<java.sql.Time>(){

         @Override
         public java.sql.Time deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String dateFormated = element.getAsString();
               if (CommonUtils.isStrValid(dateFormated)) {
                  Calendar cal = Calendar.getInstance();
                  cal.setTime(dateFormat.parse(dateFormated));

                  return new java.sql.Time(cal.getTime().getTime());
               }
            }
            catch (Exception e) {}

            return null;
         }

      });

      builder.registerTypeAdapter(java.sql.Timestamp.class, new JsonSerializer<java.sql.Timestamp>(){

         @Override
         public JsonElement serialize(java.sql.Timestamp src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
               Calendar cal = Calendar.getInstance();
               cal.setTime(src);

               return new JsonPrimitive(dateFormat.format(cal.getTime()));
            }
            return null;
         }

      });

      builder.registerTypeAdapter(java.sql.Timestamp.class, new JsonDeserializer<java.sql.Timestamp>(){

         @Override
         public java.sql.Timestamp deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String dateFormated = element.getAsString();
               if (CommonUtils.isStrValid(dateFormated)) {
                  Calendar cal = Calendar.getInstance();
                  cal.setTime(dateFormat.parse(dateFormated));

                  return new java.sql.Timestamp(cal.getTime().getTime());
               }
            }
            catch (Exception e) {}

            return null;
         }

      });

      builder.registerTypeAdapter(java.util.Date.class, new JsonSerializer<java.util.Date>(){

         @Override
         public JsonElement serialize(java.util.Date src, Type typeOfSrc, JsonSerializationContext context) {
            return src == null ? null : new JsonPrimitive(dateFormat.format(src));
         }

      });

      builder.registerTypeAdapter(java.util.Date.class, new JsonDeserializer<java.util.Date>(){

         @Override
         public java.util.Date deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String dateFormated = element.getAsString();
               if (CommonUtils.isStrValid(dateFormated)) {
                  return dateFormat.parse(dateFormated);
               }
            }
            catch (Exception e) {}

            return null;
         }

      });

      // ---BOOLEAN---
      builder.registerTypeAdapter(boolean.class, new JsonDeserializer<java.lang.Boolean>(){

         @Override
         public java.lang.Boolean deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String value = element.getAsString();
               if (CommonUtils.isStrValid(value)) {
                  return Boolean.parseBoolean(value);
               }
            }
            catch (Exception e) {}

            return false;
         }
      });

      builder.registerTypeAdapter(java.lang.Boolean.class, new JsonDeserializer<java.lang.Boolean>(){

         @Override
         public java.lang.Boolean deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String value = element.getAsString();
               if (CommonUtils.isStrValid(value)) {
                  return Boolean.parseBoolean(value);
               }
            }
            catch (Exception e) {}

            return null;
         }
      });

      // ---BYTE---
      builder.registerTypeAdapter(byte.class, new JsonDeserializer<java.lang.Byte>(){

         @Override
         public java.lang.Byte deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String value = element.getAsString();
               if (CommonUtils.isStrValid(value)) {
                  return Byte.parseByte(value);
               }
            }
            catch (Exception e) {}

            return 0;
         }
      });

      builder.registerTypeAdapter(java.lang.Byte.class, new JsonDeserializer<java.lang.Byte>(){

         @Override
         public java.lang.Byte deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String value = element.getAsString();
               if (CommonUtils.isStrValid(value)) {
                  return Byte.parseByte(value);
               }
            }
            catch (Exception e) {}

            return null;
         }
      });

      // ---SHORT---
      builder.registerTypeAdapter(short.class, new JsonDeserializer<java.lang.Short>(){

         @Override
         public java.lang.Short deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String value = element.getAsString();
               if (CommonUtils.isStrValid(value)) {
                  return Short.parseShort(value);
               }
            }
            catch (Exception e) {}

            return 0;
         }
      });

      builder.registerTypeAdapter(java.lang.Short.class, new JsonDeserializer<java.lang.Short>(){

         @Override
         public java.lang.Short deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String value = element.getAsString();
               if (CommonUtils.isStrValid(value)) {
                  return Short.parseShort(value);
               }
            }
            catch (Exception e) {}

            return null;
         }
      });

      // ---INT---
      builder.registerTypeAdapter(int.class, new JsonDeserializer<java.lang.Integer>(){

         @Override
         public java.lang.Integer deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String value = element.getAsString();
               if (CommonUtils.isStrValid(value)) {
                  return Integer.parseInt(value);
               }
            }
            catch (Exception e) {}

            return 0;
         }
      });

      builder.registerTypeAdapter(java.lang.Integer.class, new JsonDeserializer<java.lang.Integer>(){

         @Override
         public java.lang.Integer deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String value = element.getAsString();
               if (CommonUtils.isStrValid(value)) {
                  return Integer.parseInt(value);
               }
            }
            catch (Exception e) {}

            return null;
         }
      });

      // ---LONG---
      builder.registerTypeAdapter(long.class, new JsonDeserializer<java.lang.Long>(){

         @Override
         public java.lang.Long deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String value = element.getAsString();
               if (CommonUtils.isStrValid(value)) {
                  return Long.parseLong(value);
               }
            }
            catch (Exception e) {}

            return 0l;
         }
      });

      builder.registerTypeAdapter(java.lang.Long.class, new JsonDeserializer<java.lang.Long>(){

         @Override
         public java.lang.Long deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String value = element.getAsString();
               if (CommonUtils.isStrValid(value)) {
                  return Long.parseLong(value);
               }
            }
            catch (Exception e) {}

            return null;
         }
      });

      // ---FLOAT---
      builder.registerTypeAdapter(float.class, new JsonDeserializer<java.lang.Float>(){

         @Override
         public java.lang.Float deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String value = element.getAsString();
               if (CommonUtils.isStrValid(value)) {
                  return Float.parseFloat(value);
               }
            }
            catch (Exception e) {}

            return 0f;
         }
      });

      builder.registerTypeAdapter(java.lang.Float.class, new JsonDeserializer<java.lang.Float>(){

         @Override
         public java.lang.Float deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String value = element.getAsString();
               if (CommonUtils.isStrValid(value)) {
                  return Float.parseFloat(value);
               }
            }
            catch (Exception e) {}

            return null;
         }
      });

      // ---FLOAT---
      builder.registerTypeAdapter(double.class, new JsonDeserializer<java.lang.Double>(){

         @Override
         public java.lang.Double deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String value = element.getAsString();
               if (CommonUtils.isStrValid(value)) {
                  return Double.parseDouble(value);
               }
            }
            catch (Exception e) {}

            return 0d;
         }
      });

      builder.registerTypeAdapter(java.lang.Double.class, new JsonDeserializer<java.lang.Double>(){

         @Override
         public java.lang.Double deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {

            try {
               String value = element.getAsString();
               if (CommonUtils.isStrValid(value)) {
                  return Double.parseDouble(value);
               }
            }
            catch (Exception e) {}

            return null;
         }
      });

      // ---BYTE[]---
      builder.registerTypeAdapter(byte[].class, new JsonSerializer<byte[]>(){

         @Override
         public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return getBase64Json(null, src);
         }
      });

      builder.registerTypeAdapter(byte[].class, new JsonDeserializer<byte[]>(){

         @Override
         public byte[] deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {
            return getBytesFromJson(element);
         }
      });

      builder.registerTypeAdapter(java.lang.Byte[].class, new JsonSerializer<java.lang.Byte[]>(){

         @Override
         public JsonElement serialize(java.lang.Byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
               return getBase64Json(null, ArrayUtils.toPrimitive(src));
            }
            return null;
         }
      });

      builder.registerTypeAdapter(java.lang.Byte[].class, new JsonDeserializer<java.lang.Byte[]>(){

         @Override
         public java.lang.Byte[] deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {
            byte[] bytes = getBytesFromJson(element);
            if (bytes != null) {
               return ArrayUtils.toObject(bytes);
            }
            return null;
         }
      });

      this.mainSerializer = builder.create();
   }
}