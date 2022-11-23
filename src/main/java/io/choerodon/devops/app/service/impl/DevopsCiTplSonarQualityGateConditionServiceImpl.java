package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateConditionVO;
import io.choerodon.devops.app.service.DevopsCiTplSonarQualityGateConditionService;
import io.choerodon.devops.infra.dto.DevopsCiTplSonarQualityGateConditionDTO;
import io.choerodon.devops.infra.mapper.DevopsCiTplSonarQualityGateConditionMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

@Service
public class DevopsCiTplSonarQualityGateConditionServiceImpl implements DevopsCiTplSonarQualityGateConditionService {
    @Autowired
    private DevopsCiTplSonarQualityGateConditionMapper devopsCiTplSonarQualityGateConditionMapper;

    @Override
    public List<DevopsCiSonarQualityGateConditionVO> listByGateId(Long gateId) {
        List<DevopsCiTplSonarQualityGateConditionDTO> devopsCiTplSonarQualityGateConditionDTOS = devopsCiTplSonarQualityGateConditionMapper.listByGateId(gateId);
        return ConvertUtils.convertList(devopsCiTplSonarQualityGateConditionDTOS, DevopsCiSonarQualityGateConditionVO.class);
    }
}