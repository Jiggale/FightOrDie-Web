package com.fod.fod_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.fod")
@MapperScan("com.fod.mapper")
public class FodBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FodBackendApplication.class, args);
	}

}
