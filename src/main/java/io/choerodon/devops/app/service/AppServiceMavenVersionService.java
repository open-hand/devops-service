package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.AppServiceMavenVersionDTO;

/**
 * 应用版本表(AppServiceMavenVersion)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:43
 */
public interface AppServiceMavenVersionService {

    AppServiceMavenVersionDTO queryByAppServiceVersionId(Long appServiceVersionId);

    void create(AppServiceMavenVersionDTO appServiceMavenVersionDTO);

}

