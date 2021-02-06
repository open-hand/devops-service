package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsClusterNodeDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author lihao
 */
public interface DevopsClusterNodeMapper extends BaseMapper<DevopsClusterNodeDTO> {
    Integer batchInsert(@Param("devopsClusterNodeDTOList") List<DevopsClusterNodeDTO> devopsClusterNodeDTOList);

    int countByRoleSet(@Param("clusterId") Long clusterId,
                       @Param("roleSet") Set<Integer> roleSet);

    /**
     * 根据集群id查出所有节点
     *
     * @param clusterId 集群id
     * @return 节点列表
     */
    List<DevopsClusterNodeDTO> listByClusterId(@Param("clusterId") Long clusterId);

    /**
     * 通过cluster id删除node
     * @param clusterId 集群id
     */
    void deleteByClusterId(@Param("clusterId") Long clusterId);
}
