package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.infra.dto.DevopsNormalInstanceDTO;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:25
 */
public interface DevopsNormalInstanceService {
    /**
     * 部署java应用
     * @param projectId
     * @param jarDeployVO
     */
    void deployJavaInstance(Long projectId, JarDeployVO jarDeployVO);

    /**
     * 查询java实例列表
     * @param hostId
     * @return
     */
    List<DevopsNormalInstanceDTO> listByHostId(Long hostId);

    void baseUpdate(DevopsNormalInstanceDTO devopsNormalInstanceDTO);

    void baseDelete(Long instanceId);

    DevopsNormalInstanceDTO baseQuery(Long instanceId);
}
