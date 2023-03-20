package io.choerodon.devops;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import io.choerodon.resource.annoation.EnableChoerodonResourceServer;

@EnableFeignClients("io.choerodon")
@EnableEurekaClient
@EnableDiscoveryClient
@SpringBootApplication
@EnableChoerodonResourceServer
@EnableAsync
public class DevopsServiceApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsServiceApplication.class);

    public static void main(String[] args) {
        try {
            String values = "# Default values for register-server.\n" +
                    "# This is a YAML-formatted file.\n" +
                    "# Declare variables to be passed into your templates.\n" +
                    "\n" +
                    "replicaCount: 1\n" +
                    "\n" +
                    "image:\n" +
                    "  repository: \"\"\n" +
                    "  pullPolicy: Always\n" +
                    "\n" +
                    "config:\n" +
                    "  port: 8080\n" +
                    "\n" +
                    "resources:\n" +
                    "  # We usually recommend not to specify default resources and to leave this as a conscious\n" +
                    "  # choice for the user. This also increases chances charts run on environments with little\n" +
                    "  # resources,such as Minikube. If you do want to specify resources,uncomment the following\n" +
                    "  # lines,adjust them as necessary,and remove the curly braces after 'resources:'.\n" +
                    "  limits:\n" +
                    "    # cpu: 100m\n" +
                    "    memory: 500Mi\n" +
                    "  requests:\n" +
                    "    # cpu: 100m\n" +
                    "    memory: 256Mi\n" +
                    "\n" +
                    "\n";


            SpringApplication.run(DevopsServiceApplication.class, args);
        } catch (Exception e) {
            LOGGER.error("start error",e);
        }
    }

    // 初始化redisTemplate
    @Primary
    @Bean
    public RedisTemplate<String, Object> devOpsRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }


    @Bean(name = "restTemplateForIp")
    public RestTemplate restTemplateForIp() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        //30s
        requestFactory.setConnectTimeout(301000);
        requestFactory.setReadTimeout(301000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }
}
