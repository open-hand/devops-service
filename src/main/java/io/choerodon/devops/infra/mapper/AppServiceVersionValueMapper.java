package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.AppServiceVersionValueDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

public interface AppServiceVersionValueMapper extends BaseMapper<AppServiceVersionValueDTO> {
    void deleteByIds(@Param("valueIds") Set<Long> valueIds);
}
