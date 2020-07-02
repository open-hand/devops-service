package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.CiCdStageRecordService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiCdStageRecordDTO;
import io.choerodon.devops.infra.mapper.CiCdStageRecordMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/2 11:07
 */
@Service
public class CiCdStageRecordServiceImpl implements CiCdStageRecordService {

    @Autowired
    private CiCdStageRecordMapper ciCdStageRecordMapper;

    @Override
    public List<CiCdStageRecordDTO> queryByPipelineRecordId(Long pipelineRecordId) {
        Assert.notNull(pipelineRecordId, PipelineCheckConstant.ERROR_PIPELINE_RECORD_ID_IS_NULL);
        CiCdStageRecordDTO ciCdStageRecordDTO = new CiCdStageRecordDTO();
        ciCdStageRecordDTO.setCicdPipelineRecordId(pipelineRecordId);
        return ciCdStageRecordMapper.select(ciCdStageRecordDTO);
    }
}
