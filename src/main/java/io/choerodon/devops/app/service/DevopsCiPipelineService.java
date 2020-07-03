package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.CiCdPipelineVO;
import io.choerodon.devops.api.vo.DevopsCiPipelineVO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineDTO;

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
    DevopsCiPipelineDTO create(Long projectId, CiCdPipelineVO ciCdPipelineVO);

    /**
     * 更新流水线
     */
    DevopsCiPipelineDTO update(Long projectId, Long ciPipelineId, DevopsCiPipelineVO devopsCiPipelineVO);

    /**
     * 查询流水线详情（包含阶段和job信息）
     */
    DevopsCiPipelineVO query(Long projectId, Long ciPipelineId);

    /**
     * 根据应用服务id查询流水线
     *
     * @param appServiceId 应用服务id
     * @return
     */
    DevopsCiPipelineDTO queryByAppSvcId(Long appServiceId);

    /**
     * 查询项目下流水线列表（包含5条执行记录）
     */
    List<DevopsCiPipelineVO> listByProjectIdAndAppName(Long projectId, String name);

    /**
     * 查询流水线信息
     */
    DevopsCiPipelineVO queryById(Long ciPipelineId);

    /**
     * 停用流水线
     */
    DevopsCiPipelineDTO disablePipeline(Long projectId, Long ciPipelineId);

    /**
     * 删除流水线
     */
    void deletePipeline(Long projectId, Long ciPipelineId);

    /**
     * 启用流水线
     */
    DevopsCiPipelineDTO enablePipeline(Long projectId, Long ciPipelineId);

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
