package io.choerodon.devops.app.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.AppServiceInstanceForRecordVO;
import io.choerodon.devops.api.vo.DeployRecordCountVO;
import io.choerodon.devops.api.vo.DeployRecordVO;
import io.choerodon.devops.api.vo.DevopsDeployRecordVO;
import io.choerodon.devops.app.service.DevopsDeployRecordService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.PermissionHelper;
import io.choerodon.devops.app.service.PipelineService;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsDeployRecordMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Sheep on 2019/7/29.
 */
@Service
public class DevopsDeployRecordServiceImpl implements DevopsDeployRecordService {
    private static final String DEPLOY_STATUS = "deployStatus";
    private static final String DEPLOY_TYPE = "deployType";
    private static final String PIPELINE_ID = "pipelineId";
    private static final String RUNNING = "running";
    private static final String ENV_ID = "env";

    @Autowired
    private DevopsDeployRecordMapper devopsDeployRecordMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private PipelineService pipelineService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;

    @Override
    public Page<DevopsDeployRecordVO> pageByProjectId(Long projectId, String params, PageRequest pageable) {
        Boolean projectOwnerOrRoot = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId);

        Page<DevopsDeployRecordDTO> devopsDeployRecordDTOPageInfo = basePageByProjectId(projectId, params, pageable);

        Page<DevopsDeployRecordVO> devopsDeployRecordVOPageInfo = ConvertUtils.convertPage(devopsDeployRecordDTOPageInfo, DevopsDeployRecordVO.class);

        //查询用户信息
        List<Long> userIds = devopsDeployRecordVOPageInfo.getContent().stream().map(DevopsDeployRecordVO::getDeployCreatedBy).collect(Collectors.toList());
        Map<Long, IamUserDTO> userMap = new HashMap<>(pageable.getSize());
        baseServiceClientOperator.listUsersByIds(userIds).forEach(user -> userMap.put(user.getId(), user));

        //设置环境信息以及用户信息
        devopsDeployRecordVOPageInfo.getContent().forEach(devopsDeployRecordVO -> {
            if (devopsDeployRecordVO.getDeployType().equals("auto") && !devopsDeployRecordVO.getDeployStatus().equals("success")) {
                pipelineService.setPipelineRecordDetail(projectOwnerOrRoot, devopsDeployRecordVO);
            }
            if (userMap.containsKey(devopsDeployRecordVO.getDeployCreatedBy())) {
                IamUserDTO targetUser = userMap.get(devopsDeployRecordVO.getDeployCreatedBy());
                devopsDeployRecordVO.setUserName(targetUser.getRealName());
                if (targetUser.getLdap()) {
                    devopsDeployRecordVO.setUserLoginName(targetUser.getLoginName());
                } else {
                    devopsDeployRecordVO.setUserLoginName(targetUser.getEmail());
                }
                devopsDeployRecordVO.setUserImage(targetUser.getImageUrl());
            }
            //处理显示编号
            devopsDeployRecordVO.setViewId(CiCdPipelineUtils.handleId(devopsDeployRecordVO.getId()));
        });
        return devopsDeployRecordVOPageInfo;
    }


    @Override
    public Page<DevopsDeployRecordDTO> basePageByProjectId(Long projectId, String params, PageRequest pageable) {
        Map<String, Object> maps = TypeUtil.castMapParams(params);
        Map<String, Object> cast = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        if (cast.get(DEPLOY_TYPE) != null && cast.get(DEPLOY_STATUS) != null) {
            if (DeployType.MANUAL.getType().equals(cast.get(DEPLOY_TYPE)) && RUNNING.equals(cast.get(DEPLOY_STATUS))) {
                cast.put(DEPLOY_STATUS, "operating");
            } else if (DeployType.AUTO.getType().equals(cast.get(DEPLOY_TYPE)) && RUNNING.equals(cast.get(DEPLOY_STATUS))) {
                cast.put(DEPLOY_STATUS, RUNNING);
            }
        }
        Object pipelineId = cast.get(PIPELINE_ID);
        if (pipelineId instanceof String) {
            // 解密流水线id
            cast.put(PIPELINE_ID, Long.valueOf(KeyDecryptHelper.decryptValueOrIgnore((String) pipelineId)));
        }

        // 解密查询参数中的环境id
        Object envId = cast.get(ENV_ID);
        if (envId instanceof String) {
            cast.put(ENV_ID, Long.valueOf(KeyDecryptHelper.decryptValueOrIgnore((String) envId)));
        }

        maps.put(TypeUtil.SEARCH_PARAM, cast);

        return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> devopsDeployRecordMapper.listByProjectId(projectId,
                TypeUtil.cast(maps.get(TypeUtil.PARAMS)),
                TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM))
        ));
    }


    @Override
    public void baseCreate(DevopsDeployRecordDTO devopsDeployRecordDTO) {
        Objects.requireNonNull(devopsDeployRecordDTO.getDeployTime(), "Deploy time can't be null");
        if (devopsDeployRecordMapper.insert(devopsDeployRecordDTO) != 1) {
            throw new CommonException("error.deploy.record.insert");
        }
    }

//    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
//    @Override
//    public void createRecordForBatchDeployment(Long projectId, Long envId, List<DevopsDeployRecordInstanceDTO> instances) {
//        if (CollectionUtils.isEmpty(instances)) {
//            throw new CommonException("error.instances.empty");
//        }
//        DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(projectId, DeployType.BATCH.getType(), null, String.valueOf(envId), new Date());
//        baseCreate(devopsDeployRecordDTO);
//        Long deployRecordId = devopsDeployRecordDTO.getId();
//        instances.forEach(i -> i.setDeployRecordId(deployRecordId));
//        devopsDeployRecordInstanceMapper.batchInsert(instances);
//    }

    @Override
    public void baseDelete(DevopsDeployRecordDTO devopsDeployRecordDTO) {
        devopsDeployRecordMapper.delete(devopsDeployRecordDTO);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void deleteRecordByEnv(Long envId) {
        DevopsDeployRecordDTO deleteCondition = new DevopsDeployRecordDTO();
        // 删除手动部署的纪录
        deleteCondition.setDeployPayloadId(envId);
        deleteCondition.setDeployMode(DeployModeEnum.ENV.value());
        devopsDeployRecordMapper.delete(deleteCondition);
    }

    @Override
    @Transactional
    public void deleteRelatedRecordOfInstance(Long instanceId) {
        devopsDeployRecordMapper.deleteRelatedRecordOfInstance(instanceId);
    }

    @Override
    public DeployRecordCountVO countByDate(Long projectId, Date startTime, Date endTime) {
        DeployRecordCountVO deployRecordCountVO = new DeployRecordCountVO();
        deployRecordCountVO.setId(projectId);

        List<DevopsDeployRecordDTO> devopsDeployRecordDTOList = devopsDeployRecordMapper.selectByProjectIdAndDate(projectId,
                new java.sql.Date(startTime.getTime()),
                new java.sql.Date(endTime.getTime()));
        // 按日期分组
        Map<String, List<DevopsDeployRecordDTO>> map = devopsDeployRecordDTOList.stream()
                .collect(Collectors.groupingBy(t -> new java.sql.Date(t.getDeployTime().getTime()).toString()));

        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate startDate = startTime.toInstant().atZone(zoneId).toLocalDate();
        LocalDate endDate = endTime.toInstant().atZone(zoneId).toLocalDate();

        while (startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
            String date = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            long countNum = 0;
            // 计算成功发送的邮件数
            List<DevopsDeployRecordDTO> devopsDeployRecordDTOS = map.get(date);
            if (!CollectionUtils.isEmpty(devopsDeployRecordDTOS)) {
                countNum = devopsDeployRecordDTOS.size();
            }

            deployRecordCountVO.getData().add(countNum);
            startDate = startDate.plusDays(1);
        }
        return deployRecordCountVO;
    }

    @Override
    public List<AppServiceInstanceForRecordVO> queryByBatchDeployRecordId(Long recordId) {
        // 这里不校验recordId是不是批量部署类型的部署纪录的id
        return devopsDeployRecordMapper.queryByBatchDeployRecordId(recordId);
    }


    @Override
    public Page<DeployRecordVO> paging(Long projectId, PageRequest pageRequest, String deployType, String deployMode, String deployPayloadName, String deployResult, String deployObjectName, String deployObjectVersion) {
        Page<DeployRecordVO> deployRecordVOPage = PageHelper
                .doPageAndSort(pageRequest,
                        () -> devopsDeployRecordMapper.listByParams(
                                projectId,
                                deployType,
                                deployMode,
                                deployPayloadName,
                                deployResult,
                                deployObjectName,
                                deployObjectVersion));
        // 添加用户信息
        if (CollectionUtils.isEmpty(deployRecordVOPage.getContent())) {
            return deployRecordVOPage;
        }
        List<Long> uIds = deployRecordVOPage.getContent().stream().map(DeployRecordVO::getCreatedBy).collect(Collectors.toList());

        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.queryUsersByUserIds(uIds);

        Map<Long, IamUserDTO> userMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));

        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();

        deployRecordVOPage.getContent().forEach(v -> {
            IamUserDTO iamUserDTO = userMap.get(v.getCreatedBy());
            if (iamUserDTO != null) {
                v.setExecuteUser(iamUserDTO);
            }
            v.setViewId(CiCdPipelineUtils.handleId(v.getId()));


            if (DeployModeEnum.ENV.value().equals(v.getDeployMode())) {
                // 计算部署结果
                v.setDeployResult(v.getCommandStatus());
                // 计算集群状态
                DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(v.getDeployPayloadId());
                v.setConnect(upgradeClusterList.contains(devopsEnvironmentDTO.getClusterId()));
            }

        });

        return deployRecordVOPage;
    }
}
