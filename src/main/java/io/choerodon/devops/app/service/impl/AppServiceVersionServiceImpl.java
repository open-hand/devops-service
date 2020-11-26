package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.chart.ChartTagVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.harbor.HarborImageTagDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.ProjectConfigType;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.feign.RdupmClient;
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
    private ChartUtil chartUtil;
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
    private RdupmClient rdupmClient;
    @Autowired
    private DevopsConfigMapper devopsConfigMapper;
    @Autowired
    private DevopsRegistrySecretMapper devopsRegistrySecretMapper;
    @Autowired
    private AppServiceShareRuleMapper appServiceShareRuleMapper;
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper;
    @Autowired
    private PipelineAppDeployService pipelineAppDeployService;
    @Autowired
    private PipelineTaskService pipelineTaskService;
    @Autowired
    private PipelineStageService pipelineStageService;
    @Autowired
    private PipelineService pipelineService;

    @Autowired
    private TransactionalProducer producer;

    private static final Gson GSON = new Gson();

    /**
     * 方法中抛出{@link DevopsCiInvalidException}而不是{@link CommonException}是为了返回非200的状态码。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void create(String image, String harborConfigId, String repoType, String token, String version, String commit, MultipartFile files, String ref) {
        try {
            doCreate(image, TypeUtil.objToLong(harborConfigId), repoType, token, version, commit, files, ref);
        } catch (Exception e) {
            if (e instanceof CommonException) {
                throw new DevopsCiInvalidException(((CommonException) e).getCode(), e, ((CommonException) e).getParameters());
            }
            throw new DevopsCiInvalidException(e);
        }

    }

    private void doCreate(String image, Long harborConfigId, String repoType, String token, String version, String commit, MultipartFile files, String ref) {
        AppServiceDTO appServiceDTO = appServiceMapper.queryByToken(token);

        AppServiceVersionValueDTO appServiceVersionValueDTO = new AppServiceVersionValueDTO();
        AppServiceVersionDTO newVersion = new AppServiceVersionDTO();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
        Tenant organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        AppServiceVersionDTO oldVersionInDb = baseQueryByAppServiceIdAndVersion(appServiceDTO.getId(), version);
        newVersion.setAppServiceId(appServiceDTO.getId());
        newVersion.setImage(image);
        newVersion.setCommit(commit);
        newVersion.setRef(ref);
        newVersion.setVersion(version);
        //根据配置id 查询仓库是自定义还是默认
        HarborRepoDTO harborRepoDTO = rdupmClient.queryHarborRepoConfig(appServiceDTO.getProjectId(), appServiceDTO.getId()).getBody();
        if (Objects.isNull(harborRepoDTO)
                || Objects.isNull(harborRepoDTO.getHarborRepoConfig())
                || harborRepoDTO.getHarborRepoConfig().getRepoId().longValue() != harborConfigId) {
            throw new DevopsCiInvalidException("error.harbor.configuration.expiration");
        }
        newVersion.setHarborConfigId(harborConfigId);
        newVersion.setRepoType(repoType);

        // 查询helm仓库配置id
        DevopsConfigDTO devopsConfigDTO = devopsConfigService.queryRealConfig(appServiceDTO.getId(), APP_SERVICE, CHART, AUTH_TYPE_PULL);
        ConfigVO helmConfig = GSON.fromJson(devopsConfigDTO.getConfig(), ConfigVO.class);
        String helmUrl = helmConfig.getUrl();
        newVersion.setHelmConfigId(devopsConfigDTO.getId());

        newVersion.setRepository(helmUrl.endsWith("/") ? helmUrl + organization.getTenantNum() + "/" + projectDTO.getCode() + "/" : helmUrl + "/" + organization.getTenantNum() + "/" + projectDTO.getCode() + "/");

        // 取commit的一部分作为文件路径
        String commitPart = commit == null ? "" : commit.substring(0, 8);

        String storeFilePath = String.format(STORE_PATH_TEMPLATE, appServiceDTO.getId(), version, commitPart);
        String destFilePath = String.format(DESTINATION_PATH_TEMPLATE, appServiceDTO.getId(), version, commitPart);

        String path = FileUtil.multipartFileToFile(storeFilePath, files);

        // 上传chart包到 chart museum
        chartUtil.uploadChart(helmUrl, organization.getTenantNum(), projectDTO.getCode(), new File(path), helmConfig.getUserName(), helmConfig.getPassword());

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

        // 更新版本纪录和values纪录
        if (oldVersionInDb != null) {
            // 重新上传chart包后更新values
            updateValues(oldVersionInDb.getValueId(), values, oldVersionInDb);
            updateVersion(oldVersionInDb, newVersion);
        } else {
            // 新建版本时的操作
            appServiceVersionValueDTO.setValue(values);
            try {
                newVersion.setValueId(appServiceVersionValueService
                        .baseCreate(appServiceVersionValueDTO).getId());
            } catch (Exception e) {
                FileUtil.deleteDirectories(storeFilePath, destFilePath);
                throw new CommonException(ERROR_VERSION_INSERT, e);
            }

            AppServiceVersionReadmeDTO appServiceVersionReadmeDTO = new AppServiceVersionReadmeDTO();
            appServiceVersionReadmeDTO.setReadme(FileUtil.getReadme(destFilePath));
            appServiceVersionReadmeMapper.insert(appServiceVersionReadmeDTO);

            newVersion.setReadmeValueId(appServiceVersionReadmeDTO.getId());
            baseCreate(newVersion);
            // 触发CD流水线, 鉴于0.24版本时将移除这个入口(要移除CD流水线)，所以0.23版本的修改不支持更新版本时触发CD流水线
            checkAutoDeploy(newVersion);
        }


        FileUtil.deleteDirectories(destFilePath, storeFilePath);
        //生成版本成功后发送webhook json
        sendNotificationService.sendWhenAppServiceVersion(newVersion, appServiceDTO, projectDTO);
    }

    private void updateVersion(AppServiceVersionDTO oldVersionInDb, AppServiceVersionDTO newVersion) {
        // 这些字段有变化才需要更新版本的值
        if (!Objects.equals(oldVersionInDb.getCommit(), newVersion.getCommit())
                || !Objects.equals(oldVersionInDb.getRepoType(), newVersion.getRepoType())
                || !Objects.equals(oldVersionInDb.getHelmConfigId(), newVersion.getHelmConfigId())
                || !Objects.equals(oldVersionInDb.getHarborConfigId(), newVersion.getHarborConfigId())
                || !Objects.equals(oldVersionInDb.getImage(), newVersion.getImage())
                || !Objects.equals(oldVersionInDb.getRef(), newVersion.getRef())
                || !Objects.equals(oldVersionInDb.getRepository(), newVersion.getRepository())) {
            newVersion.setId(oldVersionInDb.getId());
            newVersion.setObjectVersionNumber(oldVersionInDb.getObjectVersionNumber());
            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(appServiceVersionMapper, newVersion, "error.version.update");
        }
    }

    private void updateValues(Long oldValuesId, String values, AppServiceVersionDTO applicationVersion) {
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
            applicationVersion.setLastUpdateDate(new Date());
            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(appServiceVersionMapper, applicationVersion, "error.version.update");
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
            caculateDelteFlag(appServiceId, appServiceVersionVOS.getContent());
        }

        return appServiceVersionVOS;
    }

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
                    + organization.getTenantNum() + "-" + projectDTO.getCode() + "/"
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
    public Page<AppServiceVersionRespVO> pageShareVersionByAppId(Long appServiceId, PageRequest pageable, String params) {
        Map<String, Object> paramMap = TypeUtil.castMapParams(params);
        Page<AppServiceVersionDTO> applicationDTOPageInfo = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> appServiceVersionMapper.listShareVersionByAppId(appServiceId, TypeUtil.cast(paramMap.get(TypeUtil.PARAMS))));
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
            // 删除value
            appServiceVersionValueService.baseDeleteById(appServiceVersionDTO.getValueId());
            // 删除readme
            appServiceVersionReadmeMapper.deleteByPrimaryKey(appServiceVersionDTO.getReadmeValueId());

            // 计算删除harbor镜像列表
            if (DEFAULT_REPO.equals(appServiceVersionDTO.getRepoType())) {
                HarborImageTagDTO harborImageTagDTO = caculateHarborImageTagDTO(appServiceDTO.getProjectId(), appServiceVersionDTO.getImage());
                deleteImagetags.add(harborImageTagDTO);
            }
            // 计算删除chart列表
            ChartTagVO chartTagVO = caculateChartTag(tenant.getTenantNum(), projectDTO.getCode(), appServiceDTO.getCode(), appServiceVersionDTO);
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
