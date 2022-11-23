package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.PipelineJobRecordService;
import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;
import io.choerodon.devops.infra.mapper.PipelineJobRecordMapper;

/**
 * 流水线任务记录(PipelineJobRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:34
 */
@Service
public class PipelineJobRecordServiceImpl implements PipelineJobRecordService {
    @Autowired
    private PipelineJobRecordMapper pipelineJobRecordMapper;

    @Override
    public List<PipelineJobRecordDTO> listPendingJobs(int number) {
        return pipelineJobRecordMapper.listPendingJobs(number);
    }
}

