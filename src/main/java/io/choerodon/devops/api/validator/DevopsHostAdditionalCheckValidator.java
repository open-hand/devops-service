package io.choerodon.devops.api.validator;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
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
    private static final Pattern JMETER_PATH_PATTERN = Pattern.compile("^(/[\\w\\W]+)+$");

    @Lazy
    @Autowired
    private DevopsHostService devopsHostService;

    public void validNameProjectUnique(Long projectId, String name) {
        CommonExAssertUtil.assertTrue(devopsHostService.isNameUnique(projectId, name), "error.host.name.not.unique");
    }

    public void validIpAndSshPortProjectUnique(Long projectId, String ip, Integer sshPort) {
        CommonExAssertUtil.assertTrue(devopsHostService.isSshIpPortUnique(projectId, ip, sshPort), "error.host.ip.ssh.port.not.unique");
    }

    public void validUsernamePasswordMatch(String username, String password) {
        CommonExAssertUtil.assertTrue(StringUtils.isNotEmpty(username) && StringUtils.isEmpty(password), "error.host.password.empty");
    }
}
