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
    public ApplicationMarketE getMarket(Long projectId, Long appMarketId) {
        return ConvertHelper.convert(applicationMarketMapper.getMarketApplication(appMarketId), ApplicationMarketE.class);
    }


    @Override
    public int updateImgUrl(ApplicationMarketE applicationMarketE) {
        DevopsAppMarketDO devopsAppMarketDO = ConvertHelper.convert(applicationMarketE, DevopsAppMarketDO.class);
        DevopsAppMarketDO appDo = applicationMarketMapper.selectByPrimaryKey(devopsAppMarketDO.getId());
        if (appDo != null) {
            Long objV = appDo.getObjectVersionNumber();
            devopsAppMarketDO.setObjectVersionNumber(objV);
        }
        return applicationMarketMapper.updateByPrimaryKeySelective(devopsAppMarketDO);
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
}
