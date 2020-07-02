package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.CiCdPipelineRecordService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiCdPipelineRecordDTO;
import io.choerodon.devops.infra.mapper.CiCdPipelineRecordMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/2 10:41
 */
@Service
public class CiCdPipelineRecordServiceImpl implements CiCdPipelineRecordService {


    @Autowired
    private CiCdPipelineRecordMapper ciCdPipelineRecordMapper;

    @Override
    public CiCdPipelineRecordDTO queryByGitlabPipelineId(Long gitlabPipelineId) {
        Assert.notNull(gitlabPipelineId, PipelineCheckConstant.ERROR_GITLAB_PIPELINE_ID_IS_NULL);
        CiCdPipelineRecordDTO ciCdPipelineRecordDTO = new CiCdPipelineRecordDTO();
        ciCdPipelineRecordDTO.setGitlabPipelineId(gitlabPipelineId);
        return ciCdPipelineRecordMapper.selectOne(ciCdPipelineRecordDTO);
    }
}
