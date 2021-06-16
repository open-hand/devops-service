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
import io.kubernetes.client.models.V1beta2StatefulSet;
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
import io.choerodon.devops.api.vo.DevopsStatefulSetVO;
import io.choerodon.devops.api.vo.StatefulSetInfoVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsStatefulSetMapper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:20
 */
@Service
public class DevopsStatefulSetServiceImpl implements DevopsStatefulSetService, ChartResourceOperatorService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsStatefulSetServiceImpl.class);
    @Autowired
    private DevopsStatefulSetMapper devopsStatefulSetMapper;
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
    public Page<StatefulSetInfoVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name, Boolean fromInstance) {
        Page<DevopsStatefulSetVO> devopsStatefulSetVOPage = PageHelper.doPage(pageable,
                () -> devopsStatefulSetMapper.listByEnvId(envId, name, fromInstance));
        Page<StatefulSetInfoVO> statefulSetInfoVOPage = new Page<>();
        if (CollectionUtils.isEmpty(devopsStatefulSetVOPage.getContent())) {
            return statefulSetInfoVOPage;
        }
        Set<Long> detailsIds = devopsStatefulSetVOPage.getContent().stream().map(DevopsStatefulSetVO::getResourceDetailId)
                .collect(Collectors.toSet());
        List<DevopsEnvResourceDetailDTO> devopsEnvResourceDetailDTOS = devopsEnvResourceDetailService.listByMessageIds(detailsIds);
        Map<Long, DevopsEnvResourceDetailDTO> detailDTOMap = devopsEnvResourceDetailDTOS.stream().collect(Collectors.toMap(DevopsEnvResourceDetailDTO::getId, Function.identity()));

        return ConvertUtils.convertPage(devopsStatefulSetVOPage, v -> {
            StatefulSetInfoVO statefulSetInfoVO = ConvertUtils.convertObject(v, StatefulSetInfoVO.class);
            if (detailDTOMap.get(v.getResourceDetailId()) != null) {
                // 参考实例详情查询逻辑
                V1beta2StatefulSet v1beta2StatefulSet = json.deserialize(detailDTOMap.get(v.getResourceDetailId()).getMessage(), V1beta2StatefulSet.class);


                statefulSetInfoVO.setName(v1beta2StatefulSet.getMetadata().getName());
                statefulSetInfoVO.setDesiredReplicas(TypeUtil.objToLong(v1beta2StatefulSet.getSpec().getReplicas()));
                statefulSetInfoVO.setReadyReplicas(TypeUtil.objToLong(v1beta2StatefulSet.getStatus().getReadyReplicas()));
                statefulSetInfoVO.setCurrentReplicas(TypeUtil.objToLong(v1beta2StatefulSet.getStatus().getCurrentReplicas()));

                statefulSetInfoVO.setLabels(v1beta2StatefulSet.getSpec().getSelector().getMatchLabels());
                List<Integer> portRes = new ArrayList<>();
                for (V1Container container : v1beta2StatefulSet.getSpec().getTemplate().getSpec().getContainers()) {
                    List<V1ContainerPort> ports = container.getPorts();
                    Optional.ofNullable(ports).ifPresent(portList -> {
                        for (V1ContainerPort port : portList) {
                            portRes.add(port.getContainerPort());
                        }
                    });
                }
                statefulSetInfoVO.setPorts(portRes);
                ZoneId zoneId = ZoneId.systemDefault();
                statefulSetInfoVO.setAge(v.getLastUpdateDate().toInstant().atZone(zoneId).toLocalDateTime().format(DATE_TIME_FORMATTER));

            }
            return statefulSetInfoVO;
        });
    }

    @Override
    public DevopsStatefulSetDTO selectByPrimaryKey(Long resourceId) {
        return devopsStatefulSetMapper.selectByPrimaryKey(resourceId);
    }

    @Override
    public void checkExist(Long envId, String name) {
        if (devopsStatefulSetMapper.selectCountByEnvIdAndName(envId, name) != 0) {
            throw new CommonException("error.workload.exist", "StatefulSet", name);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long baseCreate(DevopsStatefulSetDTO devopsStatefulSetDTO) {
        devopsStatefulSetMapper.insert(devopsStatefulSetDTO);
        return devopsStatefulSetDTO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(DevopsStatefulSetDTO devopsStatefulSetDTOToUpdate) {
        if (devopsStatefulSetDTOToUpdate.getObjectVersionNumber() == null) {
            DevopsStatefulSetDTO devopsStatefulSetDTO = devopsStatefulSetMapper.selectByPrimaryKey(devopsStatefulSetDTOToUpdate.getId());
            devopsStatefulSetDTOToUpdate.setObjectVersionNumber(devopsStatefulSetDTO.getObjectVersionNumber());
        }
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsStatefulSetMapper, devopsStatefulSetDTOToUpdate, "error.statefulset.update");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(Long id) {
        devopsStatefulSetMapper.deleteByPrimaryKey(id);
        devopsWorkloadResourceContentService.deleteByResourceId(ResourceType.STATEFULSET.getType(), id);
    }

    @Override
    public DevopsStatefulSetDTO baseQueryByEnvIdAndName(Long envId, String name) {
        DevopsStatefulSetDTO devopsStatefulSetDTO = new DevopsStatefulSetDTO();
        devopsStatefulSetDTO.setEnvId(envId);
        devopsStatefulSetDTO.setName(name);
        return devopsStatefulSetMapper.selectOne(devopsStatefulSetDTO);
    }

    @Override
    public void deleteByGitOps(Long id) {
        DevopsStatefulSetDTO devopsStatefulSetDTO = devopsStatefulSetMapper.selectByPrimaryKey(id);
        //校验环境是否链接
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsStatefulSetDTO.getEnvId());
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());

        devopsEnvCommandService.baseListByObject(ObjectType.STATEFULSET.getType(), id).forEach(devopsEnvCommandDTO -> devopsEnvCommandService.baseDelete(devopsEnvCommandDTO.getId()));
        devopsStatefulSetMapper.deleteByPrimaryKey(id);
        devopsWorkloadResourceContentService.deleteByResourceId(ResourceType.STATEFULSET.getType(), id);
    }

    @Override
    public DevopsStatefulSetVO createOrUpdateByGitOps(DevopsStatefulSetVO devopsStatefulSetVO, Long userId, String content) {
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsStatefulSetVO.getEnvId());
        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());
        DevopsEnvCommandDTO devopsEnvCommandDTO = WorkloadServiceImpl.initDevopsEnvCommandDTO(ResourceType.STATEFULSET.getType(), devopsStatefulSetVO.getOperateType(), userId);
        devopsEnvCommandDTO.setCreatedBy(userId);

        DevopsStatefulSetDTO devopsStatefulSetDTO = ConvertUtils.convertObject(devopsStatefulSetVO, DevopsStatefulSetDTO.class);

        if (devopsStatefulSetVO.getOperateType().equals(CREATE_TYPE)) {
            Long deployId = baseCreate(devopsStatefulSetDTO);
            devopsWorkloadResourceContentService.create(ResourceType.STATEFULSET.getType(), deployId, content);
            devopsEnvCommandDTO.setObjectId(deployId);
            devopsStatefulSetDTO.setId(deployId);
        } else {
            devopsEnvCommandDTO.setObjectId(devopsStatefulSetDTO.getId());
        }

        devopsStatefulSetDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsStatefulSetDTO);
        return io.choerodon.devops.infra.util.ConvertUtils.convertObject(devopsStatefulSetDTO, DevopsStatefulSetVO.class);
    }

    @Override
    public void saveOrUpdateChartResource(String detailsJson, AppServiceInstanceDTO appServiceInstanceDTO) {
        V1beta2StatefulSet v1beta2StatefulSet = json.deserialize(detailsJson, V1beta2StatefulSet.class);

        DevopsStatefulSetDTO oldDevopsStatefulSetDTO = baseQueryByEnvIdAndName(appServiceInstanceDTO.getEnvId(), v1beta2StatefulSet.getMetadata().getName());
        if (oldDevopsStatefulSetDTO != null) {
            oldDevopsStatefulSetDTO.setCommandId(appServiceInstanceDTO.getCommandId());
            devopsStatefulSetMapper.updateByPrimaryKeySelective(oldDevopsStatefulSetDTO);
        } else {

            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());
            if (devopsEnvironmentDTO == null) {
                LOGGER.error("save chart resource failed! env not found! envId: {}", appServiceInstanceDTO.getEnvId());
                return;
            }
            DevopsStatefulSetDTO devopsStatefulSetDTO = new DevopsStatefulSetDTO();

            devopsStatefulSetDTO.setEnvId(appServiceInstanceDTO.getEnvId());
            devopsStatefulSetDTO.setInstanceId(appServiceInstanceDTO.getId());
            devopsStatefulSetDTO.setCommandId(appServiceInstanceDTO.getId());
            devopsStatefulSetDTO.setProjectId(devopsEnvironmentDTO.getProjectId());
            devopsStatefulSetDTO.setName(v1beta2StatefulSet.getMetadata().getName());
            devopsStatefulSetMapper.insertSelective(devopsStatefulSetDTO);
        }
    }

    @Override
    public void deleteByEnvIdAndName(Long envId, String name) {
        Assert.notNull(envId, ResourceCheckConstant.ERROR_ENV_ID_IS_NULL);
        Assert.notNull(name, ResourceCheckConstant.ERROR_RESOURCE_NAME_IS_NULL);
        DevopsStatefulSetDTO devopsStatefulSetDTO = new DevopsStatefulSetDTO();
        devopsStatefulSetDTO.setEnvId(envId);
        devopsStatefulSetDTO.setName(name);
        devopsStatefulSetMapper.delete(devopsStatefulSetDTO);
    }

    @Override
    public ResourceType getType() {
        return ResourceType.STATEFULSET;
    }
}
