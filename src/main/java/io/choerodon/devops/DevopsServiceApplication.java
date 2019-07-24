package io.choerodon.devops;

import io.choerodon.resource.annoation.EnableChoerodonResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableFeignClients("io.choerodon")
@EnableEurekaClient
@EnableDiscoveryClient
@SpringBootApplication
@EnableChoerodonResourceServer
@EnableAsync
public class DevopsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevopsServiceApplication.class, args);
    }

}
