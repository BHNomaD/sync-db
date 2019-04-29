package com.nomad.app;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.app.model.EnumerationList;
import com.nomad.app.model.SinkDBConn;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

/**
 * @author Shariful Islam
 */
@SpringBootApplication
@PropertySource("classpath:application.yml")
//@ComponentScan
public class Application implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    private Environment env;

    @Value("${test.value}")
    private Integer testValue;


    public static void main(String[] args) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.warn("Uncaught error in thread {}", t.getName(), e));
        Utils.logMemoryStatus("Starting wdb-reports application");
        SpringApplication.run(Application.class, args);
    }

    @Bean(name = "datasource-01")
    public DataSource datasource01() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(env.getRequiredProperty("db1.driver"));
        dataSource.setJdbcUrl(env.getRequiredProperty("db1.url"));
        dataSource.setUsername(env.getRequiredProperty("db1.user"));
        dataSource.setPassword(env.getRequiredProperty("db1.password"));
        dataSource.setAutoCommit(true);
        dataSource.setMaximumPoolSize(env.getRequiredProperty("max.poolSize", Integer.class) * 2);
        return dataSource;
    }

    @Bean(name = "jdbc-01")
    public JdbcTemplate jdbcTemplate01(@Qualifier("datasource-01") DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setFetchSize(env.getRequiredProperty("db1.fetchSize", Integer.class));
        return jdbcTemplate;
    }

    @Bean(name = "datasource-02")
    public DataSource dataSource02() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(env.getRequiredProperty("db2.driver"));
        dataSource.setJdbcUrl(env.getRequiredProperty("db2.url"));
        dataSource.setUsername(env.getRequiredProperty("db2.user"));
        dataSource.setPassword(env.getRequiredProperty("db2.password"));
        dataSource.setAutoCommit(true);
        dataSource.setMaximumPoolSize(env.getRequiredProperty("max.poolSize", Integer.class) * 2);
        return dataSource;
    }

    @Bean(name = "jdbc-02")
    public JdbcTemplate jdbcTemplate02(@Qualifier("datasource-02") DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setFetchSize(env.getRequiredProperty("db2.fetchSize", Integer.class));
        return jdbcTemplate;
    }

    @Bean(name = "datasource-03")
    public DataSource dataSource03() {
        HikariDataSource dataSource = new HikariDataSource();
//        dataSource.setDriverClassName(env.getRequiredProperty("db3.driver"));
        dataSource.setJdbcUrl(env.getRequiredProperty("db3.url"));
        dataSource.setUsername(env.getRequiredProperty("db3.user"));
        dataSource.setPassword(env.getRequiredProperty("db3.password"));
        dataSource.setAutoCommit(true);
        dataSource.setMaximumPoolSize(env.getRequiredProperty("max.poolSize", Integer.class) * 2);
        return dataSource;
    }

    @Bean(name = "jdbc-03")
    public JdbcTemplate jdbcTemplate03(@Qualifier("datasource-03") DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setFetchSize(env.getRequiredProperty("db3.fetchSize", Integer.class));
        return jdbcTemplate;
    }

    @Bean(name = "sink-datasource-01")
    public DataSource sinkDataSource01() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(env.getRequiredProperty("sink-db1.driver"));
        dataSource.setJdbcUrl(env.getRequiredProperty("sink-db1.url"));
        dataSource.setUsername(env.getRequiredProperty("sink-db1.user"));
        dataSource.setPassword(env.getRequiredProperty("sink-db1.password"));
        dataSource.setAutoCommit(true);
        dataSource.setMaximumPoolSize(env.getRequiredProperty("max.poolSize", Integer.class) * 2);
        return dataSource;
    }

    @Bean(name = "sink-jdbc-01")
    public JdbcTemplate sinkJdbcTemplate01(@Qualifier("sink-datasource-01") DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setFetchSize(env.getRequiredProperty("sink-db1.fetchSize", Integer.class));
        return jdbcTemplate;
    }

    @Bean(name = "sink-dbcon-01")
    public SinkDBConn sinkDBConn01(@Qualifier("sink-datasource-01") DataSource dataSource) {

        SinkDBConn sinkDBConn = new SinkDBConn(dataSource);
        sinkDBConn.getJdbc().setFetchSize(env.getRequiredProperty("sink-db1.fetchSize", Integer.class));
        sinkDBConn.getConfig().put(EnumerationList.Proeprties.DB_CONFIG_NAME, env.getRequiredProperty("db-config-name", String.class));

        return sinkDBConn;
    }

    @Bean
    public LobHandler lobHandler() {
        return new DefaultLobHandler();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(objectMapper);
        messageConverter.setPrettyPrint(true);
        converters.add(messageConverter);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
