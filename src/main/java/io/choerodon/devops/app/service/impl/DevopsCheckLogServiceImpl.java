package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.MiscConstants.DEFAULT_CHART_NAME;

import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import jdk.nashorn.internal.runtime.regexp.joni.encoding.IntHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.enums.JobTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.CiTemplateStageBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateStageJobRelBusMapper;
import io.choerodon.devops.infra.mapper.DevopsCheckLogMapper;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;


@Service
public class DevopsCheckLogServiceImpl implements DevopsCheckLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCheckLogServiceImpl.class);

    public static final String FIX_APP_CENTER_DATA = "fixAppCenterData";
    public static final String FIX_PIPELINE_DATA = "fixPipelineData";
    public static final String FIX_HELM_REPO_DATA = "fixHelmRepoData";
    public static final String FIX_HELM_VERSION_DATA = "fixHelmVersionData";
    public static final String FIX_IMAGE_VERSION_DATA = "fixImageVersionData";
    public static final String MIGRATION_CD_PIPELINE_DATE = "migrationCdPipelineDate";

    private static Map<String, String> jobTypeMapping = new HashMap<>();
    public static final String DELETE_DEVOPS_ENV_RESOURCE_DETAIL_DATA = "deleteDevopsEnvResourceDetailData";

    public static final String HARBOR_CONFIG_ID_CACHE_KEY_TEMPLATE = "%s-%s";

    public static final String FIX_HELM_IMAGE_VERSION_OF_NULL_DATA = "fixHelmImageVersionOfNullData";
    @Autowired
    private DevopsCheckLogMapper devopsCheckLogMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsHelmConfigService devopsHelmConfigService;
    @Autowired
    private DevopsConfigService devopsConfigService;
    @Autowired
    private AppServiceService devopsAppService;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private AppServiceImageVersionService appServiceImageVersionService;
    @Autowired
    private AppServiceHelmVersionService appServiceHelmVersionService;
    @Autowired
    private AppServiceHelmRelService appServiceHelmRelService;

    static {
        jobTypeMapping.put(JobTypeEnum.CD_AUDIT.value(), CiJobTypeEnum.AUDIT.value());
        jobTypeMapping.put(JobTypeEnum.CD_DEPLOY.value(), CiJobTypeEnum.CHART_DEPLOY.value());
        jobTypeMapping.put(JobTypeEnum.CD_DEPLOYMENT.value(), CiJobTypeEnum.DEPLOYMENT_DEPLOY.value());
        jobTypeMapping.put(JobTypeEnum.CD_API_TEST.value(), CiJobTypeEnum.API_TEST.value());
        jobTypeMapping.put(JobTypeEnum.CD_HOST.value(), CiJobTypeEnum.HOST_DEPLOY.value());
    }


    @Autowired
    private HarborService harborService;

    @Autowired
    private DevopsEnvResourceDetailService devopsEnvResourceDetailService;


    @Autowired
    private CiTemplateStageBusMapper ciTemplateStageBusMapper;

    @Autowired
    private CiTemplateStageJobRelBusMapper ciTemplateStageJobRelBusMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkLog(String task) {
        DevopsCheckLogDTO devopsCheckLogDTO = new DevopsCheckLogDTO();
        devopsCheckLogDTO.setLog(task);
        DevopsCheckLogDTO existDevopsCheckLogDTO = devopsCheckLogMapper.selectOne(devopsCheckLogDTO);
        if (existDevopsCheckLogDTO != null) {
            LOGGER.info("fix data task {} has already been executed", task);
            return;
        }
        devopsCheckLogDTO.setBeginCheckDate(new Date());
        switch (task) {
            case FIX_HELM_REPO_DATA:
                fixHelmRepoDate();
                break;
            case FIX_HELM_VERSION_DATA:
                fixHelmVersionData();
                break;
            case FIX_IMAGE_VERSION_DATA:
                fixImageVersionData();
                break;
            case DELETE_DEVOPS_ENV_RESOURCE_DETAIL_DATA:
                deleteDevopsEnvResourceDetailData();
                break;
            case FIX_HELM_IMAGE_VERSION_OF_NULL_DATA:
                fixIHelmImageVersionOfNullData();
            default:
                LOGGER.info("version not matched");
                return;
        }
        devopsCheckLogDTO.setLog(task);
        devopsCheckLogDTO.setEndCheckDate(new Date());
        devopsCheckLogMapper.insert(devopsCheckLogDTO);
    }


    @Override
    public void fixCiTemplateStageJobRelSequence() {
        List<CiTemplateStageDTO> ciTemplateStageDTOS = ciTemplateStageBusMapper.selectAll();
        if (!CollectionUtils.isEmpty(ciTemplateStageDTOS)) {
            ciTemplateStageDTOS.forEach(ciTemplateStageDTO -> {
                List<CiTemplateStageJobRelDTO> relDTOS = ciTemplateStageJobRelBusMapper.listByStageId(ciTemplateStageDTO.getId());
                relDTOS = relDTOS.stream().sorted(Comparator.comparing(CiTemplateStageJobRelDTO::getId)).collect(Collectors.toList());
                int sequence = 0;
                for (CiTemplateStageJobRelDTO relDTO : relDTOS) {
                    relDTO.setSequence(sequence);
                    ciTemplateStageJobRelBusMapper.updateByPrimaryKeySelective(relDTO);
                    sequence++;
                }
            });
        }
    }

    private void fixIHelmImageVersionOfNullData() {
        // 应用服务版本与helm仓库关联关系
        // 数量非常大，需要分页操作
        int count = appServiceVersionService.queryCountVersionsWithHelmConfigNullOrImageConfigNull();
        int pageSize = 500;
        int total = (count + pageSize - 1) / pageSize;
        int pageNumber = 0;
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>end fix app version helm config >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");
        DevopsHelmConfigDTO devopsHelmConfigDTOOnSite = devopsHelmConfigService.queryDefaultDevopsHelmConfigByLevel(ResourceLevel.SITE.value(), 0L);
        do {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>app version helm config {}/{} >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!", pageNumber, total);
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(pageNumber);
            pageRequest.setSize(pageSize);
            List<AppServiceHelmVersionDTO> appServiceHelmVersionDTOToInsert = new ArrayList<>();

            Page<AppServiceVersionDTO> appServiceVersionDTOPage = PageHelper.doPage(pageRequest, () -> appServiceVersionService.listAllVersionsWithHelmConfigNullOrImageConfigNull());
            Map<String, Long> cachedHarborConfigId = new HashMap<>();
            HashSet<String> harborConfigIdNotExistKeys = new HashSet<>();
            appServiceVersionDTOPage.getContent().forEach(v -> {
                AppServiceHelmVersionDTO appServiceHelmVersionDTO = new AppServiceHelmVersionDTO();
                appServiceHelmVersionDTO.setAppServiceVersionId(v.getId());
                appServiceHelmVersionDTO.setHelmConfigId(v.getHelmConfigId() == null ? devopsHelmConfigDTOOnSite.getId() : v.getHelmConfigId());
                appServiceHelmVersionDTO.setHarborRepoType(v.getRepoType());
                if (v.getHarborConfigId() == null) {
                    // 如果版本关联的harbor仓库版本为null，查询默认仓库
                    String harborConfigIdCacheKey = String.format(HARBOR_CONFIG_ID_CACHE_KEY_TEMPLATE, v.getProjectId(), v.getAppServiceId());
                    if (harborConfigIdNotExistKeys.contains(harborConfigIdCacheKey)) {
                        return;
                    }
                    Long harborConfigId = cachedHarborConfigId.get(harborConfigIdCacheKey);
                    if (harborConfigId == null) {
                        try {
                            DevopsConfigDTO harborConfigDTO = harborService.queryRepoConfigToDevopsConfig(v.getProjectId(), v.getAppServiceId(), "pull");
                            harborConfigId = harborConfigDTO.getId();
                            cachedHarborConfigId.put(harborConfigIdCacheKey, harborConfigDTO.getId());
                        } catch (Exception e) {
                            harborConfigIdNotExistKeys.add(harborConfigIdCacheKey);
                            return;
                        }
                    }
                    if (harborConfigId == null) {
                        harborConfigIdNotExistKeys.add(harborConfigIdCacheKey);
                        return;
                    }
                    appServiceHelmVersionDTO.setHarborConfigId(harborConfigId);
                } else {
                    appServiceHelmVersionDTO.setHarborConfigId(v.getHarborConfigId());
                }
                appServiceHelmVersionDTO.setValueId(v.getValueId());
                appServiceHelmVersionDTO.setReadmeValueId(v.getReadmeValueId());
                appServiceHelmVersionDTO.setImage(v.getImage());
                appServiceHelmVersionDTO.setRepository(v.getRepository());
                if (appServiceHelmVersionDTO.getValueId() == null || appServiceHelmVersionDTO.getImage() == null || appServiceHelmVersionDTO.getRepository() == null) {
                    LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> fix app service version failed, version info is {} <<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(v));
                } else {
                    appServiceHelmVersionDTOToInsert.add(appServiceHelmVersionDTO);
                }
            });
            if (!CollectionUtils.isEmpty(appServiceHelmVersionDTOToInsert)) {
                appServiceHelmVersionService.batchInsertInNewTrans(appServiceHelmVersionDTOToInsert);
            }
            pageNumber++;
        } while (pageNumber <= total);
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>end fix app version helm config >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");
    }

    private void deleteDevopsEnvResourceDetailData() {
        // 每次删除4000条
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>Start Delete dirty data for devops_env_resource_detail >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");
        int count = 1;
        boolean processComplete = false;
        do {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>Process Delete dirty data for devops_env_resource_detail, count: {} >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", count);
            int size = devopsEnvResourceDetailService.batchDeleteByIdInNewTrans();
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>devops_env_resource_detail data size {}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!", size);
            if (size < 4000) {
                processComplete = true;
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>Process Delete dirty data for devops_env_resource_detail is empty,end while control.>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            }
            count++;
        } while (!processComplete);
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>End Delete dirty data for devops_env_resource_detail >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");

    }

    private void fixImageVersionData() {
        int count = appServiceVersionService.queryCountVersionsWithHarborConfig();
        int pageSize = 500;
        int total = (count + pageSize - 1) / pageSize;
        int pageNumber = 0;
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>start fix app version image record >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");
        do {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>app version image record {}/{} >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!", pageNumber, total);
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(pageNumber);
            pageRequest.setSize(pageSize);
            List<AppServiceImageVersionDTO> appServiceImageVersionDTOS = new ArrayList<>();

            Page<AppServiceVersionDTO> appServiceVersionDTOPage = PageHelper.doPage(pageRequest, () -> appServiceVersionService.listAllVersionsWithHarborConfig());
            appServiceVersionDTOPage.getContent().forEach(v -> {
                AppServiceImageVersionDTO appServiceImageVersionDTO = new AppServiceImageVersionDTO();
                appServiceImageVersionDTO.setAppServiceVersionId(v.getId());
                appServiceImageVersionDTO.setHarborRepoType(v.getRepoType());
                appServiceImageVersionDTO.setHarborConfigId(v.getHarborConfigId());
                appServiceImageVersionDTO.setImage(v.getImage());
                appServiceImageVersionDTOS.add(appServiceImageVersionDTO);
            });
            if (!CollectionUtils.isEmpty(appServiceImageVersionDTOS)) {
                appServiceImageVersionService.batchInsertInNewTrans(appServiceImageVersionDTOS);
            }
            pageNumber++;
        } while (pageNumber <= total);
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>end fix app version image record >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");

    }

    private void fixHelmVersionData() {
        // 应用服务版本与helm仓库关联关系
        // 数量非常大，需要分页操作
        int count = appServiceVersionService.queryCountVersionsWithHelmConfig();
        int pageSize = 500;
        int total = (count + pageSize - 1) / pageSize;
        int pageNumber = 0;
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>end fix app version helm config >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");
        do {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>app version helm config {}/{} >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!", pageNumber, total);
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(pageNumber);
            pageRequest.setSize(pageSize);
            List<AppServiceHelmVersionDTO> appServiceHelmVersionDTOToInsert = new ArrayList<>();

            Page<AppServiceVersionDTO> appServiceVersionDTOPage = PageHelper.doPage(pageRequest, () -> appServiceVersionService.listAllVersionsWithHelmConfig());
            appServiceVersionDTOPage.getContent().forEach(v -> {
                AppServiceHelmVersionDTO appServiceHelmVersionDTO = new AppServiceHelmVersionDTO();
                appServiceHelmVersionDTO.setAppServiceVersionId(v.getId());
                appServiceHelmVersionDTO.setHelmConfigId(v.getHelmConfigId());
                appServiceHelmVersionDTO.setHarborRepoType(v.getRepoType());
                appServiceHelmVersionDTO.setHarborConfigId(v.getHarborConfigId());
                appServiceHelmVersionDTO.setValueId(v.getValueId());
                appServiceHelmVersionDTO.setReadmeValueId(v.getReadmeValueId());
                appServiceHelmVersionDTO.setImage(v.getImage());
                appServiceHelmVersionDTO.setRepository(v.getRepository());
                if (appServiceHelmVersionDTO.getValueId() == null || appServiceHelmVersionDTO.getReadmeValueId() == null || appServiceHelmVersionDTO.getImage() == null || appServiceHelmVersionDTO.getRepository() == null) {
                    LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> fix app service version failed, version info is {} <<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(appServiceHelmVersionDTO));
                } else {
                    appServiceHelmVersionDTOToInsert.add(appServiceHelmVersionDTO);
                }

            });
            if (!CollectionUtils.isEmpty(appServiceHelmVersionDTOToInsert)) {
                appServiceHelmVersionService.batchInsertInNewTrans(appServiceHelmVersionDTOToInsert);
            }
            pageNumber++;
        } while (pageNumber <= total);
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>end fix app version helm config >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");
    }

    private void fixHelmRepoDate() {
        fixHelmConfig();
    }

    private void fixHelmConfig() {
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>start fix helm config >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");

        Set<Long> projectIds = new HashSet<>();

        List<DevopsConfigDTO> devopsConfigDTOS = devopsConfigService.listAllChart();

        List<DevopsConfigDTO> platformHelmConfig = devopsConfigDTOS.stream().filter(c -> c.getProjectId() == null && c.getAppServiceId() == null && c.getOrganizationId() == null).collect(Collectors.toList());
        List<DevopsConfigDTO> organizationHelmConfig = devopsConfigDTOS.stream().filter(c -> c.getOrganizationId() != null).collect(Collectors.toList());
        List<DevopsConfigDTO> projectHelmConfig = devopsConfigDTOS.stream().filter(c -> c.getProjectId() != null).collect(Collectors.toList());
        projectIds.addAll(projectHelmConfig.stream().map(DevopsConfigDTO::getProjectId).collect(Collectors.toSet()));
        List<DevopsConfigDTO> appHelmConfig = devopsConfigDTOS.stream().filter(c -> c.getAppServiceId() != null).collect(Collectors.toList());
        List<Long> appIds = appHelmConfig.stream().map(DevopsConfigDTO::getAppServiceId).collect(Collectors.toList());

        projectIds.addAll(devopsAppService.listProjectIdsByAppIds(appIds));

        List<AppServiceDTO> appServiceDTOList = devopsAppService.baseListByIds(new HashSet<>(appIds));
        Map<Long, AppServiceDTO> appServiceDTOMap = appServiceDTOList.stream().collect(Collectors.toMap(AppServiceDTO::getId, Function.identity()));

        List<ProjectDTO> projectDTOS = baseServiceClientOperator.queryProjectsByIds(projectIds);
        Map<Long, ProjectDTO> projectDTOMap = projectDTOS.stream().collect(Collectors.toMap(ProjectDTO::getId, Function.identity()));


        Set<Long> organizationIds = projectDTOS.stream().map(ProjectDTO::getOrganizationId).collect(Collectors.toSet());
        List<Tenant> tenants = baseServiceClientOperator.listOrganizationByIds(organizationIds);
        Map<Long, Tenant> tenantMap = tenants.stream().collect(Collectors.toMap(Tenant::getTenantId, Function.identity()));


        List<DevopsHelmConfigDTO> devopsHelmConfigDTOToInsert = new ArrayList<>();
        List<AppServiceHelmRelDTO> appServiceHelmRelDTOToInsert = new ArrayList<>();

        // 平台层
        platformHelmConfig.forEach(c -> {
            DevopsHelmConfigDTO devopsHelmConfigDTO = new DevopsHelmConfigDTO();
            devopsHelmConfigDTO.setId(c.getId());
            devopsHelmConfigDTO.setName(UUID.randomUUID().toString());
            if (DEFAULT_CHART_NAME.equals(c.getName())) {
                devopsHelmConfigDTO.setRepoDefault(true);
            }
            Map<String, String> helmConfig = JsonHelper.unmarshalByJackson(c.getConfig(), new TypeReference<Map<String, String>>() {
            });
            devopsHelmConfigDTO.setUrl(helmConfig.get("url"));
            devopsHelmConfigDTO.setUsername(helmConfig.get("userName"));
            devopsHelmConfigDTO.setPassword(helmConfig.get("password"));
            devopsHelmConfigDTO.setRepoPrivate(Boolean.parseBoolean(helmConfig.get("isPrivate")));
            devopsHelmConfigDTO.setRepoDefault(false);
            devopsHelmConfigDTO.setResourceId(0L);
            devopsHelmConfigDTO.setResourceType(ResourceLevel.SITE.value());
            devopsHelmConfigDTOToInsert.add(devopsHelmConfigDTO);
        });
        // 组织层
        organizationHelmConfig.forEach(c -> {
            DevopsHelmConfigDTO devopsHelmConfigDTO = new DevopsHelmConfigDTO();
            devopsHelmConfigDTO.setId(c.getId());
            devopsHelmConfigDTO.setName(UUID.randomUUID().toString());
            Map<String, String> helmConfig = JsonHelper.unmarshalByJackson(c.getConfig(), new TypeReference<Map<String, String>>() {
            });
            devopsHelmConfigDTO.setUrl(helmConfig.get("url"));
            devopsHelmConfigDTO.setUsername(helmConfig.get("userName"));
            devopsHelmConfigDTO.setPassword(helmConfig.get("password"));
            devopsHelmConfigDTO.setRepoPrivate(Boolean.parseBoolean(helmConfig.get("isPrivate")));
            devopsHelmConfigDTO.setResourceId(c.getOrganizationId());
            devopsHelmConfigDTO.setRepoDefault(false);
            devopsHelmConfigDTO.setResourceType(ResourceLevel.ORGANIZATION.value());
            devopsHelmConfigDTOToInsert.add(devopsHelmConfigDTO);
        });
        // 项目id为key
        Map<Long, IntHolder> indexMap = new HashMap<>();
        // 项目层
        projectHelmConfig.forEach(c -> {
            ProjectDTO projectDTO = projectDTOMap.get(c.getProjectId());
            if (projectDTO == null) {
                LOGGER.info("skip current config.id:{}", c.getId());
                return;
            }
            Tenant tenant = tenantMap.get(projectDTO.getOrganizationId());
            if (tenant == null) {
                LOGGER.info("skip current config.id:{}", c.getId());
                return;
            }

            IntHolder index = indexMap.get(c.getProjectId());
            if (index == null) {
                index = new IntHolder();
                index.value = 1;
                indexMap.put(c.getProjectId(), index);
            }
            DevopsHelmConfigDTO devopsHelmConfigDTO = new DevopsHelmConfigDTO();
            devopsHelmConfigDTO.setId(c.getId());
            devopsHelmConfigDTO.setName(String.format("自定义Helm仓库-%s", index.value));
            Map<String, String> helmConfig = JsonHelper.unmarshalByJackson(c.getConfig(), new TypeReference<Map<String, String>>() {
            });
            URL processedUrl = null;
            try {
                processedUrl = new URL(helmConfig.get("url"));
            } catch (Exception e) {
                LOGGER.warn("current config:{} errorMsg:{}", c.getConfig(), e.getMessage());
            }
            devopsHelmConfigDTO.setUrl(String.format("%s://%s/%s/%s", processedUrl.getProtocol(), processedUrl.getHost(), tenant.getTenantNum(), projectDTO.getCode()));
            devopsHelmConfigDTO.setUsername(helmConfig.get("userName"));
            devopsHelmConfigDTO.setPassword(helmConfig.get("password"));
            devopsHelmConfigDTO.setRepoPrivate(Boolean.parseBoolean(helmConfig.get("isPrivate")));
            devopsHelmConfigDTO.setRepoDefault(false);
            devopsHelmConfigDTO.setResourceId(c.getProjectId());
            devopsHelmConfigDTO.setResourceType(ResourceLevel.PROJECT.value());
            index.value++;
            devopsHelmConfigDTOToInsert.add(devopsHelmConfigDTO);
        });

        // 应用层
        appHelmConfig.forEach(c -> {
            AppServiceDTO appServiceDTO = appServiceDTOMap.get(c.getAppServiceId());
            ProjectDTO projectDTO = projectDTOMap.get(appServiceDTO.getProjectId());
            if (projectDTO == null) {
                LOGGER.info("skip current config.id:{}", c.getId());
                return;
            }
            Tenant tenant = tenantMap.get(projectDTO.getOrganizationId());
            if (tenant == null) {
                LOGGER.info("skip current config.id:{}", c.getId());
                return;
            }

            IntHolder index = indexMap.get(appServiceDTO.getProjectId());
            if (index == null) {
                index = new IntHolder();
                index.value = 1;
                indexMap.put(appServiceDTO.getProjectId(), index);
            }

            DevopsHelmConfigDTO devopsHelmConfigDTO = new DevopsHelmConfigDTO();
            devopsHelmConfigDTO.setId(c.getId());
            devopsHelmConfigDTO.setName(String.format("自定义Helm仓库-%s", index.value));
            Map<String, String> helmConfig = JsonHelper.unmarshalByJackson(c.getConfig(), new TypeReference<Map<String, String>>() {
            });
            URL processedUrl = null;
            try {
                processedUrl = new URL(helmConfig.get("url"));
            } catch (Exception e) {
                LOGGER.warn("current config:{} errorMsg:{}", c.getConfig(), e.getMessage());
            }
            devopsHelmConfigDTO.setUrl(String.format("%s://%s/%s/%s", processedUrl.getProtocol(), processedUrl.getHost(), tenant.getTenantNum(), projectDTO.getCode()));
            devopsHelmConfigDTO.setUsername(helmConfig.get("userName"));
            devopsHelmConfigDTO.setPassword(helmConfig.get("password"));
            devopsHelmConfigDTO.setRepoPrivate(Boolean.parseBoolean(helmConfig.get("isPrivate")));
            devopsHelmConfigDTO.setRepoDefault(false);
            devopsHelmConfigDTO.setResourceId(appServiceDTO.getProjectId());
            devopsHelmConfigDTO.setResourceType(ResourceLevel.PROJECT.value());
            devopsHelmConfigDTOToInsert.add(devopsHelmConfigDTO);

            AppServiceHelmRelDTO appServiceHelmRelDTO = new AppServiceHelmRelDTO();
            appServiceHelmRelDTO.setAppServiceId(appServiceDTO.getId());
            appServiceHelmRelDTO.setHelmConfigId(c.getId());

            index.value++;
            appServiceHelmRelDTOToInsert.add(appServiceHelmRelDTO);
        });

        if (!ObjectUtils.isEmpty(devopsHelmConfigDTOToInsert)) {
            devopsHelmConfigService.batchInsertInNewTrans(devopsHelmConfigDTOToInsert);
        }
        if (!ObjectUtils.isEmpty(appServiceHelmRelDTOToInsert)) {
            appServiceHelmRelService.batchInsertInNewTrans(appServiceHelmRelDTOToInsert);
        }

        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>end fix helm config >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");
    }

}
