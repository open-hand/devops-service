package io.choerodon.devops.api.validator;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.choerodon.devops.api.vo.DevopsHostCreateRequestVO;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.infra.constant.GitOpsConstants;
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
        CommonExAssertUtil.assertTrue(devopsHostService.isNameUnique(projectId, name), "devops.host.name.not.unique");
    }

    public void validIpAndSshPortProjectUnique(Long projectId, String ip, Integer sshPort) {
        CommonExAssertUtil.assertTrue(devopsHostService.isSshIpPortUnique(projectId, ip, sshPort), "devops.host.ip.ssh.port.not.unique");
    }

    public void validUsernamePasswordMatch(String username, String password) {
        CommonExAssertUtil.assertTrue(!(StringUtils.isNotEmpty(username) && StringUtils.isEmpty(password)), "devops.host.password.empty");
    }

    public boolean validIpAndSshPortComplete(DevopsHostCreateRequestVO devopsHostCreateRequestVO) {
        boolean ipEmptyFlag = StringUtils.isNotEmpty(devopsHostCreateRequestVO.getHostIp());
        boolean portEmptyFlag = devopsHostCreateRequestVO.getSshPort() != null;
        CommonExAssertUtil.assertTrue((ipEmptyFlag && portEmptyFlag) || (!ipEmptyFlag && !portEmptyFlag), "devops.host.ip.or.port.empty");
        return ipEmptyFlag;
    }

    public void validHostInformationMatch(DevopsHostCreateRequestVO devopsHostCreateRequestVO) {
        CommonExAssertUtil.assertTrue(Pattern.compile(GitOpsConstants.IP_PATTERN).matcher(devopsHostCreateRequestVO.getHostIp()).matches(), "devops.host.ip.invalid");
        CommonExAssertUtil.assertTrue(devopsHostCreateRequestVO.getSshPort() <= 65535, "devops.ssh.port.invalid");
        CommonExAssertUtil.assertTrue(StringUtils.isNotEmpty(devopsHostCreateRequestVO.getUsername()), "devops.host.username.empty");
        CommonExAssertUtil.assertTrue(StringUtils.isNotEmpty(devopsHostCreateRequestVO.getPassword()), "devops.host.password.empty");
    }

    public void validHostIdAndInstanceIdMatch(Long hostId, Long instanceId) {
        CommonExAssertUtil.assertTrue(devopsHostService.hostIdInstanceIdMatch(hostId, instanceId), "devops.host.id.instance.id.not.match");
    }

    public void validHostIdAndDockerInstanceIdMatch(Long hostId, Long instanceId) {
        CommonExAssertUtil.assertTrue(devopsHostService.hostIdDockerInstanceMatch(hostId, instanceId), "devops.host.id.instance.id.not.match");
    }
}
