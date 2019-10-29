package io.choerodon.devops.app.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import io.choerodon.devops.api.vo.PrometheusVo;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsPrometheusMapper;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;

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

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;

    @Autowired
    private UserAttrService userAttrService;

    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;

    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    private static final String TYPE_PROMETHEUS = "prometheus";

    @Override
    public PrometheusVo deploy(Long clusterId, PrometheusVo prometheusVo) {

        DevopsPrometheusDTO devopsPrometheusDTO = prometheusVoToDto(prometheusVo);
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceService.queryByClusterIdAndType(clusterId, "prometheus");
        if (devopsClusterResourceDTO.getSystemEnvId() != null) {
            AppServiceInstanceDTO releaseForPrometheus = componentReleaseService.createReleaseForPrometheus(devopsPrometheusDTO);
            if (!ObjectUtils.isEmpty(releaseForPrometheus)) {
                devopsPrometheusMapper.insertSelective(devopsPrometheusDTO);
                prometheusVo.setId(devopsPrometheusDTO.getId());

                devopsClusterResourceDTO.setClusterId(clusterId);
                devopsClusterResourceDTO.setConfigId(prometheusVo.getId());
                devopsClusterResourceDTO.setInstanceId(releaseForPrometheus.getId());
                devopsClusterResourceDTO.setName(prometheusVo.getClusterName());
                devopsClusterResourceDTO.setCode(clusterId.toString());
                devopsClusterResourceDTO.setType(TYPE_PROMETHEUS);
                devopsClusterResourceService.baseCreateOrUpdate(devopsClusterResourceDTO);
            }

        }

        return prometheusVo;
    }

    @Override
    public String queryDeployStatus(Long clusterId, Long prometheusId) {
        String status = null;
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceService.queryByClusterIdAndConfigId(clusterId, prometheusId);
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(devopsClusterResourceDTO.getId());
        status = appServiceInstanceDTO.getStatus();
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
        if (devopsEnvCommandDTO.getSha() == null) {

            //todo
        } else {

        }

        return status;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long prometheusId, Long clusterId) {
        DevopsPrometheusDTO devopsPrometheusDTO = baseQuery(prometheusId);
        if (devopsPrometheusDTO == null) {
            return;
        }
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceService.queryByClusterIdAndConfigId(clusterId, prometheusId);
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(devopsClusterResourceDTO.getId());

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        // 校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
        devopsEnvCommandDTO.setCommandType(CommandType.DELETE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);


        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getEnvIdRsa());


    }

    @Override
    public DevopsPrometheusDTO baseQuery(Long id) {
        return devopsPrometheusMapper.selectByPrimaryKey(id);
    }


    private DevopsPrometheusDTO prometheusVoToDto(PrometheusVo prometheusVo) {
        DevopsPrometheusDTO devopsPrometheusDTO = new DevopsPrometheusDTO();
        BeanUtils.copyProperties(prometheusVo, devopsPrometheusDTO);
        return devopsPrometheusDTO;
    }
}
