package io.choerodon.devops.app.service;


public interface CiTemplateStageBusService {
    void deleteStageById(Long projectId, Long ciTemplateStageId);
}
