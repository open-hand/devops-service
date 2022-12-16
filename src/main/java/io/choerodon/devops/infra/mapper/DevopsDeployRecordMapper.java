package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.AppServiceInstanceForRecordVO;
import io.choerodon.devops.api.vo.DeployRecordVO;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by Sheep on 2019/7/29.
 */

public interface DevopsDeployRecordMapper extends BaseMapper<DevopsDeployRecordDTO> {

    void deleteRelatedRecordOfInstance(@Param("instanceId") Long instanceId);

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
