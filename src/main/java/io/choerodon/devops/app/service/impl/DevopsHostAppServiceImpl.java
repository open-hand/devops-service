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
import org.springframework.util.StringUtils;
import sun.misc.BASE64Decoder;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.api.vo.host.DevopsHostAppVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.api.vo.market.JarReleaseConfigVO;
import io.choerodon.devops.api.vo.market.MarketMavenConfigVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsHostAppDTO;
import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
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
import io.choerodon.devops.infra.enums.deploy.OperationTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.DevopsHostAppMapper;
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
    private AppServiceService appServiceService;
    @Autowired
    private DevopsHostAppInstanceRelService devopsHostAppInstanceRelService;

    private static final BASE64Decoder decoder = new BASE64Decoder();

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
        String encodeValue = jarDeployVO.getValue();

        try {
            jarDeployVO.setValue(new String(decoder.decodeBuffer(encodeValue), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new CommonException("decode.values.failed", e);
        }
        List<AppServiceDTO> appServiceDTOList;
        String deployObjectName = null;
        String deployVersion = null;

        // 0.3 获取并记录信息
        List<C7nNexusComponentDTO> nexusComponentDTOList = new ArrayList<>();
        List<NexusMavenRepoDTO> mavenRepoDTOList = new ArrayList<>();
        JarPullInfoDTO jarPullInfoDTO = new JarPullInfoDTO();
        if (StringUtils.endsWithIgnoreCase(AppSourceType.MARKET.getValue(), jarDeployVO.getSourceType())
                || StringUtils.endsWithIgnoreCase(AppSourceType.HZERO.getValue(), jarDeployVO.getSourceType())) {

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
            nNexusComponentDTO.setDownloadUrl(MavenUtil.getDownloadUrl(jarReleaseConfigVO));
            nexusComponentDTOList.add(nNexusComponentDTO);

            jarDeployVO.setProdJarInfoVO(new ProdJarInfoVO(jarReleaseConfigVO.getGroupId(),
                    jarReleaseConfigVO.getArtifactId(),
                    jarReleaseConfigVO.getVersion()));

            NexusMavenRepoDTO nexusMavenRepoDTO = new NexusMavenRepoDTO();
            nexusMavenRepoDTO.setNePullUserId(marketMavenConfigVO.getPullUserName());
            nexusMavenRepoDTO.setNePullUserPassword(marketMavenConfigVO.getPullPassword());
            mavenRepoDTOList.add(nexusMavenRepoDTO);
//            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(AppSourceType.HZERO.getValue(), jarDeployVO.getSourceType())) {
//                AppServiceDTO appServiceDTO = new AppServiceDTO();
//                appServiceDTO.setId(marketServiceDeployObjectVO.getMarketServiceId());
//                appServiceDTO.setName(marketServiceDeployObjectVO.getMarketServiceName());
//                appServiceDTOList = Arrays.asList(appServiceDTO);
//            } else {
//                appServiceDTOList = appServiceService.listByProjectIdAndGAV(projectId, jarReleaseConfigVO.getGroupId(), jarReleaseConfigVO.getArtifactId());

//            }

            deploySourceVO.setMarketAppName(marketServiceDeployObjectVO.getMarketAppName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketAppVersion());
            deploySourceVO.setMarketServiceName(marketServiceDeployObjectVO.getMarketServiceName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketServiceVersion());

            //如果是市场部署将部署人员添加为应用的订阅人员
            marketServiceClientOperator.subscribeApplication(marketServiceDeployObjectVO.getMarketAppId(), DetailsHelper.getUserDetails().getUserId());

            // 添加jar包下载信息
            jarPullInfoDTO.setPullUserId(mavenRepoDTOList.get(0).getNePullUserId());
            jarPullInfoDTO.setPullUserPassword(mavenRepoDTOList.get(0).getNePullUserPassword());
            jarPullInfoDTO.setDownloadUrl(nexusComponentDTOList.get(0).getDownloadUrl());
        } else if (AppSourceType.CURRENT_PROJECT.getValue().equals(jarDeployVO.getSourceType())){
            // 0.2 从制品库获取仓库信息
            Long nexusRepoId = jarDeployVO.getProdJarInfoVO().getRepositoryId();
            String groupId = jarDeployVO.getProdJarInfoVO().getGroupId();
            String artifactId = jarDeployVO.getProdJarInfoVO().getArtifactId();
            String version = jarDeployVO.getProdJarInfoVO().getVersion();
            nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), projectId, nexusRepoId, groupId, artifactId, version);
            mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), projectId, Collections.singleton(nexusRepoId));
            deployObjectName = nexusComponentDTOList.get(0).getName();
            deployVersion = nexusComponentDTOList.get(0).getVersion();

//            appServiceDTOList = appServiceService.listByProjectIdAndGAV(projectId, groupId, artifactId);
            // 添加jar包下载信息
            jarPullInfoDTO.setPullUserId(mavenRepoDTOList.get(0).getNePullUserId());
            jarPullInfoDTO.setPullUserPassword(mavenRepoDTOList.get(0).getNePullUserPassword());
            jarPullInfoDTO.setDownloadUrl(nexusComponentDTOList.get(0).getDownloadUrl());
        }
        if (CollectionUtils.isEmpty(nexusComponentDTOList)) {
            throw new CommonException(ERROR_JAR_VERSION_NOT_FOUND);
        }
        if (CollectionUtils.isEmpty(mavenRepoDTOList)) {
            throw new CommonException("error.get.maven.config");
        }




        // 2.保存记录
        DevopsHostAppDTO devopsHostAppDTO = queryByHostIdAndName(hostId, jarDeployVO.getAppCode());
        if (devopsHostAppDTO == null) {
            devopsHostAppDTO = new DevopsHostAppDTO(hostId,
                    jarDeployVO.getAppCode(),
                    jarDeployVO.getSourceType(),
                    RdupmTypeEnum.JAR.value(),
                    OperationTypeEnum.CREATE_APP.value(),
                    calculateSourceConfig(jarDeployVO),
                    encodeValue);

            MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_SAVE_JAVA_INSTANCE_FAILED);

        }
        // todo 新应用中心，不需要这块儿逻辑，先注释
//        else {
//            // 删除原有应用关联关系
//            devopsHostAppInstanceRelService.deleteByHostIdAndInstanceInfo(hostId, devopsHostAppDTO.getId(), HostInstanceType.NORMAL_PROCESS.value());
//        }
//        // 有关联的应用，则保存关联关系
//        if (!CollectionUtils.isEmpty(appServiceDTOList)) {
//            Set<Long> appIds = appServiceDTOList.stream().map(AppServiceDTO::getId).collect(Collectors.toSet());
//            Map<Long, AppServiceDTO> appServiceDTOMap = appServiceDTOList.stream().collect(Collectors.toMap(AppServiceDTO::getId, Function.identity()));
//            Long instanceId = devopsHostAppDTO.getId();
//            appIds.forEach(appId -> devopsHostAppInstanceRelService.saveHostAppInstanceRel(projectId,
//                    hostId,
//                    appId,
//                    jarDeployVO.getSourceType(),
//                    instanceId,
//                    HostInstanceType.NORMAL_PROCESS.value(), appServiceDTOMap.get(appId) == null ? null : appServiceDTOMap.get(appId).getName()));
//        }


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

        // 3. 发送部署指令给agent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_JAR.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(javaDeployDTO));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy jar instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.JAVA_INSTANCE, hostId, devopsHostAppDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));
    }

    private String calculateSourceConfig(JarDeployVO jarDeployVO) {

        if (AppSourceType.CURRENT_PROJECT.getValue().equals(jarDeployVO.getSourceType())) {
            return JsonHelper.marshalByJackson(jarDeployVO.getProdJarInfoVO());
        } else if (AppSourceType.MARKET.getValue().equals(jarDeployVO.getSourceType())
                || AppSourceType.HZERO.getValue().equals(jarDeployVO.getSourceType())) {
            return JsonHelper.marshalByJackson(jarDeployVO.getDeployObjectId());
        } else if (AppSourceType.UPLOAD.getValue().equals(jarDeployVO.getSourceType())){
            return jarDeployVO.getJarFileUrl();
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
    public DevopsHostAppDTO queryByHostIdAndName(Long hostId, String name) {
        Assert.notNull(hostId, ResourceCheckConstant.ERROR_HOST_ID_IS_NULL);
        Assert.notNull(name, ResourceCheckConstant.ERROR_JAR_NAME_IS_NULL);
        DevopsHostAppDTO devopsHostAppDTO = new DevopsHostAppDTO(hostId, name);
        return devopsHostAppMapper.selectOne(devopsHostAppDTO);
    }

    @Override
    public Page<DevopsHostAppVO> pagingAppByHost(Long projectId, Long hostId, PageRequest pageRequest, String rdupmType, String operationType, String params) {
        Page<DevopsHostAppVO> page = PageHelper.doPage(pageRequest, () -> devopsHostAppMapper.listByOptions(hostId, rdupmType, operationType, params));

        if (CollectionUtils.isEmpty(page.getContent())) {
            return page;
        }
        UserDTOFillUtil.fillUserInfo(page.getContent(), "createdBy", "creator");
        page.getContent().forEach(devopsHostAppVO -> {
            if (AppSourceType.CURRENT_PROJECT.getValue().equals(devopsHostAppVO.getSourceType())) {
                devopsHostAppVO.setProdJarInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppVO.getSourceConfig(), ProdJarInfoVO.class));
            } else if (AppSourceType.MARKET.getValue().equals(devopsHostAppVO.getSourceType())
                    || AppSourceType.HZERO.getValue().equals(devopsHostAppVO.getSourceType())) {
                devopsHostAppVO.setDeployObjectId(JsonHelper.unmarshalByJackson(devopsHostAppVO.getSourceConfig(), Long.class));
            } else if (AppSourceType.UPLOAD.getValue().equals(devopsHostAppVO.getSourceType())){
                devopsHostAppVO.setJarFileUrl(JsonHelper.unmarshalByJackson(devopsHostAppVO.getSourceConfig(), String.class));
            }
        });
        return page;
    }

}
