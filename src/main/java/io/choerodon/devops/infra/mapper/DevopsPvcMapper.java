package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsPvcDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsPvcMapper extends BaseMapper<DevopsPvcDTO> {
    List<DevopsPvcDTO> listByOption(@Param("envId") Long envId,
                                    @Param("searchParam") Map<String, Object> searchParam,
                                    @Param("params") List<String> params);

    /**
     * 根据id更新状态
     *
     * @param id     id
     * @param status 状态
     */
    void updateStatusById(@Param("id") Long id, @Param("status") String status);
}