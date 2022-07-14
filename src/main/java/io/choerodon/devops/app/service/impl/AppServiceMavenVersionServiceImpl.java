package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.devops.app.service.AppServiceMavenVersionService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceMavenVersionDTO;
import io.choerodon.devops.infra.mapper.AppServiceMavenVersionMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 应用版本表(AppServiceMavenVersion)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:43
 */
@Service
public class AppServiceMavenVersionServiceImpl implements AppServiceMavenVersionService {
    @Autowired
    private AppServiceMavenVersionMapper appServiceMavenVersionMapper;

    @Override
    public AppServiceMavenVersionDTO queryByAppServiceVersionId(Long appServiceVersionId) {
        Assert.notNull(appServiceVersionId, ResourceCheckConstant.ERROR_SERVICE_VERSION_ID_IS_NULL);

        AppServiceMavenVersionDTO appServiceMavenVersionDTO = new AppServiceMavenVersionDTO();
        appServiceMavenVersionDTO.setAppServiceVersionId(appServiceVersionId);

        return appServiceMavenVersionMapper.selectOne(appServiceMavenVersionDTO);
    }

    @Override
    @Transactional
    public void create(AppServiceMavenVersionDTO appServiceMavenVersionDTO) {
        MapperUtil.resultJudgedInsertSelective(appServiceMavenVersionMapper, appServiceMavenVersionDTO, "error.save.maven.version");
    }
}

