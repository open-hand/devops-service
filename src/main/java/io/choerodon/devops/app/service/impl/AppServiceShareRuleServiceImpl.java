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
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.AppServiceShareRuleVO;
import io.choerodon.devops.app.service.AppServiceShareRuleService;
import io.choerodon.devops.app.service.PermissionHelper;
import io.choerodon.devops.infra.dto.AppServiceShareRuleDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceShareRuleMapper;
import io.choerodon.devops.infra.util.*;
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

        // 如果选了特定版本，那么版本类型就不生效了
        if (appServiceShareRuleDTO.getVersion() != null && appServiceShareRuleDTO.getVersionType() != null) {
            appServiceShareRuleDTO.setVersionType(null);
        }
        if (ResourceLevel.ORGANIZATION.value().equals(appServiceShareRuleDTO.getShareLevel())) {
            // 如果是组织层的共享规则，不需要目标项目id
            appServiceShareRuleDTO.setProjectId(null);
        }
        // 如果目标项目id不为空，则共享层级为项目层
        if (appServiceShareRuleDTO.getProjectId() != null) {
            appServiceShareRuleDTO.setShareLevel(ResourceLevel.PROJECT.value());
        }

        // 新建
        if (appServiceShareRuleDTO.getId() == null) {
            checkExist(appServiceShareRuleDTO);
            MapperUtil.resultJudgedInsert(appServiceShareRuleMapper, appServiceShareRuleDTO, "error.insert.application.share.rule.insert");
        } else {
            // 更新
            AppServiceShareRuleDTO oldAppServiceShareRuleDTO = appServiceShareRuleMapper.selectByPrimaryKey(appServiceShareRuleDTO.getId());
            CommonExAssertUtil.assertNotNull(oldAppServiceShareRuleDTO, "error.share.rule.id.not.exist");
            // 不相等才需要更新
            if (!ruleEquals(appServiceShareRuleDTO, oldAppServiceShareRuleDTO)) {
                checkExist(appServiceShareRuleDTO);
                oldAppServiceShareRuleDTO.setVersion(appServiceShareRuleDTO.getVersion());
                oldAppServiceShareRuleDTO.setVersionType(appServiceShareRuleDTO.getVersionType());
                oldAppServiceShareRuleDTO.setShareLevel(appServiceShareRuleDTO.getShareLevel());
                oldAppServiceShareRuleDTO.setProjectId(appServiceShareRuleDTO.getProjectId());
                MapperUtil.resultJudgedUpdateByPrimaryKeySelective(appServiceShareRuleMapper, oldAppServiceShareRuleDTO, "error.insert.application.share.rule.update");
            }
        }
        return ConvertUtils.convertObject(appServiceShareRuleDTO, AppServiceShareRuleVO.class);
    }

    /**
     * 如果存在，抛异常
     *
     * @param appServiceShareRuleDTO 数据
     */
    private void checkExist(AppServiceShareRuleDTO appServiceShareRuleDTO) {
        CommonExAssertUtil.assertTrue(appServiceShareRuleMapper.selectCount(appServiceShareRuleDTO) == 0, "error.share.rule.already.exist");
    }

    /**
     * 对比两个共享规则是否一样
     *
     * @param one      一个
     * @param theOther 另一个
     * @return true 表示相等
     */
    private boolean ruleEquals(AppServiceShareRuleDTO one, AppServiceShareRuleDTO theOther) {
        if (one == null || theOther == null) {
            return false;
        }
        if (one == theOther) {
            return true;
        }
        return Objects.equals(one.getId(), theOther.getId())
                && Objects.equals(one.getAppServiceId(), theOther.getAppServiceId())
                && Objects.equals(one.getShareLevel(), theOther.getShareLevel())
                && Objects.equals(one.getProjectId(), theOther.getProjectId())
                && Objects.equals(one.getVersion(), theOther.getVersion())
                && Objects.equals(one.getVersionType(), theOther.getVersionType());
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
