package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsCustomizeResourceContentDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Sheep on 2019/6/27.
 */
public interface DevopsCustomizeResourceContentMapper extends Mapper<DevopsCustomizeResourceContentDTO> {
    void deleteByContentIds(@Param("contentIds") List<Long> contentIds);
}
