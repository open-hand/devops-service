package io.choerodon.devops.app.service.impl;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.commons.compress.utils.IOUtils;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.ApplicationValidator;
import io.choerodon.devops.api.validator.HarborMarketVOValidator;
import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.api.vo.HarborMarketVO;
import io.choerodon.devops.api.vo.iam.MarketAppServiceImageVO;
import io.choerodon.devops.api.vo.iam.MarketAppServiceVersionImageVO;
import io.choerodon.devops.api.vo.iam.MarketImageUrlVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.harbor.User;
import io.choerodon.devops.infra.dto.iam.ApplicationDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.feign.MarketServiceClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.util.*;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:28 2019/8/8
 * Description:
 */
@Component
public class OrgAppMarketServiceImpl implements OrgAppMarketService {
    public static final Logger LOGGER = LoggerFactory.getLogger(OrgAppMarketServiceImpl.class);
    private static final Gson gson = new Gson();

    private static final String APPLICATION = "application";
    private static final String CHART = "chart";
    private static final String REPO = "repo";
    private static final String IMAGES = "images";
    private static final String PUSH_IAMGES = "push_image.sh";
    private static final String MARKET_PRO = "market-downloaded-app";
    private static final String DOWNLOADED_APP = "downloaded-app";
    private static final String HARBOR_NAME = "harbor_default";
    private static final String SITE_APP_GROUP_NAME_FORMAT = "site_%s";
    private static final String APP_OUT_FILE_FORMAT = "%s%s%s-%s-%s%s";
    private static final String APP_FILE_PATH_FORMAT = "%s%s%s%s%s";
    private static final String APP_TEMP_PATH_FORMAT = "%s%s%s";
    private static final String APP_REPOSITORY_PATH_FORMAT = "%s/%s";
    private static final String ZIP = ".zip";
    private static final String GIT = ".git";
    private static final String TGZ = ".tgz";
    private static final String VALUES = "values.yaml";
    private static final String DEPLOY_ONLY = "mkt_deploy_only";
    private static final String DOWNLOAD_ONLY = "mkt_code_only";
    private static final String ALL = "mkt_code_deploy";
    private static final String MARKET = "market";
    private static final String LINE = "line.separator";
    private static final String SHELL = "shell";
    @Value("${services.helm.url}")
    private String helmUrl;
    @Value("${services.harbor.baseUrl}")
    private String harborUrl;
    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private ChartUtil chartUtil;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private HarborService harborService;
    @Autowired
    private DevopsConfigService devopsConfigService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private AppServiceVersionValueService appServiceVersionValueService;
    @Autowired
    private AppServiceVersionReadmeService appServiceVersionReadmeService;
    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private ApplicationService applicationService;

    @Override
    public PageInfo<AppServiceUploadPayload> pageByAppId(Long appId,
                                                         PageRequest pageRequest,
                                                         String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        List<String> paramList = TypeUtil.cast(mapParams.get(TypeUtil.PARAMS));
        PageInfo<AppServiceDTO> appServiceDTOPageInfo = PageHelper.startPage(
                pageRequest.getPage(),
                pageRequest.getSize(),
                PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> appServiceMapper.listByAppId(appId, TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)), paramList));

        PageInfo<AppServiceUploadPayload> appServiceMarketVOPageInfo = ConvertUtils.convertPage(appServiceDTOPageInfo, this::dtoToMarketVO);
        List<AppServiceUploadPayload> list = appServiceMarketVOPageInfo.getList();
        list.forEach(appServiceMarketVO -> appServiceMarketVO.setAppServiceVersionUploadPayloads(
                ConvertUtils.convertList(appServiceVersionService.baseListByAppServiceId(appServiceMarketVO.getAppServiceId()), AppServiceVersionUploadPayload.class)));
        appServiceMarketVOPageInfo.setList(list);
        return appServiceMarketVOPageInfo;
    }

    @Override
    public List<AppServiceUploadPayload> listAllAppServices() {
        List<AppServiceDTO> appServiceDTOList = appServiceMapper.selectAll();
        return ConvertUtils.convertList(appServiceDTOList, this::dtoToMarketVO);
    }

    @Override
    public String createHarborRepository(HarborMarketVO harborMarketVO) {
        HarborMarketVOValidator.checkEmailAndPassword(harborMarketVO);
        return harborService.createHarborForAppMarket(harborMarketVO);
    }

    @Override
    public List<AppServiceVersionUploadPayload> listServiceVersionsByAppServiceId(Long appServiceId) {
        List<AppServiceVersionDTO> appServiceVersionDTOList = appServiceVersionService.baseListByAppServiceId(appServiceId);
        return ConvertUtils.convertList(appServiceVersionDTOList, AppServiceVersionUploadPayload.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadAPP(AppMarketUploadPayload marketUploadVO) {
        List<String> zipFileList = new ArrayList<>();
        String appFilePath = gitUtil.getWorkingDirectory(APPLICATION + System.currentTimeMillis());
        try {
            //解析打包
            MarketImageUrlVO marketImageUrlVO = appUploadResolver(marketUploadVO, zipFileList, appFilePath);
            //上传删除
            fileUpload(zipFileList, marketUploadVO, marketImageUrlVO);
        } catch (CommonException e) {
            baseServiceClientOperator.publishFail(marketUploadVO.getProjectId(), marketUploadVO.getMktAppId(), marketUploadVO.getMktAppVersionId(), e.getCode(), false);
            throw new CommonException(e.getCode());
        } finally {
            FileUtil.deleteDirectory(new File(appFilePath));
            zipFileList.forEach(FileUtil::deleteFile);
        }
    }

    @Override
    public void uploadAPPFixVersion(AppMarketFixVersionPayload appMarketFixVersionPayload) {
        List<String> zipFileList = new ArrayList<>();
        String appFilePath = gitUtil.getWorkingDirectory(APPLICATION + System.currentTimeMillis());
        try {
            MarketImageUrlVO marketImageUrlVO = appUploadResolver(appMarketFixVersionPayload.getFixVersionUploadPayload(), zipFileList, appFilePath);
            fileUploadFixVersion(zipFileList, appMarketFixVersionPayload, marketImageUrlVO);
            zipFileList.forEach(FileUtil::deleteFile);
            FileUtil.deleteDirectory(new File(appFilePath));
        } catch (CommonException e) {
            baseServiceClientOperator.publishFail(
                    appMarketFixVersionPayload.getFixVersionUploadPayload().getProjectId(),
                    appMarketFixVersionPayload.getFixVersionUploadPayload().getMktAppId(),
                    appMarketFixVersionPayload.getFixVersionUploadPayload().getMktAppVersionId(),
                    e.getCode(),
                    true);
            FileUtil.deleteDirectory(new File(appFilePath));
            throw new CommonException(e.getCode());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void downLoadApp(AppMarketDownloadPayload appMarketDownloadVO) {
        Set<Long> appServiceVersionIds = new HashSet<>();
        //创建应用
        ApplicationEventPayload applicationEventPayload = new ApplicationEventPayload();
        BeanUtils.copyProperties(appMarketDownloadVO, applicationEventPayload);
        applicationEventPayload.setId(appMarketDownloadVO.getAppId());
        applicationService.handleApplicationCreation(applicationEventPayload);

        DevopsProjectDTO projectDTO = devopsProjectService.queryByAppId(appMarketDownloadVO.getAppId());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(appMarketDownloadVO.getIamUserId());

        ApplicationDTO applicationDTO = baseServiceClientOperator.queryAppById(appMarketDownloadVO.getAppId());
        String groupPath = String.format(SITE_APP_GROUP_NAME_FORMAT, applicationDTO.getCode());
        appMarketDownloadVO.getAppServiceDownloadPayloads().forEach(downloadPayload -> {
            //1. 校验是否已经下载过
            AppServiceDTO appServiceDTO = appServiceService.baseQueryByCode(downloadPayload.getAppServiceCode(), appMarketDownloadVO.getAppId());
            downloadPayload.setAppId(appMarketDownloadVO.getAppId());
            Boolean isFirst = appServiceDTO == null;
            if (appServiceDTO == null) {
                appServiceDTO = createGitlabProject(downloadPayload, TypeUtil.objToInteger(projectDTO.getDevopsAppGroupId()), userAttrDTO.getGitlabUserId());

                //创建saga payload
                DevOpsAppServiceSyncPayload appServiceSyncPayload = new DevOpsAppServiceSyncPayload();
                BeanUtils.copyProperties(appServiceDTO, appServiceSyncPayload);
                producer.apply(
                        StartSagaBuilder.newBuilder()
                                .withSourceId(applicationDTO.getId())
                                .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_APPLICATION_SERVICE_EVENT)
                                .withLevel(ResourceLevel.SITE)
                                .withPayloadAndSerialize(appServiceSyncPayload),
                        builder -> {
                        }
                );
            }
            String applicationDir = APPLICATION + System.currentTimeMillis();
            String accessToken = appServiceService.getToken(appServiceDTO.getGitlabProjectId(), applicationDir, userAttrDTO);
            appServiceVersionIds.addAll(createAppServiceVersion(downloadPayload, appServiceDTO, groupPath, isFirst, accessToken));
        });
        if (appMarketDownloadVO.getUser() != null) {
            pushImageForDownload(appMarketDownloadVO);
        }
        baseServiceClientOperator.completeDownloadApplication(appMarketDownloadVO.getAppVersionId(), appServiceVersionIds);

    }

    private AppServiceDTO createGitlabProject(AppServiceDownloadPayload downloadPayload, Integer gitlabGroupId, Long gitlabUserId) {
        ApplicationValidator.checkApplicationService(downloadPayload.getAppServiceCode());
        AppServiceDTO appServiceDTO = ConvertUtils.convertObject(downloadPayload, AppServiceDTO.class);
        appServiceDTO.setName(downloadPayload.getAppServiceName());
        appServiceDTO.setType(downloadPayload.getAppServiceType());
        appServiceDTO.setCode(downloadPayload.getAppServiceCode());
        appServiceDTO.setActive(true);
        appServiceDTO.setSkipCheckPermission(true);
        //2. 第一次下载创建应用服务
        //2. 分配所在gitlab group 用户权限
        MemberDTO memberDTO = gitlabServiceClientOperator.queryGroupMember(gitlabGroupId, TypeUtil.objToInteger(gitlabUserId));
        if (memberDTO == null || memberDTO.getId() == null || !memberDTO.getAccessLevel().equals(AccessLevel.OWNER.value)) {
            memberDTO = new MemberDTO(TypeUtil.objToInteger(gitlabUserId), AccessLevel.OWNER.value);
            gitlabServiceClientOperator.createGroupMember(gitlabGroupId, memberDTO);
        }

        //3. 创建gitlab project
        GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.createProject(gitlabGroupId,
                downloadPayload.getAppServiceCode(),
                TypeUtil.objToInteger(gitlabUserId),
                true);
        appServiceDTO.setGitlabProjectId(gitlabProjectDTO.getId());
        appServiceDTO.setSynchro(true);
        appServiceDTO.setFailed(false);
        appServiceService.baseCreate(appServiceDTO);
        return appServiceDTO;
    }

    private Set<Long> createAppServiceVersion(AppServiceDownloadPayload downloadPayload, AppServiceDTO appServiceDTO, String groupPath, Boolean isFirst, String accessToken) {
        Long appServiceId = appServiceDTO.getId();
        Set<Long> serviceVersionIds = new HashSet<>();
        downloadPayload.getAppServiceVersionDownloadPayloads().forEach(appServiceVersionPayload -> {
            AppServiceVersionDTO versionDTO = new AppServiceVersionDTO();
            if (appServiceVersionPayload.getChartFilePath() != null && !appServiceVersionPayload.getChartFilePath().isEmpty()) {
                String chartFilePath = String.format("%s%s", gitUtil.getWorkingDirectory(APPLICATION + System.currentTimeMillis()), TGZ);
                fileDownload(appServiceVersionPayload.getChartFilePath(), chartFilePath);
                AppServiceVersionDTO appServiceVersionDTO = chartResolver(appServiceVersionPayload, appServiceId, downloadPayload.getAppServiceCode(), new File(chartFilePath), versionDTO);
                serviceVersionIds.add(appServiceVersionDTO.getId());
            }
            if (appServiceVersionPayload.getRepoFilePath() != null && !appServiceVersionPayload.getRepoFilePath().isEmpty()) {
                String repoFilePath = String.format("%s%s", gitUtil.getWorkingDirectory(APPLICATION + System.currentTimeMillis()), ZIP);
                fileDownload(appServiceVersionPayload.getRepoFilePath(), repoFilePath);
                AppServiceVersionDTO appServiceVersionDTO = gitResolver(appServiceVersionPayload, isFirst, groupPath, new File(repoFilePath), downloadPayload, accessToken, versionDTO, appServiceId);
                serviceVersionIds.add(appServiceVersionDTO.getId());
            }
        });
        return serviceVersionIds;
    }

    private MarketImageUrlVO appUploadResolver(AppMarketUploadPayload marketUploadVO, List<String> zipFileList, String appFilePath) {
        //创建根目录 应用
        FileUtil.createDirectory(appFilePath);
        File appFile = new File(appFilePath);
        MarketImageUrlVO marketImageUrlVO = null;
        switch (marketUploadVO.getStatus()) {
            case DOWNLOAD_ONLY: {
                String appRepoFilePath = String.format(APP_FILE_PATH_FORMAT, appFilePath, File.separator, REPO, File.separator, marketUploadVO.getAppCode());
                //clone 并压缩源代码
                marketUploadVO.getAppServiceUploadPayloads().forEach(appServiceMarketVO -> packageRepo(appServiceMarketVO, appRepoFilePath));
                String outputFilePath = String.format(APP_OUT_FILE_FORMAT, appFile.getParent(), File.separator, REPO, marketUploadVO.getAppCode(), System.currentTimeMillis(), ZIP);
                toZip(outputFilePath, appRepoFilePath);
                zipFileList.add(outputFilePath);
                break;
            }
            case DEPLOY_ONLY: {
                String appChartFilePath = String.format(APP_FILE_PATH_FORMAT, appFilePath, File.separator, CHART, File.separator, marketUploadVO.getAppCode());
                marketUploadVO.getAppServiceUploadPayloads().forEach(appServiceMarketVO -> packageChart(appServiceMarketVO, appChartFilePath));

                String outputFilePath = String.format(APP_OUT_FILE_FORMAT, appFile.getParent(), File.separator, CHART, marketUploadVO.getAppCode(), System.currentTimeMillis(), ZIP);
                toZip(outputFilePath, appChartFilePath);
                zipFileList.add(outputFilePath);
                marketImageUrlVO = pushImageForUpload(marketUploadVO);
                break;
            }
            case ALL: {
                String appRepoFilePath = String.format(APP_FILE_PATH_FORMAT, appFilePath, File.separator, REPO, File.separator, marketUploadVO.getAppCode());
                String appChartFilePath = String.format(APP_FILE_PATH_FORMAT, appFilePath, File.separator, CHART, File.separator, marketUploadVO.getAppCode());

                marketUploadVO.getAppServiceUploadPayloads().forEach(appServiceMarketVO -> {
                    packageRepo(appServiceMarketVO, appRepoFilePath);
                    packageChart(appServiceMarketVO, appChartFilePath);
                });

                String outputFilePath = String.format(APP_OUT_FILE_FORMAT, appFile.getParent(), File.separator, CHART, marketUploadVO.getAppCode(), System.currentTimeMillis(), ZIP);
                toZip(outputFilePath, appChartFilePath);
                zipFileList.add(outputFilePath);

                outputFilePath = String.format(APP_OUT_FILE_FORMAT, appFile.getParent(), File.separator, REPO, marketUploadVO.getAppCode(), System.currentTimeMillis(), ZIP);
                toZip(outputFilePath, appRepoFilePath);
                zipFileList.add(outputFilePath);
                marketImageUrlVO = pushImageForUpload(marketUploadVO);
                break;
            }
            default:
                throw new CommonException("error.status.publish");
        }
        return marketImageUrlVO;
    }

    /**
     * git 解析
     *
     * @param isFirst         是否第一次下载
     * @param groupPath       gitlab项目组名称
     * @param file            源码文件
     * @param downloadPayload
     * @param accessToken
     * @return
     */
    private AppServiceVersionDTO gitResolver(AppServiceVersionDownloadPayload appServiceVersionPayload,
                                             Boolean isFirst,
                                             String groupPath,
                                             File file,
                                             AppServiceDownloadPayload downloadPayload,
                                             String accessToken,
                                             AppServiceVersionDTO versionDTO,
                                             Long appServiceId) {
        Git git = null;
        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        String repositoryUrl = repoUrl + groupPath + "/" + downloadPayload.getAppServiceCode() + GIT;
        String unZipFilePath = gitUtil.getWorkingDirectory(REPO + System.currentTimeMillis());
        FileUtil.unZipFiles(file, unZipFilePath);
        unZipFilePath = String.format(APP_TEMP_PATH_FORMAT, unZipFilePath, File.separator, appServiceVersionPayload.getVersion());
        if (isFirst) {
            git = gitUtil.initGit(new File(unZipFilePath));
        } else {
            String appServiceDir = APPLICATION + System.currentTimeMillis();
            String appServiceFilePath = gitUtil.clone(appServiceDir, repositoryUrl, accessToken);
            git = gitUtil.combineAppMarket(appServiceFilePath, unZipFilePath);
        }
        //6. push 到远程仓库
        gitUtil.commitAndPushForMaster(git, repositoryUrl, appServiceVersionPayload.getVersion(), accessToken);

        if (versionDTO.getId() == null) {
            versionDTO.setAppServiceId(appServiceId);
            BeanUtils.copyProperties(appServiceVersionPayload, versionDTO);
            versionDTO = appServiceVersionService.baseCreate(versionDTO);
        }
        versionDTO.setCommit(gitUtil.getFirstCommit(git));
        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQueryByAppIdAndVersion(appServiceId, appServiceVersionPayload.getVersion());
        appServiceVersionDTO.setCommit(gitUtil.getFirstCommit(git));
        appServiceVersionService.baseUpdate(appServiceVersionDTO);
        return versionDTO;
    }

    /**
     * chart 解析 下载
     *
     * @param appServiceVersionPayload
     * @param appServiceId
     * @param appServiceCode
     * @param file                     chart文件
     * @return
     */
    private AppServiceVersionDTO chartResolver(AppServiceVersionDownloadPayload appServiceVersionPayload, Long appServiceId, String appServiceCode, File file, AppServiceVersionDTO versionDTO) {
        String unZipPath = String.format(APP_TEMP_PATH_FORMAT, file.getParentFile().getAbsolutePath(), File.separator, System.currentTimeMillis());
        FileUtil.createDirectory(unZipPath);
        FileUtil.unTarGZ(file, unZipPath);
        File zipDirectory = new File(String.format(APP_TEMP_PATH_FORMAT, unZipPath, File.separator, appServiceCode));
        helmUrl = helmUrl.endsWith("/") ? helmUrl : helmUrl + "/";
        versionDTO.setAppServiceId(appServiceId);
        AppServiceVersionDTO appServiceVersionDTO = null;
        //解析 解压过后的文件
        if (zipDirectory.exists() && zipDirectory.isDirectory()) {

            File[] listFiles = zipDirectory.listFiles();
            BeanUtils.copyProperties(appServiceVersionPayload, versionDTO);
            //9. 获取替换Repository
            List<File> appMarkets = Arrays.stream(listFiles).parallel()
                    .filter(k -> k.getName().equals(VALUES))
                    .collect(Collectors.toCollection(ArrayList::new));
            if (!appMarkets.isEmpty() && appMarkets.size() == 1) {
                File valuesFile = appMarkets.get(0);

                Map<String, String> params = new HashMap<>();
                params.put(appServiceVersionPayload.getImage(), String.format("%s/%s/%s", harborUrl, MARKET_PRO, appServiceCode));
                FileUtil.fileToInputStream(valuesFile, params);

                //10. 创建appServiceValue
                AppServiceVersionValueDTO versionValueDTO = new AppServiceVersionValueDTO();
                versionValueDTO.setValue(FileUtil.getFileContent(valuesFile));
                versionDTO.setValueId(appServiceVersionValueService.baseCreate(versionValueDTO).getId());
                // 创建ReadMe
                AppServiceVersionReadmeDTO versionReadmeDTO = new AppServiceVersionReadmeDTO();
                versionReadmeDTO.setReadme(FileUtil.getReadme(unZipPath));
                versionDTO.setReadmeValueId(appServiceVersionReadmeService.baseCreate(versionReadmeDTO).getId());
                //创建version
                appServiceVersionDTO = appServiceVersionService.baseCreate(versionDTO);
            }
        } else {
            FileUtil.deleteDirectory(zipDirectory);
            throw new CommonException("error.zip.empty");
        }

        String newTgzFile = gitUtil.getWorkingDirectory(CHART + System.currentTimeMillis());
        FileUtil.toTgz(String.format(APP_TEMP_PATH_FORMAT, unZipPath, File.separator, appServiceCode), newTgzFile);
        chartUtil.uploadChart(MARKET, DOWNLOADED_APP, new File(newTgzFile + TGZ));
        FileUtil.deleteFile(file);
        FileUtil.deleteDirectory(new File(unZipPath));
        FileUtil.deleteDirectory(new File(newTgzFile));

        versionDTO.setRepository(String.format("%s/%s/%s", harborUrl, MARKET_PRO, appServiceCode));
        appServiceVersionService.baseUpdate(versionDTO);
        return appServiceVersionDTO;
    }

    /**
     * 源码打包
     *
     * @param appServiceMarketVO
     * @param appFilePath
     */
    private void packageRepo(AppServiceUploadPayload appServiceMarketVO, String appFilePath) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceMarketVO.getAppServiceId());
        ApplicationDTO applicationDTO = baseServiceClientOperator.queryAppById(appServiceDTO.getAppId());
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(applicationDTO.getOrganizationId());

        //1.创建目录 应用服务仓库
        FileUtil.createDirectory(appFilePath, appServiceDTO.getCode());
        String appServiceRepositoryPath = String.format(APP_REPOSITORY_PATH_FORMAT, appFilePath, appServiceDTO.getCode());

        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        String token = gitlabServiceClientOperator.getAdminToken();
        appServiceDTO.setRepoUrl(repoUrl + organizationDTO.getCode()
                + "-" + applicationDTO.getCode() + "/" + appServiceDTO.getCode() + GIT);
        appServiceMarketVO.getAppServiceVersionUploadPayloads().forEach(appServiceMarketVersionVO -> {
            AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(appServiceMarketVersionVO.getId());
            //2. 创建目录 应用服务版本

            FileUtil.createDirectory(appServiceRepositoryPath, appServiceVersionDTO.getVersion());
            String appServiceVersionPath = String.format(APP_REPOSITORY_PATH_FORMAT, appServiceRepositoryPath, appServiceVersionDTO.getVersion());

            //3.clone源码,checkout到版本所在commit，并删除.git文件
            gitUtil.cloneAndCheckout(appServiceVersionPath, appServiceDTO.getRepoUrl(), token, appServiceVersionDTO.getCommit());
            toZip(String.format("%s%s", appServiceVersionPath, ZIP), appServiceVersionPath);
            FileUtil.deleteDirectory(new File(appServiceVersionPath));
        });
    }

    /**
     * chart打包
     *
     * @param appServiceMarketVO
     * @param appFilePath
     */
    private void packageChart(AppServiceUploadPayload appServiceMarketVO, String appFilePath) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceMarketVO.getAppServiceId());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getAppId());
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        //1.创建目录 应用服务仓库
        FileUtil.createDirectory(appFilePath, appServiceDTO.getCode());
        String appServiceChartPath = String.format(APP_TEMP_PATH_FORMAT, appFilePath, File.separator, appServiceDTO.getCode());
        appServiceMarketVO.getAppServiceVersionUploadPayloads().forEach(appServiceMarketVersionVO -> {
            //2.下载chart
            AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(appServiceMarketVersionVO.getId());
            chartUtil.downloadChart(appServiceVersionDTO, organizationDTO, projectDTO, appServiceDTO, appServiceChartPath);
            analysisChart(appServiceChartPath, appServiceDTO.getCode(), appServiceVersionDTO, appServiceMarketVO.getHarborUrl());
        });
    }

    /**
     * chart解析 上传
     *
     * @param zipPath
     * @param appServiceCode
     * @param appServiceVersionDTO
     * @param harborUrl
     */
    private void analysisChart(String zipPath, String appServiceCode, AppServiceVersionDTO appServiceVersionDTO, String harborUrl) {
        String tgzFileName = String.format("%s%s%s-%s.tgz",
                zipPath,
                File.separator,
                appServiceCode,
                appServiceVersionDTO.getVersion());
        FileUtil.unTarGZ(tgzFileName, zipPath);
        FileUtil.deleteFile(tgzFileName);

        String unTarGZPath = String.format(APP_TEMP_PATH_FORMAT, zipPath, File.separator, appServiceCode);
        File zipDirectory = new File(unTarGZPath);
        //解析 解压过后的文件
        if (zipDirectory.exists() && zipDirectory.isDirectory()) {
            File[] listFiles = zipDirectory.listFiles();
            //获取替换Repository
            List<File> appMarkets = Arrays.stream(listFiles).parallel()
                    .filter(k -> VALUES.equals(k.getName()))
                    .collect(Collectors.toCollection(ArrayList::new));
            if (!appMarkets.isEmpty() && appMarkets.size() == 1) {
                Map<String, String> params = new HashMap<>();
                String image = appServiceVersionDTO.getImage().replace(":" + appServiceVersionDTO.getVersion(), "");
                params.put(image, String.format(APP_REPOSITORY_PATH_FORMAT, harborUrl, appServiceVersionDTO.getVersion()));
                FileUtil.fileToInputStream(appMarkets.get(0), params);
            }
        } else {
            FileUtil.deleteDirectory(new File(zipPath).getParentFile());
            throw new CommonException("error.chart.empty");
        }
        // 打包
        String newChartFilePath = String.format(APP_TEMP_PATH_FORMAT, zipPath, File.separator, appServiceVersionDTO.getVersion());
        FileUtil.toTgz(unTarGZPath, newChartFilePath);
        FileUtil.deleteDirectory(new File(unTarGZPath));
    }

    private void toZip(String outputPath, String filePath) {
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(outputPath));
            FileUtil.toZip(filePath, outputStream, true);
            FileUtil.deleteDirectory(new File(filePath));
        } catch (FileNotFoundException e) {
            throw new CommonException("error.zip.repository", e.getMessage());
        }
    }

    private MarketImageUrlVO pushImageForUpload(AppMarketUploadPayload appMarketUploadVO) {
        MarketImageUrlVO marketImageUrlVO = new MarketImageUrlVO();
        marketImageUrlVO.setAppCode(appMarketUploadVO.getAppCode());

        File file = new File(String.format("%s%s%s", SHELL, File.separator, PUSH_IAMGES));
        if (!file.exists()) {
            FileUtil.createDirectory(SHELL);
            String shellPath = this.getClass().getResource(String.format("/%s/%s", SHELL, PUSH_IAMGES)).getPath();
            FileUtil.copyFile(shellPath, SHELL);
        }

        List<MarketAppServiceImageVO> imageVOList = new ArrayList<>();
        //获取push_image 脚本目录
        // 创建images
        appMarketUploadVO.getAppServiceUploadPayloads().forEach(appServiceMarketVO -> {
            MarketAppServiceImageVO appServiceImageVO = new MarketAppServiceImageVO();
            appServiceImageVO.setServiceCode(appServiceMarketVO.getAppServiceCode());
            List<MarketAppServiceVersionImageVO> appServiceVersionImageVOS = new ArrayList<>();
            StringBuilder stringBuilder = new StringBuilder();
            appServiceMarketVO.getAppServiceVersionUploadPayloads().forEach(t -> {
                stringBuilder.append(appServiceVersionService.baseQuery(t.getId()).getImage());
                stringBuilder.append(System.getProperty(LINE));

                MarketAppServiceVersionImageVO appServiceVersionImageVO = new MarketAppServiceVersionImageVO();
                appServiceVersionImageVO.setVersion(t.getVersion());
                appServiceVersionImageVO.setImageUrl(String.format("%s:%s", appServiceMarketVO.getHarborUrl(), t.getVersion()));
                appServiceVersionImageVOS.add(appServiceVersionImageVO);
            });
            appServiceImageVO.setServiceVersionVOS(appServiceVersionImageVOS);
            imageVOList.add(appServiceImageVO);
            FileUtil.saveDataToFile(SHELL, IMAGES, stringBuilder.toString());

            //获取原仓库配置
            ConfigVO configVO = devopsConfigService.queryByResourceId(
                    appServiceService.baseQuery(appServiceMarketVO.getAppServiceId()).getChartConfigId(), "harbor")
                    .get(0).getConfig();
            User oldUser = new User();
            BeanUtils.copyProperties(configVO, oldUser);
            oldUser.setUsername(configVO.getUserName());
            User newUser = new User();
            newUser.setUsername(appMarketUploadVO.getUser().getRobotName());
            newUser.setPassword(appMarketUploadVO.getUser().getRobotToken());
            // 执行脚本
            callScript(SHELL, appServiceMarketVO.getHarborUrl(), newUser, oldUser);
            FileUtil.deleteFile(String.format(APP_TEMP_PATH_FORMAT, SHELL, File.separator, IMAGES));
        });
        marketImageUrlVO.setServiceImageVOS(imageVOList);
        return marketImageUrlVO;
    }

    private void pushImageForDownload(AppMarketDownloadPayload appMarketDownloadVO) {
        //获取push_image 脚本目录
        String shellPath = this.getClass().getResource(SHELL).getPath();

        appMarketDownloadVO.getAppServiceDownloadPayloads().forEach(appServiceMarketVO -> {
            StringBuilder stringBuilder = new StringBuilder();
            appServiceMarketVO.getAppServiceVersionDownloadPayloads().forEach(t -> {
                stringBuilder.append(t.getImage());
                stringBuilder.append(System.getProperty(LINE));
            });
            FileUtil.saveDataToFile(shellPath, IMAGES, stringBuilder.toString());

            //获取新仓库配置
            ConfigVO configVO = gson.fromJson(devopsConfigService.baseQueryByName(null, HARBOR_NAME).getConfig(), ConfigVO.class);
            User newUser = new User();
            BeanUtils.copyProperties(configVO, newUser);
            newUser.setUsername(configVO.getUserName());
            User oldUser = new User();
            oldUser.setUsername(appMarketDownloadVO.getUser().getRobotName());
            oldUser.setPassword(appMarketDownloadVO.getUser().getRobotToken());
            harborUrl = harborUrl.endsWith("/") ? harborUrl : harborUrl + "/";

            callScript(new File(shellPath).getAbsolutePath(), String.format("%s%s", harborUrl, MARKET_PRO), newUser, oldUser);
            FileUtil.deleteFile(String.format(APP_TEMP_PATH_FORMAT, shellPath, File.separator, IMAGES));
        });
    }

    /**
     * 脚本文件具体执行及脚本执行过程探测
     *
     * @param script 脚本文件绝对路径
     */
    private void callScript(String script, String harborUrl, User newUser, User oldUser) {
        try {
            String cmd = String.format("sh /shell/%s %s %s %s %s %s", PUSH_IAMGES, harborUrl, newUser.getUsername(), newUser.getPassword(), oldUser.getUsername(), oldUser.getPassword());
            LOGGER.info(cmd);
            //执行脚本并等待脚本执行完成
            Process process = Runtime.getRuntime().exec(cmd);

            //写出脚本执行中的过程信息
            BufferedReader infoInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorInput = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line = "";
            while ((line = infoInput.readLine()) != null) {
                LOGGER.info(line);
            }
            while ((line = errorInput.readLine()) != null) {
                LOGGER.error(line);
            }
            infoInput.close();
            errorInput.close();

            //阻塞执行线程直至脚本执行完成后返回
            process.waitFor();
        } catch (Exception e) {
            throw new CommonException("error.exec.push.image",e.getMessage());
        }
    }

    private void fileUpload(List<String> zipFileList, AppMarketUploadPayload appMarketUploadVO, MarketImageUrlVO marketImageUrlVO) {
        List<MultipartBody.Part> files = new ArrayList<>();
        zipFileList.forEach(f -> {
            File file = new File(f);
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("files", file.getName(), requestFile);
            files.add(body);
        });
        String mapJson = marketImageUrlVO != null ? gson.toJson(marketImageUrlVO) : null;
        String getawayUrl = appMarketUploadVO.getSaasGetawayUrl().endsWith("/") ? appMarketUploadVO.getSaasGetawayUrl() : appMarketUploadVO.getSaasGetawayUrl() + "/";
        MarketServiceClient marketServiceClient = RetrofitHandler.getMarketServiceClient(getawayUrl, MARKET);
        try {
            Boolean uploadSuccess = marketServiceClient.uploadFile(appMarketUploadVO.getAppVersion(), files, mapJson).execute().body().getBody();
            if (uploadSuccess == null || !uploadSuccess) {
                throw new CommonException("error.upload.file", uploadSuccess);
            }
        } catch (IOException e) {
            throw new CommonException("error.upload.file", e.getMessage());
        }
    }

    private void fileUploadFixVersion(List<String> zipFileList, AppMarketFixVersionPayload appMarketFixVersionPayload, MarketImageUrlVO marketImageUrlVO) {
        List<MultipartBody.Part> files = new ArrayList<>();
        zipFileList.forEach(f -> {
            File file = new File(f);
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("files", file.getName(), requestFile);
            files.add(body);
        });

        String imageJson = marketImageUrlVO != null ? gson.toJson(marketImageUrlVO) : null;
        String appJson = gson.toJson(appMarketFixVersionPayload.getMarketApplicationVO());
        String getawayUrl = appMarketFixVersionPayload.getFixVersionUploadPayload().getSaasGetawayUrl().endsWith("/") ? appMarketFixVersionPayload.getFixVersionUploadPayload().getSaasGetawayUrl() : appMarketFixVersionPayload.getFixVersionUploadPayload() + "/";
        MarketServiceClient marketServiceClient = RetrofitHandler.getMarketServiceClient(getawayUrl, MARKET);
        try {
            Boolean uploadSuccess = marketServiceClient.updateAppPublishInfoFix(
                    appMarketFixVersionPayload.getMarketApplicationVO().getCode(),
                    appMarketFixVersionPayload.getMarketApplicationVO().getVersion(),
                    appJson,
                    files,
                    imageJson).execute().body().getBody();
            if (uploadSuccess == null || !uploadSuccess) {
                throw new CommonException("error.upload.file", uploadSuccess);
            }
        } catch (IOException e) {
            throw new CommonException("error.upload.file", e.getMessage());
        }
    }

    private void fileDownload(String fileUrl, String downloadFilePath) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setType(CHART);
        configurationProperties.setInsecureSkipTlsVerify(false);
        List<String> fileUrlList = Arrays.asList(fileUrl.split("/"));
        String fileName = fileUrlList.get(fileUrlList.size() - 1);
        fileUrl = fileUrl.replace(fileName, "");
        configurationProperties.setBaseUrl(fileUrl);

        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        MarketServiceClient marketServiceClient = retrofit.create(MarketServiceClient.class);
        Call<ResponseBody> getTaz = marketServiceClient.downloadFile(fileName);
        FileOutputStream fos = null;
        try {
            Response<ResponseBody> response = getTaz.execute();
            fos = new FileOutputStream(downloadFilePath);
            if (response.body() != null) {
                InputStream is = response.body().byteStream();
                byte[] buffer = new byte[4096];
                int r = 0;
                while ((r = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, r);
                }
                is.close();
            }
            fos.close();
        } catch (IOException e) {
            IOUtils.closeQuietly(fos);
            throw new CommonException("error.download.file", e.getMessage());
        }
    }

    private AppServiceUploadPayload dtoToMarketVO(AppServiceDTO applicationDTO) {
        AppServiceUploadPayload appServiceMarketVO = new AppServiceUploadPayload();
        appServiceMarketVO.setAppServiceId(applicationDTO.getId());
        appServiceMarketVO.setAppServiceCode(applicationDTO.getCode());
        appServiceMarketVO.setAppServiceName(applicationDTO.getName());
        return appServiceMarketVO;
    }
}
