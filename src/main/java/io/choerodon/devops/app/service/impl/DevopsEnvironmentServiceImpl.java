package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.MiddlewareAppServiceName.MIDDLE_APP_SERVICE_NAME_MAP;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Functions;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.validator.DevopsEnvironmentValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.api.vo.market.MarketServiceVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.DevopsEnvUserPayload;
import io.choerodon.devops.app.eventhandler.payload.EnvGitlabProjectPayload;
import io.choerodon.devops.app.eventhandler.payload.GitlabProjectPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.CommitDTO;
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.gitlab.ProjectHookDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.market.ApplicationTypeEnums;
import io.choerodon.devops.infra.feign.operator.AsgardServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by younger on 2018/4/9.
 */
@Service
public class DevopsEnvironmentServiceImpl implements DevopsEnvironmentService {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DevopsEnvironmentServiceImpl.class);
    /**
     * 集群对应的环境name clusterName-env
     */
    private static final String SYSTEM_ENV_NAME = "%s-env";

    private static final Gson gson = new Gson();
    protected static final String MASTER = "master";
    protected static final String README = "README.md";
    protected static final String README_CONTENT = "# This is gitops env repository!";
    private static final String ENV = "ENV";
    private static final String ERROR_CODE_EXIST = "error.code.exist";
    private static final String ERROR_GITLAB_USER_SYNC_FAILED = "error.gitlab.user.sync.failed";
    private static final String LOGIN_NAME = "loginName";
    private static final String REAL_NAME = "realName";
    private static final Pattern CODE = Pattern.compile("[a-z]([-a-z0-9]*[a-z0-9])?");
    private static final String ERROR_CLUSTER_ENV_NUM_MAX = "error.cluster.env.num.max";

    /**
     * gitlab用于环境库的webhook地址
     */
    protected String gitOpsWebHookUrl;

    @Value("${services.gateway.url}")
    private String gatewayUrl;

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsEnvironmentValidator devopsEnvironmentValidator;
    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private DevopsEnvGroupService devopsEnvGroupService;
    @Autowired
    private DevopsClusterService devopsClusterService;
    @Autowired
    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    @Autowired
    private DevopsEnvUserPermissionMapper devopsEnvUserPermissionMapper;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsEnvCommitService devopsEnvCommitService;
    @Autowired
    @Lazy
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsEnvFileErrorMapper devopsEnvFileErrorMapper;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private DevopsGitService devopsGitService;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper;
    @Autowired
    private DevopsServiceMapper devopsServiceMapper;
    @Autowired
    private DevopsIngressMapper devopsIngressMapper;
    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper;
    @Autowired
    private DevopsConfigMapMapper devopsConfigMapMapper;
    @Autowired
    private DevopsSecretMapper devopsSecretMapper;
    @Autowired
    private DevopsCustomizeResourceMapper devopsCustomizeResourceMapper;
    @Autowired
    private DevopsEnvAppServiceMapper devopsEnvAppServiceMapper;
    @Autowired
    private DevopsEnvGroupMapper devopsEnvGroupMapper;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private GitlabGroupService gitlabGroupService;
    @Autowired
    private DevopsClusterMapper devopsClusterMapper;
    @Autowired
    private DevopsSecretService devopsSecretService;
    @Autowired
    private DevopsConfigMapService devopsConfigMapService;
    @Autowired
    private DevopsCustomizeResourceService devopsCustomizeResourceService;
    @Autowired
    private DevopsPvcService devopsPvcService;
    @Autowired
    private DevopsDeployValueService devopsDeployValueService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private DevopsRegistrySecretService devopsRegistrySecretService;
    @Autowired
    private DevopsClusterProPermissionService devopsClusterProPermissionService;
    @Autowired
    private DevopsEnvFileService devopsEnvFileService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsEnvFileErrorService devopsEnvFileErrorService;
    @Autowired
    private PolarisScanningService polarisScanningService;
    @Autowired
    private SendNotificationService sendNotificationService;
    @Autowired
    private AsgardServiceClientOperator asgardServiceClientOperator;
    @Autowired
    private DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;

    @PostConstruct
    private void init() {
        gitOpsWebHookUrl = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
        gitOpsWebHookUrl += GitOpsConstants.GITOPS_WEBHOOK_RELATIVE_URL;
    }

    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_ENV, description = "创建环境", inputSchema = "{}")
    @Transactional(rollbackFor = Exception.class)
    public void create(Long projectId, DevopsEnvironmentReqVO devopsEnvironmentReqVO) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = ConvertUtils.convertObject(devopsEnvironmentReqVO, DevopsEnvironmentDTO.class);
        if (!checkEnableCreateEnv(projectId)) {
            throw new CommonException(ERROR_CLUSTER_ENV_NUM_MAX);
        }
        if (!devopsClusterProPermissionService.projectHasClusterPermission(projectId, devopsEnvironmentReqVO.getClusterId())) {
            throw new CommonException("error.project.miss.cluster.permission");
        }

        // 创建环境时默认跳过权限校验
        devopsEnvironmentDTO.setSkipCheckPermission(Boolean.TRUE);
        devopsEnvironmentDTO.setProjectId(projectId);

        checkCode(projectId, devopsEnvironmentReqVO.getClusterId(), devopsEnvironmentReqVO.getCode());
        devopsEnvGroupService.checkGroupIdInProject(devopsEnvironmentDTO.getDevopsEnvGroupId(), projectId);

        devopsEnvironmentDTO.setType(EnvironmentType.USER.getValue());
        devopsEnvironmentDTO.setActive(true);
        devopsEnvironmentDTO.setConnected(false);
        devopsEnvironmentDTO.setSynchro(false);
        devopsEnvironmentDTO.setFailed(false);
        devopsEnvironmentDTO.setAutoDeploy(true);
        devopsEnvironmentDTO.setClusterId(devopsEnvironmentReqVO.getClusterId());
        devopsEnvironmentDTO.setToken(GenerateUUID.generateUUID());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        if (userAttrDTO == null) {
            throw new CommonException(ERROR_GITLAB_USER_SYNC_FAILED);
        }

        boolean isGitlabRoot = false;

        if (Boolean.TRUE.equals(userAttrDTO.getGitlabAdmin())) {
            // 如果这边表存了gitlabAdmin这个字段,那么gitlabUserId就不会为空,所以不判断此字段为空
            isGitlabRoot = gitlabServiceClientOperator.isGitlabAdmin(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }

        // 查询创建环境所在的gitlab环境组
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);

        if (!isGitlabRoot) {
            MemberDTO memberDTO = gitlabServiceClientOperator.queryGroupMember(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()),
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            if (memberDTO == null || !memberDTO.getAccessLevel().equals(AccessLevel.OWNER.toValue())) {
                throw new CommonException("error.user.not.gitlab.owner");
            }
        }

        List<String> sshKeys = FileUtil.getSshKey(
                organizationDTO.getTenantNum() + "/" + projectDTO.getDevopsComponentCode() + "/" + devopsEnvironmentReqVO.getCode());
        devopsEnvironmentDTO.setEnvIdRsa(sshKeys.get(0));
        devopsEnvironmentDTO.setEnvIdRsaPub(sshKeys.get(1));

        producer.apply(
                StartSagaBuilder.newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_ENV)
                        .withSourceId(projectId),
                builder -> {
                    Long envId = baseCreate(devopsEnvironmentDTO).getId();
                    devopsEnvironmentDTO.setId(envId);

                    EnvGitlabProjectPayload gitlabProjectPayload = new EnvGitlabProjectPayload();
                    gitlabProjectPayload.setGroupId(TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()));
                    gitlabProjectPayload.setUserId(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
                    gitlabProjectPayload.setPath(devopsEnvironmentReqVO.getCode());
                    gitlabProjectPayload.setOrganizationId(organizationDTO.getTenantId());
                    gitlabProjectPayload.setType(ENV);
                    IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userAttrDTO.getIamUserId());
                    gitlabProjectPayload.setLoginName(iamUserDTO.getLoginName());
                    gitlabProjectPayload.setRealName(iamUserDTO.getRealName());
                    gitlabProjectPayload.setIamUserId(userAttrDTO.getIamUserId());
                    gitlabProjectPayload.setClusterId(devopsEnvironmentReqVO.getClusterId());
                    gitlabProjectPayload.setIamProjectId(projectId);
                    gitlabProjectPayload.setSkipCheckPermission(devopsEnvironmentDTO.getSkipCheckPermission());
                    gitlabProjectPayload.setEnvId(envId);

                    agentCommandService.initEnv(devopsEnvironmentDTO, devopsEnvironmentReqVO.getClusterId());

                    builder.withPayloadAndSerialize(gitlabProjectPayload)
                            .withRefId(String.valueOf(devopsEnvironmentDTO.getId()))
                            .withRefType("env");
                }
        );
    }

    private static boolean isCodePatternValid(String code) {
        return CODE.matcher(code).matches();
    }

    @Override
    public List<DevopsEnvGroupEnvsVO> listDevopsEnvGroupEnvs(Long projectId, Boolean active) {
        List<DevopsEnvGroupEnvsVO> devopsEnvGroupEnvsDTOS = new ArrayList<>();
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();
        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = baseListByProjectIdAndActive(projectId, active).stream().peek(t ->
                setEnvStatus(upgradeClusterList, t)
        )
                .collect(Collectors.toList());
        List<DevopsEnvironmentRepVO> devopsEnviromentRepDTOS = ConvertUtils.convertList(devopsEnvironmentDTOS, DevopsEnvironmentRepVO.class);
        if (!active) {
            DevopsEnvGroupEnvsVO devopsEnvGroupEnvsDTO = new DevopsEnvGroupEnvsVO();
            devopsEnvGroupEnvsDTO.setDevopsEnvironmentRepDTOs(devopsEnviromentRepDTOS);
            devopsEnvGroupEnvsDTOS.add(devopsEnvGroupEnvsDTO);
            return devopsEnvGroupEnvsDTOS;
        }
        List<DevopsEnvGroupDTO> devopsEnvGroupES = devopsEnvGroupService.baseListByProjectId(projectId);
        devopsEnviromentRepDTOS.forEach(devopsEnviromentRepDTO -> {
            DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(devopsEnviromentRepDTO.getClusterId());
            devopsEnviromentRepDTO.setClusterName(devopsClusterDTO == null ? null : devopsClusterDTO.getName());
            if (devopsEnviromentRepDTO.getDevopsEnvGroupId() == null) {
                devopsEnviromentRepDTO.setDevopsEnvGroupId(0L);
            }
        });
        //按照环境组分组查询，有环境的环境组放前面，没环境的环境组放后面
        Map<Long, List<DevopsEnvironmentRepVO>> resultMaps = devopsEnviromentRepDTOS.stream()
                .collect(Collectors.groupingBy(DevopsEnvironmentRepVO::getDevopsEnvGroupId));

        List<Long> envGroupIds = new ArrayList<>();
        resultMaps.forEach((key, value) -> {
            envGroupIds.add(key);
            DevopsEnvGroupEnvsVO devopsEnvGroupEnvsDTO = new DevopsEnvGroupEnvsVO();
            DevopsEnvGroupDTO devopsEnvGroupDTO = new DevopsEnvGroupDTO();
            if (key != 0) {
                devopsEnvGroupDTO = devopsEnvGroupService.baseQuery(key);
            }
            devopsEnvGroupEnvsDTO.setDevopsEnvGroupId(devopsEnvGroupDTO.getId());
            devopsEnvGroupEnvsDTO.setDevopsEnvGroupName(devopsEnvGroupDTO.getName());
            devopsEnvGroupEnvsDTO.setDevopsEnvironmentRepDTOs(value);
            devopsEnvGroupEnvsDTOS.add(devopsEnvGroupEnvsDTO);
        });
        devopsEnvGroupES.forEach(devopsEnvGroupE -> {
            if (!envGroupIds.contains(devopsEnvGroupE.getId())) {
                DevopsEnvGroupEnvsVO devopsEnvGroupEnvsDTO = new DevopsEnvGroupEnvsVO();
                devopsEnvGroupEnvsDTO.setDevopsEnvGroupId(devopsEnvGroupE.getId());
                devopsEnvGroupEnvsDTO.setDevopsEnvGroupName(devopsEnvGroupE.getName());
                devopsEnvGroupEnvsDTOS.add(devopsEnvGroupEnvsDTO);
            }
        });
        return devopsEnvGroupEnvsDTOS;
    }

    @Override
    public List<DevopsEnvGroupEnvsVO> listEnvTreeMenu(Long projectId) {
        List<DevopsEnvGroupEnvsVO> devopsEnvGroupEnvsDTOS = new ArrayList<>();
        // 获得环境列表(包含激活与不激活)
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();

        List<DevopsEnvironmentDTO> devopsEnvironmentList = devopsEnvironmentMapper.listByProjectId(projectId)
                .stream()
                .peek(t -> setEnvStatus(upgradeClusterList, t))
                .collect(Collectors.toList());

        // 没有环境列表则返回空列表
        if (devopsEnvironmentList.isEmpty()) {
            List<DevopsEnvGroupDTO> devopsEnvGroupES = devopsEnvGroupService.baseListByProjectId(projectId);
            devopsEnvGroupES.forEach(g -> {
                DevopsEnvGroupEnvsVO devopsEnvGroupEnvsDTO2 = new DevopsEnvGroupEnvsVO();
                devopsEnvGroupEnvsDTO2.setDevopsEnvGroupId(g.getId());
                devopsEnvGroupEnvsDTO2.setDevopsEnvGroupName(g.getName());
                devopsEnvGroupEnvsDTOS.add(devopsEnvGroupEnvsDTO2);
            });
            devopsEnvGroupEnvsDTOS.add(new DevopsEnvGroupEnvsVO());
            return devopsEnvGroupEnvsDTOS;
        }
        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = devopsEnvironmentList.stream()
                .peek(t -> setEnvStatus(upgradeClusterList, t))
                .collect(Collectors.toList());

        List<DevopsEnvironmentRepVO> devopsEnvironmentRepDTOS = ConvertUtils.convertList(devopsEnvironmentDTOS, DevopsEnvironmentRepVO.class);

        List<DevopsEnvGroupDTO> devopsEnvGroupES = devopsEnvGroupService.baseListByProjectId(projectId);

        Map<Long, List<DevopsEnvironmentRepVO>> resultMaps = sort(devopsEnvironmentRepDTOS);

        List<Long> groupIds = new ArrayList<>(resultMaps.keySet());
        Map<Long, DevopsEnvGroupDTO> devopsEnvGroupDTOMap = new HashMap<>();
        devopsEnvGroupMapper.listByIdList(groupIds)
                .forEach(i -> devopsEnvGroupDTOMap.put(i.getId(), i));

        List<Long> envGroupIds = new ArrayList<>();

        //有环境的分组
        resultMaps.forEach((key, value) -> {
            envGroupIds.add(key);
            DevopsEnvGroupEnvsVO devopsEnvGroupEnvsDTO1 = new DevopsEnvGroupEnvsVO();
            DevopsEnvGroupDTO devopsEnvGroupDTO = new DevopsEnvGroupDTO();
            if (key != 0) {
                devopsEnvGroupDTO = Optional.ofNullable(devopsEnvGroupDTOMap.get(key)).
                        orElseThrow(() -> new CommonException("error.env.group.not.exist"));
            }
            devopsEnvGroupEnvsDTO1.setDevopsEnvGroupId(devopsEnvGroupDTO.getId());
            devopsEnvGroupEnvsDTO1.setDevopsEnvGroupName(devopsEnvGroupDTO.getName());
            devopsEnvGroupEnvsDTO1.setDevopsEnvironmentRepDTOs(value);
            devopsEnvGroupEnvsDTOS.add(devopsEnvGroupEnvsDTO1);
        });
        //没有环境的分组
        devopsEnvGroupES.forEach(devopsEnvGroupE -> {
            if (!envGroupIds.contains(devopsEnvGroupE.getId())) {
                DevopsEnvGroupEnvsVO devopsEnvGroupEnvsDTO2 = new DevopsEnvGroupEnvsVO();
                devopsEnvGroupEnvsDTO2.setDevopsEnvGroupId(devopsEnvGroupE.getId());
                devopsEnvGroupEnvsDTO2.setDevopsEnvGroupName(devopsEnvGroupE.getName());
                devopsEnvGroupEnvsDTOS.add(devopsEnvGroupEnvsDTO2);
            }
        });
        if (!resultMaps.containsKey(0L)) {
            devopsEnvGroupEnvsDTOS.add(new DevopsEnvGroupEnvsVO());
        }
        return devopsEnvGroupEnvsDTOS;
    }

    @Override
    public List<DevopsEnvironmentRepVO> listByGroup(Long projectId, Long groupId) {
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();
        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = devopsEnvironmentMapper.listByProjectIdAndGroupId(projectId, groupId)
                .stream()
                .peek(t -> setEnvStatus(upgradeClusterList, t))
                .collect(Collectors.toList());
        if (devopsEnvironmentDTOS.isEmpty()) {
            return new ArrayList<>();
        }

        // 如果没有传入groupId，则赋值0
        if (groupId == null) {
            groupId = 0L;
        }
        List<DevopsEnvironmentRepVO> devopsEnvironmentRepVOS = ConvertUtils.convertList(devopsEnvironmentDTOS, DevopsEnvironmentRepVO.class);
        List<String> refIds = devopsEnvironmentRepVOS.stream().map(devopsEnvironmentRepVO -> String.valueOf(devopsEnvironmentRepVO.getId())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(refIds)) {
            Map<String, SagaInstanceDetails> stringSagaInstanceDetailsMap = SagaInstanceUtils.listToMap(asgardServiceClientOperator.queryByRefTypeAndRefIds(ENV.toLowerCase(), refIds, SagaTopicCodeConstants.DEVOPS_CREATE_ENV));
            devopsEnvironmentRepVOS.forEach(devopsEnvironmentRepVO -> {
                devopsEnvironmentRepVO.setSagaInstanceId(SagaInstanceUtils.fillInstanceId(stringSagaInstanceDetailsMap, String.valueOf(devopsEnvironmentRepVO.getId())));
            });
        }
        return sort(devopsEnvironmentRepVOS).get(groupId);
    }

    @Override
    public Map<Long, List<DevopsEnvironmentRepVO>> sort(List<DevopsEnvironmentRepVO> devopsEnvironmentRepDTOS) {

        devopsEnvironmentRepDTOS.forEach(devopsEnvironmentRepDTO -> {
            if (devopsEnvironmentRepDTO.getDevopsEnvGroupId() == null) {
                devopsEnvironmentRepDTO.setDevopsEnvGroupId(0L);
            }
        });

        // 按分组选出处理中以及失败的环境
        Map<Long, List<DevopsEnvironmentRepVO>> synchroAndFailResultMaps = devopsEnvironmentRepDTOS
                .stream()
                .filter(i -> (!i.getSynchro()) || (i.getSynchro() && i.getFailed()))
                .sorted(Comparator.comparing(DevopsEnvironmentRepVO::getId).reversed())
                .collect(Collectors.groupingBy(DevopsEnvironmentRepVO::getDevopsEnvGroupId));
        // 按分组选出创建成功、没有停用的环境列表环境列表
        Map<Long, List<DevopsEnvironmentRepVO>> resultMaps = devopsEnvironmentRepDTOS
                .stream()
                .filter(i -> i.getSynchro() && !i.getFailed() && i.getActive())
                .sorted(
                        Comparator.comparing(DevopsEnvironmentRepVO::getConnected)
                                .thenComparing(DevopsEnvironmentRepVO::getId)
                                .reversed()
                )
                .collect(Collectors.groupingBy(DevopsEnvironmentRepVO::getDevopsEnvGroupId));
        // 按分组选出未激活环境
        Map<Long, List<DevopsEnvironmentRepVO>> notActiveResultMaps = devopsEnvironmentRepDTOS
                .stream()
                .filter(i -> i.getSynchro() && !i.getFailed() && !i.getActive())
                .collect(Collectors.groupingBy(DevopsEnvironmentRepVO::getDevopsEnvGroupId));

        synchroAndFailResultMaps.forEach((key, value) -> {
            List<DevopsEnvironmentRepVO> devopsEnvironmentRepVOList = resultMaps.get(key);
            if (devopsEnvironmentRepVOList != null) {
                devopsEnvironmentRepVOList.addAll(value);
                resultMaps.put(key, devopsEnvironmentRepVOList);
            } else {
                resultMaps.put(key, value);
            }
        });

        // 把按分组选出的停用环境加入到激活环境分组中
        notActiveResultMaps.forEach((key, value) -> {
            List<DevopsEnvironmentRepVO> devopsEnvironmentRepVOList = resultMaps.get(key);
            if (devopsEnvironmentRepVOList != null) {
                devopsEnvironmentRepVOList.addAll(value);
                resultMaps.put(key, devopsEnvironmentRepVOList);
            } else {
                resultMaps.put(key, value);
            }
        });

        return resultMaps;
    }

    @Override
    public List<DevopsEnvironmentRepVO> listByProjectIdAndActive(Long projectId, Boolean active) {

        // 查询当前用户的环境权限
        List<Long> permissionEnvIds = devopsEnvUserPermissionService
                .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                .filter(DevopsEnvUserPermissionDTO::getPermitted)
                .map(DevopsEnvUserPermissionDTO::getEnvId).collect(Collectors.toList());
        // 查询当前用户是否为项目所有者
        Boolean projectOwnerOrRoot = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId);

        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();
        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = baseListByProjectIdAndActive(projectId, active).stream()
                .filter(devopsEnvironmentE -> !devopsEnvironmentE.getFailed()).peek(t -> {
                    setEnvStatus(upgradeClusterList, t);
                    // 项目成员返回拥有对应权限的环境，项目所有者返回所有环境
                    setPermission(t, permissionEnvIds, projectOwnerOrRoot);
                })
                .collect(Collectors.toList());
        return ConvertUtils.convertList(devopsEnvironmentDTOS, DevopsEnvironmentRepVO.class);
    }

    @Override
    public List<DevopsEnvironmentViewVO> listInstanceEnvTree(Long projectId) {
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();

        List<DevopsEnvironmentViewVO> connectedEnvs = new ArrayList<>();
        List<DevopsEnvironmentViewVO> unConnectedEnvs = new ArrayList<>();

        boolean projectOwnerOrRoot = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId);

        List<DevopsEnvironmentViewDTO> views;
        if (projectOwnerOrRoot) {
            views = devopsEnvironmentMapper.listAllInstanceEnvTree(projectId);
        } else {
            views = devopsEnvironmentMapper.listMemberInstanceEnvTree(projectId, DetailsHelper.getUserDetails().getUserId());
        }

        // 市场服务id
        Set<Long> marketServiceIds = new HashSet<>();

        views.forEach(e -> {
            // 将DTO层对象转为VO
            DevopsEnvironmentViewVO vo = new DevopsEnvironmentViewVO();
            BeanUtils.copyProperties(e, vo, "apps");
            boolean connected = upgradeClusterList.contains(e.getClusterId());
            vo.setConnect(connected);
            vo.setApps(e.getApps().stream().map(app -> {

                DevopsAppServiceViewVO appVO = new DevopsAppServiceViewVO();
                BeanUtils.copyProperties(app, appVO, "instances");
                // 如果应用服务没有名字，认为它是市场服务
                if (appVO.getName() == null) {
                    marketServiceIds.add(appVO.getId());
                }
                appVO.setType(appServiceService.checkAppServiceType(projectId, app.getProjectId(), app.getSource()));
                appVO.setInstances(app.getInstances().stream().map(ins -> {
                    DevopsAppServiceInstanceViewVO insVO = new DevopsAppServiceInstanceViewVO();
                    BeanUtils.copyProperties(ins, insVO);
                    return insVO;
                }).collect(Collectors.toList()));

                return appVO;
            }).collect(Collectors.toList()));

            if (connected) {
                connectedEnvs.add(vo);
            } else {
                unConnectedEnvs.add(vo);
            }
        });

        // 为了将环境按照状态排序: 连接（运行中） > 未连接
        connectedEnvs.addAll(unConnectedEnvs);

        // 填充缺失的市场服务的名称
        fillMarketServiceNames(projectId, marketServiceIds, connectedEnvs);
        return connectedEnvs;
    }

    private void fillMarketServiceNames(Long projectId, Set<Long> marketServiceIds, List<DevopsEnvironmentViewVO> envs) {
        if (!marketServiceIds.isEmpty()) {
            // 如果市场服务不为空，查询数据填充
            Map<Long, MarketServiceVO> marketServices = marketServiceClientOperator.queryMarketServiceByIds(projectId, marketServiceIds).stream().collect(Collectors.toMap(MarketServiceVO::getId, Function.identity()));
            envs.forEach(env -> env.getApps().forEach(app -> {
                if (app.getName() == null) {
                    MarketServiceVO marketServiceVO = marketServices.get(app.getId());
                    if (marketServiceVO != null) {
                        if (ApplicationTypeEnums.MIDDLEWARE.getValue().equals(marketServiceVO.getMarketAppType())) {
                            app.setName(MIDDLE_APP_SERVICE_NAME_MAP.get(marketServiceVO.getMarketAppName() + "-" + marketServiceVO.getMarketServiceName()));
                        } else {
                            app.setName(marketServiceVO.getMarketServiceName());
                        }
                    } else {
                        app.setName(MiscConstants.UNKNOWN_SERVICE);
                    }
                }
            }));
        }
    }

    @Override
    public List<DevopsResourceEnvOverviewVO> listResourceEnvTree(Long projectId) {
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();

        List<DevopsResourceEnvOverviewVO> connectedEnvs = new ArrayList<>();
        List<DevopsResourceEnvOverviewVO> unConnectedEnvs = new ArrayList<>();

        boolean projectOwnerOrRoot = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId);

        List<DevopsResourceEnvOverviewDTO> views;
        if (projectOwnerOrRoot) {
            views = devopsEnvironmentMapper.listAllResourceEnvTree(projectId);
        } else {
            views = devopsEnvironmentMapper.listMemberResourceEnvTree(projectId, DetailsHelper.getUserDetails().getUserId());
        }

        views.forEach(e -> {
            // 应前端要求返回该字段的空数组
            e.setPvcs(new ArrayList<>());
            // 将DTO层对象转为VO
            DevopsResourceEnvOverviewVO vo = new DevopsResourceEnvOverviewVO();
            BeanUtils.copyProperties(e, vo);
            boolean connected = upgradeClusterList.contains(e.getClusterId());
            vo.setConnect(connected);

            if (connected) {
                connectedEnvs.add(vo);
            } else {
                unConnectedEnvs.add(vo);
            }
        });

        // 为了将环境按照状态排序: 连接（运行中） > 未连接
        connectedEnvs.addAll(unConnectedEnvs);
        return connectedEnvs;
    }

    @Override
    public Boolean updateActive(Long projectId, Long environmentId, Boolean active) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, environmentId);

        if (active == null) {
            active = Boolean.TRUE;
        }
        List<Long> updatedClusterList = clusterConnectionHandler.getUpdatedClusterList();

        // 要停用环境时，对环境进行校验
        if (!active) {
            // 如果已连接
            if (updatedClusterList.contains(devopsEnvironmentDTO.getClusterId())) {
                devopsEnvironmentValidator.checkEnvCanDisabled(environmentId);
            } else {
                if (!CollectionUtils.isEmpty(devopsCdEnvDeployInfoService.queryCurrentByEnvId(environmentId))) {
                    throw new CommonException("error.env.stop.pipeline.app.deploy.exist");
                }
            }
        }

        devopsEnvironmentDTO.setActive(active);
        baseUpdate(devopsEnvironmentDTO);
        //发送web hook
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        if (Boolean.TRUE.equals(active)) {
            sendNotificationService.sendWhenEnvEnable(devopsEnvironmentDTO, projectDTO.getOrganizationId());
        }
        if (Boolean.FALSE.equals(active)) {
            sendNotificationService.sendWhenEnvDisable(devopsEnvironmentDTO, projectDTO.getOrganizationId());
        }
        return true;
    }

    @Override
    public DevopsEnvironmentUpdateVO query(Long environmentId) {
        return ConvertUtils.convertObject(baseQueryById(environmentId), DevopsEnvironmentUpdateVO.class);
    }

    @Override
    public DevopsEnvironmentInfoVO queryInfoById(Long projectId, Long environmentId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        DevopsEnvironmentInfoDTO envInfo = devopsEnvironmentMapper.queryInfoById(environmentId);
        if (envInfo == null) {
            return null;
        }

        DevopsEnvironmentInfoVO vo = new DevopsEnvironmentInfoVO();
        BeanUtils.copyProperties(envInfo, vo);
        vo.setFail(envInfo.getFailed());

        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();
        vo.setConnect(upgradeClusterList.contains(envInfo.getClusterId()));


        if (envInfo.getSagaSyncCommit() == null) {
            if (devopsEnvFileErrorMapper.queryErrorFileCountByEnvId(environmentId) > 0) {
                vo.setGitopsStatus(EnvironmentGitopsStatus.FAILED.getValue());
            } else {
                vo.setGitopsStatus(EnvironmentGitopsStatus.PROCESSING.getValue());
            }
            return vo;
        }

        if (envInfo.getSagaSyncCommit().equals(envInfo.getAgentSyncCommit()) &&
                envInfo.getSagaSyncCommit().equals(envInfo.getDevopsSyncCommit())) {
            vo.setGitopsStatus(EnvironmentGitopsStatus.FINISHED.getValue());
        } else {
            if (devopsEnvFileErrorMapper.queryErrorFileCountByEnvId(environmentId) > 0) {
                vo.setGitopsStatus(EnvironmentGitopsStatus.FAILED.getValue());
            } else {
                vo.setGitopsStatus(EnvironmentGitopsStatus.PROCESSING.getValue());
            }
        }

        gitlabUrl = gitlabUrl.endsWith("/") ? gitlabUrl.substring(0, gitlabUrl.length() - 1) : gitlabUrl;
        vo.setGitlabUrl(String.format("%s/%s-%s-gitops/%s/", gitlabUrl, organizationDTO.getTenantNum(), projectDTO.getDevopsComponentCode(), envInfo.getCode()));

        // 设置环境所属集群所在项目
        Long clusterId = vo.getClusterId();
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(clusterId);
        ProjectDTO clusterBelongedProject = baseServiceClientOperator.queryIamProjectById(devopsClusterDTO.getProjectId(), false, false, false);
        vo.setClusterBelongedProjectName(clusterBelongedProject.getName());

        return vo;
    }

    @Override
    public DevopsEnvResourceCountVO queryEnvResourceCount(Long environmentId) {
        DevopsEnvResourceCountVO devopsEnvResourceCountVO = devopsEnvironmentMapper.queryEnvResourceCount(environmentId);
        Long workloadCount = devopsEnvResourceCountVO.getCronJobCount() +
                devopsEnvResourceCountVO.getDaemonSetCount() +
                devopsEnvResourceCountVO.getDeploymentCount() +
                devopsEnvResourceCountVO.getJobCount() +
                devopsEnvResourceCountVO.getStatefulSetCount();
        devopsEnvResourceCountVO.setWorkloadCount(workloadCount);
        return devopsEnvResourceCountVO;
    }


    @Override
    public void checkEnv(DevopsEnvironmentDTO devopsEnvironmentDTO, UserAttrDTO userAttrDTO) {
        //校验用户是否有环境的权限
        devopsEnvUserPermissionService.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()), devopsEnvironmentDTO.getId());

        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());


        //检验gitops库是否存在，校验操作人是否是有gitops库的权限
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentDTO, userAttrDTO);
    }


    @Override
    public DevopsEnvironmentUpdateVO update(DevopsEnvironmentUpdateVO devopsEnvironmentUpdateDTO, Long projectId) {
        permissionHelper.checkEnvBelongToProject(projectId, devopsEnvironmentUpdateDTO.getId());
        DevopsEnvironmentDTO toUpdate = new DevopsEnvironmentDTO();
        toUpdate.setId(devopsEnvironmentUpdateDTO.getId());
        toUpdate.setName(devopsEnvironmentUpdateDTO.getName());
        toUpdate.setDescription(devopsEnvironmentUpdateDTO.getDescription());
        toUpdate.setObjectVersionNumber(devopsEnvironmentUpdateDTO.getObjectVersionNumber());

        devopsEnvGroupService.checkGroupIdInProject(devopsEnvironmentUpdateDTO.getDevopsEnvGroupId(), projectId);

        toUpdate.setDevopsEnvGroupId(devopsEnvironmentUpdateDTO.getDevopsEnvGroupId());
        return ConvertUtils.convertObject(baseUpdate(toUpdate), DevopsEnvironmentUpdateVO.class);
    }

    @Override
    public void setEnvStatus(List<Long> upgradeEnvList, DevopsEnvironmentDTO t) {
        t.setConnected(upgradeEnvList.contains(t.getClusterId()));
    }


    @Override
    public void retryGitOps(Long projectId, Long envId) {
        // TODO 参考{@link io.choerodon.devops.app.service.impl.DevopsClusterResourceServiceImpl.retrySystemEnvGitOps}改写
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, envId);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(GitUserNameUtil.getUserId());
        if (userAttrDTO == null) {
            throw new CommonException(ERROR_GITLAB_USER_SYNC_FAILED);
        }
        CommitDTO commitDO = gitlabServiceClientOperator.listCommits(devopsEnvironmentDTO.getGitlabEnvProjectId().intValue(), userAttrDTO.getGitlabUserId().intValue(), 1, 1).get(0);
        PushWebHookVO pushWebHookVO = new PushWebHookVO();
        pushWebHookVO.setCheckoutSha(commitDO.getId());
        pushWebHookVO.setUserId(userAttrDTO.getGitlabUserId().intValue());
        pushWebHookVO.setProjectId(devopsEnvironmentDTO.getGitlabEnvProjectId().intValue());
        CommitVO commitDTO = new CommitVO();
        commitDTO.setId(commitDO.getId());
        commitDTO.setTimestamp(commitDO.getTimestamp());
        pushWebHookVO.setCommits(ArrayUtil.singleAsList(commitDTO));

        //当环境总览第一阶段为空，第一阶段的commit不是最新commit, 第一阶段和第二阶段commit不一致时，可以重新触发gitOps
        if (devopsEnvironmentDTO.getSagaSyncCommit() == null) {
            devopsGitService.fileResourceSyncSaga(pushWebHookVO, devopsEnvironmentDTO.getToken());
        } else {
            DevopsEnvCommitDTO sagaSyncCommit = devopsEnvCommitService.baseQuery(devopsEnvironmentDTO.getSagaSyncCommit());
            if (!devopsEnvironmentDTO.getSagaSyncCommit().equals(devopsEnvironmentDTO.getDevopsSyncCommit()) || !sagaSyncCommit.getCommitSha().equals(commitDO.getId())) {
                devopsGitService.fileResourceSyncSaga(pushWebHookVO, devopsEnvironmentDTO.getToken());
            }
        }
    }

    // 开启新事务
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean retrySystemEnvGitOps(Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = baseQueryById(envId);
        if (devopsEnvironmentDTO == null) {
            LOGGER.info("Retry cluster env GitOps: the environment with id {} is unexpectedly null", envId);
            return false;
        }

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(GitUserNameUtil.getUserId());
        if (userAttrDTO == null) {
            throw new CommonException(ERROR_GITLAB_USER_SYNC_FAILED);
        }

        // 查询GitLab上环境最新的commit
        CommitDTO commitDO = gitlabServiceClientOperator.listCommits(devopsEnvironmentDTO.getGitlabEnvProjectId().intValue(), userAttrDTO.getGitlabUserId().intValue(), 1, 1).get(0);

        // 当环境总览第一阶段为空，第一阶段的commit不是最新commit, 第一阶段和第二阶段commit不一致时，可以重新触发gitOps
        if (GitOpsUtil.isToRetryGitOps(
                devopsEnvironmentDTO.getSagaSyncCommit(),
                devopsEnvCommitService.baseQuery(devopsEnvironmentDTO.getSagaSyncCommit()).getCommitSha(),
                devopsEnvironmentDTO.getDevopsSyncCommit(), commitDO.getId())) {

            PushWebHookVO pushWebHookVO = new PushWebHookVO();
            pushWebHookVO.setCheckoutSha(commitDO.getId());
            pushWebHookVO.setUserId(userAttrDTO.getGitlabUserId().intValue());
            pushWebHookVO.setProjectId(devopsEnvironmentDTO.getGitlabEnvProjectId().intValue());
            CommitVO commitDTO = new CommitVO();
            commitDTO.setId(commitDO.getId());
            commitDTO.setTimestamp(commitDO.getTimestamp());
            pushWebHookVO.setCommits(ArrayUtil.singleAsList(commitDTO));

            devopsGitService.fileResourceSyncSaga(pushWebHookVO, devopsEnvironmentDTO.getToken());
            return true;
        }
        return false;
    }

    /**
     * 判断是否还能创建环境
     *
     * @param projectId
     */
    @Override
    public Boolean checkEnableCreateEnv(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        ResourceLimitVO resourceLimitVO = baseServiceClientOperator.queryResourceLimit(projectDTO.getOrganizationId());
        if (resourceLimitVO != null) {
            DevopsEnvironmentDTO example = new DevopsEnvironmentDTO();
            example.setProjectId(projectId);
            int num = devopsEnvironmentMapper.selectCount(example);
            return num < resourceLimitVO.getEnvMaxNumber();
        }
        return true;
    }

    @Override
    public void checkCode(Long projectId, Long clusterId, String code) {
        if (!isCodePatternValid(code)) {
            throw new CommonException("error.env.code.notMatch");
        }
        // 兼容甄零的收紧权限逻辑，不需要校验集群中已存在code
//        if (doesNamespaceExistInCluster(clusterId, code)) {
//            throw new CommonException(ERROR_CODE_EXIST);
//        }
        baseCheckCode(projectId, clusterId, code);
    }

    private boolean doesNamespaceExistInCluster(Long clusterId, String code) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        // 考虑创建环境时,集群已删除的情况
        if (devopsClusterDTO == null) {
            throw new CommonException("error.cluster.not.exist", clusterId);
        }
        if (devopsClusterDTO.getNamespaces() != null) {
            return JSONArray.parseArray(devopsClusterDTO.getNamespaces(), String.class).stream().anyMatch(namespace -> namespace.equals(code));
        }
        return false;
    }

    @Override
    public boolean isCodeValid(Long projectId, Long clusterId, String code) {
        return isCodePatternValid(code)
                && isCodeUniqueInClusterAndProject(projectId, clusterId, code);
    }

    @Override
    public List<DevopsEnvironmentRepVO> listByProjectId(Long projectId, Long appServiceId) {
        List<DevopsEnvironmentRepVO> devopsEnviromentRepDTOList = listByProjectIdAndActive(projectId, true);

        if (appServiceId == null) {
            return devopsEnviromentRepDTOList.stream().filter(t ->
                    appServiceInstanceService.baseListByEnvId(t.getId()).stream()
                            .anyMatch(applicationInstanceDTO ->
                                    applicationInstanceDTO.getStatus().equals(InstanceStatus.RUNNING.getStatus())))
                    .collect(Collectors.toList());
        } else {
            return devopsEnviromentRepDTOList.stream().filter(t ->
                    appServiceInstanceService.baseListByEnvId(t.getId()).stream()
                            .anyMatch(applicationInstanceDTO ->
                                    applicationInstanceDTO.getStatus().equals(InstanceStatus.RUNNING.getStatus())
                                            && applicationInstanceDTO.getAppServiceId().equals(appServiceId)))
                    .collect(Collectors.toList());
        }
    }

    protected void checkGitlabProjectIdNotUsedBefore(Long gitlabProjectId) {
        DevopsEnvironmentDTO condition = new DevopsEnvironmentDTO();
        condition.setGitlabEnvProjectId(gitlabProjectId);
        CommonExAssertUtil.assertTrue(devopsEnvironmentMapper.selectCount(condition) == 0, "error.gitlab.project.associated.with.other.env");
    }

    @Override
    public void handleCreateEnvSaga(EnvGitlabProjectPayload gitlabProjectPayload) {
        DevopsProjectDTO gitlabGroupE = devopsProjectService.baseQueryByGitlabEnvGroupId(
                TypeUtil.objToInteger(gitlabProjectPayload.getGroupId()));
        DevopsEnvironmentDTO devopsEnvironmentDTO = baseQueryByClusterIdAndCode(gitlabProjectPayload.getClusterId(), gitlabProjectPayload.getPath());

        CommonExAssertUtil.assertNotNull(devopsEnvironmentDTO, "error.env.id.not.exist", gitlabProjectPayload.getPath());

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(gitlabGroupE.getIamProjectId());
        CommonExAssertUtil.assertNotNull(projectDTO, "error.project.query");

        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        GitlabProjectDTO gitlabProjectDO = gitlabServiceClientOperator.queryProjectByName(
                organizationDTO.getTenantNum() + "-" + projectDTO.getDevopsComponentCode() + "-gitops",
                devopsEnvironmentDTO.getCode(),
                gitlabProjectPayload.getUserId(),
                false);
        if (gitlabProjectDO == null || gitlabProjectDO.getId() == null) {
            gitlabProjectDO = gitlabServiceClientOperator.createProject(
                    gitlabProjectPayload.getGroupId(),
                    gitlabProjectPayload.getPath(),
                    gitlabProjectPayload.getUserId(),
                    false);
        } else {
            checkGitlabProjectIdNotUsedBefore(TypeUtil.objToLong(gitlabProjectDO.getId()));
        }
        devopsEnvironmentDTO.setGitlabEnvProjectId(TypeUtil.objToLong(gitlabProjectDO.getId()));
        if (gitlabServiceClientOperator.listDeployKey(gitlabProjectDO.getId(), gitlabProjectPayload.getUserId()).isEmpty()) {
            gitlabServiceClientOperator.createDeployKey(
                    gitlabProjectDO.getId(),
                    gitlabProjectPayload.getPath(),
                    devopsEnvironmentDTO.getEnvIdRsaPub(),
                    true,
                    GitUserNameUtil.getAdminId());
        }
        ProjectHookDTO projectHookDTO = ProjectHookDTO.allHook();
        projectHookDTO.setEnableSslVerification(true);
        projectHookDTO.setProjectId(gitlabProjectDO.getId());
        projectHookDTO.setToken(devopsEnvironmentDTO.getToken());
        projectHookDTO.setUrl(gitOpsWebHookUrl);
        List<ProjectHookDTO> projectHookDTOS = gitlabServiceClientOperator.listProjectHook(gitlabProjectDO.getId(),
                gitlabProjectPayload.getUserId());
        if (projectHookDTOS == null || projectHookDTOS.isEmpty()) {
            devopsEnvironmentDTO.setHookId(TypeUtil.objToLong(gitlabServiceClientOperator.createWebHook(
                    gitlabProjectDO.getId(), gitlabProjectPayload.getUserId(), projectHookDTO).getId()));
        } else {
            devopsEnvironmentDTO.setHookId(TypeUtil.objToLong(projectHookDTOS.get(0).getId()));
        }
        if (!gitlabServiceClientOperator.getFile(gitlabProjectDO.getId(), MASTER, README)) {
            LOGGER.info("Add readme for env with id {}", devopsEnvironmentDTO.getId());
            gitlabServiceClientOperator.createFile(gitlabProjectDO.getId(),
                    README, README_CONTENT, "ADD README", gitlabProjectPayload.getUserId());
        }

        // 创建环境时初始化用户权限，分为gitlab权限和devops环境用户表权限
        gitlabProjectPayload.setGitlabProjectId(gitlabProjectDO.getId());
        try {
            initUserPermissionWhenCreatingEnv(gitlabProjectPayload, devopsEnvironmentDTO.getId(), projectDTO.getId());
        } catch (Exception ex) {
            LOGGER.warn("Failed to init user permission when creating env {}", devopsEnvironmentDTO.getCode());
            LOGGER.warn("And the ex is", ex);
        }

        devopsEnvironmentDTO.setSynchro(true);
        baseUpdate(devopsEnvironmentDTO);
    }

    @Override
    public void initUserPermissionWhenCreatingEnv(EnvGitlabProjectPayload gitlabProjectPayload, Long envId, Long projectId) {
        // 跳过权限检查，项目下所有成员自动分配权限
        if (Boolean.TRUE.equals(gitlabProjectPayload.getSkipCheckPermission())) {
            List<Long> iamUserIds = baseServiceClientOperator.getAllMemberIdsWithoutOwner(gitlabProjectPayload.getIamProjectId());
            List<Integer> gitlabUserIds = userAttrService.baseListByUserIds(iamUserIds)
                    .stream()
                    .map(UserAttrDTO::getGitlabUserId)
                    .map(TypeUtil::objToInteger)
                    .collect(Collectors.toList());

            gitlabServiceClientOperator.denyAllAccessRequestInvolved(gitlabUserIds, gitlabProjectPayload.getGroupId());

            gitlabUserIds.forEach(userId -> {
                try {
                    updateGitlabMemberPermission(
                            gitlabProjectPayload.getGroupId(),
                            gitlabProjectPayload.getGitlabProjectId(),
                            userId);
                } catch (Exception ex) {
                    LOGGER.warn("Skip user permission for env due to ex. User id: {}, env: {}", userId, envId);
                    LOGGER.warn("The ex is ", ex);
                }
            });
            return;
        }

        // 需要分配权限的用户id
        List<Long> userIds = gitlabProjectPayload.getUserIds();
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        // 获取项目下所有项目成员
        Page<UserVO> allProjectMemberPage = getMembersFromProject(new PageRequest(0, 0), projectId, "");

        // 所有项目成员中有权限的
        List<UserVO> usersToBeAdded = allProjectMemberPage.getContent().stream().filter(e -> userIds.contains(e.getId())).collect(Collectors.toList());
        Map<Long, UserAttrDTO> devopsUsersToBeAdded = userAttrService.baseListByUserIds(usersToBeAdded.stream().map(UserVO::getId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(UserAttrDTO::getIamUserId, Functions.identity()));
        usersToBeAdded.forEach(e -> {
            Long userId = e.getId();
            String loginName = e.getLoginName();
            String realName = e.getRealName();
            if (devopsUsersToBeAdded.get(userId) == null) {
                return;
            }

            updateGitlabMemberPermission(
                    gitlabProjectPayload.getGroupId(),
                    gitlabProjectPayload.getGitlabProjectId(),
                    TypeUtil.objToInteger(devopsUsersToBeAdded.get(userId).getGitlabUserId()));
            // 添加devops数据库记录
            devopsEnvUserPermissionService.baseCreate(new DevopsEnvUserPermissionDTO(loginName, userId, realName, envId, true));
        });
    }

    /**
     * update gitlab project member permission
     *
     * @param gitlabGroupId   gitlab group id
     * @param gitlabProjectId gitlab project id
     * @param gitlabUserId    gitlab user id
     */
    @Override
    public void updateGitlabMemberPermission(Integer gitlabGroupId, Integer gitlabProjectId, Integer gitlabUserId) {
        // 删除组和用户之间的关系，如果存在
        MemberDTO memberDTO = gitlabServiceClientOperator.queryGroupMember(gitlabGroupId, TypeUtil.objToInteger(gitlabUserId));
        if (memberDTO != null) {
            gitlabServiceClientOperator.deleteGroupMember(gitlabGroupId, TypeUtil.objToInteger(gitlabUserId));
            UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(TypeUtil.objToLong(gitlabUserId));
            List<Long> gitlabProjectIds = devopsEnvironmentMapper.listGitlabProjectIdByEnvPermission(TypeUtil.objToLong(gitlabGroupId), userAttrDTO.getIamUserId());
            if (gitlabProjectIds != null && !gitlabProjectIds.isEmpty()) {
                gitlabProjectIds.forEach(aLong -> {
                    if (aLong == null) {
                        return;
                    }
                    MemberDTO gitlabMemberDTO = gitlabServiceClientOperator.getProjectMember(gitlabProjectId, TypeUtil.objToInteger(aLong));
                    if (gitlabMemberDTO == null || gitlabMemberDTO.getId() == null) {
                        gitlabServiceClientOperator.createProjectMember(gitlabProjectId, new MemberDTO(TypeUtil.objToInteger(aLong), 40, ""));
                    }
                });
            }
        }
        // 当项目不存在用户权限纪录时(防止失败重试时报成员已存在异常)，添加gitlab用户权限
        MemberDTO gitlabMemberDTO = gitlabServiceClientOperator.getProjectMember(gitlabProjectId, TypeUtil.objToInteger(gitlabUserId));
        if (gitlabMemberDTO == null || gitlabMemberDTO.getId() == null) {
            gitlabServiceClientOperator.createProjectMember(gitlabProjectId, new MemberDTO(TypeUtil.objToInteger(gitlabUserId), 40, ""));
        }
    }

    @Override
    public EnvSyncStatusVO queryEnvSyncStatus(Long projectId, Long envId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        DevopsEnvironmentDTO devopsEnvironmentDTO = baseQueryById(envId);
        if (devopsEnvironmentDTO == null) {
            return null;
        }
        EnvSyncStatusVO envSyncStatusDTO = new EnvSyncStatusVO();
        if (devopsEnvironmentDTO.getAgentSyncCommit() != null) {
            envSyncStatusDTO.setAgentSyncCommit(devopsEnvCommitService
                    .baseQuery(devopsEnvironmentDTO.getAgentSyncCommit()).getCommitSha());
        }
        if (devopsEnvironmentDTO.getDevopsSyncCommit() != null) {
            envSyncStatusDTO.setDevopsSyncCommit(devopsEnvCommitService
                    .baseQuery(devopsEnvironmentDTO.getDevopsSyncCommit())
                    .getCommitSha());
        }
        if (devopsEnvironmentDTO.getSagaSyncCommit() != null) {
            envSyncStatusDTO.setSagaSyncCommit(devopsEnvCommitService
                    .baseQuery(devopsEnvironmentDTO.getSagaSyncCommit()).getCommitSha());
        }

        gitlabUrl = gitlabUrl.endsWith("/") ? gitlabUrl.substring(0, gitlabUrl.length() - 1) : gitlabUrl;
        envSyncStatusDTO.setCommitUrl(String.format("%s/%s-%s-gitops/%s/commit/",
                gitlabUrl, organizationDTO.getTenantNum(), projectDTO.getDevopsComponentCode(),
                devopsEnvironmentDTO.getCode()));
        return envSyncStatusDTO;
    }

    @Override
    public Page<DevopsUserPermissionVO> pageUserPermissionByEnvId(Long projectId, PageRequest
            pageable, String params, Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(envId);

        RoleAssignmentSearchVO roleAssignmentSearchVO = new RoleAssignmentSearchVO();
        roleAssignmentSearchVO.setEnabled(true);
        Map<String, Object> searchParamMap = null;
        List<String> paramList = null;
        // 处理搜索参数
        if (!StringUtils.isEmpty(params)) {
            Map maps = gson.fromJson(params, Map.class);
            searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
            paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
            roleAssignmentSearchVO.setParam(paramList == null ? null : paramList.toArray(new String[0]));
            if (searchParamMap != null) {
                if (searchParamMap.get(LOGIN_NAME) != null) {
                    String loginName = TypeUtil.objToString(searchParamMap.get(LOGIN_NAME));
                    roleAssignmentSearchVO.setLoginName(loginName);
                }
                if (searchParamMap.get(REAL_NAME) != null) {
                    String realName = TypeUtil.objToString(searchParamMap.get(REAL_NAME));
                    roleAssignmentSearchVO.setRealName(realName);
                }
            }
        }

        // 根据搜索参数查询所有的项目所有者
        List<DevopsUserPermissionVO> projectOwners = ConvertUtils.convertList(baseServiceClientOperator.listUsersWithGitlabLabel(projectId, roleAssignmentSearchVO, LabelType.GITLAB_PROJECT_OWNER.getValue()),
                iamUserDTO -> DevopsUserPermissionVO.iamUserTOUserPermissionVO(iamUserDTO, true));
        List<DevopsUserPermissionVO> projectMembers = ConvertUtils.convertList(baseServiceClientOperator.listUsersWithGitlabLabel(projectId, roleAssignmentSearchVO, LabelType.GITLAB_PROJECT_DEVELOPER.getValue()),
                iamUserDTO -> DevopsUserPermissionVO.iamUserTOUserPermissionVO(iamUserDTO, false));
        if (!devopsEnvironmentDTO.getSkipCheckPermission()) {
            // 根据搜索参数查询数据库中所有的环境权限分配数据
            List<DevopsEnvUserPermissionDTO> devopsEnvUserPermissionDTOS = devopsEnvUserPermissionMapper.listUserEnvPermissionByOption(envId, searchParamMap, paramList);
            List<Long> permissions = devopsEnvUserPermissionDTOS.stream().map(DevopsEnvUserPermissionDTO::getIamUserId).collect(Collectors.toList());

            Set<Long> userIds = projectMembers.stream().map(DevopsUserVO::getIamUserId).collect(Collectors.toSet());
            Map<Long, Boolean> userGitlabProjectOwnerMap = baseServiceClientOperator.checkUsersAreGitlabProjectOwner(userIds, projectId);

            projectMembers = projectMembers
                    .stream()
                    .filter(member -> permissions.contains(member.getIamUserId()) || Boolean.TRUE.equals(userGitlabProjectOwnerMap.get(member.getIamUserId())))
                    .collect(Collectors.toList());
            projectMembers.forEach(devopsUserPermissionVO -> {
                if (permissions.contains(devopsUserPermissionVO.getIamUserId())) {
                    devopsEnvUserPermissionDTOS.forEach(devopsEnvUserPermissionDTO -> {
                        if (devopsEnvUserPermissionDTO.getIamUserId().equals(devopsUserPermissionVO.getIamUserId())) {
                            devopsUserPermissionVO.setCreationDate(devopsEnvUserPermissionDTO.getCreationDate());
                        }
                    });
                }
            });
        }
        return DevopsUserPermissionVO.combineOwnerAndMember(projectMembers, projectOwners, pageable, devopsEnvironmentDTO.getCreatedBy());
    }

    @Override
    public Page<DevopsUserVO> listNonRelatedMembers(Long projectId, Long envId, Long selectedIamUserId, PageRequest pageable, String params) {
        RoleAssignmentSearchVO roleAssignmentSearchVO = new RoleAssignmentSearchVO();
        roleAssignmentSearchVO.setEnabled(true);
        // 处理搜索参数
        if (!StringUtils.isEmpty(params)) {
            Map maps = gson.fromJson(params, Map.class);
            Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
            List<String> paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));

            roleAssignmentSearchVO.setParam(paramList == null ? null : paramList.toArray(new String[0]));
            if (searchParamMap.get(LOGIN_NAME) != null) {
                String loginName = TypeUtil.objToString(searchParamMap.get(LOGIN_NAME));
                roleAssignmentSearchVO.setLoginName(loginName);
            }

            if (searchParamMap.get(REAL_NAME) != null) {
                String realName = TypeUtil.objToString(searchParamMap.get(REAL_NAME));
                roleAssignmentSearchVO.setRealName(realName);
            }
        }

        // 根据参数搜索所有的项目成员
        List<IamUserDTO> allProjectMembers = baseServiceClientOperator.listUsersWithGitlabLabel(projectId, roleAssignmentSearchVO, LabelType.GITLAB_PROJECT_DEVELOPER.getValue());
        if (allProjectMembers.isEmpty()) {
            Page<DevopsUserVO> pageInfo = new Page<>();
            pageInfo.setContent(new ArrayList<>());
            return pageInfo;
        }

        // 获取项目下所有的项目所有者（带上搜索参数搜索可以获得更精确的结果）
        List<Long> allProjectOwnerIds = baseServiceClientOperator.listUsersWithGitlabLabel(projectId, roleAssignmentSearchVO, LabelType.GITLAB_PROJECT_OWNER.getValue())
                .stream()
                .map(IamUserDTO::getId)
                .collect(Collectors.toList());

        // 数据库中已被分配权限的
        List<Long> assigned = devopsEnvUserPermissionMapper.listUserIdsByEnvId(envId);

        // 过滤项目成员中的项目所有者和已被分配权限的
        List<IamUserDTO> members = allProjectMembers.stream()
                .filter(member -> !allProjectOwnerIds.contains(member.getId()))
                .filter(member -> !assigned.contains(member.getId()))
                .collect(Collectors.toList());

        if (selectedIamUserId != null) {
            IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(selectedIamUserId);
            if (!members.isEmpty()) {
                members.remove(iamUserDTO);
                members.add(0, iamUserDTO);
            } else {
                members.add(iamUserDTO);
            }
        }

        Page<IamUserDTO> pageInfo = PageInfoUtil.createPageFromList(members, pageable);

        return ConvertUtils.convertPage(pageInfo, member -> new DevopsUserVO(member.getId(), member.getLdap() ? member.getLoginName() : member.getEmail(), member.getRealName(), member.getImageUrl()));
    }

    @Override
    public void deletePermissionOfUser(Long projectId, Long envId, Long userId) {
        if (envId == null || userId == null) {
            return;
        }

        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(envId);

        if (environmentDTO == null) {
            return;
        }

        CommonExAssertUtil.assertTrue(projectId.equals(environmentDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(userId);

        if (userAttrDTO == null) {
            return;
        }

        if (userAttrDTO.getGitlabUserId() == null) {
            throw new CommonException(ERROR_GITLAB_USER_SYNC_FAILED);
        }


        if (baseServiceClientOperator.isGitlabProjectOwner(userAttrDTO.getIamUserId(), projectId)) {
            throw new CommonException("error.delete.permission.of.project.owner");
        }

        // 删除数据库中的纪录
        DevopsEnvUserPermissionDTO devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionDTO();
        devopsEnvUserPermissionDTO.setEnvId(envId);
        devopsEnvUserPermissionDTO.setIamUserId(userId);
        devopsEnvUserPermissionMapper.delete(devopsEnvUserPermissionDTO);


        // 删除GitLab对应的项目的权限
        DevopsEnvUserPayload userPayload = new DevopsEnvUserPayload();
        userPayload.setIamProjectId(projectId);
        userPayload.setGitlabProjectId(environmentDTO.getGitlabEnvProjectId().intValue());
        userPayload.setEnvId(envId);
        userPayload.setAddGitlabUserIds(Collections.emptyList());
        // 待删除的用户
        userPayload.setDeleteGitlabUserIds(Collections.singletonList(userAttrDTO.getGitlabUserId().intValue()));
        userPayload.setDevopsEnvironmentDTO(environmentDTO);
        userPayload.setOption(3);

        // 发送saga进行相应的gitlab侧的数据处理
        producer.apply(
                StartSagaBuilder.newBuilder()
                        .withSourceId(projectId)
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("env")
                        .withRefId(String.valueOf(envId))
                        .withPayloadAndSerialize(userPayload)
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_UPDATE_ENV_PERMISSION),
                builder -> {
                });
    }

    @Override
    public List<DevopsUserVO> listAllUserPermission(Long envId) {
        return ConvertUtils.convertList(devopsEnvUserPermissionService.baseListByEnvId(envId), DevopsUserVO.class);
    }


    @Saga(code = SagaTopicCodeConstants.DEVOPS_UPDATE_ENV_PERMISSION, description = "更新环境的权限")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateEnvUserPermission(Long projectId, DevopsEnvPermissionUpdateVO devopsEnvPermissionUpdateVO) {
        DevopsEnvironmentDTO preEnvironmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(devopsEnvPermissionUpdateVO.getEnvId());
        CommonExAssertUtil.assertTrue(projectId.equals(preEnvironmentDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);


        DevopsEnvUserPayload userPayload = new DevopsEnvUserPayload();
        userPayload.setEnvId(preEnvironmentDTO.getId());
        userPayload.setGitlabProjectId(preEnvironmentDTO.getGitlabEnvProjectId().intValue());
        userPayload.setIamProjectId(preEnvironmentDTO.getProjectId());
        userPayload.setIamUserIds(devopsEnvPermissionUpdateVO.getUserIds());
        userPayload.setDevopsEnvironmentDTO(preEnvironmentDTO);

        List<Long> addIamUserIds = devopsEnvPermissionUpdateVO.getUserIds();
        // 判断更新的情况
        if (preEnvironmentDTO.getSkipCheckPermission()) {
            if (devopsEnvPermissionUpdateVO.getSkipCheckPermission()) {
                return;
            } else {
                // 待添加的用户列表为空
                if (CollectionUtils.isEmpty(addIamUserIds)) {
                    return;
                }
                // 添加权限
                List<IamUserDTO> addIamUsers = baseServiceClientOperator.listUsersByIds(addIamUserIds);
                addIamUsers.forEach(e -> devopsEnvUserPermissionService.baseCreate(new DevopsEnvUserPermissionDTO(e.getLoginName(), e.getId(), e.getRealName(), preEnvironmentDTO.getId(), true)));

                userPayload.setOption(1);

                // 更新字段
                preEnvironmentDTO.setSkipCheckPermission(devopsEnvPermissionUpdateVO.getSkipCheckPermission());
                preEnvironmentDTO.setObjectVersionNumber(devopsEnvPermissionUpdateVO.getObjectVersionNumber());
                devopsEnvironmentMapper.updateByPrimaryKeySelective(preEnvironmentDTO);
            }
        } else {
            if (devopsEnvPermissionUpdateVO.getSkipCheckPermission()) {
                // 删除原先所有的分配情况
                devopsEnvUserPermissionService.deleteByEnvId(preEnvironmentDTO.getId());
                userPayload.setOption(2);

                // 更新字段
                preEnvironmentDTO.setSkipCheckPermission(devopsEnvPermissionUpdateVO.getSkipCheckPermission());
                preEnvironmentDTO.setObjectVersionNumber(devopsEnvPermissionUpdateVO.getObjectVersionNumber());
                devopsEnvironmentMapper.updateByPrimaryKeySelective(preEnvironmentDTO);
            } else {
                // 待添加的用户列表为空
                if (CollectionUtils.isEmpty(addIamUserIds)) {
                    return;
                }
                // 待添加的用户
                List<Integer> addGitlabUserIds = userAttrService.baseListByUserIds(addIamUserIds)
                        .stream()
                        .map(UserAttrDTO::getGitlabUserId)
                        .map(TypeUtil::objToInteger)
                        .collect(Collectors.toList());
                userPayload.setAddGitlabUserIds(addGitlabUserIds);
                userPayload.setDeleteGitlabUserIds(Collections.emptyList());

                devopsEnvUserPermissionService.baseUpdate(devopsEnvPermissionUpdateVO.getEnvId(), addIamUserIds, Collections.emptyList());

                userPayload.setOption(3);
            }
        }

        // 发送saga进行相应的gitlab侧的数据处理
        producer.apply(
                StartSagaBuilder.newBuilder()
                        .withSourceId(preEnvironmentDTO.getProjectId())
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("env")
                        .withRefId(String.valueOf(preEnvironmentDTO.getId()))
                        .withPayloadAndSerialize(userPayload)
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_UPDATE_ENV_PERMISSION),
                builder -> {
                });
    }


    private Page<UserVO> getMembersFromProject(PageRequest pageable, Long projectId, String searchParams) {
        RoleAssignmentSearchVO roleAssignmentSearchVO = new RoleAssignmentSearchVO();
        if (!StringUtils.isEmpty(searchParams)) {
            Map maps = gson.fromJson(searchParams, Map.class);
            Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
            List<String> param = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
            roleAssignmentSearchVO.setParam(param == null ? null : param.toArray(new String[0]));
            if (searchParamMap.get(LOGIN_NAME) != null) {
                String loginName = TypeUtil.objToString(searchParamMap.get(LOGIN_NAME));
                String subLogin = loginName.substring(1, loginName.length() - 1);
                roleAssignmentSearchVO.setLoginName(subLogin);
            }
            if (searchParamMap.get(REAL_NAME) != null) {
                String realName = TypeUtil.objToString(searchParamMap.get(REAL_NAME));
                String subReal = realName.substring(1, realName.length() - 1);
                roleAssignmentSearchVO.setRealName(subReal);
            }
        }
        // 所有项目成员，可能还带有项目所有者的角色
        List<IamUserDTO> allProjectMembers = baseServiceClientOperator.listUsersWithGitlabLabel(projectId, new RoleAssignmentSearchVO(), LabelType.GITLAB_PROJECT_DEVELOPER.getValue());
        // 所有项目所有者
        List<IamUserDTO> allProjectOwners = baseServiceClientOperator.listUsersWithGitlabLabel(projectId, new RoleAssignmentSearchVO(), LabelType.GITLAB_PROJECT_OWNER.getValue());
        //合并项目所有者和项目成员
        Set<IamUserDTO> iamUserDTOS = new HashSet<>(allProjectMembers);
        iamUserDTOS.addAll(allProjectOwners);

        if (iamUserDTOS.isEmpty()) {
            return ConvertUtils.convertPage(new Page<>(), UserVO.class);
        } else {
            iamUserDTOS.forEach(e -> e.setProjectOwner(allProjectOwners.contains(e)));
        }
        List<UserVO> iamUserVOS = ConvertUtils.convertList(iamUserDTOS, UserVO.class);
        return PageInfoUtil.createPageFromList(iamUserVOS, pageable);
    }

    private void setPermission(DevopsEnvironmentDTO devopsEnvironmentDTO, List<Long> permissionEnvIds,
                               Boolean isProjectOwner) {
        if (devopsEnvironmentDTO.getSkipCheckPermission()) {
            devopsEnvironmentDTO.setPermission(true);
        } else {
            devopsEnvironmentDTO.setPermission(permissionEnvIds.contains(devopsEnvironmentDTO.getId()) || isProjectOwner);
        }
    }

    @Saga(code = SagaTopicCodeConstants.DEVOPS_DELETE_ENV,
            description = "devops删除停用和失败的环境")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeactivatedOrFailedEnvironment(Long projectId, Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(envId);
        CommonExAssertUtil.assertNotNull(devopsEnvironmentDTO, "error.env.id.not.exist", envId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsEnvironmentDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();
        //排除掉运行中的环境
        if (Boolean.TRUE.equals(devopsEnvironmentDTO.getActive()) && Boolean.FALSE.equals(devopsEnvironmentDTO.getFailed()) && upgradeClusterList.contains(devopsEnvironmentDTO.getClusterId())) {
            throw new CommonException("error.env.delete");
        }

        if (!CollectionUtils.isEmpty(devopsCdEnvDeployInfoService.queryCurrentByEnvId(envId))) {
            throw new CommonException("error.delete.env.with.pipeline");
        }

        devopsEnvironmentDTO.setSynchro(Boolean.FALSE);
        JSONObject JSONObject = new JSONObject();
        JSONObject.put("envId", envId);
        devopsEnvironmentMapper.updateByPrimaryKeySelective(devopsEnvironmentDTO);
        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("env")
                        .withRefId(String.valueOf(envId))
                        .withJson(JSONObject.toString())
                        .withSourceId(projectId)
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_DELETE_ENV),
                builder -> {
                });
    }

    @Override
    public void deleteEnvSaga(Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(envId);
        if (devopsEnvironmentDTO == null) {
            LogUtil.loggerInfoObjectNullWithId("env", envId, LOGGER);
            LOGGER.info("Delete env: environment with id {} is skipped", envId);
            return;
        }

        // 删除对应的环境-应用服务关联关系
        DevopsEnvAppServiceDTO deleteCondition = new DevopsEnvAppServiceDTO();
        deleteCondition.setEnvId(envId);
        devopsEnvAppServiceMapper.delete(deleteCondition);

        // 删除环境对应的实例
        appServiceInstanceService.baseListByEnvId(envId).forEach(instanceE ->
                devopsEnvCommandService.baseListByObject(ObjectType.INSTANCE.getType(), instanceE.getId()).forEach(t -> devopsEnvCommandService.baseDeleteByEnvCommandId(t)));
        appServiceInstanceService.deleteByEnvId(envId);

        // 删除环境对应的域名、域名路径
        devopsIngressService.baseListByEnvId(envId).forEach(ingressE ->
                devopsEnvCommandService.baseListByObject(ObjectType.INGRESS.getType(), ingressE.getId()).forEach(t -> devopsEnvCommandService.baseDeleteByEnvCommandId(t)));
        devopsIngressService.deleteIngressAndIngressPathByEnvId(envId);

        // 删除环境对应的网络和网络实例
        devopsServiceService.baseListByEnvId(envId).forEach(serviceE ->
                devopsEnvCommandService.baseListByObject(ObjectType.SERVICE.getType(), serviceE.getId()).forEach(t -> devopsEnvCommandService.baseDeleteByEnvCommandId(t)));
        devopsServiceService.baseDeleteServiceAndInstanceByEnvId(envId);

        // 删除实例对应的部署纪录
        devopsDeployRecordService.deleteRecordByEnv(envId);

        // 删除环境对应的secret
        devopsSecretService.baseListByEnv(envId).forEach(secretE ->
                devopsEnvCommandService.baseListByObject(ObjectType.SECRET.getType(), secretE.getId()).forEach(t -> devopsEnvCommandService.baseDeleteByEnvCommandId(t)));
        devopsSecretService.baseDeleteSecretByEnvId(envId);

        // 删除环境对应的configMap
        devopsConfigMapService.baseListByEnv(envId).forEach(configMapE ->
                devopsEnvCommandService.baseListByObject(ObjectType.CONFIGMAP.getType(), configMapE.getId()).forEach(t -> devopsEnvCommandService.baseDeleteByEnvCommandId(t)));
        devopsConfigMapService.baseDeleteByEnvId(envId);

        // 删除环境对应的自定义资源
        devopsCustomizeResourceService.baseListByEnvId(envId).forEach(customE ->
                devopsEnvCommandService.baseListByObject(ObjectType.CUSTOM.getType(), customE.getId()).forEach(t -> devopsEnvCommandService.baseDeleteByEnvCommandId(t)));
        devopsCustomizeResourceService.baseDeleteCustomizeResourceByEnvId(envId);
        // 删除PVC
        devopsCustomizeResourceService.baseListByEnvId(envId).forEach(pvcE ->
                devopsEnvCommandService.baseListByObject(ObjectType.PERSISTENTVOLUMECLAIM.getType(), pvcE.getId()).forEach(t -> devopsEnvCommandService.baseDeleteByEnvCommandId(t)));
        devopsPvcService.baseDeleteByEnvId(envId);

        // 删除环境关联的部署配置
        devopsDeployValueService.deleteByEnvId(envId);

        // 删除RegistrySecret
        devopsRegistrySecretService.deleteByEnvId(envId);

        // 删除文件解析相关纪录
        devopsEnvFileService.deleteByEnvId(envId);
        devopsEnvFileResourceService.deleteByEnvId(envId);
        devopsEnvFileErrorService.deleteByEnvId(envId);

        // 删除polaris扫描相关的数据
        polarisScanningService.deleteAllByScopeAndScopeId(PolarisScopeType.ENV, envId);

        // 删除gitlab库, 删除之前查询是否存在
        if (devopsEnvironmentDTO.getGitlabEnvProjectId() != null) {
            Integer gitlabProjectId = TypeUtil.objToInt(devopsEnvironmentDTO.getGitlabEnvProjectId());

            DevopsEnvironmentDTO condition = new DevopsEnvironmentDTO();
            condition.setGitlabEnvProjectId(devopsEnvironmentDTO.getGitlabEnvProjectId());
            // 断言这个gitlab仓库只有这个环境关联（大多数情况下，这个判断是多余的，排除可能的脏数据）
            if (devopsEnvironmentMapper.selectCount(condition) == 1) {
                GitlabProjectDTO gitlabProjectDO = gitlabServiceClientOperator.queryProjectById(gitlabProjectId);
                if (gitlabProjectDO != null && gitlabProjectDO.getId() != null) {
                    UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
                    Integer gitlabUserId = TypeUtil.objToInt(userAttrDTO.getGitlabUserId());
                    gitlabServiceClientOperator.deleteProjectById(gitlabProjectId, gitlabUserId);
                    LOGGER.info("Successfully delete gitlab project {} for env with id {}", gitlabProjectId, devopsEnvironmentDTO.getId());
                }
            } else {
                LOGGER.warn("The gitlab project id {} is associated with other environment, so skip...", gitlabProjectId);
            }
        }

        // 删除环境
        baseDeleteById(envId);

        //更新集群关联的namespaces数据
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(devopsEnvironmentDTO.getClusterId());
        if (devopsClusterDTO != null && devopsClusterDTO.getNamespaces() != null) {
            List<String> namespaces = JSONArray.parseArray(devopsClusterDTO.getNamespaces(), String.class);
            namespaces.remove(devopsEnvironmentDTO.getCode());
            devopsClusterDTO.setNamespaces((JSONArray.toJSONString(namespaces)));
            devopsClusterService.baseUpdate(null, devopsClusterDTO);
        }


        // 删除环境命名空间
        if (devopsEnvironmentDTO.getClusterId() != null) {
            agentCommandService.deleteEnv(envId, devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getClusterId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public DevopsEnvironmentDTO createSystemEnv(Long clusterId) {
        // 因为创建系统环境需要一些时间，要考虑并发的情况，所以给特定的cluster纪录加上行锁
        DevopsClusterDTO cluster = devopsClusterMapper.queryClusterForUpdate(clusterId);

        if (cluster == null) {
            throw new CommonException("error.cluster.not.exists");
        }

        if (cluster.getSystemEnvId() != null) {
            return baseQueryById(cluster.getSystemEnvId());
        }

        Long projectId = cluster.getProjectId();

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        if (userAttrDTO == null) {
            throw new CommonException(ERROR_GITLAB_USER_SYNC_FAILED);
        }

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
        // 查询所在的gitlab环境组
        if (devopsProjectDTO.getDevopsClusterEnvGroupId() == null) {
            // 如果是0.20版本之前创建的项目，是没有这个GitLab组的，此时创建
            gitlabGroupService.createClusterEnvGroup(projectDTO, organizationDTO, userAttrDTO);

            devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
        }

        String envCode = GitOpsUtil.getSystemEnvCode(cluster.getCode());

        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        // 创建集群环境时默认不跳过权限校验
        devopsEnvironmentDTO.setSkipCheckPermission(Boolean.FALSE);
        devopsEnvironmentDTO.setName(String.format(SYSTEM_ENV_NAME, cluster.getName()));
        devopsEnvironmentDTO.setCode(envCode);
        devopsEnvironmentDTO.setType(EnvironmentType.SYSTEM.getValue());
        devopsEnvironmentDTO.setActive(true);
        devopsEnvironmentDTO.setSynchro(false);
        devopsEnvironmentDTO.setFailed(false);
        devopsEnvironmentDTO.setClusterId(clusterId);
        devopsEnvironmentDTO.setToken(GenerateUUID.generateUUID());
        devopsEnvironmentDTO.setProjectId(projectId);

        boolean isGitlabRoot = false;

        if (Boolean.TRUE.equals(userAttrDTO.getGitlabAdmin())) {
            // 如果这边表存了gitlabAdmin这个字段,那么gitlabUserId就不会为空,所以不判断此字段为空
            isGitlabRoot = gitlabServiceClientOperator.isGitlabAdmin(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }

        if (!isGitlabRoot) {
            MemberDTO memberDTO = gitlabServiceClientOperator.queryGroupMember(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsClusterEnvGroupId()),
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            if (memberDTO == null || !memberDTO.getAccessLevel().equals(AccessLevel.OWNER.toValue())) {
                throw new CommonException("error.user.not.owner");
            }
        }

        // 生成deployKey
        List<String> sshKeys = FileUtil.getSshKey(
                organizationDTO.getTenantNum() + "/" + projectDTO.getDevopsComponentCode() + "/" + devopsEnvironmentDTO.getCode());
        devopsEnvironmentDTO.setEnvIdRsa(sshKeys.get(0));
        devopsEnvironmentDTO.setEnvIdRsaPub(sshKeys.get(1));

        // 创建环境纪录
        Long envId = baseCreate(devopsEnvironmentDTO).getId();
        devopsEnvironmentDTO.setId(envId);

        // 准备创建GitLab的项目
        Integer gitlabUserId = TypeUtil.objToInteger(userAttrDTO.getGitlabUserId());
        GitlabProjectDTO gitlabProjectDO = gitlabServiceClientOperator.queryProjectByName(
                GitOpsUtil.renderGroupPath(organizationDTO.getTenantNum(),
                        projectDTO.getDevopsComponentCode(),
                        GitOpsConstants.CLUSTER_ENV_GROUP_SUFFIX),
                envCode,
                gitlabUserId,
                false);
        if (gitlabProjectDO == null || gitlabProjectDO.getId() == null) {
            gitlabProjectDO = gitlabServiceClientOperator.createProject(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsClusterEnvGroupId()),
                    envCode,
                    gitlabUserId,
                    false);
        } else {
            checkGitlabProjectIdNotUsedBefore(TypeUtil.objToLong(gitlabProjectDO.getId()));
        }
        devopsEnvironmentDTO.setGitlabEnvProjectId(TypeUtil.objToLong(gitlabProjectDO.getId()));
        if (gitlabServiceClientOperator.listDeployKey(gitlabProjectDO.getId(), gitlabUserId).isEmpty()) {
            gitlabServiceClientOperator.createDeployKey(
                    gitlabProjectDO.getId(),
                    devopsEnvironmentDTO.getCode(),
                    devopsEnvironmentDTO.getEnvIdRsaPub(),
                    true,
                    GitUserNameUtil.getAdminId()
            );
        }

        // 初始化web hook
        ProjectHookDTO projectHookDTO = ProjectHookDTO.allHook();
        projectHookDTO.setEnableSslVerification(true);
        projectHookDTO.setProjectId(gitlabProjectDO.getId());
        projectHookDTO.setToken(devopsEnvironmentDTO.getToken());
        projectHookDTO.setUrl(gitOpsWebHookUrl);
        List<ProjectHookDTO> projectHookDTOS = gitlabServiceClientOperator.listProjectHook(gitlabProjectDO.getId(),
                gitlabUserId);
        if (projectHookDTOS == null || projectHookDTOS.isEmpty()) {
            devopsEnvironmentDTO.setHookId(TypeUtil.objToLong(gitlabServiceClientOperator.createWebHook(
                    gitlabProjectDO.getId(), gitlabUserId, projectHookDTO).getId()));
        } else {
            devopsEnvironmentDTO.setHookId(TypeUtil.objToLong(projectHookDTOS.get(0).getId()));
        }

        // 初始化环境库，添加一个readme
        if (!gitlabServiceClientOperator.getFile(gitlabProjectDO.getId(), MASTER, README)) {
            gitlabServiceClientOperator.createFile(gitlabProjectDO.getId(),
                    README, README_CONTENT, "ADD README", gitlabUserId);
        }

        // 创建集群环境时不需要项目所有者初始化用户权限，因为有组的权限了

        devopsEnvironmentDTO.setSynchro(true);
        baseUpdate(devopsEnvironmentDTO);

        agentCommandService.initEnv(devopsEnvironmentDTO, devopsEnvironmentDTO.getClusterId());
        return devopsEnvironmentDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void deleteSystemEnv(Long projectId, Long clusterId, String clusterCode, Long envId) {
        if (envId != null) {
            deleteEnvSaga(envId);
        } else {
            // 可能是gitlab项目创建成功，但是数据库纪录被回滚了，这时候判断gitlab是否有对应项目
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
            Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

            UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            String systemEnvProjectCode = GitOpsUtil.getSystemEnvCode(clusterCode);
            Integer gitlabUserId = TypeUtil.objToInteger(userAttrDTO.getGitlabUserId());
            GitlabProjectDTO gitlabProjectDO = gitlabServiceClientOperator.queryProjectByName(
                    GitOpsUtil.renderGroupPath(organizationDTO.getTenantNum(),
                            projectDTO.getDevopsComponentCode(),
                            GitOpsConstants.CLUSTER_ENV_GROUP_SUFFIX),
                    systemEnvProjectCode,
                    gitlabUserId,
                    false);
            if (gitlabProjectDO != null && gitlabProjectDO.getId() != null) {
                DevopsEnvironmentDTO condition = new DevopsEnvironmentDTO();
                condition.setGitlabEnvProjectId(TypeUtil.objToLong(gitlabProjectDO.getId()));
                // 确保只有gitlab项目没有关联到某个环境（这种情况一般不会出现）
                if (devopsEnvironmentMapper.selectCount(condition) == 0) {
                    gitlabServiceClientOperator.deleteProjectById(gitlabProjectDO.getId(), gitlabUserId);
                    LOGGER.info("Successfully delete gitlab project {} for system env with cluster id {}", gitlabProjectDO.getId(), clusterId);
                }
            }
        }
    }

    @Override
    public DevopsEnvironmentDTO queryByTokenWithClusterCode(String token) {
        return devopsEnvironmentMapper.queryByTokenWithClusterCode(token);
    }

    @Override
    public List<DevopsEnvironmentDTO> listByProjectIdAndName(Long projectId, String envName) {
        return devopsEnvironmentMapper.listByProjectIdAndName(projectId, envName);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void updateDevopsEnvGroupIdNullByProjectIdAndGroupId(Long projectId, Long envGroupId) {
        devopsEnvironmentMapper.updateDevopsEnvGroupIdNullByProjectIdAndGroupId(Objects.requireNonNull(projectId), Objects.requireNonNull(envGroupId));
    }

    @Override
    public Long countEnvByOption(final Long projectId, @Nullable Long clusterId, @Nullable Boolean isFailed) {
        // 如果集群id有值，projectId就不传值
        final Long projectIdValue = clusterId == null ? projectId : null;
        return (long) devopsEnvironmentMapper.countByOptions(clusterId, projectIdValue, isFailed, EnvironmentType.USER.getValue());
    }

    @Override
    public List<DevopsClusterRepVO> listDevopsCluster(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        List<DevopsClusterRepVO> devopsClusterRepVOS = ConvertUtils.convertList(devopsClusterService.baseListByProjectId(projectId, projectDTO.getOrganizationId()), DevopsClusterRepVO.class);
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();
        devopsClusterRepVOS.forEach(t -> {
            if (upgradeClusterList.contains(t.getId())) {
                t.setConnect(true);
                if (t.getStatus().equalsIgnoreCase(ClusterStatusEnum.DISCONNECT.value())) {
                    t.setStatus(ClusterStatusEnum.RUNNING.value());
                }
            }
        });
        return devopsClusterRepVOS;
    }

    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_SET_ENV_ERR,
            description = "devops创建环境失败(devops set env status create err)", inputSchemaClass = GitlabProjectPayload.class)
    public void setEnvErrStatus(String data, Long projectId) {
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(projectId)
                        .withRefType("")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_SET_ENV_ERR),
                builder -> builder
                        .withJson(data)
                        .withRefId("")
                        .withSourceId(projectId));

    }

    @Override
    public DevopsEnvironmentRepVO queryByCode(Long projectId, String code) {
        return ConvertUtils.convertObject(baseQueryByProjectIdAndCode(projectId, code), DevopsEnvironmentRepVO.class);
    }


    @Override
    public DevopsEnvironmentDTO baseCreate(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        if (devopsEnvironmentMapper.insert(devopsEnvironmentDTO) != 1) {
            throw new CommonException("error.environment.create");
        }
        return devopsEnvironmentDTO;
    }

    @Override
    public DevopsEnvironmentDTO baseQueryById(Long id) {
        return devopsEnvironmentMapper.queryByIdWithClusterCode(id);
    }

    @Override
    public DevopsEnvironmentDTO baseUpdate(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        if (devopsEnvironmentDTO.getDevopsEnvGroupId() == null) {
            devopsEnvironmentMapper.updateDevopsEnvGroupId(devopsEnvironmentDTO.getId());
        }
        devopsEnvironmentDTO.setObjectVersionNumber(devopsEnvironmentMapper.selectByPrimaryKey(
                devopsEnvironmentDTO.getId()).getObjectVersionNumber());
        if (devopsEnvironmentMapper.updateByPrimaryKeySelective(devopsEnvironmentDTO) != 1) {
            DevopsEnvironmentDTO devopsEnvironmentDTO1 = devopsEnvironmentMapper.selectByPrimaryKey(devopsEnvironmentDTO.getId());
            LOGGER.error("\nenvName:{},envType:{},\nisActive:{},isConnected:{},isSynchro:{},isFailed:{}\nsagaCommit:{},devopsCommit:{},agentCommit:{},",
                    devopsEnvironmentDTO1.getName(), devopsEnvironmentDTO1.getType(),
                    devopsEnvironmentDTO1.getActive(), devopsEnvironmentDTO1.getConnected(), devopsEnvironmentDTO1.getSynchro(), devopsEnvironmentDTO1.getFailed(),
                    devopsEnvironmentDTO1.getSagaSyncCommit(),
                    devopsEnvironmentDTO1.getDevopsSyncCommit(),
                    devopsEnvironmentDTO1.getAgentSyncCommit());
            throw new CommonException("error.environment.update");
        }
        return devopsEnvironmentDTO;
    }

    /**
     * 校验了集群下code唯一和校验了项目下code唯一
     */
    @Override
    public void baseCheckCode(Long projectId, Long clusterId, String code) {
        if (!isCodeUniqueInClusterAndProject(projectId, clusterId, code)) {
            throw new CommonException(ERROR_CODE_EXIST);
        }
    }

    private boolean isCodeUniqueInClusterAndProject(Long projectId, Long clusterId, String code) {
        DevopsEnvironmentDTO environmentDTO = new DevopsEnvironmentDTO();
        environmentDTO.setClusterId(Objects.requireNonNull(clusterId));
        environmentDTO.setCode(Objects.requireNonNull(code));
        if (devopsEnvironmentMapper.selectCount(environmentDTO) != 0) {
            return false;
        }

        environmentDTO.setClusterId(null);
        environmentDTO.setProjectId(Objects.requireNonNull(projectId));
        environmentDTO.setType(EnvironmentType.USER.getValue());
        return devopsEnvironmentMapper.selectCount(environmentDTO) == 0;
    }

    @Override
    public List<DevopsEnvironmentDTO> baseListByProjectId(Long projectId) {
        DevopsEnvironmentDTO environmentDTO = new DevopsEnvironmentDTO();
        environmentDTO.setProjectId(projectId);
        return devopsEnvironmentMapper.select(environmentDTO);
    }

    @Override
    public List<DevopsEnvironmentDTO> baseListByProjectIdAndActive(Long projectId, Boolean active) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setProjectId(projectId);
        devopsEnvironmentDTO.setActive(active);
        devopsEnvironmentDTO.setType(EnvironmentType.USER.getValue());
        return devopsEnvironmentMapper.select(devopsEnvironmentDTO);
    }

    @Override
    public DevopsEnvironmentDTO baseQueryByClusterIdAndCode(Long clusterId, String code) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setClusterId(Objects.requireNonNull(clusterId));
        devopsEnvironmentDTO.setCode(Objects.requireNonNull(code));
        return devopsEnvironmentMapper.selectOne(devopsEnvironmentDTO);
    }

    @Override
    public DevopsEnvironmentDTO baseQueryByProjectIdAndCode(Long projectId, String code) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setProjectId(projectId);
        devopsEnvironmentDTO.setCode(code);
        return devopsEnvironmentMapper.selectOne(devopsEnvironmentDTO);
    }

    @Override
    public DevopsEnvironmentDTO baseQueryByToken(String token) {
        return devopsEnvironmentMapper.queryByToken(token);
    }

    @Override
    public void baseUpdateSagaSyncEnvCommit(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        devopsEnvironmentMapper.updateSagaSyncEnvCommit(devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getSagaSyncCommit());
    }

    @Override
    public void baseUpdateDevopsSyncEnvCommit(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        devopsEnvironmentMapper.updateDevopsSyncEnvCommit(devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getDevopsSyncCommit());
    }

    @Override
    public void baseUpdateAgentSyncEnvCommit(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        devopsEnvironmentMapper.updateAgentSyncEnvCommit(devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getAgentSyncCommit());
    }


    @Override
    public void baseDeleteById(Long id) {
        devopsEnvironmentMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<DevopsEnvironmentDTO> baseListUserEnvByClusterId(Long clusterId) {
        DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO();
        devopsEnvironmentDO.setClusterId(clusterId);
        devopsEnvironmentDO.setType(EnvironmentType.USER.getValue());
        return devopsEnvironmentMapper.select(devopsEnvironmentDO);
    }

    @Override
    public List<DevopsEnvironmentDTO> listEnvWithInstancesByClusterIdForAgent(Long clusterId) {
        return devopsEnvironmentMapper.listEnvWithInstancesByClusterIdForAgent(clusterId);
    }

    @Override
    public List<DevopsEnvironmentDTO> baseListByIds(List<Long> envIds) {
        return devopsEnvironmentMapper.listByIds(envIds);
    }

    @Override
    public Boolean disableCheck(Long projectId, Long envId) {
        // 停用环境校验资源和流水线
        // pipeLineAppDeploy为空
        boolean pipeLineAppDeployEmpty = CollectionUtils.isEmpty(devopsCdEnvDeployInfoService.queryCurrentByEnvId(envId));

        DevopsEnvResourceCountVO devopsEnvResourceCountVO = devopsEnvironmentMapper.queryEnvResourceCount(envId);

        return devopsEnvResourceCountVO.getRunningInstanceCount() == 0
                && devopsEnvResourceCountVO.getServiceCount() == 0
                && devopsEnvResourceCountVO.getIngressCount() == 0
                && devopsEnvResourceCountVO.getCertificationCount() == 0
                && devopsEnvResourceCountVO.getSecretCount() == 0
                && devopsEnvResourceCountVO.getConfigMapCount() == 0
                && devopsEnvResourceCountVO.getPvcCount() == 0
                && devopsEnvResourceCountVO.getCustomCount() == 0
                && pipeLineAppDeployEmpty;
    }

    @Override
    public Boolean deleteCheck(Long projectId, Long envId) {
        // 删除环境只校验是否有流水线
        return CollectionUtils.isEmpty(devopsCdEnvDeployInfoService.queryCurrentByEnvId(envId));
    }

    @Override
    public EnvironmentMsgVO checkExist(Long projectId, Long envId, Long objectId, String type) {
        // type为null表示查询环境是否存在
        EnvironmentMsgVO environmentMsgVO = new EnvironmentMsgVO(false, false, false);
        if (type == null) {
            if (devopsEnvironmentMapper.selectByPrimaryKey(envId) != null) {
                environmentMsgVO.setCheckEnvExist(true);
            }
            return environmentMsgVO;
        }
        // type为app表示查询应用服务是否存在
        if ("app".equals(type)) {
            DevopsEnvAppServiceDTO devopsEnvAppServiceDTO = new DevopsEnvAppServiceDTO();
            devopsEnvAppServiceDTO.setEnvId(envId);
            devopsEnvAppServiceDTO.setAppServiceId(objectId);
            if (devopsEnvAppServiceMapper.selectOne(devopsEnvAppServiceDTO) != null) {
                environmentMsgVO.setCheckAppExist(true);
            }
            return environmentMsgVO;
        }
        boolean check = false;
        ObjectType objectType = ObjectType.valueOf(type.toUpperCase());
        switch (objectType) {
            case INSTANCE:
                if (appServiceInstanceMapper.countNonDeletedInstancesWithEnv(envId, objectId) == 1) {
                    check = true;
                }
                break;
            case SERVICE:
                //状态不为删除时返回true,即选出为删除状态的实例数量为0返回true
                if (devopsServiceMapper.countNonDeletedServiceWithEnv(envId, objectId) == 1) {
                    check = true;
                }
                break;
            case CONFIGMAP:
                DevopsConfigMapDTO devopsConfigMapDTO = new DevopsConfigMapDTO();
                devopsConfigMapDTO.setEnvId(envId);
                devopsConfigMapDTO.setId(objectId);
                if (devopsConfigMapMapper.selectCount(devopsConfigMapDTO) == 1) {
                    check = true;
                }
                break;
            case INGRESS:
                DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
                devopsIngressDTO.setEnvId(envId);
                devopsIngressDTO.setId(objectId);
                if (devopsIngressMapper.selectCount(devopsIngressDTO) == 1) {
                    check = true;
                }
                break;
            case CERTIFICATE:
                CertificationDTO certificationDTO = new CertificationDTO();
                certificationDTO.setEnvId(envId);
                certificationDTO.setId(objectId);
                if (devopsCertificationMapper.selectCount(certificationDTO) == 1) {
                    check = true;
                }
                break;
            case SECRET:
                DevopsSecretDTO devopsSecretDTO = new DevopsSecretDTO();
                devopsSecretDTO.setEnvId(envId);
                devopsSecretDTO.setId(objectId);
                if (devopsSecretMapper.selectCount(devopsSecretDTO) == 1) {
                    check = true;
                }
                break;
            case CUSTOM:
                DevopsCustomizeResourceDTO devopsCustomizeResourceDTO = new DevopsCustomizeResourceDTO();
                devopsCustomizeResourceDTO.setEnvId(envId);
                devopsCustomizeResourceDTO.setId(objectId);
                if (devopsCustomizeResourceMapper.selectCount(devopsCustomizeResourceDTO) == 1) {
                    check = true;
                }
                break;
            default:
                break;
        }
        environmentMsgVO.setCheckResources(check);
        return environmentMsgVO;
    }

    @Override
    public void updateAutoDeploy(Long projectId, @Nullable Long envId, @Nullable Boolean isAutoDeploy) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, envId);
        devopsEnvironmentDTO.setAutoDeploy(isAutoDeploy);
        if (devopsEnvironmentMapper.updateByPrimaryKey(devopsEnvironmentDTO) != 1) {
            throw new CommonException("error.update.env");
        }
    }

    @Override
    public EnvAutoDeployVO queryAutoDeploy(Long projectId, @Nullable Long envId) {
        EnvAutoDeployVO envAutoDeployVO = new EnvAutoDeployVO();
        envAutoDeployVO.setExistAutoDeploy(false);
        List<DevopsCdEnvDeployInfoDTO> list = devopsCdEnvDeployInfoService.queryCurrentByEnvId(envId);
        if (!CollectionUtils.isEmpty(list)) {
            envAutoDeployVO.setExistAutoDeploy(true);
        }
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, envId);
        envAutoDeployVO.setAutoDeployStatus(devopsEnvironmentDTO.getAutoDeploy());
        return envAutoDeployVO;
    }

    @Override
    public DevopsEnvironmentDTO getProjectEnvironment(Long projectId, Long envId) {
        // 查询环境
        DevopsEnvironmentDTO devopsEnvironmentDTO = baseQueryById(envId);
        CommonExAssertUtil.assertNotNull(devopsEnvironmentDTO, "error.env.id.not.exist", envId);
        // 校验环境和项目匹配
        CommonExAssertUtil.assertTrue(projectId.equals(devopsEnvironmentDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        return devopsEnvironmentDTO;
    }
}
