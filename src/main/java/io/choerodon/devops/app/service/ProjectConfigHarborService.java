package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.ProjectConfigVO;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/13
 */
public interface ProjectConfigHarborService {
    void createHarbor(ProjectConfigVO config, Long projectId);
}
