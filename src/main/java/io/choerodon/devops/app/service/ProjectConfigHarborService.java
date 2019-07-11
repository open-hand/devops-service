package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.ProjectConfigDTO;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/13
 */
public interface ProjectConfigHarborService {
    void createHarbor(ProjectConfigDTO config, Long projectId);
}
