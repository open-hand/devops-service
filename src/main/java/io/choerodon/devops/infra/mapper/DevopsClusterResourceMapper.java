package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsClusterResourceDTO;
import io.choerodon.mybatis.common.BaseMapper;

import org.apache.ibatis.annotations.Param;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
public interface DevopsClusterResourceMapper extends BaseMapper<DevopsClusterResourceDTO> {

    DevopsClusterResourceDTO queryByClusterIdAndType(@Param("clusterId") Long clusterId, @Param("type") String type);

    /**
     * 查询可能存在的CertManager版本 （不论状态）
     *
     * @param clusterId 集群id
     * @return 可能存在的CertManager版本 （不论状态）
     */
    String queryCertManagerVersion(@Param("clusterId") Long clusterId);
}
