package ru.kabor.demand.prediction.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()
			.authorizeRequests()
				.antMatchers("/css/**", "/fonts/**","/images/**","/js/**","/locales/**").permitAll()			
				.antMatchers("/index.html").hasRole("USER")
				.antMatchers("/multiple.html").hasRole("USER")
				.antMatchers("/uploadForm").hasRole("USER")
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
				  .withUser("analyst").password("secret").roles("USER");
	}
	
	/*private CsrfTokenRepository csrfTokenRepository() { 
	    HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository(); 
	    repository.setSessionAttributeName("_csrf");
	    return repository; 
	}*/
}