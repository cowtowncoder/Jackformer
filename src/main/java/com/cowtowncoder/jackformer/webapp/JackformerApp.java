package com.cowtowncoder.jackformer.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.cowtowncoder.jackformer.webapp.config.JackConfig;

// Instead of @SpringBootApplication specify specific component annotations
// it typically provides
@SpringBootConfiguration
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
// Scan main level, config
@ComponentScan(basePackageClasses= {JackConfig.class, JackformerApp.class })
public class JackformerApp
{
	public static void main(String[] args) {
		SpringApplication.run(JackformerApp.class, args);
	}

	/*
     @Bean
     public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
          return args -> {
               System.out.println("Let's inspect the beans provided by Spring Boot:");

               String[] beanNames = ctx.getBeanDefinitionNames();
               java.util.Arrays.sort(beanNames);
               for (String beanName : beanNames) {
                    System.out.println(beanName);
               }

          };
     }
     */
}
