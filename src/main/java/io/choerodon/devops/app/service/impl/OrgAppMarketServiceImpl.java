package io.choerodon.devops.app.service.impl;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.devops.api.validator.HarborMarketVOValidator;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.ApplicationValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.payload.AppMarketDownloadPayload;
import io.choerodon.devops.app.eventhandler.payload.AppServiceDownloadPayload;
import io.choerodon.devops.app.eventhandler.payload.AppServiceVersionDownloadPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.harbor.User;
import io.choerodon.devops.infra.dto.iam.ApplicationDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.enums.PublishTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.thread.CommandWaitForThread;
import io.choerodon.devops.infra.util.*;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:28 2019/8/8
 * Description:
 */
@Component
public class OrgAppMarketServiceImpl implements OrgAppMarketService {
    public static final Logger LOGGER = LoggerFactory.getLogger(CommandWaitForThread.class);

    private static final String APPLICATION = "application";
    private static final String CHART = "chart";
    private static final String IMAGES = "images";
    private static final String PUSH_IAMGES = "push_image.sh";
    private static final String MARKET_PRO = "market-downloaded-app";
    private static final String HARBOR_NAME = "harbor_default";
    private static final String SITE_APP_GROUP_NAME_FORMAT = "site_%s";
    private static final String APP_REPO_FORMAT = "%s%srepo-%s-%s%s";
    private static final String APP_CHART_FORMAT = "%s%schart-%s-%s%s";


    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private ChartUtil chartUtil;
    @Value("${services.helm.url}")
    private String helmUrl;
    @Value("${services.harbor.baseUrl}")
    private String harborUrl;
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
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

    @Override
    public PageInfo<AppServiceUploadVO> pageByAppId(Long appId,
                                                    PageRequest pageRequest,
                                                    String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        List<String> paramList = TypeUtil.cast(mapParams.get(TypeUtil.PARAMS));
        PageInfo<AppServiceDTO> appServiceDTOPageInfo = PageHelper
                .startPage(pageRequest.getPage(),
                        pageRequest.getSize(),
                        PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                        appServiceMapper.listByAppId(appId, TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)), paramList));

        PageInfo<AppServiceUploadVO> appServiceMarketVOPageInfo = ConvertUtils.convertPage(appServiceDTOPageInfo, this::dtoToMarketVO);
        List<AppServiceUploadVO> list = appServiceMarketVOPageInfo.getList();
        list.forEach(appServiceMarketVO -> appServiceMarketVO.setAppServiceVersionUploadVOS(
                ConvertUtils.convertList(appServiceVersionService.baseListByAppServiceId(appServiceMarketVO.getAppServiceId()), AppServiceVersionUploadVO.class)));
        appServiceMarketVOPageInfo.setList(list);
        return appServiceMarketVOPageInfo;
    }

    @Override
    public List<AppServiceUploadVO> listAllAppServices() {
        List<AppServiceDTO> appServiceDTOList = appServiceMapper.selectAll();
        return ConvertUtils.convertList(appServiceDTOList, this::dtoToMarketVO);
    }

    @Override
    public void upload(AppMarketUploadVO marketUploadVO) {
        //创建根目录 应用
        String appFilePath = gitUtil.getWorkingDirectory(APPLICATION + System.currentTimeMillis());
        FileUtil.createDirectory(appFilePath);
        File appFile = new File(appFilePath);
        if (marketUploadVO.getStatus().equals(PublishTypeEnum.DOWNLOAD_ONLY.value())) {
            String appRepoFilePath = String.format("%s%s%s%s%s", appFilePath, File.separator, "repo", File.separator, marketUploadVO.getAppCode());
            //clone 并压缩源代码
            marketUploadVO.getAppServiceUploadVOS().forEach(appServiceMarketVO -> packageSourceCode(appServiceMarketVO, appRepoFilePath, marketUploadVO.getIamUserId()));
            //上传
            fileUpload(APP_REPO_FORMAT, appFile, appRepoFilePath, marketUploadVO.getAppCode(), marketUploadVO.getSaasGetawayUrl(), null);
        } else if (marketUploadVO.getStatus().equals(PublishTypeEnum.DEPLOY_ONLY.value())) {
            String appChartFilePath = String.format("%s%s%s%s%s", appFilePath, File.separator, "chart", File.separator, marketUploadVO.getAppCode());
            marketUploadVO.getAppServiceUploadVOS().forEach(appServiceMarketVO -> packageChart(appServiceMarketVO, appChartFilePath));
//            Map<String, String> map = pushImageForUpload(marketUploadVO);
            fileUpload(APP_CHART_FORMAT, appFile, appChartFilePath, marketUploadVO.getAppCode(), marketUploadVO.getSaasGetawayUrl(), null);
        } else {
            String appRepoFilePath = String.format("%s%s%s%s%s", appFilePath, File.separator, "repo", File.separator, marketUploadVO.getAppCode());
            String appChartFilePath = String.format("%s%s%s%s%s", appFilePath, File.separator, "chart", File.separator, marketUploadVO.getAppCode());

            marketUploadVO.getAppServiceUploadVOS().forEach(appServiceMarketVO -> {
                packageSourceCode(appServiceMarketVO, appRepoFilePath, marketUploadVO.getIamUserId());
                packageChart(appServiceMarketVO, appChartFilePath);
            });
//            Map<String, String> map = pushImageForUpload(marketUploadVO);

            fileUpload(APP_REPO_FORMAT, appFile, appRepoFilePath, marketUploadVO.getAppCode(), marketUploadVO.getSaasGetawayUrl(), null);
            fileUpload(APP_CHART_FORMAT, appFile, appChartFilePath, marketUploadVO.getAppCode(), marketUploadVO.getSaasGetawayUrl(), null);
        }
        FileUtil.deleteDirectory(appFile);
    }

    private void fileUpload(String type, File appFile, String inputFilePath, String appCode, String uploadUrl, Map map) {
        String outputFilePath = String.format(type, appFile.getParent(), File.separator, appCode, System.currentTimeMillis(), ".zip");
        toZip(outputFilePath, inputFilePath);
        //todo 调用上传接口
//        ConfigurationProperties configurationProperties = new ConfigurationProperties();
//        configurationProperties.setBaseUrl(uploadUrl);
//        configurationProperties.setInsecureSkipTlsVerify(false);
//        configurationProperties.setType("market");
//        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
//        MarketServiceClient marketServiceClient = retrofit.create(MarketServiceClient.class);
//        marketServiceClient.uploadFile().execute();
        FileUtil.deleteFile(outputFilePath);
    }

    @Override
    public String createHarborRepository(HarborMarketVO harborMarketVO) {
        HarborMarketVOValidator.checkEmailAndPassword(harborMarketVO);
        return harborService.createHarborForAppMarket(harborMarketVO);
    }

    @Override
    public List<AppServiceVersionUploadVO> listServiceVersionsByAppServiceId(Long appServiceId) {
        List<AppServiceVersionDTO> appServiceVersionDTOList = appServiceVersionService.baseListByAppServiceId(appServiceId);
        return ConvertUtils.convertList(appServiceVersionDTOList, AppServiceVersionUploadVO.class);
    }

    @Override
    public void downLoadApp(AppMarketDownloadPayload appMarketDownloadVO) {
        File file = new File("D:\\mydata_file\\test\\application1565938344784.zip");
        String unZipPath = "D:\\mydata_file\\test\\temp";
        FileUtil.unZipFiles(file, unZipPath);
        File zipDirectory = new File(unZipPath);

        DevopsProjectDTO projectDTO = devopsProjectService.queryByAppId(appMarketDownloadVO.getAppId());

        ApplicationDTO applicationDTO = baseServiceClientOperator.queryAppById(appMarketDownloadVO.getAppId());
        String groupPath = String.format(SITE_APP_GROUP_NAME_FORMAT, applicationDTO.getCode());
        appMarketDownloadVO.getAppServiceMarketDownloadVOS().forEach(appServicePayload -> {
            appServicePayload.setAppId(appMarketDownloadVO.getAppId());
            //解析 解压过后的文件
            if (zipDirectory.exists() && zipDirectory.isDirectory() && zipDirectory.listFiles() != null) {
                File[] appFiles = zipDirectory.listFiles()[0].listFiles();
                //获取替换Repository
                for (File appServiceFile : appFiles) {
                    if (appServiceFile.getName().contains(appServicePayload.getAppServiceCode())) {
                        createRemoteAppService(appServicePayload,
                                TypeUtil.objToInteger(projectDTO.getDevopsAppGroupId()),
                                appMarketDownloadVO.getIamUserId(),
                                groupPath,
                                Arrays.asList(appServiceFile.listFiles()));
                    }
                }
            } else {
                FileUtil.deleteDirectory(zipDirectory);
                throw new CommonException("error.chart.empty");
            }
        });

        pushImageForDownload(appMarketDownloadVO);
    }


    private void createRemoteAppService(AppServiceDownloadPayload downloadPayload, Integer gitlabGroupId, Long iamUserId, String groupPath, List<File> fileList) {
        ApplicationValidator.checkApplicationService(downloadPayload.getAppServiceCode());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(iamUserId);

        //1. 校验是否已经下载过
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByCode(downloadPayload.getAppServiceCode(), downloadPayload.getAppId());
        Boolean isFirst = appServiceDTO == null;
        if (appServiceDTO == null) {
            appServiceDTO = ConvertUtils.convertObject(downloadPayload, AppServiceDTO.class);
            appServiceDTO.setActive(true);
            appServiceDTO.setIsSkipCheckPermission(true);
            //2. 第一次下载创建应用服务
            //2. 分配所在gitlab group 用户权限
            MemberDTO memberDTO = gitlabServiceClientOperator.queryGroupMember(gitlabGroupId, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            if (memberDTO.getId() == null || !memberDTO.getAccessLevel().equals(AccessLevel.OWNER.value)) {
                memberDTO = new MemberDTO();
                memberDTO.setId(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
                memberDTO.setAccessLevel(AccessLevel.OWNER.value);
                gitlabServiceClientOperator.createGroupMember(gitlabGroupId, memberDTO);
            }

            //3. 创建gitlab project
            GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.createProject(gitlabGroupId,
                    downloadPayload.getAppServiceCode(),
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()),
                    true);
            appServiceDTO.setGitlabProjectId(gitlabProjectDTO.getId());
            appServiceDTO.setSynchro(true);
            appServiceDTO.setFailed(false);
            appServiceService.baseCreate(appServiceDTO);
        }

        String applicationDir = APPLICATION + System.currentTimeMillis();
        String accessToken = appServiceService.getToken(appServiceDTO.getGitlabProjectId(), applicationDir, userAttrDTO);
        Long appServiceId = appServiceDTO.getId();
        downloadPayload.getAppServiceVersionDownloadVOS().forEach(appServiceVersionPayload -> {
            Git git = null;
            String repository = null;
            List<File> versionFileList = fileList.stream().filter(f -> f.getName()
                    .equals(appServiceVersionPayload.getVersion()) || f.getName().equals(String.format("%s%s", appServiceVersionPayload.getVersion(), ".tgz")))
                    .collect(Collectors.toList());
            if (versionFileList != null && !versionFileList.isEmpty()) {
                for (File file : versionFileList) {
                    if (file.getName().contains(".tgz")) {
                        repository = chartResolver(appServiceVersionPayload, appServiceId, downloadPayload.getAppServiceCode(), file);
                    } else {
                        git = gitResolver(isFirst, groupPath, file, downloadPayload, accessToken);
                    }
                }
                AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQueryByAppIdAndVersion(appServiceId, appServiceVersionPayload.getVersion());
                appServiceVersionDTO.setCommit(gitUtil.getFirstCommit(git));
                appServiceVersionDTO.setRepository(repository);
                appServiceVersionService.baseUpdate(appServiceVersionDTO);
            } else {
                throw new CommonException("error.app.service.version");
            }
        });
    }

    private Git gitResolver(Boolean isFirst, String groupPath, File file, AppServiceDownloadPayload downloadPayload, String accessToken) {
        Git git = null;
        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        String repositoryUrl = repoUrl + groupPath + "/" + downloadPayload.getAppServiceCode() + ".git";
        if (isFirst) {
            git = gitUtil.initGit(file);
        } else {
            String appServiceDir = APPLICATION + System.currentTimeMillis();
            String appServiceFilePath = gitUtil.clone(appServiceDir, repositoryUrl, accessToken);
            git = gitUtil.combineAppMarket(appServiceFilePath, file.getAbsolutePath());
        }
        //6. push 到远程仓库
        gitUtil.commitAndPushForMaster(git, repositoryUrl, file.getName(), accessToken);
        return git;
    }


    private String chartResolver(AppServiceVersionDownloadPayload appServiceVersionPayload, Long appServiceId, String appServiceCode, File file) {
        String unZipPath = String.format("%s%s%s", file.getParentFile().getAbsolutePath(), File.separator, "chart");
        FileUtil.unTarGZ(file.getAbsoluteFile(), unZipPath);
        File zipDirectory = new File(String.format("%s%s%s", unZipPath, File.separator, appServiceCode));
        helmUrl = helmUrl.endsWith("/") ? helmUrl : helmUrl + "/";
        AppServiceVersionDTO versionDTO = new AppServiceVersionDTO();
        versionDTO.setAppServiceId(appServiceId);
        //解析 解压过后的文件
        if (zipDirectory.exists() && zipDirectory.isDirectory()) {

            File[] listFiles = zipDirectory.listFiles();
            BeanUtils.copyProperties(appServiceVersionPayload, versionDTO);
            //9. 获取替换Repository
            List<File> appMarkets = Arrays.stream(listFiles).parallel()
                    .filter(k -> k.getName().equals("values.yaml"))
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
                appServiceVersionService.baseCreate(versionDTO);
            }
        } else {
            FileUtil.deleteDirectory(zipDirectory);
            throw new CommonException("error.zip.empty");
        }

        chartUtil.uploadChart("market", "downloaded-app", file);
        return String.format("%s/%s/%s", harborUrl, MARKET_PRO, appServiceCode);

//        //11. 打包 上传
//        String newZipPath = file.getPath();
//        FileUtil.deleteFile(file);
//        try {
//            FileOutputStream fileOutputStream = new FileOutputStream(new File(newZipPath));
//            FileUtil.toZip(unZipPath, fileOutputStream, true);
//            chartUtil.uploadChart("market", "downloaded-app", new File(newZipPath));
//        } catch (FileNotFoundException e) {
//            throw new CommonException("error.upload.chart");
//        } finally {
//            FileUtil.deleteDirectory(new File(unZipPath));
//            FileUtil.deleteDirectory(new File(newZipPath));
//        }

    }

    private void packageSourceCode(AppServiceUploadVO appServiceMarketVO, String appFilePath, Long iamUserId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceMarketVO.getAppServiceId());
        ApplicationDTO applicationDTO = baseServiceClientOperator.queryAppById(appServiceDTO.getAppId());
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(applicationDTO.getOrganizationId());

        //1.创建目录 应用服务仓库
        FileUtil.createDirectory(appFilePath, appServiceDTO.getCode());
        String appServiceRepositoryPath = String.format("%s/%s", appFilePath, appServiceDTO.getCode());

        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(iamUserId);
        String newToken = appServiceService.getToken(appServiceDTO.getGitlabProjectId(), appFilePath, userAttrDTO);
        appServiceDTO.setRepoUrl(repoUrl + organizationDTO.getCode()
                + "-" + applicationDTO.getCode() + "/" + appServiceDTO.getCode() + ".git");
        appServiceMarketVO.getAppServiceVersionUploadVOS().forEach(appServiceMarketVersionVO -> {
            AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(appServiceMarketVersionVO.getId());
            //2. 创建目录 应用服务版本

            FileUtil.createDirectory(appServiceRepositoryPath, appServiceVersionDTO.getVersion());
            String appServiceVersionPath = String.format("%s/%s", appServiceRepositoryPath, appServiceVersionDTO.getVersion());

            //3.clone源码,checkout到版本所在commit，并删除.git文件
            gitUtil.cloneAndCheckout(appServiceVersionPath, appServiceDTO.getRepoUrl(), newToken, appServiceVersionDTO.getCommit());
            toZip(String.format("%s%s", appServiceVersionPath, ".zip"), appServiceVersionPath);
            FileUtil.deleteDirectory(new File(appServiceVersionPath));
        });
    }

    private void packageChart(AppServiceUploadVO appServiceMarketVO, String appFilePath) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceMarketVO.getAppServiceId());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getAppId());
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        //1.创建目录 应用服务仓库
        FileUtil.createDirectory(appFilePath, appServiceDTO.getCode());
        String appServiceChartPath = String.format("%s%s%s", appFilePath, File.separator, appServiceDTO.getCode());
        appServiceMarketVO.getAppServiceVersionUploadVOS().forEach(appServiceMarketVersionVO -> {
            //2.下载chart
            AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(appServiceMarketVersionVO.getId());
            chartUtil.downloadChart(appServiceVersionDTO, organizationDTO, projectDTO, appServiceDTO, appServiceChartPath);
        });
    }

    private void analysisChart(String zipPath, String appServiceCode, AppServiceVersionDTO appServiceVersionDTO, String harborUrl) {
        FileUtil.unTarGZ(String.format("%s%s%s-%s.tgz",
                zipPath,
                File.separator,
                appServiceCode,
                appServiceVersionDTO.getVersion()), zipPath);
        String unZipPath = String.format("%s%s%s", zipPath, File.separator, appServiceCode);
        File zipDirectory = new File(unZipPath);
        //解析 解压过后的文件
        if (zipDirectory.exists() && zipDirectory.isDirectory()) {
            File[] listFiles = zipDirectory.listFiles();
            //获取替换Repository
            List<File> appMarkets = Arrays.stream(listFiles).parallel()
                    .filter(k -> "values.yaml".equals(k.getName()))
                    .collect(Collectors.toCollection(ArrayList::new));
            if (!appMarkets.isEmpty() && appMarkets.size() == 1) {
                Map<String, String> params = new HashMap<>();
                String image = appServiceVersionDTO.getImage().replace(":" + appServiceVersionDTO.getVersion(), "");
                params.put(image, String.format("%s/%s", harborUrl, appServiceVersionDTO.getVersion()));
                FileUtil.fileToInputStream(appMarkets.get(0), params);
            }
        } else {
            FileUtil.deleteDirectory(new File(zipPath).getParentFile());
            throw new CommonException("error.chart.empty");
        }
        // 打包
        String chartFilePath = String.format("%s%s%s-%s.tgz", zipPath, File.separator, appServiceCode, appServiceVersionDTO.getVersion());
        FileUtil.deleteFile(new File(chartFilePath));
        String newChartFilePath = String.format("%s%s%s.tgz", zipPath, File.separator, appServiceVersionDTO.getVersion());

        toZip(newChartFilePath, unZipPath);
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

    private Map<String, String> pushImageForUpload(AppMarketUploadVO appMarketUploadVO) {
        Map<String, String> iamgeMap = new HashMap<>();

        //获取push_image 脚本目录
        String shellPath = new File(this.getClass().getResource("/shell").getPath()).getAbsolutePath();
        // 创建images
        appMarketUploadVO.getAppServiceUploadVOS().forEach(appServiceMarketVO -> {
            StringBuilder stringBuilder = new StringBuilder();
            appServiceMarketVO.getAppServiceVersionUploadVOS().forEach(t -> {
                stringBuilder.append(appServiceVersionService.baseQuery(t.getId()).getImage());
                stringBuilder.append(System.getProperty("line.separator"));
                iamgeMap.put(String.format("%s-%s", appServiceMarketVO.getAppServiceCode(), t.getVersion()), String.format("%s:%s", appServiceMarketVO.getHarborUrl(), t.getVersion()));
            });
            FileUtil.saveDataToFile(shellPath, IMAGES, stringBuilder.toString());

            //获取原仓库配置
            ConfigVO configVO = devopsConfigService.queryByResourceId(
                    appServiceService.baseQuery(appServiceMarketVO.getAppServiceId()).getChartConfigId(), "harbor")
                    .get(0).getConfig();
            User user = new User();
            BeanUtils.copyProperties(configVO, user);

            // 执行脚本
            callScript(shellPath, appServiceMarketVO.getHarborUrl(), appMarketUploadVO.getUser(), user);
            FileUtil.deleteFile(String.format("%s%s%s", shellPath, File.separator, IMAGES));
        });
        return iamgeMap;
    }

    private void pushImageForDownload(AppMarketDownloadPayload appMarketDownloadVO) {
        //获取push_image 脚本目录
        String shellPath = this.getClass().getResource("/shell").getPath();

        appMarketDownloadVO.getAppServiceMarketDownloadVOS().forEach(appServiceMarketVO -> {
            StringBuilder stringBuilder = new StringBuilder();
            appServiceMarketVO.getAppServiceVersionDownloadVOS().forEach(t -> {
                stringBuilder.append(t.getImage());
                stringBuilder.append(System.getProperty("line.separator"));
            });
            FileUtil.saveDataToFile(shellPath, IMAGES, stringBuilder.toString());

            //获取新仓库配置
            ConfigVO configVO = TypeUtil.cast(devopsConfigService.baseQueryByName(null, HARBOR_NAME).getConfig());
            User user = new User();
            BeanUtils.copyProperties(configVO, user);
            harborUrl = harborUrl.endsWith("/") ? harborUrl : harborUrl + "/";

            callScript(shellPath, String.format("%s/%s", harborUrl, MARKET_PRO), user, appMarketDownloadVO.getUser());
            FileUtil.deleteFile(String.format("%s%s%s", shellPath, File.separator, IMAGES));
        });
    }

    /**
     * 脚本文件具体执行及脚本执行过程探测
     *
     * @param script 脚本文件绝对路径
     */
    private void callScript(String script, String harborUrl, User newUser, User oldUser) {
        try {
            String cmd = String.format("cd %s \n" +
                            " sh %s %s %s %s %s %s",
                    script, PUSH_IAMGES, harborUrl, newUser.getUsername(), newUser.getPassword(), oldUser.getUsername(), oldUser.getPassword());
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
            throw new CommonException("error.exec.push.image");
        }
    }


    private AppServiceUploadVO dtoToMarketVO(AppServiceDTO applicationDTO) {
        AppServiceUploadVO appServiceMarketVO = new AppServiceUploadVO();
        appServiceMarketVO.setAppServiceId(applicationDTO.getId());
        appServiceMarketVO.setAppServiceCode(applicationDTO.getCode());
        appServiceMarketVO.setAppServiceName(applicationDTO.getName());
        return appServiceMarketVO;
    }
}
