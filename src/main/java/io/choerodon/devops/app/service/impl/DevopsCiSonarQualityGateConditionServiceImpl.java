package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateConditionVO;
import io.choerodon.devops.app.service.DevopsCiSonarQualityGateConditionService;
import io.choerodon.devops.infra.mapper.DevopsCiSonarQualityGateConditionMapper;

@Service
public class DevopsCiSonarQualityGateConditionServiceImpl implements DevopsCiSonarQualityGateConditionService {
    @Autowired
    private DevopsCiSonarQualityGateConditionMapper devopsCiSOnarQualityGateConditionMapper;

    @Override
    public void createConditions(Long gateId, String sonarGateId, List<DevopsCiSonarQualityGateConditionVO> sonarQualityGateConditionVOList) {

    }

    @Override
    public void deleteByGateId(Long gateId) {
        devopsCiSOnarQualityGateConditionMapper.listByGateId(gateId);
    }
}
