package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.ConfigFileDetailDTO;

/**
 * 配置文件详情表(ConfigFileDetail)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-15 09:25:17
 */
public interface ConfigFileDetailService {

    ConfigFileDetailDTO baseCreate(ConfigFileDetailDTO configFileDetailDTO);

    void baseUpdate(ConfigFileDetailDTO configFileDetailDTO);

    void baseDelete(Long id);

    ConfigFileDetailDTO baseQueryById(Long id);
}

