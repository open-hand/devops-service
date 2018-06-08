package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.api.validator.ApplicationValidator;
import io.choerodon.devops.app.service.ApplicationService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.ApplicationTemplateE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabUserE;
import io.choerodon.devops.domain.application.event.GitlabProjectPayload;
import io.choerodon.devops.domain.application.factory.ApplicationFactory;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.common.util.*;
import io.choerodon.devops.infra.common.util.enums.AccessLevel;
import io.choerodon.devops.infra.config.CiYamlConfig;
import io.choerodon.event.producer.execute.EventProducerTemplate;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by younger on 2018/3/28.
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    private static final String MASTER = "master";
    private static final String DEVELOP = "develop";
    private static final String APPLICATION = "application";
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private ApplicationTemplateRepository applicationTemplateRepository;
    @Autowired
    private DevopsProjectRepository devopsProjectRepository;
    @Autowired
    private EventProducerTemplate eventProducerTemplate;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private CiYamlConfig ciYamlConfig;
    @Autowired
    private GitlabUserRepository gitlabUserRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;

    @Override
    public ApplicationRepDTO create(Long projectId, ApplicationDTO applicationDTO) {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ApplicationValidator.checkApplication(applicationDTO);
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        ApplicationE applicationE = ConvertHelper.convert(applicationDTO, ApplicationE.class);
        applicationE.initProjectE(projectId);
        applicationRepository.checkName(applicationE);
        applicationRepository.checkCode(applicationE);
        applicationE.initActive(true);
        applicationE.initSynchro(false);
        GitlabGroupE gitlabGroupE = devopsProjectRepository.queryDevopsProject(applicationE.getProjectE().getId());
        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload();
        gitlabProjectPayload.setType(APPLICATION);
        gitlabProjectPayload.setPath(applicationDTO.getCode());
        gitlabProjectPayload.setOrganizationId(organization.getId());
        gitlabProjectPayload.setUserId(TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        gitlabProjectPayload.setGroupId(gitlabGroupE.getId());
        Exception exception = eventProducerTemplate.execute("CreateGitlabProject", "gitlab-service", gitlabProjectPayload,
                (String uuid) -> {
                    applicationE.initUuid(uuid);
                    if (applicationRepository.create(applicationE) != 1) {
                        throw new CommonException("error.order.create.insert");
                    }
                });
        if (exception != null) {
            throw new CommonException(exception.getMessage());
        }
        return ConvertHelper.convert(applicationRepository.queryByCode(applicationE.getCode(),
                applicationE.getProjectE().getId()), ApplicationRepDTO.class);
    }

    @Override
    public ApplicationRepDTO query(Long projectId, Long applicationId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        ApplicationE applicationE = applicationRepository.query(applicationId);
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        applicationE.initGitlabProjectEByUrl(gitlabUrl + urlSlash
                + organization.getCode() + "-" + projectE.getCode() + "/"
                + applicationE.getCode() + ".git");
        return ConvertHelper.convert(applicationE, ApplicationRepDTO.class);
    }

    @Override
    public Boolean update(Long projectId, ApplicationUpdateDTO applicationUpdateDTO) {
        ApplicationE applicationE = ConvertHelper.convert(applicationUpdateDTO, ApplicationE.class);
        applicationE.initProjectE(projectId);
        applicationRepository.checkName(applicationE);
        if (applicationRepository.update(applicationE) != 1) {
            throw new CommonException("error.application.update");
        }
        return true;
    }

    @Override
    public Boolean active(Long applicationId, Boolean active) {
        if (!active) {
            applicationRepository.checkAppCanDisable(applicationId);
        }
        ApplicationE applicationE = applicationRepository.query(applicationId);
        applicationE.initActive(active);
        if (applicationRepository.update(applicationE) != 1) {
            throw new CommonException("error.application.active");
        }
        return true;
    }

    @Override
    public Page<ApplicationRepDTO> listByOptions(Long projectId, Boolean isActive, PageRequest pageRequest, String params) {
        Page<ApplicationE> applicationES =
                applicationRepository.listByOptions(projectId, isActive, pageRequest, params);
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        for (ApplicationE applicationE : applicationES) {
            applicationE.initGitlabProjectEByUrl(gitlabUrl + urlSlash
                    + organization.getCode() + "-" + projectE.getCode() + "/"
                    + applicationE.getCode() + ".git");
        }
        return ConvertPageHelper.convertPage(applicationES, ApplicationRepDTO.class);
    }

    @Override
    public List<ApplicationRepDTO> listByActive(Long projectId) {
        return ConvertHelper.convertList(applicationRepository.listByActive(projectId), ApplicationRepDTO.class);
    }

    @Override
    public List<ApplicationRepDTO> listAll(Long projectId) {
        return ConvertHelper.convertList(applicationRepository.listAll(projectId), ApplicationRepDTO.class);
    }

    @Override
    public void checkName(Long projectId, String name) {
        ApplicationE applicationE = ApplicationFactory.createApplicationE();
        applicationE.initProjectE(projectId);
        applicationE.setName(name);
        applicationRepository.checkName(applicationE);
    }

    @Override
    public void checkCode(Long projectId, String code) {
        ApplicationE applicationE = ApplicationFactory.createApplicationE();
        applicationE.initProjectE(projectId);
        applicationE.setCode(code);
        applicationRepository.checkCode(applicationE);
    }

    @Override
    public List<ApplicationTemplateRepDTO> listTemplate(Long projectId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        return ConvertHelper.convertList(applicationTemplateRepository.list(projectE.getOrganization().getId()),
                ApplicationTemplateRepDTO.class);
    }

    @Override
    public void operationApplication(GitlabProjectEventDTO gitlabProjectEventDTO) {
        GitlabGroupE gitlabGroupE = devopsProjectRepository.queryByGitlabGroupId(
                TypeUtil.objToInteger(gitlabProjectEventDTO.getGroupId()));
        ApplicationE applicationE = applicationRepository.queryByCode(gitlabProjectEventDTO.getPath(),
                gitlabGroupE.getProjectE().getId());
        ProjectE projectE = iamRepository.queryIamProject(gitlabGroupE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        if (applicationE.getApplicationTemplateE() != null) {
            ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.query(
                    applicationE.getApplicationTemplateE().getId());
            String applicationDir = APPLICATION + System.currentTimeMillis();
            //拉取模板
            String repoUrl = applicationTemplateE.getRepoUrl();
            repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1, repoUrl.length()) : repoUrl;
            Git git = gitUtil.clone(applicationDir, !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl);
            //渲染模板里面的参数
            try {
                File file = new File(gitUtil.getWorkingDirectory(applicationDir));
                Map<String, String> params = new HashMap<>();
                params.put("{{group.name}}", organization.getCode() + "-" + projectE.getCode());
                params.put("{{service.code}}", applicationE.getCode());
                FileUtil.replaceReturnFile(file, params);
            } catch (Exception e) {
                //删除模板
                gitUtil.deleteWorkingDirectory(applicationDir);
                throw new CommonException(e.getMessage());
            }

            List<String> tokens = gitlabRepository.listTokenByUserId(gitlabProjectEventDTO.getGitlabProjectId(),
                    applicationDir, gitlabProjectEventDTO.getUserId());
            String accessToken = "";
            if (tokens.isEmpty()) {
                accessToken = gitlabRepository.createToken(gitlabProjectEventDTO.getGitlabProjectId(),
                        applicationDir, gitlabProjectEventDTO.getUserId());
            } else {
                accessToken = tokens.get(tokens.size() - 1);
            }
            repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            applicationE.initGitlabProjectEByUrl(repoUrl
                    + organization.getCode() + "-" + projectE.getCode() + "/"
                    + applicationE.getCode() + ".git");
            GitlabUserE gitlabUserE = gitlabUserRepository.getGitlabUserByUserId(gitlabProjectEventDTO.getUserId());
            gitUtil.push(git, applicationDir, applicationE.getGitlabProjectE().getRepoURL(),
                    gitlabUserE.getUsername(), accessToken, APPLICATION);
            gitlabRepository.createProtectBranch(gitlabProjectEventDTO.getGitlabProjectId(), MASTER,
                    AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(), gitlabProjectEventDTO.getUserId());
            gitlabRepository.createProtectBranch(gitlabProjectEventDTO.getGitlabProjectId(),
                    DEVELOP, AccessLevel.DEVELOPER.toString(), AccessLevel.DEVELOPER.toString(),
                    gitlabProjectEventDTO.getUserId());
        }
        gitlabRepository.updateProject(gitlabProjectEventDTO.getGitlabProjectId(), gitlabProjectEventDTO.getUserId());
        try {
            String token = GenerateUUID.generateUUID();
            gitlabRepository.addVariable(gitlabProjectEventDTO.getGitlabProjectId(), "Token",
                    token,
                    false, gitlabProjectEventDTO.getUserId());
            applicationE.setToken(token);
            applicationE.initGitlabProjectE(
                    TypeUtil.objToInteger(gitlabProjectEventDTO.getGitlabProjectId()));
            applicationE.initSynchro(true);
            if (applicationRepository.update(applicationE) != 1) {
                throw new CommonException("error.application.update");
            }
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public Boolean applicationExist(String uuid) {
        return applicationRepository.applicationExist(uuid);
    }


    @Override
    public String queryFile(String token) {
        ApplicationE applicationE = applicationRepository.queryByToken(token);
        if (applicationE == null) {
            throw new CommonException("error.app.query.by.token");
        }
        ProjectE projectE = iamRepository.queryIamProject(applicationE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        InputStream inputStream = this.getClass().getResourceAsStream("/shell/ci.sh");
        Map<String, String> params = new HashMap<>();
        params.put("{{ GROUP_NAME }}", organization.getCode() + "-" + projectE.getCode());
        params.put("{{ PROJECT_NAME }}", applicationE.getCode());
        return FileUtil.replaceReturnString(inputStream, params);
    }

    @Override
    public List<ApplicationCodeDTO> listByEnvId(Long projectId, Long envId, String status) {
        return ConvertHelper.convertList(applicationRepository.listByEnvId(projectId, envId, status),
                ApplicationCodeDTO.class);
    }

    @Override
    public Page<ApplicationCodeDTO> pageByEnvId(Long projectId, Long envId, PageRequest pageRequest) {
        return ConvertPageHelper.convertPage(applicationRepository.pageByEnvId(projectId, envId, pageRequest),
                ApplicationCodeDTO.class);
    }

    @Override
    public Page<ApplicationDTO> listByActiveAndPubAndVersion(Long projectId, PageRequest pageRequest, String params) {
        return ConvertPageHelper.convertPage(applicationRepository
                        .listByActiveAndPubAndVersion(projectId, true, pageRequest, params),
                ApplicationDTO.class);
    }

}
