package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.DevopsEnvGroupVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvGroupE;
import io.choerodon.devops.infra.dto.DevopsEnvGroupDTO;

public interface DevopsEnvGroupService {
    DevopsEnvGroupVO create(String name, Long projectId);

    DevopsEnvGroupVO update(DevopsEnvGroupVO devopsEnvGroupDTO, Long projectId);

    List<DevopsEnvGroupVO> listByProject(Long projectId);

    Boolean checkUniqueInProject(String name, Long projectId);

    void delete(Long id);

    DevopsEnvGroupDTO baseCreate(DevopsEnvGroupDTO devopsEnvGroupDTO);

    DevopsEnvGroupDTO baseUpdate(DevopsEnvGroupDTO devopsEnvGroupDTO);

    List<DevopsEnvGroupDTO> baseListByProjectId(Long projectId);

    DevopsEnvGroupDTO baseQuery(Long id);

    Boolean baseCheckUniqueInProject(Long id, String name, Long projectId);

    Boolean baseCheckUniqueInProject(String name, Long projectId);

    void baseDelete(Long id);

}
