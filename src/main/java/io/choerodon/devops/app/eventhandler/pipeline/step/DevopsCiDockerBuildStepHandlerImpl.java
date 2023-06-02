package io.choerodon.devops.app.eventhandler.pipeline.step;

import java.util.List;

import io.choerodon.devops.app.service.CiTemplateDockerService;
import io.choerodon.devops.app.service.DevopsCiDockerBuildConfigService;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/6/21 11:05
 */
public class DevopsCiDockerBuildStepHandlerImpl extends DevopsCiImageBuildStepHandler {

    public DevopsCiDockerBuildStepHandlerImpl(DevopsCiDockerBuildConfigService devopsCiDockerBuildConfigService, CiTemplateDockerService ciTemplateDockerService) {
        super(devopsCiDockerBuildConfigService, ciTemplateDockerService);
    }

    /**
     * 生成docker构建命令
     * todo 由于docker 跳过证书配置是在服务端配置，目前生成的docker构建指令中没有配置相关字段，后续处理
     *
     * @param skipTlsVerify
     * @param imageScan
     * @param jobId
     * @param dockerBuildContextDir
     * @param dockerFilePath
     * @param commands
     */
    protected void generateBuildAndScanImageCmd(boolean skipTlsVerify, boolean imageScan, Long configId, String dockerBuildContextDir, String dockerFilePath, List<String> commands) {

        String dockerBuildCmd = "docker build -t ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${C7N_VERSION} --file %s %s";
        String dockerPushCmd = "docker push ${DOCKER_REGISTRY}/${GROUP_NAME}/${PROJECT_NAME}:${C7N_VERSION}";
        commands.add(String.format(dockerBuildCmd, dockerFilePath, dockerBuildContextDir));
        if (imageScan) {
            String resolveCommond = "trivyScanImageForDocker %s";
            commands.add(String.format(resolveCommond, configId));
        }
        commands.add(dockerPushCmd);

    }
}
