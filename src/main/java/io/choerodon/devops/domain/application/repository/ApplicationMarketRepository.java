package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.ApplicationMarketE;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketDO;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketVersionDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by ernst on 2018/5/12.
 */
public interface ApplicationMarketRepository {
    void create(ApplicationMarketE applicationMarketE);

    Page<ApplicationMarketE> listMarketAppsByProjectId(Long projectId, PageRequest pageRequest, String searchParam);

    Page<ApplicationMarketE> listMarketApps(List<Long> projectIds, PageRequest pageRequest, String searchParam);

    ApplicationMarketE getMarket(Long projectId, Long appMarketId, List<Long> projectIds);

    Boolean checkCanPub(Long appId);

    Long getMarketIdByAppId(Long appId);

    void checkProject(Long projectId, Long appMarketId);

    void checkDeployed(Long projectId, Long appMarketId, Long versionId, List<Long> projectIds);

    void unpublishApplication(Long appMarketId);

    void unpublishVersion(Long appMarketId, Long versionId);

    void update(DevopsAppMarketDO devopsAppMarketDO);

    List<DevopsAppMarketVersionDO> getVersions(Long projectId, Long appMarketId);
}
