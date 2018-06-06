package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.ApplicationMarketE;
import io.choerodon.devops.domain.application.repository.ApplicationMarketRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketDO;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketVersionDO;
import io.choerodon.devops.infra.mapper.ApplicationMarketMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by ernst on 2018/3/28.
 */
@Service
public class ApplicationMarketRepositoryImpl implements ApplicationMarketRepository {

    private JSON json = new JSON();

    private ApplicationMarketMapper applicationMarketMapper;

    public ApplicationMarketRepositoryImpl(ApplicationMarketMapper applicationMarketMapper) {
        this.applicationMarketMapper = applicationMarketMapper;
    }


    @Override
    public void create(ApplicationMarketE applicationMarketE) {
        DevopsAppMarketDO devopsAppMarketDO = ConvertHelper.convert(applicationMarketE, DevopsAppMarketDO.class);
        applicationMarketMapper.insert(devopsAppMarketDO);
    }

    @Override
    public Page<ApplicationMarketE> listMarketAppsByProjectId(Long projectId, PageRequest pageRequest, String searchParam) {
        //TODO 排序
        Page<DevopsAppMarketDO> applicationMarketQueryDOPage;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            applicationMarketQueryDOPage = PageHelper.doPageAndSort(
                    pageRequest, () -> applicationMarketMapper.listMarketApplicationInProject(
                            projectId,
                            TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                            TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM))));
        } else {
            applicationMarketQueryDOPage = PageHelper.doPageAndSort(
                    pageRequest, () -> applicationMarketMapper.listMarketApplicationInProject(projectId, null, null));
        }
        return ConvertPageHelper.convertPage(applicationMarketQueryDOPage, ApplicationMarketE.class);
    }

    @Override
    public Page<ApplicationMarketE> listMarketApps(List<Long> projectIds, PageRequest pageRequest, String searchParam) {
        //TODO 排序
        Page<DevopsAppMarketDO> applicationMarketQueryDOPage;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            applicationMarketQueryDOPage = PageHelper.doPageAndSort(
                    pageRequest, () -> applicationMarketMapper.listMarketApplication(
                            projectIds,
                            TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                            TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM))));
        } else {
            applicationMarketQueryDOPage = PageHelper.doPageAndSort(
                    pageRequest, () -> applicationMarketMapper.listMarketApplication(projectIds, null, null));
        }
        return ConvertPageHelper.convertPage(applicationMarketQueryDOPage, ApplicationMarketE.class);
    }

    @Override
    public ApplicationMarketE getMarket(Long projectId, Long appMarketId, List<Long> projectIds) {
        return ConvertHelper.convert(
                applicationMarketMapper.getMarketApplication(projectId, appMarketId, projectIds),
                ApplicationMarketE.class);
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
        applicationMarketMapper.unpublishApplicationVersions(appMarketId, null);
        applicationMarketMapper.deleteByPrimaryKey(appMarketId);
    }

    @Override
    public void unpublishVersion(Long appMarketId, Long versionId) {
        applicationMarketMapper.unpublishApplicationVersions(appMarketId, versionId);
    }

    @Override
    public void update(DevopsAppMarketDO devopsAppMarketDO) {
        devopsAppMarketDO.setObjectVersionNumber(
                applicationMarketMapper.selectByPrimaryKey(devopsAppMarketDO.getId()).getObjectVersionNumber());
        applicationMarketMapper.updateByPrimaryKeySelective(devopsAppMarketDO);
    }

    @Override
    public List<DevopsAppMarketVersionDO> getVersions(Long projectId, Long appMarketId, Boolean isPublish) {
        return applicationMarketMapper.listAppVersions(projectId, appMarketId, isPublish, null, null);
    }

    @Override
    public Page<DevopsAppMarketVersionDO> getVersions(Long projectId, Long appMarketId, Boolean isPublish,
                                                      PageRequest pageRequest, String params) {
        Map<String, Object> searchParam = null;
        String param = null;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> searchParamMap = json.deserialize(params, Map.class);
            searchParam = TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM));
            param = TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM));
        }
        Map<String, Object> finalSearchParam = searchParam;
        String finalParam = param;
        return PageHelper.doPageAndSort(pageRequest,
                () -> applicationMarketMapper.listAppVersions(
                        projectId, appMarketId, isPublish,
                        finalSearchParam, finalParam));
    }
}
