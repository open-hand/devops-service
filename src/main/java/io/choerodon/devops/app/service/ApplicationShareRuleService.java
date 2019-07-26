package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.AccessTokenCheckResultVO;
import io.choerodon.devops.api.vo.AccessTokenVO;
import io.choerodon.devops.api.vo.AppVersionAndValueVO;
import io.choerodon.devops.api.vo.ApplicationShareRuleVO;
import io.choerodon.devops.infra.dto.ApplicationShareRuleDTO;

/**
 * Created by ernst on 2018/5/12.
 */
public interface ApplicationShareRuleService {

    ApplicationShareRuleVO createOrUpdate(Long projectId, ApplicationShareRuleVO applicationShareRuleVO);

    PageInfo<ApplicationShareRuleVO> pageByOptions(Long projectId, PageRequest pageRequest, String params);

    ApplicationShareRuleVO query(Long projectId, Long ruleId);

    AppVersionAndValueVO getValuesAndChart(Long versionId);

    AccessTokenCheckResultVO checkToken(AccessTokenVO tokenDTO);

    void saveToken(AccessTokenVO tokenDTO);

}
