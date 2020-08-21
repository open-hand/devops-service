package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsConfigMapRespVO;
import io.choerodon.devops.api.vo.DevopsConfigMapVO;
import io.choerodon.devops.infra.dto.DevopsConfigMapDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;

public interface DevopsConfigMapService {

    void createOrUpdate(Long projectId, Boolean isSync, DevopsConfigMapVO devopsConfigMapVO);

    void deleteByGitOps(Long configMapId);

    void delete(Long projectId, Long configMapId);

    void checkName(Long envId, String name);

    boolean isNameUnique(Long envId, String name);

    DevopsConfigMapRespVO createOrUpdateByGitOps(DevopsConfigMapVO devopsConfigMapVO, Long userId);

    DevopsConfigMapRespVO query(Long configMapId);

    Page<DevopsConfigMapRespVO> pageByOptions(Long projectId, Long envId, PageRequest pageable, String searchParam, Long appServiceId);

    DevopsConfigMapDTO baseQueryByEnvIdAndName(Long envId, String name);

    DevopsConfigMapDTO baseCreate(DevopsConfigMapDTO devopsConfigMapE);

    DevopsConfigMapDTO baseUpdate(DevopsConfigMapDTO devopsConfigMapE);

    DevopsConfigMapDTO baseQueryById(Long id);

    void baseDelete(Long id);

    Page<DevopsConfigMapDTO> basePageByEnv(Long envId, PageRequest pageable, String params, Long appServiceId);

    List<DevopsConfigMapDTO> baseListByEnv(Long envId);

    void baseDeleteByEnvId(Long envId);

}
