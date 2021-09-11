package io.choerodon.devops.infra.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.impl.DevopsClusterServiceImpl;
import io.choerodon.devops.infra.dto.repo.DockerDeployDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/16 11:12
 */
public class HostDeployUtil {

    private static final String HOST_COMMAND_TEMPLATE;
    private static final String HOST_RUN_COMMAND_TEMPLATE;
    private static final String FILE_DOWNLOAD_WITH_AUTHENTICATION_COMMAND = "mkdir -p %s &&curl -o %s -u %s:%s %s";
    private static final String FILE_DOWNLOAD_COMMAND = "mkdir -p %s &&curl -o %s %s";

    private HostDeployUtil() {
    }

    static {
        try (InputStream inputStream = DevopsClusterServiceImpl.class.getResourceAsStream("/shell/host_command_template.sh")) {
            HOST_COMMAND_TEMPLATE = org.apache.commons.io.IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException("error.load.install.deploy.sh");
        }
        try (InputStream inputStream = DevopsClusterServiceImpl.class.getResourceAsStream("/shell/host_run_command_template.sh")) {
            HOST_RUN_COMMAND_TEMPLATE = org.apache.commons.io.IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException("error.load.install.deploy.sh");
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

    public static String genWorkingDir(Long instanceId) {
        return "$HOME/choerodon/" + instanceId + "/";
    }


    public static String genDownloadCommand(String pullUserId, String pullUserPassword, String downloadUrl, String workingPath, String appFile) {
        if (!StringUtils.isEmpty(pullUserId) && !StringUtils.isEmpty(pullUserPassword)) {
            return String.format(FILE_DOWNLOAD_WITH_AUTHENTICATION_COMMAND, workingPath, appFile, pullUserId, pullUserPassword, downloadUrl);
        } else {
            return String.format(FILE_DOWNLOAD_COMMAND, workingPath, appFile, downloadUrl);
        }
    }

    private static Boolean checkInstruction(String type, String instruction) {
        if (type.equals("jar")) {
            return instruction.contains("${jar}");
        } else {
            return instruction.contains("${containerName}") && instruction.contains("${imageName}") && instruction.contains(" -d ");
        }
    }

    public static String genCommand(Map<String, String> params, String command) {
        params.put("{{ COMMAND }}", removeComments(command));
        return FileUtil.replaceReturnString(HOST_COMMAND_TEMPLATE, params);
    }

    public static String genRunCommand(Map<String, String> params, String runCommand) {
        params.put("{{ COMMAND }}", removeComments(runCommand));
        return FileUtil.replaceReturnString(HOST_RUN_COMMAND_TEMPLATE, params);
    }

    private static String removeComments(String rawCommand) {
        StringBuilder commandSB = new StringBuilder();
        String[] lines = rawCommand.split("\n");
        for (String line : lines) {
            if (line.length() > 0 && !line.contains("#")) {
                commandSB.append(line);
            }
        }
        return commandSB.toString();
    }
}
