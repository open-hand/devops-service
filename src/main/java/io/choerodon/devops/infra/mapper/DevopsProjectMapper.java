package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Created by younger on 2018/3/29.
 */
public interface DevopsProjectMapper extends Mapper<DevopsProjectDTO> {

    void updateObjectVersionNumber(@Param("iamProjectId") Long iamProjectId);

    DevopsProjectDTO queryByGitlabGroupId(@Param("gitlabGroupId") Integer gitlabGroupId);

    Long queryAppIdByProjectId(@Param("iamProjectId") Long iamProjectId);

    /**
     * 0.19版本前修复表中appId的值，可以在0.20版本删除
     */
    void fixAppIdValue();
}
