package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.api.vo.appversion.AppServiceHelmVersionVO;
import io.choerodon.devops.app.service.AppServiceHelmVersionService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceHelmVersionDTO;
import io.choerodon.devops.infra.mapper.AppServiceHelmVersionMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 应用版本表(AppServiceHelmVersion)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:41
 */
@Service
public class AppServiceHelmVersionServiceImpl implements AppServiceHelmVersionService {
    @Autowired
    private AppServiceHelmVersionMapper appServiceHelmVersionMapper;

    @Override
    public List<AppServiceHelmVersionVO> listByAppVersionIds(Set<Long> versionIds) {
        return appServiceHelmVersionMapper.listByAppVersionIds(versionIds);
    }

    @Override
    public AppServiceHelmVersionDTO queryByAppServiceVersionId(Long appServiceVersionId) {
        Assert.notNull(appServiceVersionId, ResourceCheckConstant.ERROR_SERVICE_VERSION_ID_IS_NULL);

        AppServiceHelmVersionDTO appServiceHelmVersionDTO = new AppServiceHelmVersionDTO();
        appServiceHelmVersionDTO.setAppServiceVersionId(appServiceVersionId);

        return appServiceHelmVersionMapper.selectOne(appServiceHelmVersionDTO);
    }

    @Override
    @Transactional
    public void create(AppServiceHelmVersionDTO appServiceHelmVersionDTO) {
        MapperUtil.resultJudgedInsertSelective(appServiceHelmVersionMapper, appServiceHelmVersionDTO, "error.save.helm.version");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByAppServiceVersionId(Long appServiceVersionId) {
        Assert.notNull(appServiceVersionId, ResourceCheckConstant.ERROR_SERVICE_VERSION_ID_IS_NULL);

        AppServiceHelmVersionDTO appServiceHelmVersionDTO = new AppServiceHelmVersionDTO();
        appServiceHelmVersionDTO.setAppServiceVersionId(appServiceVersionId);
        appServiceHelmVersionMapper.delete(appServiceHelmVersionDTO);
    }
}

