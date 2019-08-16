package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.AppServiceLatestVersionDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Created by younger on 2018/3/28.
 */
public interface AppServiceVersionMapper extends Mapper<AppServiceVersionDTO> {

    List<AppServiceVersionDTO> listApplicationVersion(
            @Param("projectId") Long projectId,
            @Param("appServiceId") Long appServiceId,
            @Param("searchParam") Map<String, Object> searchParam,
            @Param("params") List<String> params,
            @Param("isProjectOwner") Boolean isProjectOwner,
            @Param("userId") Long userId);

    List<AppServiceVersionDTO> listByOptions(
            @Param("appServiceId") Long appServiceId,
            @Param("searchParam") Map<String, Object> searchParam,
            @Param("params") List<String> params);

    List<AppServiceLatestVersionDTO> listAppNewestVersion(@Param("projectId") Long projectId,
                                                          @Param("projectIds") List<Long> projectIds);

    List<AppServiceVersionDTO> listByAppIdAndEnvId(@Param("projectId") Long projectId,
                                                   @Param("appServiceId") Long appServiceId,
                                                   @Param("envId") Long envId);

    String queryValue(@Param("versionId") Long versionId);

    List<AppServiceVersionDTO> listByAppId(@Param("appServiceId") Long appServiceId);

    List<AppServiceVersionDTO> selectByAppIdAndParamWithPage(@Param("appServiceId") Long appServiceId,
                                                             @Param("isPublish") Boolean isPublish,
                                                             @Param("version") String searchParam);

    List<AppServiceVersionDTO> listAppDeployedVersion(@Param("projectId") Long projectId,
                                                      @Param("appServiceId") Long appServiceId);

    List<AppServiceVersionDTO> listByPublished(@Param("applicationId") Long applicationId);

    List<Long> listByAppIdAndVersionIds(@Param("applicationId") Long applicationId);

    List<AppServiceVersionDTO> listUpgradeVersion(@Param("appServiceServiceId") Long appServiceServiceId);

    Integer checkByProjectAndVersionId(@Param("projectId") Long projectId, @Param("appServiceServiceId") Long appServiceServiceId);

    AppServiceVersionDTO queryNewestVersion(@Param("appServiceId") Long appServiceId);

    List<AppServiceVersionDTO> listByAppVersionIds(@Param("appServiceServiceIds") List<Long> appServiceServiceIds);

    List<AppServiceVersionDTO> listByAppIdAndBranch(@Param("appServiceId") Long appServiceId, @Param("branch") String branch);

    String queryByPipelineId(@Param("pipelineId") Long pipelineId, @Param("branch") String branch, @Param("appServiceId") Long appServiceId);

    String queryValueByAppServiceId(@Param("appServiceId") Long appServiceId);

    void updateRepository(@Param("helmUrl") String url);

    AppServiceVersionDTO queryByCommitSha(@Param("appServiceId") Long appServiceId, @Param("ref") String ref, @Param("commit") String commit);


    void updateObjectVersionNumber(@Param("versionId") Long versionId);

    void updatePublishTime();

    List<AppServiceVersionDTO> listShareVersionByAppId(@Param("appServiceId") Long appServiceId,
                                                       @Param("params") List<String> params);

    /**
     * 查询所有应用服务最新的版本
     *
     * @return
     */
    AppServiceVersionDTO queryServiceVersionByAppServiceId(@Param("appServiceId") Long appServiceId, @Param("share") String share);
}
