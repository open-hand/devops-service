package io.choerodon.devops.app.service;

import java.util.List;

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
     * @param projectId          项目id
     * @param devopsCiPipelineVO
     * @return
     */
    DevopsCiPipelineDTO create(Long projectId, DevopsCiPipelineVO devopsCiPipelineVO);

    /**
     * 更新流水线
     *
     * @param projectId          项目id
     * @param ciPipelineId       流水线id
     * @param devopsCiPipelineVO
     * @return
     */
    DevopsCiPipelineDTO update(Long projectId, Long ciPipelineId, DevopsCiPipelineVO devopsCiPipelineVO);

    /**
     * 查询流水线详情（包含阶段和job信息）
     * @param projectId
     * @param ciPipelineId
     * @return
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
     * @param projectId
     * @param name
     * @return
     */
    List<DevopsCiPipelineVO> listByProjectIdAndAppName(Long projectId, String name);

    /**
     * 查询流水线信息
     * @param ciPipelineId
     * @return
     */
    DevopsCiPipelineVO queryById(Long ciPipelineId);

    /**
     * 停用流水线
     * @param projectId 项目id
     * @param ciPipelineId 流水线id
     * @return
     */
    DevopsCiPipelineDTO disablePipeline(Long projectId, Long ciPipelineId);

    /**
     * 删除流水线
     * @param projectId
     * @param ciPipelineId
     */
    void deletePipeline(Long projectId, Long ciPipelineId);

    /**
     * 启用流水线
     * @param projectId 项目id
     * @param ciPipelineId 流水线id
     * @return
     */
    DevopsCiPipelineDTO enablePipeline(Long projectId, Long ciPipelineId);
}
