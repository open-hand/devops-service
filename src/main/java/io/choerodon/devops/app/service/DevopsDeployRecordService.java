package io.choerodon.devops.app.service;


import java.util.Date;
import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.AppServiceInstanceForRecordVO;
import io.choerodon.devops.api.vo.DeployRecordCountVO;
import io.choerodon.devops.api.vo.DeployRecordVO;
import io.choerodon.devops.api.vo.DevopsDeployRecordVO;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.devops.infra.dto.DevopsDeployRecordInstanceDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Sheep on 2019/7/29.
 */
public interface DevopsDeployRecordService {

    Page<DevopsDeployRecordVO> pageByProjectId(Long projectId, String params, PageRequest pageable);


    Page<DevopsDeployRecordDTO> basePageByProjectId(Long projectId, String params, PageRequest pageable);

    void baseCreate(DevopsDeployRecordDTO devopsDeployRecordDTO);

//    void createRecordForBatchDeployment(Long projectId, Long envId, List<DevopsDeployRecordInstanceDTO> instances);

    void baseDelete(DevopsDeployRecordDTO devopsDeployRecordDTO);

    /**
     * 通过环境id删除只属于该环境的手动部署纪录
     *
     * @param envId 环境id
     */
//    void deleteManualAndBatchRecordByEnv(Long envId);

    /**
     * 通过部署纪录id删除关联的 `devops_deploy_record_instance`表纪录
     *
     * @param recordIds 部署纪录id
     */
//    void deleteRecordInstanceByRecordIds(List<Long> recordIds);


    /**
     * 删除手动部署生成的实例相关的部署纪录
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
     * @param projectId
     * @param pageRequest
     * @param deployType
     * @param deployResult
     * @return
     */
    Page<DeployRecordVO> paging(Long projectId, PageRequest pageRequest, String envName, String appServiceName, String deployType, String deployResult);
}
