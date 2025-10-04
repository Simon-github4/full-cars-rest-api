package com.fullcars.restapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/*@ComponentScan(
	    basePackages = "com.fullcars.restapi",
	    excludeFilters = @ComponentScan.Filter(
	        type = FilterType.REGEX,
	        pattern = "com\\.fullcars\\.restapi\\.facturacion\\..*"
	    )
	)*/
@EnableJpaRepositories(basePackages = "com.fullcars.restapi.repository")
@EntityScan(basePackages = "com.fullcars.restapi.model")
@SpringBootApplication(scanBasePackages = "com.fullcars.restapi")
@EnableAsync
public class ErpBackendFullcarsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErpBackendFullcarsApplication.class, args);
		
	}

}
