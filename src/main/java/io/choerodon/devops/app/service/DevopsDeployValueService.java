package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsDeployValueVO;
import io.choerodon.devops.infra.dto.DevopsDeployValueDTO;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:57 2019/4/10
 * Description:
 */
public interface DevopsDeployValueService {

    /**
     * 项目下创建流水线配置
     *
     * @param projectId
     * @param devopsDeployValueVO
     * @return
     */
    DevopsDeployValueVO createOrUpdate(Long projectId, DevopsDeployValueVO devopsDeployValueVO);

    /**
     * 项目下删除配置
     *
     * @param projectId
     * @param valueId
     */
    void delete(Long projectId, Long valueId);

    /**
     * 项目下获取部署配置
     *
     * @param projectId
     * @param appServiceId
     * @param envId
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<DevopsDeployValueVO> pageByOptions(Long projectId, Long appServiceId, Long envId, PageRequest pageRequest, String params);

    /**
     * 项目下查询配置详情
     *
     * @param pipelineId
     * @param valueId
     * @return
     */
    DevopsDeployValueVO query(Long pipelineId, Long valueId);

    /**
     * 名称校验
     *
     * @param projectId
     * @param name
     */
    void checkName(Long projectId, String name);

    /**
     * 根据应用Id和环境Id获取配置
     *
     * @param projectId
     * @param appServiceId
     * @param envId
     * @return
     */
    List<DevopsDeployValueVO> listByEnvAndApp(Long projectId, Long appServiceId, Long envId);

    /**
     * 检测能否删除
     *
     * @param projectId
     * @param valueId
     * @return
     */
    Boolean checkDelete(Long projectId, Long valueId);

    PageInfo<DevopsDeployValueDTO> basePageByOptions(Long projectId, Long appServiceId, Long envId, Long userId, PageRequest pageRequest, String params);

    DevopsDeployValueDTO baseCreateOrUpdate(DevopsDeployValueDTO pipelineRecordE);

    void baseDelete(Long valueId);

    DevopsDeployValueDTO baseQueryById(Long valueId);

    void baseCheckName(Long projectId, String name);

    List<DevopsDeployValueDTO> baseQueryByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId);
}
