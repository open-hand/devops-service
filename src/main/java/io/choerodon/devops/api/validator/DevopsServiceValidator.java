package io.choerodon.devops.api.validator;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsServiceReqVO;
import io.choerodon.devops.infra.dto.PortMapVO;

/**
 * Created by Zenger on 2018/4/26.
 */
public class DevopsServiceValidator {

    private static final String NUM_0_255 = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";

    // ip
    private static final String IP_PATTERN =
            String.format("(%s\\.%s\\.%s\\.%s)", NUM_0_255, NUM_0_255, NUM_0_255, NUM_0_255);
    // ip,ip,ip ...
    private static final String EXTERNAL_IP_PATTERN = String.format("(%s,)*%s", IP_PATTERN, IP_PATTERN);

    //service name
    private static final String NAME_PATTERN = "[a-z]([-a-z0-9]*[a-z0-9])?";

    private DevopsServiceValidator() {
    }

    /**
     * 参数校验
     */
    public static void checkService(DevopsServiceReqVO devopsServiceReqVO) {
        devopsServiceReqVO.getPorts().forEach(DevopsServiceValidator::checkPorts);
        checkName(devopsServiceReqVO.getName());
        if (!StringUtils.isEmpty(devopsServiceReqVO.getExternalIp())
                && !Pattern.matches(EXTERNAL_IP_PATTERN, devopsServiceReqVO.getExternalIp())) {
            throw new CommonException("error.externalIp.notMatch");

        }
    }

    public static void checkName(String name) {
        if (!Pattern.matches(NAME_PATTERN, name)) {
            throw new CommonException("error.network.name.notMatch");
        }
    }

    private static void checkPorts(PortMapVO port) {
        if (!checkPort(port.getPort())) {
            throw new CommonException("error.port.illegal");
        }
        if (!checkPort(Long.valueOf(port.getTargetPort()))) {
            throw new CommonException("error.targetPort.illegal");
        }
        if (port.getNodePort() != null && !checkPort(port.getNodePort())) {
            throw new CommonException("error.nodePort.illegal");
        }

    }

    private static Boolean checkPort(Long port) {
        return port >= 0 && port <= 65535;
    }
}
