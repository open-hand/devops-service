package io.choerodon.devops.app.service.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.asgard.saga.annotation.Saga;
<<<<<<< HEAD
import io.choerodon.asgard.saga.feign.SagaClient;
=======
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
>>>>>>> [IMP]重构后端代码
import io.choerodon.base.domain.PageRequest;
<<<<<<< HEAD
import io.choerodon.devops.api.vo.*;
=======
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.AppInstanceValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.entity.*;
import io.choerodon.devops.api.vo.iam.entity.iam.UserE;
<<<<<<< HEAD
>>>>>>> [IMP]重构后端断码
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.dataobject.AppInstanceInfoDTO;
import io.choerodon.devops.infra.dto.ApplicationInstanceDTO;
import io.choerodon.devops.infra.dto.ApplicationInstanceOverViewDTO;
import io.choerodon.devops.infra.dto.ApplicationLatestVersionDTO;
=======
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.domain.application.valueobject.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
>>>>>>> [IMP]重构后端代码
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.ApplicationInstanceMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvApplicationMapper;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.helper.CommandSender;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Created by Zenger on 2018/4/12.
 */
@Service
public class ApplicationInstanceServiceImpl implements ApplicationInstanceService {

    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String CHOERODON = "choerodon-test";
    private static final String YAML_SUFFIX = ".yaml";
    private static final String RELEASE_PREFIX = "release-";
    private static final String FILE_SEPARATOR = "file.separator";
    private static final String C7NHELM_RELEASE = "C7NHelmRelease";
    private static final String RELEASE_NAME = "ReleaseName";


    private static Gson gson = new Gson();

    @Value("${agent.version}")
    private String agentExpectVersion;
    @Value("${services.helm.url}")
    private String helmUrl;
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    private CommandSender commandSender;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    private ResourceFileCheckHandler resourceFileCheckHandler;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private ApplicationVersionService applicationVersionService;
    @Autowired
    private DevopsDeployValueService devopsDeployValueService;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private DevopsProjectConfigService devopsProjectConfigService;
    @Autowired
    private DevopsRegistrySecretService devopsRegistrySecretService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvCommandValueService devopsEnvCommandValueService;
    @Autowired
    private DevopsEnvPodService devopsEnvPodService;
    @Autowired
<<<<<<< HEAD
    private DevopsEnvApplicationMapper envApplicationMapper;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
=======
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
>>>>>>> [IMP]重构后端代码
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private PipelineAppDeployService pipelineAppDeployService;

    @Override
    public AppInstanceInfoVO queryInfoById(Long instanceId) {
        AppInstanceInfoDTO appInstanceInfoDTO = applicationInstanceMapper.queryInfoById(instanceId);
        if (appInstanceInfoDTO == null) {
            return null;
        }
        AppInstanceInfoVO appInstanceInfoVO = new AppInstanceInfoVO();
        BeanUtils.copyProperties(appInstanceInfoDTO, appInstanceInfoVO);
        return appInstanceInfoVO;
    }

    @Override
    public PageInfo<DevopsEnvPreviewInstanceVO> pageByOptions(Long projectId, PageRequest pageRequest,
                                                              Long envId, Long versionId, Long appId, Long instanceId, String params) {
        PageInfo<DevopsEnvPreviewInstanceVO> devopsEnvPreviewInstanceDTOPageInfo = new PageInfo<>();

        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        PageInfo<ApplicationInstanceDTO> applicationInstanceDTOPageInfo = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                applicationInstanceMapper
                        .listApplicationInstance(projectId, envId, versionId, appId, instanceId, searchParamMap, paramMap));

        BeanUtils.copyProperties(applicationInstanceDTOPageInfo, devopsEnvPreviewInstanceDTOPageInfo);

        return devopsEnvPreviewInstanceDTOPageInfo;

    }


    @Override
    public List<ApplicationInstanceOverViewVO> listApplicationInstanceOverView(Long projectId, Long appId) {


        //查询出当前用户有权限的环境列表，如果是项目所有者，则有全部环境权限
        List<Long> permissionEnvIds = devopsEnvUserPermissionService
                .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                .filter(DevopsEnvUserPermissionDTO::getPermitted).map(DevopsEnvUserPermissionDTO::getEnvId)
                .collect(Collectors.toList());

        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(projectId);
        if (iamServiceClientOperator.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectDTO)) {
            permissionEnvIds = devopsEnvironmentService.baseListByProjectId(projectId).stream()
                    .map(DevopsEnvironmentDTO::getId).collect(Collectors.toList());
        }

        List<ApplicationInstanceOverViewDTO> applicationInstanceOverViewDTOS = baseListApplicationInstanceOverView(projectId, appId,
                permissionEnvIds);

        List<ApplicationLatestVersionDTO> appLatestVersionList = applicationVersionService.baseListAppNewestVersion(projectId);

        Map<Long, ApplicationLatestVersionDTO> latestVersionList = appLatestVersionList.stream()
                .collect(Collectors.toMap(ApplicationLatestVersionDTO::getAppId, t -> t, (a, b) -> b));

        //查询部署总览，每个应用最新的版本以及在每个环境每个实例部署的版本
        Map<Long, Integer> appInstancesListMap = new HashMap<>();
        List<ApplicationInstanceOverViewVO> applicationInstanceOverViewVOS = new ArrayList<>();
        applicationInstanceOverViewDTOS.forEach(t -> {
            ApplicationInstanceOverViewVO applicationInstanceOverViewVO = new ApplicationInstanceOverViewVO();
            if (appInstancesListMap.get(t.getAppId()) == null) {
                if (t.getInstanceId() != null
                        || t.getVersionId().equals(latestVersionList.get(t.getAppId()).getVersionId())) {
                    applicationInstanceOverViewVO = new ApplicationInstanceOverViewVO(
                            t.getAppId(),
                            t.getPublishLevel(),
                            t.getAppName(),
                            t.getAppCode(),
                            latestVersionList.get(t.getAppId()).getVersionId(),
                            latestVersionList.get(t.getAppId()).getVersion());
                    applicationInstanceOverViewVO.setProjectId(t.getProjectId());
                    if (t.getInstanceId() != null) {
                        initInstanceOverView(applicationInstanceOverViewVO, t, latestVersionList.get(t.getAppId()).getVersionId());
                    }
<<<<<<< HEAD
<<<<<<<HEAD
                    appInstancesListMap.put(t.getAppId(), appInstancesList.size());
                    appInstancesList.add(instancesDTO);
=======
                } else {
                    instancesDTO = appInstancesList.get(appInstancesListMap.get(t.getAppId()));
                    addInstanceIfNotExist(instancesDTO, t);
>>>>>>> [IMP]重构后端代码
=======
                    appInstancesListMap.put(t.getAppId(), applicationInstanceOverViewVOS.size());
                    applicationInstanceOverViewVOS.add(applicationInstanceOverViewVO);
>>>>>>> [IMP]重构后端代码
                }
            } else {
                applicationInstanceOverViewVO = applicationInstanceOverViewVOS.get(appInstancesListMap.get(t.getAppId()));
                initInstanceOverViewIfNotExist(applicationInstanceOverViewVO, t);
            }
            if (t.getInstanceId() != null
                    && t.getVersion().equalsIgnoreCase(applicationInstanceOverViewVO.getLatestVersion())) {
                applicationInstanceOverViewVO.addLatestVersionRunning();
            }
        });
<<<<<<< HEAD
        return appInstancesList;
    }

<<<<<<<HEAD

    private void addAppInstance(ApplicationInstancesVO instancesDTO, ApplicationInstancesDO instancesDO,
=======
                                private void addAppInstance(ApplicationInstancesVO instancesDTO, ApplicationInstanceOverViewDTO
                                        instancesDO,
>>>>>>>[IMP]重构后端代码
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
<<<<<<<HEAD
=======
        return applicationInstanceOverViewVOS;
    }

    private void initInstanceOverView(ApplicationInstanceOverViewVO applicationInstanceOverViewVO, ApplicationInstanceOverViewDTO
            applicationInstanceOverViewDTO,
                                      Long latestVersionId) {
        EnvVersionVO envVersionVO = new EnvVersionVO(
                applicationInstanceOverViewDTO.getVersionId(),
                applicationInstanceOverViewDTO.getVersion(),
                applicationInstanceOverViewDTO.getInstanceId(),
                applicationInstanceOverViewDTO.getInstanceCode(),
                applicationInstanceOverViewDTO.getInstanceStatus());
        EnvInstanceVO envInstanceVO = new EnvInstanceVO(applicationInstanceOverViewDTO.getEnvId());
        if (applicationInstanceOverViewDTO.getVersionId().equals(latestVersionId)) {
            envVersionVO.setLatest(true);
>>>>>>> [IMP]重构后端代码
        }
        envInstanceVO.addEnvVersionDTOS(envVersionVO);
        applicationInstanceOverViewVO.appendEnvInstanceVOS(envInstanceVO);
        if (applicationInstanceOverViewVO.getLatestVersionId().equals(applicationInstanceOverViewDTO.getVersionId())) {
            applicationInstanceOverViewVO.appendInstances(new EnvInstancesVO(
                    applicationInstanceOverViewDTO.getInstanceId(), applicationInstanceOverViewDTO.getInstanceCode(), applicationInstanceOverViewDTO.getInstanceStatus()));
        }
    }
=======
}
        envInstanceDTO.addEnvVersionDTOS(envVersionDTO);
                instancesDTO.appendEnvInstanceDTOS(envInstanceDTO);
                if(instancesDTO.getLatestVersionId().equals(instancesDO.getVersionId())){
                instancesDTO.appendInstances(new EnvInstancesDTO(
                instancesDO.getInstanceId(),instancesDO.getInstanceCode(),instancesDO.getInstanceStatus()));
                }
                }

                >>>>>>>[IMP]重构后端代码

<<<<<<< HEAD
private void addInstanceIfNotExist(ApplicationInstancesVO instancesDTO,
        ApplicationInstancesDO instancesDO){
        EnvInstanceDTO envInstanceDTO=instancesDTO.queryLastEnvInstanceDTO();
        if(instancesDTO.getLatestVersionId().equals(instancesDO.getVersionId())){
        instancesDTO.appendInstances(new EnvInstancesDTO(
        instancesDO.getInstanceId(),instancesDO.getInstanceCode(),instancesDO.getInstanceStatus()));
        }
        if(envInstanceDTO.getEnvId().equals(instancesDO.getEnvId())){
        EnvVersionDTO envVersionDTO=envInstanceDTO.queryLastEnvVersionDTO();
        if(envVersionDTO.getVersion().equals(instancesDO.getVersion())){
        envVersionDTO.appendInstanceList(
        instancesDO.getInstanceId(),
        instancesDO.getInstanceCode(),
        instancesDO.getInstanceStatus());
        }else{
        envInstanceDTO.addEnvVersionDTOS(new EnvVersionDTO(
        instancesDO.getVersionId(),
        instancesDO.getVersion(),
        instancesDO.getInstanceId(),
        instancesDO.getInstanceCode(),
        instancesDO.getInstanceStatus()));
        }
        }else{
        EnvVersionDTO envVersionDTO=new EnvVersionDTO(
        instancesDO.getVersionId(),
        instancesDO.getVersion(),
        instancesDO.getInstanceId(),
        instancesDO.getInstanceCode(),
        instancesDO.getInstanceStatus());
        envInstanceDTO=new EnvInstanceDTO(instancesDO.getEnvId());
        if(instancesDO.getVersionId().equals(instancesDTO.getLatestVersionId())){
        envVersionDTO.setLatest(true);
        }
        envInstanceDTO.addEnvVersionDTOS(envVersionDTO);
        instancesDTO.appendEnvInstanceDTOS(envInstanceDTO);
        }
=======
    private void initInstanceOverViewIfNotExist(ApplicationInstanceOverViewVO applicationInstanceOverViewVO,
                                                ApplicationInstanceOverViewDTO applicationInstanceOverViewDTO) {
        EnvInstanceVO envInstanceVO = applicationInstanceOverViewVO.queryLastEnvInstanceVO();
        if (applicationInstanceOverViewVO.getLatestVersionId().equals(applicationInstanceOverViewDTO.getVersionId())) {
            applicationInstanceOverViewVO.appendInstances(new EnvInstancesVO(
                    applicationInstanceOverViewDTO.getInstanceId(), applicationInstanceOverViewDTO.getInstanceCode(), applicationInstanceOverViewDTO.getInstanceStatus()));
        }
        if (envInstanceVO.getEnvId().equals(applicationInstanceOverViewDTO.getEnvId())) {
            EnvVersionVO envVersionVO = envInstanceVO.queryLastEnvVersionVO();
            if (envVersionVO.getVersion().equals(applicationInstanceOverViewDTO.getVersion())) {
                envVersionVO.appendInstanceList(
                        applicationInstanceOverViewDTO.getInstanceId(),
                        applicationInstanceOverViewDTO.getInstanceCode(),
                        applicationInstanceOverViewDTO.getInstanceStatus());
            } else {
                envInstanceVO.addEnvVersionDTOS(new EnvVersionVO(
                        applicationInstanceOverViewDTO.getVersionId(),
                        applicationInstanceOverViewDTO.getVersion(),
                        applicationInstanceOverViewDTO.getInstanceId(),
                        applicationInstanceOverViewDTO.getInstanceCode(),
                        applicationInstanceOverViewDTO.getInstanceStatus()));
            }
        } else {
            EnvVersionVO envVersionVO = new EnvVersionVO(
                    applicationInstanceOverViewDTO.getVersionId(),
                    applicationInstanceOverViewDTO.getVersion(),
                    applicationInstanceOverViewDTO.getInstanceId(),
                    applicationInstanceOverViewDTO.getInstanceCode(),
                    applicationInstanceOverViewDTO.getInstanceStatus());
            envInstanceVO = new EnvInstanceVO(applicationInstanceOverViewDTO.getEnvId());
            if (applicationInstanceOverViewDTO.getVersionId().equals(applicationInstanceOverViewVO.getLatestVersionId())) {
                envVersionVO.setLatest(true);
            }
            envInstanceVO.addEnvVersionDTOS(envVersionVO);
            applicationInstanceOverViewVO.appendEnvInstanceVOS(envInstanceVO);
>>>>>>> [IMP]重构后端代码
        }

        <<<<<<<HEAD
=======

        >>>>>>>[IMP]重构后端代码
@Override
public ReplaceResult queryValues(String type,Long instanceId,Long versionId){
        ReplaceResult replaceResult=new ReplaceResult();
        String versionValue=FileUtil.checkValueFormat(applicationVersionRepository.queryValue(versionId));

        if(type.equals(UPDATE)){
        ApplicationInstanceE applicationInstanceE=applicationInstanceRepository.selectById(instanceId);
        if(applicationInstanceE.getValueId()==null){
        replaceResult.setYaml(getReplaceResult(versionValue,applicationInstanceRepository.queryValueByInstanceId(instanceId)).getYaml());
        }else{
        DevopsDeployValueE devopsDeployValueE=devopsDeployValueRepository.queryById(applicationInstanceE.getValueId());
        replaceResult.setYaml(getReplaceResult(versionValue,devopsDeployValueE.getValue()).getYaml());
        replaceResult.setName(devopsDeployValueE.getName());
        replaceResult.setId(devopsDeployValueE.getId());
        replaceResult.setObjectVersionNumber(devopsDeployValueE.getObjectVersionNumber());
        }
        }else{
        try{
        FileUtil.checkYamlFormat(versionValue);
        }catch(Exception e){
        replaceResult.setYaml(versionValue);
        return replaceResult;
        }
        replaceResult.setYaml(versionValue);
        }
        return replaceResult;
        }

<<<<<<< HEAD
@Override
public ReplaceResult queryUpgradeValue(Long instanceId,Long versionId){
        ApplicationInstanceE applicationInstanceE=applicationInstanceRepository.selectById(instanceId);
        String yaml=FileUtil.checkValueFormat(applicationInstanceRepository.queryValueByInstanceId(instanceId));
        String versionValue=applicationVersionRepository.queryValue(versionId);
        ReplaceResult replaceResult=new ReplaceResult();
        if(applicationInstanceE.getValueId()!=null){
        DevopsDeployValueE devopsDeployValueE=devopsDeployValueRepository.queryById(applicationInstanceE.getValueId());
        replaceResult.setName(devopsDeployValueE.getName());
        replaceResult.setId(devopsDeployValueE.getId());
        replaceResult.setObjectVersionNumber(devopsDeployValueE.getObjectVersionNumber());
        }
        replaceResult.setYaml(getReplaceResult(versionValue,yaml).getYaml());

        <<<<<<<HEAD
        return replaceResult;
                }

@Override
public DeployTimeDTO listDeployTime(Long projectId,Long envId,Long[]appIds,Date startTime,Date endTime){
        if(appIds.length==0){
        return new DeployTimeDTO();
        }
        List<DeployDO> deployDOS=applicationInstanceRepository
        .listDeployTime(projectId,envId,appIds,startTime,endTime);
        DeployTimeDTO deployTimeDTO=new DeployTimeDTO();
        List<Date> creationDates=deployDOS.stream().map(DeployDO::getCreationDate).collect(Collectors.toList());
        creationDates=new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder())
        .collect(Collectors.toList());
        List<DeployAppDTO> deployAppDTOS=new ArrayList<>();
        Map<String, List<DeployDO>>resultMaps=deployDOS.stream()
        .collect(Collectors.groupingBy(DeployDO::getAppName));
        resultMaps.forEach((key,value)->{
        DeployAppDTO deployAppDTO=new DeployAppDTO();
        List<DeployAppDetail> deployAppDetails=new ArrayList<>();
        deployAppDTO.setAppName(key);
        value.forEach(deployDO->{
        DeployAppDetail deployAppDetail=new DeployAppDetail();
        deployAppDetail.setDeployDate(deployDO.getCreationDate());
        deployAppDetail.setDeployTime(
        getDeployTime(deployDO.getLastUpdateDate().getTime()-deployDO.getCreationDate().getTime()));
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
public DeployFrequencyDTO listDeployFrequency(Long projectId,Long[]envIds,Long appId,Date startTime,
        Date endTime){
        if(envIds.length==0){
        return new DeployFrequencyDTO();
        }
        List<DeployDO> deployFrequencyDOS=applicationInstanceRepository
        .listDeployFrequency(projectId,envIds,appId,startTime,endTime);
        Map<String, List<DeployDO>>resultMaps=deployFrequencyDOS.stream()
        .collect(Collectors.groupingBy(t->new java.sql.Date(t.getCreationDate().getTime()).toString()));
        List<Long> deployFrequencys=new LinkedList<>();
        List<Long> deploySuccessFrequency=new LinkedList<>();
        List<Long> deployFailFrequency=new LinkedList<>();
        List<String> creationDates=deployFrequencyDOS.stream()
        .map(deployDO->new java.sql.Date(deployDO.getCreationDate().getTime()).toString())
        .collect(Collectors.toList());
        creationDates=new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder())
        .collect(Collectors.toList());
        creationDates.forEach(date->{
        Long[]newDeployFrequencys={0L};
        Long[]newDeploySuccessFrequency={0L};
        Long[]newDeployFailFrequency={0L};
        resultMaps.get(date).forEach(deployFrequencyDO->{
        newDeployFrequencys[0]=newDeployFrequencys[0]+1L;
        if(deployFrequencyDO.getStatus().equals(CommandStatus.SUCCESS.getStatus())){
        newDeploySuccessFrequency[0]=newDeploySuccessFrequency[0]+1L;
        }else{
        newDeployFailFrequency[0]=newDeployFailFrequency[0]+1L;
        }
        });
        deployFrequencys.add(newDeployFrequencys[0]);
        deploySuccessFrequency.add(newDeploySuccessFrequency[0]);
        deployFailFrequency.add(newDeployFailFrequency[0]);
        });
        DeployFrequencyDTO deployFrequencyDTO=new DeployFrequencyDTO();
        deployFrequencyDTO.setCreationDates(creationDates);
        deployFrequencyDTO.setDeployFailFrequency(deployFailFrequency);
        deployFrequencyDTO.setDeploySuccessFrequency(deploySuccessFrequency);
        deployFrequencyDTO.setDeployFrequencys(deployFrequencys);
        return deployFrequencyDTO;
        }

@Override
public PageInfo<DeployDetailDTO> pageDeployFrequencyDetail(Long projectId,PageRequest pageRequest,Long[]envIds,
        Long appId,Date startTime,Date endTime){
        if(envIds.length==0){
        return new PageInfo<>();
        }
        PageInfo<DeployDO> deployDOS=applicationInstanceRepository.pageDeployFrequencyDetail(projectId,pageRequest,
        envIds,appId,startTime,endTime);
        return getDeployDetailDTOS(deployDOS);
        }

@Override
public PageInfo<DeployDetailDTO> pageDeployTimeDetail(Long projectId,PageRequest pageRequest,Long[]appIds,
        Long envId,
        Date startTime,Date endTime){
        if(appIds.length==0){
        return new PageInfo<>();
        }
        PageInfo<DeployDO> deployDOS=applicationInstanceRepository.pageDeployTimeDetail(projectId,pageRequest,envId,
        appIds,startTime,endTime);
        return getDeployDetailDTOS(deployDOS);
        }

@Override
public void deployTestApp(ApplicationDeployDTO applicationDeployDTO){
        String versionValue=applicationVersionRepository.queryValue(applicationDeployDTO.getAppVersionId());
        ApplicationE applicationE=applicationRepository.query(applicationDeployDTO.getAppId());

        String secretCode=null;
        secretCode=getSecret(applicationE,secretCode,CHOERODON,null,applicationDeployDTO.getEnvironmentId());

        ApplicationVersionE applicationVersionE=applicationVersionRepository.query(applicationDeployDTO.getAppVersionId());
        FileUtil.checkYamlFormat(applicationDeployDTO.getValues());
        String deployValue=getReplaceResult(versionValue,
        applicationDeployDTO.getValues()).getDeltaYaml().trim();
        deployService.deployTestApp(applicationE,applicationVersionE,applicationDeployDTO.getInstanceName(),secretCode,applicationDeployDTO.getEnvironmentId(),deployValue);
        }

@Override
public InstanceControllerDetailDTO getInstanceResourceDetailJson(Long instanceId,String resourceName,ResourceType resourceType){
        String message=getAndCheckResourceDetail(instanceId,resourceName,resourceType);

        try{
        return new InstanceControllerDetailDTO(instanceId,new ObjectMapper().readTree(message));
        }catch(IOException e){
        throw new CommonException("error.instance.resource.json.read.failed",instanceId,message);
        }
        }

@Override
public InstanceControllerDetailDTO getInstanceResourceDetailYaml(Long instanceId,String resourceName,ResourceType resourceType){
        String message=getAndCheckResourceDetail(instanceId,resourceName,resourceType);

        try{
        return new InstanceControllerDetailDTO(instanceId,JsonYamlConversionUtil.json2yaml(message));
        }catch(IOException e){
        throw new CommonException(JsonYamlConversionUtil.ERROR_JSON_TO_YAML_FAILED,message);
        }
        }

private String getAndCheckResourceDetail(Long instanceId,String resourceName,ResourceType resourceType){
        String message=applicationInstanceRepository.getInstanceResourceDetailJson(instanceId,resourceName,resourceType);
        if(StringUtils.isEmpty(message)){
        throw new CommonException("error.instance.resource.not.found",instanceId,resourceType.getType());
        }
        return message;
        }

@Override
public void getTestAppStatus(Map<Long, List<String>>testReleases){
        deployService.getTestAppStatus(testReleases);
        }

@Override
public void operationPodCount(String deploymentName,Long envId,Long count){

        DevopsEnvironmentE devopsEnvironmentE=devopsEnvironmentRepository.queryById(envId);

        UserAttrE userAttrE=userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE,userAttrE);

        =======

        return replaceResult;
        }
=======
    @Override
    public InstanceValueVO queryDeployValue(String type, Long instanceId, Long versionId) {
        InstanceValueVO instanceValueVO = new InstanceValueVO();
        String versionValue = FileUtil.checkValueFormat(applicationVersionService.baseQueryValue(versionId));

        if (type.equals(UPDATE)) {
            ApplicationInstanceDTO applicationInstanceDTO = baseQuery(instanceId);
            if (applicationInstanceDTO.getValueId() != null) {
                DevopsDeployValueDTO devopsDeployValueDTO = devopsDeployValueService.baseQueryById(applicationInstanceDTO.getValueId());
                instanceValueVO.setName(devopsDeployValueDTO.getName());
                instanceValueVO.setId(devopsDeployValueDTO.getId());
                instanceValueVO.setObjectVersionNumber(devopsDeployValueDTO.getObjectVersionNumber());
            }
            instanceValueVO.setYaml(getReplaceResult(versionValue, baseQueryValueByInstanceId(instanceId)).getYaml());
        } else {
            try {
                FileUtil.checkYamlFormat(versionValue);
            } catch (Exception e) {
                instanceValueVO.setYaml(versionValue);
                return instanceValueVO;
            }
            instanceValueVO.setYaml(versionValue);
        }
        return instanceValueVO;
    }

    @Override
    public InstanceValueVO queryUpgradeValue(Long instanceId, Long versionId) {
        ApplicationInstanceDTO applicationInstanceDTO = baseQuery(instanceId);
        String yaml = FileUtil.checkValueFormat(baseQueryValueByInstanceId(instanceId));
        String versionValue = applicationVersionService.baseQueryValue(versionId);
        InstanceValueVO instanceValueVO = new InstanceValueVO();
        if (applicationInstanceDTO.getValueId() != null) {
            DevopsDeployValueDTO devopsDeployValueDTO = devopsDeployValueService.baseQueryById(applicationInstanceDTO.getValueId());
            instanceValueVO.setName(devopsDeployValueDTO.getName());
            instanceValueVO.setId(devopsDeployValueDTO.getId());
            instanceValueVO.setObjectVersionNumber(devopsDeployValueDTO.getObjectVersionNumber());
        }
        instanceValueVO.setYaml(getReplaceResult(versionValue, yaml).getYaml());
        return instanceValueVO;
    }
>>>>>>> [IMP]重构后端代码

@Override
public DeployTimeDTO listDeployTime(Long projectId,Long envId,Long[]appIds,Date
        startTime,Date endTime){
        if(appIds.length==0){
        return new DeployTimeDTO();
        }
        List<DeployDO> deployDOS=applicationInstanceRepository
        .listDeployTime(projectId,envId,appIds,startTime,endTime);
        DeployTimeDTO deployTimeDTO=new DeployTimeDTO();
        List<Date> creationDates=deployDOS.stream().map(DeployDO::getCreationDate).collect(Collectors.toList());
        creationDates=new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder())
        .collect(Collectors.toList());
        List<DeployAppDTO> deployAppDTOS=new ArrayList<>();
        Map<String, List<DeployDO>>resultMaps=deployDOS.stream()
        .collect(Collectors.groupingBy(DeployDO::getAppName));
        resultMaps.forEach((key,value)->{
        DeployAppDTO deployAppDTO=new DeployAppDTO();
        List<DeployAppDetail> deployAppDetails=new ArrayList<>();
        deployAppDTO.setAppName(key);
        value.forEach(deployDO->{
        DeployAppDetail deployAppDetail=new DeployAppDetail();
        deployAppDetail.setDeployDate(deployDO.getCreationDate());
        deployAppDetail.setDeployTime(
        getDeployTime(deployDO.getLastUpdateDate().getTime()-deployDO.getCreationDate().getTime()));
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
public DeployFrequencyDTO listDeployFrequency(Long projectId,Long[]
        envIds,Long appId,Date startTime,
        Date endTime){
        if(envIds.length==0){
        return new DeployFrequencyDTO();
        }
        List<DeployDO> deployFrequencyDOS=applicationInstanceRepository
        .listDeployFrequency(projectId,envIds,appId,startTime,endTime);
        Map<String, List<DeployDO>>resultMaps=deployFrequencyDOS.stream()
        .collect(Collectors.groupingBy(t->new java.sql.Date(t.getCreationDate().getTime()).toString()));
        List<Long> deployFrequencys=new LinkedList<>();
        List<Long> deploySuccessFrequency=new LinkedList<>();
        List<Long> deployFailFrequency=new LinkedList<>();
        List<String> creationDates=deployFrequencyDOS.stream()
        .map(deployDO->new java.sql.Date(deployDO.getCreationDate().getTime()).toString())
        .collect(Collectors.toList());
        creationDates=new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder())
        .collect(Collectors.toList());
        creationDates.forEach(date->{
        Long[]newDeployFrequencys={0L};
        Long[]newDeploySuccessFrequency={0L};
        Long[]newDeployFailFrequency={0L};
        resultMaps.get(date).forEach(deployFrequencyDO->{
        newDeployFrequencys[0]=newDeployFrequencys[0]+1L;
        if(deployFrequencyDO.getStatus().equals(CommandStatus.SUCCESS.getStatus())){
        newDeploySuccessFrequency[0]=newDeploySuccessFrequency[0]+1L;
        }else{
        newDeployFailFrequency[0]=newDeployFailFrequency[0]+1L;
        }
        });
        deployFrequencys.add(newDeployFrequencys[0]);
        deploySuccessFrequency.add(newDeploySuccessFrequency[0]);
        deployFailFrequency.add(newDeployFailFrequency[0]);
        });
        DeployFrequencyDTO deployFrequencyDTO=new DeployFrequencyDTO();
        deployFrequencyDTO.setCreationDates(creationDates);
        deployFrequencyDTO.setDeployFailFrequency(deployFailFrequency);
        deployFrequencyDTO.setDeploySuccessFrequency(deploySuccessFrequency);
        deployFrequencyDTO.setDeployFrequencys(deployFrequencys);
        return deployFrequencyDTO;
        }

@Override
public PageInfo<DeployDetailDTO> pageDeployFrequencyDetail(Long
        projectId,PageRequest pageRequest,Long[]envIds,
        Long appId,Date startTime,Date endTime){
        if(envIds.length==0){
        return new PageInfo<>();
        }
        PageInfo<DeployDO> deployDOS=applicationInstanceRepository.pageDeployFrequencyDetail(projectId,pageRequest,
        envIds,appId,startTime,endTime);
        return getDeployDetailDTOS(deployDOS);
        }

<<<<<<< HEAD
@Override
public void deployTestApp(ApplicationDeployDTO applicationDeployDTO){
        String versionValue=applicationVersionRepository.baseQueryValue(applicationDeployDTO.getAppVersionId());
        ApplicationE applicationE=applicationRepository.query(applicationDeployDTO.getAppId());

        String secretCode=null;
        secretCode=getSecret(applicationE,secretCode,CHOERODON,null,applicationDeployDTO.getEnvironmentId());

        ApplicationVersionE applicationVersionE=applicationVersionRepository.baseQuery(applicationDeployDTO.getAppVersionId());
        FileUtil.checkYamlFormat(applicationDeployDTO.getValues());
        String deployValue=getReplaceResult(versionValue,
        applicationDeployDTO.getValues()).getDeltaYaml().trim();
        deployService.deployTestApp(applicationE,applicationVersionE,applicationDeployDTO.getInstanceName(),secretCode,applicationDeployDTO.getEnvironmentId(),deployValue);
        }


@Override
public InstanceControllerDetailDTO getInstanceResourceDetailJson(Long
        instanceId,String resourceName,ResourceType resourceType){
        String message=getAndCheckResourceDetail(instanceId,resourceName,resourceType);

        try{
        return new InstanceControllerDetailDTO(instanceId,new ObjectMapper().readTree(message));
        }catch(IOException e){
        throw new CommonException("error.instance.resource.json.read.failed",instanceId,message);
        }
=======
    @Override
    public void deployTestApp(ApplicationDeployVO applicationDeployVO) {
        String versionValue = applicationVersionRepository.baseQueryValue(applicationDeployVO.getAppVersionId());
        ApplicationE applicationE = applicationRepository.query(applicationDeployVO.getAppId());

        String secretCode = null;
        secretCode = getSecret(applicationE, secretCode, CHOERODON, null, applicationDeployVO.getEnvironmentId());

        ApplicationVersionE applicationVersionE = applicationVersionRepository.baseQuery(applicationDeployVO.getAppVersionId());
        FileUtil.checkYamlFormat(applicationDeployVO.getValues());
        String deployValue = getReplaceResult(versionValue,
                applicationDeployVO.getValues()).getDeltaYaml().trim();
        agentCommandService.deployTestApp(applicationE, applicationVersionE, applicationDeployVO.getInstanceName(), secretCode, applicationDeployVO.getEnvironmentId(), deployValue);
    }


    @Override
    public InstanceControllerDetailVO queryInstanceResourceDetailJson(Long
                                                                              instanceId, String resourceName, ResourceType resourceType) {
        String message = getAndCheckResourceDetail(instanceId, resourceName, resourceType);

        try {
            return new InstanceControllerDetailVO(instanceId, new ObjectMapper().readTree(message));
        } catch (IOException e) {
            throw new CommonException("error.instance.resource.json.read.failed", instanceId, message);
>>>>>>> [IMP]重构后端代码
        }

<<<<<<< HEAD
@Override
public InstanceControllerDetailDTO getInstanceResourceDetailYaml(Long
        instanceId,String resourceName,ResourceType resourceType){
        String message=getAndCheckResourceDetail(instanceId,resourceName,resourceType);

        try{
        return new InstanceControllerDetailDTO(instanceId,JsonYamlConversionUtil.json2yaml(message));
        }catch(IOException e){
        throw new CommonException(JsonYamlConversionUtil.ERROR_JSON_TO_YAML_FAILED,message);
        }
=======
    @Override
    public InstanceControllerDetailVO getInstanceResourceDetailYaml(Long
                                                                            instanceId, String resourceName, ResourceType resourceType) {
        String message = getAndCheckResourceDetail(instanceId, resourceName, resourceType);

        try {
            return new InstanceControllerDetailVO(instanceId, JsonYamlConversionUtil.json2yaml(message));
        } catch (IOException e) {
            throw new CommonException(JsonYamlConversionUtil.ERROR_JSON_TO_YAML_FAILED, message);
>>>>>>> [IMP]重构后端代码
        }

<<<<<<< HEAD
private String getAndCheckResourceDetail(Long instanceId,String
        resourceName,ResourceType resourceType){
        String message=applicationInstanceRepository.getInstanceResourceDetailJson(instanceId,resourceName,resourceType);
        if(StringUtils.isEmpty(message)){
        throw new CommonException("error.instance.resource.not.found",instanceId,resourceType.getType());
=======
    private String getAndCheckResourceDetail(Long instanceId, String
            resourceName, ResourceType resourceType) {
        String message = baseGetInstanceResourceDetailJson(instanceId, resourceName, resourceType);
        if (StringUtils.isEmpty(message)) {
            throw new CommonException("error.instance.resource.not.found", instanceId, resourceType.getType());
>>>>>>> [IMP]重构后端代码
        }
        return message;
        }

<<<<<<< HEAD
@Override
public void getTestAppStatus(Map<Long, List<String>>testReleases){
        deployService.getTestAppStatus(testReleases);
        }
=======
    @Override
    public void getTestAppStatus(Map<Long, List<String>> testReleases) {
        agentCommandService.getTestAppStatus(testReleases);
    }
>>>>>>> [IMP]重构后端代码

@Override
public void operationPodCount(String deploymentName,Long envId,Long
        count){

<<<<<<< HEAD
        DevopsEnvironmentE devopsEnvironmentE=devopsEnvironmentRepository.queryById(envId);
=======
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(envId);
>>>>>>> [IMP] 重构Repository

<<<<<<< HEAD
        UserAttrE userAttrE=userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
=======
        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
>>>>>>> [REF] refactor UserAttrRepository

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE,userAttrE);

        >>>>>>>[IMP]重构后端代码
        //不能减少到0
        if(count==0){
        return;
        }
        deployService.operatePodCount(deploymentName,devopsEnvironmentE.getCode(),devopsEnvironmentE.getClusterE().getId(),count);
        }
<<<<<<< HEAD
=======
        agentCommandService.operatePodCount(deploymentName, devopsEnvironmentE.getCode(), devopsEnvironmentE.getClusterE().getId(), count);
    }
>>>>>>> [IMP]重构后端代码


        <<<<<<<HEAD
private PageInfo<DeployDetailDTO> getDeployDetailDTOS(PageInfo<DeployDO> deployDOS){
        =======
private PageInfo<DeployDetailDTO> getDeployDetailDTOS
        (PageInfo<DeployDO> deployDOS){
        >>>>>>>[IMP]重构后端代码
        PageInfo<DeployDetailDTO> pageDeployDetailDTOS=new PageInfo<>();
        List<DeployDetailDTO> deployDetailDTOS=new ArrayList<>();
        BeanUtils.copyProperties(deployDOS,pageDeployDetailDTOS);
        deployDOS.getList().forEach(deployDO->{
        DeployDetailDTO deployDetailDTO=new DeployDetailDTO();
        BeanUtils.copyProperties(deployDO,deployDetailDTO);
        deployDetailDTO.setDeployTime(
        getDeployTime(deployDO.getLastUpdateDate().getTime()-deployDO.getCreationDate().getTime()));
        if(deployDO.getCreatedBy()!=0){
        UserE userE=iamRepository.queryUserByUserId(deployDO.getCreatedBy());
        deployDetailDTO.setLastUpdatedName(userE.getRealName());
        }
        deployDetailDTOS.add(deployDetailDTO);
        });
        pageDeployDetailDTOS.setList(deployDetailDTOS);
        return pageDeployDetailDTOS;
        }

<<<<<<< HEAD
@Override
public ReplaceResult queryValue(Long instanceId){
        ReplaceResult replaceResult=new ReplaceResult();
        String yaml=FileUtil.checkValueFormat(applicationInstanceRepository.queryValueByInstanceId(
        instanceId));
        replaceResult.setYaml(yaml);
        return replaceResult;
        }

@Override
<<<<<<<HEAD
public List<ErrorLineDTO> formatValue(ReplaceResult replaceResult){
        =======
public List<ErrorLineDTO> formatValue(ReplaceResult
        replaceResult){
        >>>>>>>[IMP]重构后端代码
        try{
        FileUtil.checkYamlFormat(replaceResult.getYaml());

        String fileName=GenerateUUID.generateUUID()+YAML_SUFFIX;
        String path="deployfile";
        FileUtil.saveDataToFile(path,fileName,replaceResult.getYaml());
        //读入文件
        File file=new File(path+System.getProperty(FILE_SEPARATOR)+fileName);
        InputStreamResource inputStreamResource=new InputStreamResource(new FileInputStream(file));
        YamlPropertySourceLoader yamlPropertySourceLoader=new YamlPropertySourceLoader();
        try{
        yamlPropertySourceLoader.load("test",inputStreamResource);
        }catch(Exception e){
        FileUtil.deleteFile(path+System.getProperty(FILE_SEPARATOR)+fileName);
        return getErrorLine(e.getMessage());
        }
        FileUtil.deleteFile(path+System.getProperty(FILE_SEPARATOR)+fileName);
        }catch(Exception e){
        return getErrorLine(e.getMessage());
=======
    @Override
    public InstanceValueVO queryLastDeployValue(Long instanceId) {
        InstanceValueVO instanceValueVO = new InstanceValueVO();
        String yaml = FileUtil.checkValueFormat(baseQueryValueByInstanceId(
                instanceId));
        instanceValueVO.setYaml(yaml);
        return instanceValueVO;
    }

    @Override
    public List<ErrorLineVO> formatValue(InstanceValueVO
                                                 instanceValueVO) {
        try {
            FileUtil.checkYamlFormat(instanceValueVO.getYaml());

            String fileName = GenerateUUID.generateUUID() + YAML_SUFFIX;
            String path = "deployfile";
            FileUtil.saveDataToFile(path, fileName, instanceValueVO.getYaml());
            //读入文件
            File file = new File(path + System.getProperty(FILE_SEPARATOR) + fileName);
            InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream(file));
            YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
            try {
                yamlPropertySourceLoader.load("test", inputStreamResource);
            } catch (Exception e) {
                FileUtil.deleteFile(path + System.getProperty(FILE_SEPARATOR) + fileName);
                return getErrorLine(e.getMessage());
            }
            FileUtil.deleteFile(path + System.getProperty(FILE_SEPARATOR) + fileName);
        } catch (Exception e) {
            return getErrorLine(e.getMessage());
>>>>>>> [IMP]重构后端代码
        }
        return new ArrayList<>();
        }


@Override
<<<<<<<HEAD
public DevopsEnvPreviewDTO listByEnv(Long projectId,Long envId,String params){

        Map<String, Object> maps=gson.fromJson(params,new TypeToken<Map<String, Object>>(){
        }.getType());
        List<Long> connectedEnvList=clusterConnectionHandler.getConnectedEnvList();
        List<Long> updatedEnvList=clusterConnectionHandler.getUpdatedEnvList();

        Map<String, Object> searchParamMap=TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap=TypeUtil.cast(maps.get(TypeUtil.PARAM));

        List<ApplicationInstanceDTO> applicationInstancesDOS=applicationInstanceMapper
        .listApplicationInstance(projectId,envId,null,null,null,searchParamMap,paramMap);
        List<ApplicationInstanceE> applicationInstanceES=ConvertHelper
        .convertList(applicationInstancesDOS,ApplicationInstanceE.class);

        setInstanceConnect(applicationInstanceES,connectedEnvList,updatedEnvList);
        Map<Long, List<ApplicationInstanceE>>resultMaps=applicationInstanceES.stream()
        .collect(Collectors.groupingBy(t->t.getApplicationE().getId()));
        DevopsEnvPreviewDTO devopsEnvPreviewDTO=new DevopsEnvPreviewDTO();
        List<DevopsEnvPreviewAppDTO> devopsEnvPreviewAppDTOS=new ArrayList<>();
        resultMaps.forEach((key,value)->{
        DevopsEnvPreviewAppDTO devopsEnvPreviewAppDTO=new DevopsEnvPreviewAppDTO();
        devopsEnvPreviewAppDTO.setAppName(value.get(0).getApplicationE().getName());
        devopsEnvPreviewAppDTO.setAppCode(value.get(0).getAppCode());
        devopsEnvPreviewAppDTO.setProjectId(value.get(0).getProjectId());
        List<ApplicationInstanceVO> applicationInstanceVOS=ConvertHelper
        .convertList(value,ApplicationInstanceVO.class);

        // set instances
        devopsEnvPreviewAppDTO.setApplicationInstanceVOS(applicationInstanceVOS);

        devopsEnvPreviewAppDTOS.add(devopsEnvPreviewAppDTO);
        });
        devopsEnvPreviewDTO.setDevopsEnvPreviewAppDTOS(devopsEnvPreviewAppDTOS);
        return devopsEnvPreviewDTO;
        }
        =======
public DevopsEnvPreviewDTO listByEnv(Long projectId,Long
        envId,String params){

        Map<String, Object> maps=gson.fromJson(params,new TypeToken<Map<String, Object>>(){
        }.getType());
        List<Long> connectedEnvList=clusterConnectionHandler.getConnectedEnvList();
        List<Long> updatedEnvList=clusterConnectionHandler.getUpdatedEnvList();

        Map<String, Object> searchParamMap=TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap=TypeUtil.cast(maps.get(TypeUtil.PARAM));

        List<ApplicationInstanceDTO> applicationInstancesDOS=applicationInstanceMapper
        .listApplicationInstance(projectId,envId,null,null,null,searchParamMap,paramMap);
        List<ApplicationInstanceE> applicationInstanceES=ConvertHelper
        .convertList(applicationInstancesDOS,ApplicationInstanceE.class);

        setInstanceConnect(applicationInstanceES,connectedEnvList,updatedEnvList);
        Map<Long, List<ApplicationInstanceE>>resultMaps=applicationInstanceES.stream()
        .collect(Collectors.groupingBy(t->t.getApplicationE().getId()));
        DevopsEnvPreviewDTO devopsEnvPreviewDTO=new DevopsEnvPreviewDTO();
        List<DevopsEnvPreviewAppDTO> devopsEnvPreviewAppDTOS=new ArrayList<>();
        resultMaps.forEach((key,value)->{
        DevopsEnvPreviewAppDTO devopsEnvPreviewAppDTO=new DevopsEnvPreviewAppDTO();
        devopsEnvPreviewAppDTO.setAppName(value.get(0).getApplicationE().getName());
        devopsEnvPreviewAppDTO.setAppCode(value.get(0).getAppCode());
        devopsEnvPreviewAppDTO.setProjectId(value.get(0).getProjectId());
        List<ApplicationInstanceVO> applicationInstanceVOS=ConvertHelper
        .convertList(value,ApplicationInstanceVO.class);

        // set instances
        devopsEnvPreviewAppDTO.setApplicationInstanceVOS(applicationInstanceVOS);

        devopsEnvPreviewAppDTOS.add(devopsEnvPreviewAppDTO);
        });
        devopsEnvPreviewDTO.setDevopsEnvPreviewAppDTOS(devopsEnvPreviewAppDTOS);
        return devopsEnvPreviewDTO;
        }

        >>>>>>>[IMP]重构后端代码

<<<<<<< HEAD
@Override
public DevopsEnvResourceDTO listResourcesInHelmRelease(Long
        instanceId){

        // 获取相关的pod
        List<DevopsEnvPodDTO> devopsEnvPodDTOS=ConvertHelper
        .convertList(devopsEnvPodRepository.selectByInstanceId(instanceId),
        DevopsEnvPodDTO.class);

        DevopsEnvResourceDTO devopsEnvResourceDTO=devopsEnvResourceService
        .listResourcesInHelmRelease(instanceId);
=======
    @Override
    public DevopsEnvResourceVO listResourcesInHelmRelease(Long instanceId) {

        // 获取相关的pod
        List<DevopsEnvironmentPodVO> devopsEnvPodDTOS = ConvertUtils.convertList(devopsEnvPodService.baseListByInstanceId(instanceId), DevopsEnvironmentPodVO.class);

        DevopsEnvResourceVO devopsEnvResourceVO = devopsEnvResourceService
                .listResourcesInHelmRelease(instanceId);
>>>>>>> [IMP] 重构Repository

        // 关联其pod并设置deployment
<<<<<<< HEAD
        devopsEnvResourceDTO.setDeploymentDTOS(devopsEnvResourceDTO.getDeploymentDTOS()
        .stream()
        .peek(deploymentDTO->deploymentDTO.setDevopsEnvPodDTOS(filterPodsAssociated(devopsEnvPodDTOS,deploymentDTO.getName())))
        .collect(Collectors.toList())
        );

        // 关联其pod并设置daemonSet
        devopsEnvResourceDTO.setDaemonSetDTOS(
        devopsEnvResourceDTO.getDaemonSetDTOS()
        .stream()
        .peek(daemonSetDTO->daemonSetDTO.setDevopsEnvPodDTOS(
        filterPodsAssociatedWithDaemonSet(devopsEnvPodDTOS,daemonSetDTO.getName())
        ))
        .collect(Collectors.toList())
        );

        // 关联其pod并设置statefulSet
        devopsEnvResourceDTO.setStatefulSetDTOS(
        devopsEnvResourceDTO.getStatefulSetDTOS()
        .stream()
        .peek(statefulSetDTO->statefulSetDTO.setDevopsEnvPodDTOS(
        filterPodsAssociatedWithStatefulSet(devopsEnvPodDTOS,statefulSetDTO.getName()))
        )
        .collect(Collectors.toList())
        );


        return devopsEnvResourceDTO;
        }
=======
        devopsEnvResourceVO.setDeploymentVOS(devopsEnvResourceVO.getDeploymentVOS()
                .stream()
                .peek(deploymentVO -> deploymentVO.setDevopsEnvironmentPodVOS(filterPodsAssociated(devopsEnvPodDTOS, deploymentVO.getName())))
                .collect(Collectors.toList())
        );

        // 关联其pod并设置daemonSet
        devopsEnvResourceVO.setDaemonSetVOS(
                devopsEnvResourceVO.getDaemonSetVOS()
                        .stream()
                        .peek(daemonSetVO -> daemonSetVO.setDevopsEnvPodDTOS(
                                filterPodsAssociatedWithDaemonSet(devopsEnvPodDTOS, daemonSetVO.getName())
                        ))
                        .collect(Collectors.toList())
        );

        // 关联其pod并设置statefulSet
        devopsEnvResourceVO.setStatefulSetVOS(
                devopsEnvResourceVO.getStatefulSetVOS()
                        .stream()
                        .peek(statefulSetVO -> statefulSetVO.setDevopsEnvironmentPodVOS(
                                filterPodsAssociatedWithStatefulSet(devopsEnvPodDTOS, statefulSetVO.getName()))
                        )
                        .collect(Collectors.toList())
        );


        return devopsEnvResourceVO;
    }
>>>>>>> [IMP]重构后端代码

<<<<<<< HEAD
        <<<<<<<HEAD
@Override
public DevopsEnvResourceDTO listResourcesInHelmRelease(Long instanceId){

        // 获取相关的pod
        List<DevopsEnvPodDTO> devopsEnvPodDTOS=ConvertHelper
        .convertList(devopsEnvPodRepository.selectByInstanceId(instanceId),
        DevopsEnvPodDTO.class);

        DevopsEnvResourceDTO devopsEnvResourceDTO=devopsEnvResourceService
        .listResourcesInHelmRelease(instanceId);

        // 关联其pod并设置deployment
        devopsEnvResourceDTO.setDeploymentDTOS(devopsEnvResourceDTO.getDeploymentDTOS()
        .stream()
        .peek(deploymentDTO->deploymentDTO.setDevopsEnvPodDTOS(filterPodsAssociated(devopsEnvPodDTOS,deploymentDTO.getName())))
        .collect(Collectors.toList())
        );

        // 关联其pod并设置daemonSet
        devopsEnvResourceDTO.setDaemonSetDTOS(
        devopsEnvResourceDTO.getDaemonSetDTOS()
        .stream()
        .peek(daemonSetDTO->daemonSetDTO.setDevopsEnvPodDTOS(
        filterPodsAssociatedWithDaemonSet(devopsEnvPodDTOS,daemonSetDTO.getName())
        ))
        .collect(Collectors.toList())
        );

        // 关联其pod并设置statefulSet
        devopsEnvResourceDTO.setStatefulSetDTOS(
        devopsEnvResourceDTO.getStatefulSetDTOS()
        .stream()
        .peek(statefulSetDTO->statefulSetDTO.setDevopsEnvPodDTOS(
        filterPodsAssociatedWithStatefulSet(devopsEnvPodDTOS,statefulSetDTO.getName()))
        )
        .collect(Collectors.toList())
        );


<<<<<<< HEAD
        return devopsEnvResourceDTO;
        }

/**
 * filter the pods that are associated with the deployment.
 *
 * @param devopsEnvPodDTOS the pods to be filtered
 * @param deploymentName   the name of deployment
 * @return the pods
 */
private List<DevopsEnvPodDTO> filterPodsAssociated(List<DevopsEnvPodDTO> devopsEnvPodDTOS,String deploymentName){
        return devopsEnvPodDTOS.stream().filter(devopsEnvPodDTO->{
        String podName=devopsEnvPodDTO.getName();
        String controllerNameFromPod=podName.substring(0,
        podName.lastIndexOf('-',podName.lastIndexOf('-')-1));
        return deploymentName.equals(controllerNameFromPod);
        }
=======
<<<<<<< HEAD
=======
>>>>>>> [REF] refactor DevopsBranchRepository
    /**
     * filter the pods that are associated with the deployment.
     *
     * @param devopsEnvPodDTOS the pods to be filtered
     * @param deploymentName   the name of deployment
     * @return the pods
     */
    private List<DevopsEnvironmentPodVO> filterPodsAssociated
    (List<DevopsEnvironmentPodVO> devopsEnvPodDTOS, String
            deploymentName) {
        return devopsEnvPodDTOS.stream().filter(devopsEnvPodDTO -> {
                    String podName = devopsEnvPodDTO.getName();
                    String controllerNameFromPod = podName.substring(0,
                            podName.lastIndexOf('-', podName.lastIndexOf('-') - 1));
                    return deploymentName.equals(controllerNameFromPod);
                }
>>>>>>> [REF] refactor DeployDetailRepository
        ).collect(Collectors.toList());
        }

<<<<<<< HEAD
/**
 * filter the pods that are associated with the daemonSet.
 *
 * @param devopsEnvPodDTOS the pods to be filtered
 * @param daemonSetName    the name of daemonSet
 * @return the pods
 */
private List<DevopsEnvPodDTO> filterPodsAssociatedWithDaemonSet(List<DevopsEnvPodDTO> devopsEnvPodDTOS,String daemonSetName){
=======
    /**
     * filter the pods that are associated with the daemonSet.
     *
     * @param devopsEnvPodDTOS the pods to be filtered
     * @param daemonSetName    the name of daemonSet
     * @return the pods
     */
    private List<DevopsEnvironmentPodVO> filterPodsAssociatedWithDaemonSet
    (List<DevopsEnvironmentPodVO> devopsEnvPodDTOS, String
            daemonSetName) {
>>>>>>> [IMP] 重构Repository
        return devopsEnvPodDTOS
        .stream()
        .filter(
        devopsEnvPodDTO->daemonSetName.equals(devopsEnvPodDTO.getName().substring(0,devopsEnvPodDTO.getName().lastIndexOf('-')))
        )
        .collect(Collectors.toList());
        }

<<<<<<< HEAD
/**
 * filter the pods that are associated with the statefulSet.
 *
 * @param devopsEnvPodDTOS the pods to be filtered
 * @param statefulSetName  the name of statefulSet
 * @return the pods
 */
private List<DevopsEnvPodDTO> filterPodsAssociatedWithStatefulSet(List<DevopsEnvPodDTO> devopsEnvPodDTOS,String statefulSetName){
=======
    /**
     * filter the pods that are associated with the statefulSet.
     *
     * @param devopsEnvPodDTOS the pods to be filtered
     * @param statefulSetName  the name of statefulSet
     * @return the pods
     */
    private List<DevopsEnvironmentPodVO> filterPodsAssociatedWithStatefulSet
    (List<DevopsEnvironmentPodVO> devopsEnvPodDTOS, String
            statefulSetName) {
>>>>>>> [IMP] 重构Repository
        // statefulSet名称逻辑和daemonSet一致
        return filterPodsAssociatedWithDaemonSet(devopsEnvPodDTOS,statefulSetName);
        }

<<<<<<< HEAD
@Override
@Saga(code = "devops-create-instance",
        description = "Devops创建实例", inputSchema = "{}")
@Transactional(rollbackFor = Exception.class)
public ApplicationInstanceVO createOrUpdate(ApplicationDeployDTO applicationDeployDTO){
=======
    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_INSTANCE,
            description = "Devops创建实例", inputSchema = "{}")
    @Transactional(rollbackFor = Exception.class)
    public ApplicationInstanceVO createOrUpdate
            (ApplicationDeployVO applicationDeployVO) {

<<<<<<< HEAD
        /**
         * filter the pods that are associated with the statefulSet.
         *
         * @param devopsEnvironmentPodVOS the pods to be filtered
         * @param statefulSetName  the name of statefulSet
         * @return the pods
         */
        private List<DevopsEnvironmentPodVO> filterPodsAssociatedWithStatefulSet (List <DevopsEnvironmentPodVO> devopsEnvironmentPodVOS, String statefulSetName){
            // statefulSet名称逻辑和daemonSet一致
            return filterPodsAssociatedWithDaemonSet(devopsEnvironmentPodVOS, statefulSetName);
        }
>>>>>>> [REF] refactor DeployDetailRepository

        DevopsEnvironmentE devopsEnvironmentE=devopsEnvironmentRepository.queryById(applicationDeployDTO.getEnvironmentId());

<<<<<<< HEAD
        UserAttrE userAttrE=userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
=======
        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
>>>>>>> [REF] refactor UserAttrRepository

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE,userAttrE);
=======
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(applicationDeployVO.getEnvironmentId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
>>>>>>> [IMP]重构后端代码

<<<<<<< HEAD
        //校验values
        FileUtil.checkYamlFormat(applicationDeployVO.getValues());

        ApplicationE applicationE=applicationRepository.query(applicationDeployDTO.getAppId());
        ApplicationVersionE applicationVersionE=
        applicationVersionRepository.query(applicationDeployDTO.getAppVersionId());
        =======
/**
 * filter the pods that are associated with the deployment.
 *
 * @param devopsEnvPodDTOS the pods to be filtered
 * @param deploymentName   the name of deployment
 * @return the pods
 */
private List<DevopsEnvPodDTO> filterPodsAssociated
        (List<DevopsEnvPodDTO> devopsEnvPodDTOS,String
        deploymentName){
        return devopsEnvPodDTOS.stream().filter(devopsEnvPodDTO->{
        String podName=devopsEnvPodDTO.getName();
        String controllerNameFromPod=podName.substring(0,
        podName.lastIndexOf('-',podName.lastIndexOf('-')-1));
        return deploymentName.equals(controllerNameFromPod);
        }
        ).collect(Collectors.toList());
        }

/**
 * filter the pods that are associated with the daemonSet.
 *
 * @param devopsEnvPodDTOS the pods to be filtered
 * @param daemonSetName    the name of daemonSet
 * @return the pods
 */
private List<DevopsEnvPodDTO> filterPodsAssociatedWithDaemonSet
        (List<DevopsEnvPodDTO> devopsEnvPodDTOS,String
        daemonSetName){
        return devopsEnvPodDTOS
        .stream()
        .filter(
        devopsEnvPodDTO->daemonSetName.equals(devopsEnvPodDTO.getName().substring(0,devopsEnvPodDTO.getName().lastIndexOf('-')))
        )
        .collect(Collectors.toList());
        }

/**
 * filter the pods that are associated with the statefulSet.
 *
 * @param devopsEnvPodDTOS the pods to be filtered
 * @param statefulSetName  the name of statefulSet
 * @return the pods
 */
private List<DevopsEnvPodDTO> filterPodsAssociatedWithStatefulSet
        (List<DevopsEnvPodDTO> devopsEnvPodDTOS,String
        statefulSetName){
        // statefulSet名称逻辑和daemonSet一致
        return filterPodsAssociatedWithDaemonSet(devopsEnvPodDTOS,statefulSetName);
        }

@Override
@Saga(code = "devops-create-instance",
        description = "Devops创建实例", inputSchema = "{}")
@Transactional(rollbackFor = Exception.class)
public ApplicationInstanceVO createOrUpdate
        (ApplicationDeployDTO applicationDeployDTO){
=======
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(applicationDeployDTO.getEnvironmentId());
>>>>>>> [IMP] 重构Repository

        DevopsEnvironmentE devopsEnvironmentE=devopsEnvironmentRepository.queryById(applicationDeployDTO.getEnvironmentId());

        UserAttrE userAttrE=userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE,userAttrE);

        //校验values
        FileUtil.checkYamlFormat(applicationDeployDTO.getValues());

<<<<<<< HEAD

        ApplicationE applicationE=applicationRepository.query(applicationDeployDTO.getAppId());
        ApplicationVersionE applicationVersionE=
        applicationVersionRepository.baseQuery(applicationDeployDTO.getAppVersionId());
        >>>>>>>[IMP]重构后端代码

        //初始化ApplicationInstanceE,DevopsEnvCommandE,DevopsEnvCommandValueE
        ApplicationInstanceE applicationInstanceE=initApplicationInstanceE(applicationDeployDTO);
        DevopsEnvCommandE devopsEnvCommandE=initDevopsEnvCommandE(applicationDeployDTO);
        DevopsEnvCommandValueE devopsEnvCommandValueE=initDevopsEnvCommandValueE(applicationDeployDTO);

        String secretCode=null;
        secretCode=getSecret(applicationE,secretCode,devopsEnvironmentE.getCode(),devopsEnvironmentE.getId(),devopsEnvironmentE.getClusterE().getId());


        // 初始化自定义实例名
        String code;
        if(applicationDeployDTO.getType().equals(CREATE)){
        if(applicationDeployDTO.getInstanceName()==null||applicationDeployDTO.getInstanceName().trim().equals("")){
        code=String.format("%s-%s",applicationE.getCode(),GenerateUUID.generateUUID().substring(0,5));
        }else{
        code=applicationDeployDTO.getInstanceName();
=======
        ApplicationDTO applicationDTO = applicationService.baseQuery(applicationDeployVO.getAppId());
        ApplicationVersionDTO applicationVersionDTO =
                applicationVersionService.baseQuery(applicationDeployVO.getAppVersionId());

        //初始化ApplicationInstanceDTO,DevopsEnvCommandDTO,DevopsEnvCommandValueDTO
        ApplicationInstanceDTO applicationInstanceDTO = initApplicationInstanceDTO(applicationDeployVO);
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(applicationDeployVO);
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = initDevopsEnvCommandValueDTO(applicationDeployVO);

        String secretCode = null;
        //获取部署实例时授权secret的code
        secretCode = getSecret(applicationDTO, secretCode, devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getClusterId());

        // 初始化自定义实例名
        String code;
        if (applicationDeployVO.getType().equals(CREATE)) {
            if (applicationDeployVO.getInstanceName() == null || applicationDeployVO.getInstanceName().trim().equals("")) {
                code = String.format("%s-%s", applicationDTO.getCode(), GenerateUUID.generateUUID().substring(0, 5));
            } else {
                code = applicationDeployVO.getInstanceName();
            }
        } else {
            code = applicationInstanceDTO.getCode();
            //更新实例的时候校验gitops库文件是否存在,处理部署实例时，由于没有创gitops文件导致的部署失败
            resourceFileCheckHandler.check(devopsEnvironmentDTO, applicationDeployVO.getAppInstanceId(), code, C7NHELM_RELEASE);

            //从未关联部署配置到关联部署配置，或者从一个部署配置关联另外一个部署配置，如果values是一样的，虽然getIsNotChange为false,但是此时也应该直接设置为isNotChange为true
            DevopsEnvCommandDTO oldDevopsEnvCommandE = devopsEnvCommandService.baseQuery(baseQuery(applicationInstanceDTO.getId()).getCommandId());
            String deployValue = baseQueryValueByInstanceId(applicationInstanceDTO.getId());
            if (applicationDeployVO.getAppVersionId().equals(oldDevopsEnvCommandE.getObjectVersionId()) && deployValue.equals(applicationDeployVO.getValues())) {
                applicationDeployVO.setIsNotChange(true);
            }
>>>>>>> [IMP]重构后端代码
        }
        }else{
        code=applicationInstanceE.getCode();
        //更新实例的时候校验gitops库文件是否存在,处理部署实例时，由于没有创gitops文件导致的部署失败
        resourceFileCheckHandler.check(devopsEnvironmentE,applicationDeployDTO.getAppInstanceId(),code,C7NHELM_RELEASE);

        //从未关联部署配置到关联部署配置，或者从一个部署配置关联另外一个部署配置，如果values是一样的，虽然getIsNotChange为false,但是此时也应该直接设置为isNotChange为true
        DevopsEnvCommandE oldDevopsEnvCommandE=devopsEnvCommandRepository.query(applicationInstanceRepository.selectById(applicationInstanceE.getId()).getCommandId());
        String deployValue=applicationInstanceRepository.queryValueByInstanceId(applicationInstanceE.getId());
        if(applicationDeployDTO.getAppVersionId().equals(oldDevopsEnvCommandE.getObjectVersionId())&&deployValue.equals(applicationDeployDTO.getValues())){
        applicationDeployDTO.setIsNotChange(true);
        }
        }
        <<<<<<<HEAD

<<<<<<< HEAD
//更新时候，如果isNotChange的值为true，则直接向agent发送更新指令，不走gitops,否则走操作gitops库文件逻辑

        if(applicationDeployDTO.getIsNotChange()){
                applicationInstanceE=restartDeploy(applicationDeployDTO,devopsEnvironmentE,applicationE,applicationVersionE,applicationInstanceE,devopsEnvCommandValueE,secretCode);
                }else{
                //存储数据
                if(applicationDeployDTO.getType().equals(CREATE)){
                // 默认为部署应用的环境和应用之间创建关联关系，如果不存在
                createEnvAppRelationShipIfNon(applicationDeployDTO.getAppId(),applicationDeployDTO.getEnvironmentId());

                =======

                //更新时候，如果isNotChange的值为true，则直接向agent发送更新指令，不走gitops,否则走操作gitops库文件逻辑

                if(applicationDeployDTO.getIsNotChange()){
                applicationInstanceE=restartDeploy(applicationDeployDTO,devopsEnvironmentE,applicationE,applicationVersionE,applicationInstanceE,devopsEnvCommandValueE,secretCode);
                }else{
                //存储数据
                if(applicationDeployDTO.getType().equals(CREATE)){
                >>>>>>>[IMP]重构后端代码
                applicationInstanceE.setCode(code);
                applicationInstanceE.setId(applicationInstanceRepository.create(applicationInstanceE).getId());
                devopsEnvCommandE.setObjectId(applicationInstanceE.getId());
                devopsEnvCommandE.initDevopsEnvCommandValueE(
                devopsEnvCommandValueRepository.create(devopsEnvCommandValueE).getId());
                applicationInstanceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
                applicationInstanceRepository.update(applicationInstanceE);
                }else{
                devopsEnvCommandE.setObjectId(applicationInstanceE.getId());
                devopsEnvCommandE.initDevopsEnvCommandValueE(
                devopsEnvCommandValueRepository.create(devopsEnvCommandValueE).getId());
                applicationInstanceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
                applicationInstanceRepository.update(applicationInstanceE);
                }
                }
                applicationDeployDTO.setAppInstanceId(applicationInstanceE.getId());
                applicationDeployDTO.setInstanceName(code);
                InstanceSagaDTO instanceSagaDTO=new InstanceSagaDTO(applicationE.getProjectE().getId(),userAttrE.getGitlabUserId(),secretCode);
                instanceSagaDTO.setApplicationE(applicationE);
                instanceSagaDTO.setApplicationVersionE(applicationVersionE);
                instanceSagaDTO.setApplicationDeployDTO(applicationDeployDTO);
                instanceSagaDTO.setDevopsEnvironmentE(devopsEnvironmentE);

                String input=gson.toJson(instanceSagaDTO);

                sagaClient.startSaga("devops-create-instance",new StartInstanceDTO(input,"env",devopsEnvironmentE.getId().toString(),ResourceLevel.PROJECT.value(),devopsEnvironmentE.getProjectE().getId()));

                return ConvertHelper.convert(applicationInstanceE,ApplicationInstanceVO.class);
        }
        <<<<<<<HEAD

/**
 * 为环境和应用创建关联关系如果不存在
 *
 * @param appId 应用id
 * @param envId 环境id
 */
private void createEnvAppRelationShipIfNon(Long appId,Long envId){
        DevopsEnvApplicationDO devopsEnvApplicationDO=new DevopsEnvApplicationDO();
        devopsEnvApplicationDO.setAppId(appId);
        devopsEnvApplicationDO.setEnvId(envId);
        envApplicationMapper.insertIgnore(devopsEnvApplicationDO);
        =======

@Override
public void createInstanceBySaga(InstanceSagaDTO
        instanceSagaDTO){

        try{
        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String filePath=clusterConnectionHandler.handDevopsEnvGitRepository(instanceSagaDTO.getProjectId(),instanceSagaDTO.getDevopsEnvironmentE().getCode(),instanceSagaDTO.getDevopsEnvironmentE().getEnvIdRsa());

        //在gitops库处理instance文件
        ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler=new ResourceConvertToYamlHandler<>();
        resourceConvertToYamlHandler.setType(getC7NHelmRelease(
        instanceSagaDTO.getApplicationDeployDTO().getInstanceName(),instanceSagaDTO.getApplicationVersionE().getRepository(),instanceSagaDTO.getApplicationE().getCode(),instanceSagaDTO.getApplicationVersionE().getVersion(),instanceSagaDTO.getApplicationDeployDTO().getValues(),instanceSagaDTO.getApplicationDeployDTO().getAppVersionId(),instanceSagaDTO.getSecretCode()));

        resourceConvertToYamlHandler.operationEnvGitlabFile(
        RELEASE_PREFIX+instanceSagaDTO.getApplicationDeployDTO().getInstanceName(),
        instanceSagaDTO.getDevopsEnvironmentE().getGitlabEnvProjectId().intValue(),
        instanceSagaDTO.getApplicationDeployDTO().getType(),
        instanceSagaDTO.getGitlabUserId(),
        instanceSagaDTO.getApplicationDeployDTO().getAppInstanceId(),C7NHELM_RELEASE,null,false,instanceSagaDTO.getDevopsEnvironmentE().getId(),filePath);
        }catch(Exception e){
        //有异常更新实例以及command的状态
        ApplicationInstanceE applicationInstanceE=applicationInstanceRepository.selectById(instanceSagaDTO.getApplicationDeployDTO().getAppInstanceId());
        applicationInstanceE.setStatus(CommandStatus.FAILED.getStatus());
        applicationInstanceRepository.update(applicationInstanceE);
        DevopsEnvCommandE devopsEnvCommandE=devopsEnvCommandRepository.query(applicationInstanceE.getCommandId());
        devopsEnvCommandE.setStatus(CommandStatus.FAILED.getStatus());
        devopsEnvCommandE.setError("create or update gitOps file failed!");
        devopsEnvCommandRepository.update(devopsEnvCommandE);
        }
        >>>>>>>[IMP]重构后端代码
        }

@Override
public void createInstanceBySaga(InstanceSagaDTO instanceSagaDTO){

        <<<<<<<HEAD
        try{
                //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
                String filePath=clusterConnectionHandler.handDevopsEnvGitRepository(instanceSagaDTO.getProjectId(),instanceSagaDTO.getDevopsEnvironmentE().getCode(),instanceSagaDTO.getDevopsEnvironmentE().getEnvIdRsa());

                //在gitops库处理instance文件
                ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler=new ResourceConvertToYamlHandler<>();
        resourceConvertToYamlHandler.setType(getC7NHelmRelease(
        instanceSagaDTO.getApplicationDeployDTO().getInstanceName(),instanceSagaDTO.getApplicationVersionE().getRepository(),instanceSagaDTO.getApplicationE().getCode(),instanceSagaDTO.getApplicationVersionE().getVersion(),instanceSagaDTO.getApplicationDeployDTO().getValues(),instanceSagaDTO.getApplicationDeployDTO().getAppVersionId(),instanceSagaDTO.getSecretCode()));

        resourceConvertToYamlHandler.operationEnvGitlabFile(
        RELEASE_PREFIX+instanceSagaDTO.getApplicationDeployDTO().getInstanceName(),
        instanceSagaDTO.getDevopsEnvironmentE().getGitlabEnvProjectId().intValue(),
        instanceSagaDTO.getApplicationDeployDTO().getType(),
        instanceSagaDTO.getGitlabUserId(),
        instanceSagaDTO.getApplicationDeployDTO().getAppInstanceId(),C7NHELM_RELEASE,null,false,instanceSagaDTO.getDevopsEnvironmentE().getId(),filePath);
        }catch(Exception e){
        //有异常更新实例以及command的状态
        ApplicationInstanceE applicationInstanceE=applicationInstanceRepository.selectById(instanceSagaDTO.getApplicationDeployDTO().getAppInstanceId());
        applicationInstanceE.setStatus(CommandStatus.FAILED.getStatus());
        applicationInstanceRepository.update(applicationInstanceE);
        DevopsEnvCommandE devopsEnvCommandE=devopsEnvCommandRepository.query(applicationInstanceE.getCommandId());
        devopsEnvCommandE.setStatus(CommandStatus.FAILED.getStatus());
        devopsEnvCommandE.setError("create or update gitOps file failed!");
        devopsEnvCommandRepository.update(devopsEnvCommandE);
        }
        }


private ApplicationInstanceE restartDeploy(ApplicationDeployDTO applicationDeployDTO,DevopsEnvironmentE devopsEnvironmentE,ApplicationE applicationE,ApplicationVersionE applicationVersionE,ApplicationInstanceE applicationInstanceE,DevopsEnvCommandValueE devopsEnvCommandValueE,String secretCode){
        DevopsEnvCommandE devopsEnvCommandE;
        applicationInstanceRepository.update(applicationInstanceE);
        applicationInstanceE=applicationInstanceRepository.selectById(applicationDeployDTO.getAppInstanceId());
        devopsEnvCommandE=devopsEnvCommandRepository.query(applicationInstanceE.getCommandId());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        deployService.deploy(applicationE,applicationVersionE,applicationInstanceE.getCode(),devopsEnvironmentE,
        devopsEnvCommandValueE.getValue(),devopsEnvCommandRepository.create(devopsEnvCommandE).getId(),secretCode);
        return applicationInstanceE;
=======
        //更新时候，如果isNotChange的值为true，则直接向agent发送更新指令，不走gitops,否则走操作gitops库文件逻辑
        if (applicationDeployVO.getIsNotChange()) {
            applicationInstanceDTO = restartDeploy(devopsEnvironmentDTO, applicationDTO, applicationVersionDTO, applicationInstanceDTO, devopsEnvCommandValueDTO, secretCode);
        } else {
            //存储数据
            if (applicationDeployVO.getType().equals(CREATE)) {
                applicationInstanceDTO.setCode(code);
                applicationInstanceDTO.setId(baseCreate(applicationInstanceDTO).getId());
                devopsEnvCommandDTO.setObjectId(applicationInstanceDTO.getId());
                devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
                applicationInstanceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
                baseUpdate(applicationInstanceDTO);
            } else {
                devopsEnvCommandDTO.setObjectId(applicationInstanceDTO.getId());
                devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
                applicationInstanceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
                baseUpdate(applicationInstanceDTO);
            }
        }
        applicationDeployVO.setAppInstanceId(applicationInstanceDTO.getId());
        applicationDeployVO.setInstanceName(code);
        InstanceSagaPayload instanceSagaPayload = new InstanceSagaPayload(applicationDTO.getProjectId(), userAttrDTO.getGitlabUserId(), secretCode);
        instanceSagaPayload.setApplicationDTO(applicationDTO);
        instanceSagaPayload.setApplicationVersionDTO(applicationVersionDTO);
        instanceSagaPayload.setApplicationDeployVO(applicationDeployVO);
        instanceSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_INSTANCE),
                builder -> builder
                        .withPayloadAndSerialize(instanceSagaPayload)
                        .withRefId(devopsEnvironmentDTO.getId().toString()));
        return ConvertUtils.convertObject(applicationInstanceDTO, ApplicationInstanceVO.class);
    }

    @Override
    public void createInstanceBySaga(InstanceSagaPayload
                                             instanceSagaPayload) {

        try {
            //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
            String filePath = clusterConnectionHandler.handDevopsEnvGitRepository(instanceSagaPayload.getProjectId(), instanceSagaPayload.getDevopsEnvironmentDTO().getCode(), instanceSagaPayload.getDevopsEnvironmentDTO().getEnvIdRsa());

            //在gitops库处理instance文件
            ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(getC7NHelmRelease(
                    instanceSagaPayload.getApplicationDeployVO().getInstanceName(), instanceSagaPayload.getApplicationVersionDTO().getRepository(), instanceSagaPayload.getApplicationDTO().getCode(), instanceSagaPayload.getApplicationVersionDTO().getVersion(), instanceSagaPayload.getApplicationDeployVO().getValues(), instanceSagaPayload.getApplicationDeployVO().getAppVersionId(), instanceSagaPayload.getSecretCode()));

            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    RELEASE_PREFIX + instanceSagaPayload.getApplicationDeployVO().getInstanceName(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId().intValue(),
                    instanceSagaPayload.getApplicationDeployVO().getType(),
                    instanceSagaPayload.getGitlabUserId(),
                    instanceSagaPayload.getApplicationDeployVO().getAppInstanceId(), C7NHELM_RELEASE, null, false, instanceSagaPayload.getDevopsEnvironmentE().getId(), filePath);
        } catch (Exception e) {
            //有异常更新实例以及command的状态
            ApplicationInstanceDTO applicationInstanceDTO = baseQuery(instanceSagaPayload.getApplicationDeployVO().getAppInstanceId());
            applicationInstanceDTO.setStatus(CommandStatus.FAILED.getStatus());
            baseUpdate(applicationInstanceDTO);
            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(applicationInstanceDTO.getCommandId());
            devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
            devopsEnvCommandDTO.setError("create or update gitOps file failed!");
            devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
>>>>>>> [IMP]重构后端代码
        }

<<<<<<< HEAD
private String getSecret(ApplicationE applicationE,String secretCode,String namespace,Long envId,Long clusterId){
        =======
private ApplicationInstanceE restartDeploy
        (ApplicationDeployDTO
        applicationDeployDTO,DevopsEnvironmentE
        devopsEnvironmentE,ApplicationE
        applicationE,ApplicationVersionE
        applicationVersionE,ApplicationInstanceE
        applicationInstanceE,DevopsEnvCommandValueE
        devopsEnvCommandValueE,String secretCode){
        DevopsEnvCommandE devopsEnvCommandE;
        applicationInstanceRepository.update(applicationInstanceE);
        applicationInstanceE=applicationInstanceRepository.selectById(applicationDeployDTO.getAppInstanceId());
        devopsEnvCommandE=devopsEnvCommandRepository.query(applicationInstanceE.getCommandId());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        deployService.deploy(applicationE,applicationVersionE,applicationInstanceE.getCode(),devopsEnvironmentE,
        devopsEnvCommandValueE.getValue(),devopsEnvCommandRepository.create(devopsEnvCommandE).getId(),secretCode);
        return applicationInstanceE;
        }

private String getSecret(ApplicationE applicationE,String
        secretCode,String namespace,Long envId,Long clusterId){
        >>>>>>>[IMP]重构后端代码
        //如果应用绑定了私有镜像库,则处理secret
<<<<<<< HEAD
        if(applicationE.getHarborConfigE()!=null){
        DevopsProjectConfigE devopsProjectConfigE=devopsProjectConfigRepository.queryByPrimaryKey(applicationE.getHarborConfigE().getId());
        if(devopsProjectConfigE.getConfig().getPrivate()!=null){
        DevopsRegistrySecretE devopsRegistrySecretE=devopsRegistrySecretRepository.queryByEnv(namespace,devopsProjectConfigE.getId());
        if(devopsRegistrySecretE==null){
        //当配置在当前环境下没有创建过secret.则新增secret信息，并通知k8s创建secret
        List<DevopsRegistrySecretE> devopsRegistrySecretES=devopsRegistrySecretRepository.listByConfig(devopsProjectConfigE.getId());
        if(devopsRegistrySecretES.isEmpty()){
        secretCode=String.format("%s%s%s%s","registry-secret-",devopsProjectConfigE.getId(),"-",GenerateUUID.generateUUID().substring(0,5));
        }else{
        secretCode=devopsRegistrySecretES.get(0).getSecretCode();
        }
        devopsRegistrySecretE=new DevopsRegistrySecretE(envId,devopsProjectConfigE.getId(),namespace,secretCode,gson.toJson(devopsProjectConfigE.getConfig()));
        devopsRegistrySecretRepository.create(devopsRegistrySecretE);
        deployService.operateSecret(clusterId,namespace,secretCode,devopsProjectConfigE.getConfig(),CREATE);
        }else{
        //判断如果某个配置有发生过修改，则需要修改secret信息，并通知k8s更新secret
        if(!devopsRegistrySecretE.getSecretDetail().equals(gson.toJson(devopsProjectConfigE.getConfig()))){
        devopsRegistrySecretE.setSecretDetail(gson.toJson(devopsProjectConfigE.getConfig()));
        devopsRegistrySecretRepository.update(devopsRegistrySecretE);
        deployService.operateSecret(clusterId,namespace,devopsRegistrySecretE.getSecretCode(),devopsProjectConfigE.getConfig(),UPDATE);
        }else{
        if(!devopsRegistrySecretE.getStatus()){
        deployService.operateSecret(clusterId,namespace,devopsRegistrySecretE.getSecretCode(),devopsProjectConfigE.getConfig(),UPDATE);
        }
        }
        secretCode=devopsRegistrySecretE.getSecretCode();
        }
        }
=======
        if (applicationE.getHarborConfigE() != null) {
            DevopsProjectConfigE devopsProjectConfigE = devopsProjectConfigRepository.baseQuery(applicationE.getHarborConfigE().getId());
            if (devopsProjectConfigE.getConfig().getPrivate() != null) {
                DevopsRegistrySecretE devopsRegistrySecretE = devopsRegistrySecretRepository.baseQueryByEnvAndId(namespace, devopsProjectConfigE.getId());
                if (devopsRegistrySecretE == null) {
=======
    private ApplicationInstanceDTO restartDeploy
            (DevopsEnvironmentDTO
                     devopsEnvironmentDTO, ApplicationDTO
                     applicationDTO, ApplicationVersionDTO
                     applicationVersionDTO, ApplicationInstanceDTO
                     applicationInstanceDTO, DevopsEnvCommandValueDTO
                     devopsEnvCommandValueDTO, String secretCode) {
        DevopsEnvCommandDTO devopsEnvCommandDTO;
        baseUpdate(applicationInstanceDTO);
        applicationInstanceDTO = baseQuery(applicationInstanceDTO.getId());
        devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(applicationInstanceDTO.getCommandId());
        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        agentCommandService.deploy(applicationDTO, applicationVersionDTO, applicationInstanceDTO.getCode(), devopsEnvironmentDTO,
                devopsEnvCommandValueDTO.getValue(), devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId(), secretCode);
        return applicationInstanceDTO;
    }

    private String getSecret(ApplicationDTO applicationDTO, String
            secretCode, DevopsEnvironmentDTO devopsEnvironmentDTO) {
        //如果应用绑定了私有镜像库,则处理secret
        if (applicationDTO.getHarborConfigId() != null) {
            DevopsProjectConfigDTO devopsProjectConfigDTO = devopsProjectConfigService.baseQuery(applicationDTO.getHarborConfigId());
            ProjectConfigVO projectConfigVO = gson.fromJson(devopsProjectConfigDTO.getConfig(), ProjectConfigVO.class);
            if (projectConfigVO.getPrivate() != null) {
                DevopsRegistrySecretDTO devopsRegistrySecretDTO = devopsRegistrySecretService.baseQueryByEnvAndId(devopsEnvironmentDTO.getCode(), devopsProjectConfigDTO.getId());
                if (devopsRegistrySecretDTO == null) {
>>>>>>> [IMP]重构后端代码
                    //当配置在当前环境下没有创建过secret.则新增secret信息，并通知k8s创建secret
                    List<DevopsRegistrySecretDTO> devopsRegistrySecretDTOS = devopsRegistrySecretService.baseListByConfig(devopsProjectConfigDTO.getId());
                    if (devopsRegistrySecretDTOS.isEmpty()) {
                        secretCode = String.format("%s%s%s%s", "registry-secret-", devopsProjectConfigDTO.getId(), "-", GenerateUUID.generateUUID().substring(0, 5));
                    } else {
                        secretCode = devopsRegistrySecretDTOS.get(0).getSecretCode();
                    }
                    devopsRegistrySecretDTO = new DevopsRegistrySecretDTO(devopsEnvironmentDTO.getId(), devopsProjectConfigDTO.getId(), devopsEnvironmentDTO.getCode(), secretCode, devopsProjectConfigDTO.getConfig());
                    devopsRegistrySecretService.baseCreate(devopsRegistrySecretDTO);
                    agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), secretCode, projectConfigVO, CREATE);
                } else {
                    //判断如果某个配置有发生过修改，则需要修改secret信息，并通知k8s更新secret
                    if (!devopsRegistrySecretDTO.getSecretDetail().equals(gson.toJson(devopsProjectConfigDTO.getConfig()))) {
                        devopsRegistrySecretDTO.setSecretDetail(gson.toJson(devopsProjectConfigDTO.getConfig()));
                        devopsRegistrySecretService.baseUpdate(devopsRegistrySecretDTO);
                        agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), devopsRegistrySecretDTO.getSecretCode(), projectConfigVO, UPDATE);
                    } else {
                        if (!devopsRegistrySecretDTO.getStatus()) {
                            agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), devopsRegistrySecretDTO.getSecretCode(), projectConfigVO, UPDATE);
                        }
                    }
                    secretCode = devopsRegistrySecretDTO.getSecretCode();
                }
            }
>>>>>>> [IMP]重构后端断码
        }
        return secretCode;
        }

<<<<<<< HEAD
@Override
<<<<<<<HEAD
public ApplicationInstanceVO createOrUpdateByGitOps(ApplicationDeployDTO applicationDeployDTO,Long userId){
        =======
public ApplicationInstanceVO createOrUpdateByGitOps
        (ApplicationDeployDTO applicationDeployDTO,Long userId){
        >>>>>>>[IMP]重构后端代码
        DevopsEnvironmentE devopsEnvironmentE=devopsEnvironmentRepository
        .queryById(applicationDeployDTO.getEnvironmentId());
=======
    @Override
    public ApplicationInstanceVO createOrUpdateByGitOps
            (ApplicationDeployVO applicationDeployVO, Long userId) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
<<<<<<< HEAD
                .baseQueryById(applicationDeployDTO.getEnvironmentId());
>>>>>>> [IMP] 重构Repository
=======
                .baseQueryById(applicationDeployVO.getEnvironmentId());
>>>>>>> [IMP]重构后端代码
        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        //校验values
        FileUtil.checkYamlFormat(applicationDeployVO.getValues());

        //初始化ApplicationInstanceE,DevopsEnvCommandE,DevopsEnvCommandValueE
<<<<<<< HEAD
        ApplicationInstanceE applicationInstanceE=initApplicationInstanceE(applicationDeployDTO);
        DevopsEnvCommandE devopsEnvCommandE=initDevopsEnvCommandE(applicationDeployDTO);
        DevopsEnvCommandValueE devopsEnvCommandValueE=initDevopsEnvCommandValueE(applicationDeployDTO);

        //实例相关对象数据库操作
        if(applicationDeployDTO.getType().equals(CREATE)){
        applicationInstanceE.setCode(applicationDeployDTO.getInstanceName());
        applicationInstanceE.setId(applicationInstanceRepository.create(applicationInstanceE).getId());
        }else{
        applicationInstanceRepository.update(applicationInstanceE);
=======
        ApplicationInstanceE applicationInstanceE = initApplicationInstanceDTO(applicationDeployVO);
        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandDTO(applicationDeployVO);
        DevopsEnvCommandValueE devopsEnvCommandValueE = initDevopsEnvCommandValueDTO(applicationDeployVO);

        //实例相关对象数据库操作
        if (applicationDeployVO.getType().equals(CREATE)) {
            applicationInstanceE.setCode(applicationDeployVO.getInstanceName());
            applicationInstanceE.setId(applicationInstanceRepository.create(applicationInstanceE).getId());
        } else {
            applicationInstanceRepository.update(applicationInstanceE);
>>>>>>> [IMP]重构后端代码
        }
        devopsEnvCommandE.setCreatedBy(userId);
        devopsEnvCommandE.setObjectId(applicationInstanceE.getId());
        devopsEnvCommandE.initDevopsEnvCommandValueE(
        devopsEnvCommandValueRepository.create(devopsEnvCommandValueE).getId());
        applicationInstanceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        applicationInstanceRepository.update(applicationInstanceE);
        return ConvertHelper.convert(applicationInstanceE,ApplicationInstanceVO.class);
        }

<<<<<<< HEAD
        <<<<<<<HEAD
private ApplicationInstanceE initApplicationInstanceE(ApplicationDeployDTO applicationDeployDTO){
        =======
private ApplicationInstanceE initApplicationInstanceE
        (ApplicationDeployDTO applicationDeployDTO){
        >>>>>>>[IMP]重构后端代码

        ApplicationInstanceE applicationInstanceE=new ApplicationInstanceE();
        applicationInstanceE.initApplicationEById(applicationDeployDTO.getAppId());
        applicationInstanceE.initDevopsEnvironmentEById(applicationDeployDTO.getEnvironmentId());
        applicationInstanceE.setStatus(InstanceStatus.OPERATIING.getStatus());
        applicationInstanceE.setValueId(applicationDeployDTO.getValueId());
        if(applicationDeployDTO.getType().equals(UPDATE)){
        ApplicationInstanceE newApplicationInstanceE=applicationInstanceRepository.selectById(
        applicationDeployDTO.getAppInstanceId());
        applicationInstanceE.setCode(newApplicationInstanceE.getCode());
        applicationInstanceE.setId(applicationDeployDTO.getAppInstanceId());
        }
        return applicationInstanceE;
        }

        <<<<<<<HEAD
private DevopsEnvCommandE initDevopsEnvCommandE(ApplicationDeployDTO applicationDeployDTO){
        =======
private DevopsEnvCommandE initDevopsEnvCommandE
        (ApplicationDeployDTO applicationDeployDTO){
        >>>>>>>[IMP]重构后端代码
        DevopsEnvCommandE devopsEnvCommandE=new DevopsEnvCommandE();
        switch(applicationDeployDTO.getType()){
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
        devopsEnvCommandE.setObjectVersionId(applicationDeployDTO.getAppVersionId());
        devopsEnvCommandE.setObject(ObjectType.INSTANCE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandE;
        }

        <<<<<<<HEAD
private DevopsEnvCommandValueE initDevopsEnvCommandValueE(ApplicationDeployDTO applicationDeployDTO){
        =======
private DevopsEnvCommandValueE initDevopsEnvCommandValueE
        (ApplicationDeployDTO applicationDeployDTO){
        >>>>>>>[IMP]重构后端代码
        DevopsEnvCommandValueE devopsEnvCommandValueE=new DevopsEnvCommandValueE();
        devopsEnvCommandValueE.setValue(applicationDeployDTO.getValues());
        return devopsEnvCommandValueE;
        }

@Override
<<<<<<<HEAD
public List<AppInstanceCodeDTO> listByOptions(Long projectId,Long appId,Long appVersionId,Long envId){
        =======
public List<AppInstanceCodeDTO> listByOptions(Long
        projectId,Long appId,Long appVersionId,Long envId){
        >>>>>>>[IMP]重构后端代码
        return ConvertHelper.convertList(applicationInstanceRepository
        .listByOptions(projectId,appId,appVersionId,envId),AppInstanceCodeDTO.class);
        }

@Override
<<<<<<<HEAD
public List<AppInstanceCodeDTO> listByAppIdAndEnvId(Long projectId,Long appId,Long envId){
        =======
public List<AppInstanceCodeDTO> listByAppIdAndEnvId(Long
        projectId,Long appId,Long envId){
        >>>>>>>[IMP]重构后端代码
        return ConvertHelper.convertList(applicationInstanceRepository
        .listByAppIdAndEnvId(projectId,appId,envId),AppInstanceCodeDTO.class);
        }

@Override
<<<<<<<HEAD
public List<AppInstanceCodeDTO> getByAppIdAndEnvId(Long projectId,Long appId,Long envId){
        =======
public List<AppInstanceCodeDTO> getByAppIdAndEnvId(Long
        projectId,Long appId,Long envId){
        >>>>>>>[IMP]重构后端代码
        return ConvertHelper.convertList(applicationInstanceRepository
        .getByAppIdAndEnvId(projectId,appId,envId),AppInstanceCodeDTO.class);
        }

@Override
public void instanceStop(Long instanceId){
        ApplicationInstanceE instanceE=applicationInstanceRepository.selectById(instanceId);

<<<<<<< HEAD
        DevopsEnvironmentE devopsEnvironmentE=devopsEnvironmentRepository.queryById(instanceE.getDevopsEnvironmentE().getId());
=======
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(instanceE.getDevopsEnvironmentE().getId());
>>>>>>> [IMP] 重构Repository

<<<<<<< HEAD
        UserAttrE userAttrE=userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
=======
        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
>>>>>>> [REF] refactor UserAttrRepository

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE,userAttrE);
        if(!instanceE.getStatus().equals(InstanceStatus.RUNNING.getStatus())){
        throw new CommonException("error.instance.notRunning");
        }
        DevopsEnvCommandE devopsEnvCommandE=devopsEnvCommandRepository
        .queryByObject(ObjectType.INSTANCE.getType(),instanceId);
        devopsEnvCommandE.setCommandType(CommandType.STOP.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE=devopsEnvCommandRepository.create(devopsEnvCommandE);
        String namespace=getNameSpace(instanceE.getDevopsEnvironmentE().getId());
        String releaseName=updateInstanceStatus(instanceId,InstanceStatus.OPERATIING.getStatus());
        Map<String, String> stopMap=new HashMap<>();
        stopMap.put(RELEASE_NAME,releaseName);
        String payload=gson.toJson(stopMap);
        Long envId=instanceE.getDevopsEnvironmentE().getId();
        sentInstance(payload,releaseName,HelmType.HELM_RELEASE_STOP.toValue(),
        namespace,devopsEnvCommandE.getId(),envId,devopsEnvironmentE.getClusterE().getId());
        }

@Override
public void instanceStart(Long instanceId){
        ApplicationInstanceE instanceE=applicationInstanceRepository.selectById(instanceId);

<<<<<<< HEAD
        DevopsEnvironmentE devopsEnvironmentE=devopsEnvironmentRepository.queryById(instanceE.getDevopsEnvironmentE().getId());
=======
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(instanceE.getDevopsEnvironmentE().getId());
>>>>>>> [IMP] 重构Repository

<<<<<<< HEAD
        UserAttrE userAttrE=userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
=======
        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
>>>>>>> [REF] refactor UserAttrRepository

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE,userAttrE);

        if(!instanceE.getStatus().equals(InstanceStatus.STOPPED.getStatus())){
        throw new CommonException("error.instance.notStop");
        }
        DevopsEnvCommandE devopsEnvCommandE=devopsEnvCommandRepository
        .queryByObject(ObjectType.INSTANCE.getType(),instanceId);
        devopsEnvCommandE.setCommandType(CommandType.RESTART.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE=devopsEnvCommandRepository.create(devopsEnvCommandE);
        String namespace=getNameSpace(instanceE.getDevopsEnvironmentE().getId());
        String releaseName=updateInstanceStatus(instanceId,InstanceStatus.OPERATIING.getStatus());
        Map<String, String> stopMap=new HashMap<>();
        stopMap.put(RELEASE_NAME,releaseName);
        String payload=gson.toJson(stopMap);
        Long envId=instanceE.getDevopsEnvironmentE().getId();
        sentInstance(payload,releaseName,HelmType.HELM_RELEASE_START.toValue(),
        namespace,devopsEnvCommandE.getId(),envId,devopsEnvironmentE.getClusterE().getId());
        }
=======
    private ApplicationInstanceDTO initApplicationInstanceDTO
            (ApplicationDeployVO applicationDeployVO) {

        ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO();
        applicationInstanceDTO.setAppId(applicationDeployVO.getAppId());
        applicationInstanceDTO.setEnvId(applicationDeployVO.getEnvironmentId());
        applicationInstanceDTO.setStatus(InstanceStatus.OPERATIING.getStatus());
        applicationDeployVO.setValueId(applicationDeployVO.getValueId());
        if (applicationDeployVO.getType().equals(UPDATE)) {
            ApplicationInstanceDTO oldApplicationInstanceDTO = baseQuery(
                    applicationDeployVO.getAppInstanceId());
            applicationInstanceDTO.setCode(oldApplicationInstanceDTO.getCode());
            applicationInstanceDTO.setId(applicationDeployVO.getAppInstanceId());
        }
        return applicationInstanceDTO;
    }

    private DevopsEnvCommandDTO initDevopsEnvCommandDTO
            (ApplicationDeployVO applicationDeployVO) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        switch (applicationDeployVO.getType()) {
            case CREATE:
                devopsEnvCommandDTO.setCommandType(CommandType.CREATE.getType());
                break;
            case UPDATE:
                devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
                break;
            default:
                devopsEnvCommandDTO.setCommandType(CommandType.DELETE.getType());
                break;
        }
        devopsEnvCommandDTO.setObjectVersionId(applicationDeployVO.getAppVersionId());
        devopsEnvCommandDTO.setObject(ObjectType.INSTANCE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }

    private DevopsEnvCommandValueDTO initDevopsEnvCommandValueDTO
            (ApplicationDeployVO applicationDeployVO) {
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = new DevopsEnvCommandValueDTO();
        devopsEnvCommandValueDTO.setValue(applicationDeployVO.getValues());
        return devopsEnvCommandValueDTO;
    }

    @Override
    public List<RunningInstanceVO> listRunningInstance(Long projectId, Long appId, Long appVersionId, Long envId) {
        return ConvertUtils.convertList(baseListByOptions(projectId, appId, appVersionId, envId), RunningInstanceVO.class);
    }

    @Override
    public List<RunningInstanceVO> listByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return ConvertUtils.convertList(baseListByAppIdAndEnvId(projectId, appId, envId), RunningInstanceVO.class);
    }

    @Override
    public List<RunningInstanceVO> getByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return ConvertHelper.convertList(
                .baseListByAppIdAndEnvId(projectId, appId, envId),RunningInstanceVO.class);
    }

    @Override
    public void stopInstance(Long instanceId) {

        ApplicationInstanceDTO applicationInstanceDTO = baseQuery(instanceId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(applicationInstanceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        if (!applicationInstanceDTO.getStatus().equals(InstanceStatus.RUNNING.getStatus())) {
            throw new CommonException("error.instance.not.running");
        }

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                .baseQueryByObject(ObjectType.INSTANCE.getType(), instanceId);
        devopsEnvCommandDTO.setCommandType(CommandType.STOP.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
        updateInstanceStatus(instanceId, devopsEnvCommandDTO.getId(), InstanceStatus.OPERATIING.getStatus());


        //发送停止实例的command
        Map<String, String> stopMap = new HashMap<>();
        stopMap.put(RELEASE_NAME, applicationInstanceDTO.getCode());
        String payload = gson.toJson(stopMap);
        instanceCommand(payload, applicationInstanceDTO.getCode(), HelmType.HELM_RELEASE_STOP.toValue(),
                devopsEnvironmentDTO.getCode(), devopsEnvCommandDTO.getId(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getClusterId());
    }

    @Override
    public void startInstance(Long instanceId) {
        ApplicationInstanceDTO applicationInstanceDTO = baseQuery(instanceId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(applicationInstanceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        if (!applicationInstanceDTO.getStatus().equals(InstanceStatus.STOPPED.getStatus())) {
            throw new CommonException("error.instance.not.stop");
        }

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                .baseQueryByObject(ObjectType.INSTANCE.getType(), instanceId);
        devopsEnvCommandDTO.setCommandType(CommandType.RESTART.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
        updateInstanceStatus(instanceId, devopsEnvCommandDTO.getId(), InstanceStatus.OPERATIING.getStatus());


        //发送重启实例的command
        Map<String, String> stopMap = new HashMap<>();
        stopMap.put(RELEASE_NAME, applicationInstanceDTO.getCode());
        String payload = gson.toJson(stopMap);
        instanceCommand(payload, applicationInstanceDTO.getCode(), HelmType.HELM_RELEASE_START.toValue(),
                devopsEnvironmentDTO.getCode(), devopsEnvCommandDTO.getId(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getClusterId());
    }
>>>>>>> [IMP]重构后端代码

        <<<<<<<HEAD
@Override
public void instanceReStart(Long instanceId){
        ApplicationInstanceE instanceE=applicationInstanceRepository.selectById(instanceId);

<<<<<<< HEAD
        DevopsEnvironmentE devopsEnvironmentE=devopsEnvironmentRepository.queryById(instanceE.getDevopsEnvironmentE().getId());

        UserAttrE userAttrE=userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE,userAttrE);

        DevopsEnvCommandE devopsEnvCommandE=devopsEnvCommandRepository.query(instanceE.getCommandId());
        ApplicationE applicationE=applicationRepository.query(instanceE.getApplicationE().getId());
        ApplicationVersionE applicationVersionE=applicationVersionRepository
        .query(devopsEnvCommandE.getObjectVersionId());

        String value=applicationInstanceRepository.queryValueByInstanceId(instanceId);
        instanceE.setStatus(InstanceStatus.OPERATIING.getStatus());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        Long commandId=devopsEnvCommandRepository.create(devopsEnvCommandE).getId();
        instanceE.setCommandId(commandId);
        applicationInstanceRepository.update(instanceE);
        String secretCode=null;
        secretCode=getSecret(applicationE,secretCode,devopsEnvironmentE.getCode(),devopsEnvironmentE.getId(),devopsEnvironmentE.getClusterE().getId());
        deployService.deploy(applicationE,applicationVersionE,instanceE.getCode(),devopsEnvironmentE,value,commandId,secretCode);
        }

@Override
@Transactional(rollbackFor = Exception.class)
public void instanceDelete(Long instanceId){
        ApplicationInstanceE instanceE=applicationInstanceRepository.selectById(instanceId);

<<<<<<< HEAD
        DevopsEnvironmentE devopsEnvironmentE=devopsEnvironmentRepository.queryById(instanceE.getDevopsEnvironmentE().getId());

        UserAttrE userAttrE=userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
=======
        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
>>>>>>> [REF] refactor UserAttrRepository

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE,userAttrE);

        =======

<<<<<<< HEAD
@Override
public void instanceReStart(Long instanceId){
        ApplicationInstanceE instanceE=applicationInstanceRepository.selectById(instanceId);
=======
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(instanceE.getDevopsEnvironmentE().getId());
>>>>>>> [IMP] 重构Repository

        DevopsEnvironmentE devopsEnvironmentE=devopsEnvironmentRepository.queryById(instanceE.getDevopsEnvironmentE().getId());

        UserAttrE userAttrE=userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE,userAttrE);

        DevopsEnvCommandE devopsEnvCommandE=devopsEnvCommandRepository.query(instanceE.getCommandId());
        ApplicationE applicationE=applicationRepository.query(instanceE.getApplicationE().getId());
        ApplicationVersionE applicationVersionE=applicationVersionRepository
        .baseQuery(devopsEnvCommandE.getObjectVersionId());

        String value=applicationInstanceRepository.queryValueByInstanceId(instanceId);
        instanceE.setStatus(InstanceStatus.OPERATIING.getStatus());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        Long commandId=devopsEnvCommandRepository.create(devopsEnvCommandE).getId();
        instanceE.setCommandId(commandId);
        applicationInstanceRepository.update(instanceE);
        String secretCode=null;
        secretCode=getSecret(applicationE,secretCode,devopsEnvironmentE.getCode(),devopsEnvironmentE.getId(),devopsEnvironmentE.getClusterE().getId());
        deployService.deploy(applicationE,applicationVersionE,instanceE.getCode(),devopsEnvironmentE,value,commandId,secretCode);
        }
=======
    @Override
    public void restartInstance(Long instanceId) {
        ApplicationInstanceDTO applicationInstanceDTO = baseQuery(instanceId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(applicationInstanceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(applicationInstanceDTO.getCommandId());
        ApplicationDTO applicationDTO = applicationService.baseQuery(applicationInstanceDTO.getAppId());
        ApplicationVersionDTO applicationVersionDTO = applicationVersionService
                .baseQuery(devopsEnvCommandDTO.getObjectVersionId());

        String value = baseQueryValueByInstanceId(instanceId);

        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

        updateInstanceStatus(instanceId, devopsEnvCommandDTO.getId(),InstanceStatus.OPERATIING.getStatus());

        //获取授权secret
        String secretCode = getSecret(applicationDTO, null, devopsEnvironmentDTO);

        //发送实例重新部署的command
        agentCommandService.deploy(applicationDTO, applicationVersionDTO, applicationInstanceDTO.getCode(), devopsEnvironmentDTO, value, devopsEnvCommandDTO.getId(), secretCode);
    }
>>>>>>> [IMP]重构后端代码


@Override
@Transactional(rollbackFor = Exception.class)

<<<<<<< HEAD
public void instanceDelete(Long instanceId){
        ApplicationInstanceE instanceE=applicationInstanceRepository.selectById(instanceId);

<<<<<<< HEAD
        DevopsEnvironmentE devopsEnvironmentE=devopsEnvironmentRepository.queryById(instanceE.getDevopsEnvironmentE().getId());
=======
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(instanceE.getDevopsEnvironmentE().getId());
>>>>>>> [IMP] 重构Repository

<<<<<<< HEAD
        UserAttrE userAttrE=userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
=======
        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
>>>>>>> [REF] refactor UserAttrRepository

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE,userAttrE);

        >>>>>>>[IMP]重构后端代码
        DevopsEnvCommandE devopsEnvCommandE;
        if(instanceE.getCommandId()==null){
        devopsEnvCommandE=devopsEnvCommandRepository
        .queryByObject(ObjectType.INSTANCE.getType(),instanceE.getId());
        }else{
        devopsEnvCommandE=devopsEnvCommandRepository
        .query(instanceE.getCommandId());
        }
        devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandE.setId(null);
=======
    public void deleteInstance(Long instanceId) {

        ApplicationInstanceDTO applicationInstanceDTO = baseQuery(instanceId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(applicationInstanceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(applicationInstanceDTO.getCommandId());
        devopsEnvCommandDTO.setCommandType(CommandType.DELETE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
>>>>>>> [IMP]重构后端代码

        updateInstanceStatus(instanceId, devopsEnvCommandDTO.getId(),InstanceStatus.OPERATIING.getStatus());

<<<<<<< HEAD
        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path=clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentE.getProjectE().getId(),devopsEnvironmentE.getCode(),devopsEnvironmentE.getEnvIdRsa());

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
<<<<<<< HEAD
        DevopsEnvFileResourceE devopsEnvFileResourceE=devopsEnvFileResourceRepository
        .queryByEnvIdAndResource(devopsEnvironmentE.getId(),instanceId,C7NHELM_RELEASE);
        if(devopsEnvFileResourceE==null){
        applicationInstanceRepository.deleteInstanceRelInfo(instanceId);
        if(gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),"master",
        RELEASE_PREFIX+instanceE.getCode()+YAML_SUFFIX)){
        gitlabRepository.deleteFile(
        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
        RELEASE_PREFIX+instanceE.getCode()+YAML_SUFFIX,
        "DELETE FILE",
        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        }
        return;
        }else{
        if(!gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),"master",
        devopsEnvFileResourceE.getFilePath())){
        applicationInstanceRepository.deleteInstanceRelInfo(instanceId);
        devopsEnvFileResourceRepository.deleteFileResource(devopsEnvFileResourceE.getId());
        return;
        }
        }
        List<DevopsEnvFileResourceE> devopsEnvFileResourceES=devopsEnvFileResourceRepository
        .queryByEnvIdAndPath(devopsEnvironmentE.getId(),devopsEnvFileResourceE.getFilePath());
        if(devopsEnvFileResourceES.size()==1){
        if(gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),"master",
        devopsEnvFileResourceE.getFilePath())){
        gitlabRepository.deleteFile(
        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
        devopsEnvFileResourceE.getFilePath(),
        "DELETE FILE",
        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        }
        }else{
        ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler=new ResourceConvertToYamlHandler<>();
        C7nHelmRelease c7nHelmRelease=new C7nHelmRelease();
        Metadata metadata=new Metadata();
        metadata.setName(instanceE.getCode());
        c7nHelmRelease.setMetadata(metadata);
        resourceConvertToYamlHandler.setType(c7nHelmRelease);
        Integer projectId=TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
        resourceConvertToYamlHandler.operationEnvGitlabFile(
        RELEASE_PREFIX+instanceE.getCode(),
        projectId,
        "delete",
        userAttrE.getGitlabUserId(),
        instanceE.getId(),C7NHELM_RELEASE,null,false,devopsEnvironmentE.getId(),path);
=======
        DevopsEnvFileResourceVO devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentE.getId(), instanceId, C7NHELM_RELEASE);
        if (devopsEnvFileResourceE == null) {
            applicationInstanceRepository.deleteInstanceRelInfo(instanceId);
            if (gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    RELEASE_PREFIX + instanceE.getCode() + YAML_SUFFIX)) {
                gitlabRepository.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                        RELEASE_PREFIX + instanceE.getCode() + YAML_SUFFIX,
=======
        pipelineAppDeployService.baseUpdateWithInstanceId(instanceId);


        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getEnvIdRsa());

        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(), instanceId, C7NHELM_RELEASE);

        //如果文件对象对应关系不存在，证明没有部署成功，删掉gitops文件,删掉资源
        if (devopsEnvFileResourceDTO == null) {
            baseDelete(instanceId);
            baseDeleteInstanceRelInfo(instanceId);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    RELEASE_PREFIX + applicationInstanceDTO.getCode() + YAML_SUFFIX)) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        RELEASE_PREFIX + applicationInstanceDTO.getCode() + YAML_SUFFIX,
>>>>>>> [IMP]重构后端代码
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
            return;
        } else {
            //如果文件对象对应关系存在，但是gitops文件不存在，也直接删掉资源
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceDTO.getFilePath())) {
                baseDelete(instanceId);
                baseDeleteInstanceRelInfo(instanceId);
                devopsEnvFileResourceService.baseDelete(devopsEnvFileResourceDTO.getId());
                return;
            }
        }

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        List<DevopsEnvFileResourceDTO> devopsEnvFileResourceES = devopsEnvFileResourceService
                .baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), devopsEnvFileResourceDTO.getFilePath());
        if (devopsEnvFileResourceES.size() == 1) {
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceDTO.getFilePath())) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        devopsEnvFileResourceDTO.getFilePath(),
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
        } else {
            ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
            Metadata metadata = new Metadata();
            metadata.setName(applicationInstanceDTO.getCode());
            c7nHelmRelease.setMetadata(metadata);
            resourceConvertToYamlHandler.setType(c7nHelmRelease);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    RELEASE_PREFIX + applicationInstanceDTO.getCode(),
                    projectId,
                    "delete",
<<<<<<< HEAD
                    userAttrE.getGitlabUserId(),
                    instanceE.getId(), C7NHELM_RELEASE, null, false, devopsEnvironmentE.getId(), path);
>>>>>>> [IMP] 重构Repository
        }
<<<<<<< HEAD
        appDeployRepository.updateInstanceId(instanceId);
        }
=======
        appDeployRepository.baseUpdateWithInstanceId(instanceId);
=======
                    userAttrDTO.getGitlabUserId(),
                    applicationInstanceDTO.getId(), C7NHELM_RELEASE, null, false, devopsEnvironmentDTO.getId(), path);
        }
>>>>>>> [IMP]重构后端代码
    }

>>>>>>> [IMP] 重构部分Repository

<<<<<<< HEAD
        <<<<<<<HEAD
@Override
public ReplaceResult previewValues(ReplaceResult previewReplaceResult,Long appVersionId){
        String versionValue=applicationVersionRepository.queryValue(appVersionId);
        =======
=======
    @Override
    public InstanceValueVO queryPreviewValues(InstanceValueVO
                                                      previewInstanceValueVO, Long appVersionId) {
        String versionValue = applicationVersionService.baseQueryValue(appVersionId);
        try {
            FileUtil.checkYamlFormat(previewInstanceValueVO.getYaml());
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
        return getReplaceResult(versionValue, previewInstanceValueVO.getYaml());
    }
>>>>>>> [IMP]重构后端代码

@Override
public ReplaceResult previewValues(ReplaceResult
        previewReplaceResult,Long appVersionId){
        String versionValue=applicationVersionRepository.baseQueryValue(appVersionId);
        >>>>>>>[IMP]重构后端代码
        try{
        FileUtil.checkYamlFormat(previewReplaceResult.getYaml());
        }catch(Exception e){
        throw new CommonException(e.getMessage(),e);
        }
        <<<<<<<HEAD
        ReplaceResult replaceResult=getReplaceResult(versionValue,previewReplaceResult.getYaml());
                replaceResult.setTotalLine(FileUtil.getFileTotalLine(replaceResult.getYaml())+1);
                return replaceResult;
                =======
                return getReplaceResult(versionValue,previewReplaceResult.getYaml());
                >>>>>>>[IMP]重构后端代码
                }

@Override
public void instanceDeleteByGitOps(Long instanceId){
        ApplicationInstanceE instanceE=applicationInstanceRepository.selectById(instanceId);

<<<<<<< HEAD
        DevopsEnvironmentE devopsEnvironmentE=devopsEnvironmentRepository
        .queryById(instanceE.getDevopsEnvironmentE().getId());
=======
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .baseQueryById(instanceE.getDevopsEnvironmentE().getId());
>>>>>>> [IMP] 重构Repository

        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        applicationInstanceRepository.deleteInstanceRelInfo(instanceId);
        applicationInstanceRepository.deleteById(instanceId);
        }


@Override
<<<<<<<HEAD
public void checkName(String instanceName,Long envId){
        =======
public void checkName(String instanceName,Long
        envId){
        >>>>>>>[IMP]重构后端代码
        AppInstanceValidator.checkName(instanceName);
        ApplicationInstanceE applicationInstanceE=new ApplicationInstanceE();
        applicationInstanceE.setCode(instanceName);
<<<<<<< HEAD
        applicationInstanceRepository.checkName(instanceName,envId);
        appDeployRepository.checkName(instanceName,envId);
        }
=======
        applicationInstanceRepository.checkName(instanceName, envId);
        appDeployRepository.baseCheckName(instanceName, envId);
    }
>>>>>>> [IMP] 重构部分Repository

<<<<<<< HEAD
<<<<<<< HEAD
private String getNameSpace(Long envId){
        return devopsEnvironmentRepository.queryById(envId).getCode();
        }
=======
    private String getNameSpace(Long envId) {
        return devopsEnvironmentRepository.baseQueryById(envId).getCode();
    }
>>>>>>> [IMP] 重构Repository

        <<<<<<<HEAD
private String updateInstanceStatus(Long instanceId,String status){
        =======
private String updateInstanceStatus(Long
        instanceId,String status){
        >>>>>>>[IMP]重构后端代码
        ApplicationInstanceE instanceE=applicationInstanceRepository.selectById(instanceId);
        instanceE.setStatus(status);
        applicationInstanceRepository.update(instanceE);
        return instanceE.getCode();
        }

        <<<<<<<HEAD
private void sentInstance(String payload,String name,String type,String namespace,Long commandId,Long envId,
        =======
private void sentInstance(String payload,String
        name,String type,String namespace,Long
        commandId,Long envId,
        >>>>>>>[IMP]重构后端代码
        Long clusterId){
        Msg msg=new Msg();
        msg.setKey("cluster:"+clusterId+".env:"+namespace+".envId:"+envId+".release:"+name);
=======

    private void updateInstanceStatus(Long instanceId, Long commandId, String status) {
        ApplicationInstanceDTO instanceDTO = baseQuery(instanceId);
        instanceDTO.setStatus(status);
        instanceDTO.setCommandId(commandId);
        baseUpdate(instanceDTO);
    }

    private void instanceCommand(String payload, String name,
                                 String type, String namespace,
                                 Long commandId, Long envId,
                                 Long clusterId) {
        Msg msg = new Msg();
        msg.setKey("cluster:" + clusterId + ".env:" + namespace + ".envId:" + envId + ".release:" + name);
>>>>>>> [IMP]重构后端代码
        msg.setType(type);
        msg.setPayload(payload);
        msg.setCommandId(commandId);
        commandSender.sendMsg(msg);
        }

<<<<<<< HEAD
        <<<<<<<HEAD
private List<ErrorLineDTO> getErrorLine(String value){
        =======
private List<ErrorLineDTO> getErrorLine(String
        value){
        >>>>>>>[IMP]重构后端代码
        List<ErrorLineDTO> errorLines=new ArrayList<>();
        List<Long> lineNumbers=new ArrayList<>();
        String[]errorMsg=value.split("\\^");
        for(int i=0;i<value.length();i++){
        int j;
        for(j=i;j<value.length();j++){
        if(value.substring(i,j).equals("line")){
        lineNumbers.add(TypeUtil.objToLong(value.substring(j,value.indexOf(',',j)).trim()));
        }
        }
        }
        for(int i=0;i<lineNumbers.size();i++){
        ErrorLineDTO errorLineDTO=new ErrorLineDTO();
        errorLineDTO.setLineNumber(lineNumbers.get(i));
        errorLineDTO.setErrorMsg(errorMsg[i]);
        errorLines.add(errorLineDTO);
=======
    private List<ErrorLineVO> getErrorLine(String
                                                   value) {
        List<ErrorLineVO> errorLines = new ArrayList<>();
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
            ErrorLineVO errorLineVO = new ErrorLineVO();
            errorLineVO.setLineNumber(lineNumbers.get(i));
            errorLineVO.setErrorMsg(errorMsg[i]);
            errorLines.add(errorLineVO);
>>>>>>> [IMP]重构后端代码
        }
        return errorLines;
        }

<<<<<<< HEAD
        <<<<<<<HEAD
private void setInstanceConnect(List<ApplicationInstanceVO> applicationInstanceVOS,
        List<Long> connectedEnvList,List<Long> updatedEnvList){
        =======
private void setInstanceConnect
        (List<ApplicationInstanceVO> applicationInstanceVOS,
        List<Long> connectedEnvList,List<Long> updatedEnvList){
        >>>>>>>[IMP]重构后端代码
        applicationInstanceVOS.forEach(applicationInstanceVO->
        {
        DevopsEnvironmentE devopsEnvironmentE=devopsEnvironmentRepository
        .queryById(applicationInstanceVO.getEnvId());
        if(connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
        &&updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())){
        applicationInstanceVO.setConnect(true);
        }
        }
=======
    private void setInstanceConnect
            (List<ApplicationInstanceVO> applicationInstanceVOS,
             List<Long> connectedEnvList, List<Long> updatedEnvList) {
        applicationInstanceVOS.forEach(applicationInstanceVO ->
                {
                    DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                            .baseQueryById(applicationInstanceVO.getEnvId());
                    if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                            && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                        applicationInstanceVO.setConnect(true);
                    }
                }
>>>>>>> [IMP] 重构Repository
        );
        }
        <<<<<<<HEAD

private C7nHelmRelease getC7NHelmRelease(String code,String repository,String appCode,String version,String deployValue,Long deployVersionId,
        String secretName){
        C7nHelmRelease c7nHelmRelease=new C7nHelmRelease();
        c7nHelmRelease.getMetadata().setName(code);
        c7nHelmRelease.getSpec().setRepoUrl(repository);
        c7nHelmRelease.getSpec().setChartName(appCode);
        c7nHelmRelease.getSpec().setChartVersion(version);
        if(secretName!=null){
        c7nHelmRelease.getSpec().setImagePullSecrets(Arrays.asList(new ImagePullSecret(secretName)));
        =======

private C7nHelmRelease getC7NHelmRelease(String
        code,String repository,String appCode,String
        version,String deployValue,Long
        deployVersionId,
        String secretName){
        C7nHelmRelease c7nHelmRelease=new C7nHelmRelease();
        c7nHelmRelease.getMetadata().setName(code);
        c7nHelmRelease.getSpec().setRepoUrl(repository);
        c7nHelmRelease.getSpec().setChartName(appCode);
        c7nHelmRelease.getSpec().setChartVersion(version);
        if(secretName!=null){
        c7nHelmRelease.getSpec().setImagePullSecrets(Arrays.asList(new ImagePullSecret(secretName)));
        }
        c7nHelmRelease.getSpec().setValues(
        getReplaceResult(applicationVersionRepository.queryValue(deployVersionId),
        deployValue).getDeltaYaml().trim());
        return c7nHelmRelease;
        }


private String getDeployTime(Long diff){
        float num=(float)diff/(60*1000);
        DecimalFormat df=new DecimalFormat("0.00");
        return df.format(num);
        }

<<<<<<< HEAD
@Override
public ReplaceResult getReplaceResult(String
        versionValue,String deployValue){
        if(versionValue.equals(deployValue)||deployValue.equals("")){
        ReplaceResult replaceResult=new ReplaceResult();
        replaceResult.setDeltaYaml("");
        replaceResult.setYaml(versionValue);
        replaceResult.setHighlightMarkers(new ArrayList<>());
        replaceResult.setNewLines(new ArrayList<>());
        return replaceResult;
        >>>>>>>[IMP]重构后端代码
        }
        c7nHelmRelease.getSpec().setValues(
        getReplaceResult(applicationVersionRepository.queryValue(deployVersionId),
        deployValue).getDeltaYaml().trim());
        return c7nHelmRelease;
        }

        String fileName=GenerateUUID.generateUUID()+YAML_SUFFIX;
        String path="deployfile";
        FileUtil.saveDataToFile(path,fileName,versionValue+"\n"+"---"+"\n"+deployValue);
        ReplaceResult replaceResult;
        try{
        replaceResult=FileUtil.replaceNew(path+System.getProperty(FILE_SEPARATOR)+fileName);
        }catch(Exception e){
        throw new CommonException(e.getMessage(),e);
        }
        if(replaceResult.getHighlightMarkers()==null){
        replaceResult.setHighlightMarkers(new ArrayList<>());
        }
        replaceResult.setTotalLine(FileUtil.getFileTotalLine(replaceResult.getYaml()));
        FileUtil.deleteFile(path+System.getProperty(FILE_SEPARATOR)+fileName);
        return replaceResult;
        }

        <<<<<<<HEAD
private String getDeployTime(Long diff){
        float num=(float)diff/(60*1000);
        DecimalFormat df=new DecimalFormat("0.00");
        return df.format(num);
        }

@Override
public ReplaceResult getReplaceResult(String versionValue,String deployValue){
        if(versionValue.equals(deployValue)||deployValue.equals("")){
        ReplaceResult replaceResult=new ReplaceResult();
        replaceResult.setDeltaYaml("");
        replaceResult.setYaml(versionValue);
        replaceResult.setHighlightMarkers(new ArrayList<>());
        replaceResult.setNewLines(new ArrayList<>());
        return replaceResult;
        =======
@Override
public ApplicationInstanceVO deployRemote
        (ApplicationRemoteDeployDTO appRemoteDeployDTO){
        ApplicationE applicationE=createApplication(appRemoteDeployDTO);
        ApplicationVersionE versionE=createVersion(applicationE,appRemoteDeployDTO.getVersionRemoteDTO());
        ApplicationDeployDTO applicationDeployDTO=new ApplicationDeployDTO();
        BeanUtils.copyProperties(appRemoteDeployDTO,applicationDeployDTO);
        applicationDeployDTO.setAppId(applicationE.getId());
        applicationDeployDTO.setAppVersionId(versionE.getId());
        applicationDeployDTO.setValues(appRemoteDeployDTO.getVersionRemoteDTO().getValues());
        return createOrUpdate(applicationDeployDTO);
        }
=======
    @Override
    public InstanceValueVO getReplaceResult(String
                                                    versionValue, String deployValue) {
        if (versionValue.equals(deployValue) || deployValue.equals("")) {
            InstanceValueVO instanceValueVO = new InstanceValueVO();
            instanceValueVO.setDeltaYaml("");
            instanceValueVO.setYaml(versionValue);
            instanceValueVO.setHighlightMarkers(new ArrayList<>());
            instanceValueVO.setNewLines(new ArrayList<>());
            return instanceValueVO;
        }

        String fileName = GenerateUUID.generateUUID() + YAML_SUFFIX;
        String path = "deployfile";
        FileUtil.saveDataToFile(path, fileName, versionValue + "\n" + "---" + "\n" + deployValue);
        InstanceValueVO instanceValueVO;
        try {
            instanceValueVO = FileUtil.replaceNew(path + System.getProperty(FILE_SEPARATOR) + fileName);
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
        if (instanceValueVO.getHighlightMarkers() == null) {
            instanceValueVO.setHighlightMarkers(new ArrayList<>());
        }
        instanceValueVO.setTotalLine(FileUtil.getFileTotalLine(instanceValueVO.getYaml()));
        FileUtil.deleteFile(path + System.getProperty(FILE_SEPARATOR) + fileName);
        return instanceValueVO;
    }

    @Override
    public ApplicationInstanceVO deployRemote
            (ApplicationRemoteDeployDTO appRemoteDeployDTO) {
        ApplicationE applicationE = createApplication(appRemoteDeployDTO);
        ApplicationVersionE versionE = createVersion(applicationE, appRemoteDeployDTO.getVersionRemoteDTO());
        ApplicationDeployVO applicationDeployVO = new ApplicationDeployVO();
        BeanUtils.copyProperties(appRemoteDeployDTO, applicationDeployVO);
        applicationDeployVO.setAppId(applicationE.getId());
        applicationDeployVO.setAppVersionId(versionE.getId());
        applicationDeployVO.setValues(appRemoteDeployDTO.getVersionRemoteDTO().getValues());
        return createOrUpdate(applicationDeployVO);
    }
>>>>>>> [IMP]重构后端代码


private ApplicationE createApplication
        (ApplicationRemoteDeployDTO appRemoteDeployDTO){
        String code=appRemoteDeployDTO.getAppRemoteDTO().getCode();
        String name=appRemoteDeployDTO.getAppRemoteDTO().getName();
        ApplicationE applicationE=applicationRepository.queryByCodeWithNullProject(code);
        if(applicationE==null){
        applicationE=new ApplicationE();
        DevopsProjectConfigE harborConfigE=createConfig("harbor",appRemoteDeployDTO.getAppRemoteDTO().getCode(),appRemoteDeployDTO.getHarbor());
        DevopsProjectConfigE chartConfigE=createConfig("chart",appRemoteDeployDTO.getAppRemoteDTO().getCode(),appRemoteDeployDTO.getChart());
        applicationE.setType(appRemoteDeployDTO.getAppRemoteDTO().getType());
        applicationE.setCode(code);
        applicationE.setName(name);
        applicationE.setActive(true);
        applicationE.setSynchro(true);
        applicationE.setIsSkipCheckPermission(true);
        applicationE.initHarborConfig(harborConfigE.getId());
        applicationE.initChartConfig(chartConfigE.getId());
        return applicationRepository.create(applicationE);
        }
        return applicationE;
        }


private ApplicationVersionE createVersion(ApplicationE
        applicationE,ApplicationVersionRemoteDTO
        versionRemoteDTO){
        ApplicationVersionE versionE=applicationVersionRepository.queryByAppAndVersion(applicationE.getId(),versionRemoteDTO.getVersion());
        if(versionE==null){
        ApplicationVersionValueE versionValueE=new ApplicationVersionValueE();
        versionValueE.setValue(versionRemoteDTO.getValues());
        versionValueE=versionValueRepository.create(versionValueE);
        ApplicationVersionReadmeV versionReadmeV=new ApplicationVersionReadmeV();
        versionReadmeV.setReadme(versionRemoteDTO.getReadMeValue());
        versionReadmeV=versionReadmeRepository.create(versionReadmeV);
        versionE=new ApplicationVersionE();
        BeanUtils.copyProperties(versionRemoteDTO,versionE);
        versionE.setApplicationE(applicationE);
        versionE.setApplicationVersionValueE(versionValueE);
        versionE.setApplicationVersionReadmeV(versionReadmeV);
        return applicationVersionRepository.create(versionE);
        >>>>>>>[IMP]重构后端代码
        }
        return versionE;
        }

<<<<<<< HEAD
        <<<<<<<HEAD
        String fileName=GenerateUUID.generateUUID()+YAML_SUFFIX;
                String path="deployfile";
                FileUtil.saveDataToFile(path,fileName,versionValue+"\n"+"---"+"\n"+deployValue);
                ReplaceResult replaceResult;
                try{
                replaceResult=FileUtil.replaceNew(path+System.getProperty(FILE_SEPARATOR)+fileName);
                }catch(Exception e){
                throw new CommonException(e.getMessage(),e);
                }
                if(replaceResult.getHighlightMarkers()==null){
                replaceResult.setHighlightMarkers(new ArrayList<>());
        }
        replaceResult.setTotalLine(FileUtil.getFileTotalLine(replaceResult.getYaml()));
        FileUtil.deleteFile(path+System.getProperty(FILE_SEPARATOR)+fileName);
        return replaceResult;
        =======
/**
 * 创建远程配置
 *
 * @param type
 * @param code
 * @param projectConfigDTO
 * @return
 */
private DevopsProjectConfigE createConfig
        (String type,String code,ProjectConfigDTO
        projectConfigDTO){
        String name=code+"-"+type;
        DevopsProjectConfigE devopsPrpjectConfigE=devopsProjectConfigRepository.queryByNameWithNullProject(name);
        if(devopsPrpjectConfigE==null){
        DevopsProjectConfigDTO devopsProjectConfigDTO=new DevopsProjectConfigDTO();
        devopsProjectConfigDTO.setConfig(projectConfigDTO);
        devopsPrpjectConfigE=ConvertHelper.convert(devopsProjectConfigDTO,DevopsProjectConfigE.class);
        devopsPrpjectConfigE.setName(name);
        devopsPrpjectConfigE.setType(type);
        return devopsProjectConfigRepository.create(devopsPrpjectConfigE);
=======
    /**
     * 创建远程配置
     *
     * @param type
     * @param code
     * @param projectConfigVO
     * @return
     */
    private DevopsProjectConfigE createConfig
    (String type, String code, ProjectConfigVO
            projectConfigVO) {
        String name = code + "-" + type;
        DevopsProjectConfigE devopsPrpjectConfigE = devopsProjectConfigRepository.baseQueryByNameWithNullProject(name);
        if (devopsPrpjectConfigE == null) {
            DevopsProjectConfigVO devopsProjectConfigVO = new DevopsProjectConfigVO();
            devopsProjectConfigVO.setConfig(projectConfigVO);
            devopsPrpjectConfigE = ConvertHelper.convert(devopsProjectConfigVO, DevopsProjectConfigE.class);
            devopsPrpjectConfigE.setName(name);
            devopsPrpjectConfigE.setType(type);
            return devopsProjectConfigRepository.baseCreate(devopsPrpjectConfigE);
>>>>>>> [IMP]重构后端断码
        }
        return devopsPrpjectConfigE;
        >>>>>>>[IMP]重构后端代码
        }

@Override
public ApplicationInstanceVO deployRemote(ApplicationRemoteDeployDTO appRemoteDeployDTO){
        ApplicationE applicationE=createApplication(appRemoteDeployDTO);
        ApplicationVersionE versionE=createVersion(applicationE,appRemoteDeployDTO.getVersionRemoteDTO());
        ApplicationDeployDTO applicationDeployDTO=new ApplicationDeployDTO();
        BeanUtils.copyProperties(appRemoteDeployDTO,applicationDeployDTO);
        applicationDeployDTO.setAppId(applicationE.getId());
        applicationDeployDTO.setAppVersionId(versionE.getId());
        applicationDeployDTO.setValues(appRemoteDeployDTO.getVersionRemoteDTO().getValues());
        return createOrUpdate(applicationDeployDTO);
        }


<<<<<<< HEAD
        <<<<<<<HEAD
private ApplicationE createApplication(ApplicationRemoteDeployDTO appRemoteDeployDTO){
        String code=appRemoteDeployDTO.getAppRemoteDTO().getCode();
        String name=appRemoteDeployDTO.getAppRemoteDTO().getName();
        ApplicationE applicationE=applicationRepository.queryByCodeWithNullProject(code);
        if(applicationE==null){
        applicationE=new ApplicationE();
        DevopsProjectConfigE harborConfigE=createConfig("harbor",appRemoteDeployDTO.getAppRemoteDTO().getCode(),appRemoteDeployDTO.getHarbor());
        DevopsProjectConfigE chartConfigE=createConfig("chart",appRemoteDeployDTO.getAppRemoteDTO().getCode(),appRemoteDeployDTO.getChart());
        applicationE.setType(appRemoteDeployDTO.getAppRemoteDTO().getType());
        applicationE.setCode(code);
        applicationE.setName(name);
        applicationE.setActive(true);
        applicationE.setSynchro(true);
        applicationE.setIsSkipCheckPermission(true);
        applicationE.initHarborConfig(harborConfigE.getId());
        applicationE.initChartConfig(chartConfigE.getId());
        return applicationRepository.create(applicationE);
        }
        return applicationE;
        }

private ApplicationVersionE createVersion(ApplicationE applicationE,ApplicationVersionRemoteDTO versionRemoteDTO){
        ApplicationVersionE versionE=applicationVersionRepository.queryByAppAndVersion(applicationE.getId(),versionRemoteDTO.getVersion());
        if(versionE==null){
        ApplicationVersionValueE versionValueE=new ApplicationVersionValueE();
        versionValueE.setValue(versionRemoteDTO.getValues());
        versionValueE=versionValueRepository.create(versionValueE);
        ApplicationVersionReadmeV versionReadmeV=new ApplicationVersionReadmeV();
        versionReadmeV.setReadme(versionRemoteDTO.getReadMeValue());
        versionReadmeV=versionReadmeRepository.create(versionReadmeV);
        versionE=new ApplicationVersionE();
        BeanUtils.copyProperties(versionRemoteDTO,versionE);
        versionE.setApplicationE(applicationE);
        versionE.setApplicationVersionValueE(versionValueE);
        versionE.setApplicationVersionReadmeV(versionReadmeV);
        return applicationVersionRepository.create(versionE);
        }
        return versionE;
        }

/**
 * 创建远程配置
 *
 * @param type
 * @param code
 * @param projectConfigDTO
 * @return
 */
private DevopsProjectConfigE createConfig(String type,String code,ProjectConfigDTO projectConfigDTO){
        String name=code+"-"+type;
        DevopsProjectConfigE devopsPrpjectConfigE=devopsProjectConfigRepository.queryByNameWithNullProject(name);
        if(devopsPrpjectConfigE==null){
        DevopsProjectConfigDTO devopsProjectConfigDTO=new DevopsProjectConfigDTO();
        devopsProjectConfigDTO.setConfig(projectConfigDTO);
        devopsPrpjectConfigE=ConvertHelper.convert(devopsProjectConfigDTO,DevopsProjectConfigE.class);
        devopsPrpjectConfigE.setName(name);
        devopsPrpjectConfigE.setType(type);
        return devopsProjectConfigRepository.create(devopsPrpjectConfigE);
        =======
public ApplicationInstanceDTO baseQueryByCodeAndEnv
        (String code,Long envId){
        ApplicationInstanceDTO applicationInstanceDTO=new ApplicationInstanceDTO();
=======
    public ApplicationInstanceDTO baseQueryByCodeAndEnv
            (String code, Long envId) {
        ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO();
>>>>>>> [IMP]重构后端代码
        applicationInstanceDTO.setCode(code);
        applicationInstanceDTO.setEnvId(envId);
        return applicationInstanceMapper.selectOne(applicationInstanceDTO);
        }

public ApplicationInstanceDTO baseCreate
        (ApplicationInstanceDTO applicationInstanceDTO){
        if(applicationInstanceMapper.insert(applicationInstanceDTO)!=1){
        throw new CommonException("error.application.instance.create");
        }
        return applicationInstanceDTO;
        }

public ApplicationInstanceDTO baseQuery(Long id){
        return applicationInstanceMapper.selectByPrimaryKey(id);
        }

public List<ApplicationInstanceDTO> baseListByOptions
        (Long projectId,Long appId,Long appVersionId,Long
        envId){
        return applicationInstanceMapper.listApplicationInstanceCode(
        projectId,envId,appVersionId,appId);
        }

public List<ApplicationInstanceDTO> baseListByAppIdAndEnvId
        (Long projectId,Long appId,Long envId){
        return applicationInstanceMapper.listRunningAndFailedInstance(
        projectId,envId,appId);
        }

public int baseCountByOptions(Long envId,Long
        appId,String appInstanceCode){
        return applicationInstanceMapper.countByOptions(envId,appId,appInstanceCode);
        }

public String baseQueryValueByEnvIdAndAppId(Long
        envId,Long appId){
        return applicationInstanceMapper.queryValueByEnvIdAndAppId(envId,appId);
        }

public void baseUpdate(ApplicationInstanceDTO
        applicationInstanceDTO){
        applicationInstanceDTO.setObjectVersionNumber(
        applicationInstanceMapper.selectByPrimaryKey(applicationInstanceDTO.getId()).getObjectVersionNumber());
        if(applicationInstanceMapper.updateByPrimaryKeySelective(applicationInstanceDTO)!=1){
        throw new CommonException("error.instance.update");
        }
        }


public List<ApplicationInstanceDTO> baseListByEnvId
        (Long envId){
        ApplicationInstanceDTO applicationInstanceDTO=new ApplicationInstanceDTO();
        applicationInstanceDTO.setEnvId(envId);
        return applicationInstanceMapper
        .select(applicationInstanceDTO);
        }

public List<ApplicationInstanceOverViewDTO> baseListApplicationInstanceOverView
        (Long projectId,Long appId,List<Long> envIds){
        if(envIds!=null&&envIds.isEmpty()){
        envIds=null;
        }
        return applicationInstanceMapper.listApplicationInstanceOverView(projectId,appId,envIds);
        }

public List<ApplicationInstanceDTO> baseList(){
        return applicationInstanceMapper.selectAll();
        }

public String baseQueryValueByInstanceId(Long
        instanceId){
        return applicationInstanceMapper.queryByInstanceId(instanceId);
        }

public void baseDelete(Long id){
        applicationInstanceMapper.deleteByPrimaryKey(id);
        }


public List<DeployDTO> baseListDeployTime(Long
        projectId,Long envId,Long[]appIds,Date
        startTime,Date endTime){
        return applicationInstanceMapper
        .listDeployTime(projectId,envId,appIds,new java.sql.Date(startTime.getTime()),
        new java.sql.Date(endTime.getTime()));
        }

public List<DeployDTO> baselistDeployFrequency(Long
        projectId,Long[]envIds,Long appId,Date
        startTime,Date endTime){
        return applicationInstanceMapper
        .listDeployFrequency(projectId,envIds,appId,new java.sql.Date(startTime.getTime()),
        new java.sql.Date(endTime.getTime()));
        }

public PageInfo<DeployDTO> basePageDeployFrequencyDetail
        (Long projectId,PageRequest pageRequest,Long[]
        envIds,Long appId,
        Date startTime,Date endTime){
        return PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(),PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(()->
        applicationInstanceMapper
        .listDeployFrequency(projectId,envIds,appId,new java.sql.Date(startTime.getTime()),
        new java.sql.Date(endTime.getTime())));
        }

public PageInfo<DeployDTO> basePageDeployTimeDetail
        (Long projectId,PageRequest pageRequest,Long
        envId,Long[]appIds,
        Date startTime,Date endTime){
        return PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(),PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(()->
        applicationInstanceMapper
        .listDeployTime(projectId,envId,appIds,new java.sql.Date(startTime.getTime()),
        new java.sql.Date(endTime.getTime())));
        }

<<<<<<< HEAD
public List<ApplicationInstanceDTO> baseListByAppId
        (Long appId){
        ApplicationInstanceDTO applicationInstanceDTO=new ApplicationInstanceDTO();
=======
    @Override
    public List<ApplicationInstanceDTO> baseListByAppId(Long appId) {
        ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO();
>>>>>>> [REF] refactor ApplicationVersionController
        applicationInstanceDTO.setAppId(appId);
        return applicationInstanceMapper.select(applicationInstanceDTO);
        }

public void deleteByEnvId(Long envId){
        ApplicationInstanceDTO applicationInstanceDTO=new ApplicationInstanceDTO();
        applicationInstanceDTO.setEnvId(envId);
        applicationInstanceMapper.delete(applicationInstanceDTO);
        }

public List<ApplicationInstanceDTO> baseListByValueId
        (Long valueId){
        ApplicationInstanceDTO applicationInstanceDTO=new ApplicationInstanceDTO();
        applicationInstanceDTO.setValueId(valueId);
        return applicationInstanceMapper.select(applicationInstanceDTO);
        }


public void baseCheckName(String instanceName,Long
        envId){
        ApplicationInstanceDTO applicationInstanceDTO=new ApplicationInstanceDTO();
        applicationInstanceDTO.setCode(instanceName);
        applicationInstanceDTO.setEnvId(envId);
        if(applicationInstanceMapper.selectOne(applicationInstanceDTO)!=null){
        throw new CommonException("error.app.instance.name.already.exist");
        >>>>>>>[IMP]重构后端代码
        }
        return devopsPrpjectConfigE;
        }
        <<<<<<<HEAD
=======

public String baseGetInstanceResourceDetailJson
        (Long instanceId,String resourceName,ResourceType
        resourceType){
        return applicationInstanceMapper.getInstanceResourceDetailJson(instanceId,resourceName,resourceType.getType());
        }

<<<<<<< HEAD
public void deleteInstanceRelInfo(Long instanceId){
=======
    public void baseDeleteInstanceRelInfo(Long instanceId) {
>>>>>>> [IMP]重构后端代码
        applicationInstanceMapper.deleteInstanceRelInfo(instanceId);
        }


        >>>>>>>[IMP]重构后端代码
        }
