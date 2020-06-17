package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.AppServiceShareRuleVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by ernst on 2018/5/12.
 */
public interface AppServiceShareRuleService {

    AppServiceShareRuleVO createOrUpdate(Long projectId, AppServiceShareRuleVO appServiceShareRuleVO);

    Page<AppServiceShareRuleVO> pageByOptions(Long projectId, Long appServiceId, PageRequest pageable, String params);

    AppServiceShareRuleVO query(Long projectId, Long ruleId);

    void delete(Long projectId, Long ruleId);

}
