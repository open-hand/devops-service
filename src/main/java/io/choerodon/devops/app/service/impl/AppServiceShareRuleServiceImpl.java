package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.kubernetes.client.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.AppServiceShareRuleVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.AppServiceShareRuleDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceShareRuleMapper;
import io.choerodon.devops.infra.mapper.AppServiceVersionReadmeMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;


/**
 * Created by ernst on 2018/5/12.
 */
@Service
public class AppServiceShareRuleServiceImpl implements AppServiceShareRuleService {

    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${services.helm.url}")
    private String helmUrl;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private AppServiceShareRuleMapper appServiceShareRuleMapper;

    @Override
    @Transactional
    public AppServiceShareRuleVO createOrUpdate(Long projectId, AppServiceShareRuleVO appServiceShareRuleVO) {
        AppServiceShareRuleDTO appServiceShareRuleDTO = ConvertUtils.convertObject(appServiceShareRuleVO, AppServiceShareRuleDTO.class);
        if (appServiceShareRuleDTO.getId() == null) {
            if (appServiceShareRuleMapper.insert(appServiceShareRuleDTO) != 1) {
                throw new CommonException("error.insert.application.share.rule.insert");
            }
        } else {
            if (appServiceShareRuleMapper.updateByPrimaryKeySelective(appServiceShareRuleDTO) != 1) {
                throw new CommonException("error.insert.application.share.rule.update");
            }
        }
        return ConvertUtils.convertObject(appServiceShareRuleDTO, AppServiceShareRuleVO.class);
    }

    @Override
    public PageInfo<AppServiceShareRuleVO> pageByOptions(Long projectId, Long appServiceId, PageRequest pageRequest, String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        PageInfo<AppServiceShareRuleDTO> devopsProjectConfigDTOPageInfo = PageHelper.startPage(
                pageRequest.getPage(),
                pageRequest.getSize(),
                PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
                () -> appServiceShareRuleMapper.listByOptions(appServiceId,
                        TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)),
                        TypeUtil.cast(mapParams.get(TypeUtil.PARAMS))));
        PageInfo<AppServiceShareRuleVO> shareRuleVOPageInfo = ConvertUtils.convertPage(devopsProjectConfigDTOPageInfo, AppServiceShareRuleVO.class);
        List<AppServiceShareRuleVO> appServiceShareRuleVOS = shareRuleVOPageInfo.getList().stream().peek(t -> {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(t.getProjectId());
            t.setProjectName(projectDTO.getName());
            t.setAppName(projectDTO.getApplicationName());
        }).collect(Collectors.toList());
        shareRuleVOPageInfo.setList(appServiceShareRuleVOS);
        return shareRuleVOPageInfo;
    }

    @Override
    public AppServiceShareRuleVO query(Long projectId, Long ruleId) {
        AppServiceShareRuleVO appServiceShareRuleVO = ConvertUtils.convertObject(appServiceShareRuleMapper.selectByPrimaryKey(ruleId), AppServiceShareRuleVO.class);
        appServiceShareRuleVO.setProjectName(baseServiceClientOperator.queryIamProjectById(appServiceShareRuleVO.getProjectId()).getName());
        return appServiceShareRuleVO;
    }

}
