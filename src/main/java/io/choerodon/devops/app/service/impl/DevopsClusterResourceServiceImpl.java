package io.choerodon.devops.app.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ClusterConfigVO;
import io.choerodon.devops.api.vo.DevopsEnvPodVO;
import io.choerodon.devops.api.vo.PrometheusVo;
import io.choerodon.devops.api.vo.kubernetes.C7nHelmRelease;
import io.choerodon.devops.api.vo.kubernetes.Metadata;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.CertificationStatus;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsCertificationMapper;
import io.choerodon.devops.infra.mapper.DevopsClusterResourceMapper;
import io.choerodon.devops.infra.mapper.DevopsPrometheusMapper;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;

import static org.eclipse.jgit.lib.Constants.MASTER;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
@Service
public class DevopsClusterResourceServiceImpl implements DevopsClusterResourceService {
    @Autowired
    private DevopsClusterResourceMapper devopsClusterResourceMapper;
    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper;
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

    @Autowired
    private DevopsEnvPodService devopsEnvPodService;
    private static final String TYPE_PROMETHEUS = "prometheus";
    private static final String PROMETHEUS_PREFIX = "prometheus-";
    private static final String STATUS_SUCCESS = "success";
    private static final String STATUS_FAIL = "fail";

    @Override
    public void baseCreate(DevopsClusterResourceDTO devopsClusterResourceDTO) {
        if (devopsClusterResourceMapper.insertSelective(devopsClusterResourceDTO) != 1) {
            throw new CommonException("error.insert.cluster.resource");
        }
    }

    public void baseUpdate(DevopsClusterResourceDTO devopsClusterResourceDTO) {
        if (devopsClusterResourceMapper.updateByPrimaryKeySelective(devopsClusterResourceDTO) != 1) {
            throw new CommonException("error.update.cluster.resource");
        }

    }

    @Override
    public void operateCertManager(DevopsClusterResourceDTO devopsClusterResourceDTO, Long clusterId) {
        DevopsClusterResourceDTO clusterResourceDTO = devopsClusterResourceMapper.queryByOptions(devopsClusterResourceDTO);
        if (!ObjectUtils.isEmpty(clusterResourceDTO)) {
            baseUpdate(clusterResourceDTO);
        } else {
            // 新增集群的資源
            devopsClusterResourceDTO.setClusterId(clusterId);
            baseCreate(devopsClusterResourceDTO);
            // 让agent创建cert-mannager
            agentCommandService.createCertManager(clusterId);
            // 发送消息告知agent,发送创建cert-manager的信息
            // agentCommandService.getCertManagerStatus(clusterId);
        }
    }

    @Override
    public DevopsClusterResourceDTO queryCertManager(Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = new DevopsClusterResourceDTO();
        devopsClusterResourceDTO.setClusterId(clusterId);
        devopsClusterResourceDTO.setType("cert-mannager");
        DevopsClusterResourceDTO devopsClusterResourceDTO1 = devopsClusterResourceMapper.selectOne(devopsClusterResourceDTO);
        return devopsClusterResourceDTO1;
    }

    @Override
    public Boolean deleteCertManager(Long clusterId) {
        if (!checkCertManager(clusterId)) {
            return false;
        }
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, "cert-mannager");
        baseUpdate(devopsClusterResourceDTO);
        agentCommandService.unloadCertManager(clusterId);
        return true;
    }

    public Boolean checkValidity(Date date, Date validFrom, Date validUntil) {
        return validFrom != null && validUntil != null
                && date.after(validFrom) && date.before(validUntil);
    }

    /**
     * 验证cert-manager 管理的证书是否存在启用或者操作状态的
     *
     * @param clusterId
     * @return
     */
    @Override
    public Boolean checkCertManager(Long clusterId) {
        List<CertificationDTO> certificationDTOS = devopsCertificationMapper.listClusterCertification(clusterId);
        if (CollectionUtils.isEmpty(certificationDTOS)) {
            return false;
        }
        Set<Long> ids = new HashSet<>();
        certificationDTOS.forEach(dto -> {
            boolean is_faile = CertificationStatus.FAILED.getStatus().equals(dto.getStatus()) || CertificationStatus.OVERDUE.getStatus().equals(dto.getStatus());
            if (!is_faile) {
                if (CertificationStatus.ACTIVE.getStatus().equals(dto.getStatus())) {
                    if (!checkValidity(new Date(), dto.getValidFrom(), dto.getValidUntil())) {
                        dto.setStatus(CertificationStatus.OVERDUE.getStatus());
                        CertificationDTO certificationDTO = new CertificationDTO();
                        certificationDTO.setId(dto.getId());
                        certificationDTO.setStatus(CertificationStatus.OVERDUE.getStatus());
                        certificationDTO.setObjectVersionNumber(dto.getObjectVersionNumber());
                        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
                    }
                } else {
                    ids.add(dto.getId());
                }
            }
        });
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        return false;
    }

    @Override
    public DevopsClusterResourceDTO queryByClusterIdAndConfigId(Long clusterId, Long configId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndConfigId(clusterId, configId);
        return devopsClusterResourceDTO;
    }


    @Override
    public DevopsClusterResourceDTO queryByClusterIdAndType(Long clusterId, String type) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, type);
        return devopsClusterResourceDTO;
    }

    @Override
    public List<DevopsClusterResourceDTO> listClusterResource(Long clusterId) {
        return null;
    }

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
                devopsClusterResourceDTO.setObjectId(releaseForPrometheus.getId());
                devopsClusterResourceDTO.setName(devopsClusterDTO.getName());
                devopsClusterResourceDTO.setCode(devopsClusterDTO.getCode());
                devopsClusterResourceDTO.setType(TYPE_PROMETHEUS);
                devopsClusterResourceService.baseCreate(devopsClusterResourceDTO);
            }

        }

        return prometheusVo;
    }

    @Override
    public ClusterConfigVO queryDeployProess(Long projectId, Long clusterId, Long prometheusId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceService.queryByClusterIdAndConfigId(clusterId, prometheusId);
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(devopsClusterResourceDTO.getId());
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
        DevopsEnvPodVO devopsEnvPodVO = new DevopsEnvPodVO();
        devopsEnvPodVO.setClusterId(clusterId);
        devopsEnvPodVO.setInstanceCode(appServiceInstanceDTO.getCode());
        devopsEnvPodVO.setName(appServiceInstanceDTO.getCode());
        devopsEnvPodService.fillContainers(devopsEnvPodVO);

        ClusterConfigVO clusterConfigVO = new ClusterConfigVO();
        if (ObjectUtils.isEmpty(devopsEnvCommandDTO.getSha())) {
            clusterConfigVO.setStatus("");
        }
        if (appServiceInstanceDTO.getStatus().equals("running")) {
            clusterConfigVO.setStatus("running");
        }
        devopsEnvPodVO.getContainers().stream().forEach(containerVO -> {
            if (containerVO.getReady()) {
                clusterConfigVO.setStatus("ready");
            }
        });
        return clusterConfigVO;
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

    @Override
    public ClusterConfigVO queryPrometheusStatus(Long clusterId, Long prometheusId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceService.queryByClusterIdAndConfigId(clusterId, prometheusId);
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(devopsClusterResourceDTO.getId());
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
        ClusterConfigVO clusterConfigVO = new ClusterConfigVO();
        if (STATUS_SUCCESS.equals(devopsEnvCommandDTO.getStatus())) {
            clusterConfigVO.setStatus(STATUS_SUCCESS);
        } else {
            clusterConfigVO.setStatus(STATUS_FAIL);
            clusterConfigVO.setMessage(devopsEnvCommandDTO.getError());
        }
        return clusterConfigVO;
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
