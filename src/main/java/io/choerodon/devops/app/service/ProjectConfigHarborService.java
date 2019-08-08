package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.ConfigVO;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/13
 */
public interface ProjectConfigHarborService {
    void createHarbor(ConfigVO config, Long projectId);
}
