package io.choerodon.devops.app.eventhandler.pipeline.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.CiJobWebHookVO;
import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.app.service.DevopsCiJobService;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/3 9:03
 */
public abstract class AbstractJobHandler {
    @Autowired
    private DevopsCiJobService devopsCiJobService;

    /**
     * 初始化ci任务记录时，需要记录当前的任务配置信息
     *
     * @param devopsCiJobDTO
     * @param job
     */
    public void fillJobAdditionalInfo(DevopsCiJobDTO devopsCiJobDTO, CiJobWebHookVO job) {

    }

    public void saveAdditionalRecordInfo(DevopsCiJobRecordDTO devopsCiJobRecordDTO, Long gitlabPipelineId, CiJobWebHookVO ciJobWebHookVO) {

    }

    @Transactional(rollbackFor = Exception.class)
    public DevopsCiJobDTO saveJobInfo(Long ciPipelineId, Long ciStageId, DevopsCiJobVO devopsCiJobVO) {
        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
        // 保存任务配置
        Long configId = saveConfig(devopsCiJobVO);

        devopsCiJobDTO.setCiPipelineId(ciPipelineId);
        devopsCiJobDTO.setCiStageId(ciStageId);
        devopsCiJobDTO.setConfigId(configId);
        devopsCiJobService.create(devopsCiJobDTO);
        return devopsCiJobDTO;
    }

    /**
     * 保存任务配置，实现类如果需要存储任务配置则重写
     *
     * @param devopsCiJobVO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    protected Long saveConfig(DevopsCiJobVO devopsCiJobVO) {
        // do nothong
        return null;
    }

    public abstract CiJobTypeEnum getType();

    /**
     * 查询流水线详情时，给包含任务配置的任务填充信息
     *
     * @param devopsCiJobVO
     */
    public void fillJobConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        // do nothing
    }

    /**
     * 把配置转换为gitlab-ci配置（maven,sonarqube）
     *
     * @param organizationId 组织id
     * @param projectId      项目id
     * @param devopsCiJobDTO 生成脚本
     * @return 生成的脚本列表
     */
    public abstract List<String> buildScript(Long organizationId, Long projectId, DevopsCiJobDTO devopsCiJobDTO);
}
