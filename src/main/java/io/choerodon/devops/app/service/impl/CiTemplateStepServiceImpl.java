package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.template.CiTemplateDockerVO;
import io.choerodon.devops.api.vo.template.CiTemplateSonarVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiTemplateStepService;
import io.choerodon.devops.infra.dto.CiTemplateDockerDTO;
import io.choerodon.devops.infra.dto.CiTemplateSonarDTO;
import io.choerodon.devops.infra.dto.CiTemplateStepDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.mapper.CiTemplateDockerMapper;
import io.choerodon.devops.infra.mapper.CiTemplateSonarMapper;
import io.choerodon.devops.infra.mapper.CiTemplateStepMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 流水线步骤模板(CiTemplateStep)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */
@Service
public class CiTemplateStepServiceImpl implements CiTemplateStepService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CiTemplateStepServiceImpl.class);

    @Autowired
    private CiTemplateStepMapper ciTemplateStepMapper;

    @Autowired
    private CiTemplateDockerMapper ciTemplateDockerMapper;

    @Autowired
    private CiTemplateSonarMapper ciTemplateSonarMapper;


    @Override
    public List<CiTemplateStepVO> listByJobIds(Set<Long> jobIds) {
        return ciTemplateStepMapper.listByJobIds(jobIds);
    }

    @Override
    public CiTemplateStepVO queryCiTemplateStepById(Long ciTemplateStepId) {
        CiTemplateStepDTO ciTemplateStepDTO = ciTemplateStepMapper.selectByPrimaryKey(ciTemplateStepId);
        if (ciTemplateStepDTO == null) {
            return new CiTemplateStepVO();
        }
        CiTemplateStepVO reCiTemplateStepVO = ConvertUtils.convertObject(ciTemplateStepDTO, CiTemplateStepVO.class);
        if (reCiTemplateStepVO.getBuiltIn()) {
            return reCiTemplateStepVO;
        }
        switch (DevopsCiStepTypeEnum.valueOf(ciTemplateStepDTO.getType())) {
            case DOCKER_BUILD:
                fillDockerConfig(reCiTemplateStepVO);
                break;
            case SONAR:
                fillSonarConfig(reCiTemplateStepVO);
                break;

            default:
                LOGGER.info("type not matched");
        }
        return reCiTemplateStepVO;
    }

    private void fillSonarConfig(CiTemplateStepVO reCiTemplateStepVO) {
        CiTemplateSonarDTO ciTemplateSonarDTO = new CiTemplateSonarDTO();
        ciTemplateSonarDTO.setCiTemplateStepId(reCiTemplateStepVO.getCiTemplateJobId());
        CiTemplateSonarDTO templateSonarDTO = ciTemplateSonarMapper.selectOne(ciTemplateSonarDTO);
        if (templateSonarDTO != null) {
            reCiTemplateStepVO.setCiTemplateSonarVO(ConvertUtils.convertObject(templateSonarDTO, CiTemplateSonarVO.class));
        }
    }

    private void fillDockerConfig(CiTemplateStepVO reCiTemplateStepVO) {
        CiTemplateDockerDTO ciTemplateDockerDTO = new CiTemplateDockerDTO();
        ciTemplateDockerDTO.setCiTemplateStepId(reCiTemplateStepVO.getId());
        CiTemplateDockerDTO templateDockerDTO = ciTemplateDockerMapper.selectOne(ciTemplateDockerDTO);
        if (templateDockerDTO != null) {
            reCiTemplateStepVO.setCiTemplateDockerVO(ConvertUtils.convertObject(templateDockerDTO, CiTemplateDockerVO.class));
        }
    }
}

