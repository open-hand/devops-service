package io.choerodon.devops.app.eventhandler.pipeline.exec;

import java.util.List;

import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.harbor.ProdImageInfoVO;
import io.choerodon.devops.api.vo.pipeline.AppDeployConfigVO;
import io.choerodon.devops.api.vo.pipeline.CiDeployDeployCfgVO;
import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusRepoDTO;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.CiCommandTypeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.DESEncryptUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/4 16:03
 */
@Service
public class DeploymentDeployCommandHandler extends AbstractAppDeployCommandHandler {

    @Autowired
    private CiDeployDeployCfgService ciDeployDeployCfgService;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;
    @Autowired
    private CiPipelineImageService ciPipelineImageService;
    @Autowired
    private RdupmClientOperator rdupmClientOperator;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private CiPipelineMavenService ciPipelineMavenService;
    @Autowired
    private DevopsDeployGroupService devopsDeployGroupService;

    @Override
    public CiCommandTypeEnum getType() {
        return CiCommandTypeEnum.DEPLOYMENT_DEPLOY;
    }

    @Override
    protected void deployApp(AppServiceDTO appServiceDTO,
                             StringBuilder log,
                             AppDeployConfigVO appDeployConfigVO,
                             Long projectId,
                             Long appServiceId,
                             Long envId,
                             String appCode,
                             String appName,
                             DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO) {
        Long gitlabPipelineId = devopsCiPipelineRecordDTO.getGitlabPipelineId();
        Long objectId = null;
        if (DeployTypeEnum.UPDATE.value().equals(appDeployConfigVO.getDeployType())) {
            // 3. 如果是更新应用，先判断应用是否存在。不存在则跳过。
            DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(appDeployConfigVO.getAppId());
            if (devopsDeployAppCenterEnvDTO == null) {
                log.append("App: ").append(appDeployConfigVO.getAppId()).append(" not found, is deleted? Skip this task.").append(System.lineSeparator());
                return;
            }
            objectId = devopsDeployAppCenterEnvDTO.getObjectId();
        }
        CiDeployDeployCfgVO ciDeployDeployCfgVO = ciDeployDeployCfgService.queryConfigVoById(appDeployConfigVO.getId());

        DevopsDeployGroupAppConfigVO devopsDeployGroupAppConfigVO = ciDeployDeployCfgVO.getAppConfig();
        List<DevopsDeployGroupContainerConfigVO> devopsDeployGroupContainerConfigVOS = ciDeployDeployCfgVO.getContainerConfig();
        DevopsDeployGroupVO devopsDeployGroupVO = new DevopsDeployGroupVO(appName,
                appCode,
                projectId,
                envId,
                devopsDeployGroupAppConfigVO,
                devopsDeployGroupContainerConfigVOS,
                objectId);


        devopsDeployGroupContainerConfigVOS.forEach(config -> {
            if (config.getPipelineJobName() != null) {
                if (RdupmTypeEnum.DOCKER.value().equals(config.getType())) {
                    fillDockerConfig(projectId, appServiceId, gitlabPipelineId, config);

                } else {
                    CiPipelineMavenDTO ciPipelineMavenDTO = ciPipelineMavenService.queryByGitlabPipelineId(appServiceId,
                            gitlabPipelineId,
                            config.getPipelineJobName());
                    ProdJarInfoVO prodJarInfoVO = new ProdJarInfoVO(ciPipelineMavenDTO.getNexusRepoId(),
                            ciPipelineMavenDTO.getGroupId(),
                            ciPipelineMavenDTO.getArtifactId(),
                            getMavenVersion(ciPipelineMavenDTO.getVersion()));

                    if (ciPipelineMavenDTO.getNexusRepoId() == null) {
                        prodJarInfoVO.setDownloadUrl(ciPipelineMavenDTO.calculateDownloadUrl());
                        prodJarInfoVO.setUsername(DESEncryptUtil.decode(ciPipelineMavenDTO.getUsername()));
                        prodJarInfoVO.setPassword(DESEncryptUtil.decode(ciPipelineMavenDTO.getPassword()));
                    }
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
                    C7nNexusRepoDTO c7nNexusRepoDTO = rdupmClientOperator.getMavenRepo(projectDTO.getOrganizationId(),
                            projectId,
                            ciPipelineMavenDTO.getNexusRepoId());

                    prodJarInfoVO.setNexusId(c7nNexusRepoDTO.getConfigId());


                    DevopsDeployGroupJarDeployVO devopsDeployGroupJarDeployVO = new DevopsDeployGroupJarDeployVO();
                    devopsDeployGroupJarDeployVO.setProdJarInfoVO(prodJarInfoVO);
                    devopsDeployGroupJarDeployVO.setSourceType(AppSourceType.CURRENT_PROJECT.getValue());
                    config.setPipelineJobName(null);
                    config.setSourceType(AppSourceType.CURRENT_PROJECT.getValue());
                    config.setJarDeployVO(devopsDeployGroupJarDeployVO);
                }
            }
        });


        DevopsDeployAppCenterEnvVO devopsDeployAppCenterEnvVO = devopsDeployGroupService.createOrUpdate(projectId,
                devopsDeployGroupVO,
                ciDeployDeployCfgVO.getDeployType(),
                false,
                true);
        Long commandId = devopsDeployAppCenterEnvVO.getCommandId();
        Long appId;
        if (DeployTypeEnum.CREATE.value().equals(ciDeployDeployCfgVO.getDeployType())) {
            appId = devopsDeployAppCenterEnvVO.getId();
            ciDeployDeployCfgVO.setAppId(appId);
            ciDeployDeployCfgVO.setDeployType(DeployTypeEnum.UPDATE.value());
            ciDeployDeployCfgService.baseUpdateByVO(ciDeployDeployCfgVO);
        }

        log.append("Deploy app success.").append(System.lineSeparator());
    }

    @Override
    protected AppDeployConfigVO queryConfigById(Long configId) {
        CiDeployDeployCfgDTO ciDeployDeployCfgDTO = ciDeployDeployCfgService.queryConfigById(configId);
        return ConvertUtils.convertObject(ciDeployDeployCfgDTO, CiDeployDeployCfgVO.class);
    }

    private String getMavenVersion(String version) {
        if (version.contains(BaseConstants.Symbol.SLASH)) {
            return version.split(BaseConstants.Symbol.SLASH)[0];
        } else {
            return version;
        }
    }

    protected void fillDockerConfig(Long projectId, Long appServiceId, Long gitlabPipelineId, DevopsDeployGroupContainerConfigVO config) {
        CiPipelineImageDTO ciPipelineImageDTO = ciPipelineImageService.queryByGitlabPipelineId(appServiceId,
                gitlabPipelineId,
                config.getPipelineJobName());
        HarborRepoDTO harborRepoDTO = rdupmClientOperator.queryHarborRepoConfigById(projectId,
                ciPipelineImageDTO.getHarborRepoId(),
                ciPipelineImageDTO.getRepoType());

        DevopsDeployGroupDockerDeployVO dockerDeployVO = new DevopsDeployGroupDockerDeployVO();
        dockerDeployVO.setSourceType(AppSourceType.CURRENT_PROJECT.getValue());

        int index = ciPipelineImageDTO.getImageTag().lastIndexOf(":");
        String imageName = ciPipelineImageDTO.getImageTag().substring(0, index);
        String tagName = ciPipelineImageDTO.getImageTag().substring(index + 1);

        ProdImageInfoVO prodImageInfoVO = new ProdImageInfoVO(harborRepoDTO.getHarborRepoConfig().getRepoName(),
                harborRepoDTO.getRepoType(),
                harborRepoDTO.getHarborRepoConfig().getRepoId(),
                imageName,
                tagName,
                Boolean.TRUE.toString().equals(harborRepoDTO.getHarborRepoConfig().getIsPrivate()),
                ciPipelineImageDTO.getImageTag());
        dockerDeployVO.setImageInfo(prodImageInfoVO);
        config.setPipelineJobName(null);
        config.setSourceType(AppSourceType.CURRENT_PROJECT.getValue());
        config.setDockerDeployVO(dockerDeployVO);
    }


}
