package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.ApplicationMarketE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by ernst on 2018/5/12.
 */
public interface ApplicationMarketRepository {
    void create(ApplicationMarketE applicationMarketE);

    Page<ApplicationMarketE> listMarketAppsByProjectId(Long projectId, PageRequest pageRequest, String searchParam);

    Page<ApplicationMarketE> listMarketApps(List<Long> projectIds, PageRequest pageRequest, String searchParam);

    ApplicationMarketE getMarket(Long projectId, Long appMarketId);

    int updateImgUrl(ApplicationMarketE applicationMarketE);

    Boolean checkCanPub(Long appId);

    Long getMarketIdByAppId(Long appId);
}
