package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.CiCdPipelineVO;
import io.choerodon.devops.api.vo.DevopsCiPipelineVO;
import io.choerodon.devops.infra.dto.CiCdPipelineDTO;

/**
 * 〈功能简述〉
 * 〈ci流水线service〉
 *
 * @author wanghao
 * @Date 2020/4/2 17:59
 */
public interface DevopsCiPipelineService {
    /**
     * 创建流水线
     *
     * @param projectId      项目id
     * @param ciCdPipelineVO 流水线数据
     * @return 创建的流水线
     */
    CiCdPipelineDTO create(Long projectId, CiCdPipelineVO ciCdPipelineVO);

    /**
     * 更新流水线
     */
    CiCdPipelineDTO update(Long projectId, Long ciPipelineId, CiCdPipelineVO ciCdPipelineVO);

    /**
     * 查询流水线详情（包含阶段和job信息）
     */
    CiCdPipelineVO query(Long projectId, Long pipelineId);

    /**
     * 根据应用服务id查询流水线
     *
     * @param appServiceId 应用服务id
     * @return
     */
    CiCdPipelineDTO queryByAppSvcId(Long appServiceId);

    /**
     * 查询项目下流水线列表（包含5条执行记录）
     */
    List<CiCdPipelineVO> listByProjectIdAndAppName(Long projectId, String name);

    /**
     * 查询流水线信息
     */
    CiCdPipelineVO queryById(Long ciPipelineId);

    /**
     * 停用流水线
     */
    CiCdPipelineDTO disablePipeline(Long projectId, Long ciPipelineId);

    /**
     * 删除流水线
     */
    void deletePipeline(Long projectId, Long ciPipelineId);

    /**
     * 启用流水线
     */
    CiCdPipelineDTO enablePipeline(Long projectId, Long ciPipelineId);

    /**
     * 全新执行流水线
     */
    void executeNew(Long projectId, Long ciPipelineId, Long gitlabProjectId, String ref);

    /**
     * 校验用户是否有分支权限
     */
    void checkUserBranchPushPermission(Long projectId, Long gitlabUserId, Long gitlabProjectId, String ref);

    /**
     * 查询这个应用服务关联的CI流水线的数量
     *
     * @param appServiceId 应用服务id
     * @return 数量
     */
    int selectCountByAppServiceId(Long appServiceId);
}
