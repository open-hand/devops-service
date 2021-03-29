package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsPvDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsPvMapper extends BaseMapper<DevopsPvDTO> {


    List<DevopsPvDTO> listPvByOptions(@Param("organizationId") Long organizationId,
                                      @Param("projectId") Long projectId,
                                      @Param("clusterId") Long clusterId,
                                      @Param("orderBy") String orderBy,
                                      @Param("searchParam") Map<String, Object> searchParamMap,
                                      @Param("params") List<String> params);

    /**
     * 和cluster表和pvc表做连接查询获取name
     *
     * @return
     */
    DevopsPvDTO queryById(@Param("pvId") Long pvId);

    /**
     * 通过名称与集群id查询PV
     *
     * @param name
     * @param clusterId
     * @return
     */
    DevopsPvDTO queryByNameAndClusterId(@Param("name") String name,
                                        @Param("clusterId") Long clusterId);

    /**
     * 通过环境id和名称查找pvc
     *
     * @param envId 环境id
     * @param name  pv名称
     * @return pv纪录
     */
    DevopsPvDTO queryByEnvIdAndName(@Param("envId") Long envId,
                                    @Param("pvName") String name);

    /**
     * 根据集群id和PV名称查询PV
     *
     * @param clusterId 集群id
     * @param name      pv名称
     * @return pv
     */
    DevopsPvDTO queryWithEnvByClusterIdAndName(@Param("clusterId") Long clusterId,
                                               @Param("name") String name);


    DevopsPvDTO queryWithEnvByPrimaryKey(@Param("pvId") Long pvId);

    /**
     * 根据id更新状态
     *
     * @param id     id
     * @param status 状态
     */
    void updateStatusById(@Param("id") Long id, @Param("status") String status);

    List<DevopsPvDTO> listByPvIds(@Param("pvIds") List<Long> pvIds);

    List<String> listLabelsByClusterId(Long clusterId);
}
