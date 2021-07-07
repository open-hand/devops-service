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
import io.kubernetes.client.models.V1beta2DaemonSet;
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
import io.choerodon.devops.api.vo.DaemonSetInfoVO;
import io.choerodon.devops.api.vo.DevopsDaemonSetVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsDaemonSetMapper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:19
 */
@Service
public class DevopsDaemonSetServiceImpl implements DevopsDaemonSetService, ChartResourceOperatorService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsDaemonSetServiceImpl.class);
    @Autowired
    private DevopsDaemonSetMapper devopsDaemonSetMapper;
    private final JSON json = new JSON();
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

    @Override
    public Page<DaemonSetInfoVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name, Boolean fromInstance) {
        Page<DevopsDaemonSetVO> devopsDaemonSetVOPage = PageHelper.doPage(pageable, () -> devopsDaemonSetMapper.listByEnvId(envId, name, fromInstance));
        Page<DaemonSetInfoVO> daemonSetInfoVOPage = new Page<>();
        if (CollectionUtils.isEmpty(devopsDaemonSetVOPage.getContent())) {
            return daemonSetInfoVOPage;
        }
        Set<Long> detailsIds = devopsDaemonSetVOPage.getContent().stream().map(DevopsDaemonSetVO::getResourceDetailId)
                .collect(Collectors.toSet());
        List<DevopsEnvResourceDetailDTO> devopsEnvResourceDetailDTOS = devopsEnvResourceDetailService.listByMessageIds(detailsIds);
        Map<Long, DevopsEnvResourceDetailDTO> detailDTOMap = devopsEnvResourceDetailDTOS.stream().collect(Collectors.toMap(DevopsEnvResourceDetailDTO::getId, Function.identity()));

        return ConvertUtils.convertPage(devopsDaemonSetVOPage, v -> {
            DaemonSetInfoVO daemonSetInfoVO = ConvertUtils.convertObject(v, DaemonSetInfoVO.class);
            if (detailDTOMap.get(v.getResourceDetailId()) != null) {
                // 参考实例详情查询逻辑
                V1beta2DaemonSet v1beta2DaemonSet = json.deserialize(detailDTOMap.get(v.getResourceDetailId()).getMessage(), V1beta2DaemonSet.class);


                daemonSetInfoVO.setName(v1beta2DaemonSet.getMetadata().getName());
                daemonSetInfoVO.setCurrentScheduled(TypeUtil.objToLong(v1beta2DaemonSet.getStatus().getCurrentNumberScheduled()));
                daemonSetInfoVO.setDesiredScheduled(TypeUtil.objToLong(v1beta2DaemonSet.getStatus().getDesiredNumberScheduled()));
                daemonSetInfoVO.setNumberAvailable(TypeUtil.objToLong(v1beta2DaemonSet.getStatus().getNumberAvailable()));
                daemonSetInfoVO.setNumberReady(TypeUtil.objToLong(v1beta2DaemonSet.getStatus().getNumberReady()));

                daemonSetInfoVO.setLabels(v1beta2DaemonSet.getSpec().getSelector().getMatchLabels());
                List<Integer> portRes = new ArrayList<>();
                for (V1Container container : v1beta2DaemonSet.getSpec().getTemplate().getSpec().getContainers()) {
                    List<V1ContainerPort> ports = container.getPorts();
                    Optional.ofNullable(ports).ifPresent(portList -> {
                        for (V1ContainerPort port : portList) {
                            portRes.add(port.getContainerPort());
                        }
                    });
                }
                daemonSetInfoVO.setPorts(portRes);
                ZoneId zoneId = ZoneId.systemDefault();
                daemonSetInfoVO.setAge(v.getLastUpdateDate().toInstant().atZone(zoneId).toLocalDateTime().format(DATE_TIME_FORMATTER));
            }
            return daemonSetInfoVO;
        });
    }


    @Override
    public DevopsDaemonSetDTO selectByPrimaryKey(Long resourceId) {
        return devopsDaemonSetMapper.selectByPrimaryKey(resourceId);
    }

    @Override
    public void checkExist(Long envId, String name) {
        if (devopsDaemonSetMapper.selectCountByEnvIdAndName(envId, name) != 0) {
            throw new CommonException("error.workload.exist", "DaemonSet", name);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long baseCreate(DevopsDaemonSetDTO devopsDaemonSetDTO) {
        devopsDaemonSetMapper.insert(devopsDaemonSetDTO);
        return devopsDaemonSetDTO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(DevopsDaemonSetDTO devopsDaemonSetDTOToUpdate) {
        if (devopsDaemonSetDTOToUpdate.getObjectVersionNumber() == null) {
            DevopsDaemonSetDTO devopsDaemonSetDTO = devopsDaemonSetMapper.selectByPrimaryKey(devopsDaemonSetDTOToUpdate.getId());
            devopsDaemonSetDTOToUpdate.setObjectVersionNumber(devopsDaemonSetDTO.getObjectVersionNumber());
        }
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsDaemonSetMapper, devopsDaemonSetDTOToUpdate, "error.daemonset.update");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(Long id) {
        devopsDaemonSetMapper.deleteByPrimaryKey(id);
        devopsWorkloadResourceContentService.deleteByResourceId(ResourceType.DAEMONSET.getType(), id);
    }

    @Override
    public DevopsDaemonSetDTO baseQueryByEnvIdAndName(Long envId, String name) {
        DevopsDaemonSetDTO devopsDaemonSetDTO = new DevopsDaemonSetDTO();
        devopsDaemonSetDTO.setEnvId(envId);
        devopsDaemonSetDTO.setName(name);
        return devopsDaemonSetMapper.selectOne(devopsDaemonSetDTO);
    }

    @Override
    @Transactional
    public void saveOrUpdateChartResource(String detailsJson, AppServiceInstanceDTO appServiceInstanceDTO) {
        V1beta2DaemonSet v1beta2DaemonSet = json.deserialize(detailsJson, V1beta2DaemonSet.class);

        DevopsDaemonSetDTO oldDevopsDaemonSetDTO = baseQueryByEnvIdAndName(appServiceInstanceDTO.getEnvId(), v1beta2DaemonSet.getMetadata().getName());
        if (oldDevopsDaemonSetDTO != null) {
            oldDevopsDaemonSetDTO.setCommandId(appServiceInstanceDTO.getCommandId());
            devopsDaemonSetMapper.updateByPrimaryKeySelective(oldDevopsDaemonSetDTO);
        } else {

            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());
            if (devopsEnvironmentDTO == null) {
                LOGGER.error("save chart resource failed! env not found! envId: {}", appServiceInstanceDTO.getEnvId());
                return;
            }
            DevopsDaemonSetDTO devopsDaemonSetDTO = new DevopsDaemonSetDTO();

            devopsDaemonSetDTO.setEnvId(appServiceInstanceDTO.getEnvId());
            devopsDaemonSetDTO.setInstanceId(appServiceInstanceDTO.getId());
            devopsDaemonSetDTO.setCommandId(appServiceInstanceDTO.getId());
            devopsDaemonSetDTO.setProjectId(devopsEnvironmentDTO.getProjectId());
            devopsDaemonSetDTO.setName(v1beta2DaemonSet.getMetadata().getName());
            devopsDaemonSetMapper.insertSelective(devopsDaemonSetDTO);
        }
    }


    @Override
    @Transactional
    public void deleteByEnvIdAndName(Long envId, String name) {
        Assert.notNull(envId, ResourceCheckConstant.ERROR_ENV_ID_IS_NULL);
        Assert.notNull(name, ResourceCheckConstant.ERROR_RESOURCE_NAME_IS_NULL);
        DevopsDaemonSetDTO devopsDaemonSetDTO = new DevopsDaemonSetDTO();
        devopsDaemonSetDTO.setEnvId(envId);
        devopsDaemonSetDTO.setName(name);
        devopsDaemonSetMapper.delete(devopsDaemonSetDTO);

    }

    @Override
    public ResourceType getType() {
        return ResourceType.DAEMONSET;
    }


    @Override
    public void deleteByGitOps(Long id) {
        DevopsDaemonSetDTO devopsDaemonSetDTO = devopsDaemonSetMapper.selectByPrimaryKey(id);
        //校验环境是否链接
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsDaemonSetDTO.getEnvId());
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());

        devopsEnvCommandService.baseListByObject(ObjectType.DAEMONSET.getType(), id).forEach(devopsEnvCommandDTO -> devopsEnvCommandService.baseDelete(devopsEnvCommandDTO.getId()));
        devopsDaemonSetMapper.deleteByPrimaryKey(id);
        devopsWorkloadResourceContentService.deleteByResourceId(ResourceType.DAEMONSET.getType(), id);
    }

    @Override
    public DevopsDaemonSetVO createOrUpdateByGitOps(DevopsDaemonSetVO devopsDaemonSetVO, Long userId, String content) {
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsDaemonSetVO.getEnvId());
        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());
        DevopsEnvCommandDTO devopsEnvCommandDTO = WorkloadServiceImpl.initDevopsEnvCommandDTO(ResourceType.DAEMONSET.getType(), devopsDaemonSetVO.getOperateType(), userId);
        devopsEnvCommandDTO.setCreatedBy(userId);

        DevopsDaemonSetDTO devopsDaemonSetDTO = ConvertUtils.convertObject(devopsDaemonSetVO, DevopsDaemonSetDTO.class);

        if (devopsDaemonSetVO.getOperateType().equals(CREATE_TYPE)) {
            Long daemonSetId = baseCreate(devopsDaemonSetDTO);
            devopsWorkloadResourceContentService.create(ResourceType.DAEMONSET.getType(), daemonSetId, content);
            devopsEnvCommandDTO.setObjectId(daemonSetId);
            devopsDaemonSetDTO.setId(daemonSetId);
        } else {
            devopsEnvCommandDTO.setObjectId(devopsDaemonSetDTO.getId());
        }

        devopsDaemonSetDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsDaemonSetDTO);
        return io.choerodon.devops.infra.util.ConvertUtils.convertObject(devopsDaemonSetDTO, DevopsDaemonSetVO.class);
    }
}
