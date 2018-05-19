package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsProjectDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by younger on 2018/3/29.
 */
public interface DevopsProjectMapper extends BaseMapper<DevopsProjectDO> {

    DevopsProjectDO queryByGitlabGroupId(@Param("gitlabGroupId") Integer gitlabGroupId);
}
