package io.choerodon.devops.infra.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.choerodon.devops.app.eventhandler.pipeline.step.DevopsCiDockerBuildStepHandlerImpl;
import io.choerodon.devops.app.eventhandler.pipeline.step.DevopsCiImageBuildStepHandler;
import io.choerodon.devops.app.eventhandler.pipeline.step.DevopsCiKanikoBuildStepHandlerImpl;
import io.choerodon.devops.app.service.CiTemplateDockerService;
import io.choerodon.devops.app.service.DevopsCiDockerBuildConfigService;

/**
 * 根据配置注入不同的bean
 *
 */
@Configuration
public class PipelineConfiguration {

    @Bean
    @ConditionalOnProperty(value = "devops.ci.image-build-type", havingValue = "docker")
    public DevopsCiImageBuildStepHandler devopsCiDockerBuildStepHandler(DevopsCiDockerBuildConfigService devopsCiDockerBuildConfigService, CiTemplateDockerService ciTemplateDockerService) {
        return new DevopsCiDockerBuildStepHandlerImpl(devopsCiDockerBuildConfigService, ciTemplateDockerService);
    }

    @Bean
    @ConditionalOnProperty(value = "devops.ci.image-build-type", havingValue = "kaniko", matchIfMissing = true)
    public DevopsCiImageBuildStepHandler devopsCiKanikoBuildStepHandler(DevopsCiDockerBuildConfigService devopsCiDockerBuildConfigService, CiTemplateDockerService ciTemplateDockerService) {
        return new DevopsCiKanikoBuildStepHandlerImpl(devopsCiDockerBuildConfigService, ciTemplateDockerService);
    }
}
