package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.appversion.AppServiceHelmVersionVO;
import io.choerodon.devops.app.service.AppServiceHelmVersionService;
import io.choerodon.devops.infra.mapper.AppServiceHelmVersionMapper;

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
}

