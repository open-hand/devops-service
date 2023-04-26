package io.choerodon.devops.api.validator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

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
        if (!ObjectUtils.isEmpty(devopsServiceReqVO.getExternalIp())) {
            if (devopsServiceReqVO.getExternalIp().startsWith(",")) {
                devopsServiceReqVO.setExternalIp(devopsServiceReqVO.getExternalIp().substring(1));
            }
            String[] ips = devopsServiceReqVO.getExternalIp().split(",");
            Arrays.asList(ips).forEach(ip -> {
                if (!ObjectUtils.isEmpty(ip) && !Pattern.matches(IP_PATTERN, ip)) {
                    throw new CommonException("devops.externalIp.notMatch");
                }
            });
        }
        checkIPAndPortUnique(devopsServiceReqVO, targetServiceId);
    }

    public static void checkName(String name) {
        if (!Pattern.matches(NAME_PATTERN, name)) {
            throw new CommonException("devops.network.name.notMatch");
        }
    }

    private static void checkPorts(PortMapVO port) {
        if (!checkPort(port.getPort())) {
            throw new CommonException("devops.port.illegal");
        }
        if (!checkPort(Integer.valueOf(port.getTargetPort()))) {
            throw new CommonException("devops.targetPort.illegal");
        }
        if (port.getNodePort() != null && !checkPort(port.getNodePort())) {
            throw new CommonException("devops.nodePort.illegal");
        }

    }

    private static Boolean checkPort(Integer port) {
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
            case NODE_PORT:
                // 同一环境下，如果nodePort不为空，那么必须唯一
                List<DevopsServiceDTO> serviceOfNodePort = serviceGroupByType.get(NODE_PORT);
                if (!CollectionUtils.isEmpty(serviceOfNodePort)) {
                    serviceOfNodePort.forEach(s -> {
                        List<PortMapVO> portMapVOList = JsonHelper.unmarshalByJackson(s.getPorts(), new TypeReference<List<PortMapVO>>() {
                        });
                        portMapVOList.forEach(portMapVO -> {
                            Integer nodePort = portMapVO.getNodePort();
                            if (nodePort != null) {
                                devopsServiceReqVO.getPorts().forEach(p -> {
                                    if (Objects.equals(nodePort, p.getNodePort())) {
                                        throw new CommonException("devops.same.nodePort.exist");
                                    }
                                });
                            }
                        });
                    });
                }
                break;
            default:
                List<DevopsServiceDTO> serviceOfClusterIp = serviceGroupByType.get(CLUSTER_IP);
                if (!CollectionUtils.isEmpty(serviceOfClusterIp)) {
                    serviceOfClusterIp.forEach(s -> {
                        List<PortMapVO> portMapVOList = JsonHelper.unmarshalByJackson(s.getPorts(), new TypeReference<List<PortMapVO>>() {
                        });
                        portMapVOList.forEach(portMapVO -> {
                            Integer port = portMapVO.getPort();
                            String externalIp = s.getExternalIp();
                            devopsServiceReqVO.getPorts().forEach(p -> {
                                if (!ObjectUtils.isEmpty(devopsServiceReqVO.getExternalIp())) {
                                    if (Objects.equals(port, p.getPort()) && Objects.equals(externalIp, devopsServiceReqVO.getExternalIp())) {
                                        throw new CommonException("devops.same.externalIp.port.exist");
                                    }
                                }
                            });
                        });
                    });
                }
        }

    }
}
