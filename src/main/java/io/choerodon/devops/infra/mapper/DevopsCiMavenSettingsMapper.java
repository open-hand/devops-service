package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiMavenSettingsDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author zmf
 * @since 20-4-16
 */
public interface DevopsCiMavenSettingsMapper extends BaseMapper<DevopsCiMavenSettingsDTO> {
    String queryMavenSettings(@Param("jobId") Long jobId, @Param("sequence") Long sequence);

    /**
     * 根据job id列表批量删除纪录
     *
     * @param jobIds 猪齿鱼job id
     */
    void deleteByJobIds(@Param("jobIds") List<Long> jobIds);
}
