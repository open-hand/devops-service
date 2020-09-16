package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author zmf
 * @since 2020/9/15
 */
public interface DevopsHostMapper extends BaseMapper<DevopsHostDTO> {
    /**
     * 根据参数查询主机数据列表
     *
     * @param projectId   项目id
     * @param searchParam 查询参数
     * @param params      模糊搜索参数
     * @return 主机列表
     */
    List<DevopsHostDTO> listByOptions(@Param("project_id") Long projectId,
                                      @Param("searchParam") Map<String, Object> searchParam,
                                      @Param("params") List<String> params);
}
