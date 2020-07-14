package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsPipelineRecordRelService;
import io.choerodon.devops.infra.constant.PipelineConstants;
import io.choerodon.devops.infra.dto.DevopsPipelineRecordRelDTO;
import io.choerodon.devops.infra.mapper.DevopsPipelineRecordRelMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/14 20:52
 */
@Service
public class DevopsPipelineRecordRelServiceImpl implements DevopsPipelineRecordRelService {


    private static final String ERROR_UPDATE_PIPELINE_RECORD_REL = "error.update.pipeline.record.rel";
    @Autowired
    private DevopsPipelineRecordRelMapper devopsPipelineRecordRelMapper;


    @Override
    @Transactional
    public void save(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO) {
        if (devopsPipelineRecordRelMapper.insertSelective(devopsPipelineRecordRelDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_PIPELINE_RECORD_REL);
        }
    }

    @Override
    public DevopsPipelineRecordRelDTO queryByPipelineIdAndCiPipelineRecordId(Long pipelineId, Long ciPipelineRecordId) {
        DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = new DevopsPipelineRecordRelDTO();
        devopsPipelineRecordRelDTO.setPipelineId(pipelineId);
        devopsPipelineRecordRelDTO.setCiPipelineRecordId(ciPipelineRecordId);
        devopsPipelineRecordRelDTO.setCdPipelineRecordId(PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID);
        return devopsPipelineRecordRelMapper.selectOne(devopsPipelineRecordRelDTO);
    }

    @Override
    @Transactional
    public void update(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO) {
        if (devopsPipelineRecordRelMapper.updateByPrimaryKeySelective(devopsPipelineRecordRelDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_PIPELINE_RECORD_REL);
        }
    }

}
