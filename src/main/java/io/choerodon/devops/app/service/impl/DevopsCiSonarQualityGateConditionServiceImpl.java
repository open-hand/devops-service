package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateConditionVO;
import io.choerodon.devops.api.vo.sonar.QualityGateCondition;
import io.choerodon.devops.app.service.DevopsCiSonarQualityGateConditionService;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.dto.DevopsCiSonarQualityGateConditionDTO;
import io.choerodon.devops.infra.feign.operator.SonarClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCiSonarQualityGateConditionMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsCiSonarQualityGateConditionServiceImpl implements DevopsCiSonarQualityGateConditionService {
    @Autowired
    private SonarClientOperator sonarClientOperator;
    @Autowired
    private DevopsCiSonarQualityGateConditionMapper devopsCiSOnarQualityGateConditionMapper;

    @Override
    public void createConditions(Long gateId, String sonarGateId, List<DevopsCiSonarQualityGateConditionVO> sonarQualityGateConditionVOList) {
        sonarQualityGateConditionVOList.forEach(devopsCiSonarQualityGateConditionVO -> {
            DevopsCiSonarQualityGateConditionDTO sonarQualityGateConditionDTO = new DevopsCiSonarQualityGateConditionDTO();
            sonarQualityGateConditionDTO.setGateId(gateId);
            sonarQualityGateConditionDTO.setGatesMetric(devopsCiSonarQualityGateConditionVO.getGatesMetric());
            sonarQualityGateConditionDTO.setGatesValue(devopsCiSonarQualityGateConditionVO.getGatesValue());
            sonarQualityGateConditionDTO.setGatesScope(devopsCiSonarQualityGateConditionVO.getGatesScope());
            sonarQualityGateConditionDTO.setGatesOperator(devopsCiSonarQualityGateConditionVO.getGatesOperator());

            QualityGateCondition qualityGateCondition = sonarClientOperator.createQualityGateCondition(sonarGateId, devopsCiSonarQualityGateConditionVO.getGatesMetric(), devopsCiSonarQualityGateConditionVO.getGatesOperator(), devopsCiSonarQualityGateConditionVO.getGatesValue());
            sonarQualityGateConditionDTO.setSonarConditionId(qualityGateCondition.getId());
            MapperUtil.resultJudgedInsert(devopsCiSOnarQualityGateConditionMapper, sonarQualityGateConditionDTO, ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_CONDITION_CREATE);
        });
    }

    @Override
    public List<DevopsCiSonarQualityGateConditionVO> listByGateId(Long gateId) {
        return ConvertUtils.convertList(devopsCiSOnarQualityGateConditionMapper.listByGateId(gateId), DevopsCiSonarQualityGateConditionVO.class);
    }

    @Override
    public void deleteByGateId(Long gateId) {
        devopsCiSOnarQualityGateConditionMapper.deleteByGateId(gateId);
    }
}
