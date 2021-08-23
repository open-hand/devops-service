package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.deploy.DockerDeployVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.api.vo.hrdsCode.HarborC7nRepoImageTagVo;
import io.choerodon.devops.api.vo.market.JarReleaseConfigVO;
import io.choerodon.devops.api.vo.market.MarketHarborConfigVO;
import io.choerodon.devops.api.vo.market.MarketMavenConfigVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.DockerPullAccountDTO;
import io.choerodon.devops.infra.dto.repo.JarPullInfoDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.DeploymentSourceTypeEnums;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MavenUtil;
import io.choerodon.devops.infra.util.TypeUtil;


/**
 * @Author: shanyu
 * @DateTime: 2021-08-19 18:46
 **/
@Service
public class DevopsDeployGroupServiceImpl implements DevopsDeployGroupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsDeployGroupServiceImpl.class);

    private static final String AUTH_TYPE = "pull";
    private static final String WGET_COMMAND_TEMPLATE = "wget %s -o /choerodon/%s";
    private static final String WGET_COMMAND_WITH_AUTHENTICATION_TEMPLATE = "wget --user=%s --password=%s %s -o /choerodon/%s";
    private static final String ERROR_IMAGE_TAG_NOT_FOUND = "error.image.tag.not.found";

    private static final String IF_NOT_PRESENT = "IfNotPresent";

    @Value("${devops.jar.image}")
    private String jarImage;

    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private RdupmClientOperator rdupmClientOperator;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    private WorkloadService workloadService;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private HarborService harborService;
    @Autowired
    private DevopsDeploymentService devopsDeploymentService;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;

    @Override
    public DevopsDeployGroupVO appConfigDetail(Long projectId, Long devopsConfigGroupId) {
        DevopsDeployGroupVO devopsDeployGroupVO = devopsDeploymentService.queryDeployGroupInfoById(devopsConfigGroupId);
        devopsDeployGroupVO.setAppConfig(JsonHelper.unmarshalByJackson(devopsDeployGroupVO.getAppConfigJson(), DevopsDeployGroupAppConfigVO.class));
        devopsDeployGroupVO.setContainerConfig(JsonHelper.unmarshalByJackson(devopsDeployGroupVO.getContainerConfigJson(), new TypeReference<List<DevopsDeployGroupContainerConfigVO>>() {
        }));
        return devopsDeployGroupVO;
    }

    @Transactional
    @Override
    public void createOrUpdate(Long projectId, DevopsDeployGroupVO devopsDeployGroupVO, String operateType) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, devopsDeployGroupVO.getEnvId());
        devopsDeployGroupVO.setProjectId(projectId);
        // 校验配置
        validateConfig(devopsDeployGroupVO);

        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = new DevopsDeployAppCenterEnvDTO();
        if (MiscConstants.CREATE_TYPE.equals(operateType)) {
            // 插入应用记录
            devopsDeployAppCenterEnvDTO.setProjectId(projectId);
            devopsDeployAppCenterEnvDTO.setEnvId(devopsDeployAppCenterEnvDTO.getEnvId());
            devopsDeployAppCenterEnvDTO.setName(devopsDeployAppCenterEnvDTO.getName());
            devopsDeployAppCenterEnvDTO.setCode(devopsDeployGroupVO.getCode());
            devopsDeployAppCenterService.baseCreate(devopsDeployAppCenterEnvDTO);
        } else {
            // 更新应用记录
            devopsDeployAppCenterEnvDTO.setId(devopsDeployGroupVO.getInstanceId());
            devopsDeployAppCenterService.baseUpdate(devopsDeployAppCenterEnvDTO);
        }

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsDeployGroupVO.getProjectId());

        // 生成deployment
        String deployment = buildDeploymentYaml(projectDTO, devopsEnvironmentDTO, devopsDeployGroupVO);
        // 创建deployment
        WorkloadBaseCreateOrUpdateVO workloadBaseCreateOrUpdateVO = new WorkloadBaseCreateOrUpdateVO();
        workloadBaseCreateOrUpdateVO.setEnvId(String.valueOf(devopsDeployGroupVO.getEnvId()));
        workloadBaseCreateOrUpdateVO.setOperateType(operateType);
        workloadBaseCreateOrUpdateVO.setContent(deployment);
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put(DevopsDeploymentServiceImpl.EXTRA_INFO_KEY_APP_CONFIG, JsonHelper.marshalByJackson(devopsDeployGroupVO.getAppConfig()));
        extraInfo.put(DevopsDeploymentServiceImpl.EXTRA_INFO_KEY_CONTAINER_CONFIG, JsonHelper.marshalByJackson(devopsDeployGroupVO.getContainerConfig()));
        extraInfo.put(DevopsDeploymentServiceImpl.EXTRA_INFO_KEY_SOURCE_TYPE, DeploymentSourceTypeEnums.DEPLOY_GROUP);
        extraInfo.put(DevopsDeploymentServiceImpl.INSTANCE_ID, devopsDeployAppCenterEnvDTO.getId());

        workloadBaseCreateOrUpdateVO.setExtraConfig(extraInfo);
        workloadService.createOrUpdate(projectId, workloadBaseCreateOrUpdateVO, null, ResourceType.DEPLOYMENT);

        if (MiscConstants.CREATE_TYPE.equals(operateType)) {
            DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.queryByInstanceIdAndSourceType(devopsDeployGroupVO.getInstanceId(), DeploymentSourceTypeEnums.DEPLOY_GROUP.getType());
            devopsDeployAppCenterEnvDTO.setObjectId(devopsDeploymentDTO.getId());
            devopsDeployAppCenterService.baseUpdate(devopsDeployAppCenterEnvDTO);
        }
    }

    public String buildDeploymentYaml(ProjectDTO projectDTO, DevopsEnvironmentDTO devopsEnvironmentDTO, DevopsDeployGroupVO devopsDeployGroupVO) {
        V1beta2Deployment deployment = new V1beta2Deployment();
        try {
            // 构建deployment相关配置
            addAppConfig(projectDTO, devopsEnvironmentDTO, devopsDeployGroupVO, deployment);
            // 构建podTemplate相关配置
            addContainerConfig(projectDTO, devopsEnvironmentDTO, devopsDeployGroupVO, deployment);
        } catch (IOException e) {
            throw new CommonException("error.parse.config", e.getMessage());
        }
        Yaml yaml = new Yaml();
        return yaml.dump(deployment);
    }

    private V1beta2Deployment addAppConfig(ProjectDTO projectDTO, DevopsEnvironmentDTO devopsEnvironmentDTO, DevopsDeployGroupVO devopsDeployGroupVO, V1beta2Deployment deployment) throws IOException {
        DevopsDeployGroupAppConfigVO devopsDeployGroupAppConfigVO = devopsDeployGroupVO.getAppConfig();
        // 设置名称、labels、annotations
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(devopsDeployGroupVO.getName());
        if (!CollectionUtils.isEmpty(devopsDeployGroupAppConfigVO.getLabels())) {
            metadata.setLabels(devopsDeployGroupAppConfigVO.getLabels());
        }
        if (!CollectionUtils.isEmpty(devopsDeployGroupAppConfigVO.getAnnotations())) {
            metadata.setAnnotations(devopsDeployGroupAppConfigVO.getAnnotations());
        }
        deployment.setMetadata(metadata);

        // 设置spec
        V1beta2DeploymentSpec v1beta2DeploymentSpec = new V1beta2DeploymentSpec();
        v1beta2DeploymentSpec.setReplicas(devopsDeployGroupAppConfigVO.getReplicas());

        // 设置升级策略
        V1beta2DeploymentStrategy v1beta2DeploymentStrategy = new V1beta2DeploymentStrategy();
        v1beta2DeploymentStrategy.setType("RollingUpdate");
        V1beta2RollingUpdateDeployment rollingUpdate = new V1beta2RollingUpdateDeployment();
        if (devopsDeployGroupAppConfigVO.getMaxSurge() != null) {
            rollingUpdate.setMaxSurge(new IntOrString(devopsDeployGroupAppConfigVO.getMaxSurge()));
        }
        if (devopsDeployGroupAppConfigVO.getMaxUnavailable() != null) {
            rollingUpdate.setMaxSurge(new IntOrString(devopsDeployGroupAppConfigVO.getMaxUnavailable()));
        }
        v1beta2DeploymentSpec.setStrategy(v1beta2DeploymentStrategy);

        // 设置dns策略
        V1PodSpec v1PodSpec = new V1PodSpec();
        v1PodSpec.setDnsPolicy(devopsDeployGroupAppConfigVO.getDnsPolicy());
        V1PodDNSConfig v1PodDNSConfig = new V1PodDNSConfig();
        if (!StringUtils.isEmpty(devopsDeployGroupAppConfigVO.getNameServers())) {
            v1PodDNSConfig.setNameservers(Arrays.asList(devopsDeployGroupAppConfigVO.getNameServers().split(",")));
        }
        if (!StringUtils.isEmpty(devopsDeployGroupAppConfigVO.getSearches())) {
            v1PodDNSConfig.setSearches(Arrays.asList(devopsDeployGroupAppConfigVO.getSearches().split(",")));
        }
        if (!CollectionUtils.isEmpty(devopsDeployGroupAppConfigVO.getOptions())) {
            List<V1PodDNSConfigOption> v1PodDNSConfigOptionList = new ArrayList<>();
            devopsDeployGroupAppConfigVO.getOptions().forEach((key, value) -> {
                V1PodDNSConfigOption v1PodDNSConfigOption = new V1PodDNSConfigOption();
                v1PodDNSConfigOption.setName(key);
                v1PodDNSConfigOption.setValue(value);
                v1PodDNSConfigOptionList.add(v1PodDNSConfigOption);
            });
            v1PodDNSConfig.setOptions(v1PodDNSConfigOptionList);
        }
        v1PodSpec.setDnsConfig(v1PodDNSConfig);

        // 设置节点selector
        if (!CollectionUtils.isEmpty(devopsDeployGroupAppConfigVO.getNodeSelector())) {
            v1PodSpec.setNodeSelector(devopsDeployGroupAppConfigVO.getNodeSelector());
        }

        // 设置hostAlias
        if (!CollectionUtils.isEmpty(devopsDeployGroupAppConfigVO.getHostAlias())) {
            List<V1HostAlias> hostAliasList = new ArrayList<>();
            Map<String, String> hostAliasMap = devopsDeployGroupAppConfigVO.getHostAlias();
            hostAliasMap.forEach((ip, hostNames) -> {
                V1HostAlias hostAlias = new V1HostAlias();
                hostAlias.setIp(ip);
                hostAlias.setHostnames(Arrays.asList(hostNames.split(",")));
                hostAliasList.add(hostAlias);
            });
            v1PodSpec.setHostAliases(hostAliasList);
        }

        V1PodTemplateSpec v1PodTemplateSpec = new V1PodTemplateSpec();
        v1PodTemplateSpec.setSpec(v1PodSpec);
        v1beta2DeploymentSpec.setTemplate(v1PodTemplateSpec);

        deployment.metadata(metadata).spec(v1beta2DeploymentSpec);

        return deployment;
    }

    private V1beta2Deployment addContainerConfig(ProjectDTO projectDTO, DevopsEnvironmentDTO devopsEnvironmentDTO, DevopsDeployGroupVO devopsDeployGroupVO, V1beta2Deployment deployment) throws IOException {
        List<DevopsDeployGroupContainerConfigVO> devopsDeployGroupContainerConfigVOS = devopsDeployGroupVO.getContainerConfig();
        List<V1Container> containers = new ArrayList<>();
        boolean hasJarRdupm = false;
        List<String> initContainerCommands = new ArrayList<>();
        List<V1LocalObjectReference> imagePullSecrets = new ArrayList<>();
        for (DevopsDeployGroupContainerConfigVO devopsDeployGroupContainerConfigVO : devopsDeployGroupContainerConfigVOS) {
            String type = devopsDeployGroupContainerConfigVO.getType();
            V1Container v1Container = getBaseContainerConfig(devopsDeployGroupContainerConfigVO);
            switch (type) {
                case "docker":
                    // docker需要创建secret
                    containers.add(processImageConfig(projectDTO, devopsEnvironmentDTO, devopsDeployGroupContainerConfigVO, v1Container, imagePullSecrets));
                    break;
                case "jar":
                    hasJarRdupm = true;
                    // jar需要设置init-container
                    containers.add(processJarConfig(projectDTO, devopsDeployGroupContainerConfigVO, v1Container, initContainerCommands));
                    break;
                default:
                    throw new CommonException("error.unsupported.rdupm.type");
            }
        }
        V1PodSpec v1PodSpec = deployment.getSpec().getTemplate().getSpec();
        // 设置jar和镜像容器
        v1PodSpec.setContainers(containers);
        // 如果有jar的制品，那么需要设置init-container,同时还要设置volumes使init-container与jar的container共享目录
        if (hasJarRdupm) {
            List<V1Container> initContainers = new ArrayList<>();
            V1Container v1InitContainer = new V1Container();
            v1InitContainer.setName("pull-jar");
            v1InitContainer.setImagePullPolicy(IF_NOT_PRESENT);
            v1InitContainer.setImage(jarImage);
            v1InitContainer.setCommand(initContainerCommands);

            List<V1VolumeMount> v1VolumeMountList = new ArrayList<>();
            V1VolumeMount v1VolumeMount = new V1VolumeMount();
            v1VolumeMount.setMountPath("/choerodon");
            v1VolumeMount.setName("jar");
            v1VolumeMountList.add(v1VolumeMount);
            v1InitContainer.setVolumeMounts(v1VolumeMountList);

            initContainers.add(v1InitContainer);

            v1PodSpec.setInitContainers(initContainers);
        }

        // 设置卷挂载
        List<V1Volume> v1VolumeList = new ArrayList<>();
        V1Volume v1Volume = new V1Volume();
        v1Volume.setName("jar");
        v1Volume.setEmptyDir(new V1EmptyDirVolumeSource());
        v1VolumeList.add(v1Volume);

        v1PodSpec.setVolumes(v1VolumeList);

        // 设置镜像拉取secret
        v1PodSpec.setImagePullSecrets(imagePullSecrets);

        return deployment;
    }

    /**
     * 校验配置是否满足要求
     *
     * @param devopsDeployGroupVO
     */
    void validateConfig(DevopsDeployGroupVO devopsDeployGroupVO) {
        // todo 校验配置
    }


    /**
     * 构建基础的容器配置信息，名称、环境变量、资源限制（设置name、env、resources字段）
     *
     * @param devopsDeployGroupContainerConfigVO
     * @return
     */
    V1Container getBaseContainerConfig(DevopsDeployGroupContainerConfigVO devopsDeployGroupContainerConfigVO) {
        V1Container v1Container = new V1Container();
        v1Container.setName(devopsDeployGroupContainerConfigVO.getName());

        // 设置环境变量
        if (!CollectionUtils.isEmpty(devopsDeployGroupContainerConfigVO.getEnvs())) {
            List<V1EnvVar> envVarList = new ArrayList<>();
            Map<String, String> envMap = devopsDeployGroupContainerConfigVO.getEnvs();
            envMap.forEach((key, value) -> {
                V1EnvVar v1EnvVar = new V1EnvVar();
                v1EnvVar.setName(key);
                v1EnvVar.setValue(value);
                envVarList.add(v1EnvVar);
            });
            v1Container.setEnv(envVarList);
        }

        // 设置资源限制
        V1ResourceRequirements resourceRequirements = new V1ResourceRequirements();
        Map<String, Quantity> requests = new HashMap<>();
        if (!StringUtils.isEmpty(devopsDeployGroupContainerConfigVO.getRequestCpu())) {
            requests.put("cpu", new Quantity(new BigDecimal(devopsDeployGroupContainerConfigVO.getRequestCpu()), Quantity.Format.BINARY_SI));
        }
        if (!StringUtils.isEmpty(devopsDeployGroupContainerConfigVO.getRequestCpu())) {
            requests.put("memory", new Quantity(new BigDecimal(devopsDeployGroupContainerConfigVO.getRequestMemory()), Quantity.Format.BINARY_SI));
        }

        Map<String, Quantity> limits = new HashMap<>();
        if (!StringUtils.isEmpty(devopsDeployGroupContainerConfigVO.getRequestCpu())) {
            limits.put("cpu", new Quantity(new BigDecimal(devopsDeployGroupContainerConfigVO.getLimitCpu()), Quantity.Format.BINARY_SI));
        }
        if (!StringUtils.isEmpty(devopsDeployGroupContainerConfigVO.getRequestCpu())) {
            limits.put("memory", new Quantity(new BigDecimal(devopsDeployGroupContainerConfigVO.getLimitMemory()), Quantity.Format.BINARY_SI));
        }
        resourceRequirements.setLimits(limits);
        resourceRequirements.setRequests(requests);

        v1Container.setResources(resourceRequirements);

        // 设置端口
        List<V1ContainerPort> containerPortList = new ArrayList<>();

        devopsDeployGroupContainerConfigVO.getPorts().forEach(port -> {
            V1ContainerPort v1ContainerPort = new V1ContainerPort();
            v1ContainerPort.setName(port.get("name"));
            v1ContainerPort.setProtocol(port.get("protocol"));
            v1ContainerPort.setContainerPort(Integer.valueOf(port.get("containerPort")));
            containerPortList.add(v1ContainerPort);
        });
        v1Container.setPorts(containerPortList);

        return v1Container;
    }

    /**
     * 根据jar信息配置容器信息，包括下载jar包、上传jar包、java运行的container（设置image字段）
     *
     * @param devopsDeployGroupContainerConfigVO
     * @param v1Container
     * @param initContainerCommands
     * @return
     */
    V1Container processJarConfig(ProjectDTO projectDTO, DevopsDeployGroupContainerConfigVO devopsDeployGroupContainerConfigVO, V1Container v1Container, List<String> initContainerCommands) {
        // 处理用户上传的jar
        if (AppSourceType.UPLOAD.getValue().equals(devopsDeployGroupContainerConfigVO.getSourceType())) {
            initContainerCommands.add(String.format(WGET_COMMAND_TEMPLATE, devopsDeployGroupContainerConfigVO.getJarFileDownloadUrl(), devopsDeployGroupContainerConfigVO.getName() + ".jar"));
        } else {
            // 制品库中的jar
            JarPullInfoDTO jarPullInfoDTO = getJarPullInfo(projectDTO, devopsDeployGroupContainerConfigVO.getJarDeployVO());
            initContainerCommands.add(String.format(WGET_COMMAND_WITH_AUTHENTICATION_TEMPLATE, jarPullInfoDTO.getPullUserId(), jarPullInfoDTO.getPullUserPassword(), jarPullInfoDTO.getDownloadUrl(), devopsDeployGroupContainerConfigVO.getName() + ".jar"));
        }
        List<String> commands = new ArrayList<>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add(String.format("java $JAVA_OPTS -jar /choerodon/%s", devopsDeployGroupContainerConfigVO.getName() + ".jar"));
        v1Container.setName(devopsDeployGroupContainerConfigVO.getName());
        v1Container.setCommand(commands);

        v1Container.setImage(jarImage);
        v1Container.setImagePullPolicy(IF_NOT_PRESENT);

        List<V1VolumeMount> v1VolumeMountList = new ArrayList<>();
        V1VolumeMount v1VolumeMount = new V1VolumeMount();
        v1VolumeMount.setMountPath("/choerodon");
        v1VolumeMountList.add(v1VolumeMount);
        v1Container.setVolumeMounts(v1VolumeMountList);


        return v1Container;
    }

    /**
     * 根据镜像信息配置容器信息，包括构建secret并通过websocket发送给agent创建、运行的container（设置image字段）
     *
     * @param devopsDeployGroupContainerConfigVO
     * @param v1Container
     * @return
     */
    private V1Container processImageConfig(ProjectDTO projectDTO, DevopsEnvironmentDTO devopsEnvironmentDTO, DevopsDeployGroupContainerConfigVO devopsDeployGroupContainerConfigVO, V1Container v1Container, List<V1LocalObjectReference> imagePullSecrets) {
        DevopsDeployGroupDockerConfigVO dockerDeployVO = devopsDeployGroupContainerConfigVO.getDockerDeployVO();

        DockerPullAccountDTO pullAccountDTO = createPullAccount(projectDTO, devopsEnvironmentDTO, dockerDeployVO);
        if (pullAccountDTO != null) {
            ConfigVO configVO = new ConfigVO();
            configVO.setUrl(pullAccountDTO.getHarborUrl());
            configVO.setUserName(pullAccountDTO.getPullAccount());
            configVO.setPassword(pullAccountDTO.getPullPassword());
            String imagePullSecretName = "secret-" + devopsDeployGroupContainerConfigVO.getName();
            V1LocalObjectReference v1LocalObjectReference = new V1LocalObjectReference();
            v1LocalObjectReference.setName(imagePullSecretName);
            imagePullSecrets.add(v1LocalObjectReference);
            agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), imagePullSecretName, configVO);
        }

        v1Container.setName(devopsDeployGroupContainerConfigVO.getName());
        v1Container.setImage(dockerDeployVO.getImageInfo().getImageName() + ":" + dockerDeployVO.getImageInfo().getTag());
        v1Container.setImagePullPolicy(IF_NOT_PRESENT);
        return v1Container;
    }

    private JarPullInfoDTO getJarPullInfo(ProjectDTO projectDTO, JarDeployVO jarDeployVO) {
        String sourceType = jarDeployVO.getSourceType();
        List<C7nNexusComponentDTO> nexusComponentDTOList = new ArrayList<>();
        List<NexusMavenRepoDTO> mavenRepoDTOList = new ArrayList<>();
        if (StringUtils.endsWithIgnoreCase(AppSourceType.MARKET.getValue(), sourceType)
                || StringUtils.endsWithIgnoreCase(AppSourceType.HZERO.getValue(), sourceType)) {
            MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectDTO.getId()), Objects.requireNonNull(jarDeployVO.getDeployObjectId()));
            JarReleaseConfigVO jarReleaseConfigVO = JsonHelper.unmarshalByJackson(marketServiceDeployObjectVO.getMarketJarLocation(), JarReleaseConfigVO.class);
            if (Objects.isNull(marketServiceDeployObjectVO.getMarketMavenConfigVO())) {
                throw new CommonException("error.maven.deploy.object.not.exist");
            }

            MarketMavenConfigVO marketMavenConfigVO = marketServiceDeployObjectVO.getMarketMavenConfigVO();
            C7nNexusComponentDTO nNexusComponentDTO = new C7nNexusComponentDTO();

            nNexusComponentDTO.setName(jarReleaseConfigVO.getArtifactId());
            nNexusComponentDTO.setVersion(jarReleaseConfigVO.getVersion());
            nNexusComponentDTO.setGroup(jarReleaseConfigVO.getGroupId());
            nNexusComponentDTO.setDownloadUrl(MavenUtil.getDownloadUrl(jarReleaseConfigVO));
            nexusComponentDTOList.add(nNexusComponentDTO);

            jarDeployVO.setProdJarInfoVO(new ProdJarInfoVO(jarReleaseConfigVO.getGroupId(),
                    jarReleaseConfigVO.getArtifactId(),
                    jarReleaseConfigVO.getVersion()));

            NexusMavenRepoDTO nexusMavenRepoDTO = new NexusMavenRepoDTO();
            nexusMavenRepoDTO.setNePullUserId(marketMavenConfigVO.getPullUserName());
            nexusMavenRepoDTO.setNePullUserPassword(marketMavenConfigVO.getPullPassword());
            mavenRepoDTOList.add(nexusMavenRepoDTO);
            //如果是市场部署将部署人员添加为应用的订阅人员
            marketServiceClientOperator.subscribeApplication(marketServiceDeployObjectVO.getMarketAppId(), DetailsHelper.getUserDetails().getUserId());
        } else {
            // 0.2 从制品库获取仓库信息
            Long nexusRepoId = jarDeployVO.getProdJarInfoVO().getRepositoryId();
            String groupId = jarDeployVO.getProdJarInfoVO().getGroupId();
            String artifactId = jarDeployVO.getProdJarInfoVO().getArtifactId();
            String version = jarDeployVO.getProdJarInfoVO().getVersion();
            nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), projectDTO.getId(), nexusRepoId, groupId, artifactId, version);
            mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), projectDTO.getId(), Collections.singleton(nexusRepoId));

        }
        if (CollectionUtils.isEmpty(nexusComponentDTOList)) {
            throw new CommonException("error.jar.version.not.found");
        }
        if (CollectionUtils.isEmpty(mavenRepoDTOList)) {
            throw new CommonException("error.get.maven.config");
        }

        JarPullInfoDTO jarPullInfoDTO = new JarPullInfoDTO();
        jarPullInfoDTO.setPullUserId(mavenRepoDTOList.get(0).getNePullUserId());
        jarPullInfoDTO.setPullUserPassword(mavenRepoDTOList.get(0).getNePullUserPassword());
        jarPullInfoDTO.setDownloadUrl(nexusComponentDTOList.get(0).getDownloadUrl());
        return jarPullInfoDTO;
    }

    private DockerPullAccountDTO createPullAccount(ProjectDTO projectDTO, DevopsEnvironmentDTO devopsEnvironmentDTO, DevopsDeployGroupDockerConfigVO dockerDeployVO) {
        DockerPullAccountDTO dockerPullAccountDTO = new DockerPullAccountDTO();
        if (isMarketOrHzero(dockerDeployVO)) {
            MarketServiceDeployObjectVO marketServiceDeployObjectVO = getMarketServiceDeployObjectVO(projectDTO.getId(), dockerDeployVO);
            MarketHarborConfigVO marketHarborConfigVO = marketServiceDeployObjectVO.getMarketHarborConfigVO();
            dockerPullAccountDTO = initDockerPullAccountDTO(marketHarborConfigVO);
        } else if (AppSourceType.CURRENT_PROJECT.getValue().equals(dockerDeployVO.getSourceType())) {
            dockerPullAccountDTO = ConvertUtils.convertObject(getHarborC7nRepoImageTagVo(dockerDeployVO), DockerPullAccountDTO.class);
        } else if (AppSourceType.SHARE.getValue().equals(dockerDeployVO.getSourceType())) {
            dockerPullAccountDTO = getShareServiceDockerPullAccount(dockerDeployVO, devopsEnvironmentDTO);
        }
        return dockerPullAccountDTO;
    }

    private HarborC7nRepoImageTagVo getHarborC7nRepoImageTagVo(DockerDeployVO dockerDeployVO) {
        HarborC7nRepoImageTagVo imageTagVo = rdupmClientOperator.listImageTag(dockerDeployVO.getImageInfo().getRepoType(), TypeUtil.objToLong(dockerDeployVO.getImageInfo().getRepoId()), dockerDeployVO.getImageInfo().getImageName(), dockerDeployVO.getImageInfo().getTag());
        if (CollectionUtils.isEmpty(imageTagVo.getImageTagList())) {
            throw new CommonException(ERROR_IMAGE_TAG_NOT_FOUND);
        }
        return imageTagVo;
    }

    private DockerPullAccountDTO getShareServiceDockerPullAccount(DevopsDeployGroupDockerConfigVO dockerConfigVO, DevopsEnvironmentDTO devopsEnvironmentDTO) {
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(dockerConfigVO.getAppServiceId());

        String secretCode;
        //如果应用绑定了私有镜像库,则处理secret
        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(dockerConfigVO.getAppServiceVersionId());

        DevopsConfigDTO devopsConfigDTO;
        if (appServiceVersionDTO.getHarborConfigId() != null) {
            devopsConfigDTO = harborService.queryRepoConfigByIdToDevopsConfig(appServiceDTO.getId(), appServiceDTO.getProjectId(),
                    appServiceVersionDTO.getHarborConfigId(), appServiceVersionDTO.getRepoType(), AUTH_TYPE);
        } else {
            //查询harbor的用户名密码
            devopsConfigDTO = harborService.queryRepoConfigToDevopsConfig(appServiceDTO.getProjectId(),
                    appServiceDTO.getId(), AUTH_TYPE);
        }
        LOGGER.debug("Docker config for app service with id {} and code {} and version id: {} is not null. And the config id is {}...", appServiceDTO.getId(), appServiceDTO.getCode(), dockerConfigVO.getAppServiceVersionId(), devopsConfigDTO.getId());

        ConfigVO configVO = JsonHelper.unmarshalByJackson(devopsConfigDTO.getConfig(), ConfigVO.class);
        if (configVO.getPrivate() != null && configVO.getPrivate()) {
            LOGGER.debug("Docker config for app service with id {} and code {} and version id: {} is private.", appServiceDTO.getId(), appServiceDTO.getCode(), dockerConfigVO.getAppServiceVersionId());
            DockerPullAccountDTO dockerPullAccountDTO = new DockerPullAccountDTO();
            dockerPullAccountDTO.setHarborUrl(configVO.getUrl());
            dockerPullAccountDTO.setPullAccount(configVO.getUserName());
            dockerPullAccountDTO.setPullPassword(configVO.getPassword());
            return dockerPullAccountDTO;
        } else {
            return null;
        }
    }


    private DockerPullAccountDTO initDockerPullAccountDTO(MarketHarborConfigVO marketHarborConfigVO) {
        return new DockerPullAccountDTO()
                .setHarborUrl(marketHarborConfigVO.getRepoUrl())
                .setPullAccount(marketHarborConfigVO.getRobotName())
                .setPullPassword(marketHarborConfigVO.getToken());
    }

    private boolean isMarketOrHzero(DockerDeployVO dockerDeployVO) {
        return AppSourceType.MARKET.getValue().equals(dockerDeployVO.getSourceType())
                || AppSourceType.HZERO.getValue().equals(dockerDeployVO.getSourceType());
    }

    private MarketServiceDeployObjectVO getMarketServiceDeployObjectVO(Long projectId, DockerDeployVO dockerDeployVO) {
        MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(dockerDeployVO.getDeployObjectId()));
        if (Objects.isNull(marketServiceDeployObjectVO.getMarketHarborConfigVO())) {
            throw new CommonException("error.harbor.deploy.object.not.exist");
        }
        return marketServiceDeployObjectVO;
    }

}
