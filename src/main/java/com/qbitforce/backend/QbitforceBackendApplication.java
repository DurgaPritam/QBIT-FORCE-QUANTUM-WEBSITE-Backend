package com.qbitforce.backend;

import com.qbitforce.backend.config.EnvFileLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QbitforceBackendApplication {

    public static void main(String[] args) {
        EnvFileLoader.load();
        SpringApplication.run(QbitforceBackendApplication.class, args);
    }
}
