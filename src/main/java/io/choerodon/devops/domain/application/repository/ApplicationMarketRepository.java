package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.domain.application.entity.ApplicationMarketE;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketDO;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketVersionDO;

/**
 * Created by ernst on 2018/5/12.
 */
public interface ApplicationMarketRepository {
    void create(ApplicationMarketE applicationMarketE);

    PageInfo<ApplicationMarketE> listMarketAppsByProjectId(Long projectId, PageRequest pageRequest, String searchParam);

    PageInfo<ApplicationMarketE> listMarketApps(List<Long> projectIds, PageRequest pageRequest, String searchParam);

    ApplicationMarketE getMarket(Long projectId, Long appMarketId);

    Boolean checkCanPub(Long appId);

    Long getMarketIdByAppId(Long appId);

    void checkProject(Long projectId, Long appMarketId);

    void checkDeployed(Long projectId, Long appMarketId, Long versionId, List<Long> projectIds);

    void unpublishApplication(Long appMarketId);

    void unpublishVersion(Long appMarketId, Long versionId);

    void updateVersion(Long appMarketId, Long versionId, Boolean isPublish);

    void update(DevopsAppMarketDO devopsAppMarketDO);

    List<DevopsAppMarketVersionDO> getVersions(Long projectId, Long appMarketId, Boolean isPublish);

    PageInfo<DevopsAppMarketVersionDO> getVersions(Long projectId, Long appMarketId, Boolean isPublish,
                                                   PageRequest pageRequest, String searchParam);

    ApplicationMarketE queryByAppId(Long appId);

    void checkMarketVersion(Long appMarketId, Long versionId);

}
