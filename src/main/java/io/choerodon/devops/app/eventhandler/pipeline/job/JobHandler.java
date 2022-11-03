package io.choerodon.devops.app.eventhandler.pipeline.job;

import io.choerodon.devops.api.vo.CiJobWebHookVO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/3 9:03
 */
public interface JobHandler {

    /**
     * 初始化ci任务记录时，需要记录当前的任务配置信息
     *
     * @param devopsCiJobDTO
     * @param job
     */
    void fillJobAdditionalInfo(DevopsCiJobDTO devopsCiJobDTO, CiJobWebHookVO job);

    void saveAdditionalRecordInfo(DevopsCiJobRecordDTO devopsCiJobRecordDTO, Long gitlabPipelineId, CiJobWebHookVO ciJobWebHookVO);


    CiJobTypeEnum getType();


}
