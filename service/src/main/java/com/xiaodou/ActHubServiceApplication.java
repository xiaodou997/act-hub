package com.xiaodou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author luoxiaodou
 */
@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class ActHubServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActHubServiceApplication.class, args);
    }

}

