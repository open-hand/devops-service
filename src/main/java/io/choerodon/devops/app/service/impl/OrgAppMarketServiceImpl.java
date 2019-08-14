package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.harbor.User;
import io.choerodon.devops.infra.dto.iam.ApplicationDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.PublishTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
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
    private static final String APPLICATION_SERVICE = "application-service";
    private static final String IMAGES = "images";
    private static final String PUSH_IAMGES = "push_image.sh";

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
        toZip(String.format("%s-%s", appFilePath, "source-code.zip"), appFilePath);

    }

    @Override
    public void createHarborRepository(HarborMarketVO harborMarketVO) {
        harborService.createHarborForAppMarket(harborMarketVO);
    }

    @Override
    public List<AppServiceMarketVersionVO> listServiceVersionsByAppServiceId(Long appServiceId) {
        List<AppServiceVersionDTO> appServiceVersionDTOList = appServiceVersionService.baseListByAppServiceId(appServiceId);
        return ConvertUtils.convertList(appServiceVersionDTOList, AppServiceMarketVersionVO.class);
    }

    private void packageSourceCode(AppServiceMarketVO appServiceMarketVO, String appFilePath, Long iamUserId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceMarketVO.getAppServiceId());
        ApplicationDTO applicationDTO = baseServiceClientOperator.queryAppById(appServiceDTO.getAppId());
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(applicationDTO.getOrganizationId());

        //1.创建目录 应用服务仓库
        String appServiceFileName = String.format("%s-%s", APPLICATION_SERVICE, appServiceDTO.getCode());
        FileUtil.createDirectory(appFilePath, appServiceFileName);
        String appServiceRepositoryPath = String.format("%s/%s", appFilePath, appServiceFileName);

        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(iamUserId);
        String newToken = appServiceService.getToken(appServiceDTO.getGitlabProjectId(), appFilePath, userAttrDTO);
        appServiceDTO.setRepoUrl(repoUrl + organizationDTO.getCode()
                + "-" + applicationDTO.getCode() + "/" + appServiceDTO.getCode() + ".git");
//        appServiceDTO.setRepoUrl(repoUrl + "testorg0110-testpro0110" + "/" + appServiceDTO.getCode() + ".git");
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
        String appServiceFileName = String.format("%s-%s", APPLICATION_SERVICE, appServiceDTO.getCode());
        FileUtil.createDirectory(appFilePath, appServiceFileName);
        String appServiceChartPath = String.format("%s%s%s", appFilePath, File.separator, appServiceFileName);
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
            FileUtil.deleteDirectory(zipDirectory);
            throw new CommonException("error.chart.empty");
        }
        // 打包
        String chartFilePath = String.format("%s%s%s-%s.tgz", zipPath, File.separator, appServiceCode, appServiceVersionDTO.getVersion());
        FileUtil.deleteFile(new File(chartFilePath));
        toZip(chartFilePath, unZipPath);
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
