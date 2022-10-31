package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.AppServiceInstanceForRecordVO;
import io.choerodon.devops.api.vo.DeployRecordVO;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by Sheep on 2019/7/29.
 */

public interface DevopsDeployRecordMapper extends BaseMapper<DevopsDeployRecordDTO> {

    List<DevopsDeployRecordDTO> listByProjectId(@Param("projectId") Long projectId,
                                                @Param("params") List<String> params,
                                                @Param("searchParam") Map<String, Object> searchParam);

    void deleteRelatedRecordOfInstance(@Param("instanceId") Long instanceId);

    void batchInsertSelective(@Param("records") List<DevopsDeployRecordDTO> records);

    List<DevopsDeployRecordDTO> selectByProjectIdAndDate(@Param("projectId") Long projectId,
                                                         @Param("startTime") java.sql.Date startTime,
                                                         @Param("endTime") java.sql.Date endTime);

    List<Long> queryRecordIdByEnvIdAndDeployType(
            @Param("envId") String envId,
            @Param("deployType") String deployType);

    List<AppServiceInstanceForRecordVO> queryByBatchDeployRecordId(@Param("recordId") Long recordId);

    List<DeployRecordVO> listByParams(@Param("projectId") Long projectId,
                                      @Param("deployType") String deployType,
                                      @Param("deployMode") String deployMode,
                                      @Param("deployPayloadName") String deployPayloadName,
                                      @Param("deployResult") String deployResult,
                                      @Param("deployObjectName") String deployObjectName,
                                      @Param("deployObjectVersion") String deployObjectVersion,
                                      @Param("createdBy") Long createdBy);

    DeployRecordVO queryEnvDeployRecordByCommandId(@Param("commandId") Long commandId);

    DeployRecordVO queryHostDeployRecordByCommandId(@Param("commandId") Long commandId);
}
