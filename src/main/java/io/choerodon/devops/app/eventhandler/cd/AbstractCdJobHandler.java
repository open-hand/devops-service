package io.choerodon.devops.app.eventhandler.cd;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_PIPELINE_JOB_FINISH;

import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.devops.api.vo.cd.PipelineJobFinishVO;
import io.choerodon.devops.api.vo.cd.PipelineJobRecordVO;
import io.choerodon.devops.api.vo.cd.PipelineJobVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.PipelineJobDTO;
import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;
import io.choerodon.devops.infra.enums.cd.CdJobTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/3 9:03
 */
@Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = DEVOPS_PIPELINE_JOB_FINISH, description = "流水线任务执行结束", inputSchemaClass = PipelineJobFinishVO.class)

public abstract class AbstractCdJobHandler {
    @Autowired
    @Lazy
    protected PipelineService pipelineService;
    @Autowired
    @Lazy
    protected PipelineJobService pipelineJobService;
    @Autowired
    @Lazy
    protected PipelineJobRecordService pipelineJobRecordService;
    @Autowired
    protected PipelineStageRecordService pipelineStageRecordService;
    @Autowired
    @Lazy
    protected PipelineRecordService pipelineRecordService;

    public abstract CdJobTypeEnum getType();

    /**
     * 校验任务配置信息
     *
     * @param projectId
     * @param pipelineJobVO
     */
    protected void checkConfigInfo(Long projectId, PipelineJobVO pipelineJobVO) {

    }

    @Transactional(rollbackFor = Exception.class)
    public PipelineJobDTO saveJobInfo(Long projectId,
                                      Long pipelineId,
                                      Long versionId,
                                      Long stageId,
                                      PipelineJobVO pipelineJobVO) {
        PipelineJobDTO pipelineJobDTO = ConvertUtils.convertObject(pipelineJobVO, PipelineJobDTO.class);
        // 校验任务配置
        checkConfigInfo(projectId, pipelineJobVO);
        // 保存任务配置
        Long configId = saveConfig(pipelineId, pipelineJobVO);

        pipelineJobDTO.setId(null);
        pipelineJobDTO.setPipelineId(pipelineId);
        pipelineJobDTO.setVersionId(versionId);
        pipelineJobDTO.setStageId(stageId);
        pipelineJobDTO.setConfigId(configId);
        pipelineJobService.baseCreate(pipelineJobDTO);
        return pipelineJobDTO;
    }


    /**
     * 保存任务配置，实现类如果需要存储任务配置则重写
     *
     * @param pipelineId
     * @param devopsCiJobVO
     * @return
     */
    protected Long saveConfig(Long pipelineId, PipelineJobVO devopsCiJobVO) {
        // do nothing
        return null;
    }

    /**
     * 查询流水线详情时，给包含任务配置的任务填充信息
     *
     * @param pipelineJobVO
     */
    public void fillJobConfigInfo(PipelineJobVO pipelineJobVO) {
        // do nothing
    }

    /**
     * 添加job额外的配置信息，比如部署任务是否可编辑
     *
     * @param pipelineJobVO
     */
    public void fillJobAdditionalInfo(PipelineJobVO pipelineJobVO) {

    }

    /**
     * 初始化流水线记录时，给不同任务类型初始化额外的记录信息
     *
     * @param pipelineId
     * @param job
     * @param pipelineJobRecordDTO
     */
    public void initAdditionalRecordInfo(Long pipelineId, PipelineJobDTO job, PipelineJobRecordDTO pipelineJobRecordDTO) {

    }

    /**
     * 查询流水线记录详情时按任务类型填充特殊数据
     *
     * @param pipelineJobRecordVO
     */
    public abstract void fillAdditionalRecordInfo(PipelineJobRecordVO pipelineJobRecordVO);

    public void execCommand(Long jobRecordId, StringBuilder log) {

    }
}
