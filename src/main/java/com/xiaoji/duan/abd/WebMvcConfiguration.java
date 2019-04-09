package com.xiaoji.duan.abd;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Value("${picture.address}")
    private  String picture;
    @Value("${sapicture.address}")
    private  String sapicture;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        //addResourceHandler是指你想在url请求的路径

        //addResourceLocations是图片存放的真实路径salogos

        registry.addResourceHandler("/logos/**").addResourceLocations("file:"+picture);
        registry.addResourceHandler("/salogos/**").addResourceLocations("file:"+sapicture);

    }
}