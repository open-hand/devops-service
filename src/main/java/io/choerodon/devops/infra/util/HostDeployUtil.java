package io.choerodon.devops.infra.util;

import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.repo.DockerDeployDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/16 11:12
 */
public class HostDeployUtil {
    public static String genDockerRunCmd(DockerDeployDTO dockerDeployDTO, String value) {
        String[] strings = value.split("\n");
        String values = "";
        for (String s : strings) {
            if (s.length() > 0 && !s.contains("#") && s.contains("docker")) {
                values = s;
            }
        }
        if (StringUtils.isEmpty(values) || !checkInstruction("image", values)) {
            throw new CommonException("error.instruction");
        }

        // 判断镜像是否存在 存在删除 部署
        StringBuilder dockerRunExec = new StringBuilder();
        dockerRunExec.append(values.replace("${containerName}", dockerDeployDTO.getName()).replace("${imageName}", dockerDeployDTO.getImage()));
        return dockerRunExec.toString();
    }

    private static Boolean checkInstruction(String type, String instruction) {
        if (type.equals("jar")) {
            return instruction.contains("${jar}");
        } else {
            return instruction.contains("${containerName}") && instruction.contains("${imageName}") && instruction.contains(" -d ");
        }
    }
}
