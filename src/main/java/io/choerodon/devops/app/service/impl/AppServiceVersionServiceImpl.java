package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.CUSTOM_REPO;
import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.DEFAULT_REPO;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.appversion.AppServiceHelmVersionVO;
import io.choerodon.devops.api.vo.appversion.AppServiceImageVersionVO;
import io.choerodon.devops.api.vo.appversion.AppServiceMavenVersionVO;
import io.choerodon.devops.api.vo.chart.ChartTagVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.harbor.HarborImageTagDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.ProjectConfigType;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;

@Service
public class AppServiceVersionServiceImpl implements AppServiceVersionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppServiceVersionServiceImpl.class);

    /**
     * 存储chart包的文件路径模板
     * devops-应用服务id-版本号-commit值前8位
     */
    private static final String DESTINATION_PATH_TEMPLATE = "devops-%s-%s-%s";
    /**
     * 解压chart包的文件路径模板
     * stores-应用服务id-版本号-commit值前8位
     */
    private static final String STORE_PATH_TEMPLATE = "stores-%s-%s-%s";

    private static final String APP_SERVICE = "appService";
    private static final String CHART = "chart";
    private static final String HARBOR_DEFAULT = "harbor_default";
    private static final String ERROR_VERSION_INSERT = "error.version.insert";
    private static final String ERROR_VERSION_UPDATE="error.version.update";

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Autowired
    private AppServiceService applicationService;
    @Autowired
    private ChartUtil chartUtil;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private AppServiceVersionValueService appServiceVersionValueService;
    @Autowired
    private DevopsGitlabCommitService devopsGitlabCommitService;
    @Autowired
    private AppServiceVersionMapper appServiceVersionMapper;
    @Autowired
    private AppServiceVersionReadmeMapper appServiceVersionReadmeMapper;
    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private DevopsConfigService devopsConfigService;
    @Autowired
    private SendNotificationService sendNotificationService;
    @Autowired
    private DevopsCiPipelineChartService devopsCiPipelineChartService;
    @Autowired
    private DevopsConfigMapper devopsConfigMapper;
    @Autowired
    private DevopsRegistrySecretMapper devopsRegistrySecretMapper;
    @Autowired
    private AppServiceShareRuleMapper appServiceShareRuleMapper;
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper;
    @Autowired
    private CiPipelineImageService ciPipelineImageService;
    @Autowired
    private AppServiceHelmVersionService appServiceHelmVersionService;
    @Autowired
    private AppServiceImageVersionService appServiceImageVersionService;
    @Autowired
    private AppServiceMavenVersionService appServiceMavenVersionService;
    @Autowired
    private CiPipelineMavenService ciPipelineMavenService;
    @Autowired
    private CiPipelineAppVersionService ciPipelineAppVersionService;
    @Autowired
    private DevopsHelmConfigService devopsHelmConfigService;

    @Autowired
    @Qualifier(value = "restTemplateForIp")
    private RestTemplate restTemplate;
    @Autowired
    private TransactionalProducer producer;

    private static final Gson GSON = new Gson();

    /**
     * 方法中抛出{@link DevopsCiInvalidException}而不是{@link CommonException}是为了返回非200的状态码。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void create(String image,
                       String harborConfigId,
                       String repoType,
                       String token,
                       String version,
                       String commit,
                       MultipartFile files,
                       String ref,
                       Long gitlabPipelineId,
                       String jobName) {
        try {

            AppServiceDTO appServiceDTO = appServiceMapper.queryByToken(token);

            AppServiceVersionDTO appServiceVersionDTO = saveAppVersion(version, commit, ref, gitlabPipelineId, appServiceDTO.getId());

            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
            Tenant organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

            // 查询helm仓库配置id
            DevopsHelmConfigDTO devopsHelmConfigDTO = devopsHelmConfigService.queryAppConfig(appServiceDTO.getId(), projectDTO.getId(), organization.getTenantId());

            String repository;
            if (ResourceLevel.PROJECT.value().equals(devopsHelmConfigDTO.getResourceType())) {
                repository = devopsHelmConfigDTO.getUrl();
            } else {
                repository = devopsHelmConfigDTO.getUrl().endsWith("/") ? devopsHelmConfigDTO.getUrl() + organization.getTenantNum() + "/" + projectDTO.getDevopsComponentCode() + "/" : devopsHelmConfigDTO.getUrl() + "/" + organization.getTenantNum() + "/" + projectDTO.getDevopsComponentCode() + "/";
            }
            // 取commit的一部分作为文件路径
            String commitPart = commit == null ? "" : commit.substring(0, 8);
            String storeFilePath = String.format(STORE_PATH_TEMPLATE, appServiceDTO.getId(), version, commitPart);
            String destFilePath = String.format(DESTINATION_PATH_TEMPLATE, appServiceDTO.getId(), version, commitPart);
            String path = FileUtil.multipartFileToFile(storeFilePath, files);

            uploadChart(files, devopsHelmConfigDTO, repository);

            // 解析chart包中的values文件
            String values = getValues(storeFilePath, destFilePath, path);

            AppServiceHelmVersionDTO appServiceHelmVersionDTO = appServiceHelmVersionService.queryByAppServiceVersionId(appServiceVersionDTO.getId());
            if (appServiceHelmVersionDTO == null) {
                AppServiceVersionValueDTO appServiceVersionValueDTO = new AppServiceVersionValueDTO();
                appServiceVersionValueDTO.setValue(values);
                appServiceVersionValueService.baseCreate(appServiceVersionValueDTO);

                AppServiceVersionReadmeDTO appServiceVersionReadmeDTO = new AppServiceVersionReadmeDTO();
                appServiceVersionReadmeDTO.setReadme(FileUtil.getReadme(destFilePath));
                appServiceVersionReadmeMapper.insert(appServiceVersionReadmeDTO);

                appServiceHelmVersionDTO = new AppServiceHelmVersionDTO();
                appServiceHelmVersionDTO.setAppServiceVersionId(appServiceVersionDTO.getId());
                appServiceHelmVersionDTO.setValueId(appServiceVersionValueDTO.getId());
                appServiceHelmVersionDTO.setReadmeValueId(appServiceVersionReadmeDTO.getId());
                appServiceHelmVersionDTO.setHarborRepoType(repoType);
                appServiceHelmVersionDTO.setHarborConfigId(TypeUtil.objToLong(harborConfigId));
                appServiceHelmVersionDTO.setHelmConfigId(devopsHelmConfigDTO.getId());
                appServiceHelmVersionDTO.setRepository(repository);

                appServiceHelmVersionService.create(appServiceHelmVersionDTO);
            } else {
                updateValues(appServiceHelmVersionDTO.getValueId(), values);
            }

            FileUtil.deleteDirectories(destFilePath, storeFilePath);
            //生成版本成功后发送webhook json
            sendNotificationService.sendWhenAppServiceVersion(appServiceVersionDTO, appServiceDTO, projectDTO);

            // 保存流水线chart版本信息
            if (gitlabPipelineId != null && StringUtils.isNotBlank(jobName)) {
                Long appServiceId = appServiceVersionDTO.getAppServiceId();
                DevopsCiPipelineChartDTO devopsCiPipelineChartDTO = devopsCiPipelineChartService.queryByPipelineIdAndJobName(appServiceId,
                        gitlabPipelineId,
                        jobName);
                if (devopsCiPipelineChartDTO == null) {
                    devopsCiPipelineChartService.baseCreate(new DevopsCiPipelineChartDTO(appServiceId,
                            gitlabPipelineId,
                            jobName,
                            appServiceVersionDTO.getVersion(),
                            appServiceVersionDTO.getId()));
                }
            }
        } catch (Exception e) {
            if (e instanceof CommonException) {
                throw new DevopsCiInvalidException(((CommonException) e).getCode(), e, ((CommonException) e).getParameters());
            }
            throw new DevopsCiInvalidException(e);
        }

    }

    private void uploadChart(MultipartFile files, DevopsHelmConfigDTO devopsHelmConfigDTO, String repository) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        ByteArrayResource fileAsResource = null;
        try {
            byte[] bytes = files.getBytes();

            fileAsResource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return files.getOriginalFilename();
                }
                @Override
                public long contentLength() {
                    return files.getSize();
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
        params.add("chart", fileAsResource);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if (devopsHelmConfigDTO.getRepoPrivate()) {
            String credentials = devopsHelmConfigDTO.getUsername() + ":"
                    + devopsHelmConfigDTO.getPassword();
            headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes()));
        }
        HttpEntity<MultiValueMap<String,Object>> requestEntity  = new HttpEntity<>(params, headers);

        ResponseEntity<String> entity = null;
        try {
            entity = restTemplate.postForEntity(repository + "/api/charts", requestEntity, String.class);
            if (!entity.getStatusCode().is2xxSuccessful() && !HttpStatus.CONFLICT.equals(entity.getStatusCode())) {
                throw new CommonException("error.upload.chart");
            }
        } catch (HttpClientErrorException e) {
            if (!HttpStatus.CONFLICT.equals(e.getStatusCode())) {
                throw new CommonException("error.upload.chart", e);
            }
        } catch (RestClientException e) {
            throw new CommonException(e);
        }
    }

    private String getValues(String storeFilePath, String destFilePath, String path) {
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

        try {
            FileUtil.checkYamlFormat(values);
        } catch (CommonException e) {
            FileUtil.deleteDirectories(storeFilePath, destFilePath);
            throw new CommonException("The format of the values.yaml in the chart is invalid!", e);
        }
        return values;
    }

//    private AppServiceVersionDTO doCreate(String image,
//                                          Long harborConfigId,
//                                          String repoType,
//                                          String token,
//                                          String version,
//                                          String commit,
//                                          MultipartFile files,
//                                          String ref) {
//        AppServiceDTO appServiceDTO = appServiceMapper.queryByToken(token);
//
//        AppServiceVersionValueDTO appServiceVersionValueDTO = new AppServiceVersionValueDTO();
//        AppServiceVersionDTO newVersion = new AppServiceVersionDTO();
//        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
//        Tenant organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
//        AppServiceVersionDTO oldVersionInDb = baseQueryByAppServiceIdAndVersion(appServiceDTO.getId(), version);
//        newVersion.setAppServiceId(appServiceDTO.getId());
//        newVersion.setImage(image);
//        newVersion.setCommit(commit);
//        newVersion.setRef(ref);
//        newVersion.setVersion(version);
//        //根据配置id 查询仓库是自定义还是默认
////        HarborRepoDTO harborRepoDTO = rdupmClient.queryHarborRepoConfig(appServiceDTO.getProjectId(), appServiceDTO.getId()).getBody();
////        if (Objects.isNull(harborRepoDTO)
////                || Objects.isNull(harborRepoDTO.getHarborRepoConfig())
////                || harborRepoDTO.getHarborRepoConfig().getRepoId().longValue() != harborConfigId) {
////            throw new DevopsCiInvalidException("error.harbor.configuration.expiration");
////        }
//        newVersion.setHarborConfigId(harborConfigId);
//        newVersion.setRepoType(repoType);
//
//        // 查询helm仓库配置id
//        DevopsConfigDTO devopsConfigDTO = devopsConfigService.queryRealConfig(appServiceDTO.getId(), APP_SERVICE, CHART, AUTH_TYPE_PULL);
//        ConfigVO helmConfig = GSON.fromJson(devopsConfigDTO.getConfig(), ConfigVO.class);
//        String helmUrl = helmConfig.getUrl();
//        newVersion.setHelmConfigId(devopsConfigDTO.getId());
//
//        newVersion.setRepository(helmUrl.endsWith("/") ? helmUrl + organization.getTenantNum() + "/" + projectDTO.getDevopsComponentCode() + "/" : helmUrl + "/" + organization.getTenantNum() + "/" + projectDTO.getDevopsComponentCode() + "/");
//
//        // 取commit的一部分作为文件路径
//        String commitPart = commit == null ? "" : commit.substring(0, 8);
//
//        String storeFilePath = String.format(STORE_PATH_TEMPLATE, appServiceDTO.getId(), version, commitPart);
//        String destFilePath = String.format(DESTINATION_PATH_TEMPLATE, appServiceDTO.getId(), version, commitPart);
//
//        String path = FileUtil.multipartFileToFile(storeFilePath, files);
//
//        // 上传chart包到 chart museum
//        chartUtil.uploadChart(helmUrl, organization.getTenantNum(), projectDTO.getDevopsComponentCode(), new File(path), helmConfig.getUserName(), helmConfig.getPassword());
//
//        FileUtil.unTarGZ(path, destFilePath);
//
//        // 使用深度优先遍历查找文件, 避免查询到子chart的values值
//        File valuesFile = FileUtil.queryFileFromFilesBFS(new File(destFilePath), "values.yaml");
//
//        if (valuesFile == null) {
//            FileUtil.deleteDirectories(storeFilePath, destFilePath);
//            throw new CommonException("error.find.values.yaml.in.chart");
//        }
//
//        String values;
//        try (FileInputStream fis = new FileInputStream(valuesFile)) {
//            values = FileUtil.replaceReturnString(fis, null);
//        } catch (IOException e) {
//            FileUtil.deleteDirectories(storeFilePath, destFilePath);
//            throw new CommonException(e);
//        }
//
//        try {
//            FileUtil.checkYamlFormat(values);
//        } catch (CommonException e) {
//            FileUtil.deleteDirectories(storeFilePath, destFilePath);
//            throw new CommonException("The format of the values.yaml in the chart is invalid!", e);
//        }
//
//        // 更新版本纪录和values纪录
//        if (oldVersionInDb != null) {
//            // 重新上传chart包后更新values
//            updateValues(oldVersionInDb.getValueId(), values);
//            updateVersion(oldVersionInDb, newVersion);
//        } else {
//            // 新建版本时的操作
//            appServiceVersionValueDTO.setValue(values);
//            try {
//                newVersion.setValueId(appServiceVersionValueService
//                        .baseCreate(appServiceVersionValueDTO).getId());
//            } catch (Exception e) {
//                FileUtil.deleteDirectories(storeFilePath, destFilePath);
//                throw new CommonException(ERROR_VERSION_INSERT, e);
//            }
//
//            AppServiceVersionReadmeDTO appServiceVersionReadmeDTO = new AppServiceVersionReadmeDTO();
//            appServiceVersionReadmeDTO.setReadme(FileUtil.getReadme(destFilePath));
//            appServiceVersionReadmeMapper.insert(appServiceVersionReadmeDTO);
//
//            newVersion.setReadmeValueId(appServiceVersionReadmeDTO.getId());
//            newVersion = baseCreate(newVersion);
//        }
//
//
//        FileUtil.deleteDirectories(destFilePath, storeFilePath);
//        //生成版本成功后发送webhook json
//        sendNotificationService.sendWhenAppServiceVersion(newVersion, appServiceDTO, projectDTO);
//        return newVersion;
//    }

    private void updateVersion(AppServiceVersionDTO oldVersionInDb, AppServiceVersionDTO newVersion) {
        newVersion.setId(oldVersionInDb.getId());
        newVersion.setLastUpdateDate(new Date());
        newVersion.setObjectVersionNumber(oldVersionInDb.getObjectVersionNumber());
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(appServiceVersionMapper, newVersion, ERROR_VERSION_UPDATE);
    }

    private void updateValues(Long oldValuesId, String values) {
        AppServiceVersionValueDTO appServiceVersionValueDTO = new AppServiceVersionValueDTO();
        appServiceVersionValueDTO.setId(oldValuesId);

        AppServiceVersionValueDTO old = appServiceVersionValueService.baseQuery(oldValuesId);

        if (old == null) {
            return;
        }

        // values变了才更新
        if (!Objects.equals(old.getValue(), values)) {
            appServiceVersionValueDTO.setValue(values);
            appServiceVersionValueService.baseUpdate(appServiceVersionValueDTO);
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
    public Page<AppServiceVersionVO> pageByOptions(Long projectId, Long appServiceId, Long appServiceVersionId, Boolean deployOnly, Boolean doPage, String params, PageRequest pageable, String version) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        Page<AppServiceVersionDTO> applicationVersionDTOPageInfo;
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        Boolean share = !(appServiceDTO.getProjectId() == null || appServiceDTO.getProjectId().equals(projectId));
        if (doPage != null && doPage) {
            applicationVersionDTOPageInfo = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> appServiceVersionMapper.listByAppServiceIdAndVersion(appServiceId,
                    appServiceVersionId,
                    projectId,
                    share,
                    deployOnly,
                    TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)),
                    PageRequestUtil.checkSortIsEmpty(pageable), version));
        } else {
            applicationVersionDTOPageInfo = new Page<>();
            List<AppServiceVersionDTO> appServiceVersionDTOS = appServiceVersionMapper.listByAppServiceIdAndVersion(appServiceId, appServiceVersionId, projectId, share, deployOnly,
                    TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)),
                    PageRequestUtil.checkSortIsEmpty(pageable), version);
            if (doPage == null) {
                applicationVersionDTOPageInfo.setContent(appServiceVersionDTOS.stream().limit(20).collect(Collectors.toList()));
            } else {
                applicationVersionDTOPageInfo.setContent(appServiceVersionDTOS);
            }
        }
        Page<AppServiceVersionVO> appServiceVersionVOS = ConvertUtils.convertPage(applicationVersionDTOPageInfo, AppServiceVersionVO.class);

        if (!CollectionUtils.isEmpty(appServiceVersionVOS.getContent())) {
            // 计算应用服务版本是否可以被删除
            caculateDelteFlag(appServiceId, appServiceVersionVOS.getContent());
            // 添加版本关联的helm、image、jar版本信息
            addVersionInfo(appServiceVersionVOS.getContent());
        }

        return appServiceVersionVOS;
    }

    /**
     * 添加版本关联的helm、image、jar版本信息
     * @param appServiceVersionVOList
     */
    private void addVersionInfo(List<AppServiceVersionVO> appServiceVersionVOList) {
        Set<Long> versionIds = appServiceVersionVOList.stream().map(AppServiceVersionVO::getId).collect(Collectors.toSet());

        // 批量查询各版本信息
        Map<Long, AppServiceHelmVersionVO> helmVersionMap = new HashMap<>();
        List<AppServiceHelmVersionVO> appServiceHelmVersionVOS = appServiceHelmVersionService.listByAppVersionIds(versionIds);
        if (!CollectionUtils.isEmpty(appServiceHelmVersionVOS)) {
            helmVersionMap = appServiceHelmVersionVOS.stream().collect(Collectors.toMap(AppServiceHelmVersionVO::getAppServiceVersionId, Function.identity()));
        }
        Map<Long, AppServiceImageVersionVO> imageVersionMap = new HashMap<>();
        List<AppServiceImageVersionVO> appServiceImageVersionVOS = appServiceImageVersionService.listByAppVersionIds(versionIds);
        if (!CollectionUtils.isEmpty(appServiceImageVersionVOS)) {
            imageVersionMap = appServiceImageVersionVOS.stream().collect(Collectors.toMap(AppServiceImageVersionVO::getAppServiceVersionId, Function.identity()));
        }

        Map<Long, AppServiceMavenVersionVO> mavenVersionMap = new HashMap<>();
        List<AppServiceMavenVersionVO> appServiceMavenVersionVOS = appServiceMavenVersionService.listByAppVersionIds(versionIds);
        if (!CollectionUtils.isEmpty(appServiceMavenVersionVOS)) {
            mavenVersionMap = appServiceMavenVersionVOS.stream().collect(Collectors.toMap(AppServiceMavenVersionVO::getAppServiceVersionId, Function.identity()));
        }

        // 填充版本信息
        Map<Long, AppServiceHelmVersionVO> finalHelmVersionMap = helmVersionMap;
        Map<Long, AppServiceImageVersionVO> finalImageVersionMap = imageVersionMap;
        Map<Long, AppServiceMavenVersionVO> finalMavenVersionMap = mavenVersionMap;
        appServiceVersionVOList.forEach(appServiceVersionVO -> {
            Long appServiceVersionId = appServiceVersionVO.getId();

            AppServiceHelmVersionVO appServiceHelmVersionVO = finalHelmVersionMap.get(appServiceVersionId);
            AppServiceImageVersionVO appServiceImageVersionVO = finalImageVersionMap.get(appServiceVersionId);
            AppServiceMavenVersionVO appServiceMavenVersionVO = finalMavenVersionMap.get(appServiceVersionId);

            appServiceVersionVO.setAppServiceHelmVersionVO(appServiceHelmVersionVO);
            appServiceVersionVO.setAppServiceImageVersionVO(appServiceImageVersionVO);
            appServiceVersionVO.setAppServiceMavenVersionVO(appServiceMavenVersionVO);
        });


    }

    /**
     * 计算应用服务版本是否可以被删除
     * 1. 有实例的版本不能删除
     * 2. 有共享规则的版本不能删除
     * @param appServiceId
     * @param content
     */
    private void caculateDelteFlag(Long appServiceId, List<AppServiceVersionVO> content) {
        List<AppServiceVersionDTO> appServiceVersionDTOS = appServiceInstanceMapper.queryVersionByAppId(appServiceId);
        List<AppServiceVersionDTO> effectAppServiceVersionDTOS = appServiceInstanceMapper.queryEffectVersionByAppId(appServiceId);
        Map<Long, AppServiceVersionDTO> map = new HashMap<>();
        content.forEach(v -> {

            if (!CollectionUtils.isEmpty(appServiceVersionDTOS)) {
                appServiceVersionDTOS.forEach(appServiceVersionDTO -> {
                    if (map.get(appServiceVersionDTO.getId()) == null) {
                        map.put(appServiceVersionDTO.getId(), appServiceVersionDTO);
                    }
                });
            }

            if (!CollectionUtils.isEmpty(effectAppServiceVersionDTOS)) {
                effectAppServiceVersionDTOS.forEach(appServiceVersionDTO -> {
                    if (map.get(appServiceVersionDTO.getId()) == null) {
                        map.put(appServiceVersionDTO.getId(), appServiceVersionDTO);
                    }
                });
            }
            AppServiceVersionDTO appServiceVersionDTO = map.get(v.getId());
            if (appServiceVersionDTO != null) {
                v.setDeleteFlag(false);
                return;
            }
            // 是否存在共享规则
            AppServiceShareRuleDTO record = new AppServiceShareRuleDTO();
            record.setAppServiceId(appServiceId);
            record.setVersion(v.getVersion());
            List<AppServiceShareRuleDTO> appServiceShareRuleDTOS = appServiceShareRuleMapper.select(record);
            if (!CollectionUtils.isEmpty(appServiceShareRuleDTOS)) {
                v.setDeleteFlag(false);
            }
        });
    }

    @Override
    public List<AppServiceVersionRespVO> listUpgradeableAppVersion(Long projectId, Long appServiceServiceId) {
        baseCheckByProjectAndVersionId(projectId, appServiceServiceId);
        return ConvertUtils.convertList(
                baseListUpgradeVersion(appServiceServiceId), AppServiceVersionRespVO.class);
    }

    @Override
    public String queryVersionValue(Long appServiceServiceId) {
        AppServiceVersionDTO appServiceVersionDTO = baseQuery(appServiceServiceId);
        AppServiceHelmVersionDTO appServiceHelmVersionDTO = appServiceHelmVersionService.queryByAppServiceVersionId(appServiceVersionDTO.getId());
        return appServiceVersionValueService.baseQuery(appServiceHelmVersionDTO.getValueId()).getValue();
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
        Tenant organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
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
                    + organization.getTenantNum() + "-" + projectDTO.getDevopsComponentCode() + "/"
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
                throw new CommonException(ERROR_VERSION_UPDATE);
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
    public Page<AppServiceVersionRespVO> pageShareVersionByAppId(Long appServiceId, PageRequest pageable, String params) {
        Map<String, Object> paramMap = TypeUtil.castMapParams(params);
        Page<AppServiceVersionDTO> applicationDTOPageInfo = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> appServiceVersionMapper.listShareVersionByAppId(appServiceId, TypeUtil.cast(paramMap.get(TypeUtil.PARAMS))));
        return ConvertUtils.convertPage(applicationDTOPageInfo, AppServiceVersionRespVO.class);
    }

    @Override
    public Page<AppServiceVersionRespVO> pageShareVersionByAppServiceIdAndVersion(Long appServiceId, PageRequest pageable, String version) {
        return ConvertUtils.convertPage(PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> appServiceVersionMapper.pageShareVersionByAppServiceIdAndVersion(appServiceId, version)), AppServiceVersionRespVO.class);
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
        return appServiceVersionMapper.queryValue(Objects.requireNonNull(versionId));
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
    public void baseUpdate(AppServiceVersionDTO appServiceVersionDTO) {
        if (appServiceVersionMapper.updateByPrimaryKey(appServiceVersionDTO) != 1) {
            throw new CommonException(ERROR_VERSION_UPDATE);
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
    public List<AppServiceVersionDTO> baseQueryByCommitSha(Long appServiceId, String ref, String sha) {
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
                .getContent().stream().map(AppServiceVO::getId).collect(Collectors.toList());
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
        if (!CollectionUtils.isEmpty(appServiceVersionDTOS)) {
            Set<Long> valueIds = new HashSet<>();
            Set<Long> readmeIds = new HashSet<>();
            Set<Long> configIds = new HashSet<>();
            Set<Long> versionIds = new HashSet<>();
            appServiceVersionDTOS.forEach(appServiceVersionDTO -> {
                versionIds.add(appServiceVersionDTO.getId());
                if (appServiceVersionDTO.getValueId() != null) {
                    valueIds.add(appServiceVersionDTO.getValueId());
                }
                if (appServiceVersionDTO.getReadmeValueId() != null) {
                    readmeIds.add(appServiceVersionDTO.getReadmeValueId());
                }

                if (appServiceVersionDTO.getHarborConfigId() != null) {
                    configIds.add(appServiceVersionDTO.getHarborConfigId());
                }
                if (appServiceVersionDTO.getHelmConfigId() != null) {
                    configIds.add(appServiceVersionDTO.getHelmConfigId());
                }

            });
            if (!CollectionUtils.isEmpty(valueIds)) {
                appServiceVersionValueService.deleteByIds(valueIds);
            }
            if (!CollectionUtils.isEmpty(readmeIds)) {
                appServiceVersionReadmeMapper.deleteByIds(readmeIds);
            }
            if (!CollectionUtils.isEmpty(configIds)) {
                devopsConfigService.deleteByConfigIds(configIds);
            }
            appServiceVersionMapper.deleteByIds(versionIds);
        }
    }

    private Long queryDefaultHarborId() {
        DevopsConfigDTO devopsConfigDTO = new DevopsConfigDTO();
        devopsConfigDTO.setName(MiscConstants.DEFAULT_HARBOR_NAME);
        return devopsConfigMapper.selectOne(devopsConfigDTO).getId();
    }

    @Override
    public void fixHarbor() {
        //修复appVsersion表，register_secret表
        LOGGER.info("start fix appVsersion table");
        //根据appServiceID 进行分组

        Long defaultHarborConfigId = queryDefaultHarborId();

        LOGGER.info("Default harbor config id is {}", defaultHarborConfigId);

        List<Long> longList = appServiceVersionMapper.selectAllAppServiceIdWithNullHarborConfig();
        LOGGER.info("Start to fix null harbor config id versions. the app-service id size is {}", longList.size());
        for (Long appServiceId : longList) {
            handlerVersion(appServiceId);
        }
        LOGGER.info("End to fix null harbor config id versions");

        // 修harbor config id 非null的
        LOGGER.info("Start to fix default harbor config id versions");
        appServiceVersionMapper.updateDefaultHarborRecords(defaultHarborConfigId);
        LOGGER.info("Finish to fix default harbor config id versions");

        LOGGER.info("Start to fix non default harbor config id versions");
        appServiceVersionMapper.updateCustomHarborRecords(defaultHarborConfigId);
        LOGGER.info("Finish to fix non default harbor config id versions");

        LOGGER.info("end fix appVsersion table");
        LOGGER.info("start fix register_secret");
        int count = devopsRegistrySecretMapper.selectCount(null);
        int pageSize = 100;
        int total = (count + pageSize - 1) / pageSize;
        int pageNumber = 0;
        do {
            PageRequest pageable = new PageRequest();
            pageable.setPage(pageNumber);
            pageable.setSize(pageSize);
            pageable.setSort(new Sort("id"));
            Page<DevopsRegistrySecretDTO> doPageAndSort = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable),
                    () -> devopsRegistrySecretMapper.selectAll());
            if (!CollectionUtils.isEmpty(doPageAndSort.getContent())) {
                for (DevopsRegistrySecretDTO devopsRegistrySecretDTO : doPageAndSort) {
                    DevopsConfigDTO devopsConfigDTO = devopsConfigMapper.selectByPrimaryKey(devopsRegistrySecretDTO.getConfigId());
                    if (!Objects.isNull(devopsConfigDTO) && HARBOR_DEFAULT.equals(devopsConfigDTO.getName())) {
                        devopsRegistrySecretDTO.setConfigId(null);
                        devopsRegistrySecretDTO.setRepoType(DEFAULT_REPO);
                        devopsRegistrySecretMapper.updateByPrimaryKey(devopsRegistrySecretDTO);
                    } else {
                        devopsRegistrySecretDTO.setRepoType(CUSTOM_REPO);
                        devopsRegistrySecretMapper.updateByPrimaryKey(devopsRegistrySecretDTO);
                    }
                }
            }
            pageNumber++;
        } while (pageNumber <= total);

        LOGGER.info("end fix register_secret");
    }

    @Override
    @Transactional
    @Saga(code = SagaTopicCodeConstants.DEVOPS_DELETE_APPLICATION_SERVICE_VERSION, inputSchemaClass = CustomResourceVO.class, description = "批量删除应用服务版本")
    public void batchDelete(Long projectId, Long appServiceId, Set<Long> versionIds) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        // 校验版本是否能够删除
        checkVersion(appServiceId, versionIds);

        CommonExAssertUtil.assertTrue(projectId.equals(appServiceDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        Tenant tenant = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        List<HarborImageTagDTO> deleteImagetags = new ArrayList<>();
        List<ChartTagVO> deleteChartTags = new ArrayList<>();
        versionIds.forEach(id -> {
            // 查询应用服务版本
            AppServiceVersionDTO appServiceVersionDTO = appServiceVersionMapper.selectByPrimaryKey(id);
            AppServiceHelmVersionDTO appServiceHelmVersionDTO = appServiceHelmVersionService.queryByAppServiceVersionId(appServiceVersionDTO.getId());
            // 删除value
            appServiceVersionValueService.baseDeleteById(appServiceHelmVersionDTO.getValueId());
            // 删除readme
            appServiceVersionReadmeMapper.deleteByPrimaryKey(appServiceHelmVersionDTO.getReadmeValueId());

            // 计算删除harbor镜像列表
            if (DEFAULT_REPO.equals(appServiceHelmVersionDTO.getHarborRepoType())) {
                HarborImageTagDTO harborImageTagDTO = caculateHarborImageTagDTO(appServiceDTO.getProjectId(), appServiceHelmVersionDTO.getImage());
                deleteImagetags.add(harborImageTagDTO);
            }
            // 计算删除chart列表
            ChartTagVO chartTagVO = caculateChartTag(tenant.getTenantNum(), projectDTO.getDevopsComponentCode(), appServiceDTO.getCode(), appServiceVersionDTO);
            deleteChartTags.add(chartTagVO);

            // 删除应用服务版本
            appServiceVersionMapper.deleteByPrimaryKey(appServiceVersionDTO.getId());
        });
        CustomResourceVO customResourceVO = new CustomResourceVO();
        customResourceVO.setHarborImageTagDTOS(deleteImagetags);
        customResourceVO.setChartTagVOS(deleteChartTags);


        // 发送saga
        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(appServiceDTO.getId())
                        .withRefType("app")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_DELETE_APPLICATION_SERVICE_VERSION),
                builder -> builder
                        .withJson(GSON.toJson(customResourceVO))
                        .withRefId(appServiceDTO.getId().toString()));
    }

    @Override
    public AppServiceVersionDTO queryByCommitShaAndRef(Long appServiceId, String commitSha, String ref) {

        return appServiceVersionMapper.queryByCommitShaAndRef(appServiceId, commitSha, ref);
    }

    @Override
    public AppServiceVersionWithHelmConfigVO queryVersionWithHelmConfig(Long projectId, Long appServiceVersionId) {
        AppServiceVersionWithHelmConfigVO appServiceVersionWithHelmConfigVO = io.choerodon.core.utils.ConvertUtils.convertObject(appServiceVersionMapper.selectByPrimaryKey(appServiceVersionId), AppServiceVersionWithHelmConfigVO.class);
        AppServiceHelmVersionDTO appServiceHelmVersionDTO = appServiceHelmVersionService.queryByAppServiceVersionId(appServiceVersionId);
        if (appServiceHelmVersionDTO != null) {
            Long helmConfigId = appServiceHelmVersionDTO.getHelmConfigId();
            if (helmConfigId == null) {
                throw new FeignException("error.helm.config.id.null");
            }
            DevopsHelmConfigDTO devopsHelmConfigDTO = devopsHelmConfigService.queryById(helmConfigId);
            if (devopsHelmConfigDTO == null) {
                throw new FeignException("error.helm.config.not.exist");
            }
            appServiceVersionWithHelmConfigVO.setHelmConfig(new ConfigVO(devopsHelmConfigDTO.getUrl(),
                    devopsHelmConfigDTO.getName(),
                    devopsHelmConfigDTO.getPassword(),
                    devopsHelmConfigDTO.getRepoPrivate()));
        }
        return appServiceVersionWithHelmConfigVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppServiceVersionDTO publishAppVersion(String token, String version, String commit, String ref, Long gitlabPipelineId, String jobName) {
        try {
            // 1. 创建应用服务版本
            AppServiceDTO appServiceDTO = appServiceMapper.queryByToken(token);
            Long appServiceId = appServiceDTO.getId();
            AppServiceVersionDTO appServiceVersionDTO = saveAppVersion(version, commit, ref, gitlabPipelineId, appServiceId);

            // 2. 保存流水线任务记录信息
            if (gitlabPipelineId != null && StringUtils.isNotBlank(jobName)) {
                CiPipelineAppVersionDTO ciPipelineAppVersionDTO = ciPipelineAppVersionService.queryByPipelineIdAndJobName(appServiceId,
                        gitlabPipelineId,
                        jobName);
                if (ciPipelineAppVersionDTO == null) {
                    ciPipelineAppVersionService.baseCreate(new CiPipelineAppVersionDTO(appServiceId,
                            gitlabPipelineId,
                            jobName,
                            appServiceVersionDTO.getId()));
                }
            }
            return appServiceVersionDTO;
        } catch (Exception e) {
            if (e instanceof CommonException) {
                throw new DevopsCiInvalidException(((CommonException) e).getCode(), e, ((CommonException) e).getParameters());
            }
            throw new DevopsCiInvalidException(e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public AppServiceVersionDTO saveAppVersion(String version, String commit, String ref, Long gitlabPipelineId, Long appServiceId) {
        AppServiceVersionDTO appServiceVersionDTO = baseQueryByAppServiceIdAndVersion(appServiceId, version);
        // 不存在才创建
        if (appServiceVersionDTO == null) {
            appServiceVersionDTO = create(appServiceId, version, commit, ref);
        }
        Long appServiceVersionId = appServiceVersionDTO.getId();
        // 2. 创建helm版本

        // 3. 创建image版本
        // 3.1 查询流水线中最新的镜像版本
        AppServiceImageVersionDTO appServiceImageVersionDTO = appServiceImageVersionService.queryByAppServiceVersionId(appServiceVersionId);
        if (appServiceImageVersionDTO == null) {
            CiPipelineImageDTO ciPipelineImageDTO = ciPipelineImageService.queryPipelineLatestImage(appServiceId, gitlabPipelineId);
            if (ciPipelineImageDTO != null) {
                appServiceImageVersionDTO = new AppServiceImageVersionDTO();
                appServiceImageVersionDTO.setAppServiceVersionId(appServiceVersionId);
                appServiceImageVersionDTO.setImage(ciPipelineImageDTO.getImageTag());
                appServiceImageVersionDTO.setHarborRepoType(ciPipelineImageDTO.getRepoType());
                appServiceImageVersionDTO.setHarborConfigId(ciPipelineImageDTO.getHarborRepoId());
                appServiceImageVersionService.create(appServiceImageVersionDTO);
            }
        }

        // 4. 创建jar版本
        AppServiceMavenVersionDTO appServiceMavenVersionDTO = appServiceMavenVersionService.queryByAppServiceVersionId(appServiceVersionId);
        if (appServiceMavenVersionDTO == null) {
            CiPipelineMavenDTO ciPipelineMavenDTO = ciPipelineMavenService.queryPipelineLatestImage(appServiceId, gitlabPipelineId);
            if (ciPipelineMavenDTO != null) {
                appServiceMavenVersionDTO = new AppServiceMavenVersionDTO();
                appServiceMavenVersionDTO.setAppServiceVersionId(appServiceVersionId);
                appServiceMavenVersionDTO.setGroupId(ciPipelineMavenDTO.getGroupId());
                appServiceMavenVersionDTO.setArtifactId(ciPipelineMavenDTO.getArtifactId());
                appServiceMavenVersionDTO.setVersion(ciPipelineMavenDTO.getVersion());
                appServiceMavenVersionDTO.setMavenRepoUrl(ciPipelineMavenDTO.getMavenRepoUrl());
                appServiceMavenVersionDTO.setUsername(ciPipelineMavenDTO.getUsername());
                appServiceMavenVersionDTO.setPassword(ciPipelineMavenDTO.getPassword());
                appServiceMavenVersionDTO.setNexusRepoId(ciPipelineMavenDTO.getNexusRepoId());
                appServiceMavenVersionService.create(appServiceMavenVersionDTO);
            }
        }
        return appServiceVersionDTO;
    }

    @Override
    @Transactional
    public AppServiceVersionDTO create(Long appServiceId, String version, String commit, String ref) {
        AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO();
        appServiceVersionDTO.setAppServiceId(appServiceId);
        appServiceVersionDTO.setVersion(version);
        appServiceVersionDTO.setCommit(commit);
        appServiceVersionDTO.setRef(ref);
        return MapperUtil.resultJudgedInsertSelective(appServiceVersionMapper, appServiceVersionDTO, "error.save.version");
    }

    private Set<AppServiceVersionDTO> checkVersion(Long appServiceId, Set<Long> versionIds) {
        Set<AppServiceVersionDTO> deleteErrorVersion = new HashSet<>();
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
        appServiceInstanceDTO.setAppServiceId(appServiceId);
        List<AppServiceVersionDTO> appServiceVersionDTOS = appServiceInstanceMapper.queryVersionByAppId(appServiceId);
        List<AppServiceVersionDTO> effectAppServiceVersionDTOS = appServiceInstanceMapper.queryEffectVersionByAppId(appServiceId);
        Map<Long, AppServiceVersionDTO> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(appServiceVersionDTOS)) {
            appServiceVersionDTOS.forEach(appServiceVersionDTO -> {
                if (map.get(appServiceVersionDTO.getId()) == null) {
                    map.put(appServiceVersionDTO.getId(), appServiceVersionDTO);
                }
            });
        }

        if (!CollectionUtils.isEmpty(effectAppServiceVersionDTOS)) {
            effectAppServiceVersionDTOS.forEach(appServiceVersionDTO -> {
                if (map.get(appServiceVersionDTO.getId()) == null) {
                    map.put(appServiceVersionDTO.getId(), appServiceVersionDTO);
                }
            });
        }

        versionIds.forEach(v -> {
            AppServiceVersionDTO appServiceVersionDTO = map.get(v);
            if (appServiceVersionDTO != null) {
                throw new CommonException("error.delete.version.invalid.status");
            }
            // 是否存在共享规则
            AppServiceVersionDTO versionDTO = appServiceVersionMapper.selectByPrimaryKey(v);
            AppServiceShareRuleDTO record = new AppServiceShareRuleDTO();
            record.setAppServiceId(appServiceId);
            record.setVersion(versionDTO.getVersion());
            List<AppServiceShareRuleDTO> appServiceShareRuleDTOS = appServiceShareRuleMapper.select(record);
            if (!CollectionUtils.isEmpty(appServiceShareRuleDTOS)) {
                throw new CommonException("error.delete.version.invalid.status");
            }

        });

        return deleteErrorVersion;
    }


    private ChartTagVO caculateChartTag(String tenantNum, String projectCode, String chartName, AppServiceVersionDTO appServiceVersionDTO) {
        ChartTagVO chartTagVO = new ChartTagVO();
        chartTagVO.setOrgCode(tenantNum);
        chartTagVO.setProjectCode(projectCode);
        chartTagVO.setChartName(chartName);
        chartTagVO.setChartVersion(appServiceVersionDTO.getVersion());
        chartTagVO.setRepository(appServiceVersionDTO.getRepository());
        chartTagVO.setAppServiceId(appServiceVersionDTO.getAppServiceId());
        return chartTagVO;
    }

    private HarborImageTagDTO caculateHarborImageTagDTO(Long projectId, String image) {
        HarborImageTagDTO harborImageTagDTO = new HarborImageTagDTO();
        // 镜像格式
        // dockerhub.hand-china.com/emabc-emabc-edm/emabc-edm:2020.3.20-155740-dev
        // -        域名或ip       /  项目code     / app code : image tag
        int startFlag = image.indexOf("/");
        int endFlag = image.lastIndexOf(":");

        String repoName = image.substring(startFlag + 1, endFlag);
        String tagName = image.substring(endFlag + 1);

        harborImageTagDTO.setRepoName(repoName);
        harborImageTagDTO.setTagName(tagName);
        harborImageTagDTO.setProjectId(projectId);
        return harborImageTagDTO;
    }

    @Nullable
    private DevopsConfigDTO queryConfigByAppServiceId(Long appServiceId) {
        DevopsConfigDTO configDTO = new DevopsConfigDTO();
        configDTO.setAppServiceId(appServiceId);
        return devopsConfigMapper.selectOne(configDTO);
    }

    @Nullable
    private DevopsConfigDTO queryConfigByProjectId(Long projectId) {
        DevopsConfigDTO configDTO = new DevopsConfigDTO();
        configDTO.setProjectId(projectId);
        return devopsConfigMapper.selectOne(configDTO);
    }

    @Nullable
    private DevopsConfigDTO queryConfigByOrgId(Long orgId) {
        DevopsConfigDTO configDTO = new DevopsConfigDTO();
        configDTO.setOrganizationId(orgId);
        return devopsConfigMapper.selectOne(configDTO);
    }

    private void handlerVersion(Long appServiceId) {
        LOGGER.info("fix app service id is {} data", appServiceId);
        DevopsConfigDTO devopsConfigDTO = queryConfigByAppServiceId(appServiceId);
        if (!Objects.isNull(devopsConfigDTO)) {
            //自定义仓库 ，配置和appService一样
            LOGGER.info("Custom config {} found for app-service with id {} in app service", devopsConfigDTO.getId(), appServiceId);
            appServiceVersionMapper.updateNullHarborVersionToCustomType(appServiceId, devopsConfigDTO.getId());
        } else {
            // 找项目的
            AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
            if (!Objects.isNull(appServiceDTO)) {
                devopsConfigDTO = queryConfigByProjectId(appServiceDTO.getProjectId());
                if (!Objects.isNull(devopsConfigDTO)) {
                    //自定义仓库 ，配置和project一样
                    LOGGER.info("Custom config {} found for app-service with id {} in project with id {}", devopsConfigDTO.getId(), appServiceId, appServiceDTO.getProjectId());
                    appServiceVersionMapper.updateNullHarborVersionToCustomType(appServiceId, devopsConfigDTO.getId());
                } else {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
                    if (!Objects.isNull(projectDTO)) {
                        devopsConfigDTO = queryConfigByOrgId(projectDTO.getOrganizationId());
                        if (!Objects.isNull(devopsConfigDTO)) {
                            //自定义仓库 ，配置和Org一样
                            LOGGER.info("Custom config {} found for app-service with id {} in organization with id {}", devopsConfigDTO.getId(), appServiceId, projectDTO.getOrganizationId());
                            appServiceVersionMapper.updateNullHarborVersionToCustomType(appServiceId, devopsConfigDTO.getId());
                        } else {
                            //默认仓库
                            LOGGER.info("No custom config Found for app-service with id {}, set to default", appServiceId);
                            appServiceVersionMapper.updateNullHarborVersionToDefaultType(appServiceId);
                        }
                    }
                }
            }

        }
    }
}
