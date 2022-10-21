package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.api.vo.appversion.AppServiceMavenVersionVO;
import io.choerodon.devops.app.service.AppServiceMavenVersionService;
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

    public static final String DEVOPS_SAVE_MAVEN_VERSION = "devops.save.maven.version";

    @Autowired
    private AppServiceMavenVersionMapper appServiceMavenVersionMapper;

    @Override
    public AppServiceMavenVersionDTO queryByAppServiceVersionId(Long appServiceVersionId) {
        Assert.notNull(appServiceVersionId, ResourceCheckConstant.DEVOPS_SERVICE_VERSION_ID_IS_NULL);

        AppServiceMavenVersionDTO appServiceMavenVersionDTO = new AppServiceMavenVersionDTO();
        appServiceMavenVersionDTO.setAppServiceVersionId(appServiceVersionId);

        return appServiceMavenVersionMapper.selectOne(appServiceMavenVersionDTO);
    }

    @Override
    @Transactional
    public void create(AppServiceMavenVersionDTO appServiceMavenVersionDTO) {
        MapperUtil.resultJudgedInsertSelective(appServiceMavenVersionMapper, appServiceMavenVersionDTO, DEVOPS_SAVE_MAVEN_VERSION);
    }

    @Override
    public List<AppServiceMavenVersionVO> listByAppVersionIds(Set<Long> versionIds) {
        return appServiceMavenVersionMapper.listByAppVersionIds(versionIds);
    }

    @Override
    @Transactional
    public void baseUpdate(AppServiceMavenVersionDTO appServiceMavenVersionDTO) {
        appServiceMavenVersionMapper.updateByPrimaryKeySelective(appServiceMavenVersionDTO);
    }

    @Override
    public void deleteByAppServiceVersionId(Long appServiceVersionId) {
        Assert.notNull(appServiceVersionId, ResourceCheckConstant.DEVOPS_SERVICE_VERSION_ID_IS_NULL);

        AppServiceMavenVersionDTO appServiceMavenVersionDTO = new AppServiceMavenVersionDTO();
        appServiceMavenVersionDTO.setAppServiceVersionId(appServiceVersionId);
        appServiceMavenVersionMapper.delete(appServiceMavenVersionDTO);

    }
}

