package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.DevopsEnvMessageDTO;
import io.choerodon.devops.app.service.ApplicationService;
import io.choerodon.devops.app.service.DevopsEnvApplicationService;
import io.choerodon.devops.infra.dto.DevopsEnvApplicationDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvApplicationMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ContainerPort;
import io.kubernetes.client.models.V1beta2Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
@Service
public class DevopsEnvApplicationServiceImpl implements DevopsEnvApplicationService {

    private JSON json = new JSON();

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private DevopsEnvApplicationMapper devopsEnvApplicationMapper;

    @Override
    public List<DevopsEnvApplicationVO> batchCreate(DevopsEnvApplicationCreationVO devopsEnvApplicationCreationVO) {
        return Stream.of(devopsEnvApplicationCreationVO.getAppIds())
                .map(appId -> new DevopsEnvApplicationDTO(devopsEnvApplicationCreationVO.getEnvId(), appId))
                .peek(e -> devopsEnvApplicationMapper.insertIgnore(e))
                .map(e -> ConvertUtils.convertObject(e, DevopsEnvApplicationVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationRepVO> listAppByEnvId(Long envId) {
        List<Long> appIds = baseListAppByEnvId(envId);
        List<ApplicationRepVO> applicationRepVOS = new ArrayList<>();
        appIds.forEach(v ->
                applicationRepVOS.add(ConvertUtils.convertObject(applicationService.baseQuery(v), ApplicationRepVO.class))
        );
        return applicationRepVOS;
    }

    @Override
    public List<DevopsEnvLabelVO> listLabelByAppAndEnvId(Long envId, Long appId) {
        List<DevopsEnvMessageDTO> devopsEnvMessageVOS = baseListResourceByEnvAndApp(envId, appId);
        List<DevopsEnvLabelVO> devopsEnvLabelVOS = new ArrayList<>();
        devopsEnvMessageVOS.forEach(devopsEnvMessageVO -> {
            DevopsEnvLabelVO devopsEnvLabelVO = new DevopsEnvLabelVO();
            devopsEnvLabelVO.setResourceName(devopsEnvMessageVO.getResourceName());
            V1beta2Deployment v1beta2Deployment = json.deserialize(
                    devopsEnvMessageVO.getDetail(), V1beta2Deployment.class);
            devopsEnvLabelVO.setLabels(v1beta2Deployment.getSpec().getSelector().getMatchLabels());
            devopsEnvLabelVOS.add(devopsEnvLabelVO);
        });
        return devopsEnvLabelVOS;
    }

    @Override
    public List<DevopsEnvPortVO> listPortByAppAndEnvId(Long envId, Long appId) {
        List<DevopsEnvMessageDTO> devopsEnvMessageVOS = baseListResourceByEnvAndApp(envId, appId);
        List<DevopsEnvPortVO> devopsEnvPortVOS = new ArrayList<>();
        devopsEnvMessageVOS.forEach(devopsEnvMessageVO -> {
            V1beta2Deployment v1beta2Deployment = json.deserialize(
                    devopsEnvMessageVO.getDetail(), V1beta2Deployment.class);
            List<V1Container> containers = v1beta2Deployment.getSpec().getTemplate().getSpec().getContainers();
            for (V1Container container : containers) {
                List<V1ContainerPort> ports = container.getPorts();

                Optional.ofNullable(ports).ifPresent(portList -> {
                    for (V1ContainerPort port : portList) {
                        DevopsEnvPortVO devopsEnvPortVO = new DevopsEnvPortVO();
                        devopsEnvPortVO.setResourceName(devopsEnvMessageVO.getResourceName());
                        devopsEnvPortVO.setPortName(port.getName());
                        devopsEnvPortVO.setPortValue(port.getContainerPort());
                        devopsEnvPortVOS.add(devopsEnvPortVO);
                    }
                });
            }
        });
        return devopsEnvPortVOS;
    }

    @Override
    public DevopsEnvApplicationDTO baseCreate(DevopsEnvApplicationDTO devopsEnvApplicationDTO) {
        if (devopsEnvApplicationMapper.insert(devopsEnvApplicationDTO) != 1) {
            throw new CommonException("error.insert.env.app");
        }
        return devopsEnvApplicationDTO;
    }

    @Override
    public List<Long> baseListAppByEnvId(Long envId) {
        return devopsEnvApplicationMapper.queryAppByEnvId(envId);
    }

    @Override
    public List<DevopsEnvMessageDTO> baseListResourceByEnvAndApp(Long envId, Long appId) {
        return devopsEnvApplicationMapper.listResourceByEnvAndApp(envId, appId);
    }
}
