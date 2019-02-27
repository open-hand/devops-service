package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.choerodon.devops.domain.application.handler.DevopsCiInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.app.service.ApplicationVersionService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/3.
 */
@Service
public class ApplicationVersionServiceImpl implements ApplicationVersionService {

    private static final String DESTPATH = "devops";
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

    @Value("${services.helm.url}")
    private String helmUrl;

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
            throw new DevopsCiInvalidException(e.getMessage());
        }
    }

    private void doCreate(String image, String token, String version, String commit, MultipartFile files) {
        ApplicationE applicationE = applicationRepository.queryByToken(token);

        ApplicationVersionValueE applicationVersionValueE = new ApplicationVersionValueE();
        ApplicationVersionE applicationVersionE = new ApplicationVersionE();
        ProjectE projectE = iamRepository.queryIamProject(applicationE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        ApplicationVersionE newApplicationVersionE = applicationVersionRepository
                .queryByAppAndVersion(applicationE.getId(), version);
        applicationVersionE.initApplicationEById(applicationE.getId());
        applicationVersionE.setImage(image);
        applicationVersionE.setCommit(commit);
        applicationVersionE.setVersion(version);
        applicationVersionE.setRepository("/" + organization.getCode() + "/" + projectE.getCode() + "/");
        String classPath = String.format("Charts%s%s%s%s",
                System.getProperty("file.separator"),
                organization.getCode(),
                System.getProperty("file.separator"),
                projectE.getCode());
        String path = FileUtil.multipartFileToFile(classPath, files);
        if (newApplicationVersionE != null) {
            return;
        }
        String destFilePath = DESTPATH + version;
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
                    .create(applicationVersionValueE).getId());
        } catch (Exception e) {
            throw new CommonException("error.version.insert", e);
        }
        applicationVersionE.initApplicationVersionReadmeV(FileUtil.getReadme(destFilePath));
        applicationVersionRepository.create(applicationVersionE);
        FileUtil.deleteDirectory(new File(destFilePath));
    }

    @Override
    public List<ApplicationVersionRepDTO> listByAppId(Long appId, Boolean isPublish) {
        return ConvertHelper.convertList(
                applicationVersionRepository.listByAppId(appId, isPublish), ApplicationVersionRepDTO.class);
    }

    @Override
    public Page<ApplicationVersionRepDTO> listByAppIdAndParamWithPage(Long appId, Boolean isPublish,Long appVersionId,PageRequest pageRequest,String searchParam) {
        return ConvertPageHelper.convertPage(
                applicationVersionRepository.listByAppIdAndParamWithPage(appId, isPublish,appVersionId,pageRequest,searchParam), ApplicationVersionRepDTO.class);
    }

    @Override
    public List<ApplicationVersionRepDTO> listDeployedByAppId(Long projectId, Long appId) {
        return ConvertHelper.convertList(
                applicationVersionRepository.listDeployedByAppId(projectId, appId), ApplicationVersionRepDTO.class);
    }

    @Override
    public List<ApplicationVersionRepDTO> listByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return ConvertHelper.convertList(
                applicationVersionRepository.listByAppIdAndEnvId(projectId, appId, envId),
                ApplicationVersionRepDTO.class);
    }

    @Override
    public Page<ApplicationVersionRepDTO> listApplicationVersionInApp(Long projectId, Long appId, PageRequest pageRequest, String searchParam) {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Boolean isProjectOwner = iamRepository.isProjectOwner(userAttrE.getIamUserId(), projectE);
        Page<ApplicationVersionE> applicationVersionEPage = applicationVersionRepository.listApplicationVersionInApp(
                projectId, appId, pageRequest, searchParam, isProjectOwner, userAttrE.getIamUserId());
        return ConvertPageHelper.convertPage(applicationVersionEPage, ApplicationVersionRepDTO.class);
    }

    @Override
    public List<ApplicationVersionRepDTO> getUpgradeAppVersion(Long projectId, Long appVersionId) {
        applicationVersionRepository.checkProIdAndVerId(projectId, appVersionId);
        return ConvertHelper.convertList(
                applicationVersionRepository.selectUpgradeVersions(appVersionId),
                ApplicationVersionRepDTO.class);
    }

    @Override
    public DeployVersionDTO listDeployVersions(Long appId) {
        ApplicationVersionE applicationVersionE = applicationVersionRepository.getLatestVersion(appId);
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
                            ApplicationVersionE newApplicationVersionE = applicationVersionRepository.query(newkey);
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
        ApplicationVersionE applicationVersionE = applicationVersionRepository.query(appVersionId);
        ApplicationVersionValueE applicationVersionValueE = applicationVersionValueRepository.query(applicationVersionE.getApplicationVersionValueE().getId());
        return applicationVersionValueE.getValue();
    }

    @Override
    public ApplicationVersionRepDTO queryById(Long appVersionId) {
        return ConvertHelper.convert(applicationVersionRepository.query(appVersionId), ApplicationVersionRepDTO.class);
    }

    @Override
    public List<ApplicationVersionRepDTO> listByAppVersionIds(List<Long> appVersionIds) {
        return ConvertHelper.convertList(applicationVersionRepository.listByAppVersionIds(appVersionIds), ApplicationVersionRepDTO.class);
    }

    @Override
    public List<ApplicationVersionAndCommitDTO> listByAppIdAndBranch(Long appId, String branch) {
        List<ApplicationVersionE> applicationVersionES = applicationVersionRepository.listByAppIdAndBranch(appId, branch);
        ApplicationE applicationE = applicationRepository.query(appId);
        ProjectE projectE = iamRepository.queryIamProject(applicationE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
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
    public Boolean queryByPipelineId(Long pipelineId, String branch) {
        return applicationVersionRepository.queryByPipelineId(pipelineId, branch) != null;
    }


}
