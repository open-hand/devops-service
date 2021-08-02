package io.choerodon.devops.infra.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.app.service.impl.DevopsClusterServiceImpl;
import io.choerodon.devops.infra.dto.repo.DockerDeployDTO;
import io.choerodon.devops.infra.dto.repo.JarPullInfoDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/16 11:12
 */
public class HostDeployUtil {

    private static final String JAVA_DEPLOY_COMMAND_TEMPLATE;

    private HostDeployUtil() {
    }

    static {
        try (InputStream inputStream = DevopsClusterServiceImpl.class.getResourceAsStream("/shell/java_deploy.sh")) {
            JAVA_DEPLOY_COMMAND_TEMPLATE = org.apache.commons.io.IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException("error.load.java.deploy.sh");
        }
    }

    public static String genDockerRunCmd(DockerDeployDTO dockerDeployDTO, String value) {
        String[] strings = value.split("\n");
        String values = "";
        for (String s : strings) {
            if (s.length() > 0 && !s.contains("#") && s.contains("docker")) {
                values = s;
            }
        }
        if (StringUtils.isEmpty(values) || Boolean.FALSE.equals(checkInstruction("image", values))) {
            throw new CommonException("error.instruction");
        }

        // 判断镜像是否存在 存在删除 部署
        StringBuilder dockerRunExec = new StringBuilder();
        dockerRunExec.append(values.replace("${containerName}", dockerDeployDTO.getName()).replace("${imageName}", dockerDeployDTO.getImage()));
        return dockerRunExec.toString();
    }

    public static String genJavaRunCmd(JarPullInfoDTO jarPullInfoDTO, JarDeployVO jarDeployVO, Long instanceId) {
        Map<String, String> params = new HashMap<>();
        params.put("{{ WORKING_PATH }}", "$HOME/choerodon/" + instanceId);

        String workingPath = "$HOME/choerodon/" + instanceId;
        String jarPathAndName = workingPath + "/temp-jar/" + jarDeployVO.getProdJarInfoVO().getArtifactId();

        // 2.2
        params.put("{{ JAR_NAME }}", jarPathAndName);
        params.put("{{ USER_ID }}", jarPullInfoDTO.getPullUserId());
        params.put("{{ PASSWORD }}", jarPullInfoDTO.getPullUserPassword());
        params.put("{{ DOWNLOAD_URL }}", jarPullInfoDTO.getDownloadUrl());

        // 2.3
        String[] strings = jarDeployVO.getValue().split("\n");
        String values = "";
        for (String s : strings) {
            if (s.length() > 0 && !s.contains("#") && s.contains("java")) {
                values = s;
            }
        }
        if (StringUtils.isEmpty(values) || !values.contains("${jar}")) {
            throw new CommonException("error.instruction");
        }

        String logName = jarDeployVO.getProdJarInfoVO().getArtifactId().replace(".jar", ".log");
        String logPathAndName = workingPath + "/temp-log/" + logName;
        String javaJarExec = values.replace("${jar}", jarPathAndName) + String.format(" -DchoerodonInstanceName=%s", jarDeployVO.getName()) + String.format("> %s 2>&1", logPathAndName);
        params.put("{{ JAVA_JAR_EXEC }}", javaJarExec);

        return FileUtil.replaceReturnString(JAVA_DEPLOY_COMMAND_TEMPLATE, params);
    }

    private static Boolean checkInstruction(String type, String instruction) {
        if (type.equals("jar")) {
            return instruction.contains("${jar}");
        } else {
            return instruction.contains("${containerName}") && instruction.contains("${imageName}") && instruction.contains(" -d ");
        }
    }
}
