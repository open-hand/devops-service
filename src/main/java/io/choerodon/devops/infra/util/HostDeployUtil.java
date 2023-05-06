package io.choerodon.devops.infra.util;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.impl.DevopsClusterServiceImpl;
import io.choerodon.devops.infra.dto.repo.DockerDeployDTO;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/16 11:12
 */
public class HostDeployUtil {

    private static final String HOST_COMMAND_TEMPLATE;
    private static final String FILE_DOWNLOAD_WITH_AUTHENTICATION_COMMAND = "rm -rf %s && curl -fsSLo \"%s\" -u \"%s:%s\" \"%s\"";
    private static final String FILE_DOWNLOAD_COMMAND = "rm -rf %s && curl -fsSLo \"%s\" \"%s\"";

    private HostDeployUtil() {
    }

    static {
        try (InputStream inputStream = DevopsClusterServiceImpl.class.getResourceAsStream("/shell/host_command_template.sh")) {
            HOST_COMMAND_TEMPLATE = org.apache.commons.io.IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException("devops.load.install.deploy.sh");
        }
    }

    public static String getDockerRunCmd(DockerDeployDTO dockerDeployDTO, String value) {
        String[] strings = value.split("\n");
        StringBuilder values = new StringBuilder();
        for (String s : strings) {
            s = trim(s);
            if (!s.startsWith("#")) {
                values.append(s).append("\n");
            }
        }
        if (ObjectUtils.isEmpty(values.toString()) || Boolean.FALSE.equals(checkInstruction("image", values.toString()))) {
            throw new CommonException("devops.instruction");
        }

        // 判断镜像是否存在 存在删除 部署
        int lastIndexOfColon = dockerDeployDTO.getImage().lastIndexOf(":");
        String tag = dockerDeployDTO.getImage().substring(lastIndexOfColon + 1);
        values = new StringBuilder(values.toString().replace("${containerName}", dockerDeployDTO.getContainerName()).replace("${imageName}", dockerDeployDTO.getImage()));

        String result = "";
        result += "WORK_DIR=" + (ObjectUtils.isEmpty(dockerDeployDTO.getWorkDir()) ? HostDeployUtil.getWorkingDir(Long.parseLong(dockerDeployDTO.getInstanceId()), dockerDeployDTO.getAppCode(), dockerDeployDTO.getVersion()) : dockerDeployDTO.getWorkDir()) + "\n";
        result += "cd ${WORK_DIR}" + "\n";
        result += "export IMAGE_TAG=" + tag + "\n";
        result += values;
        return result;
    }

    public static String getWorkingDir(Long instanceId, String appCode, String version) {
        if ("1".equals(version)) {
            return "/var/choerodon/" + instanceId;
        } else {
            return "/var/choerodon/" + appCode;
        }
    }


    public static String getDownloadCommand(String pullUserId, String pullUserPassword, String downloadUrl, String appFile) {
        if (!ObjectUtils.isEmpty(pullUserId) && !ObjectUtils.isEmpty(pullUserPassword)) {
            return String.format(FILE_DOWNLOAD_WITH_AUTHENTICATION_COMMAND, appFile, appFile, pullUserId, pullUserPassword, downloadUrl);
        } else {
            return String.format(FILE_DOWNLOAD_COMMAND, appFile, appFile, downloadUrl);
        }
    }

    private static Boolean checkInstruction(String type, String instruction) {
        if (type.equals("jar")) {
            return instruction.contains("${jar}");
        } else {
            return instruction.contains(" -d ");
        }
    }

    public static String getCommand(Map<String, String> params, String command) {
        params.put("{{ COMMAND }}", removeComments(command));
        return FileUtil.replaceReturnString(HOST_COMMAND_TEMPLATE, params);
    }

    private static String removeComments(String rawCommand) {
        StringBuilder commandSB = new StringBuilder();
        String[] lines = rawCommand.split("\n");
        for (String line : lines) {
            line = trim(line);
            if (line.length() > 0 && !line.startsWith("#")) {
                commandSB.append(line).append("\n");
            }
        }
        if (commandSB.length() > 0) {
            return commandSB.substring(0, commandSB.length() - 1);
        } else {
            return commandSB.toString();
        }
    }

    public static Boolean checkKillCommandExist(String deleteCommand) {
        if (ObjectUtils.isEmpty(deleteCommand)) {
            return false;
        }
        return !ObjectUtils.isEmpty(removeComments(Base64Util.decodeBuffer(deleteCommand)));
    }

    public static String genDockerRunCmd(DockerDeployDTO dockerDeployDTO, String value) {
        int lastIndexOfColon = dockerDeployDTO.getImage().lastIndexOf(":");
        String tag = dockerDeployDTO.getImage().substring(lastIndexOfColon + 1);
        String values = value.replace("${containerName}", dockerDeployDTO.getContainerName()).replace("${imageName}", dockerDeployDTO.getImage());
        String result = "";
        result += "WORK_DIR=" + (ObjectUtils.isEmpty(dockerDeployDTO.getWorkDir()) ? HostDeployUtil.getWorkingDir(Long.parseLong(dockerDeployDTO.getInstanceId()), dockerDeployDTO.getAppCode(), dockerDeployDTO.getVersion()) : dockerDeployDTO.getWorkDir()) + "\n";
        result += "cd ${WORK_DIR}" + "\n";
        result += "export IMAGE_TAG=" + tag + "\n";
        result += values;
        return result;
    }

    public static Boolean checkHealthProbExit(String deleteCommand) {
        if (ObjectUtils.isEmpty(deleteCommand)) {
            return false;
        }
        return !ObjectUtils.isEmpty(removeComments(Base64Util.decodeBuffer(deleteCommand)));
    }

    public static String trim(String s) {
        if (ObjectUtils.isEmpty(s)) {
            return s;
        }
        while (s.charAt(0) == ' ') {
            s = s.substring(1);
        }
        while (s.charAt(s.length() - 1) == ' ') {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

}
