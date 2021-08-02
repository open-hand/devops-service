package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.MiscConstants.CREATE_TYPE;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ContainerPort;
import io.kubernetes.client.models.V1Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.DevopsJobVO;
import io.choerodon.devops.api.vo.JobInfoVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsJobMapper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;


/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:21
 */
@Service
public class DevopsJobServiceImpl implements DevopsJobService, ChartResourceOperatorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsJobServiceImpl.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private DevopsJobMapper devopsJobMapper;
    @Autowired
    private DevopsEnvResourceDetailService devopsEnvResourceDetailService;
    @Autowired
    private DevopsWorkloadResourceContentService devopsWorkloadResourceContentService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;

    private JSON json = new JSON();

    @Override
    public Page<JobInfoVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name, Boolean fromInstance) {
        Page<DevopsJobVO> devopsJobVOPage = PageHelper.doPage(pageable,
                () -> devopsJobMapper.listByEnvId(envId, name, fromInstance));
        Page<JobInfoVO> jobInfoVOPage = new Page<>();
        if (CollectionUtils.isEmpty(devopsJobVOPage.getContent())) {
            return jobInfoVOPage;
        }
        Set<Long> detailsIds = devopsJobVOPage.getContent().stream().map(DevopsJobVO::getResourceDetailId)
                .collect(Collectors.toSet());
        List<DevopsEnvResourceDetailDTO> devopsEnvResourceDetailDTOS = devopsEnvResourceDetailService.listByMessageIds(detailsIds);
        Map<Long, DevopsEnvResourceDetailDTO> detailDTOMap = devopsEnvResourceDetailDTOS.stream().collect(Collectors.toMap(DevopsEnvResourceDetailDTO::getId, Function.identity()));

        return ConvertUtils.convertPage(devopsJobVOPage, v -> {
            JobInfoVO jobInfoVO = ConvertUtils.convertObject(v, JobInfoVO.class);
            if (detailDTOMap.get(v.getResourceDetailId()) != null) {
                // 参考实例详情查询逻辑
                V1Job v1Job = json.deserialize(
                        detailDTOMap.get(v.getResourceDetailId()).getMessage(),
                        V1Job.class);

                jobInfoVO.setCompletions(v1Job.getSpec().getCompletions());
                jobInfoVO.setActive(v1Job.getStatus().getActive() == null ? 0 : v1Job.getStatus().getActive());
                jobInfoVO.setLabels(v1Job.getSpec().getSelector().getMatchLabels());
                List<Integer> portRes = new ArrayList<>();
                for (V1Container container : v1Job.getSpec().getTemplate().getSpec().getContainers()) {
                    List<V1ContainerPort> ports = container.getPorts();
                    Optional.ofNullable(ports).ifPresent(portList -> {
                        for (V1ContainerPort port : portList) {
                            portRes.add(port.getContainerPort());
                        }
                    });
                }
                jobInfoVO.setPorts(portRes);
                if (v1Job.getStatus() != null && v1Job.getStatus().getCompletionTime() != null) {
                    jobInfoVO.setAge(v1Job.getStatus().getCompletionTime().toString("yyyy-MM-dd HH:mm:ss"));
                } else {
                    ZoneId zoneId = ZoneId.systemDefault();
                    jobInfoVO.setAge(v.getLastUpdateDate().toInstant().atZone(zoneId).toLocalDateTime().format(DATE_TIME_FORMATTER));
                }
            }
            return jobInfoVO;
        });
    }

    @Override
    public DevopsJobDTO selectByPrimaryKey(Long resourceId) {
        return devopsJobMapper.selectByPrimaryKey(resourceId);
    }

    @Override
    public void checkExist(Long envId, String name) {
        if (devopsJobMapper.selectCountByEnvIdAndName(envId, name) != 0) {
            throw new CommonException("error.workload.exist", "Job", name);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long baseCreate(DevopsJobDTO devopsJobDTO) {
        devopsJobMapper.insert(devopsJobDTO);
        return devopsJobDTO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(DevopsJobDTO devopsJobDTOToUpdate) {
        if (devopsJobDTOToUpdate.getObjectVersionNumber() == null) {
            DevopsJobDTO devopsJobDTO = devopsJobMapper.selectByPrimaryKey(devopsJobDTOToUpdate.getId());
            devopsJobDTOToUpdate.setObjectVersionNumber(devopsJobDTO.getObjectVersionNumber());
        }
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsJobMapper, devopsJobDTOToUpdate, "error.job.update");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(Long id) {
        devopsJobMapper.deleteByPrimaryKey(id);
        devopsWorkloadResourceContentService.deleteByResourceId(ResourceType.JOB.getType(), id);
    }

    @Override
    public DevopsJobDTO baseQueryByEnvIdAndName(Long envId, String name) {
        DevopsJobDTO devopsJobDTO = new DevopsJobDTO();
        devopsJobDTO.setEnvId(envId);
        devopsJobDTO.setName(name);
        return devopsJobMapper.selectOne(devopsJobDTO);
    }

    @Override
    public void deleteByGitOps(Long id) {
        DevopsJobDTO devopsJobDTO = devopsJobMapper.selectByPrimaryKey(id);
        //校验环境是否链接
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsJobDTO.getEnvId());
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());

        devopsEnvCommandService.baseListByObject(ObjectType.JOB.getType(), id).forEach(devopsEnvCommandDTO -> devopsEnvCommandService.baseDelete(devopsEnvCommandDTO.getId()));
        devopsJobMapper.deleteByPrimaryKey(id);
        devopsWorkloadResourceContentService.deleteByResourceId(ResourceType.JOB.getType(), id);
    }

    @Override
    @Transactional
    public DevopsJobVO createOrUpdateByGitOps(DevopsJobVO devopsJobVO, Long userId, String content) {
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsJobVO.getEnvId());
        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());
        DevopsEnvCommandDTO devopsEnvCommandDTO = WorkloadServiceImpl.initDevopsEnvCommandDTO(ResourceType.JOB.getType(), devopsJobVO.getOperateType(), userId);
        devopsEnvCommandDTO.setCreatedBy(userId);

        DevopsJobDTO devopsJobDTO = ConvertUtils.convertObject(devopsJobVO, DevopsJobDTO.class);

        if (devopsJobVO.getOperateType().equals(CREATE_TYPE)) {
            Long devopsJob = baseCreate(devopsJobDTO);
            devopsWorkloadResourceContentService.create(ResourceType.JOB.getType(), devopsJob, content);
            devopsEnvCommandDTO.setObjectId(devopsJob);
            devopsJobDTO.setId(devopsJob);
        } else {
            devopsEnvCommandDTO.setObjectId(devopsJobDTO.getId());
        }

        devopsJobDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsJobDTO);
        return io.choerodon.devops.infra.util.ConvertUtils.convertObject(devopsJobDTO, DevopsJobVO.class);
    }

    @Override
    @Transactional
    public void saveOrUpdateChartResource(String detailsJson, AppServiceInstanceDTO appServiceInstanceDTO) {
        V1Job v1Job = json.deserialize(detailsJson, V1Job.class);

        DevopsJobDTO oldDevopsJobDTO = baseQueryByEnvIdAndName(appServiceInstanceDTO.getEnvId(), v1Job.getMetadata().getName());
        if (oldDevopsJobDTO != null) {
            oldDevopsJobDTO.setCommandId(appServiceInstanceDTO.getCommandId());
            devopsJobMapper.updateByPrimaryKeySelective(oldDevopsJobDTO);
        } else {

            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());
            if (devopsEnvironmentDTO == null) {
                LOGGER.error("save chart resource failed! env not found! envId: {}", appServiceInstanceDTO.getEnvId());
                return;
            }
            DevopsJobDTO devopsJobDTO = new DevopsJobDTO();

            devopsJobDTO.setEnvId(appServiceInstanceDTO.getEnvId());
            devopsJobDTO.setInstanceId(appServiceInstanceDTO.getId());
            devopsJobDTO.setCommandId(appServiceInstanceDTO.getId());
            devopsJobDTO.setProjectId(devopsEnvironmentDTO.getProjectId());
            devopsJobDTO.setName(v1Job.getMetadata().getName());
            devopsJobMapper.insertSelective(devopsJobDTO);
        }
    }


    @Override
    @Transactional
    public void deleteByEnvIdAndName(Long envId, String name) {
        Assert.notNull(envId, ResourceCheckConstant.ERROR_ENV_ID_IS_NULL);
        Assert.notNull(name, ResourceCheckConstant.ERROR_RESOURCE_NAME_IS_NULL);
        DevopsJobDTO devopsJobDTO = new DevopsJobDTO();
        devopsJobDTO.setEnvId(envId);
        devopsJobDTO.setName(name);
        devopsJobMapper.delete(devopsJobDTO);
    }

    @Override
    public ResourceType getType() {
        return ResourceType.JOB;
    }
}
