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
     * 查询helm仓库信息
     *
     * @param id
     * @return
     */
    DevopsHelmConfigDTO queryById(Long id);

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
     * 查询指定层级的默认仓库
     *
     * @return
     */
    DevopsHelmConfigDTO queryDefaultDevopsHelmConfigByLevel(String resourceType, Long resourceId);

    /**
     * 创建创建指定层级的仓库
     *
     * @param devopsHelmConfigDTO
     */
    void createDevopsHelmConfig(DevopsHelmConfigDTO devopsHelmConfigDTO);

    @Transactional(rollbackFor = Exception.class)
    void updateDevopsHelmConfig(DevopsHelmConfigDTO devopsHelmConfigDTO);

    void updateDevopsHelmConfigToNonDefaultRepoOnOrganization(Long resourceId);

    /**
     * 查询应用服务生效的配置
     * 生效优先级 app -> project -> tenant -> site, 查到就返回
     *
     * @return
     */
    DevopsHelmConfigDTO queryAppConfig(Long appServiceId, Long projectId, Long tenantId);

    /**
     * 检查项目下仓库名称是否已存在
     *
     * @param projectId
     * @param helmConfigId
     * @param name
     * @return
     */
    boolean checkNameExists(Long projectId, Long helmConfigId, String name);

    void checkNameExistsThrowEx(Long projectId, Long helmConfigId, String name);

    /**
     * 获取chart仓库的index内容
     *
     * @param projectId
     * @param helmConfigId
     * @return
     */
    String getIndexContent(Long projectId, Long helmConfigId);

    /**
     * 应用层查询helm仓库配置列表
     *
     * @param projectId
     * @param appServiceId
     * @return
     */
    List<DevopsHelmConfigVO> listHelmConfigOnApp(Long projectId, Long appServiceId);

    /**
     * 批量插入
     */
    void batchInsertInNewTrans(List<DevopsHelmConfigDTO> devopsHelmConfigDTOS);

    /**
     * 下载chart包
     *
     * @param helmConfigId
     * @param chartUrl
     * @return
     */
    byte[] downloadChart(Long helmConfigId, String chartUrl);
}
