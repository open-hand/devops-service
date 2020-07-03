package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.CiCdJobValuesServcie;
import io.choerodon.devops.infra.dto.DevopsCdJobValuesDTO;
import io.choerodon.devops.infra.mapper.CiCdJobValuesMapper;

@Service
public class CiCdJobValuesServcieImpl implements CiCdJobValuesServcie {

    private static final String CREATE_CICD_CONTENT_FAILED = "create.ci.content.failed";
    private static final String ERROR_PIPELINE_ID_IS_NULL = "error.pipeline.id.is.null";


    @Autowired
    private CiCdJobValuesMapper ciCdJobValuesMapper;

    @Override
    public void create(DevopsCdJobValuesDTO devopsCdJobValuesDTO) {
        if (ciCdJobValuesMapper.insertSelective(devopsCdJobValuesDTO) != 1) {
            throw new CommonException(CREATE_CICD_CONTENT_FAILED);
        }
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long ciCdPipelineId) {
        if (ciCdPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        ciCdJobValuesMapper.deleteByPipelineId(ciCdPipelineId);
    }

}
