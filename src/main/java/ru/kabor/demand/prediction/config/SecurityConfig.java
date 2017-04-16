package ru.kabor.demand.prediction.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/** Settings for user access to pages and services */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
    @Value("${serverUser.adminLogin}")
    private String adminLogin;
    @Value("${serverUser.adminPassword}")
    private String adminPassword;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()
			.authorizeRequests()
				.antMatchers("/css/**", "/fonts/**","/images/**","/js/**","/locales/**").permitAll()			
				.antMatchers("/adminSingleMode.html").hasRole("ADMINISTRATOR")
				.antMatchers("/adminMultipleMode.html").hasRole("ADMINISTRATOR")
				.antMatchers("/adminElasticityMode.html").hasRole("ADMINISTRATOR")
				.antMatchers("/adminForecastAndElasticityMode.html").hasRole("ADMINISTRATOR")
				.antMatchers("/forecastsingle").hasRole("ADMINISTRATOR")
				.antMatchers("/forecastmultiple").hasRole("ADMINISTRATOR")
				.antMatchers("/elasticitymultiple").hasRole("ADMINISTRATOR")
				.antMatchers("/forecastandelasticitymultiple").hasRole("ADMINISTRATOR")
				.antMatchers("/report/*").hasRole("ADMINISTRATOR")
				.antMatchers("/login.html","/logout").permitAll()
				.and()
			.logout()
		        .logoutSuccessUrl("/login.html")
		        .and()
			.formLogin()
				.loginPage("/login.html").failureUrl("/login-error.html").loginProcessingUrl("/login.html").defaultSuccessUrl("/index.html");	
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.inMemoryAuthentication()
				  .withUser(adminLogin).password(adminPassword).roles("ADMINISTRATOR");
	}
}