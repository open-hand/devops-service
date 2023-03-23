package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsDeployValueVO;
import io.choerodon.devops.api.vo.PipelineInstanceReferenceVO;
import io.choerodon.devops.infra.dto.DevopsDeployValueDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:57 2019/4/10
 * Description:
 */
public interface DevopsDeployValueService {

    /**
     * 项目下创建流水线配置
     *
     * @param projectId           项目id
     * @param devopsDeployValueVO 配置信息
     * @return 创建后的配置信息
     */
    DevopsDeployValueVO createOrUpdate(Long projectId, DevopsDeployValueVO devopsDeployValueVO);

    /**
     * 项目下删除配置
     *
     * @param projectId 项目id
     * @param valueId   配置id
     */
    void delete(Long projectId, Long valueId);

    /**
     * 项目下获取部署配置
     *
     * @param projectId    项目id
     * @param appServiceId 应用服务id
     * @param envId        环境id
     * @param pageable     分页参数
     * @param params       查询参数
     * @return 分页的部署配置
     */
    Page<DevopsDeployValueVO> pageByOptions(Long projectId, Long appServiceId, Long envId, PageRequest pageable, String params);

    /**
     * 项目下查询配置详情
     *
     * @param pipelineId 流水线id
     * @param valueId    配置id
     * @return 配置信息
     */
    DevopsDeployValueVO query(Long pipelineId, Long valueId);

    /**
     * 校验部署配置的名称在环境下唯一
     *
     * @param projectId 项目id
     * @param name      待校验的名称
     * @param envId     环境id
     */
    void checkName(Long projectId, String name, Long envId);

    /**
     * 判断部署配置的名称在环境下唯一
     *
     * @param projectId 项目id
     * @param name      待校验的名称
     * @param envId     环境id
     * @return true表示唯一, 通过
     */
    boolean isNameUnique(Long projectId, String name, Long envId);

    /**
     * 根据应用服务Id和环境Id获取配置
     *
     * @param projectId    项目id
     * @param appServiceId 应用服务id
     * @param envId        环境id
     * @param name
     * @return 查看环境下服务的配置
     */
    List<DevopsDeployValueVO> listByEnvAndApp(Long projectId, Long appServiceId, Long envId, String name);


    List<PipelineInstanceReferenceVO> checkDelete(Long projectId, Long valueId);

    Page<DevopsDeployValueDTO> basePageByOptionsWithOwner(Long projectId, Long appServiceId, Long envId, Long userId, PageRequest pageable, String params);

    Page<DevopsDeployValueDTO> basePageByOptionsWithMember(Long projectId, Long appServiceId, Long envId, Long userId, PageRequest pageable, String params);

    DevopsDeployValueDTO baseCreateOrUpdate(DevopsDeployValueDTO pipelineRecordE);

    void baseDelete(Long valueId);

    DevopsDeployValueDTO baseQueryById(Long valueId);

    List<DevopsDeployValueDTO> baseQueryByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId, String name);

    /**
     * 根据环境id删除所有相关的部署配置
     *
     * @param envId 环境id
     */
    void deleteByEnvId(Long envId);

    List<DevopsDeployValueDTO> listValueByInstanceId(Long projectId, Long instanceId);
}
