package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by younger on 2018/3/28.
 */
public interface AppServiceVersionMapper extends BaseMapper<AppServiceVersionDTO> {

    List<AppServiceVersionDTO> listByAppIdAndEnvId(@Param("projectId") Long projectId,
                                                   @Param("appServiceId") Long appServiceId,
                                                   @Param("envId") Long envId);

    String queryValue(@Param("versionId") Long versionId);

    List<AppServiceVersionDTO> listByAppServiceId(@Param("appServiceId") Long appServiceId, @Param("param") String param);

    List<AppServiceVersionDTO> listByAppServiceIdAndVersion(@Param("appServiceId") Long appServiceId,
                                                            @Param("appServiceVersionId") Long appServiceVersionId,
                                                            @Param("projectId") Long projectId,
                                                            @Param("share") Boolean share,
                                                            @Param("deployOnly") Boolean deployOnly,
                                                            @Param("searchParam") Map<String, String> map,
                                                            @Param("params") List<String> params,
                                                            @Param("index") String index,
                                                            @Param("version") String version);

    List<AppServiceVersionDTO> listAppServiceDeployedVersion(@Param("projectId") Long projectId,
                                                             @Param("appServiceId") Long appServiceId);

    List<AppServiceVersionDTO> listUpgradeVersion(@Param("appServiceServiceId") Long appServiceServiceId);

    Integer checkByProjectAndVersionId(@Param("projectId") Long projectId, @Param("appServiceServiceId") Long appServiceServiceId);

    List<AppServiceVersionDTO> listByAppServiceVersionIds(@Param("appServiceServiceIds") List<Long> appServiceServiceIds);

    AppServiceVersionDTO selectByAppServiceVersionId(@Param("appServiceVersionId") Long appServiceVersionId);

    List<AppServiceVersionDTO> listByAppServiceIdAndBranch(@Param("appServiceId") Long appServiceId, @Param("branch") String branch);

    String queryByPipelineId(@Param("pipelineId") Long pipelineId, @Param("branch") String branch, @Param("appServiceId") Long appServiceId);

    String queryValueByAppServiceId(@Param("appServiceId") Long appServiceId);

    List<AppServiceVersionDTO> queryByCommitSha(@Param("appServiceId") Long appServiceId, @Param("ref") String ref, @Param("commit") String commit);

    List<AppServiceVersionDTO> listShareVersionByAppId(@Param("appServiceId") Long appServiceId,
                                                       @Param("params") List<String> params);

    List<AppServiceVersionDTO> pageShareVersionByAppServiceIdAndVersion(@Param("appServiceId") Long appServiceId,
                                                                        @Param("version") String version);

    /**
     * 根据应用服务id集合和share 查询应用服务最新版本
     *
     * @return
     */
    List<AppServiceVersionDTO> listServiceVersionByAppServiceIds(@Param("ids") Set<Long> ids, @Param("share") String share, @Param("projectId") Long projectId, @Param("params") String params);

    /**
     * 查询应用服务共享的所有版本
     *
     * @param appServiceId
     * @param share
     * @return
     */
    List<AppServiceVersionDTO> queryServiceVersionByAppServiceIdAndShare(@Param("appServiceId") Long appServiceId, @Param("share") String share);

    List<AppServiceVersionDTO> listVersions(@Param("appServiceVersionIds") List<Long> appServiceVersionIds);

    void deleteByIds(@Param("versionIds") Set<Long> versionIds);

    AppServiceVersionDTO queryByShareVersion(@Param("appServiceId") Long appServiceId, @Param("projectId") Long projectId);

    AppServiceVersionDTO queryByCommitShaAndRef(@Param("appServiceId") Long appServiceId, @Param("commitSha") String commitSha, @Param("ref") String ref);

    List<AppServiceVersionDTO> listAllVersionsWithHelmConfig();

    Integer queryCountVersionsWithHelmConfig();

    /**
     * 临时方法，迁移应用服务版本使用，下一个版本可删除
     *
     * @return
     */
    Integer queryCountVersionsWithHarborConfig();

    List<AppServiceVersionDTO> listAllVersionsWithHarborConfig();

    Integer queryCountVersionsWithHelmConfigNullOrImageConfigNull();

    List<AppServiceVersionDTO> listAllVersionsWithHelmConfigNullOrImageConfigNull();
    AppServiceVersionDTO queryLatestByAppServiceIdVersionType(@Param("appServiceId") Long appServiceId, @Param("version") String version);
}
