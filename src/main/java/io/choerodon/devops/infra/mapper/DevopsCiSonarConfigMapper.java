package io.choerodon.devops.infra.mapper;

import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiSonarConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 14:21
 */
public interface DevopsCiSonarConfigMapper extends BaseMapper<DevopsCiSonarConfigDTO> {

    void batchDeleteByIds(@Param("ids") Set<Long> ids);
}
