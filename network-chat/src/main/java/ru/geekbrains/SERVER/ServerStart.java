package ru.geekbrains.SERVER;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.SQLException;

public class ServerStart {
    public static void main(String[] args) throws SQLException {

        ApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");

        context.getBean("server", System.class);
//        new Server(9192);
    }
}
