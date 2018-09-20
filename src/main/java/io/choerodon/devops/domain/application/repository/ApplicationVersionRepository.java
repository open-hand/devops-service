package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;
import io.choerodon.devops.infra.dataobject.ApplicationLatestVersionDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/3.
 */
public interface ApplicationVersionRepository {

    Page<ApplicationVersionE> listApplicationVersion(Long projectId, Long appId, PageRequest pageRequest, String searchParam);

    List<ApplicationLatestVersionDO> listAppLatestVersion(Long projectId);

    ApplicationVersionE create(ApplicationVersionE applicationVersionE);

    List<ApplicationVersionE> listByAppId(Long appId, Boolean isPublish);

    List<ApplicationVersionE> listDeployedByAppId(Long projectId, Long appId);

    ApplicationVersionE query(Long appVersionId);

    List<ApplicationVersionE> listByAppIdAndEnvId(Long projectId, Long appId, Long envId);

    String queryValue(Long versionId);

    ApplicationVersionE queryByAppAndVersion(Long appId, String version);

    void updatePublishLevelByIds(List<Long> appVersionIds, Long level);

    Page<ApplicationVersionE> listApplicationVersionInApp(Long projectId,
                                                          Long appId,
                                                          PageRequest pageRequest,
                                                          String searchParam);

    List<ApplicationVersionE> listAllPublishedVersion(Long applicationId);

    Boolean checkAppAndVersion(Long appId, List<Long> appVersionIds);

    void setReadme(Long versionId, String readme);

    String getReadme(Long versionId);

    void updateVersion(ApplicationVersionE applicationVersionE);

    List<ApplicationVersionE> selectUpgradeVersions(Long appVersionId);

    void checkProIdAndVerId(Long projectId, Long appVersionId);

    ApplicationVersionE queryByCommitSha(String sha);
}
