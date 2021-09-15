package io.choerodon.devops.app.service.impl;

import java.util.*;

import org.hzero.core.base.BaseConstants;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.validator.DevopsHostAdditionalCheckValidator;
import io.choerodon.devops.api.vo.PipelineInstanceReferenceVO;
import io.choerodon.devops.api.vo.deploy.CustomDeployVO;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.FileInfoVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.api.vo.host.DevopsHostAppVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.api.vo.host.InstanceProcessInfoVO;
import io.choerodon.devops.api.vo.market.JarReleaseConfigVO;
import io.choerodon.devops.api.vo.market.MarketDeployObjectInfoVO;
import io.choerodon.devops.api.vo.market.MarketMavenConfigVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DevopsHostAppDTO;
import io.choerodon.devops.infra.dto.DevopsHostAppInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.JavaDeployDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.AppCenterDeployWayEnum;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.enums.deploy.OperationTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.DevopsHostAppMapper;
import io.choerodon.devops.infra.mapper.DevopsHostCommandMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:25
 */
@Service
public class DevopsHostAppServiceImpl implements DevopsHostAppService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsHostAppServiceImpl.class);

    private static final String ERROR_UPDATE_JAVA_INSTANCE_FAILED = "error.update.java.instance.failed";

    @Lazy
    @Autowired
    private DevopsHostAdditionalCheckValidator devopsHostAdditionalCheckValidator;
    @Autowired
    private DevopsHostAppMapper devopsHostAppMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    @Lazy
    private DevopsHostService devopsHostService;
    @Autowired
    private RdupmClientOperator rdupmClientOperator;
    @Autowired
    @Lazy
    private DevopsHostCommandService devopsHostCommandService;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private KeySocketSendHelper webSocketHelper;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private DeployConfigService deployConfigService;
    @Autowired
    private DevopsHostCommandMapper devopsHostCommandMapper;
    @Autowired
    private DevopsHostUserPermissionService devopsHostUserPermissionService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private DevopsHostAppInstanceService devopsHostAppInstanceService;
    @Autowired
    private DevopsMiddlewareService devopsMiddlewareService;
    @Autowired
    @Lazy
    private DevopsCdPipelineService devopsCdPipelineService;

    @Override
    @Transactional
    public void deployJavaInstance(Long projectId, JarDeployVO jarDeployVO) {
        Long hostId = jarDeployVO.getHostId();
        String groupId = null;
        String artifactId = null;
        String version = null;

        // 校验主机权限
        devopsHostUserPermissionService.checkUserPermissionAndThrow(projectId, hostId, DetailsHelper.getUserDetails().getUserId());

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);

        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setType(jarDeployVO.getSourceType());
        deploySourceVO.setProjectName(projectDTO.getName());

        String deployObjectName = null;
        String deployVersion = null;

        // 0.3 获取并记录信息
        List<C7nNexusComponentDTO> nexusComponentDTOList = new ArrayList<>();
        List<NexusMavenRepoDTO> mavenRepoDTOList = new ArrayList<>();

        // 标识部署对象
        String deployObjectKey = null;
        if (StringUtils.endsWithIgnoreCase(AppSourceType.MARKET.getValue(), jarDeployVO.getSourceType())
                || StringUtils.endsWithIgnoreCase(AppSourceType.HZERO.getValue(), jarDeployVO.getSourceType())) {
            deployObjectKey = String.valueOf(jarDeployVO.getMarketDeployObjectInfoVO().getMktDeployObjectId());
            MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(jarDeployVO.getMarketDeployObjectInfoVO().getMktDeployObjectId()));
            JarReleaseConfigVO jarReleaseConfigVO = JsonHelper.unmarshalByJackson(marketServiceDeployObjectVO.getMarketJarLocation(), JarReleaseConfigVO.class);
            if (Objects.isNull(marketServiceDeployObjectVO.getMarketMavenConfigVO())) {
                throw new CommonException("error.maven.deploy.object.not.exist");
            }

            deployObjectName = marketServiceDeployObjectVO.getMarketServiceName();

            MarketMavenConfigVO marketMavenConfigVO = marketServiceDeployObjectVO.getMarketMavenConfigVO();
            C7nNexusComponentDTO nNexusComponentDTO = new C7nNexusComponentDTO();

            deployVersion = jarReleaseConfigVO.getVersion();
            nNexusComponentDTO.setName(jarReleaseConfigVO.getArtifactId());
            nNexusComponentDTO.setVersion(jarReleaseConfigVO.getVersion());
            nNexusComponentDTO.setGroup(jarReleaseConfigVO.getGroupId());
            nNexusComponentDTO.setDownloadUrl(MavenUtil.getDownloadUrl(jarReleaseConfigVO));
            nexusComponentDTOList.add(nNexusComponentDTO);

            groupId = jarReleaseConfigVO.getGroupId();
            artifactId = jarReleaseConfigVO.getArtifactId();
            version = jarReleaseConfigVO.getVersion();

            jarDeployVO.setProdJarInfoVO(new ProdJarInfoVO(jarReleaseConfigVO.getGroupId(),
                    jarReleaseConfigVO.getArtifactId(),
                    jarReleaseConfigVO.getVersion()));

            NexusMavenRepoDTO nexusMavenRepoDTO = new NexusMavenRepoDTO();
            nexusMavenRepoDTO.setNePullUserId(marketMavenConfigVO.getPullUserName());
            nexusMavenRepoDTO.setNePullUserPassword(marketMavenConfigVO.getPullPassword());
            mavenRepoDTOList.add(nexusMavenRepoDTO);

            deploySourceVO.setMarketAppName(marketServiceDeployObjectVO.getMarketAppName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketAppVersion());
            deploySourceVO.setMarketServiceName(marketServiceDeployObjectVO.getMarketServiceName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketServiceVersion());

            //如果是市场部署将部署人员添加为应用的订阅人员
            marketServiceClientOperator.subscribeApplication(marketServiceDeployObjectVO.getMarketAppId(), DetailsHelper.getUserDetails().getUserId());

            deploySourceVO.setDeployObjectId(jarDeployVO.getMarketDeployObjectInfoVO().getMktDeployObjectId());

        } else if (AppSourceType.CURRENT_PROJECT.getValue().equals(jarDeployVO.getSourceType())) {
            // 0.2 从制品库获取仓库信息
            Long nexusRepoId = jarDeployVO.getProdJarInfoVO().getRepositoryId();
            groupId = jarDeployVO.getProdJarInfoVO().getGroupId();
            artifactId = jarDeployVO.getProdJarInfoVO().getArtifactId();
            version = jarDeployVO.getProdJarInfoVO().getVersion();
            nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), projectId, nexusRepoId, groupId, artifactId, version);
            mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), projectId, Collections.singleton(nexusRepoId));
            deployObjectName = nexusComponentDTOList.get(0).getName();
            deployVersion = nexusComponentDTOList.get(0).getVersion();

            deployObjectKey = new StringBuilder()
                    .append(nexusRepoId)
                    .append(BaseConstants.Symbol.COLON)
                    .append(groupId)
                    .append(BaseConstants.Symbol.COLON)
                    .append(artifactId)
                    .toString();
        }

        // 2.保存记录
        DevopsHostAppDTO devopsHostAppDTO = queryByHostIdAndCode(hostId, jarDeployVO.getAppCode());
        DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = null;
        if (devopsHostAppDTO == null) {
            devopsHostAppDTO = new DevopsHostAppDTO(projectId,
                    hostId,
                    jarDeployVO.getAppName(),
                    jarDeployVO.getAppCode(),
                    RdupmTypeEnum.JAR.value(),
                    OperationTypeEnum.CREATE_APP.value());
            MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_SAVE_JAVA_INSTANCE_FAILED);
            devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO(projectId,
                    hostId,
                    devopsHostAppDTO.getId(),
                    jarDeployVO.getAppCode() + "-" + GenerateUUID.generateRandomString(),
                    jarDeployVO.getSourceType(),
                    calculateSourceConfig(jarDeployVO),
                    jarDeployVO.getPreCommand(),
                    jarDeployVO.getRunCommand(),
                    jarDeployVO.getPostCommand());
            devopsHostAppInstanceDTO.setGroupId(groupId);
            devopsHostAppInstanceDTO.setArtifactId(artifactId);
            devopsHostAppInstanceDTO.setVersion(version);

            devopsHostAppInstanceService.baseCreate(devopsHostAppInstanceDTO);
        } else {
            devopsHostAppDTO.setName(jarDeployVO.getAppName());
            MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_UPDATE_JAVA_INSTANCE_FAILED);

            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(devopsHostAppDTO.getId());
            devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);

            devopsHostAppInstanceDTO.setPreCommand(jarDeployVO.getPreCommand());
            devopsHostAppInstanceDTO.setRunCommand(jarDeployVO.getRunCommand());
            devopsHostAppInstanceDTO.setPostCommand(jarDeployVO.getPostCommand());
            devopsHostAppInstanceDTO.setSourceType(jarDeployVO.getSourceType());
            devopsHostAppInstanceDTO.setSourceConfig(calculateSourceConfig(jarDeployVO));
            devopsHostAppInstanceDTO.setVersion(version);
            devopsHostAppInstanceService.baseUpdate(devopsHostAppInstanceDTO);
        }

        Map<String, String> params = new HashMap<>();
        String workDir = HostDeployUtil.genWorkingDir(devopsHostAppInstanceDTO.getId());
        params.put("{{ WORK_DIR }}", workDir);
        String downloadCommand;
        String appFile;
        String appFileName;
        if (AppSourceType.UPLOAD.getValue().equals(jarDeployVO.getSourceType())) {
            appFileName = jarDeployVO.getFileInfoVO().getFileName() + System.currentTimeMillis();
            appFile = workDir + appFileName;
            downloadCommand = HostDeployUtil.genDownloadCommand("none",
                    "none",
                    jarDeployVO.getFileInfoVO().getUploadUrl(),
                    workDir,
                    appFile);
        } else {
            appFileName = nexusComponentDTOList.get(0).getName() + System.currentTimeMillis();
            appFile = workDir + appFileName;
            downloadCommand = HostDeployUtil.genDownloadCommand(mavenRepoDTOList.get(0).getNePullUserId(),
                    mavenRepoDTOList.get(0).getNePullUserPassword(),
                    nexusComponentDTOList.get(0).getDownloadUrl(),
                    workDir,
                    appFile);
        }
        params.put("{{ APP_FILE_NAME }}", appFileName);
        params.put("{{ APP_FILE }}", appFile);


        JavaDeployDTO javaDeployDTO = new JavaDeployDTO(
                jarDeployVO.getAppCode(),
                String.valueOf(devopsHostAppInstanceDTO.getId()),
                downloadCommand,
                StringUtils.isEmpty(jarDeployVO.getPreCommand()) ? "" : HostDeployUtil.genCommand(params, Base64Util.decodeBuffer(jarDeployVO.getPreCommand())),
                StringUtils.isEmpty(jarDeployVO.getRunCommand()) ? "" : HostDeployUtil.genRunCommand(params, Base64Util.decodeBuffer(jarDeployVO.getRunCommand())),
                StringUtils.isEmpty(jarDeployVO.getPostCommand()) ? "" : HostDeployUtil.genCommand(params, Base64Util.decodeBuffer(jarDeployVO.getPostCommand())),
                devopsHostAppInstanceDTO.getPid());

        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.DEPLOY_INSTANCE.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.INSTANCE_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsHostAppInstanceDTO.getId());
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);

        // 保存执行记录
        Long devopsDeployRecordId = devopsDeployRecordService.saveRecord(
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
                devopsHostAppDTO.getName(),
                devopsHostAppDTO.getCode(),
                devopsHostAppDTO.getId(),
                deploySourceVO);

        // 保存用户设置部署配置文件
        deployConfigService.saveConfigSetting(projectId, devopsDeployRecordId, deployObjectKey, jarDeployVO);

        // 3. 发送部署指令给agent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_INSTANCE.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(javaDeployDTO));
        hostAgentMsgVO.setConfigSetting(deployConfigService.doCreateConfigSetting(projectId, jarDeployVO));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy jar instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.NORMAL_INSTANCE, hostId, devopsHostAppInstanceDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));
    }

    public String calculateSourceConfig(JarDeployVO jarDeployVO) {

        if (AppSourceType.CURRENT_PROJECT.getValue().equals(jarDeployVO.getSourceType())) {
            return JsonHelper.marshalByJackson(jarDeployVO.getProdJarInfoVO());
        } else if (AppSourceType.MARKET.getValue().equals(jarDeployVO.getSourceType())
                || AppSourceType.HZERO.getValue().equals(jarDeployVO.getSourceType())) {
            return JsonHelper.marshalByJackson(jarDeployVO.getMarketDeployObjectInfoVO());
        } else if (AppSourceType.UPLOAD.getValue().equals(jarDeployVO.getSourceType())) {
            return JsonHelper.marshalByJackson(jarDeployVO.getFileInfoVO());
        }
        return null;
    }

    @Override
    public List<DevopsHostAppDTO> listByHostId(Long hostId) {
        Assert.notNull(hostId, ResourceCheckConstant.ERROR_HOST_ID_IS_NULL);
        return devopsHostAppMapper.listByHostId(hostId);
    }

    @Override
    @Transactional
    public void baseUpdate(DevopsHostAppDTO devopsHostAppDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsHostAppMapper, devopsHostAppDTO, ERROR_UPDATE_JAVA_INSTANCE_FAILED);
    }

    @Override
    @Transactional
    public void baseDelete(Long instanceId) {
        devopsHostAppMapper.deleteByPrimaryKey(instanceId);
    }

    @Override
    public DevopsHostAppDTO baseQuery(Long id) {
        return devopsHostAppMapper.selectByPrimaryKey(id);
    }

    @Override
    public DevopsHostAppDTO queryByHostIdAndCode(Long hostId, String code) {
        Assert.notNull(hostId, ResourceCheckConstant.ERROR_HOST_ID_IS_NULL);
        Assert.notNull(code, ResourceCheckConstant.ERROR_JAR_NAME_IS_NULL);
        DevopsHostAppDTO devopsHostAppDTO = new DevopsHostAppDTO(hostId, code);
        return devopsHostAppMapper.selectOne(devopsHostAppDTO);
    }

    @Override
    public Page<DevopsHostAppVO> pagingAppByHost(Long projectId, Long hostId, PageRequest pageRequest, String rdupmType, String operationType, String params) {
        Page<DevopsHostAppVO> page;
        if (permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, DetailsHelper.getUserDetails().getUserId())) {
            page = PageHelper.doPage(pageRequest, () -> devopsHostAppMapper.listByOptions(hostId, rdupmType, operationType, params));
        } else {
            page = PageHelper.doPage(pageRequest, () -> devopsHostAppMapper.listOwnedByOptions(DetailsHelper.getUserDetails().getUserId(), hostId, rdupmType, operationType, params));
        }

        if (CollectionUtils.isEmpty(page.getContent())) {
            return page;
        }
        UserDTOFillUtil.fillUserInfo(page.getContent(), "createdBy", "creator");
        page.getContent().forEach(devopsHostAppVO -> {
            compoundDevopsHostAppVO(devopsHostAppVO);
            devopsHostAppVO.setDevopsHostCommandDTO(devopsHostCommandService.queryInstanceLatest(devopsHostAppVO.getInstanceId()));
        });
        return page;
    }

    @Override
    public DevopsHostAppVO queryAppById(Long projectId, Long id) {
        DevopsHostAppVO devopsHostAppVO = devopsHostAppMapper.queryAppById(id);
        if (ObjectUtils.isEmpty(devopsHostAppVO)) {
            return devopsHostAppVO;
        }
        List<DevopsHostAppVO> devopsHostAppVOS = new ArrayList<>();
        devopsHostAppVOS.add(devopsHostAppVO);
        UserDTOFillUtil.fillUserInfo(devopsHostAppVOS, "createdBy", "creator");
        devopsHostAppVO = devopsHostAppVOS.get(0);
        compoundDevopsHostAppVO(devopsHostAppVO);
        devopsHostAppVO.setDeployWay(AppCenterDeployWayEnum.HOST.getValue());
        devopsHostAppVO.setDevopsHostCommandDTO(devopsHostCommandMapper.selectLatestByInstanceId(devopsHostAppVO.getInstanceId()));
        return devopsHostAppVO;
    }

    @Override
    public void checkNameAndCodeUniqueAndThrow(Long projectId, Long appId, String name, String code) {
        checkNameUniqueAndThrow(projectId, appId, name);

        checkCodeUniqueAndThrow(projectId, appId, name);

    }

    public void checkCodeUniqueAndThrow(Long projectId, Long appId, String code) {
        if (Boolean.FALSE.equals(checkNameUnique(projectId, appId, code))) {
            throw new CommonException("error.host.app.code.exist");
        }
    }

    public void checkNameUniqueAndThrow(Long projectId, Long appId, String name) {
        if (Boolean.FALSE.equals(checkNameUnique(projectId, appId, name))) {
            throw new CommonException("error.host.app.name.exist");
        }
    }

    @Override
    public Boolean checkCodeUnique(Long projectId, Long appId, String code) {
        return devopsHostAppMapper.checkCodeUnique(projectId, appId, code);
    }

    @Override
    public Boolean checkNameUnique(Long projectId, Long appId, String name) {
        return devopsHostAppMapper.checkNameUnique(projectId, appId, name);
    }

    @Override
    public void deleteById(Long projectId, Long hostId, Long appId) {
        devopsHostAdditionalCheckValidator.validHostIdAndInstanceIdMatch(hostId, appId);
        DevopsHostAppDTO devopsHostAppDTO = devopsHostAppMapper.selectByPrimaryKey(appId);
        List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(appId);
        DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);
        if (devopsHostAppInstanceDTO.getPid() == null) {
            if (devopsHostAppDTO != null) {
                devopsHostAppMapper.deleteByPrimaryKey(appId);
            }
            devopsHostAppInstanceService.baseDelete(devopsHostAppInstanceDTO.getId());
            if (AppSourceType.MIDDLEWARE.getValue().equals(devopsHostAppInstanceDTO.getSourceType())) {
                devopsMiddlewareService.deleteByInstanceId(devopsHostAppInstanceDTO.getId());
            }
            return;
        }

        // 走中间件删除逻辑
        if (AppSourceType.MIDDLEWARE.getValue().equals(devopsHostAppInstanceDTO.getSourceType())) {
            devopsMiddlewareService.uninstallMiddleware(projectId, devopsHostAppInstanceDTO);
        } else {

            DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
            devopsHostCommandDTO.setCommandType(HostCommandEnum.KILL_INSTANCE.value());
            devopsHostCommandDTO.setHostId(hostId);
            devopsHostCommandDTO.setInstanceType(HostResourceType.INSTANCE_PROCESS.value());
            devopsHostCommandDTO.setInstanceId(devopsHostAppInstanceDTO.getId());
            devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
            devopsHostCommandService.baseCreate(devopsHostCommandDTO);


            HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
            hostAgentMsgVO.setHostId(String.valueOf(hostId));
            hostAgentMsgVO.setType(HostCommandEnum.KILL_INSTANCE.value());
            hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));


            InstanceProcessInfoVO instanceProcessInfoVO = new InstanceProcessInfoVO();
            instanceProcessInfoVO.setInstanceId(String.valueOf(devopsHostAppInstanceDTO.getId()));
            instanceProcessInfoVO.setPid(devopsHostAppInstanceDTO.getPid());
            hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(instanceProcessInfoVO));

            webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(DevopsHostAppDTO devopsHostAppDTO, String errorCode) {
        MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, errorCode);
    }

    @Override
    @Transactional
    public void deployCustomInstance(Long projectId, CustomDeployVO customDeployVO) {
        Long hostId = customDeployVO.getHostId();
        // 校验主机权限
        devopsHostUserPermissionService.checkUserPermissionAndThrow(projectId, hostId, DetailsHelper.getUserDetails().getUserId());

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);

        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setType(customDeployVO.getSourceType());
        deploySourceVO.setProjectName(projectDTO.getName());

        // 2.保存记录
        DevopsHostAppDTO devopsHostAppDTO = queryByHostIdAndCode(hostId, customDeployVO.getAppCode());
        DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = null;
        if (devopsHostAppDTO == null) {
            devopsHostAppDTO = new DevopsHostAppDTO(projectId,
                    hostId,
                    customDeployVO.getAppName(),
                    customDeployVO.getAppCode(),
                    RdupmTypeEnum.OTHER.value(),
                    OperationTypeEnum.CREATE_APP.value());
            MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_SAVE_CUSTOM_INSTANCE_FAILED);

            devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO(projectId,
                    hostId,
                    devopsHostAppDTO.getId(),
                    customDeployVO.getAppCode() + "-" + GenerateUUID.generateRandomString(),
                    customDeployVO.getSourceType(),
                    AppSourceType.UPLOAD.getValue().equals(customDeployVO.getSourceType()) ? JsonHelper.marshalByJackson(customDeployVO.getFileInfoVO()) : null,
                    customDeployVO.getPreCommand(),
                    customDeployVO.getRunCommand(),
                    customDeployVO.getPostCommand());

            devopsHostAppInstanceService.baseCreate(devopsHostAppInstanceDTO);
        } else {
            devopsHostAppDTO.setName(customDeployVO.getAppName());
            MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_UPDATE_JAVA_INSTANCE_FAILED);

            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(devopsHostAppDTO.getId());
            devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);

            devopsHostAppInstanceDTO.setPreCommand(customDeployVO.getPreCommand());
            devopsHostAppInstanceDTO.setRunCommand(customDeployVO.getRunCommand());
            devopsHostAppInstanceDTO.setPostCommand(customDeployVO.getPostCommand());
            devopsHostAppInstanceDTO.setSourceType(customDeployVO.getSourceType());
            devopsHostAppInstanceDTO.setSourceConfig(AppSourceType.UPLOAD.getValue().equals(customDeployVO.getSourceType()) ? JsonHelper.marshalByJackson(customDeployVO.getFileInfoVO()) : null);
            devopsHostAppInstanceService.baseUpdate(devopsHostAppInstanceDTO);
        }

        Map<String, String> params = new HashMap<>();
        String workDir = HostDeployUtil.genWorkingDir(devopsHostAppInstanceDTO.getId());
        if (customDeployVO.getFileInfoVO().getFileName() == null) {
            customDeployVO.getFileInfoVO().setFileName("");
        }
        String appFileName = customDeployVO.getFileInfoVO().getFileName() + System.currentTimeMillis();
        String appFile = workDir + appFileName;
        params.put("{{ WORK_DIR }}", workDir);
        params.put("{{ APP_FILE_NAME }}", appFileName);
        params.put("{{ APP_FILE }}", appFile);

        String downloadCommand = null;
        if (AppSourceType.UPLOAD.getValue().equals(customDeployVO.getSourceType())) {
            downloadCommand = HostDeployUtil.genDownloadCommand(null,
                    null,
                    customDeployVO.getFileInfoVO().getUploadUrl(),
                    workDir,
                    appFile);
        }

        JavaDeployDTO javaDeployDTO = new JavaDeployDTO(
                customDeployVO.getAppCode(),
                String.valueOf(devopsHostAppInstanceDTO.getId()),
                downloadCommand,
                StringUtils.isEmpty(customDeployVO.getPreCommand()) ? "" : HostDeployUtil.genCommand(params, Base64Util.decodeBuffer(customDeployVO.getPreCommand())),
                StringUtils.isEmpty(customDeployVO.getRunCommand()) ? "" : HostDeployUtil.genRunCommand(params, Base64Util.decodeBuffer(customDeployVO.getRunCommand())),
                StringUtils.isEmpty(customDeployVO.getPostCommand()) ? "" : HostDeployUtil.genCommand(params, Base64Util.decodeBuffer(customDeployVO.getPostCommand())),
                devopsHostAppInstanceDTO.getPid());

        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.DEPLOY_INSTANCE.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.INSTANCE_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsHostAppInstanceDTO.getId());
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);

        // 保存执行记录
        devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.MANUAL,
                null,
                DeployModeEnum.HOST,
                devopsHostDTO.getId(),
                devopsHostDTO.getName(),
                PipelineStatus.SUCCESS.toValue(),
                DeployObjectTypeEnum.OTHER,
                devopsHostDTO.getName(),
                null,
                devopsHostAppDTO.getName(),
                devopsHostAppDTO.getCode(),
                devopsHostAppDTO.getId(),
                deploySourceVO);

        // 3. 发送部署指令给agent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_INSTANCE.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(javaDeployDTO));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy custom instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.NORMAL_INSTANCE, hostId, devopsHostAppInstanceDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));
    }

    @Override
    public PipelineInstanceReferenceVO queryPipelineReferenceEnvApp(Long projectId, Long appId) {
        return devopsCdPipelineService.queryPipelineReferenceEnvApp(projectId, appId);
    }

    @Override
    public PipelineInstanceReferenceVO queryPipelineReferenceHostApp(Long projectId, Long appId) {
        return devopsCdPipelineService.queryPipelineReferenceHostApp(projectId, appId);
    }

    private void compoundDevopsHostAppVO(DevopsHostAppVO devopsHostAppVO) {
        if (AppSourceType.CURRENT_PROJECT.getValue().equals(devopsHostAppVO.getSourceType())) {
            devopsHostAppVO.setProdJarInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppVO.getSourceConfig(), ProdJarInfoVO.class));
        } else if (AppSourceType.MARKET.getValue().equals(devopsHostAppVO.getSourceType())
                || AppSourceType.HZERO.getValue().equals(devopsHostAppVO.getSourceType())) {
            devopsHostAppVO.setMarketDeployObjectInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppVO.getSourceConfig(), MarketDeployObjectInfoVO.class));
        } else if (AppSourceType.UPLOAD.getValue().equals(devopsHostAppVO.getSourceType())) {
            devopsHostAppVO.setFileInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppVO.getSourceConfig(), FileInfoVO.class));
        }
    }

    public static void main(String[] args) {
        String testStr = "djasklf\nasdfkljasdk\n";
        for (String line : testStr.split("\\\\n")) {
            System.out.println(line);
        }
    }

}
