package com.alisonyu.airforce.common.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ShellHelper {

    public static String sh(String command){
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("sh","-c",command);
        try {
            Process process = processBuilder.start();
            int exitVal = process.waitFor();
            if (exitVal == 0){
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                return bufferedReader.lines().collect(Collectors.joining());
            }else{
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                return bufferedReader.lines().collect(Collectors.joining());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }


}
