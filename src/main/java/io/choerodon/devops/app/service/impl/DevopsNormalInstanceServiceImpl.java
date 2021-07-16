package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.api.vo.market.JarReleaseConfigVO;
import io.choerodon.devops.api.vo.market.JarSourceConfig;
import io.choerodon.devops.api.vo.market.MarketMavenConfigVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.JarPullInfoDTO;
import io.choerodon.devops.infra.dto.repo.JavaDeployDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.enums.deploy.JavaInstanceStatusEnum;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostInstanceType;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.DevopsHostAppInstanceRelMapper;
import io.choerodon.devops.infra.mapper.DevopsNormalInstanceMapper;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;
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
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static io.choerodon.devops.infra.constant.DevopsHostConstants.ERROR_SAVE_APP_HOST_REL_FAILED;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:25
 */
@Service
public class DevopsNormalInstanceServiceImpl implements DevopsNormalInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsNormalInstanceServiceImpl.class);

    private static final String ERROR_SAVE_JAVA_INSTANCE_FAILED = "error.save.java.instance.failed";
    private static final String ERROR_UPDATE_JAVA_INSTANCE_FAILED = "error.update.java.instance.failed";
    private static final String ERROR_JAR_VERSION_NOT_FOUND = "error.jar.version.not.found";
    private static final String ERROR_DEPLOY_JAR_FAILED = "error.deploy.jar.failed";

    @Autowired
    private DevopsNormalInstanceMapper devopsNormalInstanceMapper;
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
    private AppServiceService appServiceService;
    @Autowired
    private DevopsHostAppInstanceRelMapper devopsHostAppInstanceRelMapper;

    @Override
    @Transactional
    public void deployJavaInstance(Long projectId, JarDeployVO jarDeployVO) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);


        Long hostId = jarDeployVO.getHostId();
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);

        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setType(jarDeployVO.getSourceType());
        deploySourceVO.setProjectName(projectDTO.getName());

        deploySourceVO.setDeployObjectId(jarDeployVO.getDeployObjectId());

        List<AppServiceDTO> appServiceDTOList = new ArrayList<>();
        String deployObjectName = null;
        String deployVersion = null;
        // 0.1 查询部署信息

        // 0.3 获取并记录信息
        List<C7nNexusComponentDTO> nexusComponentDTOList = new ArrayList<>();
        List<NexusMavenRepoDTO> mavenRepoDTOList = new ArrayList<>();
        if (StringUtils.endsWithIgnoreCase(AppSourceType.MARKET.getValue(), jarDeployVO.getSourceType())) {

            MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(jarDeployVO.getDeployObjectId()));
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
            nNexusComponentDTO.setDownloadUrl(getDownloadUrl(jarReleaseConfigVO));
            nexusComponentDTOList.add(nNexusComponentDTO);

            NexusMavenRepoDTO nexusMavenRepoDTO = new NexusMavenRepoDTO();
            nexusMavenRepoDTO.setNePullUserId(marketMavenConfigVO.getPullUserName());
            nexusMavenRepoDTO.setNePullUserPassword(marketMavenConfigVO.getPullPassword());
            mavenRepoDTOList.add(nexusMavenRepoDTO);

            appServiceDTOList = appServiceService.listByProjectIdAndGAV(projectId, jarReleaseConfigVO.getGroupId(), jarReleaseConfigVO.getArtifactId());

            JarSourceConfig jarSourceConfig = JsonHelper.unmarshalByJackson(marketServiceDeployObjectVO.getJarSource(), JarSourceConfig.class);
            deploySourceVO.setMarketAppName(marketServiceDeployObjectVO.getMarketAppName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketAppVersion());
            deploySourceVO.setMarketServiceName(marketServiceDeployObjectVO.getMarketServiceName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketServiceVersion());

            //如果是市场部署将部署人员添加为应用的订阅人员
            marketServiceClientOperator.subscribeApplication(marketServiceDeployObjectVO.getMarketAppId(), DetailsHelper.getUserDetails().getUserId());
        } else {
            // 0.2 从制品库获取仓库信息
            Long nexusRepoId = jarDeployVO.getProdJarInfoVO().getRepositoryId();
            String groupId = jarDeployVO.getProdJarInfoVO().getGroupId();
            String artifactId = jarDeployVO.getProdJarInfoVO().getArtifactId();
            String version = jarDeployVO.getProdJarInfoVO().getVersion();
            nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), projectId, nexusRepoId, groupId, artifactId, version);
            mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), projectId, Collections.singleton(nexusRepoId));
            deployObjectName = nexusComponentDTOList.get(0).getName();
            deployVersion = nexusComponentDTOList.get(0).getVersion();

            appServiceDTOList = appServiceService.listByProjectIdAndGAV(projectId, groupId, artifactId);

        }
        if (CollectionUtils.isEmpty(nexusComponentDTOList)) {
            throw new CommonException(ERROR_JAR_VERSION_NOT_FOUND);
        }
        if (CollectionUtils.isEmpty(mavenRepoDTOList)) {
            throw new CommonException("error.get.maven.config");
        }

        JavaDeployDTO javaDeployDTO = new JavaDeployDTO();

        JarPullInfoDTO jarPullInfoDTO = new JarPullInfoDTO();
        jarPullInfoDTO.setPullUserId(mavenRepoDTOList.get(0).getNePullUserId());
        jarPullInfoDTO.setPullUserPassword(mavenRepoDTOList.get(0).getNePullUserPassword());
        jarPullInfoDTO.setDownloadUrl(nexusComponentDTOList.get(0).getDownloadUrl());
        javaDeployDTO.setJarPullInfoDTO(jarPullInfoDTO);


        // 2.保存记录
        DevopsNormalInstanceDTO devopsNormalInstanceDTO = new DevopsNormalInstanceDTO();
        devopsNormalInstanceDTO.setName(deployObjectName);
        devopsNormalInstanceDTO.setSourceType(jarDeployVO.getSourceType());
        devopsNormalInstanceDTO.setStatus(JavaInstanceStatusEnum.OPERATING.value());
        devopsNormalInstanceDTO.setHostId(hostId);
        MapperUtil.resultJudgedInsertSelective(devopsNormalInstanceMapper, devopsNormalInstanceDTO, ERROR_SAVE_JAVA_INSTANCE_FAILED);

        // 有关联的应用，则保存关联关系
        if (!CollectionUtils.isEmpty(appServiceDTOList)) {
            Set<Long> appIds = appServiceDTOList.stream().map(AppServiceDTO::getId).collect(Collectors.toSet());
            appIds.forEach(appId -> {
                DevopsHostAppInstanceRelDTO devopsHostAppInstanceRelDTO = new DevopsHostAppInstanceRelDTO();
                devopsHostAppInstanceRelDTO.setAppSource(jarDeployVO.getSourceType());
                devopsHostAppInstanceRelDTO.setAppId(appId);
                devopsHostAppInstanceRelDTO.setInstanceId(devopsNormalInstanceDTO.getId());
                devopsHostAppInstanceRelDTO.setHostId(hostId);
                devopsHostAppInstanceRelDTO.setProjectId(projectId);
                devopsHostAppInstanceRelDTO.setInstanceType(HostInstanceType.NORMAL_PROCESS.value());
                MapperUtil.resultJudgedInsert(devopsHostAppInstanceRelMapper, devopsHostAppInstanceRelDTO, ERROR_SAVE_APP_HOST_REL_FAILED);

            });
        }

        javaDeployDTO.setCmd(genCmd(javaDeployDTO, jarDeployVO, devopsNormalInstanceDTO.getId()));
        javaDeployDTO.setJarName(deployObjectName);
        javaDeployDTO.setInstanceId(String.valueOf(devopsNormalInstanceDTO.getId()));

        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.DEPLOY_JAR.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.JAVA_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsNormalInstanceDTO.getId());
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
                DeployObjectTypeEnum.JAR,
                deployObjectName,
                deployVersion,
                null,
                deploySourceVO, DetailsHelper.getUserDetails().getUserId());

        // 3. 发送部署指令给agent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_JAR.value());
        hostAgentMsgVO.setKey(DevopsHostConstants.GROUP + hostId);
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(javaDeployDTO));

        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy jar instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.JAVA_INSTANCE, hostId, devopsNormalInstanceDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));


    }

    private String genCmd(JavaDeployDTO javaDeployDTO, JarDeployVO jarDeployVO, Long instanceId) {
        StringBuilder cmdStr = new StringBuilder();
        String workingPath = "$HOME/choerodon/" + instanceId;

        cmdStr.append(String.format("mkdir -p %s/temp-jar && ", workingPath));
        cmdStr.append(String.format("mkdir -p %s/temp-log && ", workingPath));
        String jarPathAndName = workingPath + "/temp-jar/" + jarDeployVO.getProdJarInfoVO().getArtifactId();
        // 2.2
        String curlExec = String.format("curl -o %s -u %s:%s %s ",
                jarPathAndName,
                javaDeployDTO.getJarPullInfoDTO().getPullUserId(),
                javaDeployDTO.getJarPullInfoDTO().getPullUserPassword(),
                javaDeployDTO.getJarPullInfoDTO().getDownloadUrl());
        cmdStr.append(curlExec).append(" && ");

        // 2.3
        String[] strings = jarDeployVO.getValue().split("\n");
        String values = "";
        for (String s : strings) {
            if (s.length() > 0 && !s.contains("#") && s.contains("java")) {
                values = s;
            }
        }
        if (StringUtils.isEmpty(values) || !values.contains("${jar}")) {
            throw new CommonException("error.instruction");
        }

        String logName = jarDeployVO.getProdJarInfoVO().getArtifactId().replace(".jar", ".log");
        String logPathAndName = workingPath + "/temp-log/" + logName;
        String javaJarExec = values.replace("${jar}", jarPathAndName);

        cmdStr.append(javaJarExec);
        StringBuilder finalCmdStr = new StringBuilder("nohup bash -c \"").append(cmdStr).append("\"").append(String.format(" > %s 2>&1 &", logPathAndName));
        return finalCmdStr.toString();
    }

    @Override
    public List<DevopsNormalInstanceDTO> listByHostId(Long hostId) {
        Assert.notNull(hostId, ResourceCheckConstant.ERROR_HOST_ID_IS_NULL);
        return devopsNormalInstanceMapper.listByHostId(hostId);
    }

    @Override
    @Transactional
    public void baseUpdate(DevopsNormalInstanceDTO devopsNormalInstanceDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsNormalInstanceMapper, devopsNormalInstanceDTO, ERROR_UPDATE_JAVA_INSTANCE_FAILED);
    }

    @Override
    @Transactional
    public void baseDelete(Long instanceId) {
        devopsNormalInstanceMapper.deleteByPrimaryKey(instanceId);
    }

    @Override
    public DevopsNormalInstanceDTO baseQuery(Long instanceId) {
        return devopsNormalInstanceMapper.selectByPrimaryKey(instanceId);
    }

    private String getDownloadUrl(JarReleaseConfigVO jarReleaseConfigVO) {
        //拼接download URL http://xxxx:17145/repository/lilly-snapshot/io/choerodon/springboot/0.0.1-SNAPSHOT/springboot-0.0.1-20210106.020444-2.jar
        return jarReleaseConfigVO.getNexusRepoUrl() + BaseConstants.Symbol.SLASH +
                jarReleaseConfigVO.getGroupId().replace(".", "/") +
                BaseConstants.Symbol.SLASH + jarReleaseConfigVO.getArtifactId() + BaseConstants.Symbol.SLASH + jarReleaseConfigVO.getVersion() +
                BaseConstants.Symbol.SLASH + jarReleaseConfigVO.getArtifactId() + BaseConstants.Symbol.MIDDLE_LINE + jarReleaseConfigVO.getSnapshotTimestamp() + ".jar";
    }
}
