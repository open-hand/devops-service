package io.choerodon.devops.app.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CiPipelineImageVO;
import io.choerodon.devops.api.vo.ImageRepoInfoVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.CiPipelineImageService;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.CiPipelineImageDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
import io.choerodon.devops.infra.enums.DevopsRegistryRepoType;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.CiPipelineImageMapper;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
import io.choerodon.devops.infra.util.ExceptionUtil;

/**
 * @author scp
 * @date 2020/7/21
 * @description
 */
@Service
public class CiPipelineImageServiceImpl implements CiPipelineImageService {

    @Autowired
    private CiPipelineImageMapper ciPipelineImageMapper;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private RdupmClientOperator rdupmClientOperator;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createOrUpdate(CiPipelineImageVO ciPipelineImageVO) {
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(ciPipelineImageVO.getToken());
        if (appServiceDTO == null) {
            throw new DevopsCiInvalidException("error.token.invalid");
        }
        Long appServiceId = appServiceDTO.getId();

        // 异常包装
        ExceptionUtil.wrapExWithCiEx(() -> {

            CiPipelineImageDTO oldCiPipelineImageDTO = queryByGitlabPipelineId(appServiceId, ciPipelineImageVO.getGitlabPipelineId(), ciPipelineImageVO.getJobName());
            if (oldCiPipelineImageDTO == null || oldCiPipelineImageDTO.getId() == null) {
                CiPipelineImageDTO ciPipelineImageDTO = new CiPipelineImageDTO();
                BeanUtils.copyProperties(ciPipelineImageVO, ciPipelineImageDTO);
                ciPipelineImageDTO.setAppServiceId(appServiceId);
                if (ciPipelineImageMapper.insertSelective(ciPipelineImageDTO) != 1) {
                    throw new CommonException("error.create.image.record");
                }
            } else {
                BeanUtils.copyProperties(ciPipelineImageVO, oldCiPipelineImageDTO);
                if (ciPipelineImageMapper.updateByPrimaryKey(oldCiPipelineImageDTO) != 1) {
                    throw new CommonException("error.update.image.record");
                }
            }
        });
    }

    @Override
    public CiPipelineImageDTO queryByGitlabPipelineId(Long appServiceId, Long gitlabPipelineId, String jobName) {
        CiPipelineImageDTO ciPipelineImageDTO = new CiPipelineImageDTO();
        ciPipelineImageDTO.setGitlabPipelineId(gitlabPipelineId);
        ciPipelineImageDTO.setAppServiceId(appServiceId);
        ciPipelineImageDTO.setJobName(jobName);
        return ciPipelineImageMapper.selectOne(ciPipelineImageDTO);
    }

    @Override
    public ImageRepoInfoVO queryRewriteRepoInfoScript(Long projectId, String token, String repoType, Long repoId) {
        ImageRepoInfoVO imageRepoInfoVO = null;
        try {
            AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(token);
            if (appServiceDTO == null) {
                throw new CommonException("error.app.svc.not.found");
            }
            CommonExAssertUtil.assertTrue((projectId.equals(appServiceDTO.getProjectId())), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
            HarborRepoDTO harborRepoDTO = rdupmClientOperator.queryHarborRepoConfigById(projectId, repoId, repoType);
            String dockerRegistry;
            String groupName;
            String dockerUsername;
            String dockerPassword;
            if (DevopsRegistryRepoType.CUSTOM_REPO.getType().equals(repoType)) {
                dockerRegistry = harborRepoDTO.getHarborRepoConfig().getRepoUrl();
                groupName = harborRepoDTO.getHarborRepoConfig().getRepoName();
                dockerUsername = harborRepoDTO.getHarborRepoConfig().getLoginName();
                dockerPassword = harborRepoDTO.getHarborRepoConfig().getPassword();

            } else {
                dockerRegistry = harborRepoDTO.getHarborRepoConfig().getRepoUrl();
                groupName = harborRepoDTO.getHarborRepoConfig().getRepoName();
                dockerUsername = harborRepoDTO.getPushRobot().getName();
                dockerPassword = harborRepoDTO.getPushRobot().getToken();
            }
            imageRepoInfoVO = new ImageRepoInfoVO();
            imageRepoInfoVO.setDockerRegistry(trimPrefix(dockerRegistry));
            imageRepoInfoVO.setGroupName(groupName);
            imageRepoInfoVO.setDockerUsername(dockerUsername);
            imageRepoInfoVO.setDockerPassword(dockerPassword);
            imageRepoInfoVO.setRepoType(repoType);
            imageRepoInfoVO.setHarborRepoId(String.valueOf(repoId));
        } catch (Exception e) {
            throw new DevopsCiInvalidException(e);
        }

        return imageRepoInfoVO;
    }

    @Override
    public ImageRepoInfoVO queryImageRepoInfo(String token, Long gitlabPipelineId) {
        String repoType = null;
        Long repoId = null;
        String dockerRegistry = null;
        String groupName = null;
        try {
            AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(token);
            if (appServiceDTO == null) {
                throw new CommonException("error.app.svc.not.found");
            }
            Long projectId = appServiceDTO.getProjectId();

            CiPipelineImageDTO ciPipelineImageDTO = ciPipelineImageMapper.queryPipelineLatestImage(appServiceDTO.getId(), gitlabPipelineId);
            repoType = ciPipelineImageDTO.getRepoType();
            repoId = ciPipelineImageDTO.getHarborRepoId();

            HarborRepoDTO harborRepoDTO = rdupmClientOperator.queryHarborRepoConfigById(projectId, repoId, repoType);
            dockerRegistry = harborRepoDTO.getHarborRepoConfig().getRepoUrl();
            groupName = harborRepoDTO.getHarborRepoConfig().getRepoName();
        } catch (Exception e) {
            throw new DevopsCiInvalidException(e);
        }

        return new ImageRepoInfoVO(String.valueOf(repoId), repoType, trimPrefix(dockerRegistry), groupName);
    }

    private String trimPrefix(String dockerRegistry) {
        String dockerUrl = dockerRegistry.replace("http://", "").replace("https://", "");
        return dockerUrl.endsWith("/") ? dockerUrl.substring(0, dockerUrl.length() - 1) : dockerUrl;
    }
}
