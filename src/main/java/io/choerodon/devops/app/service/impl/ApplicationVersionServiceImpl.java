package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.choerodon.base.domain.Sort;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.ApplicationLatestVersionDTO;
import io.choerodon.devops.infra.dto.ApplicationVersionDTO;
import io.choerodon.devops.infra.dto.ApplicationVersionReadmeDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDO;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.mapper.ApplicationVersionMapper;
import io.choerodon.devops.infra.mapper.ApplicationVersionReadmeMapper;
import io.choerodon.devops.infra.util.*;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.ApplicationVersionService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.PipelineService;
import io.choerodon.devops.api.vo.iam.entity.ApplicationE;
import io.choerodon.devops.api.vo.iam.entity.ApplicationInstanceE;
import io.choerodon.devops.api.vo.iam.entity.ApplicationVersionE;
import io.choerodon.devops.api.vo.iam.entity.ApplicationVersionValueE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.api.vo.iam.entity.DevopsGitlabCommitE;
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectConfigE;
import io.choerodon.devops.api.vo.iam.entity.PipelineAppDeployE;
import io.choerodon.devops.api.vo.iam.entity.PipelineE;
import io.choerodon.devops.api.vo.iam.entity.PipelineTaskE;
import io.choerodon.devops.api.vo.iam.entity.UserAttrE;
import io.choerodon.devops.api.vo.iam.entity.iam.UserE;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.DevopsGitlabCommitRepository;
import io.choerodon.devops.domain.application.repository.DevopsProjectConfigRepository;
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository;
import io.choerodon.devops.domain.application.repository.PipelineAppDeployRepository;
import io.choerodon.devops.domain.application.repository.PipelineRepository;
import io.choerodon.devops.domain.application.repository.PipelineStageRepository;
import io.choerodon.devops.domain.application.repository.PipelineTaskRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;

/**
 * Created by Zenger on 2018/4/3.
 */
@Service
public class ApplicationVersionServiceImpl implements ApplicationVersionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineServiceImpl.class);

    private static final String DESTPATH = "devops";
    private static final String STOREPATH = "stores";
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Autowired
    private ApplicationVersionRepository applicationVersionRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private ApplicationVersionValueRepository applicationVersionValueRepository;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private DevopsGitlabCommitRepository devopsGitlabCommitRepository;
    @Autowired
    private SagaClient sagaClient;
    @Autowired
    private DevopsProjectConfigRepository devopsProjectConfigRepository;
    @Autowired
    private PipelineAppDeployRepository appDeployRepository;
    @Autowired
    private PipelineTaskRepository taskRepository;
    @Autowired
    private PipelineStageRepository stageRepository;
    @Autowired
    private PipelineRepository pipelineRepository;
    @Autowired
    private ChartUtil chartUtil;
    @Autowired
    private PipelineService pipelineService;
    @Autowired
    private DevopsProjectRepository devopsProjectRepository;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private ApplicationVersionMapper applicationVersionMapper;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private ApplicationVersionReadmeMapper applicationVersionReadmeMapper;

    @Value("${services.helm.url}")
    private String helmUrl;

    private Gson gson = new Gson();
    private JSON json = new JSON();

    /**
     * 方法中抛出runtime Exception而不是CommonException是为了返回非200的状态码。
     */
    @Override
    public void create(String image, String token, String version, String commit, MultipartFile files) {
        try {
            doCreate(image, token, version, commit, files);
        } catch (Exception e) {
            if (e instanceof CommonException) {
                throw new DevopsCiInvalidException(((CommonException) e).getCode(), e.getCause());
            }
            throw new DevopsCiInvalidException(e.getMessage(), e);
        }
    }

    private void doCreate(String image, String token, String version, String commit, MultipartFile files) {
        ApplicationE applicationE = applicationRepository.queryByToken(token);

        ApplicationVersionValueE applicationVersionValueE = new ApplicationVersionValueE();
        ApplicationVersionE applicationVersionE = new ApplicationVersionE();
        ProjectVO projectE = iamRepository.queryIamProject(applicationE.getProjectE().getId());
        OrganizationVO organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        ApplicationVersionE newApplicationVersionE = applicationVersionRepository
                .baseQueryByAppIdAndVersion(applicationE.getId(), version);
        applicationVersionE.initApplicationEById(applicationE.getId());
        applicationVersionE.setImage(image);
        applicationVersionE.setCommit(commit);
        applicationVersionE.setVersion(version);
        if (applicationE.getChartConfigE() != null) {
            DevopsProjectConfigE devopsProjectConfigE = devopsProjectConfigRepository.queryByPrimaryKey(applicationE.getChartConfigE().getId());
            helmUrl = devopsProjectConfigE.getConfig().getUrl();
        }

        applicationVersionE.setRepository(helmUrl.endsWith("/") ? helmUrl : helmUrl + "/" + organization.getCode() + "/" + projectE.getCode() + "/");
        String storeFilePath = STOREPATH + version;

        String destFilePath = DESTPATH + version;
        String path = FileUtil.multipartFileToFile(storeFilePath, files);
        //上传chart包到chartmusume
        chartUtil.uploadChart(organization.getCode(), projectE.getCode(), new File(path));

        if (newApplicationVersionE != null) {
            return;
        }
        FileUtil.unTarGZ(path, destFilePath);
        String values;
        try (FileInputStream fis = new FileInputStream(new File(Objects.requireNonNull(FileUtil.queryFileFromFiles(
                new File(destFilePath), "values.yaml")).getAbsolutePath()))) {
            values = FileUtil.replaceReturnString(fis, null);
        } catch (IOException e) {
            throw new CommonException(e);
        }

        try {
            FileUtil.checkYamlFormat(values);
        } catch (CommonException e) {
            throw new CommonException("The format of the values.yaml in the chart is invalid!", e);
        }
        applicationVersionValueE.setValue(values);
        try {
            applicationVersionE.initApplicationVersionValueE(applicationVersionValueRepository
                    .baseCreate(applicationVersionValueE).getId());
        } catch (Exception e) {
            throw new CommonException("error.version.insert", e);
        }
        applicationVersionE.initApplicationVersionReadmeV(FileUtil.getReadme(destFilePath));
        applicationVersionRepository.baseCreate(applicationVersionE);
        FileUtil.deleteDirectory(new File(destFilePath));
        FileUtil.deleteDirectory(new File(storeFilePath));
        //流水线
        checkAutoDeploy(applicationVersionE);
    }

    /**
     * 检测能够触发自动部署
     *
     * @param versionE
     */
    public void checkAutoDeploy(ApplicationVersionE versionE) {
        ApplicationVersionE insertApplicationVersionE = applicationVersionRepository.baseQueryByAppIdAndVersion(versionE.getApplicationE().getId(), versionE.getVersion());

        if (insertApplicationVersionE != null && insertApplicationVersionE.getVersion() != null) {
            List<PipelineAppDeployE> appDeployEList = appDeployRepository.queryByAppId(insertApplicationVersionE.getApplicationE().getId())
                    .stream().map(deployE ->
                            filterAppDeploy(deployE, insertApplicationVersionE.getVersion())
                    ).collect(Collectors.toList());
            appDeployEList.removeAll(Collections.singleton(null));
            if (!appDeployEList.isEmpty()) {
                List<Long> stageList = appDeployEList.stream()
                        .map(appDeploy -> taskRepository.queryByAppDeployId(appDeploy.getId()))
                        .filter(Objects::nonNull)
                        .map(PipelineTaskE::getStageId)
                        .distinct().collect(Collectors.toList());
                List<Long> pipelineList = stageList.stream()
                        .map(stageId -> stageRepository.queryById(stageId).getPipelineId())
                        .distinct().collect(Collectors.toList());
                List<PipelineE> pipelineES = new ArrayList<>();
                pipelineList.forEach(pipelineId -> {
                    PipelineE pipelineE = pipelineRepository.queryById(pipelineId);
                    if (pipelineE.getIsEnabled() == 1 && "auto".equals(pipelineE.getTriggerType())) {
                        pipelineES.add(pipelineE);
                    }
                });

                pipelineES.forEach(pipelineE -> {
                    if (pipelineService.checkDeploy(pipelineE.getProjectId(), pipelineE.getId()).getVersions()) {
                        LOGGER.info("autoDeploy: versionId:{}, version:{} pipelineId:{}", insertApplicationVersionE.getId(), insertApplicationVersionE.getVersion(), pipelineE.getId());
                        pipelineService.executeAutoDeploy(pipelineE.getId());
                    }
                });
            }
        }
    }

    private PipelineAppDeployE filterAppDeploy(PipelineAppDeployE deployE, String version) {
        if (deployE.getTriggerVersion() == null || deployE.getTriggerVersion().isEmpty()) {
            return deployE;
        } else {
            List<String> list = Arrays.asList(deployE.getTriggerVersion().split(","));
            Optional<String> branch = list.stream().filter(version::contains).findFirst();
            if (branch.isPresent() && !branch.get().isEmpty()) {
                return deployE;
            }
            return null;
        }
    }

    @Override
    public List<ApplicationVersionRepDTO> listByAppId(Long appId, Boolean isPublish) {
        return ConvertHelper.convertList(
                applicationVersionRepository.baseListByAppId(appId, isPublish), ApplicationVersionRepDTO.class);
    }

    @Override
    public PageInfo<ApplicationVersionRepDTO> listByAppIdAndParamWithPage(Long appId, Boolean isPublish, Long appVersionId, PageRequest pageRequest, String searchParam) {
        return ConvertPageHelper.convertPageInfo(
                applicationVersionRepository.listByAppIdAndParamWithPage(appId, isPublish, appVersionId, pageRequest, searchParam), ApplicationVersionRepDTO.class);
    }

    @Override
    public List<ApplicationVersionRepDTO> listDeployedByAppId(Long projectId, Long appId) {
        return ConvertHelper.convertList(
                applicationVersionRepository.baseListAppDeployedVersion(projectId, appId), ApplicationVersionRepDTO.class);
    }

    @Override
    public List<ApplicationVersionRepDTO> listByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return ConvertHelper.convertList(
                applicationVersionRepository.baseListByAppIdAndEnvId(projectId, appId, envId),
                ApplicationVersionRepDTO.class);
    }

    @Override
    public PageInfo<ApplicationVersionRepDTO> listApplicationVersionInApp(Long projectId, Long appId, PageRequest pageRequest, String searchParams) {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectVO projectE = iamRepository.queryIamProject(projectId);
        Boolean isProjectOwner = iamRepository.isProjectOwner(userAttrE.getIamUserId(), projectE);
        PageInfo<ApplicationVersionE> applicationVersionEPage = applicationVersionRepository.basePageByOptions(
                projectId, appId, pageRequest, searchParams, isProjectOwner, userAttrE.getIamUserId());
        return ConvertPageHelper.convertPageInfo(applicationVersionEPage, ApplicationVersionRepDTO.class);
    }

    @Override
    public List<ApplicationVersionRepDTO> getUpgradeAppVersion(Long projectId, Long appVersionId) {
        applicationVersionRepository.baseCheckByProjectAndVersionId(projectId, appVersionId);
        return ConvertHelper.convertList(
                applicationVersionRepository.baseListUpgradeVersion(appVersionId),
                ApplicationVersionRepDTO.class);
    }

    @Override
    public DeployVersionDTO listDeployVersions(Long appId) {
        ApplicationVersionE applicationVersionE = applicationVersionRepository.baseQueryNewestVersion(appId);
        DeployVersionDTO deployVersionDTO = new DeployVersionDTO();
        List<DeployEnvVersionDTO> deployEnvVersionDTOS = new ArrayList<>();
        if (applicationVersionE != null) {
            Map<Long, List<ApplicationInstanceE>> envInstances = applicationInstanceRepository.listByAppId(appId).stream().filter(applicationInstanceE -> applicationInstanceE.getCommandId() != null)
                    .collect(Collectors.groupingBy(t -> t.getDevopsEnvironmentE().getId()));
            if (!envInstances.isEmpty()) {
                envInstances.forEach((key, value) -> {
                    DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(key);
                    DeployEnvVersionDTO deployEnvVersionDTO = new DeployEnvVersionDTO();
                    deployEnvVersionDTO.setEnvName(devopsEnvironmentE.getName());
                    List<DeployInstanceVersionDTO> deployInstanceVersionDTOS = new ArrayList<>();
                    Map<Long, List<ApplicationInstanceE>> versionInstances = value.stream().collect(Collectors.groupingBy(t -> {
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(t.getCommandId());
                        return devopsEnvCommandE.getObjectVersionId();
                    }));
                    if (!versionInstances.isEmpty()) {
                        versionInstances.forEach((newkey, newvalue) -> {
                            ApplicationVersionE newApplicationVersionE = applicationVersionRepository.baseQuery(newkey);
                            DeployInstanceVersionDTO deployInstanceVersionDTO = new DeployInstanceVersionDTO();
                            deployInstanceVersionDTO.setDeployVersion(newApplicationVersionE.getVersion());
                            deployInstanceVersionDTO.setInstanceCount(newvalue.size());
                            if (newApplicationVersionE.getId() < applicationVersionE.getId()) {
                                deployInstanceVersionDTO.setUpdate(true);
                            }
                            deployInstanceVersionDTOS.add(deployInstanceVersionDTO);
                        });
                    }
                    deployEnvVersionDTO.setDeployIntanceVersionDTO(deployInstanceVersionDTOS);
                    deployEnvVersionDTOS.add(deployEnvVersionDTO);
                });

                deployVersionDTO.setLatestVersion(applicationVersionE.getVersion());
                deployVersionDTO.setDeployEnvVersionDTO(deployEnvVersionDTOS);
            }
        }
        return deployVersionDTO;
    }

    @Override
    public String queryVersionValue(Long appVersionId) {
        ApplicationVersionE applicationVersionE = applicationVersionRepository.baseQuery(appVersionId);
        ApplicationVersionValueE applicationVersionValueE = applicationVersionValueRepository.baseQuery(applicationVersionE.getApplicationVersionValueE().getId());
        return applicationVersionValueE.getValue();
    }

    @Override
    public ApplicationVersionRepDTO queryById(Long appVersionId) {
        return ConvertHelper.convert(applicationVersionRepository.baseQuery(appVersionId), ApplicationVersionRepDTO.class);
    }

    @Override
    public List<ApplicationVersionRepDTO> listByAppVersionIds(List<Long> appVersionIds) {
        return ConvertHelper.convertList(applicationVersionRepository.baseListByAppVersionIds(appVersionIds), ApplicationVersionRepDTO.class);
    }

    @Override
    public List<ApplicationVersionAndCommitDTO> listByAppIdAndBranch(Long appId, String branch) {
        List<ApplicationVersionE> applicationVersionES = applicationVersionRepository.baseListByAppIdAndBranch(appId, branch);
        ApplicationE applicationE = applicationRepository.query(appId);
        ProjectVO projectE = iamRepository.queryIamProject(applicationE.getProjectE().getId());
        OrganizationVO organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        List<ApplicationVersionAndCommitDTO> applicationVersionAndCommitDTOS = new ArrayList<>();
        applicationVersionES.forEach(applicationVersionE -> {
            ApplicationVersionAndCommitDTO applicationVersionAndCommitDTO = new ApplicationVersionAndCommitDTO();
            DevopsGitlabCommitE devopsGitlabCommitE = devopsGitlabCommitRepository.queryByShaAndRef(applicationVersionE.getCommit(), branch);
            UserE userE = iamRepository.queryUserByUserId(devopsGitlabCommitE.getUserId());
            applicationVersionAndCommitDTO.setAppName(applicationE.getName());
            applicationVersionAndCommitDTO.setCommit(applicationVersionE.getCommit());
            applicationVersionAndCommitDTO.setCommitContent(devopsGitlabCommitE.getCommitContent());
            applicationVersionAndCommitDTO.setCommitUserImage(userE == null ? null : userE.getImageUrl());
            applicationVersionAndCommitDTO.setCommitUserName(userE == null ? null : userE.getRealName());
            applicationVersionAndCommitDTO.setVersion(applicationVersionE.getVersion());
            applicationVersionAndCommitDTO.setCreateDate(applicationVersionE.getCreationDate());
            applicationVersionAndCommitDTO.setCommitUrl(gitlabUrl + "/"
                    + organization.getCode() + "-" + projectE.getCode() + "/"
                    + applicationE.getCode() + ".git");
            applicationVersionAndCommitDTOS.add(applicationVersionAndCommitDTO);

        });
        return applicationVersionAndCommitDTOS;
    }

    @Override
    public Boolean queryByPipelineId(Long pipelineId, String branch, Long appId) {
        return applicationVersionRepository.baseQueryByPipelineId(pipelineId, branch, appId) != null;
    }

    @Override
    public String queryValueById(Long projectId, Long appId) {
        return applicationVersionRepository.baseQueryValueByappId(appId);
    }

    @Override
    public ApplicationVersionRepDTO queryByAppAndVersion(Long appId, String version) {
        return ConvertHelper.convert(applicationVersionRepository.baseQueryByAppIdAndVersion(appId, version), ApplicationVersionRepDTO.class);
    }



    public List<ApplicationLatestVersionDTO> baseListAppNewestVersion(Long projectId) {
        ProjectDO projectDO = iamServiceClientOperator.queryIamProject(projectId);
        List<ProjectDO> projectEList = iamServiceClientOperator.listIamProjectByOrgId(projectDO.getOrganizationId(), null, null);
        List<Long> projectIds = projectEList.stream().map(ProjectDO::getId)
                .collect(Collectors.toCollection(ArrayList::new));
        return applicationVersionMapper.listAppNewestVersion(projectId, projectIds);
    }

    public ApplicationVersionDTO baseCreate(ApplicationVersionDTO applicationVersionDTO) {
        if (applicationVersionMapper.insert(applicationVersionDTO) != 1) {
            throw new CommonException("error.version.insert");
        }
        //待创建readme
        return applicationVersionDTO;
    }

    public List<ApplicationVersionDTO> baseListByAppId(Long appId, Boolean isPublish) {
        List<ApplicationVersionDTO> applicationVersionDTOS = applicationVersionMapper.listByAppId(appId, isPublish);
        if (applicationVersionDTOS.isEmpty()) {
            return Collections.emptyList();
        }
        return applicationVersionDTOS;
    }

    public PageInfo<ApplicationVersionDTO> basePageByPublished(Long appId, Boolean isPublish, Long appVersionId, PageRequest pageRequest, String searchParam) {
        PageInfo<ApplicationVersionDTO> applicationVersionDTOPageInfo;
        applicationVersionDTOPageInfo = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
                () -> applicationVersionMapper.selectByAppIdAndParamWithPage(appId, isPublish, searchParam));
        if (appVersionId != null) {
            ApplicationVersionDTO versionDO = new ApplicationVersionDTO();
            versionDO.setId(appVersionId);
            ApplicationVersionDTO searchDO = applicationVersionMapper.selectByPrimaryKey(versionDO);
            applicationVersionDTOPageInfo.getList().removeIf(v -> v.getId().equals(appVersionId));
            applicationVersionDTOPageInfo.getList().add(0, searchDO);
        }
        return applicationVersionDTOPageInfo;
    }

    public List<ApplicationVersionDTO> baseListAppDeployedVersion(Long projectId, Long appId) {
        List<ApplicationVersionDTO> applicationVersionDTOS =
                applicationVersionMapper.listAppDeployedVersion(projectId, appId);
        if (applicationVersionDTOS.isEmpty()) {
            return Collections.emptyList();
        }
        return applicationVersionDTOS;
    }

    public ApplicationVersionDTO baseQuery(Long appVersionId) {
        return applicationVersionMapper.selectByPrimaryKey(appVersionId);
    }

    public List<ApplicationVersionDTO> baseListByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return applicationVersionMapper.listByAppIdAndEnvId(projectId, appId, envId);
    }

    public String baseQueryValue(Long versionId) {
        return applicationVersionMapper.queryValue(versionId);
    }

    public ApplicationVersionDTO baseQueryByAppIdAndVersion(Long appId, String version) {
        ApplicationVersionDTO applicationVersionDTO = new ApplicationVersionDTO();
        applicationVersionDTO.setAppId(appId);
        applicationVersionDTO.setVersion(version);
        List<ApplicationVersionDTO> applicationVersionDTOS = applicationVersionMapper.select(applicationVersionDTO);
        if (applicationVersionDTOS.isEmpty()) {
            return null;
        }
        return applicationVersionDTOS.get(0);
    }

    public void baseUpdatePublishLevelByIds(List<Long> appVersionIds, Long level) {
        ApplicationVersionDTO applicationVersionDTO = new ApplicationVersionDTO();
        applicationVersionDTO.setIsPublish(level);
        for (Long id : appVersionIds) {
            applicationVersionDTO.setId(id);
            applicationVersionDTO.setObjectVersionNumber(applicationVersionMapper.selectByPrimaryKey(id).getObjectVersionNumber());
            if (applicationVersionDTO.getObjectVersionNumber() == null) {
                applicationVersionDTO.setPublishTime(new java.sql.Date(new java.util.Date().getTime()));
                applicationVersionMapper.updateObJectVersionNumber(id);
                applicationVersionDTO.setObjectVersionNumber(1L);
            }
            applicationVersionMapper.updateByPrimaryKeySelective(applicationVersionDTO);
        }
    }

    public PageInfo<ApplicationVersionDTO> basePageByOptions(Long projectId, Long appId, PageRequest pageRequest,
                                                            String searchParam, Boolean isProjectOwner,
                                                            Long userId) {
        Sort sort = pageRequest.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if (property.equals("version")) {
                            property = "dav.version";
                        } else if (property.equals("creationDate")) {
                            property = "dav.creation_date";
                        }
                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }

        PageInfo<ApplicationVersionDTO> applicationVersionDTOPageInfo;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            applicationVersionDTOPageInfo = PageHelper
                    .startPage(pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> applicationVersionMapper.listApplicationVersion(projectId, appId,
                            TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                            TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM)), isProjectOwner, userId));
        } else {
            applicationVersionDTOPageInfo = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> applicationVersionMapper
                    .listApplicationVersion(projectId, appId, null, null, isProjectOwner, userId));
        }
        return applicationVersionDTOPageInfo;
    }

    public List<ApplicationVersionDTO> baseListByPublished(Long applicationId) {
        List<ApplicationVersionDTO> applicationVersionDTOS = applicationVersionMapper
                .listByPublished(applicationId);
        return applicationVersionDTOS;
    }

    public Boolean baseCheckByAppIdAndVersionIds(Long appId, List<Long> appVersionIds) {
        if (appId == null || appVersionIds.isEmpty()) {
            throw new CommonException("error.app.version.check");
        }
        List<Long> versionList = applicationVersionMapper.listByAppIdAndVersionIds(appId);
        if (appVersionIds.stream().anyMatch(t -> !versionList.contains(t))) {
            throw new CommonException("error.app.version.check");
        }
        return true;
    }

    public Long baseCreateReadme(String readme) {
        ApplicationVersionReadmeDTO applicationVersionReadmeDTO = new ApplicationVersionReadmeDTO(readme);
        applicationVersionReadmeMapper.insert(applicationVersionReadmeDTO);
        return applicationVersionReadmeDTO.getId();
    }

    public String baseQueryReadme(Long readmeValueId) {
        String readme;
        try {
            readme = applicationVersionReadmeMapper.selectByPrimaryKey(readmeValueId).getReadme();
        } catch (Exception ignore) {
            readme = "# 暂无";
        }
        return readme;
    }

    public void baseUpdate(ApplicationVersionDTO applicationVersionDTO) {
        if (applicationVersionMapper.updateByPrimaryKey(applicationVersionDTO) != 1) {
            throw new CommonException("error.version.update");
        }
        //待修改readme
    }

    private void updateReadme(Long readmeValueId, String readme) {
        ApplicationVersionReadmeDTO readmeDO;
        try {

            readmeDO = applicationVersionReadmeMapper.selectByPrimaryKey(readmeValueId);
            readmeDO.setReadme(readme);
            applicationVersionReadmeMapper.updateByPrimaryKey(readmeDO);
        } catch (Exception e) {
            readmeDO = new ApplicationVersionReadmeDTO(readme);
            applicationVersionReadmeMapper.insert(readmeDO);
        }
    }

    public List<ApplicationVersionDTO> baseListUpgradeVersion(Long appVersionId) {
        return applicationVersionMapper.listUpgradeVersion(appVersionId);
    }


    public void baseCheckByProjectAndVersionId(Long projectId, Long appVersionId) {
        Integer index = applicationVersionMapper.checkByProjectAndVersionId(projectId, appVersionId);
        if (index == 0) {
            throw new CommonException("error.project.AppVersion.notExist");
        }
    }

    public ApplicationVersionDTO baseQueryByCommitSha(Long appId, String ref, String sha) {
        return applicationVersionMapper.queryByCommitSha(appId, ref, sha);
    }

    public ApplicationVersionVO baseQueryNewestVersion(Long appId) {
        return ConvertHelper.convert(applicationVersionMapper.queryNewestVersion(appId), ApplicationVersionVO.class);
    }

    public List<ApplicationVersionDTO> baseListByAppVersionIds(List<Long> appVersionIds) {
        return applicationVersionMapper.listByAppVersionIds(appVersionIds);
    }

    @Override
    public List<ApplicationVersionVO> baseListByAppIdAndBranch(Long appId, String branch) {
        return ConvertHelper.convertList(applicationVersionMapper.listByAppIdAndBranch(appId, branch), ApplicationVersionVO.class);
    }

    public String baseQueryByPipelineId(Long pipelineId, String branch, Long appId) {
        return applicationVersionMapper.queryByPipelineId(pipelineId, branch, appId);
    }

    public String baseQueryValueByAppId(Long appId) {
        return applicationVersionMapper.queryValueByAppId(appId);
    }



    public void baseUpdatePublishTime() {
        applicationVersionMapper.updatePublishTime();
    }
}
