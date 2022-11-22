package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateVO;
import io.choerodon.devops.api.vo.sonar.QualityGate;
import io.choerodon.devops.app.service.DevopsCiSonarQualityGateConditionService;
import io.choerodon.devops.app.service.DevopsCiSonarQualityGateService;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.dto.DevopsCiSonarQualityGateDTO;
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
    public void deleteAll(Long id) {
        DevopsCiSonarQualityGateDTO qualityGateDTO = devopsCiSonarQualityGateMapper.selectByPrimaryKey(id);
        if (qualityGateDTO != null) {
            sonarClientOperator.deleteQualityGate(qualityGateDTO.getSonarGateId());
            devopsCiSonarQualityGateConditionService.deleteByGateId(qualityGateDTO.getId());
        }
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
        QualityGate qualityGate = sonarClientOperator.gateShow(name);
        if (qualityGate != null) {
            sonarClientOperator.deleteQualityGate(name);
        }
        return sonarClientOperator.createQualityGate(name);
    }
}
