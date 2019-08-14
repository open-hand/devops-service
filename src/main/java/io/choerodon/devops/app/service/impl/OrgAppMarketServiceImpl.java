package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.AppMarketUploadVO;
import io.choerodon.devops.api.vo.AppServiceMarketVO;
import io.choerodon.devops.api.vo.AppServiceMarketVersionVO;
import io.choerodon.devops.app.service.AppServiceVersionService;
import io.choerodon.devops.app.service.IamService;
import io.choerodon.devops.app.service.OrgAppMarketService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.PublishTypeEnum;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.util.*;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:28 2019/8/8
 * Description:
 */
@Component
public class OrgAppMarketServiceImpl implements OrgAppMarketService {
    private static final String APPLICATION = "application";
    private static final String APPLICATION_SERVICE = "application-service";

    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private IamService iamService;
    @Autowired
    private ChartUtil chartUtil;
    @Value("${services.helm.url}")
    private String helmUrl;

    @Override
    public PageInfo<AppServiceMarketVO> pageByAppId(Long appId,
                                                    PageRequest pageRequest, String params) {
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
    public void upload(AppMarketUploadVO marketUploadVO) {
        //1.创建根目录 应用
        String appFilePath = APPLICATION + System.currentTimeMillis();
        FileUtil.createDirectory(appFilePath);
        if (marketUploadVO.getStatus().equals(PublishTypeEnum.DOWNLOAD_ONLY.value())) {
            //2.clone 并压缩源代码
            marketUploadVO.getAppServiceMarketVOList().forEach(appServiceMarketVO -> packageSourceCode(appServiceMarketVO, appFilePath));
        } else if (marketUploadVO.getStatus().equals(PublishTypeEnum.DEPLOY_ONLY.value())) {

        }
    }

    @Override
    public List<AppServiceMarketVO> listAllAppServices(){
        List<AppServiceDTO> appServiceDTOList = appServiceMapper.selectAll();
        return ConvertUtils.convertList(appServiceDTOList, this::dtoToMarkVO);
    }

    @Override
    public List<AppServiceMarketVersionVO> listServiceVersionsByAppServiceId(Long appServiceId) {
        List<AppServiceVersionDTO> appServiceVersionDTOList = appServiceVersionService.baseListByAppServiceId(appServiceId);
        return ConvertUtils.convertList(appServiceVersionDTOList, AppServiceMarketVersionVO.class);
    }

    private void packageSourceCode(AppServiceMarketVO appServiceMarketVO, String appFilePath) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceMarketVO.getAppServiceId());
        //1.创建目录 应用服务仓库
        String appServiceFileName = String.format("%s-%s", APPLICATION_SERVICE, appServiceDTO.getCode());
        FileUtil.createDirectory(appFilePath, appServiceFileName);
        FileUtil.createDirectory(String.format("%s/%s", appFilePath, appServiceFileName), "repository");
        String appServiceRepositoryPath = String.format("%s/%s/%s", appFilePath, appServiceFileName, "repository");

        appServiceMarketVO.getAppServiceMarketVersionVOS().forEach(appServiceMarketVersionVO -> {
            AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(appServiceMarketVersionVO.getId());
            //2. 创建目录 应用服务版本
            FileUtil.createDirectory(appServiceRepositoryPath, appServiceVersionDTO.getVersion());
            String appServiceVersionPath = String.format("%s/%s", appServiceRepositoryPath, appServiceVersionDTO.getVersion());

            //3.clone源码,checkout到版本所在commit，并删除.git文件
            gitUtil.cloneAndCheckout(appServiceVersionPath, appServiceDTO.getGitlabProjectUrl(), appServiceDTO.getToken(), appServiceVersionDTO.getCommit());
        });
        //4.压缩文件
        toZip(appFilePath, appServiceRepositoryPath);
    }

    private void packageChart(AppServiceMarketVO appServiceMarketVO, String appFilePath) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceMarketVO.getAppServiceId());
        ProjectDTO projectDTO = iamService.queryIamProject(appServiceDTO.getAppId());
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());

        //1.创建目录 应用服务仓库
        String appServiceFileName = String.format("%s-%s", APPLICATION_SERVICE, appServiceDTO.getCode());
        FileUtil.createDirectory(appFilePath, appServiceFileName);
        FileUtil.createDirectory(String.format("%s/%s", appFilePath, appServiceFileName), "chart");
        String appServiceChartPath = String.format("%s/%s/%s", appFilePath, appServiceFileName, "chart");
        appServiceMarketVO.getAppServiceMarketVersionVOS().forEach(appServiceMarketVersionVO -> {
            //2.下载chart
            AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(appServiceMarketVersionVO.getId());
            chartUtil.downloadChart(appServiceVersionDTO, organizationDTO, projectDTO, appServiceDTO, appServiceChartPath);
        });

    }

    private void analysisChart(String zipPath, AppServiceVersionDTO appServiceVersionDTO, String harborUrl) {
        String unZipPath = String.format("%s/%s", zipPath, "unzip");
        FileUtil.unTarGZ(unZipPath, unZipPath);
        File zipDirectory = new File(unZipPath);
        //解析 解压过后的文件
        if (zipDirectory.exists() && zipDirectory.isDirectory()) {
            File[] listFiles = zipDirectory.listFiles();
            //获取替换Repository
            List<File> appMarkets = Arrays.stream(listFiles).parallel()
                    .filter(k -> k.getName().equals("values.yaml"))
                    .collect(Collectors.toCollection(ArrayList::new));
            if (!appMarkets.isEmpty() && appMarkets.size() == 1) {
                File valuesFile = appMarkets.get(0);
                Map<String, String> params = new HashMap<>();
                params.put(appServiceVersionDTO.getRepository(), String.format("%s/%s", harborUrl, appServiceVersionDTO.getVersion()));
                FileUtil.fileToInputStream(valuesFile, params);
            }
        } else {
            FileUtil.deleteDirectory(zipDirectory);
            throw new CommonException("error.chart.empty");
        }
        // 打包
        String newZipPath = String.format("new-charts/%s", appServiceVersionDTO.getVersion());
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(newZipPath));
            FileUtil.toZip(unZipPath, fileOutputStream, true);
        } catch (FileNotFoundException e) {
            throw new CommonException("error.upload.chart");
        } finally {
            FileUtil.deleteDirectory(new File(zipPath));
            FileUtil.deleteDirectory(new File(unZipPath));
        }


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

    private AppServiceMarketVO dtoToMarketVO(AppServiceDTO applicationDTO) {
        AppServiceMarketVO appServiceMarketVO = new AppServiceMarketVO();
        appServiceMarketVO.setAppServiceId(applicationDTO.getId());
        appServiceMarketVO.setAppServiceCode(applicationDTO.getCode());
        appServiceMarketVO.setAppServiceName(applicationDTO.getName());
        return appServiceMarketVO;
    }

    private AppServiceMarketVO dtoToMarkVO(AppServiceDTO applicationDTO){
        AppServiceMarketVO appServiceMarketVO = new AppServiceMarketVO();
        appServiceMarketVO.setAppServiceId(applicationDTO.getId());
        appServiceMarketVO.setAppServiceCode(applicationDTO.getCode());
        appServiceMarketVO.setAppServiceName(applicationDTO.getName());
        return appServiceMarketVO;
    }

}
