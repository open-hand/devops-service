package io.choerodon.devops.infra.mapper;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsProjectDO;

/**
 * Created by younger on 2018/3/29.
 */
public interface DevopsProjectMapper extends Mapper<DevopsProjectDO> {

    DevopsProjectDO queryByGitlabGroupId(@Param("gitlabGroupId") Integer gitlabGroupId);


    void updateObJectVersionNumber(@Param("iamProjectId") Long iamProjectId);

}
