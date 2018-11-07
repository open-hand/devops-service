package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsClusterDO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsClusterMapper extends BaseMapper<DevopsClusterDO> {

    List<DevopsClusterDO> listByProjectId(@Param("projectId") Long projectId);

    void updateSkipCheckPro(@Param("clusterId") Long clusterId, @Param("skipCheckPro") Boolean skipCheckPro);

}
