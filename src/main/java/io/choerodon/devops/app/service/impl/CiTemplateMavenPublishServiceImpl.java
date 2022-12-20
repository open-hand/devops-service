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
import io.choerodon.devops.app.service.CiTemplateMavenPublishService;
import io.choerodon.devops.infra.dto.CiTemplateMavenPublishDTO;
import io.choerodon.devops.infra.mapper.CiTemplateMavenPublishMapper;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * devops_ci_template_maven_publish(CiTemplateMavenPublish)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-15 14:06:09
 */
@Service
public class CiTemplateMavenPublishServiceImpl implements CiTemplateMavenPublishService {
    @Autowired
    private CiTemplateMavenPublishMapper ciTemplateMavenPublishMapper;


    @Override
    public CiTemplateMavenPublishDTO queryByStepId(Long stepId) {
        CiTemplateMavenPublishDTO ciTemplateMavenPublishDTO = new CiTemplateMavenPublishDTO();
        ciTemplateMavenPublishDTO.setCiTemplateStepId(stepId);
        return ciTemplateMavenPublishMapper.selectOne(ciTemplateMavenPublishDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(Long id, CiTemplateMavenPublishDTO mavenBuildConfig) {
        CiTemplateMavenPublishDTO ciTemplateMavenPublishDTO = voToDto(mavenBuildConfig);
        ciTemplateMavenPublishDTO.setId(null);
        ciTemplateMavenPublishDTO.setCiTemplateStepId(id);
        ciTemplateMavenPublishMapper.insertSelective(ciTemplateMavenPublishDTO);
    }

    @Override
    public CiTemplateMavenPublishDTO dtoToVo(CiTemplateMavenPublishDTO ciTemplateMavenPublishDTO) {
        if (!StringUtils.isEmpty(ciTemplateMavenPublishDTO.getNexusMavenRepoIdStr())) {
            ciTemplateMavenPublishDTO.setNexusMavenRepoIds(JsonHelper.unmarshalByJackson(ciTemplateMavenPublishDTO.getNexusMavenRepoIdStr(), new TypeReference<Set<Long>>() {
            }));
        }
        if (!StringUtils.isEmpty(ciTemplateMavenPublishDTO.getRepoStr())) {
            ciTemplateMavenPublishDTO.setRepos(JsonHelper.unmarshalByJackson(ciTemplateMavenPublishDTO.getRepoStr(), new TypeReference<List<MavenRepoVO>>() {
            }));
        }
        if (org.springframework.util.StringUtils.hasText(ciTemplateMavenPublishDTO.getTargetRepoStr())) {
            ciTemplateMavenPublishDTO.setTargetRepo(JsonHelper.unmarshalByJackson(ciTemplateMavenPublishDTO.getTargetRepoStr(), MavenRepoVO.class));
        }
        return ciTemplateMavenPublishDTO;
    }

    @Override
    public CiTemplateMavenPublishDTO voToDto(CiTemplateMavenPublishDTO ciTemplateMavenPublishDTO) {
        if (!CollectionUtils.isEmpty(ciTemplateMavenPublishDTO.getNexusMavenRepoIds())) {
            ciTemplateMavenPublishDTO.setNexusMavenRepoIdStr(JsonHelper.marshalByJackson(ciTemplateMavenPublishDTO.getNexusMavenRepoIds()));
        }
        if (!CollectionUtils.isEmpty(ciTemplateMavenPublishDTO.getRepos())) {
            ciTemplateMavenPublishDTO.setRepoStr(JsonHelper.marshalByJackson(ciTemplateMavenPublishDTO.getRepos()));
        }
        if (ciTemplateMavenPublishDTO.getTargetRepo() != null) {
            ciTemplateMavenPublishDTO.setTargetRepoStr(JsonHelper.marshalByJackson(ciTemplateMavenPublishDTO.getTargetRepo()));
        }
        return ciTemplateMavenPublishDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTemplateId(Long templateStepId) {
        CiTemplateMavenPublishDTO ciTemplateMavenPublishDTO = new CiTemplateMavenPublishDTO();
        ciTemplateMavenPublishDTO.setCiTemplateStepId(templateStepId);
        ciTemplateMavenPublishMapper.delete(ciTemplateMavenPublishDTO);
    }
}

