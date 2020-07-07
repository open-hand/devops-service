package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;
import io.choerodon.devops.api.vo.DevopsCdPipelineRecordVO;
import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.app.service.CiCdPipelineRecordService;
import io.choerodon.devops.app.service.DevopsCdPipelineRecordService;
import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;

@Service
public class CiCdPipelineRecordServiceImpl implements CiCdPipelineRecordService {

    @Autowired
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;

    @Autowired
    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;


    @Override
    public CiCdPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long gitlabPipelineId,Long pipelioneRecordId) {
        CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
        DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = devopsCiPipelineRecordService.queryPipelineRecordDetails(projectId, gitlabPipelineId);
        ciCdPipelineRecordVO.setCiPipelineRecordVO(devopsCiPipelineRecordVO);
        DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = devopsCdPipelineRecordService.queryPipelineRecordDetails(projectId, gitlabPipelineId);
        ciCdPipelineRecordVO.setCdPipelineRecordVO(devopsCdPipelineRecordVO);
        return ciCdPipelineRecordVO;
    }
}
