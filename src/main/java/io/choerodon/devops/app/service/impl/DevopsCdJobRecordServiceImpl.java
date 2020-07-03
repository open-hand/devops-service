package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCdJobRecordService;
import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;
import io.choerodon.devops.infra.mapper.DevopsCdJobRecordMapper;

/**
 * @author scp
 * @date 2020/7/3
 * @description
 */
@Service
public class DevopsCdJobRecordServiceImpl implements DevopsCdJobRecordService {
    @Autowired
    private DevopsCdJobRecordMapper devopsCdJobRecordMapper;

    @Override
    public List<DevopsCdJobRecordDTO> queryByStageRecordId(Long stageRecordId) {
        DevopsCdJobRecordDTO jobRecordDTO = new DevopsCdJobRecordDTO();
        jobRecordDTO.setStageRecordId(stageRecordId);
        return devopsCdJobRecordMapper.select(jobRecordDTO);
    }
}
