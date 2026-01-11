package com.gametout.gametout;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class GametoutApplication {

	public static void main(String[] args) {
        new SpringApplicationBuilder(GametoutApplication.class)
            .web(WebApplicationType.SERVLET)
            .run(args);
    }

}
