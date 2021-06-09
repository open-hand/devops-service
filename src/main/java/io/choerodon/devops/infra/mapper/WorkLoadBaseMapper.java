package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

public interface WorkLoadBaseMapper {
    Integer selectCountByEnvIdAndName(@Param("envId") Long envId,
                                      @Param("name") String name);
}
