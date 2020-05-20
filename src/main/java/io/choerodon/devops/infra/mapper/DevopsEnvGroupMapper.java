package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsEnvGroupDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Creator: Runge
 * Date: 2018/9/4
 * Time: 14:17
 * Description:
 */
public interface DevopsEnvGroupMapper extends Mapper<DevopsEnvGroupDTO> {
    /**
     * 查出在集合中的环境组
     * @param ids
     * @return
     */
    List<DevopsEnvGroupDTO> listByIdList(@Param("ids") List<Long> ids);
}
