package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.PipelineLogService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineLogDTO;
import io.choerodon.devops.infra.mapper.PipelineLogMapper;

/**
 * 流水线执行日志(PipelineLog)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:45
 */
@Service
public class PipelineLogServiceImpl implements PipelineLogService {
    @Autowired
    private PipelineLogMapper pipelineLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);
        PipelineLogDTO pipelineLogDTO = new PipelineLogDTO();
        pipelineLogDTO.setPipelineId(pipelineId);

        pipelineLogMapper.delete(pipelineLogDTO);
    }
}

