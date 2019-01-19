package com.alisonyu.airforce.tool;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class FileUtils {

    private static Logger logger = LoggerFactory.getLogger(FileUtils.class);


    public static boolean existFiles(String path){
        File file = new File(path);
        return file.exists() || existResources(path);
    }

    public static boolean existResources(String path){
        String filePath = Thread.currentThread().getContextClassLoader().getResource(path).getFile();
        File file = new File(filePath);
        return file.exists();
    }


    public static String read(String path){
        String filePath = Thread.currentThread().getContextClassLoader().getResource(path).getFile();
        File file = new File(filePath);
        try {
            return Files.readLines(file, Charsets.UTF_8)
                    .stream()
                    .collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("read file "+path+ " error!",e);
            throw new RuntimeException(e);
        }
    }

    public static JsonObject readJsonObject(String path){
        String content = read(path);
        return new JsonObject(content);
    }


}
