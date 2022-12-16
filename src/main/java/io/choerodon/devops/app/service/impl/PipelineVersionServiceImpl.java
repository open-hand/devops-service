package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.PipelineVersionService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineVersionDTO;
import io.choerodon.devops.infra.mapper.PipelineVersionMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线版本表(PipelineVersion)应用服务
 *
 * @author
 * @since 2022-11-24 15:57:18
 */
@Service
public class PipelineVersionServiceImpl implements PipelineVersionService {

    private static final String DEVOPS_SAVE_PIPELINE_VERSION_FAILED = "devops.save.pipeline.version.failed";
    @Autowired
    private PipelineVersionMapper pipelineVersionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineVersionDTO pipelineVersionDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineVersionMapper, pipelineVersionDTO, DEVOPS_SAVE_PIPELINE_VERSION_FAILED);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineVersionDTO createByPipelineId(Long pipelineId) {
        PipelineVersionDTO pipelineVersionDTO = new PipelineVersionDTO();
        pipelineVersionDTO.setPipelineId(pipelineId);
        baseCreate(pipelineVersionDTO);
        return pipelineVersionDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        PipelineVersionDTO pipelineVersionDTO = new PipelineVersionDTO();
        pipelineVersionDTO.setPipelineId(pipelineId);
        pipelineVersionMapper.delete(pipelineVersionDTO);
    }
}

