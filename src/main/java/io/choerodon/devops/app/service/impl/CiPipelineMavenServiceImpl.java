package io.choerodon.devops.app.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.CiPipelineMavenService;
import io.choerodon.devops.infra.dto.CiPipelineMavenDTO;
import io.choerodon.devops.infra.mapper.CiPipelineMavenMapper;

/**
 * @author scp
 * @date 2020/7/22
 * @description
 */
@Service
public class CiPipelineMavenServiceImpl implements CiPipelineMavenService {
    @Autowired
    private CiPipelineMavenMapper ciPipelineMavenMapper;
    @Override
    public void createOrUpdate(CiPipelineMavenDTO ciPipelineMavenDTO) {
        CiPipelineMavenDTO oldCiPipelineMavenDTO = queryByGitlabPipelineId(ciPipelineMavenDTO.getGitlabPipelineId(), ciPipelineMavenDTO.getJobName());
        if (oldCiPipelineMavenDTO == null || oldCiPipelineMavenDTO.getId() == null) {
            CiPipelineMavenDTO ciPipelineImageDTO = new CiPipelineMavenDTO();
            if (ciPipelineMavenMapper.insertSelective(ciPipelineImageDTO) != 1) {
                throw new CommonException("error.create.maven.record");
            }
        } else {
            BeanUtils.copyProperties(ciPipelineMavenDTO, oldCiPipelineMavenDTO);
            if (ciPipelineMavenMapper.updateByPrimaryKey(oldCiPipelineMavenDTO) != 1) {
                throw new CommonException("error.update.maven.record");
            }
        }
    }

    @Override
    public CiPipelineMavenDTO queryByGitlabPipelineId(Long gitlabPipelineId, String jobName) {
        CiPipelineMavenDTO ciPipelineMavenDTO = new CiPipelineMavenDTO();
        ciPipelineMavenDTO.setGitlabPipelineId(gitlabPipelineId);
        ciPipelineMavenDTO.setJobName(jobName);
        return ciPipelineMavenMapper.selectOne(ciPipelineMavenDTO);
    }
}
