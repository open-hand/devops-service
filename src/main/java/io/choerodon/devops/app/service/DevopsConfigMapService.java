package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsConfigMapDTO;
import io.choerodon.devops.api.dto.DevopsConfigMapRepDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsConfigMapService {

    void createOrUpdate(Long projectId, DevopsConfigMapDTO devopsConfigMapDTO);

    void deleteByGitOps(Long configMapId);

    void delete(Long configMapId);

    void checkName(Long envId, String name);

    DevopsConfigMapRepDTO createOrUpdateByGitOps(DevopsConfigMapDTO devopsConfigMapDTO, Long userId);

    DevopsConfigMapRepDTO query(Long configMapId);

    Page<DevopsConfigMapRepDTO> listByEnv(Long projectId, Long envId, PageRequest pageRequest, String searchParam);

}
