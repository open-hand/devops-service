package io.choerodon.devops.infra.mapper;

import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiMavenPublishConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 18:02
 */
public interface DevopsCiMavenPublishConfigMapper extends BaseMapper<DevopsCiMavenPublishConfigDTO> {
    void batchDeleteByStepIds(@Param("stepIds") Set<Long> stepIds);
}
