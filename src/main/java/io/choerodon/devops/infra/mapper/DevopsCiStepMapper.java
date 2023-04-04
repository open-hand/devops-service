package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 14:20
 */
public interface DevopsCiStepMapper extends BaseMapper<DevopsCiStepDTO> {

    List<DevopsCiStepDTO> listByJobIds(@Param("jobIds") List<Long> jobIds);

    void batchDeleteByIds(@Param("ids") Set<Long> ids);

    Long queryAppServiceIdByStepId(Long id);


    void updateSonarScanner(@Param("script") String script);

    void updateSonarMaven(@Param("script") String script);

}
