package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.DevopsHelmConfigVO;

public interface DevopsHelmConfigService {
    /**
     * 查询helm仓库列表
     * @param projectId
     * @return
     */
    List<DevopsHelmConfigVO> listHelmConfig(Long projectId);

    /**
     * 创建helm仓库
     * @param projectId
     * @param devopsHelmConfigVO
     * @return
     */
    DevopsHelmConfigVO createDevopsHelmConfig(Long projectId, DevopsHelmConfigVO devopsHelmConfigVO);

    /**
     * 更新helm仓库
     * @param projectId
     * @param devopsHelmConfigVO
     * @return
     */
    DevopsHelmConfigVO updateDevopsHelmConfig(Long projectId, DevopsHelmConfigVO devopsHelmConfigVO);

    /**
     * 删除helm仓库
     * @param projectId
     * @param helmConfigId
     */
    void deleteHelmConfig(Long projectId, Long helmConfigId);

    /**
     * 查询helm仓库信息
     * @param projectId
     * @param helmConfigId
     * @return
     */
    DevopsHelmConfigVO queryHelmConfig(Long projectId, Long helmConfigId);

    /**
     * 设置默认仓库
     * @param projectId
     * @param helmConfigId
     */
    void setDefaultHelmConfig(Long projectId, Long helmConfigId);
}
