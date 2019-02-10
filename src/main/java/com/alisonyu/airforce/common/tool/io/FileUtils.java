package com.alisonyu.airforce.common.tool.io;

import com.google.common.base.Charsets;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.stream.Collectors;

public class FileUtils {


    public static boolean existFiles(String path){
        File file = new File(path);
        return file.exists() || existResources(path);
    }

    public static boolean existResources(String path){
        URL fileUrl = Thread.currentThread().getContextClassLoader().getResource(path);
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (is!= null){
            try {
                is.close();
            } catch (IOException e) {
                return true;
            }
            return true;
        }else{
            return false;
        }
    }


    public static String read(String path){
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
        return br.lines().collect(Collectors.joining());
    }

    public static JsonObject readJsonObject(String path){
        String content = read(path);
        return new JsonObject(content);
    }


}
