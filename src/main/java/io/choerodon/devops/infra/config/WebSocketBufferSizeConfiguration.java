package io.choerodon.devops.infra.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * 配置WebSocket能接收的消息的大小
 *
 * @author zmf
 * @since 10/22/19
 */
@Configuration
public class WebSocketBufferSizeConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketBufferSizeConfiguration.class);

    /**
     * 单位 byte, 默认 128 * 1024，也就是128KiB
     * 涉及到这块的逻辑有：
     * 1. prometheus安装后返回的消息大小
     * 2. 批量部署或正常部署返回的数据大小
     * <p>
     * polaris的扫描接口不再通过ws，而是从http，所以不再和这个有关系
     */
    @Value("${websocket.buffer.maxTextMessageSize:131072}")
    private Integer maxTextMessageSize;
    @Value("${websocket.buffer.maxBinaryMessageSize:131072}")
    private Integer maxBinaryMessageSize;

    //初始化servletServerContainer 消息缓冲池大小
    @Bean
    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
        LOGGER.info("WebSocket buffer: The maxTextMessageSize now is {}", maxTextMessageSize);
        LOGGER.info("WebSocket buffer: The maxBinaryMessageSize now is {}", maxBinaryMessageSize);
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(maxTextMessageSize);
        container.setMaxBinaryMessageBufferSize(maxBinaryMessageSize);
        return container;
    }
}
