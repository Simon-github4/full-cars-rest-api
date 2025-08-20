package com.fullcars.restapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

//@ComponentScan({"com.fullcars.restapi"})
@EnableJpaRepositories(basePackages = "com.fullcars.restapi.repository")
@EntityScan(basePackages = "com.fullcars.restapi.model")
@SpringBootApplication(scanBasePackages = "com.fullcars.restapi")
public class ErpBackendFullcarsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErpBackendFullcarsApplication.class, args);
		
	}

}
