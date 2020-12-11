package io.choerodon.devops.api.validator;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsServiceReqVO;
import io.choerodon.devops.app.service.DevopsServiceService;
import io.choerodon.devops.infra.dto.DevopsServiceDTO;
import io.choerodon.devops.infra.dto.PortMapVO;
import io.choerodon.devops.infra.util.JsonHelper;

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

    private static final String NODE_PORT = "NodePort";

    private static final String CLUSTER_IP = "ClusterIP";

    private DevopsServiceValidator() {
    }

    /**
     * 参数校验
     */
    public static void checkService(DevopsServiceReqVO devopsServiceReqVO, Long targetServiceId) {
        devopsServiceReqVO.getPorts().forEach(DevopsServiceValidator::checkPorts);
        checkName(devopsServiceReqVO.getName());
        if (!StringUtils.isEmpty(devopsServiceReqVO.getExternalIp())
                && !Pattern.matches(EXTERNAL_IP_PATTERN, devopsServiceReqVO.getExternalIp())) {
            throw new CommonException("error.externalIp.notMatch");
        }
        checkIPAndPortUnique(devopsServiceReqVO, targetServiceId);
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

    private static void checkIPAndPortUnique(DevopsServiceReqVO devopsServiceReqVO, Long targetServiceId) {
        DevopsServiceService devopsServiceService = ApplicationContextHelper.getContext().getBean(DevopsServiceService.class);
        Map<String, List<DevopsServiceDTO>> serviceGroupByType = devopsServiceService.baseListByEnvId(devopsServiceReqVO.getEnvId())
                .stream()
                .filter(s -> !s.getId().equals(targetServiceId))
                .collect(Collectors.groupingBy(DevopsServiceDTO::getType));
        switch (devopsServiceReqVO.getType()) {
            // 同一环境下externalIp和servicePort必须唯一
            case CLUSTER_IP:
                List<DevopsServiceDTO> serviceOfClusterIp = serviceGroupByType.get(CLUSTER_IP);
                if (!CollectionUtils.isEmpty(serviceOfClusterIp)) {
                    serviceOfClusterIp.forEach(s -> {
                        List<PortMapVO> portMapVOList = JsonHelper.unmarshalByJackson(s.getPorts(), new TypeReference<List<PortMapVO>>() {
                        });
                        portMapVOList.forEach(portMapVO -> {
                            Long port = portMapVO.getPort();
                            String externalIp = s.getExternalIp();
                            devopsServiceReqVO.getPorts().forEach(p -> {
                                if (!StringUtils.isEmpty(devopsServiceReqVO.getExternalIp())) {
                                    if (ObjectUtils.equals(port, p.getPort()) && ObjectUtils.equals(externalIp, devopsServiceReqVO.getExternalIp())) {
                                        throw new CommonException("error.same.externalIp.port.exist");
                                    }
                                }
                            });
                        });
                    });
                }
                break;
            case NODE_PORT:
                // 同一环境下，如果nodePort不为空，那么必须唯一
                List<DevopsServiceDTO> serviceOfNodePort = serviceGroupByType.get(NODE_PORT);
                if (!CollectionUtils.isEmpty(serviceOfNodePort)) {
                    serviceOfNodePort.forEach(s -> {
                        List<PortMapVO> portMapVOList = JsonHelper.unmarshalByJackson(s.getPorts(), new TypeReference<List<PortMapVO>>() {
                        });
                        portMapVOList.forEach(portMapVO -> {
                            Long nodePort = portMapVO.getNodePort();
                            if (nodePort != null) {
                                devopsServiceReqVO.getPorts().forEach(p -> {
                                    if (ObjectUtils.equals(nodePort, p.getNodePort())) {
                                        throw new CommonException("error.same.nodePort.exist");
                                    }
                                });
                            }
                        });
                    });
                }
                break;
            default:
        }
    }
}
