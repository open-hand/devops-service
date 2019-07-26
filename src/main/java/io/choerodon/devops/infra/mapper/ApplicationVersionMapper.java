package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.ApplicationLatestVersionDTO;
import io.choerodon.devops.infra.dto.ApplicationVersionDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Created by younger on 2018/3/28.
 */
public interface ApplicationVersionMapper extends Mapper<ApplicationVersionDTO> {

    List<ApplicationVersionDTO> listApplicationVersion(
            @Param("projectId") Long projectId,
            @Param("appId") Long appId,
            @Param("searchParam") Map<String, Object> searchParam,
            @Param("param") String param,
            @Param("isProjectOwner") Boolean isProjectOwner,
            @Param("userId") Long userId);

    List<ApplicationLatestVersionDTO> listAppNewestVersion(@Param("projectId") Long projectId,
                                                           @Param("projectIds") List<Long> projectIds);

    List<ApplicationVersionDTO> listByAppIdAndEnvId(@Param("projectId") Long projectId,
                                                    @Param("appId") Long appId,
                                                    @Param("envId") Long envId);

    String queryValue(@Param("versionId") Long versionId);

    List<ApplicationVersionDTO> listByAppId(@Param("appId") Long appId,
                                            @Param("isPublish") Boolean isPublish);

    List<ApplicationVersionDTO> selectByAppIdAndParamWithPage(@Param("appId") Long appId,
                                                              @Param("isPublish") Boolean isPublish,
                                                              @Param("version") String searchParam);

    List<ApplicationVersionDTO> listAppDeployedVersion(@Param("projectId") Long projectId,
                                                       @Param("appId") Long appId);

    List<ApplicationVersionDTO> listApplicationVersionInApp(
            @Param("projectId") Long projectId,
            @Param("appId") Long appId,
            @Param("searchParam") Map<String, Object> searchParam,
            @Param("param") String param);

    List<ApplicationVersionDTO> listByPublished(@Param("applicationId") Long applicationId);

    List<Long> listByAppIdAndVersionIds(@Param("applicationId") Long applicationId);

    List<ApplicationVersionDTO> listUpgradeVersion(@Param("appVersionId") Long appVersionId);

    Integer checkByProjectAndVersionId(@Param("projectId") Long projectId, @Param("appVersionId") Long appVersionId);

    ApplicationVersionDTO queryNewestVersion(@Param("appId") Long appId);

    List<ApplicationVersionDTO> listByAppVersionIds(@Param("appVersionIds") List<Long> appVersionIds);

    List<ApplicationVersionDTO> listByAppIdAndBranch(@Param("appId") Long appId, @Param("branch") String branch);

    String queryByPipelineId(@Param("pipelineId") Long pipelineId, @Param("branch") String branch, @Param("appId") Long appId);

    String queryValueByAppId(@Param("appId") Long appId);

    void updateRepository(@Param("helmUrl") String url);

    ApplicationVersionDTO queryByCommitSha(@Param("appId") Long appId, @Param("ref") String ref, @Param("commit") String commit);


    void updateObJectVersionNumber(@Param("versionId") Long versionId);

    void updatePublishTime();

    List<ApplicationVersionDTO> listShareVersionByAppId(@Param("appId") Long appId,
                                                        @Param("params") String params);
}
