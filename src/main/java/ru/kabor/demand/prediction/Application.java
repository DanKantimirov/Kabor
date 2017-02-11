package ru.kabor.demand.prediction;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import ru.kabor.demand.prediction.config.JpaConfig;
import ru.kabor.demand.prediction.config.SchedulerConfig;
import ru.kabor.demand.prediction.config.SecurityConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration
@ComponentScan(basePackages = { "ru.kabor.demand.prediction" })
public class Application {
	private static final Logger LOG = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) throws Exception {
		LOG.warn("App starting...");
		SpringApplication.run(new Class<?>[] { Application.class, JpaConfig.class, SecurityConfig.class, SchedulerConfig.class }, args);
		LOG.warn("App started");
	}
}