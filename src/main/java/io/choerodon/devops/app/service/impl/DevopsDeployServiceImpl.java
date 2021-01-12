package io.choerodon.devops.app.service.impl;

import java.util.*;

import net.schmizz.sshj.SSHClient;
import org.hzero.core.base.BaseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import sun.misc.BASE64Decoder;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.AppServiceDeployVO;
import io.choerodon.devops.api.vo.HarborC7nImageTagVo;
import io.choerodon.devops.api.vo.deploy.DeployConfigVO;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.hrdsCode.HarborC7nRepoImageTagVo;
import io.choerodon.devops.api.vo.market.*;
import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.DevopsDeployRecordService;
import io.choerodon.devops.app.service.DevopsDeployService;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.C7nImageDeployDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusDeployDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.HostDeployType;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.DevopsHostMapper;
import io.choerodon.devops.infra.util.JsonHelper;
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
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;

    @Override
    public void hostDeploy(Long projectId, DeployConfigVO deployConfigVO) {
        if (DeployModeEnum.ENV.value().equals(deployConfigVO.getDeployType())) {
            AppServiceDeployVO appServiceDeployVO = deployConfigVO.getAppServiceDeployVO();
            appServiceDeployVO.setType("create");
            appServiceInstanceService.createOrUpdate(projectId, appServiceDeployVO, false);
        } else if (DeployModeEnum.HOST.value().equals(deployConfigVO.getDeployType())) {
            if (HostDeployType.IMAGED_DEPLOY.getValue().equals(deployConfigVO.getDeployObjectType())) {
                hostImagedeploy(projectId, deployConfigVO);
            } else if (HostDeployType.JAR_DEPLOY.getValue().equals(deployConfigVO.getDeployObjectType())) {
                hostJarDeploy(projectId, deployConfigVO);
            }
        }
    }

    private void hostJarDeploy(Long projectId, DeployConfigVO deployConfigVO) {
        LOGGER.info("========================================");
        LOGGER.info("start jar deploy cd host job,projectId:{}", projectId);
        SSHClient ssh = new SSHClient();
        StringBuilder log = new StringBuilder();
        DeployConfigVO.JarDeploy jarDeploy;
        C7nNexusComponentDTO c7nNexusComponentDTO = new C7nNexusComponentDTO();
        DeploySourceVO deploySourceVO = new DeploySourceVO();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        deploySourceVO.setType(deployConfigVO.getAppSource());
        deploySourceVO.setProjectName(projectDTO.getName());
        deploySourceVO.setDeployObjectId(deployConfigVO.getJarDeploy().getDeployObjectId());
        String deployObjectName = null;
        String deployVersion = null;
        String instanceName = null;
        try {
            // 0.1 查询部署信息

            jarDeploy = deployConfigVO.getJarDeploy();
            jarDeploy.setValue(new String(decoder.decodeBuffer(jarDeploy.getValue()), "UTF-8"));
            C7nNexusDeployDTO c7nNexusDeployDTO = new C7nNexusDeployDTO();

            // 0.2 从制品库获取仓库信息

            Long nexusRepoId = jarDeploy.getRepositoryId();
            String groupId = jarDeploy.getGroupId();
            String artifactId = jarDeploy.getArtifactId();
            String version = jarDeploy.getVersion();

            // 0.3 获取并记录信息
            List<C7nNexusComponentDTO> nexusComponentDTOList = new ArrayList<>();
            List<NexusMavenRepoDTO> mavenRepoDTOList = new ArrayList<>();
            if (StringUtils.endsWithIgnoreCase(AppSourceType.MARKET.getValue(), deployConfigVO.getAppSource())) {
                MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(jarDeploy.getDeployObjectId()));
                if (Objects.isNull(marketServiceDeployObjectVO.getMarketMavenConfigVO())) {
                    throw new CommonException("error.maven.deploy.object.not.exist");
                }

                deployObjectName = marketServiceDeployObjectVO.getMarketServiceName();
                deployVersion = marketServiceDeployObjectVO.getDevopsAppServiceVersion();

                MarketMavenConfigVO marketMavenConfigVO = marketServiceDeployObjectVO.getMarketMavenConfigVO();
                C7nNexusComponentDTO nNexusComponentDTO = new C7nNexusComponentDTO();

                nNexusComponentDTO.setName(deployConfigVO.getJarDeploy().getServerName());
                nNexusComponentDTO.setVersion(deployConfigVO.getJarDeploy().getVersion());
                nexusComponentDTOList.add(nNexusComponentDTO);
                NexusMavenRepoDTO nexusMavenRepoDTO = new NexusMavenRepoDTO();
                nexusMavenRepoDTO.setNePullUserId(marketMavenConfigVO.getPullUserName());
                nexusMavenRepoDTO.setNePullUserPassword(marketMavenConfigVO.getPullPassword());
                mavenRepoDTOList.add(nexusMavenRepoDTO);

                JarSourceConfig jarSourceConfig = JsonHelper.unmarshalByJackson(marketServiceDeployObjectVO.getJarSource(), JarSourceConfig.class);
                jarDeploy.setArtifactId(jarSourceConfig.getArtifactId());
                nNexusComponentDTO.setDownloadUrl(getDownloadUrl(JsonHelper.unmarshalByJackson(marketServiceDeployObjectVO.getMarketJarLocation(), JarReleaseConfigVO.class)));
                //如果是市场部署将部署人员添加为应用的订阅人员
                marketServiceClientOperator.subscribeApplication(marketServiceDeployObjectVO.getMarketAppId(), DetailsHelper.getUserDetails().getUserId());
            } else {
                nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), projectId, nexusRepoId, groupId, artifactId, version);
                mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), projectId, Collections.singleton(nexusRepoId));
                deploySourceVO.setType(AppSourceType.CURRENT_PROJECT.getValue());
                deploySourceVO.setProjectName(projectDTO.getName());
                deployObjectName = nexusComponentDTOList.get(0).getName();
                deployVersion = nexusComponentDTOList.get(0).getVersion();
            }
            deploySourceVO.setDeployObjectId(deployConfigVO.getJarDeploy().getDeployObjectId());
            if (CollectionUtils.isEmpty(nexusComponentDTOList)) {
                throw new CommonException(ERROR_JAR_VERSION_NOT_FOUND);
            }
            if (CollectionUtils.isEmpty(mavenRepoDTOList)) {
                throw new CommonException("error.get.maven.config");
            }
            c7nNexusDeployDTO.setPullUserId(mavenRepoDTOList.get(0).getNePullUserId());
            c7nNexusDeployDTO.setPullUserPassword(mavenRepoDTOList.get(0).getNePullUserPassword());
            c7nNexusDeployDTO.setDownloadUrl(nexusComponentDTOList.get(0).getDownloadUrl());
            c7nNexusComponentDTO = nexusComponentDTOList.get(0);
            c7nNexusDeployDTO.setJarName(jarDeploy.getArtifactId());

            sshUtil.sshConnect(deployConfigVO.getHostConnectionVO(), ssh);

            // 2. 执行jar部署
            sshUtil.sshStopJar(ssh, c7nNexusDeployDTO.getJarName(), jarDeploy.getWorkingPath(), log);
            sshUtil.sshExec(ssh, c7nNexusDeployDTO, jarDeploy.getValue(), jarDeploy.getWorkingPath(), log);
            DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(deployConfigVO.getHostConnectionVO().getHostId());
            devopsDeployRecordService.saveRecord(
                    projectId,
                    DeployType.MANUAL,
                    null,
                    DeployModeEnum.HOST,
                    devopsHostDTO.getId(),
                    devopsHostDTO.getName(),
                    PipelineStatus.SUCCESS.toValue(),
                    DeployObjectTypeEnum.JAR,
                    deployObjectName,
                    deployVersion,
                    null,
                    deploySourceVO);
        } catch (Exception e) {
            DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(deployConfigVO.getHostConnectionVO().getHostId());
            devopsDeployRecordService.saveRecord(
                    projectId,
                    DeployType.MANUAL,
                    null,
                    DeployModeEnum.HOST,
                    devopsHostDTO.getId(),
                    devopsHostDTO.getName(),
                    PipelineStatus.FAILED.toValue(),
                    DeployObjectTypeEnum.JAR,
                    deployObjectName,
                    deployVersion,
                    null,
                    deploySourceVO);
            throw new CommonException(ERROR_DEPLOY_JAR_FAILED, e);
        } finally {
            sshUtil.closeSsh(ssh, null);
        }
    }

    private String getDownloadUrl(JarReleaseConfigVO jarReleaseConfigVO) {
        //拼接download URL http://xxxx:17145/repository/lilly-snapshot/io/choerodon/springboot/0.0.1-SNAPSHOT/springboot-0.0.1-20210106.020444-2.jar
        return jarReleaseConfigVO.getNexusRepoUrl() + BaseConstants.Symbol.SLASH +
                jarReleaseConfigVO.getGroupId().replace(".", "/") +
                BaseConstants.Symbol.SLASH + jarReleaseConfigVO.getArtifactId() + BaseConstants.Symbol.SLASH + jarReleaseConfigVO.getVersion() +
                BaseConstants.Symbol.SLASH + jarReleaseConfigVO.getArtifactId() + BaseConstants.Symbol.MIDDLE_LINE + jarReleaseConfigVO.getSnapshotTimestamp() + ".jar";
    }

    private String getJarName(String url) {
        String[] arr = url.split("/");
        return arr[arr.length - 1];
    }

    private void hostImagedeploy(Long projectId, DeployConfigVO deployConfigVO) {
        LOGGER.info("========================================");
        LOGGER.info("start image deploy cd host job,projectId:{}", projectId);
        SSHClient ssh = new SSHClient();
        StringBuilder log = new StringBuilder();
        DeployConfigVO.ImageDeploy imageDeploy = new DeployConfigVO.ImageDeploy();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setDeployObjectId(deployConfigVO.getImageDeploy().getDeployObjectId());
        deploySourceVO.setType(deployConfigVO.getAppSource());
        deploySourceVO.setProjectName(projectDTO.getName());
        String deployObjectName = null;
        String deployVersion = null;
        String instanceName = null;
        try {
            // 0.1
            imageDeploy = deployConfigVO.getImageDeploy();
            imageDeploy.setValue(new String(decoder.decodeBuffer(imageDeploy.getValue()), "UTF-8"));
            // 0.2
            HarborC7nRepoImageTagVo imageTagVo = new HarborC7nRepoImageTagVo();
            C7nImageDeployDTO c7nImageDeployDTO = new C7nImageDeployDTO();

            if (StringUtils.endsWithIgnoreCase(AppSourceType.MARKET.getValue(), deployConfigVO.getAppSource())) {
                MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(deployConfigVO.getImageDeploy().getDeployObjectId()));
                if (Objects.isNull(marketServiceDeployObjectVO.getMarketHarborConfigVO())) {
                    throw new CommonException("error.harbor.deploy.object.not.exist");
                }
                MarketHarborConfigVO marketHarborConfigVO = marketServiceDeployObjectVO.getMarketHarborConfigVO();
                imageTagVo.setPullAccount(marketHarborConfigVO.getRobotName());
                imageTagVo.setHarborUrl(marketHarborConfigVO.getRepoUrl());
                imageTagVo.setPullPassword(marketHarborConfigVO.getToken());
                HarborC7nImageTagVo harborC7nImageTagVo = new HarborC7nImageTagVo();
                harborC7nImageTagVo.setPullCmd("docker pull " + marketServiceDeployObjectVO.getMarketDockerImageUrl());
                List<HarborC7nImageTagVo> harborC7nImageTagVos = new ArrayList<>();
                harborC7nImageTagVos.add(harborC7nImageTagVo);
                imageTagVo.setImageTagList(harborC7nImageTagVos);
                deploySourceVO.setMarketAppName(marketServiceDeployObjectVO.getMarketAppName());
                //部署对象的名称
                deployObjectName = marketServiceDeployObjectVO.getDevopsAppServiceName();
                deployVersion = marketServiceDeployObjectVO.getDevopsAppServiceVersion();
//                instanceName = marketServiceDeployObjectVO.getDevopsAppServiceCode() + BaseConstants.Symbol.MIDDLE_LINE + UUID.randomUUID().toString().substring(0, 5);

                deploySourceVO.setMarketAppName(marketServiceDeployObjectVO.getMarketAppName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketAppVersion());
                deploySourceVO.setMarketServiceName(marketServiceDeployObjectVO.getMarketServiceName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketServiceVersion());
                //如果是市场部署将部署人员添加为应用的订阅人员
                marketServiceClientOperator.subscribeApplication(marketServiceDeployObjectVO.getMarketAppId(), DetailsHelper.getUserDetails().getUserId());

            } else {
                imageTagVo = rdupmClientOperator.listImageTag(imageDeploy.getRepoType(), TypeUtil.objToLong(imageDeploy.getRepoId()), imageDeploy.getImageName(), imageDeploy.getTag());
                deployObjectName = imageDeploy.getImageName();
                deployVersion = imageDeploy.getTag();
            }

            if (CollectionUtils.isEmpty(imageTagVo.getImageTagList())) {
                throw new CommonException(ERROR_IMAGE_TAG_NOT_FOUND);
            }
            c7nImageDeployDTO.setPullAccount(imageTagVo.getPullAccount());
            c7nImageDeployDTO.setPullPassword(imageTagVo.getPullPassword());
            c7nImageDeployDTO.setHarborUrl(imageTagVo.getHarborUrl());
            c7nImageDeployDTO.setPullCmd(imageTagVo.getImageTagList().get(0).getPullCmd());
            // 2.
            sshUtil.sshConnect(deployConfigVO.getHostConnectionVO(), ssh);
            // 3.
            // 3.1
            sshUtil.dockerLogin(ssh, c7nImageDeployDTO, log);
            // 3.2
            sshUtil.dockerPull(ssh, c7nImageDeployDTO, log);

            sshUtil.dockerStop(ssh, imageDeploy.getContainerName(), log);
            // 3.3
            sshUtil.dockerRun(ssh, imageDeploy.getValue(), imageDeploy.getContainerName(), c7nImageDeployDTO, log);
            DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(deployConfigVO.getHostConnectionVO().getHostId());
            devopsDeployRecordService.saveRecord(
                    projectId,
                    DeployType.MANUAL,
                    null,
                    DeployModeEnum.HOST,
                    devopsHostDTO.getId(),
                    devopsHostDTO.getName(),
                    PipelineStatus.SUCCESS.toValue(),
                    DeployObjectTypeEnum.IMAGE,
                    deployObjectName,
                    deployVersion,
                    null,
                    deploySourceVO);
            LOGGER.info("========================================");
            LOGGER.info("image deploy cd host job success!!!");
        } catch (Exception e) {
            DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(deployConfigVO.getHostConnectionVO().getHostId());
            devopsDeployRecordService.saveRecord(
                    projectId,
                    DeployType.MANUAL,
                    null,
                    DeployModeEnum.HOST,
                    devopsHostDTO.getId(),
                    devopsHostDTO.getName(),
                    PipelineStatus.FAILED.toValue(),
                    DeployObjectTypeEnum.IMAGE,
                    deployObjectName,
                    deployVersion,
                    null,
                    deploySourceVO);
            throw new CommonException("error.deploy.hostImage.failed.", e);
        } finally {
            sshUtil.closeSsh(ssh, null);
        }
    }
}
