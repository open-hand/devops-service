package io.choerodon.devops.app.service;

import io.choerodon.devops.app.eventhandler.payload.ProjectPayload;

/**
 * Created by wangxiang on 2020/12/29
 */
public interface GitlabHandleService {
    void handleProjectCategoryEvent(ProjectPayload projectPayload);
}
