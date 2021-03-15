package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.AppServiceShareRuleVO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by ernst on 2018/5/12.
 */
public interface AppServiceShareRuleService {

    AppServiceShareRuleVO createOrUpdate(Long projectId, AppServiceShareRuleVO appServiceShareRuleVO);

    Page<AppServiceShareRuleVO> pageByOptions(Long projectId, Long appServiceId, PageRequest pageable, String params);

    AppServiceShareRuleVO query(Long projectId, Long ruleId);

    void delete(Long projectId, Long ruleId);

    /**
     * 对于同组织下的版本和项目
     * 目标项目根据共享规则是否能使用这个版本部署(调用此方法前提是这个版本不是这个项目下的，但是是这个项目的组织下的)
     *
     * @param appServiceVersionDTO 待判断版本
     * @param targetProjectId      目标的项目id（要使用版本部署实例的项目）
     * @return true表示可以, false表示不行
     */
    boolean hasAccessByShareRule(AppServiceVersionDTO appServiceVersionDTO, Long targetProjectId);
}
