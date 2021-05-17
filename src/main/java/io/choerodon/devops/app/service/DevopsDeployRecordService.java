package io.choerodon.devops.app.service;


import java.util.Date;
import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.AppServiceInstanceForRecordVO;
import io.choerodon.devops.api.vo.DeployRecordCountVO;
import io.choerodon.devops.api.vo.DeployRecordVO;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Sheep on 2019/7/29.
 */
public interface DevopsDeployRecordService {

    Long saveRecord(Long projectId, DeployType type, Long deployId, DeployModeEnum deployMode, Long deployPayloadId, String deployPayloadName, String deployResult, DeployObjectTypeEnum deployObjectType, String deployObjectName, String deployVersion, String instanceName, DeploySourceVO deploySourceVO, Long userId);

    Long saveFailRecord(Long projectId, DeployType type, Long deployId, DeployModeEnum deployMode, Long deployPayloadId, String deployPayloadName, String deployResult, DeployObjectTypeEnum deployObjectType, String deployObjectName, String deployVersion, String instanceName, DeploySourceVO deploySourceVO, Long userId, String errorMessage);

    void baseCreate(DevopsDeployRecordDTO devopsDeployRecordDTO);

    void updateRecord(Long recordId, String status, String errorMsg);

    void updateRecord(DevopsDeployRecordDTO devopsDeployRecordDTO);

    List<DevopsDeployRecordDTO> baseList(DevopsDeployRecordDTO devopsDeployRecordDTO);

    void baseDelete(DevopsDeployRecordDTO devopsDeployRecordDTO);

    /**
     * 通过环境id删除只属于该环境的手动部署纪录
     *
     * @param envId 环境id
     */
    void deleteRecordByEnv(Long envId);

    /**
     * 删除部署生成的实例相关的部署纪录
     *
     * @param instanceId 实例id
     */
    void deleteRelatedRecordOfInstance(Long instanceId);

    /**
     * 按时间段，统计项目每日的部署次数
     *
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
     */
    DeployRecordCountVO countByDate(Long projectId, Date startTime, Date endTime);

    /**
     * 根据批量部署id查询对应的实例信息
     *
     * @param recordId 批量部署纪录id
     * @return 对应的实例列表
     */
    List<AppServiceInstanceForRecordVO> queryByBatchDeployRecordId(Long recordId);

    /**
     * 分页查询部署记录
     *
     * @param projectId
     * @param pageRequest
     * @param deployType
     * @param deployMode
     * @param deployPayloadName
     * @param deployResult
     * @param deployObjectName
     * @param deployObjectVersion
     * @return
     */
    Page<DeployRecordVO> paging(Long projectId, PageRequest pageRequest, String deployType, String deployMode, String deployPayloadName, String deployResult, String deployObjectName, String deployObjectVersion);

    DeployRecordVO queryEnvDeployRecordByCommandId(Long commandId);
}
