package ru.geekbrains.SERVER;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class SpringContext {

    @Bean
    public Server server (int port, DataSource dataSource) throws SQLException {
        return new Server(port, dataSource);
    }

    @Bean
    public int port () {
        return 9192;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:mysql://10.10.0.2:3306/chatusers");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername("root");
        dataSource.setPassword("Alcateldc");
        return dataSource;
    }
}
