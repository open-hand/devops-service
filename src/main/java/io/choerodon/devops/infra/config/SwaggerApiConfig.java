package io.choerodon.devops.infra.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.Tag;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/5/6 11:00
 */
@Configuration
public class SwaggerApiConfig {
    public static final String APP_EXTERNAL_CONFIG = "App external config";
    public static final String APP_SERVICE = "App service";
    public static final String APP_SERVICE_INSTANCE = "App service instance";
    public static final String DEVOPS_CD_PIPELINE = "Devops cd pipeline";

    @Autowired
    public SwaggerApiConfig(Docket docket) {
        docket.tags(new Tag(APP_EXTERNAL_CONFIG, "应用服务-外置仓库配置管理"),
                new Tag(APP_SERVICE, "应用服务管理"),
                new Tag(APP_SERVICE_INSTANCE, "应用服务实例管理"),
                new Tag(DEVOPS_CD_PIPELINE, "cd流水线内部调用相关接口"));
    }
}
