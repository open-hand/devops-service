package io.choerodon.devops.app.service.impl;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.AppShareClient;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.ApplicationShareMapper;
import io.choerodon.devops.infra.mapper.ApplicationVersionMapper;
import io.choerodon.devops.infra.mapper.ApplicationVersionReadmeMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.websocket.tool.UUIDTool;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.Response;



/**
 * Created by ernst on 2018/5/12.
 */
@Service
public class ApplicationShareServiceImpl implements ApplicationShareService {
    private static final String CHARTS = "charts";
    private static final String CHART = "chart";
    private static final String ORGANIZATION = "organization";
    private static final String PROJECTS = "projects";
    private static final String IMAGES = "images";
    private static final String PUSH_IAMGES = "push_image.sh";
    private static final String JSON_FILE = ".json";

    private static final String FILE_SEPARATOR = "/";
    private static final Logger logger = LoggerFactory.getLogger(ApplicationShareServiceImpl.class);

    private static Gson gson = new Gson();
    private JSON json = new JSON();

    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${services.helm.url}")
    private String helmUrl;

    @Autowired
    private IamService iamService;
    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;
    @Autowired
    private ApplicationVersionReadmeMapper applicationVersionReadmeMapper;
    @Autowired
    private MarketConnectInfoService marketConnectInfoService;
    @Autowired
    private ChartUtil chartUtil;
    @Autowired
    private ApplicationShareMapper applicationShareMapper;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private ApplicationVersionService applicationVersionService;
    @Autowired
    private ApplicationShareResourceService applicationShareResourceService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private DevopsProjectConfigService devopsProjectConfigService;
    @Autowired
    private ApplicationVersionValueService applicationVersionValueService;
    @Autowired
    private ApplicationVersionMapper applicationVersionMapper;

    @Override
    @Transactional
    public Long create(Long projectId, ApplicationReleasingVO applicationReleasingVO) {
        List<Long> ids;
        if (applicationReleasingVO == null) {
            throw new CommonException("error.app.check");
        }
        String publishLevel = applicationReleasingVO.getPublishLevel();
        if (!ORGANIZATION.equals(publishLevel) && !PROJECTS.equals(publishLevel)) {
            throw new CommonException("error.publishLevel");
        }
        ApplicationShareDTO applicationShareDTO = new ApplicationShareDTO();
        //校验应用和版本
        if (projectId != null) {
            baseCheckPub(applicationReleasingVO.getAppId());
            List<AppMarketVersionVO> appVersions = applicationReleasingVO.getAppVersions();
            ids = appVersions.stream().map(AppMarketVersionVO::getId)
                    .collect(Collectors.toCollection(ArrayList::new));
            applicationVersionService.baseCheckByAppIdAndVersionIds(applicationReleasingVO.getAppId(), ids);
            applicationVersionService.baseUpdatePublishLevelByIds(ids, 1L);
            applicationShareDTO.setAppId(applicationReleasingVO.getAppId());
            applicationShareDTO.setPublishLevel(applicationReleasingVO.getPublishLevel());
            applicationShareDTO.setActive(true);
            applicationShareDTO.setContributor(applicationReleasingVO.getContributor());
            applicationShareDTO.setDescription(applicationReleasingVO.getDescription());
            applicationShareDTO.setCategory(applicationReleasingVO.getCategory());
            applicationShareDTO.setImgUrl(applicationReleasingVO.getImgUrl());
            applicationShareDTO.setFree(applicationReleasingVO.getFree());
        } else {
            applicationShareDTO.setId(applicationReleasingVO.getId());
            applicationShareDTO.setSite(true);
        }
        applicationShareDTO = baseCreateOrUpdate(applicationShareDTO);
        Long shareId = applicationShareDTO.getId();
        if (PROJECTS.equals(applicationReleasingVO.getPublishLevel())) {
            applicationReleasingVO.getProjectDTOS().forEach(t -> applicationShareResourceService.baseCreate(new ApplicationShareResourceDTO(shareId, t.getId())));
        }
        return baseQueryByAppId(applicationReleasingVO.getAppId()).getId();
    }

    @Override
    public PageInfo<ApplicationReleasingVO> pageByOptions(Long projectId, PageRequest pageRequest,
                                                          String searchParam) {
        PageInfo<ApplicationReleasingVO> applicationReleasingVOPageInfo = ConvertUtils.convertPage(
                basePageByProjectId(projectId, pageRequest, searchParam), ApplicationReleasingVO.class);
        List<ApplicationReleasingVO> releasingVOPageInfoList = applicationReleasingVOPageInfo.getList();
        releasingVOPageInfoList.forEach(t -> {
            if (PROJECTS.equals(t.getPublishLevel())) {
                List<ProjectReqVO> projectDTOS = applicationShareResourceService.baseListByShareId(t.getId()).stream()
                        .map(appShareResourceE -> {
                            ProjectDTO projectDTO = iamService.queryIamProject(appShareResourceE.getProjectId());
                            ProjectReqVO projectReqVO = new ProjectReqVO();
                            BeanUtils.copyProperties(projectDTO, projectReqVO);
                            return projectReqVO;
                        })
                        .collect(Collectors.toList());
                t.setProjectDTOS(projectDTOS);
            }
        });
        applicationReleasingVOPageInfo.setList(releasingVOPageInfoList);
        return applicationReleasingVOPageInfo;
    }

    @Override
    public AppVersionAndValueVO getValuesAndChart(Long versionId) {
        AppVersionAndValueVO appVersionAndValueVO = new AppVersionAndValueVO();
        String versionValue = FileUtil.checkValueFormat(applicationVersionService.baseQueryValue(versionId));
        ApplicationVersionRemoteVO versionRemoteDTO = new ApplicationVersionRemoteVO();
        versionRemoteDTO.setValues(versionValue);
        ApplicationVersionDTO applicationVersionDTO = applicationVersionService.baseQuery(versionId);
        if (applicationVersionDTO != null) {
            versionRemoteDTO.setRepository(applicationVersionDTO.getRepository());
            versionRemoteDTO.setVersion(applicationVersionDTO.getVersion());
            versionRemoteDTO.setImage(applicationVersionDTO.getImage());
            versionRemoteDTO.setReadMeValue(applicationVersionReadmeMapper.selectByPrimaryKey(applicationVersionDTO.getReadmeValueId()).getReadme());
            ApplicationDTO applicationDTO = applicationService.baseQuery(applicationVersionDTO.getAppId());
            if (applicationDTO.getHarborConfigId() == null) {
                appVersionAndValueVO.setHarbor(gson.fromJson(devopsProjectConfigService.baseQueryByName(null, "harbor_default").getConfig(), ProjectConfigVO.class));
                appVersionAndValueVO.setChart(gson.fromJson(devopsProjectConfigService.baseQueryByName(null, "chart_default").getConfig(), ProjectConfigVO.class));
            } else {
                appVersionAndValueVO.setHarbor(gson.fromJson(devopsProjectConfigService.baseQuery(applicationDTO.getHarborConfigId()).getConfig(), ProjectConfigVO.class));
                appVersionAndValueVO.setChart(gson.fromJson(devopsProjectConfigService.baseQuery(applicationDTO.getChartConfigId()).getConfig(), ProjectConfigVO.class));
            }
            appVersionAndValueVO.setVersionRemoteDTO(versionRemoteDTO);
        }
        return appVersionAndValueVO;
    }

    @Override
    public PageInfo<ApplicationReleasingVO> listMarketApps(Long projectId, PageRequest pageRequest, String searchParam) {
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        if (projectDTO != null && projectDTO.getOrganizationId() != null) {
            Long organizationId = projectDTO.getOrganizationId();
            List<ProjectDTO> projectEList = iamService.listIamProjectByOrgId(organizationId, null, null);
            List<Long> projectIds = new ArrayList<>();
            if (projectEList != null) {
                projectIds = projectEList.stream().map(ProjectDTO::getId).collect(Collectors.toList());
            }
            PageInfo<ApplicationShareDTO> applicationMarketEPage = basePageByProjectIds(projectIds, pageRequest, searchParam);

            return ConvertUtils.convertPage(applicationMarketEPage, ApplicationReleasingVO.class);
        }
        return null;
    }

    @Override
    public ApplicationReleasingVO queryById(Long projectId, Long appMarketId) {
        ApplicationShareDTO applicationShareDTO = baseQuery(projectId, appMarketId);
        List<ApplicationShareVersionDTO> versionDOList = baseListByOptions(projectId, appMarketId, true);
        List<AppMarketVersionVO> appMarketVersionVOList = ConvertHelper
                .convertList(versionDOList, AppMarketVersionVO.class);
        ApplicationReleasingVO applicationReleasingDTO =
                ConvertHelper.convert(applicationShareDTO, ApplicationReleasingVO.class);
        applicationReleasingDTO.setAppVersions(appMarketVersionVOList);

        return applicationReleasingDTO;
    }

    @Override
    public ApplicationReleasingVO queryShareApp(Long appMarketId, Long versionId) {
        ApplicationShareDTO applicationShareDTO = baseQuery(null, appMarketId);
        ApplicationDTO applicationDTO = applicationService.baseQuery(applicationShareDTO.getId());
        List<ApplicationShareVersionDTO> versionDOList = baseListByOptions(null, appMarketId, true);
        List<AppMarketVersionVO> appMarketVersionVOList = ConvertHelper
                .convertList(versionDOList, AppMarketVersionVO.class)
                .stream()
                .sorted(this::compareAppMarketVersionDTO)
                .collect(Collectors.toCollection(ArrayList::new));
        ApplicationReleasingVO applicationReleasingDTO =
                ConvertHelper.convert(applicationShareDTO, ApplicationReleasingVO.class);
        applicationReleasingDTO.setAppVersions(appMarketVersionVOList);

        Long applicationId = applicationDTO.getId();
        applicationDTO = applicationService.baseQuery(applicationId);

        Date latestUpdateDate = appMarketVersionVOList.isEmpty()
                ? getLaterDate(applicationDTO.getLastUpdateDate(), applicationShareDTO.getMarketUpdatedDate())
                : getLatestDate(
                appMarketVersionVOList.get(0).getUpdatedDate(),
                applicationDTO.getLastUpdateDate(),
                applicationShareDTO.getMarketUpdatedDate());
        applicationReleasingDTO.setLastUpdatedDate(latestUpdateDate);

        Boolean versionExist = appMarketVersionVOList.stream().anyMatch(t -> t.getId().equals(versionId));
        Long latestVersionId = versionId;
        if (!versionExist) {
            Optional<AppMarketVersionVO> optional = appMarketVersionVOList.stream()
                    .max(this::compareAppMarketVersionDTO);
            latestVersionId = optional.isPresent()
                    ? optional.get().getId()
                    : versionId;
        }
        ApplicationVersionDTO applicationVersionDTO = applicationVersionService.baseQuery(latestVersionId);
        String readme = applicationVersionService.baseQueryReadme(applicationVersionDTO.getReadmeValueId());

        applicationReleasingDTO.setReadme(readme);

        return applicationReleasingDTO;
    }

    @Override
    public String queryAppVersionReadme(Long appMarketId, Long versionId) {
        baseCheckByShareIdAndVersion(appMarketId, versionId);
        ApplicationVersionDTO applicationVersionDTO = applicationVersionService.baseQuery(versionId);
        return applicationVersionService.baseQueryReadme(applicationVersionDTO.getReadmeValueId());
    }

    @Override
    public void unpublish(Long projectId, Long appMarketId) {
        baseCheckByProjectId(projectId, appMarketId);
        baseCheckByDeployed(projectId, appMarketId, null, null);
        baseUnsharedApplication(appMarketId);
    }

    @Override
    public void unpublish(Long projectId, Long appMarketId, Long versionId) {
        baseCheckByProjectId(projectId, appMarketId);
        baseCheckByDeployed(projectId, appMarketId, versionId, null);
        baseUnsharedApplicationVersion(appMarketId, versionId);

    }

    @Override
    public void update(Long projectId, Long appMarketId, ApplicationReleasingVO applicationRelease) {
        if (applicationRelease != null) {
            String publishLevel = applicationRelease.getPublishLevel();
            if (publishLevel != null
                    && !ORGANIZATION.equals(publishLevel)
                    && !PROJECTS.equals(publishLevel)) {
                throw new CommonException("error.publishLevel");
            }
        } else {
            throw new CommonException("error.app.check");
        }
        if (applicationRelease.getId() != null
                && !appMarketId.equals(applicationRelease.getId())) {
            throw new CommonException("error.id.notMatch");
        }
        baseCheckByProjectId(projectId, appMarketId);
        ApplicationReleasingVO applicationReleasingDTO = queryById(projectId, appMarketId);
        if (applicationRelease.getAppId() != null
                && !applicationReleasingDTO.getAppId().equals(applicationRelease.getAppId())) {
            throw new CommonException("error.app.cannot.change");
        }
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        if (projectDTO == null || projectDTO.getOrganizationId() == null) {
            throw new CommonException("error.project.query");
        }
        if (applicationRelease.getPublishLevel() != null
                && !applicationRelease.getPublishLevel().equals(applicationReleasingDTO.getPublishLevel())) {
            throw new CommonException("error.publishLevel.cannot.change");
        }
        ApplicationShareDTO devopsAppMarketDO = ConvertHelper.convert(applicationRelease, ApplicationShareDTO.class);
        if (!ConvertHelper.convert(applicationReleasingDTO, ApplicationShareDTO.class).equals(devopsAppMarketDO)) {
            baseUpdate(devopsAppMarketDO);
        }
    }

    @Override
    public void update(Long projectId, Long appMarketId, List<AppMarketVersionVO> versionDTOList) {
        baseCheckByProjectId(projectId, appMarketId);

        ApplicationReleasingVO applicationReleasingDTO = queryById(projectId, appMarketId);

        List<Long> ids = versionDTOList.stream()
                .map(AppMarketVersionVO::getId).collect(Collectors.toCollection(ArrayList::new));

        applicationVersionService.baseCheckByAppIdAndVersionIds(applicationReleasingDTO.getAppId(), ids);
        applicationVersionService.baseUpdatePublishLevelByIds(ids, 1L);
    }

    @Override
    public List<AppMarketVersionVO> queryAppVersionsById(Long projectId, Long appMarketId, Boolean isPublish) {
        return ConvertHelper.convertList(baseListByOptions(projectId, appMarketId, isPublish),
                AppMarketVersionVO.class);
    }

    @Override
    public PageInfo<AppMarketVersionVO> queryAppVersionsById(Long projectId, Long appMarketId, Boolean isPublish,
                                                             PageRequest pageRequest, String searchParam) {
        return ConvertUtils.convertPage(
                basePageByOptions(projectId, appMarketId, isPublish, pageRequest, searchParam),
                AppMarketVersionVO.class);
    }

    @Override
    public AppMarketTgzVO upload(Long projectId, MultipartFile file) {
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
        String dirName = UUIDTool.genUuid();
        String classPath = String.format(
                "tmp%s%s%s%s",
                FILE_SEPARATOR,
                organizationDTO.getCode(),
                FILE_SEPARATOR,
                projectDTO.getCode());

        String destPath = String.format("%s%s%s", classPath, FILE_SEPARATOR, dirName);
        String path = FileUtil.multipartFileToFileWithSuffix(destPath, file, ".zip");
        FileUtil.unZipFiles(new File(path), destPath);
        FileUtil.deleteFile(path);
        File zipDirectory = new File(destPath);
        AppMarketTgzVO appMarketTgzVO = new AppMarketTgzVO();

        if (zipDirectory.exists() && zipDirectory.isDirectory()) {
            File[] chartsDirectory = zipDirectory.listFiles();
            if (chartsDirectory != null && chartsDirectory.length == 1) {
                File[] appFiles = chartsDirectory[0].listFiles();
                if (appFiles == null || appFiles.length == 0) {
                    FileUtil.deleteDirectory(zipDirectory);
                    throw new CommonException("error.file.empty");
                }

                List<File> appFileList = Arrays.stream(appFiles)
                        .filter(File::isDirectory).collect(Collectors.toCollection(ArrayList::new));
                // do sth with appFileList
                analyzeAppFile(appMarketTgzVO.getAppMarketList(), appFileList);
            } else {
                FileUtil.deleteDirectory(zipDirectory);
                throw new CommonException("error.zip.illegal");
            }
        } else {
            FileUtil.deleteDirectory(zipDirectory);
            throw new CommonException("error.zip.empty");
        }
        appMarketTgzVO.setFileCode(dirName);
        return appMarketTgzVO;
    }

    @Override
    public Boolean importApps(Long projectId, String fileName, Boolean isPublic) {
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
        String destPath = String.format(
                "tmp%s%s%s%s%s%s",
                FILE_SEPARATOR,
                organizationDTO.getCode(),
                FILE_SEPARATOR,
                projectDTO.getCode(),
                FILE_SEPARATOR,
                fileName);
        File zipDirectory = new File(destPath);

        if (zipDirectory.exists() && zipDirectory.isDirectory()) {
            File[] chartsDirectory = zipDirectory.listFiles();
            File[] appFiles = chartsDirectory != null ? chartsDirectory[0].listFiles() : new File[0];
            if (appFiles == null || appFiles.length == 0) {
                FileUtil.deleteDirectory(zipDirectory);
                throw new CommonException("error.file.empty");
            }
            List<File> appFileList = Arrays.stream(appFiles)
                    .filter(File::isDirectory).collect(Collectors.toCollection(ArrayList::new));
            importAppFile(projectId, appFileList, isPublic);

        } else {
            throw new CommonException("error.zip.notFound");
        }
        FileUtil.deleteDirectory(zipDirectory);
        return true;
    }

    @Override
    public void importCancel(Long projectId, String fileName) {
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
        String destPath = String.format(
                "tmp%s%s%s%s%s%s",
                FILE_SEPARATOR,
                organizationDTO.getCode(),
                FILE_SEPARATOR,
                projectDTO.getCode(),
                FILE_SEPARATOR,
                fileName);
        File zipDirectory = new File(destPath);
        FileUtil.deleteDirectory(zipDirectory);
    }

    /**
     * 导出应用市场应用 zip
     *
     * @param appMarkets 应用市场应用信息
     */
    @Override
    public void export(List<AppMarketDownloadVO> appMarkets, String fileName) {
        List<String> images = new ArrayList<>();
        for (AppMarketDownloadVO appMarketDownloadVO : appMarkets) {
            ApplicationReleasingVO applicationReleasingDTO = queryShareApp(appMarketDownloadVO.getAppMarketId(), null);
            String destpath = String.format("charts%s%s",
                    FILE_SEPARATOR,
                    applicationReleasingDTO.getCode());
            ApplicationDTO applicationDTO = applicationService.baseQuery(applicationReleasingDTO.getAppId());
            ProjectDTO projectDTO = iamService.queryIamProject(applicationDTO.getProjectId());
            OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
            applicationReleasingDTO.setAppVersions(
                    applicationReleasingDTO.getAppVersions().stream()
                            .filter(t -> appMarketDownloadVO.getAppVersionIds().contains(t.getId()))
                            .collect(Collectors.toCollection(ArrayList::new))
            );
            String appMarketJson = gson.toJson(applicationReleasingDTO);
            FileUtil.saveDataToFile(destpath, applicationReleasingDTO.getCode() + JSON_FILE, appMarketJson);
            //下载chart taz包
            getChart(images, appMarketDownloadVO, destpath, applicationDTO, projectDTO, organizationDTO);
            StringBuilder stringBuilder = new StringBuilder();
            for (String image : images) {
                stringBuilder.append(image);
                stringBuilder.append(System.getProperty("line.separator"));
            }
            InputStream inputStream = this.getClass().getResourceAsStream("/shell/push_image.sh");
            FileUtil.saveDataToFile(fileName, PUSH_IAMGES, FileUtil.replaceReturnString(inputStream, null));
            FileUtil.saveDataToFile(fileName, IMAGES, stringBuilder.toString());
            FileUtil.moveFiles(CHARTS, fileName);
        }
        try (FileOutputStream outputStream = new FileOutputStream(fileName + ".zip")) {
            FileUtil.toZip(fileName, outputStream, true);
            FileUtil.deleteDirectory(new File(CHARTS));
            FileUtil.deleteDirectory(new File(fileName));
        } catch (IOException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    @Override
    public AccessTokenCheckResultVO checkToken(AccessTokenVO tokenDTO) {
        AppShareClient appShareClient = RetrofitHandler.getAppShareClient(tokenDTO.getSaasMarketUrl());
        Response<AccessTokenCheckResultVO> tokenDTOResponse = null;

        try {
            tokenDTOResponse = appShareClient.checkTokenExist(tokenDTO.getAccessToken()).execute();
            if (!tokenDTOResponse.isSuccessful()) {
                throw new CommonException("error.check.token");
            }
        } catch (IOException e) {
            throw new CommonException("error.check.token");
        }
        return tokenDTOResponse.body();
    }

    @Override
    public void saveToken(AccessTokenVO tokenDTO) {
        DevopsMarketConnectInfoDTO connectInfoDO = new DevopsMarketConnectInfoDTO();
        BeanUtils.copyProperties(tokenDTO, connectInfoDO);
        marketConnectInfoService.baseCreateOrUpdate(connectInfoDO);
    }

    public ApplicationShareDTO baseCreateOrUpdate(ApplicationShareDTO applicationShareDTO) {
        if (applicationShareDTO.getId() == null) {
            applicationShareMapper.insert(applicationShareDTO);
        } else {
            applicationShareDTO.setObjectVersionNumber(applicationShareMapper.selectByPrimaryKey(applicationShareDTO).getObjectVersionNumber());
            applicationShareMapper.updateByPrimaryKeySelective(applicationShareDTO);
        }
        return applicationShareDTO;
    }

    public PageInfo<ApplicationShareDTO> basePageByProjectId(Long projectId, PageRequest pageRequest, String searchParam) {
        PageInfo<ApplicationShareDTO> applicationShareDTOPageInfo;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            applicationShareDTOPageInfo = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationShareMapper.listMarketApplicationInProject(
                    projectId,
                    TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM))));
        } else {
            applicationShareDTOPageInfo = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationShareMapper.listMarketApplicationInProject(projectId, null, null));
        }
        return applicationShareDTOPageInfo;
    }

    public PageInfo<ApplicationShareDTO> basePageBySite(Boolean isSite, Boolean isFree, PageRequest pageRequest, String searchParam) {

        Map<String, Object> mapParams = TypeUtil.castMapParams(searchParam);

        PageInfo<ApplicationShareDTO> applicationShareDTOPageInfo = PageHelper
                .startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                        applicationShareMapper.listMarketAppsBySite(isSite, isFree, (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM), (String) mapParams.get(TypeUtil.PARAM)));
        return applicationShareDTOPageInfo;
    }


    public PageInfo<ApplicationShareDTO> basePageByProjectIds(List<Long> projectIds, PageRequest pageRequest, String searchParam) {
        PageInfo<ApplicationShareDTO> applicationShareDTOPageInfo;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            applicationShareDTOPageInfo = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationShareMapper.listMarketApplication(
                    projectIds,
                    TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM))));
        } else {
            applicationShareDTOPageInfo = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationShareMapper.listMarketApplication(projectIds, null, null));
        }
        return applicationShareDTOPageInfo;
    }

    public ApplicationShareDTO baseQuery(Long projectId, Long shareId) {
        List<Long> projectIds = getProjectIds(projectId);
        return applicationShareMapper.queryByShareId(projectId, shareId, projectIds);
    }

    public Boolean baseCheckPub(Long appId) {

        int selectCount = applicationShareMapper.countByAppId(appId);
        if (selectCount > 0) {
            throw new CommonException("error.app.market.check");
        }
        return true;
    }

    public Long baseQueryShareIdByAppId(Long appId) {
        return applicationShareMapper.baseQueryShareIdByAppId(appId);
    }

    public void baseCheckByProjectId(Long projectId, Long shareId) {
        if (applicationShareMapper.checkByProjectId(projectId, shareId) != 1) {
            throw new CommonException("error.appMarket.project.unmatch");
        }
    }

    public void baseCheckByDeployed(Long projectId, Long shareId, Long versionId, List<Long> projectIds) {
        if (applicationShareMapper.checkByDeployed(projectId, shareId, versionId, projectIds) > 0) {
            throw new CommonException("error.appMarket.instance.deployed");
        }
    }

    public void baseUnsharedApplication(Long shareId) {
        applicationShareMapper.changeApplicationVersions(shareId, null, null);
        applicationShareMapper.deleteByPrimaryKey(shareId);
    }

    public void baseUnsharedApplicationVersion(Long shareId, Long versionId) {
        applicationShareMapper.changeApplicationVersions(shareId, versionId, null);
    }

    public void updateVersion(Long appMarketId, Long versionId, Boolean isPublish) {
        applicationShareMapper.changeApplicationVersions(appMarketId, versionId, isPublish);
    }

    public void baseUpdate(ApplicationShareDTO applicationShareDTO) {
        applicationShareDTO.setObjectVersionNumber(
                applicationShareMapper.selectByPrimaryKey(applicationShareDTO.getId()).getObjectVersionNumber());
        if (applicationShareMapper.updateByPrimaryKeySelective(applicationShareDTO) != 1) {
            throw new CommonException("error.update.share.application");
        }
    }

    public List<ApplicationShareVersionDTO> baseListByOptions(Long projectId, Long shareId, Boolean isPublish) {
        List<Long> projectIds = getProjectIds(projectId);
        return applicationShareMapper.listAppVersions(projectIds, shareId, isPublish, null, null);
    }


    public PageInfo<ApplicationShareVersionDTO> basePageByOptions(Long projectId, Long shareId, Boolean isPublish,
                                                                  PageRequest pageRequest, String params) {
        Sort sort = pageRequest.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if (property.equals("version")) {
                            property = "dav.version";
                        } else if (property.equals("updatedDate")) {
                            property = "dav.last_update_date";
                        } else if (property.equals("creationDate")) {
                            property = "dav.creation_date";
                        }

                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }

        Map<String, Object> searchParam = null;
        String param = null;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> searchParamMap = json.deserialize(params, Map.class);
            searchParam = TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM));
            param = TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM));
        }
        Map<String, Object> finalSearchParam = searchParam;
        String finalParam = param;
        List<Long> projectIds = getProjectIds(projectId);
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(
                () -> applicationShareMapper.listAppVersions(
                        projectIds, shareId, isPublish,
                        finalSearchParam, finalParam));
    }

    @Override
    public ApplicationShareDTO baseQueryByAppId(Long appId) {
        ApplicationShareDTO applicationShareDTO = new ApplicationShareDTO();
        applicationShareDTO.setAppId(appId);
        return applicationShareMapper.selectOne(applicationShareDTO);
    }

    @Override
    public int baseCountByAppId(Long appId) {
        return applicationShareMapper.countByAppId(appId);
    }

    public void baseCheckByShareIdAndVersion(Long shareId, Long versionId) {
        if (!applicationShareMapper.checkByShareIdAndVersion(shareId, versionId)) {
            throw new CommonException("error.version.notMatch");
        }
    }

    private List<Long> getProjectIds(Long projectId) {
        List<Long> projectIds;
        if (projectId != null) {
            ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(projectId);
            List<ProjectDTO> projectEList = iamServiceClientOperator.listIamProjectByOrgId(projectDTO.getOrganizationId(), null, null);
            projectIds = projectEList.stream().map(ProjectDTO::getId)
                    .collect(Collectors.toCollection(ArrayList::new));
        } else {
            projectIds = null;
        }
        return projectIds;
    }

    public PageInfo<ApplicationShareDTO> basePageByShareIds(PageRequest pageRequest, String param, List<Long> shareIds) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(param);
        PageInfo<ApplicationShareDTO> applicationShareDTOPageInfo = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
                () -> applicationShareMapper.queryByShareIds((Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM), (String) mapParams.get(TypeUtil.PARAM), shareIds));
        return applicationShareDTOPageInfo;
    }

    private Date getLatestDate(Date a, Date b, Date c) {
        if (a.after(b)) {
            return getLaterDate(a, c);
        } else {
            return getLaterDate(b, c);
        }
    }

    private Date getLaterDate(Date a, Date b) {
        return a.after(b) ? a : b;
    }

    private Integer compareAppMarketVersionDTO(AppMarketVersionVO s, AppMarketVersionVO t) {
        if (s.getUpdatedDate().before(t.getUpdatedDate())) {
            return 1;
        } else {
            if (s.getUpdatedDate().after(t.getUpdatedDate())) {
                return -1;
            } else {
                if (s.getCreationDate().before(t.getCreationDate())) {
                    return 1;
                } else {
                    return s.getCreationDate().after(t.getCreationDate()) ? -1 : 0;
                }
            }
        }
    }

    private void analyzeAppFile(List<ApplicationReleasingVO> appMarketVersionDTOS,
                                List<File> appFileList) {
        appFileList.forEach(t -> {
            String appName = t.getName();
            File[] appFiles = t.listFiles();
            if (appFiles != null && !appFileList.isEmpty()) {
                String appFileName = String.format("%s%s", appName, JSON_FILE);
                List<File> appMarkets = Arrays.stream(appFiles).parallel()
                        .filter(k -> k.getName().equals(appFileName))
                        .collect(Collectors.toCollection(ArrayList::new));
                if (!appMarkets.isEmpty() && appMarkets.size() == 1) {
                    File appMarket = appMarkets.get(0);
                    String appMarketJson = FileUtil.getFileContent(appMarket);
                    ApplicationReleasingVO appMarketVersionDTO =
                            gson.fromJson(appMarketJson, ApplicationReleasingVO.class);
                    appMarketVersionDTOS.add(appMarketVersionDTO);
                }
            }
        });
    }

    private void importAppFile(Long projectId, List<File> appFileList, Boolean isPublic) {
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
        String orgCode = organizationDTO.getCode();
        String projectCode = projectDTO.getCode();
        appFileList.stream().forEach(t -> {
            String appName = t.getName();
            File[] appFiles = t.listFiles();
            if (appFiles != null && !appFileList.isEmpty()) {
                String appFileName = String.format("%s%s", appName, JSON_FILE);
                List<File> appMarkets = Arrays.stream(appFiles).parallel()
                        .filter(k -> k.getName().equals(appFileName))
                        .collect(Collectors.toCollection(ArrayList::new));
                if (appMarkets != null && !appMarkets.isEmpty() && appMarkets.size() == 1) {
                    File appMarket = appMarkets.get(0);
                    String appMarketJson = FileUtil.getFileContent(appMarket);
                    ApplicationReleasingVO applicationReleasingDTO =
                            gson.fromJson(appMarketJson, ApplicationReleasingVO.class);
                    ApplicationDTO applicationDTO = new ApplicationDTO();
                    String appCode = applicationReleasingDTO.getCode();
                    applicationDTO.setName(applicationReleasingDTO.getName());
                    applicationDTO.setIsSkipCheckPermission(true);
                    applicationDTO.setType("normal");
                    Long appId = createOrUpdateApp(applicationDTO, appCode, projectId);
                    Boolean isVersionPublish = isPublic != null;
                    applicationReleasingDTO.getAppVersions().stream()
                            .forEach(appVersion -> createVersion(
                                    appVersion, orgCode, projectCode, appCode, appId, appFiles, isVersionPublish
                            ));
                    // 发布应用
                    releaseApp(isPublic, applicationReleasingDTO, appId);
                }
            }
        });
    }


    private void getChart(List<String> images, AppMarketDownloadVO appMarketDownloadVO, String destpath, ApplicationDTO applicationDTO, ProjectDTO projectDTO, OrganizationDTO organizationDTO) {
        appMarketDownloadVO.getAppVersionIds().forEach(appVersionId -> {

            ApplicationVersionDTO applicationVersionDTO = applicationVersionService.baseQuery(appVersionId);
            images.add(applicationVersionDTO.getImage());
            chartUtil.downloadChart(applicationVersionDTO, organizationDTO, projectDTO, applicationDTO, destpath);
        });
    }

    private void createVersion(AppMarketVersionVO appVersion,
                               String organizationCode,
                               String projectCode,
                               String appCode,
                               Long appId,
                               File[] appFiles,
                               Boolean isVersionPublish) {
        ApplicationVersionDTO applicationVersionDTO = new ApplicationVersionDTO();
        String image = String.format("%s%s%s%s%s%s%s%s%s", harborConfigurationProperties.getBaseUrl(),
                FILE_SEPARATOR,
                organizationCode,
                "-",
                projectCode,
                FILE_SEPARATOR,
                appCode,
                ":",
                appVersion.getVersion()
        );
        applicationVersionDTO.setImage(image);
        helmUrl = helmUrl.endsWith("/") ? helmUrl : helmUrl + "/";
        applicationVersionDTO.setRepository(String.format("%s%s%s%s%s",
                helmUrl,
                organizationCode,
                FILE_SEPARATOR,
                projectCode,
                FILE_SEPARATOR));
        applicationVersionDTO.setVersion(appVersion.getVersion());
        applicationVersionDTO.setAppId(appId);
        String tazName = String.format("%s%s%s%s",
                appCode,
                "-",
                appVersion.getVersion(),
                ".tgz"
        );
        List<File> tgzVersions = Arrays.stream(appFiles).parallel()
                .filter(k -> k.getName().equals(tazName))
                .collect(Collectors.toCollection(ArrayList::new));
        if (!tgzVersions.isEmpty()) {
            ApplicationVersionValueDTO applicationVersionValueDTO = new ApplicationVersionValueDTO();
            try {
                FileUtil.unTarGZ(tgzVersions.get(0).getAbsolutePath(), appCode);
                File valueYaml = FileUtil.queryFileFromFiles(new File(appCode), "values.yaml");
                if (valueYaml == null) {
                    throw new CommonException("error.version.values.notExist");
                }
                applicationVersionValueDTO.setValue(FileUtil.replaceReturnString(new FileInputStream(valueYaml), null));

                applicationVersionDTO.setValueId(applicationVersionValueService.baseCreate(applicationVersionValueDTO).getId());
            } catch (Exception e) {
                throw new CommonException("error.version.insert");
            }
            applicationVersionDTO.setReadme(FileUtil.getReadme(appCode));
            ApplicationVersionDTO version = applicationVersionService.baseQueryByAppIdAndVersion(appId, appVersion.getVersion());

            if (isVersionPublish) {
                applicationVersionDTO.setIsPublish(1L);
            } else {
                applicationVersionDTO.setIsPublish(version == null ? null : version.getIsPublish());
            }
            if (version == null) {
                if (applicationVersionMapper.insert(applicationVersionDTO) != 1) {
                    throw new CommonException("error.version.insert");
                }
            } else {
                applicationVersionDTO.setId(version.getId());
                applicationVersionService.baseUpdate(applicationVersionDTO);
            }
            String classPath = String.format("Charts%s%s%s%s",
                    FILE_SEPARATOR,
                    organizationCode,
                    FILE_SEPARATOR,
                    projectCode);
            FileUtil.copyFile(tgzVersions.get(0).getAbsolutePath(), classPath);
            //上传tgz包到chart仓库
            chartUtil.uploadChart(organizationCode, projectCode, tgzVersions.get(0));
            FileUtil.deleteDirectory(new File(appCode));
        }
    }


    private Long createOrUpdateApp(ApplicationDTO applicationDTO, String appCode, Long projectId) {
        applicationDTO.setCode(appCode);
        applicationDTO.setProjectId(projectId);
        Long appId;
        Boolean appCodeExist = false;
        try {
            applicationService.baseCheckCode(applicationDTO);
        } catch (Exception e) {
            logger.info(e.getMessage());
            appCodeExist = true;
        }
        if (!appCodeExist) {
            applicationDTO.setActive(true);
            applicationDTO.setSynchro(true);
            applicationDTO.setToken(GenerateUUID.generateUUID());
            appId = applicationService.baseCreate(applicationDTO).getId();
        } else {
            ApplicationRepVO existApplication = applicationService.queryByCode(projectId, appCode);
            appId = existApplication.getId();
            applicationDTO.setId(appId);
            applicationService.baseUpdate(applicationDTO);
        }
        return appId;
    }

    private Boolean checkAppCanPub(Long appId) {
        try {
            return baseCheckPub(appId);
        } catch (Exception e) {
            return false;
        }
    }

    private void releaseApp(Boolean isPublic,
                            ApplicationReleasingVO applicationReleasingDTO, Long appId) {
        if (isPublic != null) {
            Boolean canPub = checkAppCanPub(appId);
            if (canPub) {
                ApplicationShareDTO applicationShareDTO = new ApplicationShareDTO();
                applicationShareDTO.setAppId(appId);
                applicationShareDTO.setPublishLevel(isPublic ? PROJECTS : ORGANIZATION);
                applicationShareDTO.setActive(true);
                applicationShareDTO.setContributor(applicationReleasingDTO.getContributor());
                applicationShareDTO.setDescription(applicationReleasingDTO.getDescription());
                applicationShareDTO.setCategory(applicationReleasingDTO.getCategory());
                baseCreateOrUpdate(applicationShareDTO);
            }
        }
    }

}
