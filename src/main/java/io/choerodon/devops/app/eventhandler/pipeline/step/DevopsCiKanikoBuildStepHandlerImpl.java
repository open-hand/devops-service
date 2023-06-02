package io.choerodon.devops.app.eventhandler.pipeline.step;

import java.util.List;

import io.choerodon.devops.app.service.CiTemplateDockerService;
import io.choerodon.devops.app.service.DevopsCiDockerBuildConfigService;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/6/21 11:08
 */
public class DevopsCiKanikoBuildStepHandlerImpl extends DevopsCiImageBuildStepHandler {

    public DevopsCiKanikoBuildStepHandlerImpl(DevopsCiDockerBuildConfigService devopsCiDockerBuildConfigService, CiTemplateDockerService ciTemplateDockerService) {
        super(devopsCiDockerBuildConfigService, ciTemplateDockerService);
    }

    protected void generateBuildAndScanImageCmd(boolean skipTlsVerify, boolean imageScan, Long configId, String dockerBuildContextDir, String dockerFilePath, List<String> commands) {
        String rawCommand = "kaniko_build %s %s %s";
        commands.add(String.format(rawCommand, skipTlsVerify ? "--skip-tls-verify=true " : "--skip-tls-verify=false ", dockerBuildContextDir, dockerFilePath));
        //kaniko推镜像成功后可以执行trivy  这里是将镜像扫描的结果保存为json文件 以commmit_tag作为文件的名字 这个文件存在于runner的 /builds/orgCode-projectCode/appCode下，runner的pod停掉以后会自动删除
        if (imageScan) {
            String resolveCommond = "trivyScanImage %s";
            commands.add(String.format(resolveCommond, configId));
        }
        commands.add("skopeo_copy");
    }
}
