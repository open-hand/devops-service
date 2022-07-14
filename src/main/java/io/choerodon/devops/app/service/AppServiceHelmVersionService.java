package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.api.vo.appversion.AppServiceHelmVersionVO;

/**
 * 应用版本表(AppServiceHelmVersion)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:41
 */
public interface AppServiceHelmVersionService {

    List<AppServiceHelmVersionVO> listByAppVersionIds(Set<Long> versionIds);
}

