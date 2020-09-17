package io.choerodon.devops.api.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.infra.util.CommonExAssertUtil;

/**
 * @author zmf
 * @since 2020/9/15
 */
@Component
public class DevopsHostAdditionalCheckValidator {
    @Lazy
    @Autowired
    private DevopsHostService devopsHostService;

    public void validNameProjectUnique(Long projectId, String name) {
        CommonExAssertUtil.assertTrue(devopsHostService.isNameUnique(projectId, name), "error.host.name.not.unique");
    }

    public void validIpAndSshPortProjectUnique(Long projectId, String ip, Integer sshPort) {
        CommonExAssertUtil.assertTrue(devopsHostService.isSshIpPortUnique(projectId, ip, sshPort), "error.host.ip.ssh.port.not.unique");
    }

    public void validIpAndJmeterPortProjectUnique(Long projectId, String ip, Integer jmeterPort) {
        CommonExAssertUtil.assertTrue(devopsHostService.isIpJmeterPortUnique(projectId, ip, jmeterPort), "error.host.ip.jmeter.port.not.unique");
    }

    public void validJmeterPort(Integer jmeterPort) {
        CommonExAssertUtil.assertTrue(jmeterPort != null && jmeterPort > 0 && jmeterPort <= 65535, "error.jmeter.port.invalid");
    }
}
