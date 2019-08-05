package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.AppServiceShareRuleDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Created by ernst on 2018/5/12.
 */
public interface AppServiceShareRuleMapper extends Mapper<AppServiceShareRuleDTO> {

    List<AppServiceShareRuleDTO> listByOptions(@Param("projectId") Long projectId,
                                               @Param("searchParam") Map<String, Object> searchParam,
                                               @Param("param") String param);


    void updatePublishLevel();

    void deleteAll();
}
