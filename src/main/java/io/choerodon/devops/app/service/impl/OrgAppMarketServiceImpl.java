package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
import io.choerodon.devops.app.eventhandler.payload.AppServicePayload;
import io.choerodon.devops.app.eventhandler.payload.AppServiceVersionPayload;
import io.choerodon.devops.app.eventhandler.payload.ApplicationPayload;
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
    public PageInfo<AppServiceMarketVO> pageByAppId(Long appId,
                                                    PageRequest pageRequest,
                                                    String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        List<String> paramList = TypeUtil.cast(mapParams.get(TypeUtil.PARAMS));
        PageInfo<AppServiceDTO> appServiceDTOPageInfo = PageHelper
                .startPage(pageRequest.getPage(),
                        pageRequest.getSize(),
                        PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                        appServiceMapper.listByAppId(appId, TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)), paramList));

        PageInfo<AppServiceMarketVO> appServiceMarketVOPageInfo = ConvertUtils.convertPage(appServiceDTOPageInfo, this::dtoToMarketVO);
        List<AppServiceMarketVO> list = appServiceMarketVOPageInfo.getList();
        list.forEach(appServiceMarketVO -> appServiceMarketVO.setAppServiceMarketVersionVOS(
                ConvertUtils.convertList(appServiceVersionService.baseListByAppServiceId(appServiceMarketVO.getAppServiceId()), AppServiceMarketVersionVO.class)));
        appServiceMarketVOPageInfo.setList(list);
        return appServiceMarketVOPageInfo;
    }

    @Override
    public List<AppServiceMarketVO> listAllAppServices() {
        List<AppServiceDTO> appServiceDTOList = appServiceMapper.selectAll();
        return ConvertUtils.convertList(appServiceDTOList, this::dtoToMarketVO);
    }

    @Override
    public void upload(AppMarketUploadVO marketUploadVO) {
        //1.创建根目录 应用
        String appFilePath = gitUtil.getWorkingDirectory(APPLICATION + System.currentTimeMillis());
        FileUtil.createDirectory(appFilePath);
        if (marketUploadVO.getStatus().equals(PublishTypeEnum.DOWNLOAD_ONLY.value())) {
            //2.clone 并压缩源代码
            marketUploadVO.getAppServiceMarketVOList().forEach(appServiceMarketVO -> packageSourceCode(appServiceMarketVO, appFilePath, marketUploadVO.getIamUserId()));
        } else if (marketUploadVO.getStatus().equals(PublishTypeEnum.DEPLOY_ONLY.value())) {
            marketUploadVO.getAppServiceMarketVOList().forEach(appServiceMarketVO -> packageChart(appServiceMarketVO, appFilePath, marketUploadVO.getHarborUrl()));
            pushImage(marketUploadVO);
        } else {
            marketUploadVO.getAppServiceMarketVOList().forEach(appServiceMarketVO -> {
                packageSourceCode(appServiceMarketVO, appFilePath, marketUploadVO.getIamUserId());
                packageChart(appServiceMarketVO, appFilePath, marketUploadVO.getHarborUrl());
            });
            pushImage(marketUploadVO);
        }
        //4.压缩文件
        toZip(String.format("%s%s", appFilePath, ".zip"), appFilePath);

    }

    @Override
    public String createHarborRepository(HarborMarketVO harborMarketVO) {
        return harborService.createHarborForAppMarket(harborMarketVO);
    }

    @Override
    public List<AppServiceMarketVersionVO> listServiceVersionsByAppServiceId(Long appServiceId) {
        List<AppServiceVersionDTO> appServiceVersionDTOList = appServiceVersionService.baseListByAppServiceId(appServiceId);
        return ConvertUtils.convertList(appServiceVersionDTOList, AppServiceMarketVersionVO.class);
    }

    @Override
    public void downLoadApp(ApplicationPayload applicationPayload) {
        File file = new File("D:\\mydata_file\\test\\application1565784577743.zip");
        String unZipPath = "D:\\mydata_file\\test\\temp";
        FileUtil.unZipFiles(file, unZipPath);
        File zipDirectory = new File(unZipPath);

        DevopsProjectDTO projectDTO = devopsProjectService.baseQueryByProjectId(applicationPayload.getAppId());

//        ApplicationDTO applicationDTO = baseServiceClientOperator.queryAppById(applicationPayload.getAppId());
//        OrganizationDTO organizationDTO = iamServiceClientOperator.queryOrganizationById(applicationDTO.getOrganizationId());
//        String groupPath = String.format("%s-%s", organizationDTO.getCode(), applicationDTO.getCode());
        String groupPath = "scporg-scpapp";
        applicationPayload.getAppServicePayloads().forEach(appServicePayload -> {
            appServicePayload.setAppId(applicationPayload.getAppId());
            //解析 解压过后的文件
            if (zipDirectory.exists() && zipDirectory.isDirectory() && zipDirectory.listFiles() != null) {
                File[] appFiles = zipDirectory.listFiles()[0].listFiles();
                //获取替换Repository
                for (File appServiceFile : appFiles) {
                    if (appServiceFile.getName().contains(appServicePayload.getCode())) {
                        createRemoteAppService(appServicePayload,
                                TypeUtil.objToInteger(projectDTO.getDevopsAppGroupId()),
                                applicationPayload.getIamUserId(),
                                groupPath,
                                Arrays.asList(appServiceFile.listFiles()));
                    }
                }
            } else {
                FileUtil.deleteDirectory(zipDirectory);
                throw new CommonException("error.chart.empty");
            }
        });
    }


    private void createRemoteAppService(AppServicePayload downloadPayload, Integer gitlabGroupId, Long iamUserId, String groupPath, List<File> fileList) {
        ApplicationValidator.checkApplicationService(downloadPayload.getCode());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(iamUserId);

        //1. 校验是否已经下载过
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByCode(downloadPayload.getCode(), downloadPayload.getAppId());
        Boolean isFirst = appServiceDTO == null;
//        Boolean isFirst = false;
        if (appServiceDTO == null) {
            appServiceDTO = ConvertUtils.convertObject(downloadPayload, AppServiceDTO.class);
            appServiceDTO.setActive(true);
            appServiceDTO.setIsSkipCheckPermission(true);
            //2. 第一次下载创建应用服务
            //2. 分配所在gitlab group 用户权限
            MemberDTO memberDTO = gitlabServiceClientOperator.queryGroupMember(gitlabGroupId, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            if (memberDTO.getUserId() == null || !memberDTO.getAccessLevel().equals(AccessLevel.OWNER.value)) {
                memberDTO = new MemberDTO();
                memberDTO.setUserId(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
                memberDTO.setAccessLevel(AccessLevel.OWNER.value);
                gitlabServiceClientOperator.createGroupMember(gitlabGroupId, memberDTO);
            }

            //3. 创建gitlab project
            GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.createProject(gitlabGroupId,
                    downloadPayload.getCode(),
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
        downloadPayload.getAppServiceVersionPayloads().forEach(appServiceVersionPayload -> {
            Git git = null;
            List<File> versionFileList = fileList.stream().filter(f -> f.getName()
                    .equals(appServiceVersionPayload.getVersion()) || f.getName().equals(String.format("%s%s", appServiceVersionPayload.getVersion(), ".tgz")))
                    .collect(Collectors.toList());
            if (versionFileList != null && !versionFileList.isEmpty()) {
                for (File file : versionFileList) {
                    if (file.getName().contains(".tgz")) {
                        chartResolver(appServiceVersionPayload, appServiceId, downloadPayload.getCode(), file);
                    } else {
                        git = gitResolver(isFirst, groupPath, file, downloadPayload, accessToken);
                    }
                }
                AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQueryByAppIdAndVersion(appServiceId, appServiceVersionPayload.getVersion());
                appServiceVersionDTO.setCommit(gitUtil.getFirstCommit(git));
                appServiceVersionService.baseUpdate(appServiceVersionDTO);
                // todo
//                pushImage();
            } else {
                throw new CommonException("error.app.service.version");
            }
        });

    }

    private Git gitResolver(Boolean isFirst, String groupPath, File file, AppServicePayload downloadPayload, String accessToken) {
        Git git = null;
        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        String repositoryUrl = repoUrl + groupPath + "/" + downloadPayload.getCode() + ".git";
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


    private void chartResolver(AppServiceVersionPayload appServiceVersionPayload, Long appServiceId, String appServiceCode, File file) {
        String unZipPath = String.format("%s%s%s", file.getParentFile().getAbsolutePath(), File.separator, "chart");
        FileUtil.unTarGZ(file.getAbsoluteFile(), unZipPath);
        File zipDirectory = new File(String.format("%s%s%s", unZipPath, File.separator, appServiceCode));
        helmUrl = helmUrl.endsWith("/") ? helmUrl : helmUrl + "/";
//            helmUrl = "http://helm-charts.staging.saas.hand-china.com/";
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
                params.put(appServiceVersionPayload.getRepository(), String.format("%s/%s/%s", harborUrl, MARKET_PRO, appServiceCode));
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

        //11. 打包 上传
        String newZipPath = file.getPath();
        FileUtil.deleteFile(file);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(newZipPath));
            FileUtil.toZip(unZipPath, fileOutputStream, true);
            chartUtil.uploadChart("market", "downloaded-app", new File(newZipPath));
        } catch (FileNotFoundException e) {
            throw new CommonException("error.upload.chart");
        } finally {
            FileUtil.deleteDirectory(new File(unZipPath));
            FileUtil.deleteDirectory(new File(newZipPath));
        }

    }

    private void packageSourceCode(AppServiceMarketVO appServiceMarketVO, String appFilePath, Long iamUserId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceMarketVO.getAppServiceId());
        ApplicationDTO applicationDTO = baseServiceClientOperator.queryAppById(appServiceDTO.getAppId());
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(applicationDTO.getOrganizationId());

        //1.创建目录 应用服务仓库
        FileUtil.createDirectory(appFilePath, appServiceDTO.getCode());
        String appServiceRepositoryPath = String.format("%s/%s", appFilePath, appServiceDTO.getCode());

        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(iamUserId);
        String newToken = appServiceService.getToken(appServiceDTO.getGitlabProjectId(), appFilePath, userAttrDTO);
//        appServiceDTO.setRepoUrl(repoUrl + organizationDTO.getCode()
//                + "-" + applicationDTO.getCode() + "/" + appServiceDTO.getCode() + ".git");
        appServiceDTO.setRepoUrl(repoUrl + "testorg0110-testpro0110" + "/" + appServiceDTO.getCode() + ".git");
        appServiceMarketVO.getAppServiceMarketVersionVOS().forEach(appServiceMarketVersionVO -> {
            AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(appServiceMarketVersionVO.getId());
            //2. 创建目录 应用服务版本
            FileUtil.createDirectory(appServiceRepositoryPath, appServiceVersionDTO.getVersion());
            String appServiceVersionPath = String.format("%s/%s", appServiceRepositoryPath, appServiceVersionDTO.getVersion());

            //3.clone源码,checkout到版本所在commit，并删除.git文件
            gitUtil.cloneAndCheckout(appServiceVersionPath, appServiceDTO.getRepoUrl(), newToken, appServiceVersionDTO.getCommit());
        });
    }

    private void packageChart(AppServiceMarketVO appServiceMarketVO, String appFilePath, String harborUrl) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceMarketVO.getAppServiceId());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getAppId());
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        //1.创建目录 应用服务仓库
        FileUtil.createDirectory(appFilePath, appServiceDTO.getCode());
        String appServiceChartPath = String.format("%s%s%s", appFilePath, File.separator, appServiceDTO.getCode());
        appServiceMarketVO.getAppServiceMarketVersionVOS().forEach(appServiceMarketVersionVO -> {
            //2.下载chart
            AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(appServiceMarketVersionVO.getId());
            chartUtil.downloadChart(appServiceVersionDTO, organizationDTO, projectDTO, appServiceDTO, appServiceChartPath);
            analysisChart(appServiceChartPath, appServiceDTO.getCode(), appServiceVersionDTO, harborUrl);
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

    private void pushImage(AppMarketUploadVO appMarketUploadVO) {
        String osname = System.getProperty("os.name");
        if ("win".contains(osname)) {
            throw new CommonException("error.os.windows");
        }
        String shellPath = this.getClass().getResource("/shell").getPath();
        String scriptStr = String.format("%s/%s", shellPath.substring(1, shellPath.length()), PUSH_IAMGES);
        appMarketUploadVO.getAppServiceMarketVOList().forEach(appServiceMarketVO -> {
            StringBuilder stringBuilder = new StringBuilder();
            appServiceMarketVO.getAppServiceMarketVersionVOS().forEach(t -> {
                AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(t.getId());
                stringBuilder.append(appServiceVersionDTO.getImage());
                stringBuilder.append(System.getProperty("line.separator"));
            });
            ConfigVO configVO = devopsConfigService.queryByResourceId(
                    appServiceService.baseQuery(appServiceMarketVO.getAppServiceId()).getChartConfigId(), "harbor")
                    .get(0).getConfig();
            FileUtil.saveDataToFile(shellPath, IMAGES, stringBuilder.toString());
            callScript(scriptStr, appMarketUploadVO.getHarborUrl(), appMarketUploadVO.getUser(), configVO);
            FileUtil.deleteFile(String.format("%s%s%s", shellPath, File.separator, IMAGES));
        });
    }

    /**
     * 脚本文件具体执行及脚本执行过程探测
     *
     * @param script 脚本文件绝对路径
     */
    private void callScript(String script, String harborUrl, User user, ConfigVO configVO) {
        try {
            String cmd = String.format("sh %s %s %s %s %s %s", script, harborUrl, user.getUsername(), user.getPassword(), configVO.getUserName(), configVO.getPassword());
            //启动独立线程等待process执行完成
            CommandWaitForThread commandThread = new CommandWaitForThread(cmd);
            commandThread.start();
            while (!commandThread.isFinish()) {
                LOGGER.info("push_image.sh还未执行完毕,10s后重新探测");
                Thread.sleep(10000);
            }
            //检查脚本执行结果状态码
            if (commandThread.getExitValue() != 0) {
                throw new CommonException("error.exec.push.image");
            }
            LOGGER.info("push_image.sh执行成功,exitValue = {}", commandThread.getExitValue());
        } catch (Exception e) {
            throw new CommonException("error.exec.push.image");
        }
    }

    private AppServiceMarketVO dtoToMarketVO(AppServiceDTO applicationDTO) {
        AppServiceMarketVO appServiceMarketVO = new AppServiceMarketVO();
        appServiceMarketVO.setAppServiceId(applicationDTO.getId());
        appServiceMarketVO.setAppServiceCode(applicationDTO.getCode());
        appServiceMarketVO.setAppServiceName(applicationDTO.getName());
        return appServiceMarketVO;
    }
}
