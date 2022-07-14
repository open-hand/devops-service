package io.choerodon.devops.app.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.DevopsHelmConfigVO;
import io.choerodon.devops.infra.dto.DevopsHelmConfigDTO;

public interface DevopsHelmConfigService {
    /**
     * 查询helm仓库列表
     *
     * @param projectId
     * @return
     */
    List<DevopsHelmConfigVO> listHelmConfig(Long projectId);

    /**
     * 创建helm仓库
     *
     * @param projectId
     * @param devopsHelmConfigVO
     * @return
     */
    DevopsHelmConfigVO createDevopsHelmConfigOnProjectLevel(Long projectId, DevopsHelmConfigVO devopsHelmConfigVO);

    /**
     * 更新helm仓库
     *
     * @param projectId
     * @param devopsHelmConfigVO
     * @return
     */
    DevopsHelmConfigVO updateDevopsHelmConfigOnProjectLevel(Long projectId, DevopsHelmConfigVO devopsHelmConfigVO);

    /**
     * 删除helm仓库
     *
     * @param projectId
     * @param helmConfigId
     */
    void deleteDevopsHelmConfig(Long projectId, Long helmConfigId);

    /**
     * 查询helm仓库信息
     *
     * @param projectId
     * @param helmConfigId
     * @return
     */
    DevopsHelmConfigVO queryDevopsHelmConfig(Long projectId, Long helmConfigId);

    /**
     * 设置默认仓库
     *
     * @param projectId
     * @param helmConfigId
     */
    void setDefaultDevopsHelmConfig(Long projectId, Long helmConfigId);

    /**
     * 查询指定层级的默认仓库
     *
     * @return
     */
    DevopsHelmConfigDTO queryDefaultDevopsHelmConfigByLevel(String resourceType);

    /**
     * 创建创建指定层级的仓库
     *
     * @param devopsHelmConfigDTO
     */
    void createDevopsHelmConfig(DevopsHelmConfigDTO devopsHelmConfigDTO);

    @Transactional(rollbackFor = Exception.class)
    void updateDevopsHelmConfig(DevopsHelmConfigDTO devopsHelmConfigDTO);
}
