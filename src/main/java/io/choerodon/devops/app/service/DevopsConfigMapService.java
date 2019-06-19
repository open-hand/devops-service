package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.dto.DevopsConfigMapDTO;
import io.choerodon.devops.api.dto.DevopsConfigMapRepDTO;

public interface DevopsConfigMapService {

    void createOrUpdate(Long projectId, Boolean IsSync, DevopsConfigMapDTO devopsConfigMapDTO);

    void deleteByGitOps(Long configMapId);

    void delete(Long configMapId);

    void checkName(Long envId, String name);

    DevopsConfigMapRepDTO createOrUpdateByGitOps(DevopsConfigMapDTO devopsConfigMapDTO, Long userId);

    DevopsConfigMapRepDTO query(Long configMapId);

    PageInfo<DevopsConfigMapRepDTO> listByEnv(Long projectId, Long envId, PageRequest pageRequest, String searchParam);

}
