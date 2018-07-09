package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/9
 * Time: 14:31
 * Description:
 */
public interface DevopsMergeRequestMapper extends BaseMapper<DevopsMergeRequestDO> {

   Integer queryByAppIdAndGitlabId(@Param("applicationId") Long applicationId,
                                   @Param("gitlabMergeRequestId") Long gitlabMergeRequestId);
}
