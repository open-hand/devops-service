package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsClusterNodeDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author lihao
 */
public interface DevopsClusterNodeMapper extends BaseMapper<DevopsClusterNodeDTO> {
    Integer batchInsert(@Param("devopsClusterNodeDTOList") List<DevopsClusterNodeDTO> devopsClusterNodeDTOList);
}
