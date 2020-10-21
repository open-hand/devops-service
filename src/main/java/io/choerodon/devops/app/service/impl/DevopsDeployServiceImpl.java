package io.choerodon.devops.app.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.schmizz.sshj.SSHClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import sun.misc.BASE64Decoder;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.AppServiceDeployVO;
import io.choerodon.devops.api.vo.deploy.HostDeployConfigVO;
import io.choerodon.devops.api.vo.hrdsCode.HarborC7nRepoImageTagVo;
import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.DevopsDeployRecordService;
import io.choerodon.devops.app.service.DevopsDeployService;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.C7nImageDeployDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusDeployDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.HostDeployType;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.DevopsHostMapper;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.SshUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/10/19 16:04
 */
@Service
public class DevopsDeployServiceImpl implements DevopsDeployService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsDeployServiceImpl.class);
    private static final BASE64Decoder decoder = new BASE64Decoder();

    private static final String ERROR_IMAGE_TAG_NOT_FOUND = "error.image.tag.not.found";
    private static final String ERROR_JAR_VERSION_NOT_FOUND = "error.jar.version.not.found";
    private static final String ERROR_DEPLOY_JAR_FAILED = "error.deploy.jar.failed";


    @Autowired
    private RdupmClientOperator rdupmClientOperator;
    @Autowired
    private SshUtil sshUtil;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsHostMapper devopsHostMapper;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;

    @Override
    public void hostDeploy(Long projectId, HostDeployConfigVO hostDeployConfigVO) {
        if (HostDeployType.IMAGED_DEPLOY.getValue().equals(hostDeployConfigVO.getHostDeployType())) {
            hostImagedeploy(projectId, hostDeployConfigVO);
        } else if (HostDeployType.JAR_DEPLOY.getValue().equals(hostDeployConfigVO.getHostDeployType())) {
            hostJarDeploy(projectId, hostDeployConfigVO);
        } else if (HostDeployType.ENV_DEPLOY.getValue().equals(hostDeployConfigVO.getHostDeployType())) {
            AppServiceDeployVO appServiceDeployVO = hostDeployConfigVO.getAppServiceDeployVO();
            appServiceDeployVO.setType("create");
            appServiceInstanceService.createOrUpdate(projectId, appServiceDeployVO, false);
        }
    }

    private void hostJarDeploy(Long projectId, HostDeployConfigVO hostDeployConfigVO) {
        LOGGER.info("========================================");
        LOGGER.info("start jar deploy cd host job,projectId:{}", projectId);
        SSHClient ssh = new SSHClient();
        StringBuilder log = new StringBuilder();
        HostDeployConfigVO.JarDeploy jarDeploy;
        C7nNexusComponentDTO c7nNexusComponentDTO = new C7nNexusComponentDTO();

        try {
            // 0.1 查询部署信息

            jarDeploy = hostDeployConfigVO.getJarDeploy();
            jarDeploy.setValue(new String(decoder.decodeBuffer(jarDeploy.getValue()), "UTF-8"));
            C7nNexusDeployDTO c7nNexusDeployDTO = new C7nNexusDeployDTO();
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

            // 0.2 从制品库获取仓库信息

            Long nexusRepoId = jarDeploy.getRepositoryId();
            String groupId = jarDeploy.getGroupId();
            String artifactId = jarDeploy.getArtifactId();
            String version = jarDeploy.getVersion();

            // 0.3 获取并记录信息
            List<C7nNexusComponentDTO> nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), projectId, nexusRepoId, groupId, artifactId, version);
            if (CollectionUtils.isEmpty(nexusComponentDTOList)) {
                throw new CommonException(ERROR_JAR_VERSION_NOT_FOUND);
            }
            List<NexusMavenRepoDTO> mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), projectId, Collections.singleton(nexusRepoId));
            if (CollectionUtils.isEmpty(mavenRepoDTOList)) {
                throw new CommonException("error.get.maven.config");
            }
            c7nNexusDeployDTO.setPullUserId(mavenRepoDTOList.get(0).getNePullUserId());
            c7nNexusDeployDTO.setPullUserPassword(mavenRepoDTOList.get(0).getNePullUserPassword());
            c7nNexusDeployDTO.setDownloadUrl(nexusComponentDTOList.get(0).getDownloadUrl());
            c7nNexusComponentDTO = nexusComponentDTOList.get(0);
            c7nNexusDeployDTO.setJarName(getJarName(c7nNexusDeployDTO.getDownloadUrl()));

            sshUtil.sshConnect(hostDeployConfigVO.getHostConnectionVO(), ssh);

            // 2. 执行jar部署
            sshUtil.sshStopJar(ssh, c7nNexusDeployDTO.getJarName(), log);
            sshUtil.sshExec(ssh, c7nNexusDeployDTO, jarDeploy.getValue(), jarDeploy.getWorkingPath(), log);
            DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostDeployConfigVO.getHostConnectionVO().getHostId());
            DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(
                    projectId,
                    DeployType.MANUAL.getType(),
                    null,
                    DeployModeEnum.HOST.value(),
                    devopsHostDTO.getId(),
                    devopsHostDTO.getName(),
                    PipelineStatus.SUCCESS.toValue(),
                    new Date(),
                    DeployObjectTypeEnum.JAR.value(),
                    c7nNexusComponentDTO.getName(),
                    c7nNexusComponentDTO.getVersion(),
                    null);
            devopsDeployRecordService.baseCreate(devopsDeployRecordDTO);
        } catch (Exception e) {
            DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostDeployConfigVO.getHostConnectionVO().getHostId());
            DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(
                    projectId,
                    DeployType.MANUAL.getType(),
                    null,
                    DeployModeEnum.HOST.value(),
                    devopsHostDTO.getId(),
                    devopsHostDTO.getName(),
                    PipelineStatus.FAILED.toValue(),
                    new Date(),
                    DeployObjectTypeEnum.JAR.value(),
                    c7nNexusComponentDTO.getName(),
                    c7nNexusComponentDTO.getVersion(),
                    null);
            devopsDeployRecordService.baseCreate(devopsDeployRecordDTO);
            throw new CommonException(ERROR_DEPLOY_JAR_FAILED, e);
        } finally {
            sshUtil.closeSsh(ssh, null);
        }
    }

    private String getJarName(String url) {
        String[] arr = url.split("/");
        return arr[arr.length - 1].replace(".jar", "-") + GenerateUUID.generateRandomString() + ".jar";
    }

    private void hostImagedeploy(Long projectId, HostDeployConfigVO hostDeployConfigVO) {
        LOGGER.info("========================================");
        LOGGER.info("start image deploy cd host job,projectId:{}", projectId);
        SSHClient ssh = new SSHClient();
        StringBuilder log = new StringBuilder();
        HostDeployConfigVO.ImageDeploy imageDeploy = new HostDeployConfigVO.ImageDeploy();
        try {
            // 0.1

            imageDeploy = hostDeployConfigVO.getImageDeploy();
            imageDeploy.setValue(new String(decoder.decodeBuffer(imageDeploy.getValue()), "UTF-8"));
            // 0.2
            C7nImageDeployDTO c7nImageDeployDTO = new C7nImageDeployDTO();
            HarborC7nRepoImageTagVo imageTagVo = rdupmClientOperator.listImageTag(imageDeploy.getRepoType(), TypeUtil.objToLong(imageDeploy.getRepoId()), imageDeploy.getImageName(), imageDeploy.getTag());
            if (CollectionUtils.isEmpty(imageTagVo.getImageTagList())) {
                throw new CommonException(ERROR_IMAGE_TAG_NOT_FOUND);
            }
            c7nImageDeployDTO.setPullAccount(imageTagVo.getPullAccount());
            c7nImageDeployDTO.setPullPassword(imageTagVo.getPullPassword());
            c7nImageDeployDTO.setHarborUrl(imageTagVo.getHarborUrl());
            c7nImageDeployDTO.setPullCmd(imageTagVo.getImageTagList().get(0).getPullCmd());
            // 2.
            sshUtil.sshConnect(hostDeployConfigVO.getHostConnectionVO(), ssh);
            // 3.
            // 3.1
            sshUtil.dockerLogin(ssh, c7nImageDeployDTO, log);
            // 3.2
            sshUtil.dockerPull(ssh, c7nImageDeployDTO, log);

            sshUtil.dockerStop(ssh, imageDeploy.getContainerName(), log);
            // 3.3
            sshUtil.dockerRun(ssh, imageDeploy.getValue(), imageDeploy.getContainerName(), c7nImageDeployDTO, log);
            DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostDeployConfigVO.getHostConnectionVO().getHostId());
            DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(
                    projectId,
                    DeployType.MANUAL.getType(),
                    null,
                    DeployModeEnum.HOST.value(),
                    devopsHostDTO.getId(),
                    devopsHostDTO.getName(),
                    PipelineStatus.SUCCESS.toValue(),
                    new Date(),
                    DeployObjectTypeEnum.IMAGE.value(),
                    imageDeploy.getImageName(),
                    imageDeploy.getTag(),
                    null);
            devopsDeployRecordService.baseCreate(devopsDeployRecordDTO);
            LOGGER.info("========================================");
            LOGGER.info("image deploy cd host job success!!!");
        }catch (Exception e) {
            DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostDeployConfigVO.getHostConnectionVO().getHostId());
            DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(
                    projectId,
                    DeployType.MANUAL.getType(),
                    null,
                    DeployModeEnum.HOST.value(),
                    devopsHostDTO.getId(),
                    devopsHostDTO.getName(),
                    PipelineStatus.FAILED.toValue(),
                    new Date(),
                    DeployObjectTypeEnum.IMAGE.value(),
                    imageDeploy.getImageName(),
                    imageDeploy.getTag(),
                    null);
            devopsDeployRecordService.baseCreate(devopsDeployRecordDTO);
            throw new CommonException("error.deploy.hostImage.failed.", e);
        } finally {
            sshUtil.closeSsh(ssh, null);
        }
    }
}
