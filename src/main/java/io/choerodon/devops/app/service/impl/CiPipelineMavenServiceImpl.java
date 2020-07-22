package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.CiPipelineMavenService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.CiPipelineMavenDTO;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.mapper.CiPipelineMavenMapper;
import io.choerodon.devops.infra.util.ExceptionUtil;
import io.choerodon.devops.infra.util.MavenSettingsUtil;

/**
 * @author scp
 * @date 2020/7/22
 */
@Service
public class CiPipelineMavenServiceImpl implements CiPipelineMavenService {
    @Autowired
    private CiPipelineMavenMapper ciPipelineMavenMapper;
    @Autowired
    private AppServiceService appServiceService;

    @Transactional(rollbackFor = Exception.class)
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createOrUpdate(Long nexusRepoId, Long gitlabPipelineId, String jobName, String token, MultipartFile file) {
        ExceptionUtil.wrapExWithCiEx(() -> {
            AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(Objects.requireNonNull(token));
            if (appServiceDTO == null) {
                throw new DevopsCiInvalidException("error.token.invalid");
            }
            CiPipelineMavenDTO ciPipelineMavenDTO;
            try {
                ciPipelineMavenDTO = MavenSettingsUtil.parsePom(new String(file.getBytes(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new DevopsCiInvalidException("error.failed.to.read.pom.file");
            }
            ciPipelineMavenDTO.setGitlabPipelineId(Objects.requireNonNull(gitlabPipelineId));
            ciPipelineMavenDTO.setNexusRepoId(Objects.requireNonNull(nexusRepoId));
            ciPipelineMavenDTO.setJobName(Objects.requireNonNull(jobName));
            createOrUpdate(ciPipelineMavenDTO);
        });
    }

    @Override
    public CiPipelineMavenDTO queryByGitlabPipelineId(Long gitlabPipelineId, String jobName) {
        CiPipelineMavenDTO ciPipelineMavenDTO = new CiPipelineMavenDTO();
        ciPipelineMavenDTO.setGitlabPipelineId(gitlabPipelineId);
        ciPipelineMavenDTO.setJobName(jobName);
        return ciPipelineMavenMapper.selectOne(ciPipelineMavenDTO);
    }
}
