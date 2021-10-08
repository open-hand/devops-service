package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.AppExternalConfigDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/28 10:57
 */
public interface AppExternalConfigService {

    AppExternalConfigDTO baseQuery(Long id);

    void baseSave(AppExternalConfigDTO appExternalConfigDTO);

    void update(Long projectId, Long id, AppExternalConfigDTO appExternalConfigDTO);

    void baseDelete(Long externalConfigId);

    List<AppExternalConfigDTO> queryByRepositoryUrl(String repositoryUrl);
}
