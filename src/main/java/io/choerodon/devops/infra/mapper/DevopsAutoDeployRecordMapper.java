package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dataobject.DevopsAutoDeployRecordDO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:14 2019/2/27
 * Description:
 */
public interface DevopsAutoDeployRecordMapper extends BaseMapper<DevopsAutoDeployRecordDO> {
    List<DevopsAutoDeployRecordDO> list(@Param("projectId") Long projectId,
                                        @Param("appId") Long appId,
                                        @Param("envId") Long envId,
                                        @Param("taskName") String taskName,
                                        @Param("searchParam") Map<String, Object> searchParam,
                                        @Param("param") String param);

    void banchUpdateStatus(@Param("autoDeployId") Long autoDeployId,
                           @Param("status") String status);
}
