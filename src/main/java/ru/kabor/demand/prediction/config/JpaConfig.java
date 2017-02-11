package ru.kabor.demand.prediction.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ru.kabor.demand.prediction.Application;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = Application.class)
public class JpaConfig implements TransactionManagementConfigurer{
	    @Value("${dataSource.driverClassName}")
	    private String driver;
	    @Value("${dataSource.url}")
	    private String url;
	    @Value("${dataSource.username}")
	    private String username;
	    @Value("${dataSource.password}")
	    private String password;
	    @Value("${hibernate.dialect}")
	    private String dialect;
	    @Value("${hibernate.hbm2ddl.auto}")
	    private String hbm2ddlAuto;
	    @Value("${dataSource.maxPoolSize}")
	    private Integer maxPoolSize;
	    @Value("${dataSource.testQuery}")
	    private String testQuery;


	    @Bean
	    public DataSource configureDataSource() {
	        HikariConfig config = new HikariConfig();
	        config.setDriverClassName(driver);
	        config.setJdbcUrl(url);
	        config.setUsername(username);
	        config.setPassword(password);
	      //  config.setMaximumPoolSize(maxPoolSize);
	        config.setConnectionTestQuery(testQuery);
	        
	        //config.setDriverClassName("com.mysql.jdbc.Driver");
	        config.addDataSourceProperty("cachePrepStmts", "true");
	        config.addDataSourceProperty("prepStmtCacheSize", "250");
	        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
	        
	        return new HikariDataSource(config);
	    }

	    @Bean
	    public LocalContainerEntityManagerFactoryBean configureEntityManagerFactory() {
	        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
	        entityManagerFactoryBean.setDataSource(configureDataSource());
	        entityManagerFactoryBean.setPackagesToScan("ru.kabor");
	        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

	        Properties jpaProperties = new Properties();
	        jpaProperties.put(org.hibernate.cfg.Environment.DIALECT, dialect);
	        jpaProperties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, hbm2ddlAuto);
	        entityManagerFactoryBean.setJpaProperties(jpaProperties);

	        return entityManagerFactoryBean;
	    }

	    @Bean
	    public PlatformTransactionManager annotationDrivenTransactionManager() {
	        return new JpaTransactionManager();
	    }
	    
	    /*
	    @Bean
	    public LocalSessionFactoryBean sessionFactory() {
	      LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
	      sessionFactoryBean.setDataSource(dataSource());
	      sessionFactoryBean.setPackagesToScan(ENTITYMANAGER_PACKAGES_TO_SCAN);
	      Properties hibernateProperties = new Properties();
	      hibernateProperties.put("hibernate.dialect", HIBERNATE_DIALECT);
	      hibernateProperties.put("hibernate.show_sql", HIBERNATE_SHOW_SQL);
	      hibernateProperties.put("hibernate.hbm2ddl.auto", HIBERNATE_HBM2DDL_AUTO);
	      sessionFactoryBean.setHibernateProperties(hibernateProperties);
	      return sessionFactoryBean;
	    }

	    @Bean
	    public HibernateTransactionManager transactionManager() {
	      HibernateTransactionManager transactionManager = 
	          new HibernateTransactionManager();
	      transactionManager.setSessionFactory(sessionFactory().getObject());
	      return transactionManager;
	    }
	    
	    */
}
