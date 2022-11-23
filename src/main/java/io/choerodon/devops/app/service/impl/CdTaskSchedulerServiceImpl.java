package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.CdTaskSchedulerService;
import io.choerodon.devops.app.service.PipelineJobRecordService;
import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/23 17:26
 */
@Service
public class CdTaskSchedulerServiceImpl implements CdTaskSchedulerService {

    @Autowired
    private PipelineJobRecordService pipelineJobRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void schedulePeriodically() {
        // 查询数据库中处于pending状态的任务
        List<PipelineJobRecordDTO> pipelineJobRecordDTOS = pipelineJobRecordService.listPendingJobs(50);
        

    }
}
