package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;


import io.choerodon.devops.api.dto.DevopsEnvPodDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsClusterDO;

public interface DevopsClusterMapper extends Mapper<DevopsClusterDO> {

    List<DevopsClusterDO> listByProjectId(@Param("projectId") Long projectId,@Param("organizationId") Long organizationId);

    void updateSkipCheckPro(@Param("clusterId") Long clusterId, @Param("skipCheckPro") Boolean skipCheckPro);

    List<DevopsClusterDO> listClusters(@Param("organizationId") Long organizationId,
                                       @Param("searchParam") Map<String, Object> searchParam,
                                       @Param("param") String param);

    /**
     * 查询节点下的Pod
     * @param clusterId 集群id
     * @param nodeName 节点名称
     * @param searchParam 查询参数
     * @return pods
     */
    List<DevopsEnvPodDTO> pageQueryPodsByNodeName(@Param("clusterId") Long clusterId,
                                                  @Param("nodeName") String nodeName,
                                                  @Param("searchParam") String searchParam);
}
