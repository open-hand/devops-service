package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.domain.application.entity.DevopsAppShareE;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketVersionDO;
import io.choerodon.devops.infra.dataobject.DevopsAppShareDO;

/**
 * Created by ernst on 2018/5/12.
 */
public interface AppShareRepository {
    DevopsAppShareE create(DevopsAppShareE applicationMarketE);

    PageInfo<DevopsAppShareE> listMarketAppsByProjectId(Long projectId, PageRequest pageRequest, String searchParam);

    PageInfo<DevopsAppShareE> listMarketAppsBySite(String publishLevel, PageRequest pageRequest, String searchParam);

    PageInfo<DevopsAppShareE> listMarketApps(List<Long> projectIds, PageRequest pageRequest, String searchParam);

    DevopsAppShareE getMarket(Long projectId, Long appMarketId);

    Boolean checkCanPub(Long appId);

    Long getMarketIdByAppId(Long appId);

    void checkProject(Long projectId, Long appMarketId);

    void checkDeployed(Long projectId, Long appMarketId, Long versionId, List<Long> projectIds);

    void unpublishApplication(Long appMarketId);

    void unpublishVersion(Long appMarketId, Long versionId);

    void updateVersion(Long appMarketId, Long versionId, Boolean isPublish);

    void update(DevopsAppShareDO devopsAppMarketDO);

    List<DevopsAppMarketVersionDO> getVersions(Long projectId, Long appMarketId, Boolean isPublish);

    PageInfo<DevopsAppMarketVersionDO> getVersions(Long projectId, Long appMarketId, Boolean isPublish,
                                                   PageRequest pageRequest, String searchParam);

    DevopsAppShareE queryByAppId(Long appId);

    void checkMarketVersion(Long appMarketId, Long versionId);

    PageInfo<DevopsAppShareE> queryByShareIds(PageRequest pageRequest, String params, List<Long> shareIds);

}
