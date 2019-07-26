package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
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
import io.choerodon.devops.infra.mapper.ApplicationMapper;
import io.choerodon.devops.infra.mapper.ApplicationVersionMapper;
import io.choerodon.devops.infra.mapper.ApplicationVersionReadmeMapper;
import io.choerodon.devops.infra.mapper.DevopsProjectConfigMapper;
import io.choerodon.devops.infra.util.*;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.Response;

@Service
public class ApplicationVersionServiceImpl implements ApplicationVersionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineServiceImpl.class);

    private static final String DESTINATION_PATH = "devops";
    private static final String STORE_PATH = "stores";

    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${services.helm.url}")
    private String helmUrl;

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private ApplicationVersionValueService applicationVersionValueService;
    @Autowired
    private ApplicationInstanceService applicationInstanceService;
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
    private ApplicationVersionMapper applicationVersionMapper;
    @Autowired
    private ApplicationVersionReadmeMapper applicationVersionReadmeMapper;
    @Autowired
    private ApplicationMapper applicationMapper;
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
        ApplicationDTO applicationDTO = applicationMapper.queryByToken(token);

        ApplicationVersionValueDTO applicationVersionValueDTO = new ApplicationVersionValueDTO();
        ApplicationVersionDTO applicationVersionDTO = new ApplicationVersionDTO();
        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(applicationDTO.getProjectId());
        OrganizationDTO organization = iamServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        ApplicationVersionDTO newApplicationVersion = baseQueryByAppIdAndVersion(applicationDTO.getId(), version);
        applicationVersionDTO.setAppId(applicationDTO.getId());
        applicationVersionDTO.setImage(image);
        applicationVersionDTO.setCommit(commit);
        applicationVersionDTO.setVersion(version);
        if (applicationDTO.getChartConfigId() != null) {
            DevopsProjectConfigDTO devopsProjectConfigDTO = devopsProjectConfigMapper.selectByPrimaryKey((applicationDTO.getChartConfigId()));
            helmUrl = gson.fromJson(devopsProjectConfigDTO.getConfig(), ProjectConfigVO.class).getUrl();
        }

        applicationVersionDTO.setRepository(helmUrl.endsWith("/") ? helmUrl : helmUrl + "/" + organization.getCode() + "/" + projectDTO.getCode() + "/");
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
        applicationVersionValueDTO.setValue(values);
        try {
            applicationVersionDTO.setValueId(applicationVersionValueService
                    .baseCreate(applicationVersionValueDTO).getId());
        } catch (Exception e) {
            throw new CommonException("error.version.insert", e);
        }

        ApplicationVersionReadmeDTO applicationVersionReadmeDTO = new ApplicationVersionReadmeDTO();
        applicationVersionReadmeDTO.setReadme(FileUtil.getReadme(destFilePath));
        applicationVersionReadmeMapper.insert(applicationVersionReadmeDTO);

        applicationVersionDTO.setReadmeValueId(applicationVersionReadmeDTO.getId());
        baseCreate(applicationVersionDTO);

        FileUtil.deleteDirectory(new File(destFilePath));
        FileUtil.deleteDirectory(new File(storeFilePath));
        //流水线
        checkAutoDeploy(applicationVersionDTO);
    }

    /**
     * 检测能够触发自动部署
     *
     * @param applicationVersionDTO 版本
     */
    private void checkAutoDeploy(ApplicationVersionDTO applicationVersionDTO) {
        ApplicationVersionDTO insertApplicationVersionDTO = baseQueryByAppIdAndVersion(applicationVersionDTO.getAppId(), applicationVersionDTO.getVersion());

        if (insertApplicationVersionDTO != null && insertApplicationVersionDTO.getVersion() != null) {
            List<PipelineAppDeployDTO> appDeployDTOList = pipelineAppDeployService.baseQueryByAppId(insertApplicationVersionDTO.getAppId())
                    .stream()
                    .filter(deployDTO -> filterAppDeploy(deployDTO, insertApplicationVersionDTO.getVersion()))
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
                        LOGGER.info("autoDeploy: versionId:{}, version:{} pipelineId:{}", insertApplicationVersionDTO.getId(), insertApplicationVersionDTO.getVersion(), pipelineDTO.getId());
                        pipelineService.executeAutoDeploy(pipelineDTO.getId());
                    }
                });
            }
        }
    }

    private boolean filterAppDeploy(PipelineAppDeployDTO deployDTO, String version) {
        if (deployDTO.getTriggerVersion() == null || deployDTO.getTriggerVersion().isEmpty()) {
            return true;
        } else {
            List<String> list = Arrays.asList(deployDTO.getTriggerVersion().split(","));
            Optional<String> branch = list.stream().filter(version::contains).findFirst();
            return branch.isPresent() && !branch.get().isEmpty();
        }
    }

    @Override
    public List<ApplicationVersionRespVO> listByAppId(Long appId, Boolean isPublish) {
        return ConvertUtils.convertList(baseListByAppId(appId, isPublish), ApplicationVersionRespVO.class);
    }

    @Override
    public PageInfo<ApplicationVersionRespVO> pageByAppIdAndParam(Long appId, Boolean isPublish, Long appVersionId, PageRequest pageRequest, String searchParam) {
        return ConvertUtils.convertPage(
                basePageByPublished(appId, isPublish, appVersionId, pageRequest, searchParam), ApplicationVersionRespVO.class);
    }

    @Override
    public List<ApplicationVersionRespVO> listDeployedByAppId(Long projectId, Long appId) {
        return ConvertUtils.convertList(
                baseListAppDeployedVersion(projectId, appId), ApplicationVersionRespVO.class);
    }

    @Override
    public List<ApplicationVersionRespVO> listByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return ConvertUtils.convertList(
                baseListByAppIdAndEnvId(projectId, appId, envId), ApplicationVersionRespVO.class);
    }

    @Override
    public PageInfo<ApplicationVersionRespVO> pageApplicationVersionInApp(Long projectId, Long appId, PageRequest pageRequest, String searchParams) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(projectId);
        Boolean isProjectOwner = iamServiceClientOperator.isProjectOwner(userAttrDTO.getIamUserId(), projectDTO);
        PageInfo<ApplicationVersionDTO> applicationVersionDTOPageInfo = basePageByOptions(
                projectId, appId, pageRequest, searchParams, isProjectOwner, userAttrDTO.getIamUserId());
        return ConvertUtils.convertPage(applicationVersionDTOPageInfo, ApplicationVersionRespVO.class);
    }

    @Override
    public List<ApplicationVersionRespVO> listUpgradeableAppVersion(Long projectId, Long appVersionId) {
        baseCheckByProjectAndVersionId(projectId, appVersionId);
        return ConvertUtils.convertList(
                baseListUpgradeVersion(appVersionId), ApplicationVersionRespVO.class);
    }

    @Override
    public DeployVersionVO queryDeployedVersions(Long appId) {
        ApplicationVersionDTO applicationVersionDTO = baseQueryNewestVersion(appId);
        DeployVersionVO deployVersionVO = new DeployVersionVO();
        List<DeployEnvVersionVO> deployEnvVersionVOS = new ArrayList<>();
        if (applicationVersionDTO != null) {
            Map<Long, List<ApplicationInstanceDTO>> envInstances = applicationInstanceService.baseListByAppId(appId)
                    .stream()
                    .filter(applicationInstanceDTO -> applicationInstanceDTO.getCommandId() != null)
                    .collect(Collectors.groupingBy(ApplicationInstanceDTO::getEnvId));

            if (!envInstances.isEmpty()) {
                envInstances.forEach((key, value) -> {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(key);
                    DeployEnvVersionVO deployEnvVersionVO = new DeployEnvVersionVO();
                    deployEnvVersionVO.setEnvName(devopsEnvironmentDTO.getName());
                    List<DeployInstanceVersionVO> deployInstanceVersionVOS = new ArrayList<>();
                    Map<Long, List<ApplicationInstanceDTO>> versionInstances = value.stream().collect(Collectors.groupingBy(t -> {
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(t.getCommandId());
                        return devopsEnvCommandDTO.getObjectVersionId();
                    }));

                    if (!versionInstances.isEmpty()) {
                        versionInstances.forEach((newKey, newValue) -> {
                            ApplicationVersionDTO newApplicationVersionDTO = baseQuery(newKey);
                            DeployInstanceVersionVO deployInstanceVersionVO = new DeployInstanceVersionVO();
                            deployInstanceVersionVO.setDeployVersion(newApplicationVersionDTO.getVersion());
                            deployInstanceVersionVO.setInstanceCount(newValue.size());
                            if (newApplicationVersionDTO.getId() < applicationVersionDTO.getId()) {
                                deployInstanceVersionVO.setUpdate(true);
                            }
                            deployInstanceVersionVOS.add(deployInstanceVersionVO);
                        });
                    }

                    deployEnvVersionVO.setDeployIntanceVersionDTO(deployInstanceVersionVOS);
                    deployEnvVersionVOS.add(deployEnvVersionVO);
                });

                deployVersionVO.setLatestVersion(applicationVersionDTO.getVersion());
                deployVersionVO.setDeployEnvVersionVO(deployEnvVersionVOS);
            }
        }
        return deployVersionVO;
    }

    @Override
    public String queryVersionValue(Long appVersionId) {
        ApplicationVersionDTO applicationVersionDTO = baseQuery(appVersionId);
        return applicationVersionValueService.baseQuery(applicationVersionDTO.getValueId()).getValue();
    }

    @Override
    public ApplicationVersionRespVO queryById(Long appVersionId) {
        return ConvertUtils.convertObject(baseQuery(appVersionId), ApplicationVersionRespVO.class);
    }

    @Override
    public List<ApplicationVersionRespVO> listByAppVersionIds(List<Long> appVersionIds) {
        return ConvertUtils.convertList(baseListByAppVersionIds(appVersionIds), ApplicationVersionRespVO.class);
    }

    @Override
    public List<ApplicationVersionAndCommitVO> listByAppIdAndBranch(Long appId, String branch) {
        List<ApplicationVersionDTO> applicationVersionDTOS = baseListByAppIdAndBranch(appId, branch);
        ApplicationDTO applicationDTO = applicationService.baseQuery(appId);
        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(applicationDTO.getProjectId());
        OrganizationDTO organization = iamServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        List<ApplicationVersionAndCommitVO> applicationVersionAndCommitVOS = new ArrayList<>();

        applicationVersionDTOS.forEach(applicationVersionDTO -> {
            ApplicationVersionAndCommitVO applicationVersionAndCommitVO = new ApplicationVersionAndCommitVO();
            DevopsGitlabCommitDTO devopsGitlabCommitE = devopsGitlabCommitService.baseQueryByShaAndRef(applicationVersionDTO.getCommit(), branch);
            IamUserDTO userE = iamServiceClientOperator.queryUserByUserId(devopsGitlabCommitE.getUserId());
            applicationVersionAndCommitVO.setAppName(applicationDTO.getName());
            applicationVersionAndCommitVO.setCommit(applicationVersionDTO.getCommit());
            applicationVersionAndCommitVO.setCommitContent(devopsGitlabCommitE.getCommitContent());
            applicationVersionAndCommitVO.setCommitUserImage(userE == null ? null : userE.getImageUrl());
            applicationVersionAndCommitVO.setCommitUserName(userE == null ? null : userE.getRealName());
            applicationVersionAndCommitVO.setVersion(applicationVersionDTO.getVersion());
            applicationVersionAndCommitVO.setCreateDate(applicationVersionDTO.getCreationDate());
            applicationVersionAndCommitVO.setCommitUrl(gitlabUrl + "/"
                    + organization.getCode() + "-" + projectDTO.getCode() + "/"
                    + applicationDTO.getCode() + ".git");
            applicationVersionAndCommitVOS.add(applicationVersionAndCommitVO);

        });
        return applicationVersionAndCommitVOS;
    }

    @Override
    public Boolean queryByPipelineId(Long pipelineId, String branch, Long appId) {
        return applicationVersionMapper.queryByPipelineId(pipelineId, branch, appId) != null;
    }

    @Override
    public String queryValueById(Long projectId, Long appId) {
        return applicationVersionMapper.queryValueByAppId(appId);
    }

    @Override
    public ApplicationVersionRespVO queryByAppAndVersion(Long appId, String version) {
        return ConvertUtils.convertObject(baseQueryByAppIdAndVersion(appId, version), ApplicationVersionRespVO.class);
    }

    @Override
    public ApplicationVersionDTO baseCreate(ApplicationVersionDTO applicationVersionDTO) {
        if (applicationVersionMapper.insert(applicationVersionDTO) != 1) {
            throw new CommonException("error.version.insert");
        }
        return applicationVersionDTO;
    }

    @Override
    public List<ApplicationLatestVersionDTO> baseListAppNewestVersion(Long projectId) {
        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(projectId);
        List<ProjectDTO> projectEList = iamServiceClientOperator.listIamProjectByOrgId(projectDTO.getOrganizationId(), null, null);
        List<Long> projectIds = projectEList.stream().map(ProjectDTO::getId)
                .collect(Collectors.toCollection(ArrayList::new));
        return applicationVersionMapper.listAppNewestVersion(projectId, projectIds);
    }

    @Override
    public PageInfo<ApplicationVersionRespVO> pageVersionByAppId(Long appId, PageRequest pageRequest, String params) {
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
        Response<PageInfo<ApplicationVersionRespVO>> pageInfoResponse = null;
        try {
            pageInfoResponse = shareClient.listVersionByAppId(appId, map).execute();
            if (!pageInfoResponse.isSuccessful()) {
                throw new CommonException("error.get.app.version.shares");
            }
        } catch (IOException e) {
            throw new CommonException("error.get.app.version.shares");
        }
        return pageInfoResponse.body();
    }

    @Override
    public PageInfo<ApplicationVersionRespVO> pageShareVersionByAppId(Long appId, PageRequest pageRequest, String params) {
        PageInfo<ApplicationVersionDTO> applicationDTOPageInfo = PageHelper.startPage(
                pageRequest.getPage(),
                pageRequest.getSize(),
                PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationVersionMapper.listShareVersionByAppId(appId, params));
        return ConvertUtils.convertPage(applicationDTOPageInfo, ApplicationVersionRespVO.class);
    }

    @Override
    public AppVersionAndValueVO queryConfigByVerionId(Long appId, Long versionId) {
        DevopsMarketConnectInfoDTO marketConnectInfoDO = marketConnectInfoService.baseQuery();
        if (marketConnectInfoDO == null) {
            throw new CommonException("not.exist.remote token");
        }
        AppShareClient shareClient = RetrofitHandler.getAppShareClient(marketConnectInfoDO.getSaasMarketUrl());
        Map<String, Object> map = new HashMap<>();
        map.put("access_token", marketConnectInfoDO.getAccessToken());
        Response<AppVersionAndValueVO> versionAndValueDTOResponse = null;
        try {
            versionAndValueDTOResponse = shareClient.getConfigInfoByVerionId(appId, versionId, map).execute();
            if (!versionAndValueDTOResponse.isSuccessful()) {
                throw new CommonException("error.get.app.version.config.shares");
            }
        } catch (IOException e) {
            throw new CommonException("error.get.app.version.config.shares");
        }
        return versionAndValueDTOResponse.body();
    }

    public List<ApplicationVersionDTO> baseListByAppId(Long appId, Boolean isPublish) {
        List<ApplicationVersionDTO> applicationVersionDTOS = applicationVersionMapper.listByAppId(appId, isPublish);
        if (applicationVersionDTOS.isEmpty()) {
            return Collections.emptyList();
        }
        return applicationVersionDTOS;
    }

    public PageInfo<ApplicationVersionDTO> basePageByPublished(Long appId, Boolean isPublish, Long appVersionId, PageRequest pageRequest, String searchParam) {
        PageInfo<ApplicationVersionDTO> applicationVersionDTOPageInfo;
        applicationVersionDTOPageInfo = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
                () -> applicationVersionMapper.selectByAppIdAndParamWithPage(appId, isPublish, searchParam));
        if (appVersionId != null) {
            ApplicationVersionDTO versionDO = new ApplicationVersionDTO();
            versionDO.setId(appVersionId);
            ApplicationVersionDTO searchDO = applicationVersionMapper.selectByPrimaryKey(versionDO);
            applicationVersionDTOPageInfo.getList().removeIf(v -> v.getId().equals(appVersionId));
            applicationVersionDTOPageInfo.getList().add(0, searchDO);
        }
        if (applicationVersionDTOPageInfo.getList().isEmpty()) {
            return new PageInfo<>(Collections.emptyList());
        }
        return applicationVersionDTOPageInfo;
    }

    public List<ApplicationVersionDTO> baseListAppDeployedVersion(Long projectId, Long appId) {
        List<ApplicationVersionDTO> applicationVersionDTOS =
                applicationVersionMapper.listAppDeployedVersion(projectId, appId);
        if (applicationVersionDTOS.isEmpty()) {
            return Collections.emptyList();
        }
        return applicationVersionDTOS;
    }

    public ApplicationVersionDTO baseQuery(Long appVersionId) {
        return applicationVersionMapper.selectByPrimaryKey(appVersionId);
    }

    public List<ApplicationVersionDTO> baseListByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return applicationVersionMapper.listByAppIdAndEnvId(projectId, appId, envId);
    }

    public String baseQueryValue(Long versionId) {
        return applicationVersionMapper.queryValue(versionId);
    }

    public ApplicationVersionDTO baseQueryByAppIdAndVersion(Long appId, String version) {
        ApplicationVersionDTO applicationVersionDTO = new ApplicationVersionDTO();
        applicationVersionDTO.setAppId(appId);
        applicationVersionDTO.setVersion(version);
        List<ApplicationVersionDTO> applicationVersionDTOS = applicationVersionMapper.select(applicationVersionDTO);
        if (applicationVersionDTOS.isEmpty()) {
            return null;
        }
        return applicationVersionDTOS.get(0);
    }

    public void baseUpdatePublishLevelByIds(List<Long> appVersionIds, Long level) {
        ApplicationVersionDTO applicationVersionDTO = new ApplicationVersionDTO();
        applicationVersionDTO.setIsPublish(level);
        for (Long id : appVersionIds) {
            applicationVersionDTO.setId(id);
            applicationVersionDTO.setObjectVersionNumber(applicationVersionMapper.selectByPrimaryKey(id).getObjectVersionNumber());
            if (applicationVersionDTO.getObjectVersionNumber() == null) {
                applicationVersionDTO.setPublishTime(new java.sql.Date(new java.util.Date().getTime()));
                applicationVersionMapper.updateObJectVersionNumber(id);
                applicationVersionDTO.setObjectVersionNumber(1L);
            }
            applicationVersionMapper.updateByPrimaryKeySelective(applicationVersionDTO);
        }
    }

    public PageInfo<ApplicationVersionDTO> basePageByOptions(Long projectId, Long appId, PageRequest pageRequest,
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

        PageInfo<ApplicationVersionDTO> applicationVersionDTOPageInfo;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            applicationVersionDTOPageInfo = PageHelper
                    .startPage(pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> applicationVersionMapper.listApplicationVersion(projectId, appId,
                            TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                            TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM)), isProjectOwner, userId));
        } else {
            applicationVersionDTOPageInfo = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> applicationVersionMapper
                    .listApplicationVersion(projectId, appId, null, null, isProjectOwner, userId));
        }
        return applicationVersionDTOPageInfo;
    }

    public List<ApplicationVersionDTO> baseListByPublished(Long applicationId) {
        return applicationVersionMapper.listByPublished(applicationId);
    }

    public Boolean baseCheckByAppIdAndVersionIds(Long appId, List<Long> appVersionIds) {
        if (appId == null || appVersionIds.isEmpty()) {
            throw new CommonException("error.app.version.check");
        }
        List<Long> versionList = applicationVersionMapper.listByAppIdAndVersionIds(appId);
        if (appVersionIds.stream().anyMatch(t -> !versionList.contains(t))) {
            throw new CommonException("error.app.version.check");
        }
        return true;
    }

    public Long baseCreateReadme(String readme) {
        ApplicationVersionReadmeDTO applicationVersionReadmeDTO = new ApplicationVersionReadmeDTO(readme);
        applicationVersionReadmeMapper.insert(applicationVersionReadmeDTO);
        return applicationVersionReadmeDTO.getId();
    }

    public String baseQueryReadme(Long readmeValueId) {
        String readme;
        try {
            readme = applicationVersionReadmeMapper.selectByPrimaryKey(readmeValueId).getReadme();
        } catch (Exception ignore) {
            readme = "# 暂无";
        }
        return readme;
    }

    public void baseUpdate(ApplicationVersionDTO applicationVersionDTO) {
        if (applicationVersionMapper.updateByPrimaryKey(applicationVersionDTO) != 1) {
            throw new CommonException("error.version.update");
        }
        //待修改readme
    }

    private void updateReadme(Long readmeValueId, String readme) {
        ApplicationVersionReadmeDTO readmeDO;
        try {

            readmeDO = applicationVersionReadmeMapper.selectByPrimaryKey(readmeValueId);
            readmeDO.setReadme(readme);
            applicationVersionReadmeMapper.updateByPrimaryKey(readmeDO);
        } catch (Exception e) {
            readmeDO = new ApplicationVersionReadmeDTO(readme);
            applicationVersionReadmeMapper.insert(readmeDO);
        }
    }

    public List<ApplicationVersionDTO> baseListUpgradeVersion(Long appVersionId) {
        return applicationVersionMapper.listUpgradeVersion(appVersionId);
    }


    public void baseCheckByProjectAndVersionId(Long projectId, Long appVersionId) {
        Integer index = applicationVersionMapper.checkByProjectAndVersionId(projectId, appVersionId);
        if (index == 0) {
            throw new CommonException("error.project.AppVersion.notExist");
        }
    }

    public ApplicationVersionDTO baseQueryByCommitSha(Long appId, String ref, String sha) {
        return applicationVersionMapper.queryByCommitSha(appId, ref, sha);
    }

    public ApplicationVersionDTO baseQueryNewestVersion(Long appId) {
        return applicationVersionMapper.queryNewestVersion(appId);
    }

    public List<ApplicationVersionDTO> baseListByAppVersionIds(List<Long> appVersionIds) {
        return applicationVersionMapper.listByAppVersionIds(appVersionIds);
    }

    public List<ApplicationVersionDTO> baseListByAppIdAndBranch(Long appId, String branch) {
        return applicationVersionMapper.listByAppIdAndBranch(appId, branch);
    }

    public String baseQueryByPipelineId(Long pipelineId, String branch, Long appId) {
        return applicationVersionMapper.queryByPipelineId(pipelineId, branch, appId);
    }

    public String baseQueryValueByAppId(Long appId) {
        return applicationVersionMapper.queryValueByAppId(appId);
    }

}
