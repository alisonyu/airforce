package com.alisonyu.airforce.core.banner;

public class BannerManager {

    private static String banner = Banner.defaultBanner;

    public static void showBanner(){
        System.out.println(banner);
    }

    public static void registerBanner(String banner){
        BannerManager.banner = banner;
    }


}
