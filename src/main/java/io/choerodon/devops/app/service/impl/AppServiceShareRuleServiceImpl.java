package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.AppServiceShareRuleVO;
import io.choerodon.devops.app.service.AppServiceShareRuleService;
import io.choerodon.devops.app.service.PermissionHelper;
import io.choerodon.devops.infra.dto.AppServiceShareRuleDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceShareRuleMapper;
import io.choerodon.devops.infra.util.CiCdPipelineUtils;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;


/**
 * Created by ernst on 2018/5/12.
 */
@Service
public class AppServiceShareRuleServiceImpl implements AppServiceShareRuleService {
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private AppServiceShareRuleMapper appServiceShareRuleMapper;
    @Autowired
    private PermissionHelper permissionHelper;

    private static final String PROJECT_NAME = "组织下所有项目";

    @Override
    @Transactional
    public AppServiceShareRuleVO createOrUpdate(Long projectId, AppServiceShareRuleVO appServiceShareRuleVO) {

        permissionHelper.checkAppServiceBelongToProject(projectId, appServiceShareRuleVO.getAppServiceId());

        AppServiceShareRuleDTO appServiceShareRuleDTO = ConvertUtils.convertObject(appServiceShareRuleVO, AppServiceShareRuleDTO.class);
        if (appServiceShareRuleDTO.getVersion() != null && appServiceShareRuleDTO.getVersionType() != null) {
            appServiceShareRuleDTO.setVersionType(null);
        }
        if (appServiceShareRuleDTO.getId() == null) {
            int count = appServiceShareRuleMapper.selectCount(appServiceShareRuleDTO);
            if (count > 0) {
                throw new CommonException("error.share.rule.already.exist");
            }
            if (appServiceShareRuleMapper.insert(appServiceShareRuleDTO) != 1) {
                throw new CommonException("error.insert.application.share.rule.insert");
            }
        } else {
            AppServiceShareRuleDTO oldappServiceShareRuleDTO = appServiceShareRuleMapper.selectByPrimaryKey(appServiceShareRuleDTO.getId());
            appServiceShareRuleDTO.setObjectVersionNumber(oldappServiceShareRuleDTO.getObjectVersionNumber());
            if (appServiceShareRuleMapper.updateByPrimaryKey(appServiceShareRuleDTO) != 1) {
                throw new CommonException("error.insert.application.share.rule.update");
            }
        }
        return ConvertUtils.convertObject(appServiceShareRuleDTO, AppServiceShareRuleVO.class);
    }

    @Override
    public Page<AppServiceShareRuleVO> pageByOptions(Long projectId, Long appServiceId, PageRequest pageable, String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        Page<AppServiceShareRuleDTO> devopsProjectConfigDTOPageInfo = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable),
                () -> appServiceShareRuleMapper.listByOptions(appServiceId,
                        TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(mapParams.get(TypeUtil.PARAMS))));
        Page<AppServiceShareRuleVO> shareRuleVOPageInfo = ConvertUtils.convertPage(devopsProjectConfigDTOPageInfo, AppServiceShareRuleVO.class);
        List<AppServiceShareRuleVO> appServiceShareRuleVOS = shareRuleVOPageInfo.getContent().stream().peek(t -> {
            if (t.getProjectId() != null) {
                ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(t.getProjectId());
                t.setProjectName(projectDTO.getName());
            }
        }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(appServiceShareRuleVOS)) {
            appServiceShareRuleVOS.forEach(appServiceShareRuleVO -> {
                String handleId = CiCdPipelineUtils.handleId(appServiceShareRuleVO.getId());
                appServiceShareRuleVO.setViewId(handleId);
            });
        }
        shareRuleVOPageInfo.setContent(appServiceShareRuleVOS);
        return shareRuleVOPageInfo;
    }

    @Override
    public AppServiceShareRuleVO query(Long projectId, Long ruleId) {
        AppServiceShareRuleVO appServiceShareRuleVO = ConvertUtils.convertObject(appServiceShareRuleMapper.selectByPrimaryKey(ruleId), AppServiceShareRuleVO.class);
        if (appServiceShareRuleVO.getProjectId() == null) {
            appServiceShareRuleVO.setProjectName(PROJECT_NAME);
        } else {
            appServiceShareRuleVO.setProjectName(baseServiceClientOperator.queryIamProjectById(appServiceShareRuleVO.getProjectId()).getName());
        }
        return appServiceShareRuleVO;
    }

    @Override
    public void delete(Long projectId, Long ruleId) {
        AppServiceShareRuleDTO appServiceShareRuleDTO = appServiceShareRuleMapper.selectByPrimaryKey(ruleId);
        permissionHelper.checkAppServiceBelongToProject(projectId, appServiceShareRuleDTO.getAppServiceId());
        appServiceShareRuleMapper.deleteByPrimaryKey(ruleId);
    }

    @Override
    public boolean hasAccessByShareRule(AppServiceVersionDTO appServiceVersionDTO, Long targetProjectId) {
        AppServiceShareRuleDTO condition = new AppServiceShareRuleDTO();
        condition.setAppServiceId(Objects.requireNonNull(appServiceVersionDTO.getAppServiceId()));
        // 查出这个服务所有的共享规则
        List<AppServiceShareRuleDTO> shareRules = appServiceShareRuleMapper.select(condition);

        if (!CollectionUtils.isEmpty(shareRules)) {
            for (AppServiceShareRuleDTO rule : shareRules) {
                if (ResourceLevel.PROJECT.value().equals(rule.getShareLevel())
                        && targetProjectId.equals(rule.getProjectId())
                        && versionMatchRule(appServiceVersionDTO.getVersion(), rule.getVersion(), rule.getVersionType())) {
                    return true;
                } else {
                    // 如果是组织下共享, 且共享的规则能够匹配上
                    if (versionMatchRule(appServiceVersionDTO.getVersion(), rule.getVersion(), rule.getVersionType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 版本是否符合发布规则
     *
     * @param targetVersion    待判断的版本
     * @param shareVersion     特定版本
     * @param shareVersionType 版本类型
     * @return true表示符合
     */
    private boolean versionMatchRule(String targetVersion, @Nullable String shareVersion, @Nullable String shareVersionType) {
        if (shareVersion != null
                && shareVersion.equals(targetVersion)) {
            return true;
        } else {
            return shareVersionType != null && targetVersion.contains(shareVersionType);
        }
    }
}
