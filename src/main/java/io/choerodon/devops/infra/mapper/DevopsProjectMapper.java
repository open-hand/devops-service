package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Created by younger on 2018/3/29.
 */
public interface DevopsProjectMapper extends Mapper<DevopsProjectDTO> {

    void updateObjectVersionNumber(@Param("iamProjectId") Long iamProjectId);

    DevopsProjectDTO queryByGitlabGroupId(@Param("gitlabGroupId") Integer gitlabGroupId);
}
