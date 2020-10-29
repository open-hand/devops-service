package io.choerodon.devops.app.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import io.choerodon.devops.app.service.DevopsDeployRecordService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsDeployRecordMapper;
import io.choerodon.devops.infra.util.CiCdPipelineUtils;
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
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;


    @Override
    public void baseCreate(DevopsDeployRecordDTO devopsDeployRecordDTO) {
        Objects.requireNonNull(devopsDeployRecordDTO.getDeployTime(), "Deploy time can't be null");
        if (devopsDeployRecordMapper.insert(devopsDeployRecordDTO) != 1) {
            throw new CommonException("error.deploy.record.insert");
        }
    }


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
