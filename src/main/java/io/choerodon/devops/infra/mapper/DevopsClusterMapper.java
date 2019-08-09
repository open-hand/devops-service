package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;


public interface DevopsClusterMapper extends Mapper<DevopsClusterDTO> {

    List<DevopsClusterDTO> listByProjectId(@Param("projectId") Long projectId, @Param("organizationId") Long organizationId);

    void updateSkipCheckPro(@Param("clusterId") Long clusterId, @Param("skipCheckPro") Boolean skipCheckPro);

    List<DevopsClusterDTO> listClusters(@Param("organizationId") Long organizationId,
                                        @Param("searchParam") Map<String, Object> searchParam,
                                        @Param("params") List<String> params);

    /**
     * 查询节点下的Pod
     *
     * @param clusterId   集群id
     * @param nodeName    节点名称
     * @param searchParam 查询参数
     * @return pods
     */
    List<DevopsEnvPodDTO> pageQueryPodsByNodeName(@Param("clusterId") Long clusterId,
                                                  @Param("nodeName") String nodeName,
                                                  @Param("searchParam") Map<String, Object> searchParam,
                                                  @Param("params") List<String> params);

    void updateProjectId(@Param("orgId") Long orgId,
                         @Param("proId") Long proId);
}
