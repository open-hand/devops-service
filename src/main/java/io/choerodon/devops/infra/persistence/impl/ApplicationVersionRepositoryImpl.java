package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.repository.ApplicationVersionRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.infra.common.util.PageRequestUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.ApplicationLatestVersionDO;
import io.choerodon.devops.infra.dataobject.ApplicationVersionDO;
import io.choerodon.devops.infra.dataobject.ApplicationVersionReadmeDO;
import io.choerodon.devops.infra.mapper.ApplicationVersionMapper;
import io.choerodon.devops.infra.mapper.ApplicationVersionReadmeMapper;

/**
 * Created by Zenger on 2018/4/3.
 */
@Service
public class ApplicationVersionRepositoryImpl implements ApplicationVersionRepository {

    private static final String APP_CODE = "appCode";
    private static final String APP_NAME = "appName";
    private static JSON json = new JSON();

    @Autowired
    private ApplicationVersionMapper applicationVersionMapper;
    @Autowired
    private ApplicationVersionReadmeMapper applicationVersionReadmeMapper;
    @Autowired
    private IamRepository iamRepository;

    @Override
    public List<ApplicationLatestVersionDO> listAppLatestVersion(Long projectId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Long organizationId = projectE.getOrganization().getId();
        List<ProjectE> projectEList = iamRepository.listIamProjectByOrgId(organizationId, null, null);
        List<Long> projectIds = projectEList.stream().map(ProjectE::getId)
                .collect(Collectors.toCollection(ArrayList::new));
        return applicationVersionMapper.listAppLatestVersion(projectId, projectIds);
    }

    @Override
    public ApplicationVersionE create(ApplicationVersionE applicationVersionE) {
        ApplicationVersionDO applicationVersionDO =
                ConvertHelper.convert(applicationVersionE, ApplicationVersionDO.class);
        applicationVersionDO.setReadmeValueId(setReadme(applicationVersionE.getApplicationVersionReadmeV().getReadme()));
        if (applicationVersionMapper.insert(applicationVersionDO) != 1) {
            throw new CommonException("error.version.insert");
        }
        return ConvertHelper.convert(applicationVersionMapper.selectOne(applicationVersionDO), ApplicationVersionE.class);
    }

    @Override
    public List<ApplicationVersionE> listByAppId(Long appId, Boolean isPublish) {
        List<ApplicationVersionDO> applicationVersionDOS = applicationVersionMapper.selectByAppId(appId, isPublish);
        if (applicationVersionDOS.isEmpty()) {
            return Collections.emptyList();
        }
        return ConvertHelper.convertList(applicationVersionDOS, ApplicationVersionE.class);
    }

    @Override
    public PageInfo<ApplicationVersionE> listByAppIdAndParamWithPage(Long appId, Boolean isPublish, Long appVersionId, PageRequest pageRequest, String searchParam) {
        PageInfo<ApplicationVersionDO> applicationVersionDOPage;
        applicationVersionDOPage = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
                () -> applicationVersionMapper.selectByAppIdAndParamWithPage(appId, isPublish, searchParam));
        if (appVersionId != null) {
            ApplicationVersionDO versionDO = new ApplicationVersionDO();
            versionDO.setId(appVersionId);
            ApplicationVersionDO searchDO = applicationVersionMapper.selectByPrimaryKey(versionDO);
            applicationVersionDOPage.getList().removeIf(v -> v.getId().equals(appVersionId));
            applicationVersionDOPage.getList().add(0, searchDO);
        }
        if (applicationVersionDOPage.getList().isEmpty()) {
            return new PageInfo<>();
        }
        return ConvertPageHelper.convertPageInfo(applicationVersionDOPage, ApplicationVersionE.class);
    }

    @Override
    public List<ApplicationVersionE> listDeployedByAppId(Long projectId, Long appId) {
        List<ApplicationVersionDO> applicationVersionDOS =
                applicationVersionMapper.selectDeployedByAppId(projectId, appId);
        if (applicationVersionDOS.isEmpty()) {
            return Collections.emptyList();
        }
        return ConvertHelper.convertList(applicationVersionDOS, ApplicationVersionE.class);
    }

    @Override
    public ApplicationVersionE query(Long appVersionId) {
        return ConvertHelper.convert(
                applicationVersionMapper.selectByPrimaryKey(appVersionId), ApplicationVersionE.class);
    }

    @Override
    public List<ApplicationVersionE> listByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return ConvertHelper.convertList(
                applicationVersionMapper.listByAppIdAndEnvId(projectId, appId, envId), ApplicationVersionE.class);
    }

    @Override
    public String queryValue(Long versionId) {
        return applicationVersionMapper.queryValue(versionId);
    }

    @Override
    public ApplicationVersionE queryByAppAndVersion(Long appId, String version) {
        ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO();
        applicationVersionDO.setAppId(appId);
        applicationVersionDO.setVersion(version);
        List<ApplicationVersionDO> applicationVersionDOS = applicationVersionMapper.select(applicationVersionDO);
        if (applicationVersionDOS.isEmpty()) {
            return null;
        }
        return ConvertHelper.convert(applicationVersionDOS.get(0), ApplicationVersionE.class);
    }

    @Override
    public void updatePublishLevelByIds(List<Long> appVersionIds, Long level) {
        ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO();
        applicationVersionDO.setIsPublish(level);
        for (Long id : appVersionIds) {
            applicationVersionDO.setId(id);
            applicationVersionDO.setObjectVersionNumber(applicationVersionMapper.selectByPrimaryKey(id).getObjectVersionNumber());
            applicationVersionMapper.updateByPrimaryKeySelective(applicationVersionDO);
        }
    }

    @Override
    public PageInfo<ApplicationVersionE> listApplicationVersionInApp(Long projectId, Long appId, PageRequest pageRequest,
                                                                     String searchParam, Boolean isProjectOwner,
                                                                     Long userId) {
        Sort sort = pageRequest.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if (property.equals("version")) {
                            property = "dav.version";
                        } else if (property.equals("creationDate")) {
                            property = "dav.creation_date";
                        }
                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }

        PageInfo<ApplicationVersionDO> applicationVersionQueryDOPage;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            applicationVersionQueryDOPage = PageHelper
                    .startPage(pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> applicationVersionMapper.listApplicationVersion(projectId, appId,
                            TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                            TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM)), isProjectOwner, userId));
        } else {
            applicationVersionQueryDOPage = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> applicationVersionMapper
                    .listApplicationVersion(projectId, appId, null, null, isProjectOwner, userId));
        }
        return ConvertPageHelper.convertPageInfo(applicationVersionQueryDOPage, ApplicationVersionE.class);
    }

    @Override
    public List<ApplicationVersionE> listAllPublishedVersion(Long applicationId) {
        List<ApplicationVersionDO> applicationVersionDOList = applicationVersionMapper
                .getAllPublishedVersion(applicationId);
        return ConvertHelper.convertList(applicationVersionDOList, ApplicationVersionE.class);
    }

    @Override
    public Boolean checkAppAndVersion(Long appId, List<Long> appVersionIds) {
        if (appId == null || appVersionIds.isEmpty()) {
            throw new CommonException("error.app.version.check");
        }
        List<Long> versionList = applicationVersionMapper.selectVersionsByAppId(appId);
        if (appVersionIds.stream().anyMatch(t -> !versionList.contains(t))) {
            throw new CommonException("error.app.version.check");
        }
        return true;
    }

    @Override
    public Long setReadme(String readme) {
        ApplicationVersionReadmeDO applicationVersionReadmeDO = new ApplicationVersionReadmeDO(readme);
        applicationVersionReadmeMapper.insert(applicationVersionReadmeDO);
        return applicationVersionReadmeDO.getId();
    }

    @Override
    public String getReadme(Long readmeValueId) {
        String readme;
        try {
            readme = applicationVersionReadmeMapper.selectByPrimaryKey(readmeValueId).getReadme();
        } catch (Exception ignore) {
            readme = "# 暂无";
        }
        return readme;
    }

    @Override
    public void updateVersion(ApplicationVersionE applicationVersionE) {
        ApplicationVersionDO applicationVersionDO =
                ConvertHelper.convert(applicationVersionE, ApplicationVersionDO.class);
        if (applicationVersionMapper.updateByPrimaryKey(applicationVersionDO) != 1) {
            throw new CommonException("error.version.update");
        }
        updateReadme(applicationVersionMapper.selectByPrimaryKey(applicationVersionE.getId()).getReadmeValueId(), applicationVersionE.getApplicationVersionReadmeV().getReadme());
    }

    private void updateReadme(Long readmeValueId, String readme) {
        ApplicationVersionReadmeDO readmeDO;
        try {

            readmeDO = applicationVersionReadmeMapper.selectByPrimaryKey(readmeValueId);
            readmeDO.setReadme(readme);
            applicationVersionReadmeMapper.updateByPrimaryKey(readmeDO);
        } catch (Exception e) {
            readmeDO = new ApplicationVersionReadmeDO(readme);
            applicationVersionReadmeMapper.insert(readmeDO);
        }
    }

    @Override
    public List<ApplicationVersionE> selectUpgradeVersions(Long appVersionId) {
        return ConvertHelper.convertList(
                applicationVersionMapper.selectUpgradeVersions(appVersionId), ApplicationVersionE.class);
    }

    @Override
    public void checkProIdAndVerId(Long projectId, Long appVersionId) {
        Integer index = applicationVersionMapper.checkProIdAndVerId(projectId, appVersionId);
        if (index == 0) {
            throw new CommonException("error.project.AppVersion.notExist");
        }
    }

    @Override
    public ApplicationVersionE queryByCommitSha(Long appId, String ref, String sha) {
        return ConvertHelper.convert(applicationVersionMapper.queryByCommitSha(appId, ref, sha), ApplicationVersionE.class);
    }

    @Override
    public ApplicationVersionE getLatestVersion(Long appId) {
        return ConvertHelper.convert(applicationVersionMapper.getLatestVersion(appId), ApplicationVersionE.class);
    }

    @Override
    public List<ApplicationVersionE> listByAppVersionIds(List<Long> appVersionIds) {
        return ConvertHelper.convertList(applicationVersionMapper.listByAppVersionIds(appVersionIds), ApplicationVersionE.class);
    }

    @Override
    public List<ApplicationVersionE> listByAppIdAndBranch(Long appId, String branch) {
        return ConvertHelper.convertList(applicationVersionMapper.listByAppIdAndBranch(appId, branch), ApplicationVersionE.class);
    }

    @Override
    public String queryByPipelineId(Long pipelineId, String branch, Long appId) {
        return applicationVersionMapper.queryByPipelineId(pipelineId, branch, appId);
    }

    @Override
    public String queryValueById(Long appId) {
        return applicationVersionMapper.queryValueById(appId);
    }

    @Override
    public ApplicationVersionE queryByAppAndCode(Long appId, String appVersion) {
        ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO();
        applicationVersionDO.setAppId(appId);
        applicationVersionDO.setVersion(appVersion);
        return ConvertHelper.convert(applicationVersionMapper.selectOne(applicationVersionDO), ApplicationVersionE.class);
    }
}
