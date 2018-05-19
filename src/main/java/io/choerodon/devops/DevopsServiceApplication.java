package io.choerodon.devops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.choerodon.resource.annoation.EnableChoerodonResourceServer;

@EnableEurekaClient
@EnableFeignClients("io.choerodon")
@EnableScheduling
@SpringBootApplication
@EnableChoerodonResourceServer
public class DevopsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevopsServiceApplication.class, args);
    }

}
