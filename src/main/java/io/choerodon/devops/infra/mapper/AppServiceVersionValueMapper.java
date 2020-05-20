package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.AppServiceVersionValueDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

public interface AppServiceVersionValueMapper extends Mapper<AppServiceVersionValueDTO> {
    void deleteByIds(@Param("valueIds") Set<Long> valueIds);
}
