package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.DevopsEnvWithPolarisResultVO;
import io.choerodon.devops.api.vo.polaris.InstanceWithPolarisStorageVO;
import io.choerodon.devops.infra.dto.DevopsPolarisNamespaceResultDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author zmf
 * @since 2/17/20
 */
public interface DevopsPolarisNamespaceResultMapper extends BaseMapper<DevopsPolarisNamespaceResultDTO> {
    void batchInsert(@Param("items") List<DevopsPolarisNamespaceResultDTO> items);

    /**
     * 查询带有扫描结果的实例数据
     *
     * @param recordId 扫描纪录id
     * @param envId    环境id
     * @return 数据
     */
    String queryNamespaceResultDetail(
            @Param("recordId") Long recordId,
            @Param("envId") Long envId);

    /**
     * 查询没有扫描结果的环境数据
     *
     * @param clusterId 集群id
     * @return 数据
     */
    List<DevopsEnvWithPolarisResultVO> queryEnvWithoutPolarisResult(@Param("clusterId") Long clusterId);

    /**
     * 查询带有扫描结果的环境数据
     * 包括空的内部环境
     *
     * @param recordId  扫描纪录id
     * @param clusterId 集群id
     * @return 数据
     */
    List<DevopsEnvWithPolarisResultVO> queryEnvWithPolarisResult(@Param("recordId") Long recordId, @Param("clusterId") Long clusterId);

    List<InstanceWithPolarisStorageVO> queryInstanceWithoutResult(@Param("envId") Long envId);
}
