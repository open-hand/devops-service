package io.choerodon.devops.api.validator;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsServiceReqDTO;

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

    // positive integer
    // port:targetPort
    private static final String PORT_MAP_PATTERN = "[1-9]\\d*:[1-9]\\d*";
    // port:targetPort,port:targetPort ...
    private static final String PORTS_MAP_PATTERN = String.format("(%s,)*%s", PORT_MAP_PATTERN, PORT_MAP_PATTERN);

    //service name
    private static final String NAME_PATTERN = "[a-z]([-a-z0-9]*[a-z0-9])?";

    private DevopsServiceValidator() {
    }

    /**
     * 参数校验
     */
    public static void checkService(DevopsServiceReqDTO devopsServiceReqDTO) {
        String ports = devopsServiceReqDTO.getPorts();
        if (!ports.matches(PORTS_MAP_PATTERN)) {
            throw new CommonException("error.ports.illegal");
        }
        Arrays.stream(ports.split(",")).forEach(DevopsServiceValidator::checkPorts);

        if (!Pattern.matches(NAME_PATTERN, devopsServiceReqDTO.getName())) {
            throw new CommonException("error.network.name.notMatch");
        }

        if (!StringUtils.isEmpty(devopsServiceReqDTO.getExternalIp())
                && !Pattern.matches(EXTERNAL_IP_PATTERN, devopsServiceReqDTO.getExternalIp())) {
            throw new CommonException("error.externalIp.notMatch");

        }
    }

    private static void checkPorts(String ports) {
        if (!ports.matches(PORT_MAP_PATTERN)) {
            throw new CommonException("error.portMap.illegal");
        }
        String[] portMap = ports.split(":");
        Long port = Long.parseLong(portMap[0]);
        Long targetPort = Long.parseLong(portMap[1]);
        if (!checkPort(targetPort)) {
            throw new CommonException("error.targetPort.illegal");
        }
        if (!checkPort(port)) {
            throw new CommonException("error.port.illegal");
        }

    }

    private static Boolean checkPort(Long port) {
        return port >= 1 && port <= 65535;
    }
}
