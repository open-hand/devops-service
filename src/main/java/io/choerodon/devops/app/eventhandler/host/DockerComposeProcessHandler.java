package io.choerodon.devops.app.eventhandler.host;

import org.springframework.stereotype.Service;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/4/8 11:36
 */
@Service
public class DockerComposeProcessHandler implements HostMsgHandler {

    @Override
    public void handler(String hostId, Long commandId, String payload) {

    }

    @Override
    public String getType() {
        return null;
    }
}
