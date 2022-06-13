package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.AppExternalConfigDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/28 10:57
 */
public interface AppExternalConfigService {

    AppExternalConfigDTO baseQueryWithPassword(Long id);

    AppExternalConfigDTO baseQueryWithoutPasswordAndToken(Long id);

    void baseSave(AppExternalConfigDTO appExternalConfigDTO);

    void update(Long projectId, Long id, AppExternalConfigDTO appExternalConfigDTO);

    void baseDelete(Long externalConfigId);

    boolean checkRepositoryUrlUnique(String repositoryUrl);
}
