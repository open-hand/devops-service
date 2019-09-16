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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.mapper.AppServiceVersionMapper;
import io.choerodon.devops.infra.mapper.AppServiceVersionReadmeMapper;
import io.choerodon.devops.infra.mapper.DevopsConfigMapper;
import io.choerodon.devops.infra.util.*;

@Service
public class AppServiceVersionServiceImpl implements AppServiceVersionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppServiceVersionServiceImpl.class);

    private static final String DESTINATION_PATH = "devops";
    private static final String STORE_PATH = "stores";

    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${services.helm.url}")
    private String helmUrl;

    @Autowired
    private AppServiceService applicationService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
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
    private DevopsConfigMapper devopsConfigMapper;


    private Gson gson = new Gson();

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
        AppServiceDTO appServiceDTO = appServiceMapper.queryByToken(token);

        AppServiceVersionValueDTO appServiceVersionValueDTO = new AppServiceVersionValueDTO();
        AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
        OrganizationDTO organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        AppServiceVersionDTO newApplicationVersion = baseQueryByAppIdAndVersion(appServiceDTO.getId(), version);
        appServiceVersionDTO.setAppServiceId(appServiceDTO.getId());
        appServiceVersionDTO.setImage(image);
        appServiceVersionDTO.setCommit(commit);
        appServiceVersionDTO.setVersion(version);
        if (appServiceDTO.getChartConfigId() != null) {
            DevopsConfigDTO devopsConfigDTO = devopsConfigMapper.selectByPrimaryKey((appServiceDTO.getChartConfigId()));
            helmUrl = gson.fromJson(devopsConfigDTO.getConfig(), ConfigVO.class).getUrl();
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
    public PageInfo<AppServiceVersionVO> pageByOptions(Long projectId, Long appServiceId, Boolean deployOnly, Boolean doPage, Long appServiceVersionId, String version, PageRequest pageRequest) {
        PageInfo<AppServiceVersionDTO> applicationVersionDTOPageInfo;
        if (doPage) {
            applicationVersionDTOPageInfo = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), null)
                    .doSelectPageInfo(() -> appServiceVersionMapper.listByAppIdAndVersion(appServiceId, deployOnly, appServiceVersionId, version));
        } else {
            applicationVersionDTOPageInfo = new PageInfo<>();
            applicationVersionDTOPageInfo.setList(appServiceVersionMapper.listByAppIdAndVersion(appServiceId, deployOnly, appServiceVersionId, version));
        }
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
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(applicationDTO.getProjectId());
        OrganizationDTO organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        List<AppServiceVersionAndCommitVO> appServiceVersionAndCommitVOS = new ArrayList<>();

        appServiceVersionDTOS.forEach(applicationVersionDTO -> {
            AppServiceVersionAndCommitVO appServiceVersionAndCommitVO = new AppServiceVersionAndCommitVO();
            DevopsGitlabCommitDTO devopsGitlabCommitE = devopsGitlabCommitService.baseQueryByShaAndRef(applicationVersionDTO.getCommit(), branch);
            IamUserDTO userE = baseServiceClientOperator.queryUserByUserId(devopsGitlabCommitE.getUserId());
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
        return appServiceVersionMapper.queryValueByAppServiceId(appServiceId);
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
    public AppServiceVersionDTO baseCreateOrUpdate(AppServiceVersionDTO appServiceVersionDTO) {
        if (appServiceVersionDTO.getId() == null) {
            if (appServiceVersionMapper.insert(appServiceVersionDTO) != 1) {
                throw new CommonException("error.version.insert");
            }
        } else {
            if (appServiceVersionMapper.updateByPrimaryKey(appServiceVersionDTO) != 1) {
                throw new CommonException("error.version.update");
            }
        }
        return appServiceVersionDTO;
    }

    @Override
    public List<AppServiceVersionDTO> listServiceVersionByAppServiceIds(Set<Long> appServiceIds, String share) {

        return appServiceVersionMapper.listServiceVersionByAppServiceIds(appServiceIds, share);
    }

    @Override
    public List<AppServiceVersionVO> queryServiceVersionByAppServiceIdAndShare(Long appServiceId, String share) {
        List<AppServiceVersionDTO> versionList = appServiceVersionMapper.queryServiceVersionByAppServiceIdAndShare(appServiceId, share);
        List<AppServiceVersionVO> versionVoList = new ArrayList<>();
        if (!versionList.isEmpty()) {
            versionVoList = versionList.stream().map(this::dtoToVo).collect(Collectors.toList());
        }
        return versionVoList;
    }

    private AppServiceVersionVO dtoToVo(AppServiceVersionDTO appServiceVersionDTO) {
        AppServiceVersionVO appServiceVersionVO = new AppServiceVersionVO();
        BeanUtils.copyProperties(appServiceVersionDTO, appServiceVersionVO);
        return appServiceVersionVO;
    }

    @Override
    public List<AppServiceLatestVersionDTO> baseListAppNewestVersion(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        List<ProjectDTO> projectEList = baseServiceClientOperator.listIamProjectByOrgId(projectDTO.getOrganizationId(), null, null);
        List<Long> projectIds = projectEList.stream().map(ProjectDTO::getId)
                .collect(Collectors.toCollection(ArrayList::new));
        return appServiceVersionMapper.listAppNewestVersion(projectId, projectIds);
    }

    @Override
    public PageInfo<AppServiceVersionRespVO> pageShareVersionByAppId(Long appServiceId, PageRequest pageRequest, String params) {
        Map<String, Object> paramMap = TypeUtil.castMapParams(params);
        PageInfo<AppServiceVersionDTO> applicationDTOPageInfo = PageHelper.startPage(
                pageRequest.getPage(),
                pageRequest.getSize(),
                PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> appServiceVersionMapper.listShareVersionByAppId(appServiceId, TypeUtil.cast(paramMap.get(TypeUtil.PARAMS))));
        return ConvertUtils.convertPage(applicationDTOPageInfo, AppServiceVersionRespVO.class);
    }

    @Override
    public List<AppServiceVersionDTO> baseListByAppServiceId(Long appServiceId) {
        List<AppServiceVersionDTO> appServiceVersionDTOS = appServiceVersionMapper.listByAppId(appServiceId);
        if (appServiceVersionDTOS.isEmpty()) {
            return Collections.emptyList();
        }
        return appServiceVersionDTOS;
    }

    @Override
    public List<AppServiceVersionDTO> baseListAppDeployedVersion(Long projectId, Long appServiceId) {
        List<AppServiceVersionDTO> appServiceVersionDTOS =
                appServiceVersionMapper.listAppDeployedVersion(projectId, appServiceId);
        if (appServiceVersionDTOS.isEmpty()) {
            return Collections.emptyList();
        }
        return appServiceVersionDTOS;
    }

    @Override
    public AppServiceVersionDTO baseQuery(Long appServiceServiceId) {
        return appServiceVersionMapper.selectByPrimaryKey(appServiceServiceId);
    }

    @Override
    public List<AppServiceVersionDTO> baseListByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId) {
        return appServiceVersionMapper.listByAppIdAndEnvId(projectId, appServiceId, envId);
    }

    @Override
    public String baseQueryValue(Long versionId) {
        return appServiceVersionMapper.queryValue(versionId);
    }

    @Override
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

    @Override
    public void baseUpdatePublishLevelByIds(List<Long> appServiceServiceIds, Long level) {
        AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO();
        appServiceVersionDTO.setIsPublish(level);
        for (Long id : appServiceServiceIds) {
            appServiceVersionDTO.setId(id);
            appServiceVersionDTO.setObjectVersionNumber(appServiceVersionMapper.selectByPrimaryKey(id).getObjectVersionNumber());
            if (appServiceVersionDTO.getObjectVersionNumber() == null) {
                appServiceVersionDTO.setPublishTime(new java.sql.Date(new java.util.Date().getTime()));
                appServiceVersionMapper.updateObjectVersionNumber(id);
                appServiceVersionDTO.setObjectVersionNumber(1L);
            }
            appServiceVersionMapper.updateByPrimaryKeySelective(appServiceVersionDTO);
        }
    }

    @Override
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
        Map<String, Object> searchParamMap = TypeUtil.castMapParams(searchParam);
        applicationVersionDTOPageInfo = PageHelper
                .startPage(pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> appServiceVersionMapper.listApplicationVersion(projectId, appServiceId,
                        TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS)), isProjectOwner, userId));
        return applicationVersionDTOPageInfo;
    }

    @Override
    public List<AppServiceVersionDTO> baseListByPublished(Long applicationId) {
        return appServiceVersionMapper.listByPublished(applicationId);
    }

    @Override
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

    @Override
    public Long baseCreateReadme(String readme) {
        AppServiceVersionReadmeDTO appServiceVersionReadmeDTO = new AppServiceVersionReadmeDTO(readme);
        appServiceVersionReadmeMapper.insert(appServiceVersionReadmeDTO);
        return appServiceVersionReadmeDTO.getId();
    }

    @Override
    public String baseQueryReadme(Long readmeValueId) {
        String readme;
        try {
            readme = appServiceVersionReadmeMapper.selectByPrimaryKey(readmeValueId).getReadme();
        } catch (Exception ignore) {
            readme = "# 暂无";
        }
        return readme;
    }

    @Override
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

    @Override
    public List<AppServiceVersionDTO> baseListUpgradeVersion(Long appServiceServiceId) {
        return appServiceVersionMapper.listUpgradeVersion(appServiceServiceId);
    }


    @Override
    public void baseCheckByProjectAndVersionId(Long projectId, Long appServiceServiceId) {
        Integer index = appServiceVersionMapper.checkByProjectAndVersionId(projectId, appServiceServiceId);
        if (index == 0) {
            throw new CommonException("error.project.AppVersion.notExist");
        }
    }

    @Override
    public AppServiceVersionDTO baseQueryByCommitSha(Long appServiceId, String ref, String sha) {
        return appServiceVersionMapper.queryByCommitSha(appServiceId, ref, sha);
    }

    @Override
    public AppServiceVersionDTO baseQueryNewestVersion(Long appServiceId) {
        return appServiceVersionMapper.queryNewestVersion(appServiceId);
    }

    @Override
    public List<AppServiceVersionDTO> baseListByAppVersionIds(List<Long> appServiceServiceIds) {
        return appServiceVersionMapper.listByAppVersionIds(appServiceServiceIds);
    }

    @Override
    public List<AppServiceVersionDTO> baseListByAppIdAndBranch(Long appServiceId, String branch) {
        return appServiceVersionMapper.listByAppIdAndBranch(appServiceId, branch);
    }

    @Override
    public String baseQueryByPipelineId(Long pipelineId, String branch, Long appServiceId) {
        return appServiceVersionMapper.queryByPipelineId(pipelineId, branch, appServiceId);
    }

    @Override
    public String baseQueryValueByAppId(Long appServiceId) {
        return appServiceVersionMapper.queryValueByAppServiceId(appServiceId);
    }

}
