package io.choerodon.devops.app.service.impl;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.handler.ObjectOperation;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.domain.application.valueobject.Metadata;
import io.choerodon.devops.domain.application.valueobject.ReplaceResult;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.devops.infra.common.util.*;
import io.choerodon.devops.infra.common.util.enums.*;
import io.choerodon.devops.infra.dataobject.ApplicationInstanceDO;
import io.choerodon.devops.infra.dataobject.ApplicationInstancesDO;
import io.choerodon.devops.infra.dataobject.ApplicationLatestVersionDO;
import io.choerodon.devops.infra.dataobject.DeployDO;
import io.choerodon.devops.infra.mapper.ApplicationInstanceMapper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.helper.CommandSender;
import io.choerodon.websocket.helper.EnvListener;
import io.choerodon.websocket.helper.EnvSession;

/**
 * Created by Zenger on 2018/4/12.
 */
@Service
public class ApplicationInstanceServiceImpl implements ApplicationInstanceService {

    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    private static final String C7NHELM_RELEASE = "C7NHelmRelease";
    private static final String RELEASE_NAME = "ReleaseName";
    private static Gson gson = new Gson();

    @Autowired
    DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Value("${agent.version}")
    private String agentExpectVersion;
    @Value("${services.helm.url}")
    private String helmUrl;
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private ApplicationVersionRepository applicationVersionRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private DeployService deployService;
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private CommandSender commandSender;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private DevopsEnvCommandValueRepository devopsEnvCommandValueRepository;
    @Autowired
    private EnvListener envListener;
    @Autowired
    private EnvUtil envUtil;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper;
    @Autowired
    private DevopsEnvPodRepository devopsEnvPodRepository;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private DevopsEnvFileRepository devopsEnvFileRepository;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DeployMsgHandlerService deployMsgHandlerService;
    @Autowired
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;

    @Override
    public Page<ApplicationInstanceDTO> listApplicationInstance(Long projectId, PageRequest pageRequest,
                                                                Long envId, Long versionId, Long appId, String params) {
        Map<String, EnvSession> envs = envListener.connectedEnv();

        Page<ApplicationInstanceE> applicationInstanceEPage = applicationInstanceRepository.listApplicationInstance(
                projectId, pageRequest, envId, versionId, appId, params);
        List<ApplicationInstanceE> applicationInstanceES = applicationInstanceEPage.getContent();
        setInstanceConnect(applicationInstanceES, envs);
        Page<ApplicationInstanceDTO> applicationInstanceDTOS = ConvertPageHelper
                .convertPage(applicationInstanceEPage, ApplicationInstanceDTO.class);
        applicationInstanceDTOS.forEach(applicationInstanceDTO -> {
            List<DeploymentDTO> deploymentDTOS = new ArrayList<>();
            DevopsEnvResourceDTO devopsEnvResourceDTO = devopsEnvResourceService
                    .listResources(applicationInstanceDTO.getId());
            List<DevopsEnvPodDTO> devopsEnvPodDTOS = ConvertHelper
                    .convertList(devopsEnvPodRepository.selectByInstanceId(applicationInstanceDTO.getId()),
                            DevopsEnvPodDTO.class);
            for (DeploymentDTO deploymentDTO : devopsEnvResourceDTO.getDeploymentDTOS()) {

                List<DevopsEnvPodDTO> newDevopsEnvPodDTO = new ArrayList<>();
                devopsEnvPodDTOS.forEach(devopsEnvPodDTO -> {
                            String podName = devopsEnvPodDTO.getName();
                            String tmp = podName.substring(0, podName.lastIndexOf('-'));
                            tmp = tmp.substring(0, tmp.lastIndexOf('-'));
                            if (deploymentDTO.getName().equals(tmp)) {
                                newDevopsEnvPodDTO.add(devopsEnvPodDTO);
                            }
                        }
                );
                deploymentDTO.setDevopsEnvPodDTOS(newDevopsEnvPodDTO);
                deploymentDTOS.add(deploymentDTO);
            }
            applicationInstanceDTO.setDeploymentDTOS(deploymentDTOS);
        });
        return applicationInstanceDTOS;
    }

    @Override
    public List<ApplicationInstancesDTO> listApplicationInstances(Long projectId, Long appId) {

        List<Long> permissionEnvIds = devopsEnvUserPermissionRepository
                .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                .filter(DevopsEnvUserPermissionE::getPermitted).map(DevopsEnvUserPermissionE::getEnvId)
                .collect(Collectors.toList());

        ProjectE projectE = iamRepository.queryIamProject(projectId);
        if (devopsEnvUserPermissionRepository
                .isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectE)) {
            permissionEnvIds = devopsEnvironmentRepository.queryByProject(projectId).stream()
                    .map(DevopsEnvironmentE::getId).collect(Collectors.toList());
        }

        List<ApplicationInstancesDO> instancesDOS = applicationInstanceRepository.getDeployInstances(projectId, appId,
                permissionEnvIds);
        List<ApplicationLatestVersionDO> appLatestVersionList = applicationVersionRepository
                .listAppLatestVersion(projectId);
        Map<Long, ApplicationLatestVersionDO> latestVersionList = appLatestVersionList.stream()
                .collect(Collectors.toMap(ApplicationLatestVersionDO::getAppId, t -> t, (a, b) -> b));
        Map<Long, Integer> appInstancesListMap = new HashMap<>();
        List<ApplicationInstancesDTO> appInstancesList = new ArrayList<>();
        instancesDOS.forEach(t -> {
            ApplicationInstancesDTO instancesDTO = new ApplicationInstancesDTO();
            if (appInstancesListMap.get(t.getAppId()) == null) {
                if (t.getInstanceId() != null
                        || t.getVersionId().equals(latestVersionList.get(t.getAppId()).getVersionId())) {
                    instancesDTO = new ApplicationInstancesDTO(
                            t.getAppId(),
                            t.getPublishLevel(),
                            t.getAppName(),
                            t.getAppCode(),
                            latestVersionList.get(t.getAppId()).getVersionId(),
                            latestVersionList.get(t.getAppId()).getVersion());
                    instancesDTO.setProjectId(t.getProjectId());
                    if (t.getInstanceId() != null) {
                        addAppInstance(instancesDTO, t, latestVersionList.get(t.getAppId()).getVersionId());
                    }
                    appInstancesListMap.put(t.getAppId(), appInstancesList.size());
                    appInstancesList.add(instancesDTO);
                }
            } else {
                instancesDTO = appInstancesList.get(appInstancesListMap.get(t.getAppId()));
                addInstanceIfNotExist(instancesDTO, t);
            }
            if (t.getInstanceId() != null
                    && t.getVersion().equalsIgnoreCase(instancesDTO.getLatestVersion())) {
                instancesDTO.addLatestVersionRunning();
            }
        });
        return appInstancesList;
    }

    private void addAppInstance(ApplicationInstancesDTO instancesDTO, ApplicationInstancesDO instancesDO,
                                Long latestVersionId) {
        EnvVersionDTO envVersionDTO = new EnvVersionDTO(
                instancesDO.getVersionId(),
                instancesDO.getVersion(),
                instancesDO.getInstanceId(),
                instancesDO.getInstanceCode(),
                instancesDO.getInstanceStatus());
        EnvInstanceDTO envInstanceDTO = new EnvInstanceDTO(instancesDO.getEnvId());
        if (instancesDO.getVersionId().equals(latestVersionId)) {
            envVersionDTO.setLatest(true);
        }
        envInstanceDTO.addEnvVersionDTOS(envVersionDTO);
        instancesDTO.appendEnvInstanceDTOS(envInstanceDTO);
        if (instancesDTO.getLatestVersionId().equals(instancesDO.getVersionId())) {
            instancesDTO.appendInstances(new EnvInstancesDTO(
                    instancesDO.getInstanceId(), instancesDO.getInstanceCode(), instancesDO.getInstanceStatus()));
        }
    }

    private void addInstanceIfNotExist(ApplicationInstancesDTO instancesDTO,
                                       ApplicationInstancesDO instancesDO) {
        EnvInstanceDTO envInstanceDTO = instancesDTO.queryLastEnvInstanceDTO();
        if (instancesDTO.getLatestVersionId().equals(instancesDO.getVersionId())) {
            instancesDTO.appendInstances(new EnvInstancesDTO(
                    instancesDO.getInstanceId(), instancesDO.getInstanceCode(), instancesDO.getInstanceStatus()));
        }
        if (envInstanceDTO.getEnvId().equals(instancesDO.getEnvId())) {
            EnvVersionDTO envVersionDTO = envInstanceDTO.queryLastEnvVersionDTO();
            if (envVersionDTO.getVersion().equals(instancesDO.getVersion())) {
                envVersionDTO.appendInstanceList(
                        instancesDO.getInstanceId(),
                        instancesDO.getInstanceCode(),
                        instancesDO.getInstanceStatus());
            } else {
                envInstanceDTO.addEnvVersionDTOS(new EnvVersionDTO(
                        instancesDO.getVersionId(),
                        instancesDO.getVersion(),
                        instancesDO.getInstanceId(),
                        instancesDO.getInstanceCode(),
                        instancesDO.getInstanceStatus()));
            }
        } else {
            EnvVersionDTO envVersionDTO = new EnvVersionDTO(
                    instancesDO.getVersionId(),
                    instancesDO.getVersion(),
                    instancesDO.getInstanceId(),
                    instancesDO.getInstanceCode(),
                    instancesDO.getInstanceStatus());
            envInstanceDTO = new EnvInstanceDTO(instancesDO.getEnvId());
            if (instancesDO.getVersionId().equals(instancesDTO.getLatestVersionId())) {
                envVersionDTO.setLatest(true);
            }
            envInstanceDTO.addEnvVersionDTOS(envVersionDTO);
            instancesDTO.appendEnvInstanceDTOS(envInstanceDTO);
        }
    }

    @Override
    public ReplaceResult queryValues(Long appId, Long envId, Long versionId) {
        ReplaceResult replaceResult = new ReplaceResult();
        String versionValue = FileUtil.checkValueFormat(applicationVersionRepository.queryValue(versionId));
        try {
            FileUtil.checkYamlFormat(versionValue);
        } catch (Exception e) {
            replaceResult.setYaml(versionValue);
            replaceResult.setErrorMsg(e.getMessage());
            replaceResult.setTotalLine(FileUtil.getFileTotalLine(replaceResult.getYaml()));
            replaceResult.setErrorLines(getErrorLine(e.getMessage()));
            return replaceResult;
        }
        String deployValue = FileUtil.checkValueFormat(
                applicationInstanceRepository.queryValueByEnvIdAndAppId(envId, appId));
        replaceResult.setYaml(versionValue);
        if (deployValue != null) {
            replaceResult = getReplaceResult(versionValue, deployValue);
        }
        return replaceResult;
    }

    @Override
    public ReplaceResult queryUpgradeValue(Long instanceId, Long versionId) {
        String yaml = FileUtil.checkValueFormat(applicationInstanceRepository.queryValueByInstanceId(instanceId));
        String versionValue = applicationVersionRepository
                .queryValue(versionId);
        return getReplaceResult(versionValue, yaml);
    }

    @Override
    public DeployTimeDTO listDeployTime(Long projectId, Long envId, Long[] appIds, Date startTime, Date endTime) {
        if (appIds.length == 0) {
            return new DeployTimeDTO();
        }
        List<DeployDO> deployDOS = applicationInstanceRepository
                .listDeployTime(projectId, envId, appIds, startTime, endTime);
        DeployTimeDTO deployTimeDTO = new DeployTimeDTO();
        List<Date> creationDates = deployDOS.stream().map(DeployDO::getCreationDate).collect(Collectors.toList());
        creationDates = new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        List<DeployAppDTO> deployAppDTOS = new ArrayList<>();
        Map<String, List<DeployDO>> resultMaps = deployDOS.stream()
                .collect(Collectors.groupingBy(DeployDO::getAppName));
        resultMaps.forEach((key, value) -> {
            DeployAppDTO deployAppDTO = new DeployAppDTO();
            List<DeployAppDetail> deployAppDetails = new ArrayList<>();
            deployAppDTO.setAppName(key);
            value.forEach(deployDO -> {
                DeployAppDetail deployAppDetail = new DeployAppDetail();
                deployAppDetail.setDeployDate(deployDO.getCreationDate());
                deployAppDetail.setDeployTime(
                        getDeployTime(deployDO.getLastUpdateDate().getTime() - deployDO.getCreationDate().getTime()));
                deployAppDetails.add(deployAppDetail);
            });
            deployAppDTO.setDeployAppDetails(deployAppDetails);
            deployAppDTOS.add(deployAppDTO);
        });
        deployTimeDTO.setCreationDates(creationDates);
        deployTimeDTO.setDeployAppDTOS(deployAppDTOS);
        return deployTimeDTO;
    }

    @Override
    public DeployFrequencyDTO listDeployFrequency(Long projectId, Long[] envIds, Long appId, Date startTime,
                                                  Date endTime) {
        if (envIds.length == 0) {
            return new DeployFrequencyDTO();
        }
        List<DeployDO> deployFrequencyDOS = applicationInstanceRepository
                .listDeployFrequency(projectId, envIds, appId, startTime, endTime);
        Map<String, List<DeployDO>> resultMaps = deployFrequencyDOS.stream()
                .collect(Collectors.groupingBy(t -> new java.sql.Date(t.getCreationDate().getTime()).toString()));
        List<Long> deployFrequencys = new LinkedList<>();
        List<Long> deploySuccessFrequency = new LinkedList<>();
        List<Long> deployFailFrequency = new LinkedList<>();
        List<String> creationDates = deployFrequencyDOS.stream()
                .map(deployDO -> new java.sql.Date(deployDO.getCreationDate().getTime()).toString())
                .collect(Collectors.toList());
        creationDates = new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        creationDates.forEach(date -> {
            Long[] newDeployFrequencys = {0L};
            Long[] newDeploySuccessFrequency = {0L};
            Long[] newDeployFailFrequency = {0L};
            resultMaps.get(date).forEach(deployFrequencyDO -> {
                newDeployFrequencys[0] = newDeployFrequencys[0] + 1L;
                if (deployFrequencyDO.getStatus().equals(CommandStatus.SUCCESS.getStatus())) {
                    newDeploySuccessFrequency[0] = newDeploySuccessFrequency[0] + 1L;
                } else {
                    newDeployFailFrequency[0] = newDeployFailFrequency[0] + 1L;
                }
            });
            deployFrequencys.add(newDeployFrequencys[0]);
            deploySuccessFrequency.add(newDeploySuccessFrequency[0]);
            deployFailFrequency.add(newDeployFailFrequency[0]);
        });
        DeployFrequencyDTO deployFrequencyDTO = new DeployFrequencyDTO();
        deployFrequencyDTO.setCreationDates(creationDates);
        deployFrequencyDTO.setDeployFailFrequency(deployFailFrequency);
        deployFrequencyDTO.setDeploySuccessFrequency(deploySuccessFrequency);
        deployFrequencyDTO.setDeployFrequencys(deployFrequencys);
        return deployFrequencyDTO;
    }

    @Override
    public Page<DeployDetailDTO> pageDeployFrequencyDetail(Long projectId, PageRequest pageRequest, Long[] envIds,
                                                           Long appId, Date startTime, Date endTime) {
        if (envIds.length == 0) {
            return new Page<>();
        }
        Page<DeployDO> deployDOS = applicationInstanceRepository.pageDeployFrequencyDetail(projectId, pageRequest,
                envIds, appId, startTime, endTime);
        return getDeployDetailDTOS(deployDOS);
    }

    @Override
    public Page<DeployDetailDTO> pageDeployTimeDetail(Long projectId, PageRequest pageRequest, Long[] appIds,
                                                      Long envId,
                                                      Date startTime, Date endTime) {
        if (appIds.length == 0) {
            return new Page<>();
        }
        Page<DeployDO> deployDOS = applicationInstanceRepository.pageDeployTimeDetail(projectId, pageRequest, envId,
                appIds, startTime, endTime);
        return getDeployDetailDTOS(deployDOS);
    }

    private Page<DeployDetailDTO> getDeployDetailDTOS(Page<DeployDO> deployDOS) {
        Page<DeployDetailDTO> pageDeployDetailDTOS = new Page<>();
        List<DeployDetailDTO> deployDetailDTOS = new ArrayList<>();
        BeanUtils.copyProperties(deployDOS, pageDeployDetailDTOS);
        deployDOS.getContent().forEach(deployDO -> {
            DeployDetailDTO deployDetailDTO = new DeployDetailDTO();
            BeanUtils.copyProperties(deployDO, deployDetailDTO);
            deployDetailDTO.setDeployTime(
                    getDeployTime(deployDO.getLastUpdateDate().getTime() - deployDO.getCreationDate().getTime()));
            if (deployDO.getCreatedBy() != 0) {
                UserE userE = iamRepository.queryUserByUserId(deployDO.getCreatedBy());
                deployDetailDTO.setLastUpdatedName(userE.getRealName());
            }
            deployDetailDTOS.add(deployDetailDTO);
        });
        pageDeployDetailDTOS.setContent(deployDetailDTOS);
        return pageDeployDetailDTOS;
    }

    @Override
    public ReplaceResult queryValue(Long instanceId) {
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectById(instanceId);
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(applicationInstanceE.getCommandId());
        String yaml = FileUtil.checkValueFormat(applicationInstanceRepository.queryValueByInstanceId(
                instanceId));
        String versionValue = applicationVersionRepository
                .queryValue(devopsEnvCommandE.getObjectVersionId());
        return getReplaceResult(versionValue, yaml);
    }

    @Override
    public List<ErrorLineDTO> formatValue(ReplaceResult replaceResult) {
        try {
            FileUtil.checkYamlFormat(replaceResult.getYaml());
        } catch (Exception e) {
            return getErrorLine(e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public ReplaceResult previewValues(ReplaceResult previewReplaceResult, Long appVersionId) {
        String versionValue = applicationVersionRepository.queryValue(appVersionId);
        ReplaceResult replaceResult = getReplaceResult(versionValue, previewReplaceResult.getYaml());
        replaceResult.setTotalLine(FileUtil.getFileTotalLine(replaceResult.getYaml()) + 1);
        return replaceResult;
    }

    @Override
    public DevopsEnvPreviewDTO listByEnv(Long projectId, Long envId, String params) {
        Map<String, Object> maps = gson.fromJson(params, new TypeToken<Map<String, Object>>() {
        }.getType());
        Map<String, EnvSession> envs = envListener.connectedEnv();
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        List<ApplicationInstanceDO> applicationInstancesDOS = applicationInstanceMapper
                .listApplicationInstance(projectId, envId, null, null, searchParamMap, paramMap);
        List<ApplicationInstanceE> applicationInstanceES = ConvertHelper
                .convertList(applicationInstancesDOS, ApplicationInstanceE.class);
        setInstanceConnect(applicationInstanceES, envs);
        Map<String, List<ApplicationInstanceE>> resultMaps = applicationInstanceES.stream()
                .collect(Collectors.groupingBy(t -> t.getApplicationE().getName()));
        DevopsEnvPreviewDTO devopsEnvPreviewDTO = new DevopsEnvPreviewDTO();
        List<DevopsEnvPreviewAppDTO> devopsEnvPreviewAppDTOS = new ArrayList<>();
        resultMaps.forEach((key, value) -> {
            DevopsEnvPreviewAppDTO devopsEnvPreviewAppDTO = new DevopsEnvPreviewAppDTO();
            devopsEnvPreviewAppDTO.setAppName(key);
            ApplicationE applicationE = applicationRepository.query(value.get(0).getApplicationE().getId());
            devopsEnvPreviewAppDTO.setAppCode(applicationE.getCode());
            List<ApplicationInstanceDTO> applicationInstanceDTOS = ConvertHelper
                    .convertList(value, ApplicationInstanceDTO.class);
            List<DevopsEnvPreviewInstanceDTO> devopsEnvPreviewInstanceDTOS = new ArrayList<>();
            applicationInstanceDTOS.forEach(applicationInstanceDTO -> {
                DevopsEnvPreviewInstanceDTO devopsEnvPreviewInstanceDTO = new DevopsEnvPreviewInstanceDTO();
                BeanUtils.copyProperties(applicationInstanceDTO, devopsEnvPreviewInstanceDTO);
                List<DevopsEnvPodDTO> devopsEnvPodDTOS = ConvertHelper
                        .convertList(devopsEnvPodRepository.selectByInstanceId(devopsEnvPreviewInstanceDTO.getId()),
                                DevopsEnvPodDTO.class);
                DevopsEnvResourceDTO devopsEnvResourceDTO = devopsEnvResourceService
                        .listResources(devopsEnvPreviewInstanceDTO.getId());
                for (DeploymentDTO deploymentDTO : devopsEnvResourceDTO.getDeploymentDTOS()) {
                    List<DevopsEnvPodDTO> newDevopsEnvPodDTO = new ArrayList<>();
                    devopsEnvPodDTOS.forEach(devopsEnvPodDTO -> {
                                String podName = devopsEnvPodDTO.getName();
                                String tmp = podName.substring(0, podName.lastIndexOf('-'));
                                tmp = tmp.substring(0, tmp.lastIndexOf('-'));
                                if (deploymentDTO.getName().equals(tmp)) {
                                    newDevopsEnvPodDTO.add(devopsEnvPodDTO);
                                }
                            }
                    );
                    deploymentDTO.setDevopsEnvPodDTOS(newDevopsEnvPodDTO);
                }
                devopsEnvPreviewInstanceDTO.setDeploymentDTOS(devopsEnvResourceDTO.getDeploymentDTOS());
                devopsEnvPreviewInstanceDTO.setIngressDTOS(devopsEnvResourceDTO.getIngressDTOS());
                devopsEnvPreviewInstanceDTO.setServiceDTOS(devopsEnvResourceDTO.getServiceDTOS());
                devopsEnvPreviewInstanceDTOS.add(devopsEnvPreviewInstanceDTO);
            });
            devopsEnvPreviewAppDTO.setApplicationInstanceDTOS(devopsEnvPreviewInstanceDTOS);
            devopsEnvPreviewAppDTOS.add(devopsEnvPreviewAppDTO);
        });
        devopsEnvPreviewDTO.setDevopsEnvPreviewAppDTOS(devopsEnvPreviewAppDTOS);
        return devopsEnvPreviewDTO;
    }

    public ApplicationInstanceDTO createOrUpdate(ApplicationDeployDTO applicationDeployDTO) {

        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()),
                applicationDeployDTO.getEnvironmentId());

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .queryById(applicationDeployDTO.getEnvironmentId());

        //校验环境是否连接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);

        //校验values
        FileUtil.checkYamlFormat(applicationDeployDTO.getValues());

        ApplicationE applicationE = applicationRepository.query(applicationDeployDTO.getAppId());
        ApplicationVersionE applicationVersionE =
                applicationVersionRepository.query(applicationDeployDTO.getAppVerisonId());

        //初始化ApplicationInstanceE,DevopsEnvCommandE,DevopsEnvCommandValueE
        ApplicationInstanceE applicationInstanceE = initApplicationInstanceE(applicationDeployDTO);
        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(applicationDeployDTO);
        DevopsEnvCommandValueE devopsEnvCommandValueE = initDevopsEnvCommandValueE(applicationDeployDTO);

        //初始化实例名
        String code;
        if (applicationDeployDTO.getType().equals(CREATE)) {
            code = String.format("%s-%s", applicationE.getCode(), GenerateUUID.generateUUID().substring(0, 5));
        } else {
            code = applicationInstanceE.getCode();
        }

        //检验gitops库是否存在，校验操作人是否是有gitops库的权限
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);

        ApplicationInstanceE beforeApplicationInstanceE = applicationInstanceRepository
                .selectByCode(code, applicationDeployDTO.getEnvironmentId());
        DevopsEnvCommandE beforeDevopsEnvCommandE = new DevopsEnvCommandE();
        if (beforeApplicationInstanceE != null) {
            beforeDevopsEnvCommandE = devopsEnvCommandRepository.query(beforeApplicationInstanceE.getCommandId());
        }

        //更新时候，如果isNotChange的值为true，则直接向agent发送更新指令，不走gitops,否则走操作gitops库文件逻辑
        if (applicationDeployDTO.getIsNotChange()) {
            applicationInstanceRepository.update(applicationInstanceE);
            devopsEnvCommandE.setObjectId(applicationInstanceE.getId());
            devopsEnvCommandE.initDevopsEnvCommandValueE(
                    devopsEnvCommandValueRepository.create(devopsEnvCommandValueE).getId());
            deployService.deploy(applicationE, applicationVersionE, applicationInstanceE, devopsEnvironmentE,
                    devopsEnvCommandValueE.getValue(), devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        } else {
            //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
            String filePath = devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);

            //在gitops库处理instance文件
            ObjectOperation<C7nHelmRelease> objectOperation = new ObjectOperation<>();
            objectOperation.setType(getC7NHelmRelease(
                    code, applicationVersionE, applicationDeployDTO, applicationE));
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            objectOperation.operationEnvGitlabFile(
                    "release-" + code,
                    projectId,
                    applicationDeployDTO.getType(),
                    userAttrE.getGitlabUserId(),
                    applicationInstanceE.getId(), C7NHELM_RELEASE, devopsEnvironmentE.getId(), filePath);
            ApplicationInstanceE afterApplicationInstanceE = applicationInstanceRepository
                    .selectByCode(code, applicationDeployDTO.getEnvironmentId());
            DevopsEnvCommandE afterDevopsEnvCommandE = new DevopsEnvCommandE();
            if (afterApplicationInstanceE != null) {
                afterDevopsEnvCommandE = devopsEnvCommandRepository.query(afterApplicationInstanceE.getCommandId());
            }

            //实例相关对象数据库操作,当集群速度较快时，会导致部署速度快于gitlab创文件的返回速度，从而实例成功的状态会被错误更新为处理中，所以用before和after去区分是否部署成功。成功不再执行实例数据库操作
            if (applicationDeployDTO.getType().equals(CREATE) && afterApplicationInstanceE == null) {
                applicationInstanceE.setCode(code);
                applicationInstanceE.setId(applicationInstanceRepository.create(applicationInstanceE).getId());
                devopsEnvCommandE.setObjectId(applicationInstanceE.getId());
                devopsEnvCommandE.initDevopsEnvCommandValueE(
                        devopsEnvCommandValueRepository.create(devopsEnvCommandValueE).getId());
                applicationInstanceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
                applicationInstanceRepository.update(applicationInstanceE);
            } else if (beforeDevopsEnvCommandE.getId().equals(afterDevopsEnvCommandE.getId())) {
                devopsEnvCommandE.setObjectId(applicationInstanceE.getId());
                devopsEnvCommandE.initDevopsEnvCommandValueE(
                        devopsEnvCommandValueRepository.create(devopsEnvCommandValueE).getId());
                applicationInstanceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
                applicationInstanceRepository.update(applicationInstanceE);
            }
        }
        return ConvertHelper.convert(applicationInstanceE, ApplicationInstanceDTO.class);
    }

    public ApplicationInstanceDTO createOrUpdateByGitOps(ApplicationDeployDTO applicationDeployDTO, Long userId) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .queryById(applicationDeployDTO.getEnvironmentId());
        //校验环境是否连接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);

        //校验values
        FileUtil.checkYamlFormat(applicationDeployDTO.getValues());

        //初始化ApplicationInstanceE,DevopsEnvCommandE,DevopsEnvCommandValueE
        ApplicationInstanceE applicationInstanceE = initApplicationInstanceE(applicationDeployDTO);
        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(applicationDeployDTO);
        DevopsEnvCommandValueE devopsEnvCommandValueE = initDevopsEnvCommandValueE(applicationDeployDTO);

        //实例相关对象数据库操作
        if (applicationDeployDTO.getType().equals(CREATE)) {
            applicationInstanceE.setCode(applicationDeployDTO.getInstanceName());
            applicationInstanceE.setId(applicationInstanceRepository.create(applicationInstanceE).getId());
        } else {
            applicationInstanceRepository.update(applicationInstanceE);
        }
        devopsEnvCommandE.setCreatedBy(userId);
        devopsEnvCommandE.setObjectId(applicationInstanceE.getId());
        devopsEnvCommandE.initDevopsEnvCommandValueE(
                devopsEnvCommandValueRepository.create(devopsEnvCommandValueE).getId());
        applicationInstanceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        applicationInstanceRepository.update(applicationInstanceE);
        return ConvertHelper.convert(applicationInstanceE, ApplicationInstanceDTO.class);
    }

    private ApplicationInstanceE initApplicationInstanceE(ApplicationDeployDTO applicationDeployDTO) {

        ApplicationInstanceE applicationInstanceE = new ApplicationInstanceE();
        applicationInstanceE.initApplicationEById(applicationDeployDTO.getAppId());
        applicationInstanceE.initDevopsEnvironmentEById(applicationDeployDTO.getEnvironmentId());
        applicationInstanceE.setStatus(InstanceStatus.OPERATIING.getStatus());
        if (applicationDeployDTO.getType().equals(UPDATE)) {
            ApplicationInstanceE newApplicationInstanceE = applicationInstanceRepository.selectById(
                    applicationDeployDTO.getAppInstanceId());
            applicationInstanceE.setCode(newApplicationInstanceE.getCode());
            applicationInstanceE.setId(applicationDeployDTO.getAppInstanceId());
        }
        return applicationInstanceE;
    }

    private DevopsEnvCommandE initDevopsEnvCommandE(ApplicationDeployDTO applicationDeployDTO) {
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
        switch (applicationDeployDTO.getType()) {
            case CREATE:
                devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
                break;
            case UPDATE:
                devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
                break;
            default:
                devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
                break;
        }
        devopsEnvCommandE.setObjectVersionId(applicationDeployDTO.getAppVerisonId());
        devopsEnvCommandE.setObject(ObjectType.INSTANCE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandE;
    }

    private DevopsEnvCommandValueE initDevopsEnvCommandValueE(ApplicationDeployDTO applicationDeployDTO) {
        DevopsEnvCommandValueE devopsEnvCommandValueE = new DevopsEnvCommandValueE();
        devopsEnvCommandValueE.setValue(
                getReplaceResult(applicationVersionRepository.queryValue(applicationDeployDTO.getAppVerisonId()),
                        applicationDeployDTO.getValues()).getDeltaYaml().trim());
        return devopsEnvCommandValueE;
    }

    @Override
    public List<AppInstanceCodeDTO> listByOptions(Long projectId, Long appId, Long appVersionId, Long envId) {
        return ConvertHelper.convertList(applicationInstanceRepository
                .listByOptions(projectId, appId, appVersionId, envId), AppInstanceCodeDTO.class);
    }

    @Override
    public List<AppInstanceCodeDTO> listByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return ConvertHelper.convertList(applicationInstanceRepository
                .listByAppIdAndEnvId(projectId, appId, envId), AppInstanceCodeDTO.class);
    }

    @Override
    public void instanceStop(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);
        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()),
                instanceE.getDevopsEnvironmentE().getId());

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .queryById(instanceE.getDevopsEnvironmentE().getId());

        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);
        if (!instanceE.getStatus().equals(InstanceStatus.RUNNING.getStatus())) {
            throw new CommonException("error.instance.notRunning");
        }
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.INSTANCE.getType(), instanceId);
        devopsEnvCommandE.setCommandType(CommandType.STOP.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE = devopsEnvCommandRepository.create(devopsEnvCommandE);
        String namespace = getNameSpace(instanceE.getDevopsEnvironmentE().getId());
        String releaseName = updateInstanceStatus(instanceId, InstanceStatus.OPERATIING.getStatus());
        Map<String, String> stopMap = new HashMap<>();
        stopMap.put(RELEASE_NAME, releaseName);
        String payload = gson.toJson(stopMap);
        Long envId = instanceE.getDevopsEnvironmentE().getId();
        sentInstance(payload, releaseName, HelmType.HELM_RELEASE_STOP.toValue(),
                namespace, devopsEnvCommandE.getId(), envId, devopsEnvironmentE.getClusterE().getId());
    }

    @Override
    public void instanceStart(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);
        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()),
                instanceE.getDevopsEnvironmentE().getId());

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .queryById(instanceE.getDevopsEnvironmentE().getId());

        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);
        if (!instanceE.getStatus().equals(InstanceStatus.STOPPED.getStatus())) {
            throw new CommonException("error.instance.notStop");
        }
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.INSTANCE.getType(), instanceId);
        devopsEnvCommandE.setCommandType(CommandType.RESTART.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE = devopsEnvCommandRepository.create(devopsEnvCommandE);
        String namespace = getNameSpace(instanceE.getDevopsEnvironmentE().getId());
        String releaseName = updateInstanceStatus(instanceId, InstanceStatus.OPERATIING.getStatus());
        Map<String, String> stopMap = new HashMap<>();
        stopMap.put(RELEASE_NAME, releaseName);
        String payload = gson.toJson(stopMap);
        Long envId = instanceE.getDevopsEnvironmentE().getId();
        sentInstance(payload, releaseName, HelmType.HELM_RELEASE_START.toValue(),
                namespace, devopsEnvCommandE.getId(), envId, devopsEnvironmentE.getClusterE().getId());
    }

    @Override
    public void instanceReStart(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);
        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()),
                instanceE.getDevopsEnvironmentE().getId());
        ApplicationE applicationE = applicationRepository.query(instanceE.getApplicationE().getId());
        ApplicationVersionE applicationVersionE = applicationVersionRepository
                .query(instanceE.getApplicationVersionE().getId());
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .queryById(instanceE.getDevopsEnvironmentE().getId());
        String value = applicationInstanceRepository.queryValueByInstanceId(instanceId);
        instanceE.setStatus(InstanceStatus.OPERATIING.getStatus());
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(instanceE.getCommandId());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        Long commandId = devopsEnvCommandRepository.create(devopsEnvCommandE).getId();
        instanceE.setCommandId(commandId);
        applicationInstanceRepository.update(instanceE);
        deployService.deploy(applicationE, applicationVersionE, instanceE, devopsEnvironmentE, value, commandId);
    }

    @Override
    public void instanceDelete(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);
        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()),
                instanceE.getDevopsEnvironmentE().getId());

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .queryById(instanceE.getDevopsEnvironmentE().getId());

        //校验环境是否连接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);

        DevopsEnvCommandE devopsEnvCommandE;
        if (instanceE.getCommandId() == null) {
            devopsEnvCommandE = devopsEnvCommandRepository
                    .queryByObject(ObjectType.INSTANCE.getType(), instanceE.getId());
        } else {
            devopsEnvCommandE = devopsEnvCommandRepository
                    .query(instanceE.getCommandId());
        }
        devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandE.setId(null);

        //检验gitops库是否存在，校验操作人是否是有gitops库的权限
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);

        //实例相关对象数据库操作
        instanceE.setStatus(InstanceStatus.OPERATIING.getStatus());
        instanceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        applicationInstanceRepository.update(instanceE);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(devopsEnvironmentE.getId(), instanceId, C7NHELM_RELEASE);
        if (devopsEnvFileResourceE == null) {
            throw new CommonException("error.fileResource.not.exist");
        }
        List<DevopsEnvFileResourceE> devopsEnvFileResourceES = devopsEnvFileResourceRepository
                .queryByEnvIdAndPath(devopsEnvironmentE.getId(), devopsEnvFileResourceE.getFilePath());
        if (devopsEnvFileResourceES.size() == 1) {
            gitlabRepository.deleteFile(
                    TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                    devopsEnvFileResourceE.getFilePath(),
                    "DELETE FILE",
                    TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        } else {
            ObjectOperation<C7nHelmRelease> objectOperation = new ObjectOperation<>();
            C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
            Metadata metadata = new Metadata();
            metadata.setName(instanceE.getCode());
            c7nHelmRelease.setMetadata(metadata);
            objectOperation.setType(c7nHelmRelease);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            objectOperation.operationEnvGitlabFile(
                    "release-" + instanceE.getCode(),
                    projectId,
                    "delete",
                    userAttrE.getGitlabUserId(),
                    instanceE.getId(), C7NHELM_RELEASE, devopsEnvironmentE.getId(), path);
        }
    }

    @Override
    public void instanceDeleteByGitOps(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .queryById(instanceE.getDevopsEnvironmentE().getId());

        //校验环境是否连接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);

        //实例相关对象数据库操作
        DevopsEnvCommandE devopsEnvCommandE;
        if (instanceE.getCommandId() == null) {
            devopsEnvCommandE = devopsEnvCommandRepository
                    .queryByObject(ObjectType.INSTANCE.getType(), instanceE.getId());
        } else {
            devopsEnvCommandE = devopsEnvCommandRepository
                    .query(instanceE.getCommandId());
        }
        devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
        devopsEnvCommandRepository.update(devopsEnvCommandE);
        applicationInstanceRepository.deleteById(instanceId);
    }

    private String getNameSpace(Long envId) {
        return devopsEnvironmentRepository.queryById(envId).getCode();
    }

    private String updateInstanceStatus(Long instanceId, String status) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);
        instanceE.setStatus(status);
        applicationInstanceRepository.update(instanceE);
        return instanceE.getCode();
    }

    private void sentInstance(String payload, String name, String type, String namespace, Long commandId, Long envId, Long clusterId) {
        Msg msg = new Msg();
        msg.setKey("cluster:" + clusterId + ".env:" + namespace + ".envId:" + envId + ".release:" + name);
        msg.setType(type);
        msg.setPayload(payload);
        msg.setCommandId(commandId);
        commandSender.sendMsg(msg);
    }

    private List<ErrorLineDTO> getErrorLine(String value) {
        List<ErrorLineDTO> errorLines = new ArrayList<>();
        List<Long> lineNumbers = new ArrayList<>();
        String[] errorMsg = value.split("\\^");
        for (int i = 0; i < value.length(); i++) {
            int j;
            for (j = i; j < value.length(); j++) {
                if (value.substring(i, j).equals("line")) {
                    lineNumbers.add(TypeUtil.objToLong(value.substring(j, value.indexOf(',', j)).trim()));
                }
            }
        }
        for (int i = 0; i < lineNumbers.size(); i++) {
            ErrorLineDTO errorLineDTO = new ErrorLineDTO();
            errorLineDTO.setLineNumber(lineNumbers.get(i));
            errorLineDTO.setErrorMsg(errorMsg[i]);
            errorLines.add(errorLineDTO);
        }
        return errorLines;
    }

    private void setInstanceConnect(List<ApplicationInstanceE> applicationInstanceES,
                                    Map<String, EnvSession> envSessionMap) {
        applicationInstanceES.forEach(applicationInstanceE ->
                applicationInstanceE.setConnect(envSessionMap.entrySet().stream()
                        .anyMatch(entry -> {
                            EnvSession envSession = entry.getValue();
                            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                                    .queryById(applicationInstanceE.getDevopsEnvironmentE().getId());
                            return envSession.getClusterId().equals(devopsEnvironmentE.getClusterE().getId())
                                    && EnvUtil.compareVersion(envSession.getVersion(), agentExpectVersion) != 1;
                        })));
    }

    private C7nHelmRelease getC7NHelmRelease(String code,
                                             ApplicationVersionE applicationVersionE,
                                             ApplicationDeployDTO applicationDeployDTO,
                                             ApplicationE applicationE
    ) {
        C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
        c7nHelmRelease.getMetadata().setName(code);
        c7nHelmRelease.getSpec().setRepoUrl(helmUrl + applicationVersionE.getRepository());
        c7nHelmRelease.getSpec().setChartName(applicationE.getCode());
        c7nHelmRelease.getSpec().setChartVersion(applicationVersionE.getVersion());
        c7nHelmRelease.getSpec().setValues(
                getReplaceResult(applicationVersionRepository.queryValue(applicationDeployDTO.getAppVerisonId()),
                        applicationDeployDTO.getValues()).getDeltaYaml().trim());
        return c7nHelmRelease;

    }

    @Override
    public ReplaceResult getReplaceResult(String versionValue, String deployValue) {
        if (versionValue.equals(deployValue)) {
            ReplaceResult replaceResult = new ReplaceResult();
            replaceResult.setDeltaYaml("");
            replaceResult.setHighlightMarkers(new ArrayList<>());
            replaceResult.setNewLines(new ArrayList<>());
            return replaceResult;
        }

        String fileName = GenerateUUID.generateUUID() + ".yaml";
        String path = "deployfile";
        FileUtil.saveDataToFile(path, fileName, versionValue + "\n" + "---" + "\n" + deployValue);
        ReplaceResult replaceResult;
        try {
            replaceResult = FileUtil.replaceNew(path + System.getProperty("file.separator") + fileName);
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
        if (replaceResult.getHighlightMarkers() == null) {
            replaceResult.setHighlightMarkers(new ArrayList<>());
        }
        replaceResult.setTotalLine(FileUtil.getFileTotalLine(replaceResult.getYaml()));
        FileUtil.deleteFile(path + System.getProperty("file.separator") + fileName);
        return replaceResult;
    }

    private String getDeployTime(Long diff) {
        float num = (float) diff / (60 * 1000);
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(num);
    }
}
