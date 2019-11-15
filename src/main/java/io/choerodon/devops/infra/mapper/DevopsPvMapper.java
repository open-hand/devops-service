package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import io.choerodon.devops.infra.dto.DevopsPvDTO;

public interface DevopsPvMapper extends Mapper<DevopsPvDTO> {


    List<DevopsPvDTO> listPvByOptions(@Param("organizationId") Long organizationId,
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


    DevopsPvDTO queryWithEnvByPrimaryKey(@Param("pvId") Long pvId);
}
