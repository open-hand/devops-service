package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.Response;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.feign.AppShareClient;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.mapper.AppServiceVersionMapper;
import io.choerodon.devops.infra.mapper.AppServiceVersionReadmeMapper;
import io.choerodon.devops.infra.mapper.DevopsProjectConfigMapper;
import io.choerodon.devops.infra.util.*;

@Service
public class AppServiceVersionServiceImpl implements AppServiceVersionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineServiceImpl.class);

    private static final String DESTINATION_PATH = "devops";
    private static final String STORE_PATH = "stores";

    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${services.helm.url}")
    private String helmUrl;

    @Autowired
    private AppSevriceService applicationService;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private AppServiceVersionValueService appServiceVersionValueService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsGitlabCommitService devopsGitlabCommitService;
    @Autowired
    private PipelineAppDeployService pipelineAppDeployService;
    @Autowired
    private PipelineTaskService pipelineTaskService;
    @Autowired
    private PipelineStageService pipelineStageService;
    @Autowired
    private ChartUtil chartUtil;
    @Autowired
    private PipelineService pipelineService;
    @Autowired
    private AppServiceVersionMapper appServiceVersionMapper;
    @Autowired
    private AppServiceVersionReadmeMapper appServiceVersionReadmeMapper;
    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private DevopsProjectConfigMapper devopsProjectConfigMapper;
    @Autowired
    private MarketConnectInfoService marketConnectInfoService;


    private Gson gson = new Gson();
    private JSON json = new JSON();

    /**
     * 方法中抛出runtime Exception而不是CommonException是为了返回非200的状态码。
     */
    @Override
    public void create(String image, String token, String version, String commit, MultipartFile files) {
        try {
            doCreate(image, token, version, commit, files);
        } catch (Exception e) {
            if (e instanceof CommonException) {
                throw new DevopsCiInvalidException(((CommonException) e).getCode(), e.getCause());
            }
            throw new DevopsCiInvalidException(e.getMessage(), e);
        }
    }

    private void doCreate(String image, String token, String version, String commit, MultipartFile files) {
        AppServiceDTO applicationDTO = appServiceMapper.queryByToken(token);

        AppServiceVersionValueDTO appServiceVersionValueDTO = new AppServiceVersionValueDTO();
        AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO();
        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(applicationDTO.getProjectId());
        OrganizationDTO organization = iamServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        AppServiceVersionDTO newApplicationVersion = baseQueryByAppIdAndVersion(applicationDTO.getId(), version);
        appServiceVersionDTO.setAppServiceId(applicationDTO.getId());
        appServiceVersionDTO.setImage(image);
        appServiceVersionDTO.setCommit(commit);
        appServiceVersionDTO.setVersion(version);
        if (applicationDTO.getChartConfigId() != null) {
            DevopsProjectConfigDTO devopsProjectConfigDTO = devopsProjectConfigMapper.selectByPrimaryKey((applicationDTO.getChartConfigId()));
            helmUrl = gson.fromJson(devopsProjectConfigDTO.getConfig(), ProjectConfigVO.class).getUrl();
        }

        appServiceVersionDTO.setRepository(helmUrl.endsWith("/") ? helmUrl : helmUrl + "/" + organization.getCode() + "/" + projectDTO.getCode() + "/");
        String storeFilePath = STORE_PATH + version;

        String destFilePath = DESTINATION_PATH + version;
        String path = FileUtil.multipartFileToFile(storeFilePath, files);
        //上传chart包到chartmuseum
        chartUtil.uploadChart(organization.getCode(), projectDTO.getCode(), new File(path));

        if (newApplicationVersion != null) {
            return;
        }
        FileUtil.unTarGZ(path, destFilePath);
        String values;
        try (FileInputStream fis = new FileInputStream(new File(Objects.requireNonNull(FileUtil.queryFileFromFiles(
                new File(destFilePath), "values.yaml")).getAbsolutePath()))) {
            values = FileUtil.replaceReturnString(fis, null);
        } catch (IOException e) {
            throw new CommonException(e);
        }

        try {
            FileUtil.checkYamlFormat(values);
        } catch (CommonException e) {
            throw new CommonException("The format of the values.yaml in the chart is invalid!", e);
        }
        appServiceVersionValueDTO.setValue(values);
        try {
            appServiceVersionDTO.setValueId(appServiceVersionValueService
                    .baseCreate(appServiceVersionValueDTO).getId());
        } catch (Exception e) {
            throw new CommonException("error.version.insert", e);
        }

        AppServiceVersionReadmeDTO appServiceVersionReadmeDTO = new AppServiceVersionReadmeDTO();
        appServiceVersionReadmeDTO.setReadme(FileUtil.getReadme(destFilePath));
        appServiceVersionReadmeMapper.insert(appServiceVersionReadmeDTO);

        appServiceVersionDTO.setReadmeValueId(appServiceVersionReadmeDTO.getId());
        baseCreate(appServiceVersionDTO);

        FileUtil.deleteDirectory(new File(destFilePath));
        FileUtil.deleteDirectory(new File(storeFilePath));
        //流水线
        checkAutoDeploy(appServiceVersionDTO);
    }

    /**
     * 检测能够触发自动部署
     *
     * @param appServiceVersionDTO 版本
     */
    private void checkAutoDeploy(AppServiceVersionDTO appServiceVersionDTO) {
        AppServiceVersionDTO insertAppServiceVersionDTO = baseQueryByAppIdAndVersion(appServiceVersionDTO.getAppServiceId(), appServiceVersionDTO.getVersion());

        if (insertAppServiceVersionDTO != null && insertAppServiceVersionDTO.getVersion() != null) {
            List<PipelineAppServiceDeployDTO> appDeployDTOList = pipelineAppDeployService.baseQueryByAppId(insertAppServiceVersionDTO.getAppServiceId())
                    .stream()
                    .filter(deployDTO -> filterAppDeploy(deployDTO, insertAppServiceVersionDTO.getVersion()))
                    .collect(Collectors.toList());

            if (!appDeployDTOList.isEmpty()) {
                List<Long> stageList = appDeployDTOList.stream()
                        .map(appDeploy -> pipelineTaskService.baseQueryTaskByAppDeployId(appDeploy.getId()))
                        .filter(Objects::nonNull)
                        .map(PipelineTaskDTO::getStageId)
                        .distinct()
                        .collect(Collectors.toList());

                List<Long> pipelineList = stageList.stream()
                        .map(stageId -> pipelineStageService.baseQueryById(stageId).getPipelineId())
                        .distinct()
                        .collect(Collectors.toList());

                List<PipelineDTO> devopsPipelineDTOS = new ArrayList<>();
                pipelineList.forEach(pipelineId -> {
                    PipelineDTO pipelineE = pipelineService.baseQueryById(pipelineId);
                    if (pipelineE.getIsEnabled() == 1 && "auto".equals(pipelineE.getTriggerType())) {
                        devopsPipelineDTOS.add(pipelineE);
                    }
                });

                devopsPipelineDTOS.forEach(pipelineDTO -> {
                    if (pipelineService.checkDeploy(pipelineDTO.getProjectId(), pipelineDTO.getId()).getVersions()) {
                        LOGGER.info("autoDeploy: versionId:{}, version:{} pipelineId:{}", insertAppServiceVersionDTO.getId(), insertAppServiceVersionDTO.getVersion(), pipelineDTO.getId());
                        pipelineService.executeAutoDeploy(pipelineDTO.getId());
                    }
                });
            }
        }
    }

    private boolean filterAppDeploy(PipelineAppServiceDeployDTO deployDTO, String version) {
        if (deployDTO.getTriggerVersion() == null || deployDTO.getTriggerVersion().isEmpty()) {
            return true;
        } else {
            List<String> list = Arrays.asList(deployDTO.getTriggerVersion().split(","));
            Optional<String> branch = list.stream().filter(version::contains).findFirst();
            return branch.isPresent() && !branch.get().isEmpty();
        }
    }

    @Override
    public List<AppServiceVersionRespVO> listByAppId(Long appServiceId, Boolean isPublish) {
        return ConvertUtils.convertList(baseListByAppId(appServiceId, isPublish), AppServiceVersionRespVO.class);
    }

    @Override
    public PageInfo<AppServiceVersionRespVO> pageByAppIdAndParam(Long appServiceId, Boolean isPublish, Long appServiceServiceId, PageRequest pageRequest, String searchParam) {
        return ConvertUtils.convertPage(
                basePageByPublished(appServiceId, isPublish, appServiceServiceId, pageRequest, searchParam), AppServiceVersionRespVO.class);
    }

    @Override
    public List<AppServiceVersionRespVO> listDeployedByAppId(Long projectId, Long appServiceId) {
        return ConvertUtils.convertList(
                baseListAppDeployedVersion(projectId, appServiceId), AppServiceVersionRespVO.class);
    }

    @Override
    public List<AppServiceVersionRespVO> listByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId) {
        return ConvertUtils.convertList(
                baseListByAppIdAndEnvId(projectId, appServiceId, envId), AppServiceVersionRespVO.class);
    }

    @Override
    public PageInfo<AppServiceVersionVO> pageByOptions(Long projectId, Long appServiceId, PageRequest pageRequest, String searchParams) {
        Map<String, Object> searchParamMap = json.deserialize(searchParams, Map.class);
        PageInfo<AppServiceVersionDTO> applicationVersionDTOPageInfo = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest))
                .doSelectPageInfo(() ->
                        appServiceVersionMapper.listByOptions(
                                appServiceId,
                                TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                                TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM))));
        return ConvertUtils.convertPage(applicationVersionDTOPageInfo, AppServiceVersionVO.class);
    }

    @Override
    public List<AppServiceVersionRespVO> listUpgradeableAppVersion(Long projectId, Long appServiceServiceId) {
        baseCheckByProjectAndVersionId(projectId, appServiceServiceId);
        return ConvertUtils.convertList(
                baseListUpgradeVersion(appServiceServiceId), AppServiceVersionRespVO.class);
    }

    @Override
    public DeployVersionVO queryDeployedVersions(Long appServiceId) {
        AppServiceVersionDTO appServiceVersionDTO = baseQueryNewestVersion(appServiceId);
        DeployVersionVO deployVersionVO = new DeployVersionVO();
        List<DeployEnvVersionVO> deployEnvVersionVOS = new ArrayList<>();
        if (appServiceVersionDTO != null) {
            Map<Long, List<AppServiceInstanceDTO>> envInstances = appServiceInstanceService.baseListByAppId(appServiceId)
                    .stream()
                    .filter(applicationInstanceDTO -> applicationInstanceDTO.getCommandId() != null)
                    .collect(Collectors.groupingBy(AppServiceInstanceDTO::getEnvId));

            if (!envInstances.isEmpty()) {
                envInstances.forEach((key, value) -> {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(key);
                    DeployEnvVersionVO deployEnvVersionVO = new DeployEnvVersionVO();
                    deployEnvVersionVO.setEnvName(devopsEnvironmentDTO.getName());
                    List<DeployInstanceVersionVO> deployInstanceVersionVOS = new ArrayList<>();
                    Map<Long, List<AppServiceInstanceDTO>> versionInstances = value.stream().collect(Collectors.groupingBy(t -> {
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(t.getCommandId());
                        return devopsEnvCommandDTO.getObjectVersionId();
                    }));

                    if (!versionInstances.isEmpty()) {
                        versionInstances.forEach((newKey, newValue) -> {
                            AppServiceVersionDTO newAppServiceVersionDTO = baseQuery(newKey);
                            DeployInstanceVersionVO deployInstanceVersionVO = new DeployInstanceVersionVO();
                            deployInstanceVersionVO.setDeployVersion(newAppServiceVersionDTO.getVersion());
                            deployInstanceVersionVO.setInstanceCount(newValue.size());
                            if (newAppServiceVersionDTO.getId() < appServiceVersionDTO.getId()) {
                                deployInstanceVersionVO.setUpdate(true);
                            }
                            deployInstanceVersionVOS.add(deployInstanceVersionVO);
                        });
                    }

                    deployEnvVersionVO.setDeployIntanceVersionDTO(deployInstanceVersionVOS);
                    deployEnvVersionVOS.add(deployEnvVersionVO);
                });

                deployVersionVO.setLatestVersion(appServiceVersionDTO.getVersion());
                deployVersionVO.setDeployEnvVersionVO(deployEnvVersionVOS);
            }
        }
        return deployVersionVO;
    }

    @Override
    public String queryVersionValue(Long appServiceServiceId) {
        AppServiceVersionDTO appServiceVersionDTO = baseQuery(appServiceServiceId);
        return appServiceVersionValueService.baseQuery(appServiceVersionDTO.getValueId()).getValue();
    }

    @Override
    public AppServiceVersionRespVO queryById(Long appServiceServiceId) {
        return ConvertUtils.convertObject(baseQuery(appServiceServiceId), AppServiceVersionRespVO.class);
    }

    @Override
    public List<AppServiceVersionRespVO> listByAppServiceVersionIds(List<Long> appServiceServiceIds) {
        return ConvertUtils.convertList(baseListByAppVersionIds(appServiceServiceIds), AppServiceVersionRespVO.class);
    }

    @Override
    public List<AppServiceVersionAndCommitVO> listByAppIdAndBranch(Long appServiceId, String branch) {
        List<AppServiceVersionDTO> appServiceVersionDTOS = baseListByAppIdAndBranch(appServiceId, branch);
        AppServiceDTO applicationDTO = applicationService.baseQuery(appServiceId);
        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(applicationDTO.getProjectId());
        OrganizationDTO organization = iamServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        List<AppServiceVersionAndCommitVO> appServiceVersionAndCommitVOS = new ArrayList<>();

        appServiceVersionDTOS.forEach(applicationVersionDTO -> {
            AppServiceVersionAndCommitVO appServiceVersionAndCommitVO = new AppServiceVersionAndCommitVO();
            DevopsGitlabCommitDTO devopsGitlabCommitE = devopsGitlabCommitService.baseQueryByShaAndRef(applicationVersionDTO.getCommit(), branch);
            IamUserDTO userE = iamServiceClientOperator.queryUserByUserId(devopsGitlabCommitE.getUserId());
            appServiceVersionAndCommitVO.setAppServiceName(applicationDTO.getName());
            appServiceVersionAndCommitVO.setCommit(applicationVersionDTO.getCommit());
            appServiceVersionAndCommitVO.setCommitContent(devopsGitlabCommitE.getCommitContent());
            appServiceVersionAndCommitVO.setCommitUserImage(userE == null ? null : userE.getImageUrl());
            appServiceVersionAndCommitVO.setCommitUserName(userE == null ? null : userE.getRealName());
            appServiceVersionAndCommitVO.setVersion(applicationVersionDTO.getVersion());
            appServiceVersionAndCommitVO.setCreateDate(applicationVersionDTO.getCreationDate());
            appServiceVersionAndCommitVO.setCommitUrl(gitlabUrl + "/"
                    + organization.getCode() + "-" + projectDTO.getCode() + "/"
                    + applicationDTO.getCode() + ".git");
            appServiceVersionAndCommitVOS.add(appServiceVersionAndCommitVO);

        });
        return appServiceVersionAndCommitVOS;
    }

    @Override
    public Boolean queryByPipelineId(Long pipelineId, String branch, Long appServiceId) {
        return appServiceVersionMapper.queryByPipelineId(pipelineId, branch, appServiceId) != null;
    }

    @Override
    public String queryValueById(Long projectId, Long appServiceId) {
        return appServiceVersionMapper.queryValueByAppId(appServiceId);
    }

    @Override
    public AppServiceVersionRespVO queryByAppAndVersion(Long appServiceId, String version) {
        return ConvertUtils.convertObject(baseQueryByAppIdAndVersion(appServiceId, version), AppServiceVersionRespVO.class);
    }

    @Override
    public AppServiceVersionDTO baseCreate(AppServiceVersionDTO appServiceVersionDTO) {
        if (appServiceVersionMapper.insert(appServiceVersionDTO) != 1) {
            throw new CommonException("error.version.insert");
        }
        return appServiceVersionDTO;
    }

    @Override
    public List<AppServiceLatestVersionDTO> baseListAppNewestVersion(Long projectId) {
        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(projectId);
        List<ProjectDTO> projectEList = iamServiceClientOperator.listIamProjectByOrgId(projectDTO.getOrganizationId(), null, null);
        List<Long> projectIds = projectEList.stream().map(ProjectDTO::getId)
                .collect(Collectors.toCollection(ArrayList::new));
        return appServiceVersionMapper.listAppNewestVersion(projectId, projectIds);
    }

    @Override
    public PageInfo<MarketAppPublishVersionVO> pageVersionByAppId(Long appServiceId, PageRequest pageRequest, String params) {
        DevopsMarketConnectInfoDTO marketConnectInfoDO = marketConnectInfoService.baseQuery();
        if (marketConnectInfoDO == null) {
            throw new CommonException("not.exist.remote token");
        }
        AppShareClient shareClient = RetrofitHandler.getAppShareClient(marketConnectInfoDO.getSaasMarketUrl());
        Map<String, Object> map = new HashMap<>();
        map.put("page", pageRequest.getPage());
        map.put("size", pageRequest.getSize());
        map.put("sort", PageRequestUtil.getOrderByStr(pageRequest));
        if (params != null) {
            map.put("params", params);
        }
        map.put("access_token", marketConnectInfoDO.getAccessToken());
        Response<PageInfo<MarketAppPublishVersionVO>> pageInfoResponse = null;
        try {
            pageInfoResponse = shareClient.listVersionByAppId(appServiceId, map).execute();
            if (!pageInfoResponse.isSuccessful()) {
                throw new CommonException("error.get.app.version.shares");
            }
        } catch (IOException e) {
            throw new CommonException("error.get.app.version.shares");
        }
        return pageInfoResponse.body();
    }

    @Override
    public PageInfo<AppServiceVersionRespVO> pageShareVersionByAppId(Long appServiceId, PageRequest pageRequest, String params) {
        PageInfo<AppServiceVersionDTO> applicationDTOPageInfo = PageHelper.startPage(
                pageRequest.getPage(),
                pageRequest.getSize(),
                PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> appServiceVersionMapper.listShareVersionByAppId(appServiceId, params));
        return ConvertUtils.convertPage(applicationDTOPageInfo, AppServiceVersionRespVO.class);
    }

    @Override
    public AppServiceVersionAndValueVO queryConfigByVerionId(Long appServiceId, Long versionId) {
        DevopsMarketConnectInfoDTO marketConnectInfoDO = marketConnectInfoService.baseQuery();
        if (marketConnectInfoDO == null) {
            throw new CommonException("not.exist.remote token");
        }
        AppShareClient shareClient = RetrofitHandler.getAppShareClient(marketConnectInfoDO.getSaasMarketUrl());
        Map<String, Object> map = new HashMap<>();
        map.put("access_token", marketConnectInfoDO.getAccessToken());
        Response<AppServiceVersionAndValueVO> versionAndValueDTOResponse = null;
        try {
            versionAndValueDTOResponse = shareClient.getConfigInfoByVerionId(appServiceId, versionId, map).execute();
            if (!versionAndValueDTOResponse.isSuccessful()) {
                throw new CommonException("error.get.app.version.config.shares");
            }
        } catch (IOException e) {
            throw new CommonException("error.get.app.version.config.shares");
        }
        return versionAndValueDTOResponse.body();
    }

    public List<AppServiceVersionDTO> baseListByAppId(Long appServiceId, Boolean isPublish) {
        List<AppServiceVersionDTO> appServiceVersionDTOS = appServiceVersionMapper.listByAppId(appServiceId, isPublish);
        if (appServiceVersionDTOS.isEmpty()) {
            return Collections.emptyList();
        }
        return appServiceVersionDTOS;
    }

    public PageInfo<AppServiceVersionDTO> basePageByPublished(Long appServiceId, Boolean isPublish, Long appServiceServiceId, PageRequest pageRequest, String searchParam) {
        PageInfo<AppServiceVersionDTO> applicationVersionDTOPageInfo;
        applicationVersionDTOPageInfo = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
                () -> appServiceVersionMapper.selectByAppIdAndParamWithPage(appServiceId, isPublish, searchParam));
        if (appServiceServiceId != null) {
            AppServiceVersionDTO versionDO = new AppServiceVersionDTO();
            versionDO.setId(appServiceServiceId);
            AppServiceVersionDTO searchDO = appServiceVersionMapper.selectByPrimaryKey(versionDO);
            applicationVersionDTOPageInfo.getList().removeIf(v -> v.getId().equals(appServiceServiceId));
            applicationVersionDTOPageInfo.getList().add(0, searchDO);
        }
        if (applicationVersionDTOPageInfo.getList().isEmpty()) {
            return new PageInfo<>(Collections.emptyList());
        }
        return applicationVersionDTOPageInfo;
    }

    public List<AppServiceVersionDTO> baseListAppDeployedVersion(Long projectId, Long appServiceId) {
        List<AppServiceVersionDTO> appServiceVersionDTOS =
                appServiceVersionMapper.listAppDeployedVersion(projectId, appServiceId);
        if (appServiceVersionDTOS.isEmpty()) {
            return Collections.emptyList();
        }
        return appServiceVersionDTOS;
    }

    public AppServiceVersionDTO baseQuery(Long appServiceServiceId) {
        return appServiceVersionMapper.selectByPrimaryKey(appServiceServiceId);
    }

    public List<AppServiceVersionDTO> baseListByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId) {
        return appServiceVersionMapper.listByAppIdAndEnvId(projectId, appServiceId, envId);
    }

    public String baseQueryValue(Long versionId) {
        return appServiceVersionMapper.queryValue(versionId);
    }

    public AppServiceVersionDTO baseQueryByAppIdAndVersion(Long appServiceId, String version) {
        AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO();
        appServiceVersionDTO.setAppServiceId(appServiceId);
        appServiceVersionDTO.setVersion(version);
        List<AppServiceVersionDTO> appServiceVersionDTOS = appServiceVersionMapper.select(appServiceVersionDTO);
        if (appServiceVersionDTOS.isEmpty()) {
            return null;
        }
        return appServiceVersionDTOS.get(0);
    }

    public void baseUpdatePublishLevelByIds(List<Long> appServiceServiceIds, Long level) {
        AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO();
        appServiceVersionDTO.setIsPublish(level);
        for (Long id : appServiceServiceIds) {
            appServiceVersionDTO.setId(id);
            appServiceVersionDTO.setObjectVersionNumber(appServiceVersionMapper.selectByPrimaryKey(id).getObjectVersionNumber());
            if (appServiceVersionDTO.getObjectVersionNumber() == null) {
                appServiceVersionDTO.setPublishTime(new java.sql.Date(new java.util.Date().getTime()));
                appServiceVersionMapper.updateObJectVersionNumber(id);
                appServiceVersionDTO.setObjectVersionNumber(1L);
            }
            appServiceVersionMapper.updateByPrimaryKeySelective(appServiceVersionDTO);
        }
    }

    public PageInfo<AppServiceVersionDTO> basePageByOptions(Long projectId, Long appServiceId, PageRequest pageRequest,
                                                            String searchParam, Boolean isProjectOwner,
                                                            Long userId) {
        Sort sort = pageRequest.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if (property.equals("version")) {
                            property = "dav.version";
                        } else if (property.equals("creationDate")) {
                            property = "dav.creation_date";
                        }
                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }

        PageInfo<AppServiceVersionDTO> applicationVersionDTOPageInfo;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            applicationVersionDTOPageInfo = PageHelper
                    .startPage(pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> appServiceVersionMapper.listApplicationVersion(projectId, appServiceId,
                            TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                            TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM)), isProjectOwner, userId));
        } else {
            applicationVersionDTOPageInfo = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> appServiceVersionMapper
                    .listApplicationVersion(projectId, appServiceId, null, null, isProjectOwner, userId));
        }
        return applicationVersionDTOPageInfo;
    }

    public List<AppServiceVersionDTO> baseListByPublished(Long applicationId) {
        return appServiceVersionMapper.listByPublished(applicationId);
    }

    public Boolean baseCheckByAppIdAndVersionIds(Long appServiceId, List<Long> appServiceServiceIds) {
        if (appServiceId == null || appServiceServiceIds.isEmpty()) {
            throw new CommonException("error.app.version.check");
        }
        List<Long> versionList = appServiceVersionMapper.listByAppIdAndVersionIds(appServiceId);
        if (appServiceServiceIds.stream().anyMatch(t -> !versionList.contains(t))) {
            throw new CommonException("error.app.version.check");
        }
        return true;
    }

    public Long baseCreateReadme(String readme) {
        AppServiceVersionReadmeDTO appServiceVersionReadmeDTO = new AppServiceVersionReadmeDTO(readme);
        appServiceVersionReadmeMapper.insert(appServiceVersionReadmeDTO);
        return appServiceVersionReadmeDTO.getId();
    }

    public String baseQueryReadme(Long readmeValueId) {
        String readme;
        try {
            readme = appServiceVersionReadmeMapper.selectByPrimaryKey(readmeValueId).getReadme();
        } catch (Exception ignore) {
            readme = "# 暂无";
        }
        return readme;
    }

    public void baseUpdate(AppServiceVersionDTO appServiceVersionDTO) {
        if (appServiceVersionMapper.updateByPrimaryKey(appServiceVersionDTO) != 1) {
            throw new CommonException("error.version.update");
        }
        //待修改readme
    }

    private void updateReadme(Long readmeValueId, String readme) {
        AppServiceVersionReadmeDTO readmeDO;
        try {

            readmeDO = appServiceVersionReadmeMapper.selectByPrimaryKey(readmeValueId);
            readmeDO.setReadme(readme);
            appServiceVersionReadmeMapper.updateByPrimaryKey(readmeDO);
        } catch (Exception e) {
            readmeDO = new AppServiceVersionReadmeDTO(readme);
            appServiceVersionReadmeMapper.insert(readmeDO);
        }
    }

    public List<AppServiceVersionDTO> baseListUpgradeVersion(Long appServiceServiceId) {
        return appServiceVersionMapper.listUpgradeVersion(appServiceServiceId);
    }


    public void baseCheckByProjectAndVersionId(Long projectId, Long appServiceServiceId) {
        Integer index = appServiceVersionMapper.checkByProjectAndVersionId(projectId, appServiceServiceId);
        if (index == 0) {
            throw new CommonException("error.project.AppVersion.notExist");
        }
    }

    public AppServiceVersionDTO baseQueryByCommitSha(Long appServiceId, String ref, String sha) {
        return appServiceVersionMapper.queryByCommitSha(appServiceId, ref, sha);
    }

    public AppServiceVersionDTO baseQueryNewestVersion(Long appServiceId) {
        return appServiceVersionMapper.queryNewestVersion(appServiceId);
    }

    public List<AppServiceVersionDTO> baseListByAppVersionIds(List<Long> appServiceServiceIds) {
        return appServiceVersionMapper.listByAppVersionIds(appServiceServiceIds);
    }

    public List<AppServiceVersionDTO> baseListByAppIdAndBranch(Long appServiceId, String branch) {
        return appServiceVersionMapper.listByAppIdAndBranch(appServiceId, branch);
    }

    public String baseQueryByPipelineId(Long pipelineId, String branch, Long appServiceId) {
        return appServiceVersionMapper.queryByPipelineId(pipelineId, branch, appServiceId);
    }

    public String baseQueryValueByAppId(Long appServiceId) {
        return appServiceVersionMapper.queryValueByAppId(appServiceId);
    }

}
