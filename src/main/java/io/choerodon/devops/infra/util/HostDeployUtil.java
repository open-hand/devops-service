package io.choerodon.devops.infra.util;

import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.infra.dto.repo.DockerDeployDTO;
import io.choerodon.devops.infra.dto.repo.JavaDeployDTO;

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

    public static String genJavaRunCmd(JavaDeployDTO javaDeployDTO, JarDeployVO jarDeployVO, Long instanceId) {
        StringBuilder cmdStr = new StringBuilder();
        String workingPath = "$HOME/choerodon/" + instanceId;

        cmdStr.append(String.format("mkdir -p %s/temp-jar && ", workingPath));
        cmdStr.append(String.format("mkdir -p %s/temp-log && ", workingPath));
        String jarPathAndName = workingPath + "/temp-jar/" + jarDeployVO.getProdJarInfoVO().getArtifactId();
        // 2.2
        String curlExec = String.format("curl -o %s -u %s:%s %s ",
                jarPathAndName,
                javaDeployDTO.getJarPullInfoDTO().getPullUserId(),
                javaDeployDTO.getJarPullInfoDTO().getPullUserPassword(),
                javaDeployDTO.getJarPullInfoDTO().getDownloadUrl());
        cmdStr.append(curlExec).append(" && ");

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
        String javaJarExec = values.replace("${jar}", jarPathAndName);

        cmdStr.append(javaJarExec);
        StringBuilder finalCmdStr = new StringBuilder("nohup bash -c \"").append(cmdStr).append("\"").append(String.format(" > %s 2>&1 &", logPathAndName));
        return finalCmdStr.toString();
    }

    private static Boolean checkInstruction(String type, String instruction) {
        if (type.equals("jar")) {
            return instruction.contains("${jar}");
        } else {
            return instruction.contains("${containerName}") && instruction.contains("${imageName}") && instruction.contains(" -d ");
        }
    }
}
