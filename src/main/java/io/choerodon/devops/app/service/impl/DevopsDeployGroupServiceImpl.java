package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.CUSTOM_REPO;
import static io.choerodon.devops.app.service.AppServiceInstanceService.PARENT_WORK_LOAD_LABEL;
import static io.choerodon.devops.app.service.AppServiceInstanceService.PARENT_WORK_LOAD_NAME_LABEL;
import static io.choerodon.devops.infra.enums.ResourceType.DEPLOYMENT;
import static io.choerodon.devops.infra.util.K8sUtil.checkPortName;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.models.*;
import org.hzero.core.util.EncryptionUtils;
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
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.api.vo.harbor.HarborCustomRepo;
import io.choerodon.devops.api.vo.harbor.ProdImageInfoVO;
import io.choerodon.devops.api.vo.hrds.HarborC7nRepoImageTagVo;
import io.choerodon.devops.api.vo.market.JarReleaseConfigVO;
import io.choerodon.devops.api.vo.market.MarketMavenConfigVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.*;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.DevopsRegistryRepoType;
import io.choerodon.devops.infra.enums.WorkloadSourceTypeEnums;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
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

    private static final String WGET_COMMAND_TEMPLATE = "wget %s -O /choerodon/%s";
    private static final String WGET_COMMAND_WITH_AUTHENTICATION_TEMPLATE = "wget --user=%s --password=%s %s -O /choerodon/%s";
    private static final String ERROR_IMAGE_TAG_NOT_FOUND = "devops.image.tag.not.found";
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
    private DevopsDeploymentService devopsDeploymentService;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsRegistrySecretService devopsRegistrySecretService;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;

    @Transactional
    @Override
    public DevopsDeployAppCenterEnvVO createOrUpdate(Long projectId, DevopsDeployGroupVO devopsDeployGroupVO, String operateType, boolean onlyForContainer, Boolean fromPipeline) {
        //1. 查询校验环境
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.getProjectEnvironment(projectId, devopsDeployGroupVO.getEnvironmentId());

        // 2.校验用户是否拥有环境权限
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
        devopsDeployGroupVO.setProjectId(projectId);
        // 3.校验配置
        validateConfig(devopsDeployGroupVO, operateType, onlyForContainer);

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsDeployGroupVO.getProjectId());

        // 4.生成deployment
        String deployment = buildDeploymentYaml(projectDTO, devopsEnvironmentDTO, devopsDeployGroupVO);
        // 5.创建deployment
        WorkloadBaseCreateOrUpdateVO workloadBaseCreateOrUpdateVO = new WorkloadBaseCreateOrUpdateVO();
        workloadBaseCreateOrUpdateVO.setEnvId(String.valueOf(devopsDeployGroupVO.getEnvironmentId()));
        workloadBaseCreateOrUpdateVO.setOperateType(operateType);
        workloadBaseCreateOrUpdateVO.setContent(deployment);
        if (devopsDeployGroupVO.getInstanceId() != null) {
            workloadBaseCreateOrUpdateVO.setResourceId(String.valueOf(devopsDeployGroupVO.getInstanceId()));
        }
        workloadBaseCreateOrUpdateVO.setToDecrypt(false);
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put(DevopsDeploymentServiceImpl.EXTRA_INFO_KEY_APP_CONFIG, JsonHelper.marshalByJackson(devopsDeployGroupVO.getAppConfig()));
        extraInfo.put(DevopsDeploymentServiceImpl.EXTRA_INFO_KEY_CONTAINER_CONFIG, JsonHelper.marshalByJackson(devopsDeployGroupVO.getContainerConfig()));
        extraInfo.put(DevopsDeploymentServiceImpl.EXTRA_INFO_KEY_SOURCE_TYPE, WorkloadSourceTypeEnums.DEPLOY_GROUP.getType());

        workloadBaseCreateOrUpdateVO.setExtraInfo(extraInfo);
        DevopsEnvCommandDTO devopsEnvCommandDTO = workloadService.createOrUpdate(projectId, workloadBaseCreateOrUpdateVO, null, DEPLOYMENT, fromPipeline);

        // 更新关联的对象id
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO;
        if (MiscConstants.CREATE_TYPE.equals(operateType)) {
            DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.baseQueryByEnvIdAndName(devopsDeployGroupVO.getEnvironmentId(), devopsDeployGroupVO.getAppCode());
            // 插入应用记录
            devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.baseCreate(devopsDeployGroupVO.getAppName(), devopsDeployGroupVO.getAppCode(), projectId, devopsDeploymentDTO.getId(), devopsDeployGroupVO.getEnvironmentId(), OperationTypeEnum.CREATE_APP.value(), "", RdupmTypeEnum.DEPLOYMENT.value());

            devopsDeploymentDTO.setInstanceId(devopsDeployAppCenterEnvDTO.getId());
            devopsDeploymentService.baseUpdate(devopsDeploymentDTO);
        } else {
            // 更新应用记录
            devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByEnvIdAndCode(devopsDeployGroupVO.getEnvironmentId(), devopsDeployGroupVO.getAppCode());
            // 如果名称发生变化，更新名称
            if (!devopsDeployAppCenterEnvDTO.getName().equals(devopsDeployGroupVO.getAppName())) {
                devopsDeployAppCenterEnvDTO.setName(devopsDeployGroupVO.getAppName());
                devopsDeployAppCenterService.baseUpdate(devopsDeployAppCenterEnvDTO);
            } else {
                devopsDeployAppCenterEnvDTO.setLastUpdatedBy(DetailsHelper.getUserDetails().getUserId());
                devopsDeployAppCenterEnvDTO.setLastUpdateDate(new Date());
                devopsDeployAppCenterService.baseUpdate(devopsDeployAppCenterEnvDTO);
            }
        }
        DevopsDeployAppCenterEnvVO devopsDeployAppCenterEnvVO = ConvertUtils.convertObject(devopsDeployAppCenterEnvDTO, DevopsDeployAppCenterEnvVO.class);
        if (devopsEnvCommandDTO == null) {
            return devopsDeployAppCenterEnvVO;
        }
        devopsDeployAppCenterEnvVO.setCommandId(devopsEnvCommandDTO.getId());

        // 插入部署记录
        devopsDeployRecordService.saveRecord(
                devopsEnvironmentDTO.getProjectId(),
                fromPipeline ? DeployType.AUTO : DeployType.MANUAL,
                devopsEnvCommandDTO.getId(),
                DeployModeEnum.ENV,
                devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getName(),
                null,
                DeployObjectTypeEnum.DEPLOYMENT,
                devopsDeployGroupVO.getAppName(),
                "",
                devopsDeployGroupVO.getAppName(),
                devopsDeployGroupVO.getAppCode(),
                devopsDeployAppCenterEnvVO.getId(),
                new DeploySourceVO(AppSourceType.DEPLOYMENT, projectDTO.getName()));

        return devopsDeployAppCenterEnvVO;
    }

    @Transactional
    @Override
    public DevopsDeployAppCenterEnvVO updateContainer(Long projectId, DevopsDeployGroupVO devopsDeployGroupVO) {
        // 设置appConfig
        DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.selectByPrimaryKey(devopsDeployGroupVO.getInstanceId());
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(devopsDeploymentDTO.getInstanceId());

        devopsDeployGroupVO.setAppConfig(JsonHelper.unmarshalByJackson(devopsDeploymentDTO.getAppConfig(), DevopsDeployGroupAppConfigVO.class));
        devopsDeployGroupVO.setAppName(devopsDeployAppCenterEnvDTO.getName());
        devopsDeployGroupVO.setAppCode(devopsDeployAppCenterEnvDTO.getCode());

        return createOrUpdate(projectId, devopsDeployGroupVO, MiscConstants.UPDATE_TYPE, true, false);
    }

    public String buildDeploymentYaml(ProjectDTO projectDTO, DevopsEnvironmentDTO
            devopsEnvironmentDTO, DevopsDeployGroupVO devopsDeployGroupVO) {
        V1Deployment deployment = new V1Deployment();
        deployment.setKind(DEPLOYMENT.getType());
        deployment.setApiVersion("apps/v1");
        try {
            // 构建deployment相关配置
            addAppConfig(projectDTO, devopsEnvironmentDTO, devopsDeployGroupVO, deployment);
            // 构建podTemplate相关配置
            addContainerConfig(projectDTO, devopsEnvironmentDTO, devopsDeployGroupVO, deployment);
        } catch (IOException e) {
            throw new CommonException("devops.parse.config", e.getMessage());
        }
        JSON json = new JSON();
        String jsonStr = json.serialize(deployment);
        try {
            return JsonYamlConversionUtil.json2yaml(jsonStr);
        } catch (IOException e) {
            throw new CommonException("devops.dump.deployment.to.yaml", e);
        }
    }

    private V1Deployment addAppConfig(ProjectDTO projectDTO, DevopsEnvironmentDTO
            devopsEnvironmentDTO, DevopsDeployGroupVO devopsDeployGroupVO, V1Deployment deployment) throws IOException {
        DevopsDeployGroupAppConfigVO devopsDeployGroupAppConfigVO = devopsDeployGroupVO.getAppConfig();
        // 设置名称、labels、annotations
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(devopsDeployGroupVO.getAppCode());
        Map<String, String> annotations = new HashMap<>();
        annotations.put("choerodon.io/modify-time", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        if (!CollectionUtils.isEmpty(devopsDeployGroupAppConfigVO.getLabels())) {
            metadata.setLabels(devopsDeployGroupAppConfigVO.getLabels());
        }
        if (!CollectionUtils.isEmpty(devopsDeployGroupAppConfigVO.getAnnotations())) {
            annotations.putAll(devopsDeployGroupAppConfigVO.getAnnotations());
        }
        metadata.setAnnotations(annotations);
        deployment.setMetadata(metadata);

        // 设置spec
        V1DeploymentSpec v1beta2DeploymentSpec = new V1DeploymentSpec();
        v1beta2DeploymentSpec.setReplicas(devopsDeployGroupAppConfigVO.getReplicas());

        // 设置升级策略
        V1DeploymentStrategy v1beta2DeploymentStrategy = new V1DeploymentStrategy();
        v1beta2DeploymentStrategy.setType(devopsDeployGroupAppConfigVO.getStrategyType());
        if (devopsDeployGroupAppConfigVO.getStrategyType().equals("RollingUpdate")) {
            V1RollingUpdateDeployment rollingUpdate = new V1RollingUpdateDeployment();
            if (devopsDeployGroupAppConfigVO.getMaxSurge() != null) {
                rollingUpdate.setMaxSurge(new IntOrString(devopsDeployGroupAppConfigVO.getMaxSurge()));
            }
            if (devopsDeployGroupAppConfigVO.getMaxUnavailable() != null) {
                rollingUpdate.setMaxUnavailable(new IntOrString(devopsDeployGroupAppConfigVO.getMaxUnavailable()));
            }
            v1beta2DeploymentStrategy.setRollingUpdate(rollingUpdate);
        }
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
        matchLabels.put(PARENT_WORK_LOAD_NAME_LABEL, devopsDeployGroupVO.getAppCode());
        matchLabels.put(PARENT_WORK_LOAD_LABEL, DEPLOYMENT.getType());

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

    private V1Deployment addContainerConfig(ProjectDTO projectDTO, DevopsEnvironmentDTO
            devopsEnvironmentDTO, DevopsDeployGroupVO devopsDeployGroupVO, V1Deployment deployment) throws IOException {
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
                    throw new CommonException("devops.unsupported.rdupm.type");
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
    private void validateConfig(DevopsDeployGroupVO devopsDeployGroupVO, String operateType, boolean onlyForContainer) {
        // 如果不仅是更新容器，还要更新应用配置以及应用名称，将进行以下校验
        if (!onlyForContainer) {
            // 创建操作需要校验name和code
            // 更新操作只需要校验name
            if (MiscConstants.CREATE_TYPE.equals(operateType)) {
                devopsDeployAppCenterService.checkNameAndCodeUniqueAndThrow(devopsDeployGroupVO.getEnvironmentId(), RdupmTypeEnum.DEPLOYMENT.value(), devopsDeployGroupVO.getInstanceId(), devopsDeployGroupVO.getAppName(), devopsDeployGroupVO.getAppCode());
            } else {
                devopsDeployAppCenterService.checkNameUniqueAndThrow(devopsDeployGroupVO.getEnvironmentId(), RdupmTypeEnum.DEPLOYMENT.value(), devopsDeployGroupVO.getInstanceId(), devopsDeployGroupVO.getAppName());
            }

            if (StringUtils.isEmpty(devopsDeployGroupVO.getAppName())) {
                throw new CommonException("devops.env.app.center.name.null");
            }

            if (devopsDeployGroupVO.getAppName().length() < 1 || devopsDeployGroupVO.getAppName().length() > 53) {
                throw new CommonException("devops.env.app.center.name.length");
            }

            if (StringUtils.isEmpty(devopsDeployGroupVO.getAppCode())) {
                throw new CommonException("devops.env.app.center.code.null");
            }

            if (devopsDeployGroupVO.getAppCode().length() < 1 || devopsDeployGroupVO.getAppCode().length() > 53) {
                throw new CommonException("devops.env.app.center.code.length");
            }


            DevopsDeployGroupAppConfigVO appConfig = devopsDeployGroupVO.getAppConfig();

            appConfig.getLabels().forEach((key, value) -> {
                if (!K8sUtil.LABEL_NAME_PATTERN.matcher(key).matches()) {
                    throw new CommonException("devops.app.config.label.name.illegal");
                }
            });

            appConfig.getAnnotations().forEach((key, value) -> {
                if (!K8sUtil.ANNOTATION_NAME_PATTERN.matcher(key).matches()) {
                    throw new CommonException("devops.app.config.annotation.name.illegal");
                }
            });

            if (!StringUtils.isEmpty(appConfig.getNameServers()) && appConfig.getNameServers().split(SEPARATOR).length > 3) {
                throw new CommonException("devops.app.config.nameservers.length");
            }

            if (!StringUtils.isEmpty(appConfig.getSearches()) && appConfig.getSearches().split(SEPARATOR).length > 6) {
                throw new CommonException("devops.app.config.searches.length");
            }
        }

        List<DevopsDeployGroupContainerConfigVO> devopsDeployGroupContainerConfigVOList = devopsDeployGroupVO.getContainerConfig();

        List<String> existPorts = new ArrayList<>();
        List<String> existContainerNames = new ArrayList<>();
        devopsDeployGroupContainerConfigVOList.forEach(containerConfig -> {

            if (containerConfig.getName().length() > 64) {
                throw new CommonException("devops.container.config.name.length");
            }

            if (!K8sUtil.NAME_PATTERN.matcher(containerConfig.getName()).matches()) {
                throw new CommonException("devops.container.config.name.illegal");
            }

            if (existContainerNames.contains(containerConfig.getName())) {
                throw new CommonException("devops.container.name.exists");
            }

            existContainerNames.add(containerConfig.getName());

            if (!StringUtils.isEmpty(containerConfig.getRequestCpu()) && !StringUtils.isEmpty(containerConfig.getLimitCpu())) {
                if (Integer.parseInt(containerConfig.getRequestCpu()) > Integer.parseInt(containerConfig.getLimitCpu())) {
                    throw new CommonException("devops.container.config.cpu.request.more.than.limit");
                }
            }

            if (!StringUtils.isEmpty(containerConfig.getRequestMemory()) && !StringUtils.isEmpty(containerConfig.getLimitMemory())) {
                if (Integer.parseInt(containerConfig.getRequestMemory()) > Integer.parseInt(containerConfig.getLimitMemory())) {
                    throw new CommonException("devops.container.config.memory.request.more.than.limit");
                }
            }

            if (!CollectionUtils.isEmpty(containerConfig.getPorts())) {
                List<Map<String, String>> filteredPorts = new ArrayList<>();
                for (Map<String, String> portInfo : containerConfig.getPorts()) {
                    String name = portInfo.get("name");
                    String port = portInfo.get("containerPort");
                    String protocol = portInfo.get("protocol");
                    // 三个字段，任意一个为空就跳过校验
                    if (StringUtils.isEmpty(name) || StringUtils.isEmpty(port) || StringUtils.isEmpty(protocol)) {
                        break;
                    }

                    checkPortName(name);

                    String namePort = name + port;
                    if (existPorts.contains(namePort)) {
                        throw new CommonException("devops.container.port.exist");
                    }
                    existPorts.add(namePort);

                    if (Integer.parseInt(port) < 1 || Integer.parseInt(port) > 65535) {
                        throw new CommonException("devops.container.port.range");
                    }
                    filteredPorts.add(portInfo);
                }
                containerConfig.setPorts(filteredPorts);
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
        if (!StringUtils.isEmpty(devopsDeployGroupContainerConfigVO.getRequestMemory())) {
            requests.put("memory", new Quantity(convertMemoryResource(devopsDeployGroupContainerConfigVO.getRequestMemory()), Quantity.Format.BINARY_SI));
        }

        Map<String, Quantity> limits = new HashMap<>();
        if (!StringUtils.isEmpty(devopsDeployGroupContainerConfigVO.getLimitCpu())) {
            limits.put("cpu", new Quantity(convertCpuResource(devopsDeployGroupContainerConfigVO.getLimitCpu()), Quantity.Format.BINARY_SI));
        }
        if (!StringUtils.isEmpty(devopsDeployGroupContainerConfigVO.getLimitMemory())) {
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
            V1LocalObjectReference v1LocalObjectReference = new V1LocalObjectReference();
            v1LocalObjectReference.setName(pullAccountDTO.getSecretCode());
            imagePullSecrets.add(v1LocalObjectReference);
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
            MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectDTO.getId()), Objects.requireNonNull(jarDeployVO.getDeployObjectId()));
            JarReleaseConfigVO jarReleaseConfigVO = JsonHelper.unmarshalByJackson(marketServiceDeployObjectVO.getMarketJarLocation(), JarReleaseConfigVO.class);
            if (Objects.isNull(marketServiceDeployObjectVO.getMarketMavenConfigVO())) {
                throw new CommonException("devops.maven.deploy.object.not.exist");
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
            Long nexusRepoId = jarDeployVO.getProdJarInfoVO().getRepositoryId();
            // 0.2 从制品库获取仓库信息
            if (nexusRepoId == null) {
                JarPullInfoDTO jarPullInfoDTO = new JarPullInfoDTO();
                jarPullInfoDTO.setPullUserId(jarDeployVO.getProdJarInfoVO().getUsername());
                jarPullInfoDTO.setPullUserPassword(jarDeployVO.getProdJarInfoVO().getPassword());
                jarPullInfoDTO.setDownloadUrl(jarDeployVO.getProdJarInfoVO().getDownloadUrl());
                return jarPullInfoDTO;
            }

            String groupId = jarDeployVO.getProdJarInfoVO().getGroupId();
            String artifactId = jarDeployVO.getProdJarInfoVO().getArtifactId();
            String version = jarDeployVO.getProdJarInfoVO().getVersion();
            nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), projectDTO.getId(), nexusRepoId, groupId, artifactId, null, version);
            mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), projectDTO.getId(), Collections.singleton(nexusRepoId));

        }
        if (CollectionUtils.isEmpty(nexusComponentDTOList)) {
            throw new CommonException("devops.jar.version.not.found");
        }
        if (CollectionUtils.isEmpty(mavenRepoDTOList)) {
            throw new CommonException("devops.get.maven.config");
        }

        JarPullInfoDTO jarPullInfoDTO = new JarPullInfoDTO();
        jarPullInfoDTO.setPullUserId(mavenRepoDTOList.get(0).getNePullUserId());
        jarPullInfoDTO.setPullUserPassword(mavenRepoDTOList.get(0).getNePullUserPassword());
        jarPullInfoDTO.setDownloadUrl(nexusComponentDTOList.get(0).getDownloadUrl());
        return jarPullInfoDTO;
    }

    private DockerDeployDTO createDockerDeployDTO(ProjectDTO projectDTO, DevopsEnvironmentDTO
            devopsEnvironmentDTO, DevopsDeployGroupDockerDeployVO devopsDeployGroupDockerDeployVO) {
        DockerDeployDTO dockerDeployDTO = new DockerDeployDTO();
        DockerPullAccountDTO dockerPullAccountDTO = new DockerPullAccountDTO();
        if (isMarketOrHzero(devopsDeployGroupDockerDeployVO.getSourceType())) {
            MarketServiceDeployObjectVO marketServiceDeployObjectVO = getMarketServiceDeployObjectVO(projectDTO.getId(), devopsDeployGroupDockerDeployVO);
            dockerPullAccountDTO.setSecretCode(appServiceInstanceService.makeMarketSecret(projectDTO.getId(), devopsEnvironmentDTO, marketServiceDeployObjectVO));
            dockerDeployDTO.setImage(marketServiceDeployObjectVO.getMarketDockerImageUrl());
        } else if (AppSourceType.SHARE.getValue().equals(devopsDeployGroupDockerDeployVO.getSourceType())) {
            AppServiceDTO appServiceDTO = appServiceService.baseQuery(devopsDeployGroupDockerDeployVO.getAppServiceId());
            //如果应用绑定了私有镜像库,则处理secret
            AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(devopsDeployGroupDockerDeployVO.getAppServiceVersionId());
            dockerPullAccountDTO.setSecretCode(appServiceInstanceService.getSecret(appServiceDTO, devopsDeployGroupDockerDeployVO.getAppServiceVersionId(), devopsEnvironmentDTO));
            dockerDeployDTO.setImage(appServiceVersionDTO.getImage());
        } else {
            // 项目制品库或者用户自定义仓库
            DevopsRegistrySecretDTO devopsRegistrySecretDTO = new DevopsRegistrySecretDTO();
            devopsRegistrySecretDTO.setProjectId(projectDTO.getId());
            devopsRegistrySecretDTO.setEnvId(devopsEnvironmentDTO.getId());
            devopsRegistrySecretDTO.setNamespace(devopsEnvironmentDTO.getCode());
            devopsRegistrySecretDTO.setClusterId(devopsEnvironmentDTO.getClusterId());

            ConfigVO configVO = new ConfigVO();
            if (AppSourceType.CURRENT_PROJECT.getValue().equals(devopsDeployGroupDockerDeployVO.getSourceType())) {
                if (DevopsRegistryRepoType.DEFAULT_REPO.getType().equals(devopsDeployGroupDockerDeployVO.getImageInfo().getRepoType())) {
                    // 流水线部署组部署特殊处理，
                    ProdImageInfoVO imageInfo = devopsDeployGroupDockerDeployVO.getImageInfo();
                    String imageName = imageInfo.getImageName();
                    int i = imageName.lastIndexOf("/");
                    if (i != -1) {
                        imageInfo.setImageName(imageName.substring(i + 1));
                    }

                    HarborC7nRepoImageTagVo harborC7nRepoImageTagVo = getHarborC7nRepoImageTagVo(devopsDeployGroupDockerDeployVO);
                    configVO.setUserName(harborC7nRepoImageTagVo.getPullAccount());
                    configVO.setPassword(harborC7nRepoImageTagVo.getPullPassword());
                    configVO.setUrl(harborC7nRepoImageTagVo.getHarborUrl());
                    devopsRegistrySecretDTO.setRepoType(devopsDeployGroupDockerDeployVO.getImageInfo().getRepoType());
                    dockerDeployDTO.setImage(harborC7nRepoImageTagVo.getImageTagList().get(0).getPullCmd().replace("docker pull", "").trim());
                } else {
                    // 自定义仓库不查询镜像列表
                    HarborCustomRepo harborCustomRepo = rdupmClientOperator.queryCustomRepoById(projectDTO.getId(), devopsDeployGroupDockerDeployVO.getImageInfo().getRepoId());
                    configVO.setUserName(harborCustomRepo.getLoginName());
                    configVO.setPassword(harborCustomRepo.getPassword());
                    configVO.setUrl(harborCustomRepo.getRepoUrl());
                    devopsRegistrySecretDTO.setRepoType(devopsDeployGroupDockerDeployVO.getImageInfo().getRepoType());
                    dockerDeployDTO.setImage(devopsDeployGroupDockerDeployVO.getImageInfo().getImageUrl());
                }

            } else {
                configVO.setUserName(devopsDeployGroupDockerDeployVO.getImageInfo().getUsername());
                configVO.setPassword(devopsDeployGroupDockerDeployVO.getImageInfo().getPassword());
                configVO.setUrl(devopsDeployGroupDockerDeployVO.getImageInfo().getCustomImageName().split("/")[0]);
                devopsRegistrySecretDTO.setRepoType(CUSTOM_REPO);
                dockerDeployDTO.setImage(devopsDeployGroupDockerDeployVO.getImageInfo().getCustomImageName() + ":" + devopsDeployGroupDockerDeployVO.getImageInfo().getTag());
            }

            String secretCode = String.format("%s%s", "secret-", EncryptionUtils.MD5.encrypt(String.format("%s-%s-%s", configVO.getUrl(), configVO.getUserName(), configVO.getUserName())).substring(0, 20));

            devopsRegistrySecretDTO.setSecretCode(secretCode);
            DevopsRegistrySecretDTO existDevopsRegistrySecretDTO = devopsRegistrySecretService.baseQuery(devopsRegistrySecretDTO);
            if (existDevopsRegistrySecretDTO != null) {
                dockerPullAccountDTO.setSecretCode(secretCode);
            } else {
                //当配置在当前环境下没有创建过secret.则新增secret信息，并通知k8s创建secret
                devopsRegistrySecretDTO.setSecretDetail(JsonHelper.marshalByJackson(configVO));
                devopsRegistrySecretService.baseCreate(devopsRegistrySecretDTO);
                dockerPullAccountDTO.setSecretCode(secretCode);
            }
            agentCommandService.operateSecret(devopsRegistrySecretDTO.getClusterId(), devopsRegistrySecretDTO.getNamespace(), dockerPullAccountDTO.getSecretCode(), configVO);
        }

        dockerDeployDTO.setDockerPullAccountDTO(dockerPullAccountDTO);
        return dockerDeployDTO;
    }

    private HarborC7nRepoImageTagVo getHarborC7nRepoImageTagVo(DevopsDeployGroupDockerDeployVO devopsDeployGroupDockerDeployVO) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">>>>>>>>>>>>>>>>>>>[getHarborC7nRepoImageTagVo] imageInfo is {}<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(devopsDeployGroupDockerDeployVO.getImageInfo()));
        }
        HarborC7nRepoImageTagVo imageTagVo = rdupmClientOperator.listImageTag(devopsDeployGroupDockerDeployVO.getImageInfo().getRepoType(),
                TypeUtil.objToLong(devopsDeployGroupDockerDeployVO.getImageInfo().getRepoId()),
                devopsDeployGroupDockerDeployVO.getImageInfo().getImageName(),
                devopsDeployGroupDockerDeployVO.getImageInfo().getTag());
        if (CollectionUtils.isEmpty(imageTagVo.getImageTagList())) {
            throw new CommonException(ERROR_IMAGE_TAG_NOT_FOUND);
        }
        return imageTagVo;
    }

    private boolean isMarketOrHzero(String sourceType) {
        return AppSourceType.MARKET.getValue().equals(sourceType)
                || AppSourceType.HZERO.getValue().equals(sourceType);
    }

    private MarketServiceDeployObjectVO getMarketServiceDeployObjectVO(Long projectId, DevopsDeployGroupDockerDeployVO devopsDeployGroupDockerDeployVO) {
        MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(devopsDeployGroupDockerDeployVO.getDeployObjectId()));
        if (Objects.isNull(marketServiceDeployObjectVO.getMarketHarborConfigVO())) {
            throw new CommonException("devops.harbor.deploy.object.not.exist");
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
