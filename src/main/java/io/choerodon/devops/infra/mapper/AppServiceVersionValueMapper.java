package io.choerodon.devops.infra.mapper;

import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.AppServiceVersionValueDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface AppServiceVersionValueMapper extends BaseMapper<AppServiceVersionValueDTO> {
    void deleteByIds(@Param("valueIds") Set<Long> valueIds);
}
