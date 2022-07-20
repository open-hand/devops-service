package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.devops.api.vo.appversion.AppServiceImageVersionVO;
import io.choerodon.devops.app.service.AppServiceImageVersionService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceImageVersionDTO;
import io.choerodon.devops.infra.mapper.AppServiceImageVersionMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 应用版本表(AppServiceImageVersion)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:42
 */
@Service
public class AppServiceImageVersionServiceImpl implements AppServiceImageVersionService {
    @Autowired
    private AppServiceImageVersionMapper appServiceImageVersionMapper;

    @Override
    public void create(AppServiceImageVersionDTO appServiceImageVersionDTO) {
        MapperUtil.resultJudgedInsertSelective(appServiceImageVersionMapper, appServiceImageVersionDTO, "error.save.image.version");
    }

    @Override
    public AppServiceImageVersionDTO queryByAppServiceVersionId(Long appServiceVersionId) {
        Assert.notNull(appServiceVersionId, ResourceCheckConstant.ERROR_SERVICE_VERSION_ID_IS_NULL);

        AppServiceImageVersionDTO appServiceImageVersionDTO = new AppServiceImageVersionDTO();
        appServiceImageVersionDTO.setAppServiceVersionId(appServiceVersionId);

        return appServiceImageVersionMapper.selectOne(appServiceImageVersionDTO);
    }

    @Override
    public List<AppServiceImageVersionVO> listByAppVersionIds(Set<Long> versionIds) {
        return appServiceImageVersionMapper.listByAppVersionIds(versionIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(AppServiceImageVersionDTO appServiceImageVersionDTO) {
        appServiceImageVersionMapper.updateByPrimaryKeySelective(appServiceImageVersionDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByAppServiceVersionId(Long appServiceVersionId) {
        Assert.notNull(appServiceVersionId, ResourceCheckConstant.ERROR_SERVICE_VERSION_ID_IS_NULL);

        AppServiceImageVersionDTO appServiceImageVersionDTO = new AppServiceImageVersionDTO();
        appServiceImageVersionDTO.setAppServiceVersionId(appServiceVersionId);
        appServiceImageVersionMapper.delete(appServiceImageVersionDTO);
    }
}

