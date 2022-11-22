package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateConditionVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateVO;
import io.choerodon.devops.api.vo.sonar.QualityGate;
import io.choerodon.devops.api.vo.sonar.QualityGateCondition;
import io.choerodon.devops.api.vo.sonar.QualityGateResult;
import io.choerodon.devops.app.service.DevopsCiSonarQualityGateConditionService;
import io.choerodon.devops.app.service.DevopsCiSonarQualityGateService;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.dto.DevopsCiSonarQualityGateDTO;
import io.choerodon.devops.infra.enums.DevopsCiSonarGateConditionScopeEnum;
import io.choerodon.devops.infra.feign.operator.SonarClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCiSonarQualityGateMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsCiSonarQualityGateServiceImpl implements DevopsCiSonarQualityGateService {
    @Autowired
    private DevopsCiSonarQualityGateMapper devopsCiSonarQualityGateMapper;
    @Autowired
    private DevopsCiSonarQualityGateConditionService devopsCiSonarQualityGateConditionService;
    @Autowired
    private SonarClientOperator sonarClientOperator;

    @Override
    public void deleteAll(String sonarProjectKey) {
        QualityGate qualityGate = sonarClientOperator.gateShow(sonarProjectKey);
        if (qualityGate != null) {
            sonarClientOperator.deleteQualityGate(sonarProjectKey);

            List<String> conditionSonarIdList = qualityGate.getConditions().stream().map(QualityGateCondition::getId).collect(Collectors.toList());
            devopsCiSonarQualityGateConditionService.deleteBySonarIds(conditionSonarIdList);

            deleteBySonarId(qualityGate.getId());
        }
    }

    public void deleteBySonarId(String sonarId) {
        devopsCiSonarQualityGateMapper.deleteBySonarId(sonarId);
    }

    @Override
    public void createGate(String sonarProjectKey, Long configId, DevopsCiSonarQualityGateVO sonarQualityGateVO) {
        QualityGate qualityGate = createQualityGateOnSonarQube(sonarProjectKey);
        DevopsCiSonarQualityGateDTO devopsCiSonarQualityGateDTO = new DevopsCiSonarQualityGateDTO();
        devopsCiSonarQualityGateDTO.setSonarGateId(qualityGate.getId());
        devopsCiSonarQualityGateDTO.setName(sonarProjectKey);
        devopsCiSonarQualityGateDTO.setConfigId(configId);
        devopsCiSonarQualityGateDTO.setGatesEnable(true);
        devopsCiSonarQualityGateDTO.setGatesBlockAfterFail(sonarQualityGateVO.getGatesBlockAfterFail());
        MapperUtil.resultJudgedInsert(devopsCiSonarQualityGateMapper, devopsCiSonarQualityGateDTO, ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_CREATE);
        devopsCiSonarQualityGateConditionService.createConditions(devopsCiSonarQualityGateDTO.getId(), qualityGate.getId(), sonarQualityGateVO.getSonarQualityGateConditionVOList());
        sonarClientOperator.bindQualityGate(qualityGate.getId(), sonarProjectKey);
    }

    @Override
    public Boolean queryBlock(Long devopsCiSonarConfigId) {
        return devopsCiSonarQualityGateMapper.queryBlockByStepId(devopsCiSonarConfigId);
    }

    @Override
    public DevopsCiSonarQualityGateVO queryBySonarConfigId(Long configId) {
        DevopsCiSonarQualityGateDTO devopsCiSonarQualityGateSearchDTO = new DevopsCiSonarQualityGateDTO();
        devopsCiSonarQualityGateSearchDTO.setConfigId(configId);
        DevopsCiSonarQualityGateDTO devopsCiSonarQualityGateDTO = devopsCiSonarQualityGateMapper.selectOne(devopsCiSonarQualityGateSearchDTO);
        if (devopsCiSonarQualityGateDTO == null) {
            return null;
        }
        DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO = ConvertUtils.convertObject(devopsCiSonarQualityGateDTO, DevopsCiSonarQualityGateVO.class);
        devopsCiSonarQualityGateVO.setSonarQualityGateConditionVOList(devopsCiSonarQualityGateConditionService.listByGateId(devopsCiSonarQualityGateDTO.getId()));
        return devopsCiSonarQualityGateVO;
    }

    @Override
    public QualityGate createQualityGateOnSonarQube(String name) {
        return sonarClientOperator.createQualityGate(name);
    }

    @Override
    public DevopsCiSonarQualityGateVO buildFromSonarResult(QualityGateResult qualityGateResult) {
        DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO = new DevopsCiSonarQualityGateVO();
        devopsCiSonarQualityGateVO.setLevel(qualityGateResult.getLevel());
        List<DevopsCiSonarQualityGateConditionVO> devopsCiSonarQualityGateConditionVOS = new ArrayList<>();
        qualityGateResult.getConditions().forEach(condition -> {
            DevopsCiSonarQualityGateConditionVO devopsCiSonarQualityGateConditionVO = new DevopsCiSonarQualityGateConditionVO();
            devopsCiSonarQualityGateConditionVO.setGatesMetric(condition.getMetric());
            devopsCiSonarQualityGateConditionVO.setGatesScope(DevopsCiSonarGateConditionScopeEnum.parseScope(condition.getMetric()).getScope());
            devopsCiSonarQualityGateConditionVO.setGatesOperator(condition.getOp());
            devopsCiSonarQualityGateConditionVO.setGatesValue(condition.getError());
            devopsCiSonarQualityGateConditionVO.setActualValue(condition.getActual());
            devopsCiSonarQualityGateConditionVOS.add(devopsCiSonarQualityGateConditionVO);
        });
        devopsCiSonarQualityGateVO.setSonarQualityGateConditionVOList(devopsCiSonarQualityGateConditionVOS);
        return devopsCiSonarQualityGateVO;
    }
}
