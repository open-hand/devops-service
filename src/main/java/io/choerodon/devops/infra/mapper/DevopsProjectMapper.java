package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dataobject.DevopsProjectDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Created by younger on 2018/3/29.
 */
public interface DevopsProjectMapper extends Mapper<DevopsProjectDTO> {

    void updateObJectVersionNumber(@Param("iamProjectId") Long iamProjectId);

    DevopsProjectDTO queryByGitlabGroupId(@Param("gitlabGroupId") Integer gitlabGroupId);
}
