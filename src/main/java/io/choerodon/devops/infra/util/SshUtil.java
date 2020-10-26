package io.choerodon.devops.infra.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ExecResultInfoVO;
import io.choerodon.devops.api.vo.HostConnectionVO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.repo.C7nImageDeployDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusDeployDTO;
import io.choerodon.devops.infra.enums.CdHostAccountType;
import io.choerodon.devops.infra.enums.HostSourceEnum;
import io.choerodon.devops.infra.mapper.DevopsHostMapper;

/**
 * @author zmf
 * @since 2020/9/15
 */
@Component
public class SshUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshUtil.class);
    /**
     * 默认超时时间, 10秒
     */
    private static final int DEFAULT_TIMEOUT_MILLISECONDS = 10000;
    private static final Integer WAIT_SECONDS = 6;
    private static final String ERROR_DOCKER_LOGIN = "error.docker.login";
    private static final String ERROR_DOCKER_PULL = "error.docker.pull";
    private static final String ERROR_DOCKER_RUN = "error.docker.run";
    private static final String ERROR_DOWNLOAD_JAY = "error.download.jar";


    @Autowired
    private DevopsHostMapper devopsHostMapper;

    private SshUtil() {
    }

    /**
     * 连接主机进行测试, 如果成功返回true
     *
     * @param hostIp   主机ip
     * @param sshPort  ssh端口
     * @param authType {@link CdHostAccountType}
     * @param username 用户名
     * @param password 密码或者秘钥
     * @return true
     */
    public boolean sshConnect(String hostIp, Integer sshPort, String authType, String username, String password) {
        SSHClient ssh = new SSHClient();
        ssh.setConnectTimeout(DEFAULT_TIMEOUT_MILLISECONDS);
        ssh.setTimeout(DEFAULT_TIMEOUT_MILLISECONDS);
        Session session = null;
        try {
            addAuth(ssh, hostIp, sshPort, authType, username, password);

            session = ssh.startSession();
            Session.Command cmd = session.exec("echo Hello World!!!");
            LOGGER.info(IOUtils.readFully(cmd.getInputStream()).toString());
            cmd.join(5, TimeUnit.SECONDS);
            LOGGER.info("\n** exit status: " + cmd.getExitStatus());
            if (cmd.getExitStatus() != 0) {
                throw new CommonException("error.test.connection");
            }
        } catch (Exception ex) {
            LOGGER.warn("Failed to connect to host by ssh, the host is {}, port is {}, username: {}", hostIp, sshPort, username);
            LOGGER.warn("The ex is ", ex);
            return false;
        } finally {
            closeSsh(ssh, session);
        }
        return true;
    }

    public void sshConnect(HostConnectionVO hostConnectionVO, SSHClient ssh) throws IOException {
        //ssh.loadKnownHosts();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        //根据主机来源获取主机连接信息
        if (HostSourceEnum.EXISTHOST.getValue().equalsIgnoreCase(hostConnectionVO.getHostSource())) {
            DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostConnectionVO.getHostId());
            dtoToHostConnVo(hostConnectionVO, devopsHostDTO);
        }

        ssh.connect(hostConnectionVO.getHostIp(), TypeUtil.objToInteger(hostConnectionVO.getHostPort()));
        if (hostConnectionVO.getAccountType().equals(CdHostAccountType.ACCOUNTPASSWORD.value())) {
            ssh.authPassword(hostConnectionVO.getUsername(), hostConnectionVO.getPassword());
        } else {
            String str = Base64Util.getBase64DecodedString(hostConnectionVO.getAccountKey());
            KeyProvider keyProvider = ssh.loadKeys(str, null, null);
            ssh.authPublickey(hostConnectionVO.getUsername(), keyProvider);
        }
    }

    public void sshStopJar(SSHClient ssh, String jarName, StringBuilder log) throws IOException {
        StringBuilder stopJar = new StringBuilder();
        stopJar.append(String.format("ps aux|grep %s | grep -v grep |awk '{print  $2}' |xargs kill -9 ", jarName));
        stopJar.append(System.lineSeparator());
        stopJar.append(String.format("cd /temp-jar && rm -f %s", jarName));
        stopJar.append(" && cd /temp-log &&");
        stopJar.append(String.format("rm -f %s", jarName.replace(".jar", ".log")));
        LOGGER.info(stopJar.toString());
        Session session = null;
        try {
            session = ssh.startSession();
            final Session.Command cmd = session.exec(stopJar.toString());
            cmd.join(WAIT_SECONDS, TimeUnit.SECONDS);
            String logInfo = IOUtils.readFully(cmd.getInputStream()).toString();
            String errorInfo = IOUtils.readFully(cmd.getErrorStream()).toString();
            log.append(logInfo);
            log.append(errorInfo);
        } finally {
            assert session != null;
            session.close();
        }
    }

    public void sshExec(SSHClient ssh, C7nNexusDeployDTO c7nNexusDeployDTO, String value, String workingPath, StringBuilder log) throws IOException {
        StringBuilder cmdStr = new StringBuilder();
        if (StringUtils.isEmpty(workingPath)) {
            cmdStr.append("mkdir -p /temp/jar && ");
            cmdStr.append("mkdir -p /temp/log && ");
        } else {
            workingPath = workingPath.endsWith("/") ? workingPath.substring(0, workingPath.length() - 1) : workingPath;
            cmdStr.append(String.format("mkdir -p %s/jar && ", workingPath));
            cmdStr.append(String.format("mkdir -p %s/log && ", workingPath));
        }

        Session session = null;
        try {
            session = ssh.startSession();
            // 2.2
            String curlExec = String.format("curl -o %s -u %s:%s %s ",
                    "/temp-jar/" + c7nNexusDeployDTO.getJarName(),
                    c7nNexusDeployDTO.getPullUserId(),
                    c7nNexusDeployDTO.getPullUserPassword(),
                    c7nNexusDeployDTO.getDownloadUrl());
            cmdStr.append(curlExec).append(" && ");

            // 2.3
            String[] strings = value.split("\n");
            String values = "";
            for (String s : strings) {
                if (s.length() > 0 && !s.contains("#") && s.contains("java")) {
                    values = s;
                }
            }
            if (StringUtils.isEmpty(values) || !checkInstruction("jar", values)) {
                throw new CommonException("error.instruction");
            }

            String logName = c7nNexusDeployDTO.getJarName().replace(".jar", ".log");
            String javaJarExec = String.format("nohup %s > /temp-log/%s 2>&1 &", values.replace("${jar}", "/temp-jar/" + c7nNexusDeployDTO.getJarName()), logName);

            cmdStr.append(javaJarExec);
            cmdStr.append(System.lineSeparator());
            LOGGER.info(cmdStr.toString());

            final Session.Command cmd = session.exec(cmdStr.toString());
            cmd.join(WAIT_SECONDS, TimeUnit.SECONDS);
            String loggerInfo = IOUtils.readFully(cmd.getInputStream()).toString();
            String loggerError = IOUtils.readFully(cmd.getErrorStream()).toString();
            log.append(System.lineSeparator());
            log.append(loggerInfo);
            log.append(loggerError);
            if (loggerError.contains("Unauthorized") || loggerInfo.contains("Unauthorized") || cmd.getExitStatus() != 0) {
                throw new CommonException(ERROR_DOWNLOAD_JAY);
            }
            LOGGER.info(loggerInfo);
            LOGGER.info(loggerError);
        } finally {
            assert session != null;
            session.close();
        }

    }

    public void dockerLogin(SSHClient ssh, C7nImageDeployDTO imageTagVo, StringBuilder log) throws IOException {
        Session session = null;
        try {
            session = ssh.startSession();
            String loginExec = String.format("docker login -u '%s' -p %s %s", imageTagVo.getPullAccount(), imageTagVo.getPullPassword(), imageTagVo.getHarborUrl());
            LOGGER.info(loginExec);
            Session.Command cmd = session.exec(loginExec);

            String loggerInfo = IOUtils.readFully(cmd.getInputStream()).toString();
            String loggerError = IOUtils.readFully(cmd.getErrorStream()).toString();
            cmd.join(WAIT_SECONDS, TimeUnit.SECONDS);
            log.append(loggerInfo);
            log.append(loggerError);
            LOGGER.info(loggerInfo);
            LOGGER.info(loggerError);
            LOGGER.info("docker login status:{}", cmd.getExitStatus());

            if (cmd.getExitStatus() != 0) {
                throw new CommonException(ERROR_DOCKER_LOGIN);
            }

        } finally {
            assert session != null;
            session.close();
        }
    }

    public void dockerPull(SSHClient ssh, C7nImageDeployDTO imageTagVo, StringBuilder log) throws IOException {
        Session session = null;
        try {
            session = ssh.startSession();
            LOGGER.info(imageTagVo.getPullCmd());
            Session.Command cmd = session.exec(imageTagVo.getPullCmd());
            String loggerInfo = IOUtils.readFully(cmd.getInputStream()).toString();
            String loggerError = IOUtils.readFully(cmd.getErrorStream()).toString();
            execPullImage(cmd);
            log.append(System.lineSeparator());
            log.append(loggerInfo);
            log.append(loggerError);
            LOGGER.info(loggerInfo);
            LOGGER.info(loggerError);
            LOGGER.info("docker pull status:{}", cmd.getExitStatus());
            if (cmd.getExitStatus() != 0) {
                throw new CommonException(ERROR_DOCKER_PULL);
            }
        } finally {
            assert session != null;
            session.close();
        }
    }

    public void dockerStop(SSHClient ssh, String containerName, StringBuilder log) throws IOException {
        Session session = null;
        try {
            session = ssh.startSession();

            // 判断镜像是否存在 存在删除 部署
            StringBuilder dockerRunExec = new StringBuilder();
            dockerRunExec.append("docker stop ").append(containerName).append(" && ");
            dockerRunExec.append("docker rm ").append(containerName);
            LOGGER.info(dockerRunExec.toString());
            Session.Command cmd = session.exec(dockerRunExec.toString());
            cmd.join(WAIT_SECONDS, TimeUnit.SECONDS);
            String loggerInfo = IOUtils.readFully(cmd.getInputStream()).toString();
            String loggerError = IOUtils.readFully(cmd.getErrorStream()).toString();
            log.append(System.lineSeparator());
            log.append(loggerInfo);
            log.append(loggerError);
            LOGGER.info(loggerInfo);
            LOGGER.info(loggerError);
            LOGGER.info("docker stop status:{}", cmd.getExitStatus());
        } finally {
            assert session != null;
            session.close();
        }

    }

    public ExecResultInfoVO execCommand(SSHClient sshClient, String command) throws IOException {
        ExecResultInfoVO execResultInfoVO = new ExecResultInfoVO();
        try (Session session = sshClient.startSession()) {
            Session.Command cmd = session.exec(command);
            execResultInfoVO.setCommand(command);
            execResultInfoVO.setStdErr(IOUtils.readFully(cmd.getErrorStream()).toString());
            execResultInfoVO.setStdOut(IOUtils.readFully(cmd.getInputStream()).toString());
            execResultInfoVO.setExitCode(cmd.getExitStatus());
            return execResultInfoVO;
        }
    }

    public void dockerRun(SSHClient ssh, String value, String containerName, C7nImageDeployDTO c7nImageDeployDTO, StringBuilder log) throws IOException {
        Session session = null;
        try {
            session = ssh.startSession();
            String[] strings = value.split("\n");
            String values = "";
            for (String s : strings) {
                if (s.length() > 0 && !s.contains("#") && s.contains("docker")) {
                    values = s;
                }
            }
            LOGGER.info("docker run values is {}", values);
            if (StringUtils.isEmpty(values) || !checkInstruction("image", values)) {
                throw new CommonException("error.instruction");
            }

            // 判断镜像是否存在 存在删除 部署
            StringBuilder dockerRunExec = new StringBuilder();
            dockerRunExec.append(values.replace("${containerName}", containerName).replace("${imageName}", c7nImageDeployDTO.getPullCmd().replace("docker pull", "")));
            LOGGER.info(dockerRunExec.toString());
            Session.Command cmd = session.exec(dockerRunExec.toString());
            String loggerInfo = IOUtils.readFully(cmd.getInputStream()).toString();
            String loggerError = IOUtils.readFully(cmd.getErrorStream()).toString();
            LOGGER.info(loggerInfo);
            LOGGER.info(loggerError);
            cmd.join(WAIT_SECONDS, TimeUnit.SECONDS);
            log.append(System.lineSeparator());
            log.append(loggerInfo);
            log.append(loggerError);
            LOGGER.info("docker run status:{}", cmd.getExitStatus());
            if (cmd.getExitStatus() != 0) {
                throw new CommonException(ERROR_DOCKER_RUN);
            }
        } finally {
            assert session != null;
            session.close();
        }

    }

    private Boolean checkInstruction(String type, String instruction) {
        if (type.equals("jar")) {
            return instruction.contains("${jar}");
        } else {
            return instruction.contains("${containerName}") && instruction.contains("${imageName}") && instruction.contains(" -d ");
        }
    }

    private static void addAuth(SSHClient ssh, String hostIp, Integer sshPort, String authType, String username, String password) throws IOException {
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(hostIp, sshPort);
        if (CdHostAccountType.ACCOUNTPASSWORD.value().equals(authType)) {
            ssh.authPassword(username, password);
        } else {
            KeyProvider keyProvider = ssh.loadKeys(password, null, null);
            ssh.authPublickey(username, keyProvider);
        }
    }

    public void uploadFile(SSHClient ssh, String file, String targetFile) {
        try {
            ssh.newSCPFileTransfer().upload(new FileSystemFile(file), targetFile);
        } catch (IOException e) {
            throw new CommonException(String.format("failed to upload file %s to host(%s),target path is %s,error is:%s", file, ssh.getRemoteHostname(), targetFile, e.getMessage()));
        }
    }

    public void closeSsh(SSHClient ssh, Session session) {
        try {
            if (session != null) {
                session.close();
            }
            ssh.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sshDisconnect(SSHClient ssh) {
        try {
            ssh.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dtoToHostConnVo(HostConnectionVO hostConnectionVO, DevopsHostDTO devopsHostDTO) {
        if (devopsHostDTO != null) {
            hostConnectionVO.setHostIp(devopsHostDTO.getHostIp());
            hostConnectionVO.setHostPort(String.valueOf(devopsHostDTO.getSshPort()));
            hostConnectionVO.setAccountType(devopsHostDTO.getAuthType());
            hostConnectionVO.setUsername(devopsHostDTO.getUsername());
            hostConnectionVO.setPassword(devopsHostDTO.getPassword());
            hostConnectionVO.setAccountKey(devopsHostDTO.getPassword());
        }
    }

    /**
     * 解决pull 镜像时间较长
     * 等3分钟
     */
    private void execPullImage(Session.Command cmd) {
        for (int i = 0; i < 30; i++) {
            if (cmd.getExitStatus() == null) {
                LOGGER.info("Pulling the image!!!");
                try {
                    cmd.join(WAIT_SECONDS, TimeUnit.SECONDS);
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
    }
}
