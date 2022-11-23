package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateVO;
import io.choerodon.devops.app.service.DevopsCiTplSonarQualityGateConditionService;
import io.choerodon.devops.app.service.DevopsCiTplSonarQualityGateService;
import io.choerodon.devops.infra.dto.DevopsCiTplSonarQualityGateDTO;
import io.choerodon.devops.infra.mapper.DevopsCiTplSonarQualityGateMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

@Service
public class DevopsCiTplSonarQualityGateServiceImpl implements DevopsCiTplSonarQualityGateService {

    @Autowired
    private DevopsCiTplSonarQualityGateMapper devopsCiTplSonarQualityGateMapper;
    @Autowired
    private DevopsCiTplSonarQualityGateConditionService devopsCiTplSonarQualityGateConditionService;

    @Override
    public DevopsCiSonarQualityGateVO queryBySonarConfigId(Long sonarConfigId) {
        DevopsCiTplSonarQualityGateDTO devopsCiTplSonarQualityGateSearchDTO = new DevopsCiTplSonarQualityGateDTO();
        devopsCiTplSonarQualityGateSearchDTO.setConfigId(sonarConfigId);
        DevopsCiTplSonarQualityGateDTO devopsCiTplSonarQualityGateDTO = devopsCiTplSonarQualityGateMapper.selectOne(devopsCiTplSonarQualityGateSearchDTO);
        if (devopsCiTplSonarQualityGateDTO == null) {
            return null;
        }
        DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO = ConvertUtils.convertObject(devopsCiTplSonarQualityGateDTO, DevopsCiSonarQualityGateVO.class);
        devopsCiSonarQualityGateVO.setSonarQualityGateConditionVOList(devopsCiTplSonarQualityGateConditionService.listByGateId(devopsCiSonarQualityGateVO.getId()));
        return devopsCiSonarQualityGateVO;
    }
}
