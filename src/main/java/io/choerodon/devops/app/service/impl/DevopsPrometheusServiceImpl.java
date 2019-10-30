package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import io.choerodon.devops.api.vo.PrometheusVo;
import io.choerodon.devops.api.vo.kubernetes.C7nHelmRelease;
import io.choerodon.devops.api.vo.kubernetes.Metadata;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsPrometheusMapper;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;

import static org.eclipse.jgit.lib.Constants.MASTER;

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

    @Autowired
    private DevopsClusterService devopsClusterService;
    private static final String TYPE_PROMETHEUS = "prometheus";
    private static final String PROMETHEUS_PREFIX = "prometheus-";

    @Override
    public PrometheusVo deploy(Long clusterId, PrometheusVo prometheusVo) {

        DevopsPrometheusDTO devopsPrometheusDTO = prometheusVoToDto(prometheusVo);
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);

        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceService.queryByClusterIdAndType(clusterId, "prometheus");
        if (devopsClusterResourceDTO.getSystemEnvId() != null) {
            AppServiceInstanceDTO releaseForPrometheus = componentReleaseService.createReleaseForPrometheus(devopsPrometheusDTO);
            if (!ObjectUtils.isEmpty(releaseForPrometheus)) {
                devopsPrometheusMapper.insertSelective(devopsPrometheusDTO);
                prometheusVo.setPrometheusId(devopsPrometheusDTO.getId());

                devopsClusterResourceDTO.setClusterId(clusterId);
                devopsClusterResourceDTO.setConfigId(prometheusVo.getPrometheusId());
                devopsClusterResourceDTO.setInstanceId(releaseForPrometheus.getId());
                devopsClusterResourceDTO.setName(devopsClusterDTO.getName());
                devopsClusterResourceDTO.setCode(devopsClusterDTO.getCode());
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

        if (appServiceInstanceDTO.getStatus().equals("running")){


            return status = "running";
        }else {
        if(ObjectUtils.isEmpty( devopsEnvCommandDTO.getSha())){
        }
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

        // 查询改对象所在文件中是否含有其它对象
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(), prometheusId, "");

        if (devopsEnvFileResourceDTO == null) {
            baseDelete(prometheusId, clusterId);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    "promtheus-" + devopsClusterResourceDTO.getCode() + ".yaml")) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        "promtheus-" + devopsClusterResourceDTO.getCode() + ".yaml",
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
            return;
        } else {
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                baseDelete(prometheusId, clusterId);
                devopsEnvFileResourceService.baseDeleteById(devopsEnvFileResourceDTO.getId());
                return;
            }
        }
        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        List<DevopsEnvFileResourceDTO> devopsEnvFileResourceES = devopsEnvFileResourceService
                .baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), devopsEnvFileResourceDTO.getFilePath());

        if (devopsEnvFileResourceES.size() == 1) {
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        devopsEnvFileResourceDTO.getFilePath(),
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
        } else {
            //todo
            ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
            Metadata metadata = new Metadata();
            metadata.setName(appServiceInstanceDTO.getCode());
            c7nHelmRelease.setMetadata(metadata);
            resourceConvertToYamlHandler.setType(c7nHelmRelease);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    PROMETHEUS_PREFIX + appServiceInstanceDTO.getCode(),
                    projectId,
                    "delete",
                    userAttrDTO.getGitlabUserId(),
                    appServiceInstanceDTO.getId(), TYPE_PROMETHEUS, null, false, devopsEnvironmentDTO.getId(), path);
        }

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

    private void baseDelete(Long prometheusId, Long clusterId) {
        DevopsPrometheusDTO devopsPrometheusDTO = new DevopsPrometheusDTO();
        devopsPrometheusDTO.setId(prometheusId);
        devopsPrometheusMapper.delete(devopsPrometheusDTO);

        devopsClusterResourceService.delete(clusterId, prometheusId);
    }
}
