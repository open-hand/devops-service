package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.AccessTokenCheckResultVO;
import io.choerodon.devops.api.vo.AccessTokenVO;
import io.choerodon.devops.api.vo.AppServiceVersionAndValueVO;
import io.choerodon.devops.api.vo.AppServiceShareRuleVO;

/**
 * Created by ernst on 2018/5/12.
 */
public interface AppServiceShareRuleService {

    AppServiceShareRuleVO createOrUpdate(Long projectId, AppServiceShareRuleVO appServiceShareRuleVO);

    PageInfo<AppServiceShareRuleVO> pageByOptions(Long appServiceId, PageRequest pageRequest, String params);

    AppServiceShareRuleVO query(Long projectId, Long ruleId);

    AppServiceVersionAndValueVO getValuesAndChart(Long versionId);

    AccessTokenCheckResultVO checkToken(AccessTokenVO tokenDTO);

    void saveToken(AccessTokenVO tokenDTO);

}
