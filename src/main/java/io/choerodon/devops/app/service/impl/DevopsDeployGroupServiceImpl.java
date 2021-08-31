package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.enums.ResourceType.DEPLOYMENT;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import io.kubernetes.client.JSON;
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
import io.choerodon.devops.infra.dto.repo.*;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.DeploymentSourceTypeEnums;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.enums.deploy.OperationTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.util.*;


/**
 * @Author: shanyu
 * @DateTime: 2021-08-19 18:46
 **/
@Service
public class DevopsDeployGroupServiceImpl implements DevopsDeployGroupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsDeployGroupServiceImpl.class);

    private static final String AUTH_TYPE = "pull";
    private static final String WGET_COMMAND_TEMPLATE = "wget %s -O /choerodon/%s";
    private static final String WGET_COMMAND_WITH_AUTHENTICATION_TEMPLATE = "wget --user=%s --password=%s %s -O /choerodon/%s";
    private static final String ERROR_IMAGE_TAG_NOT_FOUND = "error.image.tag.not.found";
    private static final String SEPARATOR = ";";

    private static final String IF_NOT_PRESENT = "IfNotPresent";
    private static final String VOLUME_MOUNt_NAME = "jar";

    @Value("${devops.jar.image}")
    private String jarImage;

    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private RdupmClientOperator rdupmClientOperator;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
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
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private UserAttrService userAttrService;

    @Transactional
    @Override
    public void createOrUpdate(Long projectId, DevopsDeployGroupVO devopsDeployGroupVO, String operateType, boolean onlyForContainer) {
        //1. 查询校验环境
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.getProjectEnvironment(projectId, devopsDeployGroupVO.getEnvId());

        // 2.校验用户是否拥有环境权限
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
        devopsDeployGroupVO.setProjectId(projectId);
        // 3.校验配置
        validateConfig(devopsDeployGroupVO, onlyForContainer);

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsDeployGroupVO.getProjectId());

        // 4.生成deployment
        String deployment = buildDeploymentYaml(projectDTO, devopsEnvironmentDTO, devopsDeployGroupVO);
        // 5.创建deployment
        WorkloadBaseCreateOrUpdateVO workloadBaseCreateOrUpdateVO = new WorkloadBaseCreateOrUpdateVO();
        workloadBaseCreateOrUpdateVO.setEnvId(String.valueOf(devopsDeployGroupVO.getEnvId()));
        workloadBaseCreateOrUpdateVO.setOperateType(operateType);
        workloadBaseCreateOrUpdateVO.setContent(deployment);
        if (devopsDeployGroupVO.getInstanceId() != null) {
            workloadBaseCreateOrUpdateVO.setResourceId(String.valueOf(devopsDeployGroupVO.getInstanceId()));
        }
        workloadBaseCreateOrUpdateVO.setToDecrypt(false);
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put(DevopsDeploymentServiceImpl.EXTRA_INFO_KEY_APP_CONFIG, JsonHelper.marshalByJackson(devopsDeployGroupVO.getAppConfig()));
        extraInfo.put(DevopsDeploymentServiceImpl.EXTRA_INFO_KEY_CONTAINER_CONFIG, JsonHelper.marshalByJackson(devopsDeployGroupVO.getContainerConfig()));
        extraInfo.put(DevopsDeploymentServiceImpl.EXTRA_INFO_KEY_SOURCE_TYPE, DeploymentSourceTypeEnums.DEPLOY_GROUP.getType());

        workloadBaseCreateOrUpdateVO.setExtraInfo(extraInfo);
        workloadService.createOrUpdate(projectId, workloadBaseCreateOrUpdateVO, null, ResourceType.DEPLOYMENT);

        // 更新关联的对象id
        if (MiscConstants.CREATE_TYPE.equals(operateType)) {
            DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.baseQueryByEnvIdAndName(devopsDeployGroupVO.getEnvId(), devopsDeployGroupVO.getAppCode());
            // 插入应用记录
            DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.baseCreate(devopsDeployGroupVO.getAppName(), devopsDeployGroupVO.getAppCode(), projectId, devopsDeploymentDTO.getId(), devopsDeployGroupVO.getEnvId(), OperationTypeEnum.CREATE_APP.value(), "", RdupmTypeEnum.DEPLOYMENT.value());

            devopsDeploymentDTO.setInstanceId(devopsDeployAppCenterEnvDTO.getId());
            devopsDeploymentService.baseUpdate(devopsDeploymentDTO);
        } else {
            // 更新应用记录
            DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByEnvIdAndCode(devopsDeployGroupVO.getEnvId(), devopsDeployGroupVO.getAppCode());
            // 如果名称发生变化，更新名称
            if (!devopsDeployAppCenterEnvDTO.getName().equals(devopsDeployGroupVO.getAppName())) {
                devopsDeployAppCenterEnvDTO.setName(devopsDeployGroupVO.getAppName());
                devopsDeployAppCenterService.baseUpdate(devopsDeployAppCenterEnvDTO);
            }
        }
    }

    @Transactional
    @Override
    public void updateContainer(Long projectId, DevopsDeployGroupVO devopsDeployGroupVO) {
        // 设置appConfig
        DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.selectByPrimaryKey(devopsDeployGroupVO.getInstanceId());
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(devopsDeploymentDTO.getInstanceId());

        devopsDeployGroupVO.setAppConfig(JsonHelper.unmarshalByJackson(devopsDeploymentDTO.getAppConfig(), DevopsDeployGroupAppConfigVO.class));
        devopsDeployGroupVO.setAppName(devopsDeployAppCenterEnvDTO.getName());
        devopsDeployGroupVO.setAppCode(devopsDeployAppCenterEnvDTO.getCode());

        createOrUpdate(projectId, devopsDeployGroupVO, MiscConstants.UPDATE_TYPE, true);
    }

    public String buildDeploymentYaml(ProjectDTO projectDTO, DevopsEnvironmentDTO
            devopsEnvironmentDTO, DevopsDeployGroupVO devopsDeployGroupVO) {
        V1beta2Deployment deployment = new V1beta2Deployment();
        deployment.setKind(DEPLOYMENT.getType());
        deployment.setApiVersion("apps/v1");
        try {
            // 构建deployment相关配置
            addAppConfig(projectDTO, devopsEnvironmentDTO, devopsDeployGroupVO, deployment);
            // 构建podTemplate相关配置
            addContainerConfig(projectDTO, devopsEnvironmentDTO, devopsDeployGroupVO, deployment);
        } catch (IOException e) {
            throw new CommonException("error.parse.config", e.getMessage());
        }
        JSON json = new JSON();
        String jsonStr = json.serialize(deployment);
        try {
            return JsonYamlConversionUtil.json2yaml(jsonStr);
        } catch (IOException e) {
            throw new CommonException("error.dump.deployment.to.yaml", e);
        }
    }

    private V1beta2Deployment addAppConfig(ProjectDTO projectDTO, DevopsEnvironmentDTO
            devopsEnvironmentDTO, DevopsDeployGroupVO devopsDeployGroupVO, V1beta2Deployment deployment) throws IOException {
        DevopsDeployGroupAppConfigVO devopsDeployGroupAppConfigVO = devopsDeployGroupVO.getAppConfig();
        // 设置名称、labels、annotations
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(devopsDeployGroupVO.getAppCode());
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
            rollingUpdate.setMaxUnavailable(new IntOrString(devopsDeployGroupAppConfigVO.getMaxUnavailable()));
        }
        v1beta2DeploymentStrategy.setRollingUpdate(rollingUpdate);
        v1beta2DeploymentSpec.setStrategy(v1beta2DeploymentStrategy);

        // 设置dns策略
        V1PodSpec v1PodSpec = new V1PodSpec();
        v1PodSpec.setDnsPolicy(devopsDeployGroupAppConfigVO.getDnsPolicy());
        V1PodDNSConfig v1PodDNSConfig = new V1PodDNSConfig();
        if (!StringUtils.isEmpty(devopsDeployGroupAppConfigVO.getNameServers())) {
            v1PodDNSConfig.setNameservers(Arrays.asList(devopsDeployGroupAppConfigVO.getNameServers().split(SEPARATOR)));
        }
        if (!StringUtils.isEmpty(devopsDeployGroupAppConfigVO.getSearches())) {
            v1PodDNSConfig.setSearches(Arrays.asList(devopsDeployGroupAppConfigVO.getSearches().split(SEPARATOR)));
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
                hostAlias.setHostnames(Arrays.asList(hostNames.split(SEPARATOR)));
                hostAliasList.add(hostAlias);
            });
            v1PodSpec.setHostAliases(hostAliasList);
        }

        Map<String, String> matchLabels = new HashMap<>();
        matchLabels.put("choerodon.io/application-name", devopsDeployGroupVO.getAppCode());

        // 设置pod labels
        V1PodTemplateSpec v1PodTemplateSpec = new V1PodTemplateSpec();
        V1ObjectMeta templatePodMetadata = new V1ObjectMeta();
        templatePodMetadata.setLabels(matchLabels);
        v1PodTemplateSpec.setMetadata(templatePodMetadata);
        v1PodTemplateSpec.setSpec(v1PodSpec);
        v1beta2DeploymentSpec.setTemplate(v1PodTemplateSpec);
        // 设置pod selector
        V1LabelSelector selector = new V1LabelSelector();
        selector.setMatchLabels(matchLabels);
        v1beta2DeploymentSpec.setSelector(selector);

        deployment.metadata(metadata).spec(v1beta2DeploymentSpec);

        return deployment;
    }

    private V1beta2Deployment addContainerConfig(ProjectDTO projectDTO, DevopsEnvironmentDTO
            devopsEnvironmentDTO, DevopsDeployGroupVO devopsDeployGroupVO, V1beta2Deployment deployment) throws IOException {
        List<DevopsDeployGroupContainerConfigVO> devopsDeployGroupContainerConfigVOS = devopsDeployGroupVO.getContainerConfig();
        List<V1Container> containers = new ArrayList<>();
        boolean hasJarRdupm = false;
        List<String> initContainerCommands = new ArrayList<>();
        initContainerCommands.add("/bin/bash");
        initContainerCommands.add("-c");
        StringBuilder wgetCommandSB = new StringBuilder();

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
                    containers.add(processJarConfig(projectDTO, devopsDeployGroupContainerConfigVO, v1Container, wgetCommandSB));
                    break;
                default:
                    throw new CommonException("error.unsupported.rdupm.type");
            }
        }
        initContainerCommands.add(wgetCommandSB.toString());
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
            v1VolumeMount.setName(VOLUME_MOUNt_NAME);
            v1VolumeMountList.add(v1VolumeMount);
            v1InitContainer.setVolumeMounts(v1VolumeMountList);

            initContainers.add(v1InitContainer);

            v1PodSpec.setInitContainers(initContainers);

            // 设置卷挂载
            List<V1Volume> v1VolumeList = new ArrayList<>();
            V1Volume v1Volume = new V1Volume();
            v1Volume.setName(VOLUME_MOUNt_NAME);
            v1Volume.setEmptyDir(new V1EmptyDirVolumeSource());
            v1VolumeList.add(v1Volume);
            v1PodSpec.setVolumes(v1VolumeList);
        }

        // 设置镜像拉取secret
        v1PodSpec.setImagePullSecrets(imagePullSecrets);

        return deployment;
    }

    /**
     * 校验配置是否满足要求
     *
     * @param devopsDeployGroupVO
     */
    private void validateConfig(DevopsDeployGroupVO devopsDeployGroupVO, boolean onlyForContainer) {
        // 如果不仅是更新容器，还要更新应用配置以及应用名称，将进行以下校验
        if (!onlyForContainer) {
            if (StringUtils.isEmpty(devopsDeployGroupVO.getAppName())) {
                throw new CommonException("error.app.instance.name.null");
            }

            if (devopsDeployGroupVO.getAppName().length() < 1 || devopsDeployGroupVO.getAppName().length() > 64) {
                throw new CommonException("error.env.app.center.name.length");
            }

            if (StringUtils.isEmpty(devopsDeployGroupVO.getAppCode())) {
                throw new CommonException("error.app.instance.code.null");
            }

            if (devopsDeployGroupVO.getAppCode().length() < 1 || devopsDeployGroupVO.getAppCode().length() > 64) {
                throw new CommonException("error.env.app.center.code.length");
            }


            DevopsDeployGroupAppConfigVO appConfig = devopsDeployGroupVO.getAppConfig();

            appConfig.getLabels().forEach((key, value) -> {
                if (!K8sUtil.LABEL_NAME_PATTERN.matcher(key).matches()) {
                    throw new CommonException("error.app.config.label.name.illegal");
                }
            });

            appConfig.getAnnotations().forEach((key, value) -> {
                if (!K8sUtil.ANNOTATION_NAME_PATTERN.matcher(key).matches()) {
                    throw new CommonException("error.app.config.annotation.name.illegal");
                }
            });

            if (!StringUtils.isEmpty(appConfig.getNameServers()) && appConfig.getNameServers().split(SEPARATOR).length > 3) {
                throw new CommonException("error.app.config.nameservers.length");
            }

            if (!StringUtils.isEmpty(appConfig.getSearches()) && appConfig.getSearches().split(SEPARATOR).length > 6) {
                throw new CommonException("error.app.config.searches.length");
            }
        }

        List<DevopsDeployGroupContainerConfigVO> devopsDeployGroupContainerConfigVOList = devopsDeployGroupVO.getContainerConfig();

        List<String> existPorts = new ArrayList<>();
        devopsDeployGroupContainerConfigVOList.forEach(containerConfig -> {

            if (containerConfig.getName().length() > 64) {
                throw new CommonException("error.container.config.name.length");
            }

            if (!K8sUtil.NAME_PATTERN.matcher(containerConfig.getName()).matches()) {
                throw new CommonException("error.container.config.name.illegal");
            }

            if (!StringUtils.isEmpty(containerConfig.getRequestCpu()) && !StringUtils.isEmpty(containerConfig.getLimitCpu())) {
                if (Integer.parseInt(containerConfig.getRequestCpu()) > Integer.parseInt(containerConfig.getLimitCpu())) {
                    throw new CommonException("error.container.config.cpu.request.more.than.limit");
                }
            }

            if (!StringUtils.isEmpty(containerConfig.getRequestMemory()) && !StringUtils.isEmpty(containerConfig.getLimitMemory())) {
                if (Integer.parseInt(containerConfig.getRequestMemory()) > Integer.parseInt(containerConfig.getLimitMemory())) {
                    throw new CommonException("error.container.config.memory.request.more.than.limit");
                }
            }

            if (!CollectionUtils.isEmpty(containerConfig.getPorts())) {
                containerConfig.getPorts().forEach(portInfo -> {
                    String name = portInfo.get("name");
                    String port = portInfo.get("containerPort");
                    String namePort = name + port;
                    if (existPorts.contains(namePort)) {
                        throw new CommonException("error.container.port.exist");
                    }
                    existPorts.add(namePort);
                    if (name.length() > 40) {
                        throw new CommonException("error.container.port.name.length");
                    }
                    if (Integer.parseInt(port) < 1 || Integer.parseInt(port) > 65535) {
                        throw new CommonException("error.container.port.range");
                    }
                });
            }
        });
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
            requests.put("cpu", new Quantity(convertCpuResource(devopsDeployGroupContainerConfigVO.getRequestCpu()), Quantity.Format.BINARY_SI));
        }
        if (!StringUtils.isEmpty(devopsDeployGroupContainerConfigVO.getRequestCpu())) {
            requests.put("memory", new Quantity(convertMemoryResource(devopsDeployGroupContainerConfigVO.getRequestMemory()), Quantity.Format.BINARY_SI));
        }

        Map<String, Quantity> limits = new HashMap<>();
        if (!StringUtils.isEmpty(devopsDeployGroupContainerConfigVO.getRequestCpu())) {
            limits.put("cpu", new Quantity(convertCpuResource(devopsDeployGroupContainerConfigVO.getLimitCpu()), Quantity.Format.BINARY_SI));
        }
        if (!StringUtils.isEmpty(devopsDeployGroupContainerConfigVO.getRequestCpu())) {
            limits.put("memory", new Quantity(convertMemoryResource(devopsDeployGroupContainerConfigVO.getLimitMemory()), Quantity.Format.BINARY_SI));
        }
        resourceRequirements.setLimits(limits);
        resourceRequirements.setRequests(requests);

        v1Container.setResources(resourceRequirements);

        // 设置端口
        if (!CollectionUtils.isEmpty(devopsDeployGroupContainerConfigVO.getPorts())) {
            List<V1ContainerPort> containerPortList = new ArrayList<>();

            devopsDeployGroupContainerConfigVO.getPorts().forEach(port -> {
                V1ContainerPort v1ContainerPort = new V1ContainerPort();
                v1ContainerPort.setName(port.get("name"));
                v1ContainerPort.setProtocol(port.get("protocol"));
                v1ContainerPort.setContainerPort(Integer.valueOf(port.get("containerPort")));
                containerPortList.add(v1ContainerPort);
            });
            v1Container.setPorts(containerPortList);
        }

        return v1Container;
    }

    /**
     * 根据jar信息配置容器信息，包括下载jar包、上传jar包、java运行的container（设置image字段）
     *
     * @param devopsDeployGroupContainerConfigVO
     * @param v1Container
     * @param wgetCommandSB
     * @return
     */
    V1Container processJarConfig(ProjectDTO projectDTO, DevopsDeployGroupContainerConfigVO
            devopsDeployGroupContainerConfigVO, V1Container v1Container, StringBuilder wgetCommandSB) {
        // 处理用户上传的jar
        if (AppSourceType.UPLOAD.getValue().equals(devopsDeployGroupContainerConfigVO.getSourceType())) {
            wgetCommandSB.append(String.format(WGET_COMMAND_TEMPLATE, devopsDeployGroupContainerConfigVO.getJarDeployVO().getFileInfoVO().getJarFileUrl(), devopsDeployGroupContainerConfigVO.getName() + ".jar")).append(";");
        } else {
            // 制品库中的jar
            JarPullInfoDTO jarPullInfoDTO = getJarPullInfo(projectDTO, devopsDeployGroupContainerConfigVO.getJarDeployVO());
            wgetCommandSB.append(String.format(WGET_COMMAND_WITH_AUTHENTICATION_TEMPLATE, jarPullInfoDTO.getPullUserId(), jarPullInfoDTO.getPullUserPassword(), jarPullInfoDTO.getDownloadUrl(), devopsDeployGroupContainerConfigVO.getName() + ".jar")).append(";");
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
        v1VolumeMount.setName(VOLUME_MOUNt_NAME);
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
    private V1Container processImageConfig(ProjectDTO projectDTO, DevopsEnvironmentDTO
            devopsEnvironmentDTO, DevopsDeployGroupContainerConfigVO devopsDeployGroupContainerConfigVO, V1Container
                                                   v1Container, List<V1LocalObjectReference> imagePullSecrets) {
        DevopsDeployGroupDockerDeployVO dockerDeployVO = devopsDeployGroupContainerConfigVO.getDockerDeployVO();

        DockerDeployDTO dockerDeployDTO = createDockerDeployDTO(projectDTO, devopsEnvironmentDTO, dockerDeployVO);
        DockerPullAccountDTO pullAccountDTO = dockerDeployDTO.getDockerPullAccountDTO();
        if (pullAccountDTO != null) {
            ConfigVO configVO = new ConfigVO();
            configVO.setUrl(pullAccountDTO.getHarborUrl());
            configVO.setUserName(pullAccountDTO.getPullAccount());
            configVO.setPassword(pullAccountDTO.getPullPassword());
            String imagePullSecretName = String.format("%s%s", "secret-", GenerateUUID.generateUUID().substring(0, 20));
            V1LocalObjectReference v1LocalObjectReference = new V1LocalObjectReference();
            v1LocalObjectReference.setName(imagePullSecretName);
            imagePullSecrets.add(v1LocalObjectReference);
            agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), imagePullSecretName, configVO);
        }

        v1Container.setName(devopsDeployGroupContainerConfigVO.getName());
        v1Container.setImage(dockerDeployDTO.getImage());
        v1Container.setImagePullPolicy(IF_NOT_PRESENT);
        return v1Container;
    }

    private JarPullInfoDTO getJarPullInfo(ProjectDTO projectDTO, JarDeployVO jarDeployVO) {
        String sourceType = jarDeployVO.getSourceType();
        List<C7nNexusComponentDTO> nexusComponentDTOList = new ArrayList<>();
        List<NexusMavenRepoDTO> mavenRepoDTOList = new ArrayList<>();
        if (StringUtils.endsWithIgnoreCase(AppSourceType.MARKET.getValue(), sourceType)
                || StringUtils.endsWithIgnoreCase(AppSourceType.HZERO.getValue(), sourceType)) {
            MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectDTO.getId()), Objects.requireNonNull(jarDeployVO.getMarketDeployObjectInfoVO().getMktDeployObjectId()));
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

    private DockerDeployDTO createDockerDeployDTO(ProjectDTO projectDTO, DevopsEnvironmentDTO
            devopsEnvironmentDTO, DevopsDeployGroupDockerDeployVO dockerDeployVO) {
        DockerDeployDTO dockerDeployDTO = new DockerDeployDTO();
        DockerPullAccountDTO dockerPullAccountDTO = new DockerPullAccountDTO();
        if (isMarketOrHzero(dockerDeployVO)) {
            MarketServiceDeployObjectVO marketServiceDeployObjectVO = getMarketServiceDeployObjectVO(projectDTO.getId(), dockerDeployVO);
            MarketHarborConfigVO marketHarborConfigVO = marketServiceDeployObjectVO.getMarketHarborConfigVO();
            dockerPullAccountDTO = initDockerPullAccountDTO(marketHarborConfigVO);
            dockerDeployDTO.setDockerPullAccountDTO(dockerPullAccountDTO);
            dockerDeployDTO.setImage(marketServiceDeployObjectVO.getMarketDockerImageUrl());
        } else if (AppSourceType.CURRENT_PROJECT.getValue().equals(dockerDeployVO.getSourceType())) {
            HarborC7nRepoImageTagVo harborC7nRepoImageTagVo = getHarborC7nRepoImageTagVo(dockerDeployVO);
            dockerPullAccountDTO.setPullAccount(harborC7nRepoImageTagVo.getPullAccount());
            dockerPullAccountDTO.setPullPassword(harborC7nRepoImageTagVo.getPullPassword());
            dockerDeployDTO.setImage(harborC7nRepoImageTagVo.getImageTagList().get(0).getPullCmd().replace("docker pull", "").trim());
            dockerDeployDTO.setDockerPullAccountDTO(dockerPullAccountDTO);
        } else if (AppSourceType.SHARE.getValue().equals(dockerDeployVO.getSourceType())) {
            AppServiceDTO appServiceDTO = appServiceService.baseQuery(dockerDeployVO.getAppServiceId());
            //如果应用绑定了私有镜像库,则处理secret
            AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(dockerDeployVO.getAppServiceVersionId());
            dockerPullAccountDTO = getShareServiceDockerPullAccount(appServiceDTO, appServiceVersionDTO);
            dockerDeployDTO.setDockerPullAccountDTO(dockerPullAccountDTO);
            dockerDeployDTO.setImage(appServiceVersionDTO.getImage());
        } else {
            // 剩下的情况是自定义仓库
            if (dockerDeployVO.getImageInfo().getPrivateRepository()) {
                dockerPullAccountDTO.setPullAccount(dockerDeployVO.getImageInfo().getUsername());
                dockerPullAccountDTO.setPullPassword(dockerDeployVO.getImageInfo().getPassword());
                dockerDeployDTO.setDockerPullAccountDTO(dockerPullAccountDTO);
            }
            dockerDeployDTO.setImage(dockerDeployVO.getImageInfo().getCustomImageName() + ":" + dockerDeployVO.getImageInfo().getTag());
        }

        return dockerDeployDTO;
    }

    private HarborC7nRepoImageTagVo getHarborC7nRepoImageTagVo(DockerDeployVO dockerDeployVO) {
        HarborC7nRepoImageTagVo imageTagVo = rdupmClientOperator.listImageTag(dockerDeployVO.getImageInfo().getRepoType(), TypeUtil.objToLong(dockerDeployVO.getImageInfo().getRepoId()), dockerDeployVO.getImageInfo().getImageName(), dockerDeployVO.getImageInfo().getTag());
        if (CollectionUtils.isEmpty(imageTagVo.getImageTagList())) {
            throw new CommonException(ERROR_IMAGE_TAG_NOT_FOUND);
        }
        return imageTagVo;
    }

    private DockerPullAccountDTO getShareServiceDockerPullAccount(AppServiceDTO appServiceDTO, AppServiceVersionDTO
            appServiceVersionDTO) {
        DevopsConfigDTO devopsConfigDTO;
        if (appServiceVersionDTO.getHarborConfigId() != null) {
            devopsConfigDTO = harborService.queryRepoConfigByIdToDevopsConfig(appServiceDTO.getId(), appServiceDTO.getProjectId(),
                    appServiceVersionDTO.getHarborConfigId(), appServiceVersionDTO.getRepoType(), AUTH_TYPE);
        } else {
            //查询harbor的用户名密码
            devopsConfigDTO = harborService.queryRepoConfigToDevopsConfig(appServiceDTO.getProjectId(),
                    appServiceDTO.getId(), AUTH_TYPE);
        }
        LOGGER.debug("Docker config for app service with id {} and code {} and version id: {} is not null. And the config id is {}...", appServiceDTO.getId(), appServiceDTO.getCode(), appServiceVersionDTO.getId(), devopsConfigDTO.getId());

        ConfigVO configVO = JsonHelper.unmarshalByJackson(devopsConfigDTO.getConfig(), ConfigVO.class);
        if (configVO.getIsPrivate() != null && configVO.getIsPrivate()) {
            LOGGER.debug("Docker config for app service with id {} and code {} and version id: {} is private.", appServiceDTO.getId(), appServiceDTO.getCode(), appServiceVersionDTO.getId());
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

    private MarketServiceDeployObjectVO getMarketServiceDeployObjectVO(Long projectId, DockerDeployVO
            dockerDeployVO) {
        MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(dockerDeployVO.getDeployObjectId()));
        if (Objects.isNull(marketServiceDeployObjectVO.getMarketHarborConfigVO())) {
            throw new CommonException("error.harbor.deploy.object.not.exist");
        }
        return marketServiceDeployObjectVO;
    }

    /**
     * 前端界面展示的CPU单位是m，比如填写100，就是100m。
     * 由于这里序列化不容易添加单位，就直接用小数来表示CPU核数。
     * 具体可参考 https://kubernetes.io/zh/docs/concepts/configuration/manage-resources-containers/#meaning-of-cpu
     *
     * @param cpu
     * @return
     */
    private BigDecimal convertCpuResource(String cpu) {
        return new BigDecimal(cpu).divide(new BigDecimal(1000));
    }

    /**
     * 前端界面展示的MEMORY单位是Mi，比如填写100，就是100Mi。
     * 由于这里序列化不容易添加单位，就直接用数字来表示内存大小，1Mi=1024*1024 100Mi=100*1024*1024
     * 具体可参考 https://kubernetes.io/zh/docs/concepts/configuration/manage-resources-containers/#meaning-of-memory
     *
     * @param memory
     * @return
     */
    private BigDecimal convertMemoryResource(String memory) {
        return new BigDecimal(Long.parseLong(memory) * 1024 * 1024);
    }

}
