package io.choerodon.devops.app.service.impl;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.entity.ContentType;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.ApplicationValidator;
import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.api.vo.iam.MarketAppServiceImageVO;
import io.choerodon.devops.api.vo.iam.MarketAppServiceVersionImageVO;
import io.choerodon.devops.api.vo.iam.MarketImageUrlVO;
import io.choerodon.devops.api.vo.kubernetes.MockMultipartFile;
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
import io.choerodon.devops.infra.feign.MarketServicePublicClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
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
    private static final String MARKET_PRO = "market-downloaded-app";
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
    private static final String ERROR_UPLOAD = "error.upload.file";
    private static final String CONFIG_PATH = "root/.docker";
    private static final String CONFIG_JSON = "config.json";
    private static final String SHELL = "shell";
    private static final String PUSH_IMAGE = "push_image.sh";
    private static final String DSLASH = "//";
    private static final String SSLASH = "/";
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
    private MarketServiceClientOperator marketServiceClientOperator;

    /**
     * 执行pull/push脚本
     * kaniko工具
     *
     * @param
     */
    private static synchronized void pushImageScript(String sourceUrl, String targetUrl, String configStr) {
        try {
            FileUtil.saveDataToFile(CONFIG_PATH, CONFIG_JSON, configStr);
            String cmd = String.format("echo 'FROM %s' | kaniko -f /dev/stdin -d %s", sourceUrl, targetUrl);
            FileUtil.saveDataToFile(SHELL, PUSH_IMAGE, cmd);
            LOGGER.info(cmd);
            Process process = Runtime.getRuntime().exec(String.format("sh /%s/%s", SHELL, PUSH_IMAGE));
            BufferedReader infoInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorInput = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line = "";
            while ((line = infoInput.readLine()) != null) {
                LOGGER.info(line);
            }
            LOGGER.info("=============信息分界线=================");
            while ((line = errorInput.readLine()) != null) {
                LOGGER.error(line);
            }
            LOGGER.info("=============信息分界线=================");
            infoInput.close();
            errorInput.close();
            process.waitFor();
        } catch (Exception e) {
            throw new CommonException("error.exec.push.image", e.getMessage());
        }
    }

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
            LOGGER.info("==========应用上传，解析文件成功！！==========");
            //上传删除
            fileUpload(zipFileList, marketUploadVO, marketImageUrlVO);
            LOGGER.info("==========应用上传，上传文件！！==========");
            FileUtil.deleteDirectory(new File(appFilePath));
            zipFileList.forEach(FileUtil::deleteFile);
        } catch (CommonException e) {
            baseServiceClientOperator.publishFail(marketUploadVO.getProjectId(), marketUploadVO.getMktAppId(), marketUploadVO.getMktAppVersionId(), e.getCode(), false);
            FileUtil.deleteDirectory(new File(appFilePath));
            zipFileList.forEach(FileUtil::deleteFile);
            throw new CommonException(e.getCode());
        }
    }

    @Override
    public void uploadAPPFixVersion(AppMarketFixVersionPayload appMarketFixVersionPayload) {
        List<String> zipFileList = new ArrayList<>();
        String appFilePath = gitUtil.getWorkingDirectory(APPLICATION + System.currentTimeMillis());
        try {
            MarketImageUrlVO marketImageUrlVO = appUploadResolver(appMarketFixVersionPayload.getFixVersionUploadPayload(), zipFileList, appFilePath);
            LOGGER.info("==========应用上传修复版本，解析文件成功！！==========");
            fileUploadFixVersion(zipFileList, appMarketFixVersionPayload, marketImageUrlVO);
            LOGGER.info("==========应用上传修复版本，上传文件！！==========");
            zipFileList.forEach(FileUtil::deleteFile);
            FileUtil.deleteDirectory(new File(appFilePath));
        } catch (CommonException e) {
            baseServiceClientOperator.publishFail(
                    appMarketFixVersionPayload.getFixVersionUploadPayload().getProjectId(),
                    appMarketFixVersionPayload.getFixVersionUploadPayload().getMktAppId(),
                    appMarketFixVersionPayload.getFixVersionUploadPayload().getMktAppVersionId(),
                    e.getCode(),
                    true);
            zipFileList.forEach(FileUtil::deleteFile);
            FileUtil.deleteDirectory(new File(appFilePath));
            throw new CommonException(e.getCode(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void downLoadApp(AppMarketDownloadPayload appMarketDownloadVO) {
        Set<Long> appServiceVersionIds = new HashSet<>();

        try {
            //创建应用
            DevopsProjectDTO projectDTO = devopsProjectService.queryByAppId(appMarketDownloadVO.getAppId());
            LOGGER.info("==========应用下载，appMarketDownloadVO.getAppId(){},projectDTO=========={}", appMarketDownloadVO.getAppId(), projectDTO.getIamProjectId());
            UserAttrDTO userAttrDTO = userAttrService.baseQueryById(appMarketDownloadVO.getIamUserId());
            ApplicationDTO applicationDTO = baseServiceClientOperator.queryAppById(appMarketDownloadVO.getAppId());
            String groupPath = String.format(SITE_APP_GROUP_NAME_FORMAT, applicationDTO.getCode());
            appMarketDownloadVO.getAppServiceDownloadPayloads().forEach(downloadPayload -> {
                //1. 校验是否已经下载过
                AppServiceDTO appServiceDTO = appServiceService.baseQueryByCode(downloadPayload.getAppServiceCode(), appMarketDownloadVO.getAppId());
                downloadPayload.setAppId(appMarketDownloadVO.getAppId());
                Boolean isFirst = appServiceDTO == null;
                if (appServiceDTO == null) {
                    LOGGER.info("==========应用下载=========={}", appMarketDownloadVO.getAppCode());
                    LOGGER.info("==========应用下载=========={}", projectDTO.getDevopsAppGroupId());
                    LOGGER.info("==========应用下载=========={}", userAttrDTO.getGitlabUserId());
                    appServiceDTO = createGitlabProject(downloadPayload, appMarketDownloadVO.getAppCode(), TypeUtil.objToInteger(projectDTO.getDevopsAppGroupId()), userAttrDTO.getGitlabUserId());
                    LOGGER.info("==========应用下载，创建gitlab Project成功！！==========");

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
                LOGGER.info("=========应用下载，获取token成功=========");

                appServiceVersionIds.addAll(createAppServiceVersion(downloadPayload, appServiceDTO, groupPath, isFirst, accessToken, appMarketDownloadVO.getDownloadAppType()));
                LOGGER.info("==========应用下载文件上传成功==========");
            });
            if (!appMarketDownloadVO.getDownloadAppType().equals(DOWNLOAD_ONLY)) {
                pushImageForDownload(appMarketDownloadVO);
                LOGGER.info("==========应用下载镜像推送成功==========");
            }
            LOGGER.info("==========应用下载开始调用回传接口==========");
            baseServiceClientOperator.completeDownloadApplication(appMarketDownloadVO.getAppDownloadRecordId(), appMarketDownloadVO.getAppVersionId(), appServiceVersionIds);
            LOGGER.info("==========应用下载完成==========");
        } catch (Exception e) {
            baseServiceClientOperator.failToDownloadApplication(appMarketDownloadVO.getAppDownloadRecordId());
            throw new CommonException("error.download.app", e);
        }
    }

    private AppServiceDTO createGitlabProject(AppServiceDownloadPayload downloadPayload, String appCode, Integer gitlabGroupId, Long gitlabUserId) {
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
        GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.queryProjectByName(
                String.format(SITE_APP_GROUP_NAME_FORMAT, appCode),
                appServiceDTO.getCode(),
                TypeUtil.objToInteger(gitlabUserId));
        if (gitlabProjectDTO.getId() == null) {
            gitlabProjectDTO = gitlabServiceClientOperator.createProject(gitlabGroupId,
                    downloadPayload.getAppServiceCode(),
                    TypeUtil.objToInteger(gitlabUserId),
                    true);
        }
        appServiceDTO.setGitlabProjectId(gitlabProjectDTO.getId());
        appServiceDTO.setSynchro(true);
        appServiceDTO.setFailed(false);
        appServiceService.baseCreate(appServiceDTO);
        return appServiceDTO;
    }

    private Set<Long> createAppServiceVersion(AppServiceDownloadPayload downloadPayload, AppServiceDTO appServiceDTO, String groupPath, Boolean isFirst, String accessToken, String downloadType) {
        Long appServiceId = appServiceDTO.getId();
        Set<Long> serviceVersionIds = new HashSet<>();
        FileUtil.createDirectory(APPLICATION);
        downloadPayload.getAppServiceVersionDownloadPayloads().forEach(appServiceVersionPayload -> {
            AppServiceVersionDTO versionDTO = appServiceVersionService.baseQueryByAppIdAndVersion(appServiceId, appServiceVersionPayload.getVersion());
            versionDTO = versionDTO == null ? new AppServiceVersionDTO() : versionDTO;
            if (!downloadType.equals(DOWNLOAD_ONLY)) {
                String chartFilePath = String.format("%s%s%s%s", APPLICATION, File.separator, APPLICATION + System.currentTimeMillis(), TGZ);
                fileDownload(appServiceVersionPayload.getChartFilePath(), chartFilePath);
                LOGGER.info("=========应用下载，文件下载成功=========");
                AppServiceVersionDTO appServiceVersionDTO = chartResolver(appServiceVersionPayload, appServiceId, downloadPayload.getAppServiceCode(), new File(chartFilePath), versionDTO);
                serviceVersionIds.add(appServiceVersionDTO.getId());
            }
            if (!downloadType.equals(DEPLOY_ONLY)) {
                String repoFilePath = String.format("%s%s%s%s", APPLICATION, File.separator, APPLICATION + System.currentTimeMillis(), ZIP);
                fileDownload(appServiceVersionPayload.getRepoFilePath(), repoFilePath);
                LOGGER.info("=========应用下载，文件下载成功=========");
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
                String appRepoFilePath = String.format(APP_FILE_PATH_FORMAT, appFilePath, File.separator, REPO, File.separator, marketUploadVO.getMktAppCode());
                //clone 并压缩源代码
                marketUploadVO.getAppServiceUploadPayloads().forEach(appServiceMarketVO -> packageRepo(appServiceMarketVO, appRepoFilePath));
                String outputFilePath = String.format(APP_OUT_FILE_FORMAT, appFile.getParent(), File.separator, REPO, marketUploadVO.getMktAppCode(), System.currentTimeMillis(), ZIP);
                toZip(outputFilePath, appRepoFilePath);
                zipFileList.add(outputFilePath);
                break;
            }
            case DEPLOY_ONLY: {
                String appChartFilePath = String.format(APP_FILE_PATH_FORMAT, appFilePath, File.separator, CHART, File.separator, marketUploadVO.getMktAppCode());
                marketUploadVO.getAppServiceUploadPayloads().forEach(appServiceMarketVO -> packageChart(appServiceMarketVO, appChartFilePath));

                String outputFilePath = String.format(APP_OUT_FILE_FORMAT, appFile.getParent(), File.separator, CHART, marketUploadVO.getMktAppCode(), System.currentTimeMillis(), ZIP);
                toZip(outputFilePath, appChartFilePath);
                zipFileList.add(outputFilePath);
                marketImageUrlVO = pushImageForUpload(marketUploadVO);
                break;
            }
            case ALL: {
                String appRepoFilePath = String.format(APP_FILE_PATH_FORMAT, appFilePath, File.separator, REPO, File.separator, marketUploadVO.getMktAppCode());
                String appChartFilePath = String.format(APP_FILE_PATH_FORMAT, appFilePath, File.separator, CHART, File.separator, marketUploadVO.getMktAppCode());

                marketUploadVO.getAppServiceUploadPayloads().forEach(appServiceMarketVO -> {
                    packageRepo(appServiceMarketVO, appRepoFilePath);
                    packageChart(appServiceMarketVO, appChartFilePath);
                });

                String outputFilePath = String.format(APP_OUT_FILE_FORMAT, appFile.getParent(), File.separator, CHART, marketUploadVO.getMktAppCode(), System.currentTimeMillis(), ZIP);
                toZip(outputFilePath, appChartFilePath);
                zipFileList.add(outputFilePath);

                outputFilePath = String.format(APP_OUT_FILE_FORMAT, appFile.getParent(), File.separator, REPO, marketUploadVO.getMktAppCode(), System.currentTimeMillis(), ZIP);
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
        String appServiceDir = null;
        try {
            FileUtil.unZipFiles(file, unZipFilePath);
            LOGGER.info("=========应用下载，文件解压成功=========");
            String repoFilePath = String.format(APP_TEMP_PATH_FORMAT, unZipFilePath, File.separator, appServiceVersionPayload.getVersion());
            if (isFirst) {
                git = gitUtil.initGit(new File(repoFilePath));
                LOGGER.info("==========应用下载，git初始化成功==========");
            } else {
                appServiceDir = APPLICATION + System.currentTimeMillis();
                String appServiceFilePath = gitUtil.clone(appServiceDir, repositoryUrl, accessToken);
                LOGGER.info("==========应用下载，git clone成功==========");
                git = gitUtil.combineAppMarket(appServiceFilePath, repoFilePath);
            }
            //6. push 到远程仓库
            LOGGER.info("==========应用下载，开始推送代码==========");
            gitUtil.commitAndPushForMaster(git, repositoryUrl, appServiceVersionPayload.getVersion(), accessToken);
            LOGGER.info("==========应用下载，代码推送成功==========");

            versionDTO.setAppServiceId(appServiceId);
            BeanUtils.copyProperties(appServiceVersionPayload, versionDTO);
            LOGGER.info("==========应用下载，创建版本成功==========");
            versionDTO.setCommit(gitUtil.getFirstCommit(git));
            appServiceVersionService.baseCreateOrUpdate(versionDTO);
            LOGGER.info("==========应用下载，创建版本Service成功==========");
        } catch (Exception e) {
            throw new CommonException("error.resolver.git", e);
        } finally {
            FileUtil.deleteFile(file);
            FileUtil.deleteDirectory(new File(unZipFilePath));
            if (appServiceDir != null) {
                FileUtil.deleteDirectory(new File(appServiceDir));
            }
        }
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
        LOGGER.info("==========应用下载，chart.zip解压成功==========");
        FileUtil.unTarGZ(file, unZipPath);
        LOGGER.info("==========应用下载，chart.tgz解压成功==========");
        File zipDirectory = new File(String.format(APP_TEMP_PATH_FORMAT, unZipPath, File.separator, appServiceCode));
        helmUrl = helmUrl.endsWith("/") ? helmUrl : helmUrl + "/";
        harborUrl = harborUrl.endsWith("/") ? harborUrl : harborUrl + "/";
        versionDTO.setAppServiceId(appServiceId);
        String newTgzFile = null;
        File[] listFiles = zipDirectory.listFiles();
        try {
            //解析 解压过后的文件
            if (zipDirectory.exists() && zipDirectory.isDirectory() && listFiles != null) {

                BeanUtils.copyProperties(appServiceVersionPayload, versionDTO);
                //9. 获取替换Repository
                LOGGER.info("==========应用下载，chart开始解析文件==========");
                List<File> appMarkets = Arrays.stream(listFiles).parallel()
                        .filter(k -> k.getName().equals(VALUES))
                        .collect(Collectors.toCollection(ArrayList::new));
                if (!appMarkets.isEmpty() && appMarkets.size() == 1) {
                    File valuesFile = appMarkets.get(0);

                    Map<String, String> params = new HashMap<>();
                    params.put(appServiceVersionPayload.getImage().substring(0, appServiceVersionPayload.getImage().indexOf(":")), String.format("%s%s/%s", getDomain(harborUrl), MARKET_PRO, appServiceCode));
                    LOGGER.info("==========应用下载，chart文件替换参数1{}==========", appServiceVersionPayload.getImage().substring(0, appServiceVersionPayload.getImage().indexOf(":")));
                    LOGGER.info("==========应用下载，chart文件替换参数2{}==========", String.format("%s%s/%s", getDomain(harborUrl), MARKET_PRO, appServiceCode));

                    FileUtil.fileToInputStream(valuesFile, params);
                    LOGGER.info("==========应用下载，chart 参数替换成功==========");
                    //10. 创建appServiceValue
                    AppServiceVersionValueDTO versionValueDTO = new AppServiceVersionValueDTO();
                    versionValueDTO.setValue(FileUtil.getFileContent(valuesFile));
                    versionDTO.setValueId(appServiceVersionValueService.baseCreate(versionValueDTO).getId());
                    // 创建ReadMe
                    AppServiceVersionReadmeDTO versionReadmeDTO = new AppServiceVersionReadmeDTO();
                    versionReadmeDTO.setReadme(FileUtil.getReadme(unZipPath));
                    versionDTO.setReadmeValueId(appServiceVersionReadmeService.baseCreate(versionReadmeDTO).getId());
                }
            } else {
                FileUtil.deleteDirectory(zipDirectory);
                LOGGER.info("==========应用下载，chart.tgz解压失败=========");
                throw new CommonException("error.zip.empty");
            }
            LOGGER.info("==========应用下载，chart 开始上传chart包==========");
            newTgzFile = String.format("%s%s%s", APPLICATION, File.separator, CHART + System.currentTimeMillis());
            FileUtil.toTgz(String.format(APP_TEMP_PATH_FORMAT, unZipPath, File.separator, appServiceCode), newTgzFile);
            chartUtil.uploadChart(MARKET_PRO, appServiceCode, new File(newTgzFile + TGZ));
            LOGGER.info("==========应用下载，chart包删除成功==========");
        } catch (Exception e) {
            throw new CommonException("error.resolver.chart", e.getMessage());
        } finally {
            FileUtil.deleteFile(file);
            FileUtil.deleteDirectory(new File(unZipPath));
            FileUtil.deleteFile(newTgzFile + TGZ);
        }
        //创建version
        versionDTO.setRepository(String.format("%s/%s/%s", harborUrl, MARKET_PRO, appServiceCode));
        appServiceVersionService.baseCreateOrUpdate(versionDTO);
        LOGGER.info("==========应用下载，chart解析完结==========");
        return versionDTO;
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
        marketImageUrlVO.setAppCode(appMarketUploadVO.getMktAppCode());

        List<MarketAppServiceImageVO> imageVOList = new ArrayList<>();
        appMarketUploadVO.getAppServiceUploadPayloads().forEach(appServiceMarketVO -> {
            MarketAppServiceImageVO appServiceImageVO = new MarketAppServiceImageVO();
            appServiceImageVO.setServiceCode(appServiceMarketVO.getAppServiceCode());
            List<MarketAppServiceVersionImageVO> appServiceVersionImageVOS = new ArrayList<>();

            //获取原仓库配置
            ConfigVO configVO = devopsConfigService.queryByResourceId(
                    appServiceService.baseQuery(appServiceMarketVO.getAppServiceId()).getChartConfigId(), "harbor")
                    .get(0).getConfig();
            User sourceUser = new User();
            BeanUtils.copyProperties(configVO, sourceUser);
            sourceUser.setUsername(configVO.getUserName());

            User targetUser = new User();
            targetUser.setUsername(appMarketUploadVO.getUser().getRobotName());
            targetUser.setPassword(appMarketUploadVO.getUser().getRobotToken());

            //准备认证json
            String configStr = createConfigJson(sourceUser, getDomain(configVO.getUrl()), targetUser, getDomain(appServiceMarketVO.getHarborUrl()));
            appServiceMarketVO.getAppServiceVersionUploadPayloads().forEach(t -> {
                //推送镜像
                String targetImageUrl = String.format("%s:%s", appServiceMarketVO.getHarborUrl(), t.getVersion());
                pushImageScript(appServiceVersionService.baseQuery(t.getId()).getImage(), targetImageUrl, configStr);

                MarketAppServiceVersionImageVO appServiceVersionImageVO = new MarketAppServiceVersionImageVO();
                appServiceVersionImageVO.setVersion(t.getVersion());
                appServiceVersionImageVO.setImageUrl(targetImageUrl);
                appServiceVersionImageVOS.add(appServiceVersionImageVO);
            });
            appServiceImageVO.setServiceVersionVOS(appServiceVersionImageVOS);
            imageVOList.add(appServiceImageVO);
        });
        marketImageUrlVO.setServiceImageVOS(imageVOList);
        return marketImageUrlVO;
    }

    private void pushImageForDownload(AppMarketDownloadPayload appMarketDownloadVO) {
        HarborPayload harborPayload = new HarborPayload(null, MARKET_PRO);
        harborService.createHarborForProject(harborPayload);
        appMarketDownloadVO.getAppServiceDownloadPayloads().forEach(appServiceMarketVO -> {
            ConfigVO configVO = gson.fromJson(devopsConfigService.baseQueryByName(null, HARBOR_NAME).getConfig(), ConfigVO.class);
            User targetUser = new User();
            BeanUtils.copyProperties(configVO, targetUser);
            targetUser.setUsername(configVO.getUserName());

            User sourceUser = new User();
            sourceUser.setUsername(appMarketDownloadVO.getUser().getRobotName());
            sourceUser.setPassword(appMarketDownloadVO.getUser().getRobotToken());
            harborUrl = harborUrl.endsWith("/") ? harborUrl : harborUrl + "/";
            //准备认证json
            appServiceMarketVO.getAppServiceVersionDownloadPayloads().forEach(t -> {
                String configStr = createConfigJson(sourceUser, getDomain(t.getImage()), targetUser, getDomain(configVO.getUrl()));
                pushImageScript(t.getImage(), String.format("%s%s/%s:%s", getDomain(harborUrl), MARKET_PRO, appServiceMarketVO.getAppServiceCode(), t.getVersion()), configStr);
            });
        });
    }

    private void fileUpload(List<String> zipFileList, AppMarketUploadPayload appMarketUploadVO, MarketImageUrlVO marketImageUrlVO) {

        String mapJson = marketImageUrlVO != null ? gson.toJson(marketImageUrlVO) : null;

        Boolean result = null;
        if (appMarketUploadVO.getMarketSaaSPlatform()) {
            MultipartFile[] files = createMockMultipartFile(zipFileList);
            result = marketServiceClientOperator.uploadFile(appMarketUploadVO.getAppVersion(), files, mapJson);
        } else {
            List<MultipartBody.Part> files = createMultipartBody(zipFileList);
            String getawayUrl = appMarketUploadVO.getSaasGetawayUrl().endsWith("/") ? appMarketUploadVO.getSaasGetawayUrl() : appMarketUploadVO.getSaasGetawayUrl() + "/";
            MarketServicePublicClient marketServiceClient = RetrofitHandler.getMarketServiceClient(getawayUrl, MARKET);

            String remoteToken = baseServiceClientOperator.checkLatestToken();
            Call<ResponseBody> responseCall = marketServiceClient.uploadFile(remoteToken, appMarketUploadVO.getAppVersion(), files, mapJson);
            result = RetrofitCallExceptionParse.executeCall(responseCall, ERROR_UPLOAD, Boolean.class);
        }
        if (!result) {
            throw new CommonException(ERROR_UPLOAD);
        }
    }

    private void fileUploadFixVersion(List<String> zipFileList, AppMarketFixVersionPayload appMarketFixVersionPayload, MarketImageUrlVO marketImageUrlVO) {
        Boolean result = null;
        String imageJson = marketImageUrlVO != null ? gson.toJson(marketImageUrlVO) : null;
        String appJson = gson.toJson(appMarketFixVersionPayload.getMarketApplicationVO());
        if (appMarketFixVersionPayload.getFixVersionUploadPayload().getMarketSaaSPlatform()) {
            MultipartFile[] files = createMockMultipartFile(zipFileList);
            result = marketServiceClientOperator.updateAppPublishInfoFix(
                    appMarketFixVersionPayload.getMarketApplicationVO().getCode(),
                    appMarketFixVersionPayload.getMarketApplicationVO().getVersion(),
                    appJson,
                    files,
                    imageJson);
        } else {
            List<MultipartBody.Part> files = createMultipartBody(zipFileList);

            String getawayUrl = appMarketFixVersionPayload.getFixVersionUploadPayload().getSaasGetawayUrl().endsWith("/") ? appMarketFixVersionPayload.getFixVersionUploadPayload().getSaasGetawayUrl() : appMarketFixVersionPayload.getFixVersionUploadPayload().getSaasGetawayUrl() + "/";
            MarketServicePublicClient marketServiceClient = RetrofitHandler.getMarketServiceClient(getawayUrl, MARKET);
            String remoteToken = baseServiceClientOperator.checkLatestToken();
            Call<ResponseBody> responseCall = marketServiceClient.updateAppPublishInfoFix(
                    remoteToken,
                    appMarketFixVersionPayload.getMarketApplicationVO().getCode(),
                    appMarketFixVersionPayload.getMarketApplicationVO().getVersion(),
                    appJson,
                    files,
                    imageJson);
            result = RetrofitCallExceptionParse.executeCall(responseCall, ERROR_UPLOAD, Boolean.class);
        }
        if (!result) {
            throw new CommonException(ERROR_UPLOAD);
        }
    }

    private void fileDownload(String fileUrl, String downloadFilePath) {
        LOGGER.info("============文件下载====fileUrl{}", fileUrl);
        LOGGER.info("============文件下载====downloadFilePath{}", downloadFilePath);

        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setType(CHART);
        configurationProperties.setInsecureSkipTlsVerify(false);
        List<String> fileUrlList = Arrays.asList(fileUrl.split("/"));
        String fileName = fileUrlList.get(fileUrlList.size() - 1);
        fileUrl = fileUrl.replace(fileName, "");
        configurationProperties.setBaseUrl(fileUrl);

        String[] fileNameArr = fileName.split("[?]");
        fileName = fileNameArr[0];
        Map map = getParam(fileNameArr[1]);

        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        MarketServicePublicClient marketServiceClient = retrofit.create(MarketServicePublicClient.class);
        FileOutputStream fos = null;
        try {
            LOGGER.info("============文件下载===={}", fileName);
            LOGGER.info("============文件下载===={},====={}", map.toString(), map.size());
            Call<ResponseBody> getTaz = marketServiceClient.downloadFile(fileName, map);
            Response<ResponseBody> response = getTaz.execute();
            LOGGER.info("============文件下载===={}", response.headers());
            fos = new FileOutputStream(downloadFilePath);
            if (response.body() != null) {
                LOGGER.info("============文件下载=========1111");
                InputStream is = response.body().byteStream();
                byte[] buffer = new byte[4096];
                int r = 0;
                while ((r = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, r);
                }
                LOGGER.info("============文件下载=========2222");
                is.close();
            }
            fos.close();
        } catch (IOException e) {
            IOUtils.closeQuietly(fos);
            throw new CommonException("error.download.file", e.getMessage());
        }
    }

    /**
     * 创建kaniko 拉取image config.json
     *
     * @param sourceUser
     * @param sourceUrl
     * @param targetUser
     * @param targetUrl
     * @return
     */
    private String createConfigJson(User sourceUser, String sourceUrl, User targetUser, String targetUrl) {

        JSONObject result = new JSONObject();

        JSONObject sourceAuth = new JSONObject();
        String sourceCode = Base64.getEncoder().encodeToString(String.format("%s:%s", sourceUser.getUsername(), sourceUser.getPassword()).getBytes());
        sourceAuth.put("auth", sourceCode);

        JSONObject targetAuth = new JSONObject();
        String targetCode = Base64.getEncoder().encodeToString(String.format("%s:%s", targetUser.getUsername(), targetUser.getPassword()).getBytes());
        targetAuth.put("auth", targetCode);

        JSONObject temp = new JSONObject();
        temp.put(sourceUrl, sourceAuth);
        temp.put(targetUrl, targetAuth);

        result.put("auths", temp);

        return result.toJSONString();
    }

    /**
     * 获取仓库域名
     *
     * @param url
     * @return
     */
    private String getDomain(String url) {
        String[] strArry;
        if (url.contains(DSLASH)) {
            strArry = url.split(DSLASH);
            return strArry[1];
        } else {
            strArry = url.split(SSLASH);
            return strArry[0];
        }
    }

    private Map getParam(String url) {
        Map<String, String> map = new HashMap<>();
        String[] arr = url.split("&");
        for (String str : arr) {
            String[] temp = str.split("=");
            map.put(temp[0], temp[1]);
        }
        return map;
    }

    private MultipartFile[] createMockMultipartFile(List<String> zipFileList) {
        MultipartFile[] files = new MultipartFile[zipFileList.size()];
        for (int i = 0; i < zipFileList.size(); i++) {
            File file = new File(zipFileList.get(i));
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                files[i] = new MockMultipartFile("files", file.getName(), ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);
            } catch (Exception e) {
                throw new CommonException(ERROR_UPLOAD, e);
            } finally {
                IOUtils.closeQuietly(fileInputStream);
            }
        }
        return files;
    }

    private List<MultipartBody.Part> createMultipartBody(List<String> zipFileList) {
        List<MultipartBody.Part> files = new ArrayList<>();
        zipFileList.forEach(f -> {
            File file = new File(f);
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("files", file.getName(), requestFile);
            files.add(body);
        });
        return files;
    }

    private AppServiceUploadPayload dtoToMarketVO(AppServiceDTO applicationDTO) {
        AppServiceUploadPayload appServiceMarketVO = new AppServiceUploadPayload();
        appServiceMarketVO.setAppServiceId(applicationDTO.getId());
        appServiceMarketVO.setAppServiceCode(applicationDTO.getCode());
        appServiceMarketVO.setAppServiceName(applicationDTO.getName());
        return appServiceMarketVO;
    }
}
