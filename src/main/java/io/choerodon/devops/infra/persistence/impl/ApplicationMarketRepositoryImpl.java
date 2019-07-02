package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
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
import io.choerodon.devops.domain.application.entity.DevopsAppShareE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.repository.AppShareRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.infra.common.util.PageRequestUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketVersionDO;
import io.choerodon.devops.infra.dataobject.DevopsAppShareDO;
import io.choerodon.devops.infra.mapper.ApplicationMarketMapper;

/**
 * Created by ernst on 2018/3/28.
 */
@Service
public class ApplicationMarketRepositoryImpl implements AppShareRepository {

    private JSON json = new JSON();

    private ApplicationMarketMapper applicationMarketMapper;
    @Autowired
    private IamRepository iamRepository;

    public ApplicationMarketRepositoryImpl(ApplicationMarketMapper applicationMarketMapper) {
        this.applicationMarketMapper = applicationMarketMapper;
    }


    @Override
    public DevopsAppShareE create(DevopsAppShareE applicationMarketE) {
        DevopsAppShareDO devopsAppShareDO = ConvertHelper.convert(applicationMarketE, DevopsAppShareDO.class);
        applicationMarketMapper.insert(devopsAppShareDO);
        return ConvertHelper.convert(devopsAppShareDO, DevopsAppShareE.class);
    }

    @Override
    public PageInfo<DevopsAppShareE> listMarketAppsByProjectId(Long projectId, PageRequest pageRequest, String searchParam) {
        PageInfo<DevopsAppShareDO> applicationMarketQueryDOPage;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            applicationMarketQueryDOPage = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMarketMapper.listMarketApplicationInProject(
                    projectId,
                    TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM))));
        } else {
            applicationMarketQueryDOPage = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMarketMapper.listMarketApplicationInProject(projectId, null, null));
        }
        return ConvertPageHelper.convertPageInfo(applicationMarketQueryDOPage, DevopsAppShareE.class);
    }

    @Override
    public PageInfo<DevopsAppShareE> listMarketAppsBySite(String publishLevel, PageRequest pageRequest, String searchParam) {

        Map<String, Object> mapParams = TypeUtil.castMapParams(searchParam);

        PageInfo<DevopsAppShareDO> appShareDOPageInfo = PageHelper
                .startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                        applicationMarketMapper.listMarketAppsBySite(publishLevel, (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM), (String) mapParams.get(TypeUtil.PARAM)));
        return ConvertPageHelper.convertPageInfo(appShareDOPageInfo, DevopsAppShareE.class);
    }

    @Override
    public PageInfo<DevopsAppShareE> listMarketApps(List<Long> projectIds, PageRequest pageRequest, String searchParam) {
        PageInfo<DevopsAppShareDO> applicationMarketQueryDOPage;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            applicationMarketQueryDOPage = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMarketMapper.listMarketApplication(
                    projectIds,
                    TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM))));
        } else {
            applicationMarketQueryDOPage = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMarketMapper.listMarketApplication(projectIds, null, null));
        }
        return ConvertPageHelper.convertPageInfo(applicationMarketQueryDOPage, DevopsAppShareE.class);
    }

    @Override
    public DevopsAppShareE getMarket(Long projectId, Long appMarketId) {
        List<Long> projectIds = getProjectIds(projectId);
        return ConvertHelper.convert(
                applicationMarketMapper.getMarketApplication(projectId, appMarketId, projectIds),
                DevopsAppShareE.class);
    }

    @Override
    public Boolean checkCanPub(Long appId) {

        int selectCount = applicationMarketMapper.selectCountByAppId(appId);
        if (selectCount > 0) {
            throw new CommonException("error.app.market.check");
        }
        return true;
    }

    @Override
    public Long getMarketIdByAppId(Long appId) {
        return applicationMarketMapper.getMarketIdByAppId(appId);
    }

    @Override
    public void checkProject(Long projectId, Long appMarketId) {
        if (applicationMarketMapper.checkProject(projectId, appMarketId) != 1) {
            throw new CommonException("error.appMarket.project.unmatch");
        }
    }

    @Override
    public void checkDeployed(Long projectId, Long appMarketId, Long versionId, List<Long> projectIds) {
        if (applicationMarketMapper.checkDeployed(projectId, appMarketId, versionId, projectIds) > 0) {
            throw new CommonException("error.appMarket.instance.deployed");
        }
    }

    @Override
    public void unpublishApplication(Long appMarketId) {
        applicationMarketMapper.changeApplicationVersions(appMarketId, null, null);
        applicationMarketMapper.deleteByPrimaryKey(appMarketId);
    }

    @Override
    public void unpublishVersion(Long appMarketId, Long versionId) {
        applicationMarketMapper.changeApplicationVersions(appMarketId, versionId, null);
    }

    @Override
    public void updateVersion(Long appMarketId, Long versionId, Boolean isPublish) {
        applicationMarketMapper.changeApplicationVersions(appMarketId, versionId, isPublish);
    }

    @Override
    public void update(DevopsAppShareDO devopsAppMarketDO) {
        devopsAppMarketDO.setObjectVersionNumber(
                applicationMarketMapper.selectByPrimaryKey(devopsAppMarketDO.getId()).getObjectVersionNumber());
        applicationMarketMapper.updateByPrimaryKeySelective(devopsAppMarketDO);
    }

    @Override
    public List<DevopsAppMarketVersionDO> getVersions(Long projectId, Long appMarketId, Boolean isPublish) {
        List<Long> projectIds = getProjectIds(projectId);
        return applicationMarketMapper.listAppVersions(projectIds, appMarketId, isPublish, null, null);
    }

    @Override
    public PageInfo<DevopsAppMarketVersionDO> getVersions(Long projectId, Long appMarketId, Boolean isPublish,
                                                          PageRequest pageRequest, String params) {
        Sort sort = pageRequest.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if (property.equals("version")) {
                            property = "dav.version";
                        } else if (property.equals("updatedDate")) {
                            property = "dav.last_update_date";
                        } else if (property.equals("creationDate")) {
                            property = "dav.creation_date";
                        }

                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }

        Map<String, Object> searchParam = null;
        String param = null;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> searchParamMap = json.deserialize(params, Map.class);
            searchParam = TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM));
            param = TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM));
        }
        Map<String, Object> finalSearchParam = searchParam;
        String finalParam = param;
        List<Long> projectIds = getProjectIds(projectId);
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(
                () -> applicationMarketMapper.listAppVersions(
                        projectIds, appMarketId, isPublish,
                        finalSearchParam, finalParam));
    }

    @Override
    public DevopsAppShareE queryByAppId(Long appId) {
        DevopsAppShareDO applicationMarketDO = new DevopsAppShareDO();
        applicationMarketDO.setAppId(appId);
        return ConvertHelper.convert(applicationMarketMapper.selectOne(applicationMarketDO), DevopsAppShareE.class);
    }

    @Override
    public void checkMarketVersion(Long appMarketId, Long versionId) {
        if (!applicationMarketMapper.checkVersion(appMarketId, versionId)) {
            throw new CommonException("error.version.notMatch");
        }
    }

    private List<Long> getProjectIds(Long projectId) {
        List<Long> projectIds;
        if (projectId != null) {
            ProjectE projectE = iamRepository.queryIamProject(projectId);
            Long organizationId = projectE.getOrganization().getId();
            List<ProjectE> projectEList = iamRepository.listIamProjectByOrgId(organizationId, null, null);
            projectIds = projectEList.stream().map(ProjectE::getId)
                    .collect(Collectors.toCollection(ArrayList::new));
        } else {
            projectIds = null;
        }
        return projectIds;
    }

    @Override
    public PageInfo<DevopsAppShareE> queryByShareIds(PageRequest pageRequest, String param, List<Long> shareIds) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(param);
        PageInfo<DevopsAppShareDO> doPageInfo = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
                () -> applicationMarketMapper.queryByShareIds((Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM), (String) mapParams.get(TypeUtil.PARAM),shareIds));
        return ConvertPageHelper.convertPageInfo(doPageInfo, DevopsAppShareE.class);
    }
}
