package io.choerodon.devops.infra.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.enums.CdHostAccountType;

/**
 * @author zmf
 * @since 2020/9/15
 */
public class SshUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshUtil.class);

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
    public static boolean sshConnect(String hostIp, Integer sshPort, String authType, String username, String password) {
        SSHClient ssh = new SSHClient();
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

    private static void addAuth(SSHClient ssh, String hostIp, Integer sshPort, String authType, String username, String password) throws IOException {
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(hostIp, sshPort);
        if (CdHostAccountType.PASSWORD.value().equals(authType)) {
            ssh.authPassword(username, password);
        } else {
            KeyProvider keyProvider = ssh.loadKeys(password, null, null);
            ssh.authPublickey(username, keyProvider);
        }
    }

    private static void closeSsh(SSHClient ssh, Session session) {
        try {
            if (session != null) {
                session.close();
            }
            ssh.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
