package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.GitlabProjectSimple;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by younger on 2018/3/29.
 */
public interface DevopsProjectMapper extends BaseMapper<DevopsProjectDTO> {

    void updateObjectVersionNumber(@Param("iamProjectId") Long iamProjectId);

    DevopsProjectDTO queryByGitlabGroupId(@Param("gitlabGroupId") Integer gitlabGroupId);

    List<GitlabProjectSimple> selectByProjectIds(@Param("projectIds") List<Long> projectIds);
}
