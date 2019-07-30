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
            @Param("appServiceId") Long appServiceId,
            @Param("searchParam") Map<String, Object> searchParam,
            @Param("param") String param,
            @Param("isProjectOwner") Boolean isProjectOwner,
            @Param("userId") Long userId);

    List<ApplicationLatestVersionDTO> listAppNewestVersion(@Param("projectId") Long projectId,
                                                           @Param("projectIds") List<Long> projectIds);

    List<ApplicationVersionDTO> listByAppIdAndEnvId(@Param("projectId") Long projectId,
                                                    @Param("appServiceId") Long appServiceId,
                                                    @Param("envId") Long envId);

    String queryValue(@Param("versionId") Long versionId);

    List<ApplicationVersionDTO> listByAppId(@Param("appServiceId") Long appServiceId,
                                            @Param("isPublish") Boolean isPublish);

    List<ApplicationVersionDTO> selectByAppIdAndParamWithPage(@Param("appServiceId") Long appServiceId,
                                                              @Param("isPublish") Boolean isPublish,
                                                              @Param("version") String searchParam);

    List<ApplicationVersionDTO> listAppDeployedVersion(@Param("projectId") Long projectId,
                                                       @Param("appServiceId") Long appServiceId);

    List<ApplicationVersionDTO> listApplicationVersionInApp(
            @Param("projectId") Long projectId,
            @Param("appServiceId") Long appServiceId,
            @Param("searchParam") Map<String, Object> searchParam,
            @Param("param") String param);

    List<ApplicationVersionDTO> listByPublished(@Param("applicationId") Long applicationId);

    List<Long> listByAppIdAndVersionIds(@Param("applicationId") Long applicationId);

    List<ApplicationVersionDTO> listUpgradeVersion(@Param("appVersionId") Long appVersionId);

    Integer checkByProjectAndVersionId(@Param("projectId") Long projectId, @Param("appVersionId") Long appVersionId);

    ApplicationVersionDTO queryNewestVersion(@Param("appServiceId") Long appServiceId);

    List<ApplicationVersionDTO> listByAppVersionIds(@Param("appVersionIds") List<Long> appVersionIds);

    List<ApplicationVersionDTO> listByAppIdAndBranch(@Param("appServiceId") Long appServiceId, @Param("branch") String branch);

    String queryByPipelineId(@Param("pipelineId") Long pipelineId, @Param("branch") String branch, @Param("appServiceId") Long appServiceId);

    String queryValueByAppId(@Param("appServiceId") Long appServiceId);

    void updateRepository(@Param("helmUrl") String url);

    ApplicationVersionDTO queryByCommitSha(@Param("appServiceId") Long appServiceId, @Param("ref") String ref, @Param("commit") String commit);


    void updateObJectVersionNumber(@Param("versionId") Long versionId);

    void updatePublishTime();

    List<ApplicationVersionDTO> listShareVersionByAppId(@Param("appServiceId") Long appServiceId,
                                                        @Param("params") String params);
}
