package io.choerodon.devops.api.validator;

import org.springframework.stereotype.Component;

/**
 * @author zmf
 * @since 2020/9/15
 */
@Component
public class DevopsHostAdditionalCheckValidator {
    public void validNameProjectUnique(Long projectId, String name) {
        // TODO
    }

    public void validIpAndSshPortProjectUnique(Long projectId, String ip, Integer sshPort) {
        // TODO
    }

    public void validIpAndJmeterPortProjectUnique(Long projectId, String ip, Integer jmeterPort) {
        // TODO
    }

    public void validJmeterPort(Integer jmeterPort) {
        // TODO
    }
}
