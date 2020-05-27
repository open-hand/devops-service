package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCustomizeResourceContentDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by Sheep on 2019/6/27.
 */
public interface DevopsCustomizeResourceContentMapper extends BaseMapper<DevopsCustomizeResourceContentDTO> {
    void deleteByContentIds(@Param("contentIds") List<Long> contentIds);
}
