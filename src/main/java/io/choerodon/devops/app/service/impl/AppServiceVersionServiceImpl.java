package io.choerodon.devops.app.service.impl;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.ProjectConfigType;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.mapper.AppServiceVersionMapper;
import io.choerodon.devops.infra.mapper.AppServiceVersionReadmeMapper;
import io.choerodon.devops.infra.util.*;

@Service
public class AppServiceVersionServiceImpl implements AppServiceVersionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppServiceVersionServiceImpl.class);

    private static final String DESTINATION_PATH = "devops";
    private static final String STORE_PATH = "stores";
    private static final String APP_SERVICE = "appService";
    private static final String CHART = "chart";
    private static final String AUTHTYPE_PULL = "pull";

    private static final String ERROR_VERSION_INSERT = "error.version.insert";

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

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
    private DevopsConfigService devopsConfigService;


    private Gson gson = new Gson();

    /**
     * 方法中抛出{@link DevopsCiInvalidException}而不是{@link CommonException}是为了返回非200的状态码。
     */
    @Override
    public void create(String image, String harborConfigId, String token, String version, String commit, MultipartFile files) {
        try {
            doCreate(image, TypeUtil.objToLong(harborConfigId), token, version, commit, files);
        } catch (Exception e) {
            if (e instanceof CommonException) {
                throw new DevopsCiInvalidException(((CommonException) e).getCode(), e.getCause());
            }
            throw new DevopsCiInvalidException(e);
        }
    }

    private void doCreate(String image, Long harborConfigId, String token, String version, String commit, MultipartFile files) {
        AppServiceDTO appServiceDTO = appServiceMapper.queryByToken(token);

        AppServiceVersionValueDTO appServiceVersionValueDTO = new AppServiceVersionValueDTO();
        AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
        OrganizationDTO organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        AppServiceVersionDTO newApplicationVersion = baseQueryByAppServiceIdAndVersion(appServiceDTO.getId(), version);
        appServiceVersionDTO.setAppServiceId(appServiceDTO.getId());
        appServiceVersionDTO.setImage(image);
        appServiceVersionDTO.setCommit(commit);
        appServiceVersionDTO.setVersion(version);
        appServiceVersionDTO.setHarborConfigId(harborConfigId);

        DevopsConfigDTO devopsConfigDTO = devopsConfigService.queryRealConfig(appServiceDTO.getId(), APP_SERVICE, CHART,AUTHTYPE_PULL);
        String helmUrl = gson.fromJson(devopsConfigDTO.getConfig(), ConfigVO.class).getUrl();
        appServiceVersionDTO.setHelmConfigId(devopsConfigDTO.getId());

        appServiceVersionDTO.setRepository(helmUrl.endsWith("/") ? helmUrl + organization.getCode() + "/" + projectDTO.getCode() + "/" : helmUrl + "/" + organization.getCode() + "/" + projectDTO.getCode() + "/");
        String storeFilePath = STORE_PATH + version;

        String destFilePath = DESTINATION_PATH + version;
        String path = FileUtil.multipartFileToFile(storeFilePath, files);
        //上传chart包到chartmuseum
        chartUtil.uploadChart(helmUrl, organization.getCode(), projectDTO.getCode(), new File(path));

        FileUtil.unTarGZ(path, destFilePath);

        // 使用深度优先遍历查找文件, 避免查询到子chart的values值
        File valuesFile = FileUtil.queryFileFromFilesBFS(new File(destFilePath), "values.yaml");

        if (valuesFile == null) {
            FileUtil.deleteDirectories(storeFilePath, destFilePath);
            throw new CommonException("error.find.values.yaml.in.chart");
        }

        String values;
        try (FileInputStream fis = new FileInputStream(valuesFile)) {
            values = FileUtil.replaceReturnString(fis, null);
        } catch (IOException e) {
            FileUtil.deleteDirectories(storeFilePath, destFilePath);
            throw new CommonException(e);
        }

        // 有需求让重新上传chart包，所以校验重复推后
        if (newApplicationVersion != null) {
            try {
                // 重新上传chart包后更新values
                updateValues(newApplicationVersion.getId(), values);
            } finally {
                FileUtil.deleteDirectories(storeFilePath);
            }
            return;
        }

        try {
            FileUtil.checkYamlFormat(values);
        } catch (CommonException e) {
            FileUtil.deleteDirectories(storeFilePath, destFilePath);
            throw new CommonException("The format of the values.yaml in the chart is invalid!", e);
        }
        appServiceVersionValueDTO.setValue(values);
        try {
            appServiceVersionDTO.setValueId(appServiceVersionValueService
                    .baseCreate(appServiceVersionValueDTO).getId());
        } catch (Exception e) {
            FileUtil.deleteDirectories(storeFilePath, destFilePath);
            throw new CommonException(ERROR_VERSION_INSERT, e);
        }

        AppServiceVersionReadmeDTO appServiceVersionReadmeDTO = new AppServiceVersionReadmeDTO();
        appServiceVersionReadmeDTO.setReadme(FileUtil.getReadme(destFilePath));
        appServiceVersionReadmeMapper.insert(appServiceVersionReadmeDTO);

        appServiceVersionDTO.setReadmeValueId(appServiceVersionReadmeDTO.getId());
        baseCreate(appServiceVersionDTO);

        FileUtil.deleteDirectories(destFilePath, storeFilePath);
        //流水线
        checkAutoDeploy(appServiceVersionDTO);
    }

    private void updateValues(Long oldValuesId, String values) {
        AppServiceVersionValueDTO appServiceVersionValueDTO = new AppServiceVersionValueDTO();
        appServiceVersionValueDTO.setId(oldValuesId);

        AppServiceVersionValueDTO old = appServiceVersionValueService.baseQuery(oldValuesId);
        // values变了才更新
        if (!Objects.equals(old.getValue(), values)) {
            appServiceVersionValueDTO.setValue(values);
            appServiceVersionValueService.baseUpdate(appServiceVersionValueDTO);
        }
    }

    /**
     * 检测能够触发自动部署
     *
     * @param appServiceVersionDTO 版本
     */
    private void checkAutoDeploy(AppServiceVersionDTO appServiceVersionDTO) {
        AppServiceVersionDTO insertAppServiceVersionDTO = baseQueryByAppServiceIdAndVersion(appServiceVersionDTO.getAppServiceId(), appServiceVersionDTO.getVersion());

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
                if (!stageList.isEmpty()) {
                    List<Long> pipelineList = stageList.stream()
                            .map(stageId -> pipelineStageService.baseQueryById(stageId))
                            .filter(Objects::nonNull)
                            .map(PipelineStageDTO::getPipelineId)
                            .distinct()
                            .collect(Collectors.toList());

                    List<PipelineDTO> devopsPipelineDTOS = new ArrayList<>();
                    if (!pipelineList.isEmpty()) {
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
    public PageInfo<AppServiceVersionVO> pageByOptions(Long projectId, Long appServiceId, Long appServiceVersionId, Boolean deployOnly, Boolean doPage, String params, Pageable pageable, String version) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        Boolean market = appServiceDTO.getMktAppId() != null;
        PageInfo<AppServiceVersionDTO> applicationVersionDTOPageInfo = new PageInfo<>();
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        if (!market) {
            Boolean share = !(appServiceDTO.getProjectId() == null || appServiceDTO.getProjectId().equals(projectId));
            if (doPage != null && doPage) {
                applicationVersionDTOPageInfo = PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize(), PageRequestUtil.getOrderBy(pageable))
                        .doSelectPageInfo(() -> appServiceVersionMapper.listByAppServiceIdAndVersion(appServiceId,
                                appServiceVersionId,
                                projectId,
                                share,
                                deployOnly,
                                TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                                TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)),
                                PageRequestUtil.checkSortIsEmpty(pageable), version));
            } else {
                applicationVersionDTOPageInfo = new PageInfo<>();
                List<AppServiceVersionDTO> appServiceVersionDTOS = appServiceVersionMapper.listByAppServiceIdAndVersion(appServiceId, appServiceVersionId, projectId, share, deployOnly,
                        TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)),
                        PageRequestUtil.checkSortIsEmpty(pageable), version);
                if (doPage == null) {
                    applicationVersionDTOPageInfo.setList(appServiceVersionDTOS.stream().limit(20).collect(Collectors.toList()));
                } else {
                    applicationVersionDTOPageInfo.setList(appServiceVersionDTOS);
                }
            }
        } else {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
            List<Long> appServiceVersionIds = baseServiceClientOperator.listServiceVersionsForMarket(projectDTO.getOrganizationId(), deployOnly);
            if (appServiceVersionIds != null && !appServiceVersionIds.isEmpty()) {
                if (doPage != null && doPage) {
                    applicationVersionDTOPageInfo = PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize(), PageRequestUtil.getOrderBy(pageable))
                            .doSelectPageInfo(() -> appServiceVersionMapper.listByAppServiceVersionIdForMarket(appServiceId,
                                    appServiceVersionIds,
                                    TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                                    TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)),
                                    PageRequestUtil.checkSortIsEmpty(pageable), version));
                } else {
                    applicationVersionDTOPageInfo = new PageInfo<>();
                    List<AppServiceVersionDTO> appServiceVersionDTOS = appServiceVersionMapper.listByAppServiceVersionIdForMarket(appServiceId, appServiceVersionIds,
                            TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                            TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)),
                            PageRequestUtil.checkSortIsEmpty(pageable), version);
                    if (doPage == null) {
                        applicationVersionDTOPageInfo.setList(appServiceVersionDTOS.stream().limit(20).collect(Collectors.toList()));
                    } else {
                        applicationVersionDTOPageInfo.setList(appServiceVersionDTOS);
                    }
                }
            }
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
        return ConvertUtils.convertList(baseListByAppServiceVersionIds(appServiceServiceIds), AppServiceVersionRespVO.class);
    }

    @Override
    public List<AppServiceVersionAndCommitVO> listByAppIdAndBranch(Long appServiceId, String branch) {
        List<AppServiceVersionDTO> appServiceVersionDTOS = baseListByAppServiceIdAndBranch(appServiceId, branch);
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
        return ConvertUtils.convertObject(baseQueryByAppServiceIdAndVersion(appServiceId, version), AppServiceVersionRespVO.class);
    }

    @Override
    public AppServiceVersionDTO baseCreate(AppServiceVersionDTO appServiceVersionDTO) {
        if (appServiceVersionMapper.insert(appServiceVersionDTO) != 1) {
            throw new CommonException(ERROR_VERSION_INSERT);
        }
        return appServiceVersionDTO;
    }

    @Override
    public AppServiceVersionDTO baseCreateOrUpdate(AppServiceVersionDTO appServiceVersionDTO) {
        if (appServiceVersionDTO.getId() == null) {
            if (appServiceVersionMapper.insert(appServiceVersionDTO) != 1) {
                throw new CommonException(ERROR_VERSION_INSERT);
            }
        } else {
            if (appServiceVersionMapper.updateByPrimaryKey(appServiceVersionDTO) != 1) {
                throw new CommonException("error.version.update");
            }
        }
        return appServiceVersionDTO;
    }

    @Override
    public List<AppServiceVersionDTO> listServiceVersionByAppServiceIds(Set<Long> appServiceIds, String share, Long projectId, String params) {
        return appServiceVersionMapper.listServiceVersionByAppServiceIds(appServiceIds, share, projectId, params);
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

    @Override
    public List<AppServiceVersionVO> listServiceVersionVoByIds(Set<Long> ids) {
        return ConvertUtils.convertList(listServiceVersionByAppServiceIds(ids, null, null, null), AppServiceVersionVO.class);
    }

    @Override
    public List<AppServiceVersionVO> listVersionById(Long projectId, Long id, String params) {
        Set<Long> ids = new HashSet<>();
        ids.add(id);
        List<AppServiceVersionDTO> appServiceVersionDTOS = listAppServiceVersionByIdsAndProjectId(ids, projectId, params);
        return ConvertUtils.convertList(appServiceVersionDTOS, AppServiceVersionVO.class);
    }

    private AppServiceVersionVO dtoToVo(AppServiceVersionDTO appServiceVersionDTO) {
        AppServiceVersionVO appServiceVersionVO = new AppServiceVersionVO();
        BeanUtils.copyProperties(appServiceVersionDTO, appServiceVersionVO);
        return appServiceVersionVO;
    }

    @Override
    public List<AppServiceLatestVersionDTO> baseListAppNewestVersion(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        List<ProjectDTO> projectEList = baseServiceClientOperator.listIamProjectByOrgId(projectDTO.getOrganizationId());
        List<Long> projectIds = projectEList.stream().map(ProjectDTO::getId)
                .collect(Collectors.toCollection(ArrayList::new));
        return appServiceVersionMapper.listAppNewestVersion(projectId, projectIds);
    }

    @Override
    public PageInfo<AppServiceVersionRespVO> pageShareVersionByAppId(Long appServiceId, Pageable pageable, String params) {
        Map<String, Object> paramMap = TypeUtil.castMapParams(params);
        PageInfo<AppServiceVersionDTO> applicationDTOPageInfo = PageHelper.startPage(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                PageRequestUtil.getOrderBy(pageable)).doSelectPageInfo(() -> appServiceVersionMapper.listShareVersionByAppId(appServiceId, TypeUtil.cast(paramMap.get(TypeUtil.PARAMS))));
        return ConvertUtils.convertPage(applicationDTOPageInfo, AppServiceVersionRespVO.class);
    }

    @Override
    public List<AppServiceVersionDTO> baseListByAppServiceId(Long appServiceId) {
        List<AppServiceVersionDTO> appServiceVersionDTOS = appServiceVersionMapper.listByAppServiceId(appServiceId, null);
        if (appServiceVersionDTOS.isEmpty()) {
            return Collections.emptyList();
        }
        return appServiceVersionDTOS;
    }

    @Override
    public List<AppServiceVersionDTO> baseListAppDeployedVersion(Long projectId, Long appServiceId) {
        List<AppServiceVersionDTO> appServiceVersionDTOS =
                appServiceVersionMapper.listAppServiceDeployedVersion(projectId, appServiceId);
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
    public AppServiceVersionDTO baseQueryByAppServiceIdAndVersion(Long appServiceId, String version) {
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
    public PageInfo<AppServiceVersionDTO> basePageByOptions(Long projectId, Long appServiceId, Pageable pageable,
                                                            String searchParam, Boolean isProjectOwner,
                                                            Long userId) {
        Sort sort = pageable.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageable.getSort().iterator()).stream()
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
                .startPage(pageable.getPageNumber(), pageable.getPageSize(), sortResult).doSelectPageInfo(() -> appServiceVersionMapper.listApplicationVersion(projectId, appServiceId,
                        TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS)), isProjectOwner, userId));
        return applicationVersionDTOPageInfo;
    }

    @Override
    public void baseUpdate(AppServiceVersionDTO appServiceVersionDTO) {
        if (appServiceVersionMapper.updateByPrimaryKey(appServiceVersionDTO) != 1) {
            throw new CommonException("error.version.update");
        }
        //待修改readme
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
    public List<AppServiceVersionDTO> baseListByAppServiceVersionIds(List<Long> appServiceServiceIds) {
        return appServiceVersionMapper.listByAppServiceVersionIds(appServiceServiceIds);
    }

    @Override
    public List<AppServiceVersionDTO> baseListByAppServiceIdAndBranch(Long appServiceId, String branch) {
        return appServiceVersionMapper.listByAppServiceIdAndBranch(appServiceId, branch);
    }

    @Override
    public String baseQueryByPipelineId(Long pipelineId, String branch, Long appServiceId) {
        return appServiceVersionMapper.queryByPipelineId(pipelineId, branch, appServiceId);
    }

    @Override
    public List<AppServiceVersionDTO> baseListVersions(List<Long> appServiceVersionIds) {
        return appServiceVersionMapper.listVersions(appServiceVersionIds);
    }

    @Override
    public List<AppServiceVersionDTO> listAppServiceVersionByIdsAndProjectId(Set<Long> ids, Long projectId, String params) {
        List<AppServiceVersionDTO> appServiceVersionDTOS = new ArrayList<>();
        List<Long> appServiceIds = applicationService.listAppByProjectId(projectId, false, null, null)
                .getList().stream().map(AppServiceVO::getId).collect(Collectors.toList());
        Set<Long> localProjectIds = new HashSet<>();
        Set<Long> shareServiceIds = new HashSet<>();
        // 分别获取本项目的应用服务和共享服务
        ids.forEach(v -> {
            if (appServiceIds.contains(v)) {
                localProjectIds.add(v);
            } else {
                shareServiceIds.add(v);
            }
        });
        if (!localProjectIds.isEmpty()) {
            appServiceVersionDTOS.addAll(listServiceVersionByAppServiceIds(localProjectIds, null, projectId, params));
        }
        if (!shareServiceIds.isEmpty()) {
            // 查询共享服务的共享出来的版本
            List<AppServiceVersionDTO> shareServiceS = listServiceVersionByAppServiceIds(shareServiceIds, "share", projectId, params);
            List<AppServiceVersionDTO> projectSerivceS = listServiceVersionByAppServiceIds(shareServiceIds, "project", projectId, params);
            shareServiceS.addAll(projectSerivceS);
            // 去重
            ArrayList<AppServiceVersionDTO> collect1 = shareServiceS.stream().collect(collectingAndThen(
                    toCollection(() -> new TreeSet<>(comparing(AppServiceVersionDTO::getId))), ArrayList::new));
            appServiceVersionDTOS.addAll(collect1);
        }
        return appServiceVersionDTOS;
    }

    @Override
    public Boolean isVersionUseConfig(Long configId, String configType) {
        AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO();
        if (configType.equals(ProjectConfigType.HARBOR.getType())) {
            appServiceVersionDTO.setHarborConfigId(configId);
        } else {
            appServiceVersionDTO.setHelmConfigId(configId);
        }
        List<AppServiceVersionDTO> list = appServiceVersionMapper.select(appServiceVersionDTO);
        return !CollectionUtils.isEmpty(list);
    }

    @Override
    public void deleteByAppServiceId(Long appServiceId) {
        List<AppServiceVersionDTO> appServiceVersionDTOS = appServiceVersionMapper.listByAppServiceId(appServiceId, null);
        if(!CollectionUtils.isEmpty(appServiceVersionDTOS)){
            Set<Long> valueIds = new HashSet<>();
            Set<Long> readmeIds = new HashSet<>();
            Set<Long> configIds = new HashSet<>();
            Set<Long> versionIds = new HashSet<>();
            appServiceVersionDTOS.forEach(appServiceVersionDTO -> {
                versionIds.add(appServiceVersionDTO.getId());
                if(appServiceVersionDTO.getValueId() != null){
                    valueIds.add(appServiceVersionDTO.getValueId());
                }
                if(appServiceVersionDTO.getReadmeValueId() != null){
                    readmeIds.add(appServiceVersionDTO.getReadmeValueId());
                }

                if(appServiceVersionDTO.getHarborConfigId() != null){
                    configIds.add(appServiceVersionDTO.getHarborConfigId());
                }
                if(appServiceVersionDTO.getHelmConfigId() != null){
                    configIds.add(appServiceVersionDTO.getHelmConfigId());
                }

            });
            if(!CollectionUtils.isEmpty(valueIds)){
                appServiceVersionValueService.deleteByIds(valueIds);
            }
            if(!CollectionUtils.isEmpty(readmeIds)){
                appServiceVersionReadmeMapper.deleteByIds(readmeIds);
            }
            if(!CollectionUtils.isEmpty(configIds)){
                devopsConfigService.deleteByConfigIds(configIds);
            }
            appServiceVersionMapper.deleteByIds(versionIds);
        }
    }


}
