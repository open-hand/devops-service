package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.ApplicationTemplateDTO;
import io.choerodon.devops.api.dto.ApplicationTemplateRepDTO;
import io.choerodon.devops.api.dto.ApplicationTemplateUpdateDTO;
import io.choerodon.devops.api.dto.GitlabProjectEventDTO;
import io.choerodon.devops.api.validator.ApplicationTemplateValidator;
import io.choerodon.devops.app.service.ApplicationTemplateService;
import io.choerodon.devops.domain.application.entity.ApplicationTemplateE;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabUserE;
import io.choerodon.devops.domain.application.event.GitlabProjectPayload;
import io.choerodon.devops.domain.application.factory.ApplicationTemplateFactory;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.GitUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.Visibility;
import io.choerodon.event.producer.execute.EventProducerTemplate;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by younger on 2018/3/27.
 */

@Service
public class ApplicationTemplateServiceImpl implements ApplicationTemplateService {

    private static final String TEMPLATE = "template";
    private static final String MASTER = "master";
    @Value("${spring.application.name}")
    private String applicationName;
    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private EventProducerTemplate eventProducerTemplate;
    @Autowired
    private ApplicationTemplateRepository applicationTemplateRepository;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private GitlabUserRepository gitlabUserRepository;


    @Override
    public ApplicationTemplateRepDTO create(ApplicationTemplateDTO applicationTemplateDTO, Long organizationId) {
        ApplicationTemplateValidator.checkApplicationTemplate(applicationTemplateDTO);
        ApplicationTemplateE applicationTemplateE = ConvertHelper.convert(
                applicationTemplateDTO, ApplicationTemplateE.class);
        applicationTemplateRepository.checkCode(applicationTemplateE);
        applicationTemplateRepository.checkName(applicationTemplateE);
        Integer gitlabGroupId;
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        Organization organization = iamRepository.queryOrganizationById(organizationId);
        applicationTemplateE.initOrganization(organization.getId());
        GitlabGroupE gitlabGroupE = gitlabRepository.queryGroupByName(
                organization.getCode() + "_" + TEMPLATE, TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        if (gitlabGroupE == null) {
            GitlabGroupE gitlabGroupENew = new GitlabGroupE();
            gitlabGroupENew.initName(organization.getCode() + "_" + TEMPLATE);
            gitlabGroupENew.initPath(organization.getCode() + "_" + TEMPLATE);
            gitlabGroupENew.initVisibility(Visibility.PUBLIC);
            gitlabGroupId = gitlabRepository
                    .createGroup(gitlabGroupENew, TypeUtil.objToInteger(userAttrE.getGitlabUserId())).getId();
        } else {
            gitlabGroupId = gitlabGroupE.getId();
        }
        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload();
        gitlabProjectPayload.setGroupId(gitlabGroupId);
        gitlabProjectPayload.setUserId(TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        gitlabProjectPayload.setPath(applicationTemplateDTO.getCode());
        gitlabProjectPayload.setOrganizationId(organization.getId());
        gitlabProjectPayload.setType(TEMPLATE);
        Exception exception = eventProducerTemplate
                .execute("CreateGitlabProject", "gitlab-service", gitlabProjectPayload,
                        (String uuid) -> {
                            applicationTemplateE.initUuid(uuid);
                            if (applicationTemplateRepository.create(applicationTemplateE) == null) {
                                throw new CommonException("error.applicationTemplate.insert");
                            }
                        });
        if (exception != null) {
            throw new CommonException(exception.getMessage());
        }
        return ConvertHelper.convert(applicationTemplateRepository.queryByCode(organization.getId(),
                applicationTemplateDTO.getCode()), ApplicationTemplateRepDTO.class);
    }

    @Override
    public ApplicationTemplateRepDTO update(ApplicationTemplateUpdateDTO applicationTemplateUpdateDTO, Long organizationId) {
        ApplicationTemplateE applicationTemplateE = ConvertHelper.convert(
                applicationTemplateUpdateDTO, ApplicationTemplateE.class);
        applicationTemplateE.initOrganization(organizationId);
        return ConvertHelper.convert(applicationTemplateRepository.update(applicationTemplateE),
                ApplicationTemplateRepDTO.class);
    }

    @Override
    public void delete(Long appTemplateId) {
        ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.query(appTemplateId);
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        if (applicationTemplateE.getGitlabProjectE() != null) {
            gitlabRepository.deleteProject(
                    applicationTemplateE.getGitlabProjectE().getId(),
                    TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        }
        applicationTemplateRepository.delete(appTemplateId);
    }

    @Override
    public ApplicationTemplateRepDTO query(Long appTemplateId) {
        ApplicationTemplateRepDTO applicationTemplateRepDTO = ConvertHelper.convert(applicationTemplateRepository
                        .query(appTemplateId),
                ApplicationTemplateRepDTO.class);
        String repoUrl = applicationTemplateRepDTO.getRepoUrl();
        if (applicationTemplateRepDTO.getOrganizationId() != null) {
            repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1, repoUrl.length()) : repoUrl;
            repoUrl = !gitlabUrl.endsWith("/")
                    ? gitlabUrl + "/" + repoUrl
                    : gitlabUrl + repoUrl;
        }
        applicationTemplateRepDTO.setRepoUrl(
                repoUrl);
        return applicationTemplateRepDTO;
    }

    @Override
    public Page<ApplicationTemplateRepDTO> listByOptions(PageRequest pageRequest, Long organizationId, String searchParam) {
        Page<ApplicationTemplateRepDTO> applicationTemplateRepDTOPage = ConvertPageHelper
                .convertPage(applicationTemplateRepository.listByOptions(
                        pageRequest, organizationId, searchParam),
                        ApplicationTemplateRepDTO.class);
        List<ApplicationTemplateRepDTO> applicationTemplateRepDTOList = applicationTemplateRepDTOPage.getContent();
        for (ApplicationTemplateRepDTO applicationTemplateRepDTO : applicationTemplateRepDTOList) {
            String repoUrl = applicationTemplateRepDTO.getRepoUrl();
            if (applicationTemplateRepDTO.getOrganizationId() != null) {
                repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1, repoUrl.length()) : repoUrl;
                repoUrl = !gitlabUrl.endsWith("/")
                        ? gitlabUrl + "/" + repoUrl
                        : gitlabUrl + repoUrl;
            }
            applicationTemplateRepDTO.setRepoUrl(
                    repoUrl);
        }
        applicationTemplateRepDTOPage.setContent(applicationTemplateRepDTOList);
        return applicationTemplateRepDTOPage;
    }


    @Override
    public void operationApplicationTemplate(GitlabProjectEventDTO gitlabProjectEventDTO) {
        ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.queryByCode(
                gitlabProjectEventDTO.getOrganizationId(), gitlabProjectEventDTO.getPath());

        applicationTemplateE.initGitlabProjectE(
                TypeUtil.objToInteger(gitlabProjectEventDTO.getGitlabProjectId()));
        applicationTemplateRepository.update(applicationTemplateE);
        String applicationDir = gitlabProjectEventDTO.getType() + System.currentTimeMillis();
        if (applicationTemplateE.getCopyFrom() != null) {
            ApplicationTemplateRepDTO templateRepDTO = ConvertHelper.convert(applicationTemplateRepository
                    .query(applicationTemplateE.getCopyFrom()), ApplicationTemplateRepDTO.class);
            //拉取模板
            String repoUrl = templateRepDTO.getRepoUrl();
            String type = templateRepDTO.getCode();
            if (templateRepDTO.getOrganizationId() != null) {
                repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1, repoUrl.length()) : repoUrl;
                repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl;
                type = MASTER;
            }
            Git git = gitUtil.clone(
                    applicationDir,
                    type,
                    repoUrl);
            List<String> tokens = gitlabRepository.listTokenByUserId(gitlabProjectEventDTO.getGitlabProjectId(),
                    applicationDir, gitlabProjectEventDTO.getUserId());
            String accessToken = "";
            if (tokens.isEmpty()) {
                accessToken = gitlabRepository.createToken(gitlabProjectEventDTO.getGitlabProjectId(),
                        applicationDir, gitlabProjectEventDTO.getUserId());
            } else {
                accessToken = tokens.get(tokens.size() - 1);
            }
            GitlabUserE gitlabUserE = gitlabUserRepository.getGitlabUserByUserId(gitlabProjectEventDTO.getUserId());
            repoUrl = applicationTemplateE.getRepoUrl();
            repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1, repoUrl.length()) : repoUrl;
            gitUtil.push(
                    git,
                    applicationDir,
                    !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl,
                    gitlabUserE.getUsername(),
                    accessToken,
                    TEMPLATE);
        } else {
            gitlabRepository.createFile(gitlabProjectEventDTO.getGitlabProjectId(),
                    gitlabProjectEventDTO.getUserId());
        }
    }

    @Override
    public List<ApplicationTemplateRepDTO> list(Long organizationId) {
        List<ApplicationTemplateRepDTO> applicationTemplateRepDTOList = ConvertHelper.convertList(
                applicationTemplateRepository.list(organizationId),
                ApplicationTemplateRepDTO.class);
        for (ApplicationTemplateRepDTO applicationTemplateRepDTO : applicationTemplateRepDTOList) {
            String repoUrl = applicationTemplateRepDTO.getRepoUrl();
            if (applicationTemplateRepDTO.getOrganizationId() != null) {
                repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1, repoUrl.length()) : repoUrl;
                repoUrl = !gitlabUrl.endsWith("/")
                        ? gitlabUrl + "/" + repoUrl
                        : gitlabUrl + repoUrl;
            }
            applicationTemplateRepDTO.setRepoUrl(
                    repoUrl);
        }
        return applicationTemplateRepDTOList;
    }

    @Override
    public void checkName(Long organizationId, String name) {
        ApplicationTemplateE applicationTemplateE = ApplicationTemplateFactory.createApplicationTemplateE();
        applicationTemplateE.initOrganization(organizationId);
        applicationTemplateE.setName(name);
        applicationTemplateRepository.checkName(applicationTemplateE);
    }

    @Override
    public void checkCode(Long organizationId, String code) {
        ApplicationTemplateE applicationTemplateE = ApplicationTemplateFactory.createApplicationTemplateE();
        applicationTemplateE.initOrganization(organizationId);
        applicationTemplateE.setCode(code);
        applicationTemplateRepository.checkCode(applicationTemplateE);
    }

    @Override
    public Boolean applicationTemplateExist(String uuid) {
        return applicationTemplateRepository.applicationTemplateExist(uuid);
    }

}
