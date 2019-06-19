package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/9
 * Time: 14:31
 * Description:
 */
public interface DevopsMergeRequestMapper extends Mapper<DevopsMergeRequestDO> {

    Integer queryByAppIdAndGitlabId(@Param("projectId") Long applicationId,
                                    @Param("gitlabMergeRequestId") Long gitlabMergeRequestId);

    List<DevopsMergeRequestDO> getByProjectIdAndState(@Param("projectId") Integer gitLabProjectId,
                                                      @Param("state") String state);

    List<DevopsMergeRequestDO> listByProjectIdAndBranch(@Param("projectId")Integer gitLabProjectId, @Param("branchName") String branchName);
}
