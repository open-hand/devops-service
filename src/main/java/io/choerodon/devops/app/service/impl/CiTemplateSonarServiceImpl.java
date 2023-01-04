package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.app.service.CiTemplateMavenBuildService;
import io.choerodon.devops.app.service.CiTemplateSonarService;
import io.choerodon.devops.infra.dto.CiTemplateMavenBuildDTO;
import io.choerodon.devops.infra.dto.CiTemplateSonarDTO;
import io.choerodon.devops.infra.dto.DevopsCiTplSonarQualityGateConditionDTO;
import io.choerodon.devops.infra.dto.DevopsCiTplSonarQualityGateDTO;
import io.choerodon.devops.infra.mapper.CiTemplateSonarMapper;
import io.choerodon.devops.infra.mapper.DevopsCiTplSonarQualityGateConditionMapper;
import io.choerodon.devops.infra.mapper.DevopsCiTplSonarQualityGateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * devops_ci_template_sonar(CiTemplateSonar)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:19
 */
@Service
public class CiTemplateSonarServiceImpl implements CiTemplateSonarService {
    @Autowired
    private CiTemplateSonarMapper ciTemplateSonarmapper;

    @Autowired
    private DevopsCiTplSonarQualityGateMapper devopsCiTplSonarQualityGateMapper;

    @Autowired
    private DevopsCiTplSonarQualityGateConditionMapper devopsCiTplSonarQualityGateConditionMapper;

    @Autowired
    private CiTemplateMavenBuildService ciTemplateMavenBuildService;


    @Override
    public CiTemplateSonarDTO queryByStepId(Long stepId) {
        CiTemplateSonarDTO ciTemplateSonarDTO = new CiTemplateSonarDTO();
        ciTemplateSonarDTO.setCiTemplateStepId(stepId);
        return ciTemplateSonarmapper.selectOne(ciTemplateSonarDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(Long templateStepId, CiTemplateSonarDTO ciTemplateSonarDTO) {
        ciTemplateSonarDTO.setId(null);
        ciTemplateSonarDTO.setCiTemplateStepId(templateStepId);
        ciTemplateSonarmapper.insertSelective(ciTemplateSonarDTO);
        //插入Sonar的配置
        DevopsCiTplSonarQualityGateDTO devopsCiTplSonarQualityGateDTO = ciTemplateSonarDTO.getDevopsCiSonarQualityGateVO();
        if (devopsCiTplSonarQualityGateDTO != null) {
            if (devopsCiTplSonarQualityGateDTO.getGatesEnable()) {
                devopsCiTplSonarQualityGateDTO.setId(null);
                devopsCiTplSonarQualityGateDTO.setConfigId(ciTemplateSonarDTO.getId());
                devopsCiTplSonarQualityGateMapper.insertSelective(devopsCiTplSonarQualityGateDTO);
                //继续插入DevopsCiTplSonarQualityGateConditionDTO
                List<DevopsCiTplSonarQualityGateConditionDTO> sonarQualityGateConditionVOList = devopsCiTplSonarQualityGateDTO.getSonarQualityGateConditionVOList();
                if (!CollectionUtils.isEmpty(sonarQualityGateConditionVOList)) {
                    sonarQualityGateConditionVOList.forEach(devopsCiTplSonarQualityGateConditionDTO -> {
                        devopsCiTplSonarQualityGateConditionDTO.setId(null);
                        devopsCiTplSonarQualityGateConditionDTO.setGateId(devopsCiTplSonarQualityGateDTO.getId());
                    });
                    devopsCiTplSonarQualityGateConditionMapper.insertList(sonarQualityGateConditionVOList);
                }
            }
        }
        // 保存mvn配置
        CiTemplateMavenBuildDTO mavenBuildConfig = ciTemplateSonarDTO.getMavenBuildConfig();
        if (mavenBuildConfig != null) {
            ciTemplateMavenBuildService.baseCreate(templateStepId, mavenBuildConfig);
        }
    }

    @Override
    public void deleteByTemplateStepId(Long templateStepId) {
        CiTemplateSonarDTO ciTemplateSonarDTO = new CiTemplateSonarDTO();
        ciTemplateSonarDTO.setCiTemplateStepId(templateStepId);
        List<CiTemplateSonarDTO> ciTemplateSonarDTOS = ciTemplateSonarmapper.select(ciTemplateSonarDTO);
        if (!CollectionUtils.isEmpty(ciTemplateSonarDTOS)) {
            ciTemplateSonarDTOS.forEach(ciTemplateSonarDTO1 -> {
                DevopsCiTplSonarQualityGateDTO devopsCiTplSonarQualityGateDTO = new DevopsCiTplSonarQualityGateDTO();
                devopsCiTplSonarQualityGateDTO.setConfigId(ciTemplateSonarDTO1.getId());
                List<DevopsCiTplSonarQualityGateDTO> devopsCiTplSonarQualityGateDTOS = devopsCiTplSonarQualityGateMapper.select(devopsCiTplSonarQualityGateDTO);
                if (CollectionUtils.isEmpty(devopsCiTplSonarQualityGateDTOS)) {
                    return;
                }
                devopsCiTplSonarQualityGateDTOS.forEach(devopsCiTplSonarQualityGateDTO1 -> {
                    DevopsCiTplSonarQualityGateConditionDTO devopsCiTplSonarQualityGateConditionDTO = new DevopsCiTplSonarQualityGateConditionDTO();
                    devopsCiTplSonarQualityGateConditionDTO.setGateId(devopsCiTplSonarQualityGateDTO1.getId());
                    devopsCiTplSonarQualityGateConditionMapper.delete(devopsCiTplSonarQualityGateConditionDTO);
                });
                devopsCiTplSonarQualityGateMapper.delete(devopsCiTplSonarQualityGateDTO);

            });
        }
        ciTemplateSonarmapper.delete(ciTemplateSonarDTO);

        // 删除mvn配置
        ciTemplateMavenBuildService.deleteByTemplateStepId(templateStepId);
    }
}

