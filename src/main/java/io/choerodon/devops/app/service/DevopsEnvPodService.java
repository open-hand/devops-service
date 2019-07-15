package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsEnvPodVO;
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;

/**
 * Created by Zenger on 2018/4/17.
 */
public interface DevopsEnvPodService {

    /**
     * @param projectId
     * @param envId
     * @param appId
     * @param instanceId
     * @param pageRequest
     * @param searchParam
     * @return
     */
    PageInfo<DevopsEnvPodVO> listAppPod(Long projectId, Long envId, Long appId, Long instanceId, PageRequest pageRequest, String searchParam);

    void setContainers(DevopsEnvPodDTO devopsEnvPodDTO);

    DevopsEnvPodDTO baseQueryById(Long id);

    DevopsEnvPodDTO baseQueryByPod(DevopsEnvPodDTO devopsEnvPodDTO);

    void baseCreate(DevopsEnvPodDTO devopsEnvPodDTO);

    List<DevopsEnvPodDTO> baseListByInstanceId(Long instanceId);

    void baseUpdate(DevopsEnvPodDTO devopsEnvPodDTO);

    PageInfo<DevopsEnvPodDTO> basePageByIds(Long projectId, Long envId, Long appId, Long instanceId, PageRequest pageRequest, String searchParam);

    void baseDeleteByName(String name, String namespace);

    DevopsEnvPodDTO queryByNameAndEnvName(String name, String namespace);

}
