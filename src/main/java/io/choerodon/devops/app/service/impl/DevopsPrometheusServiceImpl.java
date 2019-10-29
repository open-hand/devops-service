package io.choerodon.devops.app.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import io.choerodon.devops.api.vo.PrometheusVo;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsClusterResourceDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsPrometheusDTO;
import io.choerodon.devops.infra.mapper.DevopsPrometheusMapper;

/**
 * @author: 25499
 * @date: 2019/10/28 15:53
 * @description:
 */
@Service
public class DevopsPrometheusServiceImpl implements DevopsPrometheusService {
    @Autowired
    private DevopsPrometheusMapper devopsPrometheusMapper;

    @Autowired
    private DevopsClusterResourceService devopsClusterResourceService;

    @Autowired
    private ComponentReleaseService componentReleaseService;

    @Autowired
    private AppServiceInstanceService appServiceInstanceService;

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;

    private static final String TYPE_PROMETHEUS = "prometheus";

    @Override
    public PrometheusVo deploy(Long clusterId, PrometheusVo prometheusVo) {
        Map<String, String> map = new HashMap<>();
        map.put("adminPassword", prometheusVo.getAdminPassword());
        map.put("clusterName", prometheusVo.getClusterName());
        map.put("pvName", prometheusVo.getPvName());
        map.put("grafanaDomain", prometheusVo.getGrafanaDomain());

        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceService.queryByClusterIdAndType(clusterId, "prometheus");
        if (devopsClusterResourceDTO.getSystemEnvId() != null) {
            AppServiceInstanceDTO releaseForPrometheus = componentReleaseService.createReleaseForPrometheus(map);

            if (!ObjectUtils.isEmpty(releaseForPrometheus)) {
                DevopsPrometheusDTO devopsPrometheusDTO = prometheusVoToDto(prometheusVo);
                devopsPrometheusMapper.insertSelective(devopsPrometheusDTO);
                prometheusVo.setId(devopsPrometheusDTO.getId());

                devopsClusterResourceDTO.setClusterId(clusterId);
                devopsClusterResourceDTO.setConfigId(prometheusVo.getId());
                devopsClusterResourceDTO.setInstanceId(releaseForPrometheus.getId());
                devopsClusterResourceDTO.setName("默认prometheus");
                devopsClusterResourceDTO.setCode("默认prometheus");
                devopsClusterResourceDTO.setType(TYPE_PROMETHEUS);
                devopsClusterResourceService.baseCreateOrUpdate(devopsClusterResourceDTO);
            }

        }

        return prometheusVo;
    }

    @Override
    public String queryDeployStatus(Long clusterId, Long prometheusId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceService.queryByClusterIdAndConfigId(clusterId, prometheusId);
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(devopsClusterResourceDTO.getId());
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());
        if (devopsEnvironmentDTO.getDevopsSyncCommit().equals(devopsEnvironmentDTO.getSagaSyncCommit())) {

            //todo
        }

        return appServiceInstanceDTO.getStatus();
    }

    private DevopsPrometheusDTO prometheusVoToDto(PrometheusVo prometheusVo) {
        DevopsPrometheusDTO devopsPrometheusDTO = new DevopsPrometheusDTO();
        BeanUtils.copyProperties(prometheusVo, devopsPrometheusDTO);
        return devopsPrometheusDTO;
    }
}
