package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.MavenRepoVO;
import io.choerodon.devops.app.service.CiTemplateMavenBuildService;
import io.choerodon.devops.infra.dto.CiTemplateMavenBuildDTO;
import io.choerodon.devops.infra.mapper.CiTemplateMavenBuildMapper;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * devops_ci_template_maven_build(CiTemplateMavenBuild)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-15 14:06:08
 */
@Service
public class CiTemplateMavenBuildServiceImpl implements CiTemplateMavenBuildService {
    @Autowired
    private CiTemplateMavenBuildMapper ciTemplateMavenBuildMapper;


    @Override
    public CiTemplateMavenBuildDTO baseQueryById(Long stepId) {
        CiTemplateMavenBuildDTO ciTemplateMavenBuildDTO = new CiTemplateMavenBuildDTO();
        ciTemplateMavenBuildDTO.setCiTemplateStepId(stepId);
        return ciTemplateMavenBuildMapper.selectOne(ciTemplateMavenBuildDTO);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(Long templateStepId, CiTemplateMavenBuildDTO mavenBuildConfig) {
        CiTemplateMavenBuildDTO devopsCiMavenBuildConfigDTO = voToDto(mavenBuildConfig);
        devopsCiMavenBuildConfigDTO.setId(null);
        devopsCiMavenBuildConfigDTO.setCiTemplateStepId(templateStepId);
        ciTemplateMavenBuildMapper.insertSelective(devopsCiMavenBuildConfigDTO);
    }


    @Override
    public CiTemplateMavenBuildDTO dtoToVo(CiTemplateMavenBuildDTO ciTemplateMavenBuildDTO) {
        if (!StringUtils.isEmpty(ciTemplateMavenBuildDTO.getNexusMavenRepoIdStr())) {
            ciTemplateMavenBuildDTO.setNexusMavenRepoIds(JsonHelper.unmarshalByJackson(ciTemplateMavenBuildDTO.getNexusMavenRepoIdStr(), new TypeReference<Set<Long>>() {
            }));
        }
        if (!StringUtils.isEmpty(ciTemplateMavenBuildDTO.getRepoStr())) {
            ciTemplateMavenBuildDTO.setRepos(JsonHelper.unmarshalByJackson(ciTemplateMavenBuildDTO.getRepoStr(), new TypeReference<List<MavenRepoVO>>() {
            }));
        }
        return ciTemplateMavenBuildDTO;
    }

    @Override
    public CiTemplateMavenBuildDTO voToDto(CiTemplateMavenBuildDTO ciTemplateMavenBuildDTO) {
        if (!CollectionUtils.isEmpty(ciTemplateMavenBuildDTO.getNexusMavenRepoIds())) {
            ciTemplateMavenBuildDTO.setNexusMavenRepoIdStr(JsonHelper.marshalByJackson(ciTemplateMavenBuildDTO.getNexusMavenRepoIds()));
        }
        if (!CollectionUtils.isEmpty(ciTemplateMavenBuildDTO.getRepos())) {
            ciTemplateMavenBuildDTO.setRepoStr(JsonHelper.marshalByJackson(ciTemplateMavenBuildDTO.getRepos()));
        }
        return ciTemplateMavenBuildDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTemplateStepId(Long templateStepId) {
        CiTemplateMavenBuildDTO templateMavenBuildDTO = new CiTemplateMavenBuildDTO();
        templateMavenBuildDTO.setCiTemplateStepId(templateStepId);
        ciTemplateMavenBuildMapper.delete(templateMavenBuildDTO);
    }
}

