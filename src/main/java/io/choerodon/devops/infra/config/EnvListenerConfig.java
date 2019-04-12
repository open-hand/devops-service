package io.choerodon.devops.infra.config;

import io.choerodon.websocket.helper.EnvListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by Sheep on 2019/4/12.
 */
@Configuration
public class EnvListenerConfig {



    @Bean("newEnvListener")
    EnvListener envListener(RedisTemplate<Object, Object> redisTemplate) {
        return new EnvListener(redisTemplate);
    }
}
