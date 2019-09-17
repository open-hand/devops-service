package io.choerodon.devops;

import java.util.Set;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import io.choerodon.resource.annoation.EnableChoerodonResourceServer;


@EnableFeignClients("io.choerodon")
@EnableEurekaClient
@EnableDiscoveryClient
@SpringBootApplication
@EnableChoerodonResourceServer
@EnableAsync
public class DevopsServiceApplication {


    private static final String CLUSTER_SESSION = "cluster-sessions-catch";


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(DevopsServiceApplication.class, args);
    }

    //初始化redisTemplate
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    //初始化servletServerContainer 消息缓冲池大小
    @Bean
    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(32768);
        container.setMaxBinaryMessageBufferSize(32768);
        return container;
    }

    //清空agent连接信息
    @PostConstruct
    public void start() {
        Set<Object> keySet = redisTemplate.opsForHash().keys(CLUSTER_SESSION);
        if (!keySet.isEmpty()) {
            redisTemplate.opsForHash().delete(CLUSTER_SESSION, keySet.toArray());
        }
    }

}
