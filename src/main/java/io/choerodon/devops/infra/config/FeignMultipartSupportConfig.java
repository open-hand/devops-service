package io.choerodon.devops.infra.config;


import feign.codec.Encoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.choerodon.devops.infra.util.FeignSpringFormEncoder;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:49 2019/9/6
 * Description:
 */
@Configuration
public class FeignMultipartSupportConfig {

    @Bean
    public Encoder encoder() {
        return new FeignSpringFormEncoder();
    }
}