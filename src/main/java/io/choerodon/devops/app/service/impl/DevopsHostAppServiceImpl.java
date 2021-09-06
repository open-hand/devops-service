package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
import sun.misc.BASE64Decoder;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.validator.DevopsHostAdditionalCheckValidator;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.FileInfoVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.api.vo.host.DevopsHostAppVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.api.vo.host.JavaProcessInfoVO;
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
import io.choerodon.devops.infra.dto.repo.JarPullInfoDTO;
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
    private static final String ERROR_JAR_VERSION_NOT_FOUND = "error.jar.version.not.found";

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

    private static final BASE64Decoder decoder = new BASE64Decoder();

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


//        String encodeValue = jarDeployVO.getValue();
//        try {
//            jarDeployVO.setValue(new String(decoder.decodeBuffer(encodeValue), StandardCharsets.UTF_8));
//        } catch (IOException e) {
//            throw new CommonException("decode.values.failed", e);
//        }
        String deployObjectName = null;
        String deployVersion = null;

        // 0.3 获取并记录信息
        List<C7nNexusComponentDTO> nexusComponentDTOList = new ArrayList<>();
        List<NexusMavenRepoDTO> mavenRepoDTOList = new ArrayList<>();

        // 标识部署对象
        String deployObjectKey = null;
        JarPullInfoDTO jarPullInfoDTO = new JarPullInfoDTO();

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

            // 添加jar包下载信息
            jarPullInfoDTO.setPullUserId(mavenRepoDTOList.get(0).getNePullUserId());
            jarPullInfoDTO.setPullUserPassword(mavenRepoDTOList.get(0).getNePullUserPassword());
            jarPullInfoDTO.setDownloadUrl(nexusComponentDTOList.get(0).getDownloadUrl());
        } else if (AppSourceType.CURRENT_PROJECT.getValue().equals(jarDeployVO.getSourceType())){
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
            // 添加jar包下载信息
            jarPullInfoDTO.setPullUserId(mavenRepoDTOList.get(0).getNePullUserId());
            jarPullInfoDTO.setPullUserPassword(mavenRepoDTOList.get(0).getNePullUserPassword());
            jarPullInfoDTO.setDownloadUrl(nexusComponentDTOList.get(0).getDownloadUrl());
        }

        // 2.保存记录
        DevopsHostAppDTO devopsHostAppDTO = queryByHostIdAndCode(hostId, jarDeployVO.getAppCode());
        if (devopsHostAppDTO == null) {
            devopsHostAppDTO = new DevopsHostAppDTO(projectId,
                    hostId,
                    jarDeployVO.getAppName(),
                    jarDeployVO.getAppCode(),
                    jarDeployVO.getSourceType(),
                    RdupmTypeEnum.JAR.value(),
                    OperationTypeEnum.CREATE_APP.value());
            MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_SAVE_JAVA_INSTANCE_FAILED);
            DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO(projectId,
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
            String finalVersion = version;
            devopsHostAppInstanceDTOS.forEach(devopsHostAppInstanceDTO -> {
                devopsHostAppInstanceDTO.setPreCommand(jarDeployVO.getPreCommand());
                devopsHostAppInstanceDTO.setRunCommand(jarDeployVO.getRunCommand());
                devopsHostAppInstanceDTO.setPostCommand(jarDeployVO.getPostCommand());
                devopsHostAppInstanceDTO.setSourceType(jarDeployVO.getSourceType());
                devopsHostAppInstanceDTO.setSourceConfig(calculateSourceConfig(jarDeployVO));
                devopsHostAppInstanceDTO.setVersion(finalVersion);
                devopsHostAppInstanceService.baseUpdate(devopsHostAppInstanceDTO);
            });
        }

        JavaDeployDTO javaDeployDTO = new JavaDeployDTO(
                jarDeployVO.getAppCode(),
                deployObjectName,
                String.valueOf(devopsHostAppDTO.getId()),
                HostDeployUtil.genJavaRunCmd(jarPullInfoDTO, jarDeployVO, devopsHostAppDTO.getId()),
                devopsHostAppDTO.getPid());

        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.DEPLOY_JAR.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.JAVA_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsHostAppDTO.getId());
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
                null,
                deploySourceVO);

        // 保存用户设置部署配置文件
        deployConfigService.saveConfigSetting(projectId, devopsDeployRecordId, deployObjectKey, jarDeployVO);

        // 3. 发送部署指令给agent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_JAR.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(javaDeployDTO));
        hostAgentMsgVO.setConfigSetting(deployConfigService.doCreateConfigSetting(projectId, jarDeployVO));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy jar instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.JAVA_INSTANCE, hostId, devopsHostAppDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));
    }

    public String calculateSourceConfig(JarDeployVO jarDeployVO) {

        if (AppSourceType.CURRENT_PROJECT.getValue().equals(jarDeployVO.getSourceType())) {
            return JsonHelper.marshalByJackson(jarDeployVO.getProdJarInfoVO());
        } else if (AppSourceType.MARKET.getValue().equals(jarDeployVO.getSourceType())
                || AppSourceType.HZERO.getValue().equals(jarDeployVO.getSourceType())) {
            return JsonHelper.marshalByJackson(jarDeployVO.getMarketDeployObjectInfoVO());
        } else if (AppSourceType.UPLOAD.getValue().equals(jarDeployVO.getSourceType())){
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
    public DevopsHostAppDTO baseQuery(Long instanceId) {
        return devopsHostAppMapper.selectByPrimaryKey(instanceId);
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
            devopsHostAppVO.setDevopsHostCommandDTO(devopsHostCommandService.queryInstanceLatest(devopsHostAppVO.getId()));
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
        devopsHostAppVO.setDevopsHostCommandDTO(devopsHostCommandMapper.selectLatestByInstanceId(devopsHostAppVO.getId()));
        return devopsHostAppVO;
    }

    @Override
    public void deleteById(Long projectId, Long hostId, Long appId) {
        devopsHostAdditionalCheckValidator.validHostIdAndInstanceIdMatch(hostId, appId);
        DevopsHostAppDTO devopsHostAppDTO = devopsHostAppMapper.selectByPrimaryKey(appId);
        if (devopsHostAppDTO.getPid() == null) {
            devopsHostAppMapper.deleteByPrimaryKey(appId);
            return;
        }
        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.KILL_JAR.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.JAVA_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(appId);
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);


        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.KILL_JAR.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));


        JavaProcessInfoVO javaProcessInfoVO = new JavaProcessInfoVO();
        javaProcessInfoVO.setInstanceId(String.valueOf(appId));
        javaProcessInfoVO.setPid(devopsHostAppDTO.getPid());
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(javaProcessInfoVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    private void compoundDevopsHostAppVO(DevopsHostAppVO devopsHostAppVO) {
        if (AppSourceType.CURRENT_PROJECT.getValue().equals(devopsHostAppVO.getSourceType())) {
            devopsHostAppVO.setProdJarInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppVO.getSourceConfig(), ProdJarInfoVO.class));
        } else if (AppSourceType.MARKET.getValue().equals(devopsHostAppVO.getSourceType())
                || AppSourceType.HZERO.getValue().equals(devopsHostAppVO.getSourceType())) {
            devopsHostAppVO.setMarketDeployObjectInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppVO.getSourceConfig(), MarketDeployObjectInfoVO.class));
        } else if (AppSourceType.UPLOAD.getValue().equals(devopsHostAppVO.getSourceType())){
            devopsHostAppVO.setFileInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppVO.getSourceConfig(), FileInfoVO.class));
        }
    }

}
