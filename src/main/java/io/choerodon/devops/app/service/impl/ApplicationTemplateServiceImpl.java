package io.choerodon.devops.app.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.ApplicationTemplateValidator;
import io.choerodon.devops.api.vo.ApplicationTemplateRepVO;
import io.choerodon.devops.api.vo.ApplicationTemplateUpdateDTO;
import io.choerodon.devops.api.vo.ApplicationTemplateVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectVO;
import io.choerodon.devops.api.vo.iam.entity.UserAttrE;
import io.choerodon.devops.app.eventhandler.payload.GitlabProjectPayload;
import io.choerodon.devops.app.service.ApplicationTemplateService;
import io.choerodon.devops.app.service.DevopsGitService;
import io.choerodon.devops.app.service.GitLabService;
import io.choerodon.devops.app.service.IamService;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.ApplicationTemplateDTO;
import io.choerodon.devops.infra.dto.gitlab.BranchDTO;
import io.choerodon.devops.infra.dto.gitlab.UserDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.enums.Visibility;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.mapper.ApplicationTemplateMapper;
import io.choerodon.devops.infra.util.*;
import io.kubernetes.client.JSON;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Created by younger on 2018/3/27.
 */

@Service
public class ApplicationTemplateServiceImpl implements ApplicationTemplateService {


    private static final String README = "README.md";
    private String[] models = new String[]{"microservice", "microserviceui", "javalib"};
    private static final String README_CONTENT =
            "# To customize a template\n"
                    + "you need to push the template code to this git repository.\n"
                    + "\n"
                    + "Please make sure the following file exists.\n"
                    + "+ **gitlab-ci.yml**. (Refer to [GitLab Documentation](https://docs.gitlab.com/ee/ci/yaml/))\n"
                    + "+ **Dockerfile**. (Refer to [Dockerfile reference](https://docs.docker.com/engine/reference/builder/))\n"
                    + "+ **Chart** setting directory. (Refer to [helm](https://github.com/kubernetes/helm))\n"
                    + "\n"
                    + "Finally, removing or re-editing this **README.md** file to make it useful.";
    private static final String TEMPLATE = "template";
    private static final String MASTER = "master";

    private Gson gson = new Gson();
    private JSON json = new JSON();

    @Value("${spring.application.name}")
    private String applicationName;
    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Autowired
    private IamService iamRepository;
    @Autowired
    private GitLabService gitlabRepository;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsGitService devopsGitRepository;
    @Autowired
    private ApplicationTemplateMapper applicationTemplateMapper;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private TransactionalProducer transactionalProducer;

    @Autowired
    private SagaClient sagaClient;


    @Override
    @Saga(code = "devops-create-gitlab-template-project",
            description = "devops创建gitlab模板项目", inputSchema = "{}")
    public ApplicationTemplateRepVO create(ApplicationTemplateVO applicationTemplateVO, Long organizationId) {
        ApplicationTemplateValidator.checkApplicationTemplate(applicationTemplateVO);
        ApplicationTemplateDTO applicationTemplateDTO = templateVoToDto(applicationTemplateVO);
        baseCheckCode(applicationTemplateDTO);
        baseCheckName(applicationTemplateDTO);
        Integer gitlabGroupId;
        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        OrganizationVO organization = iamRepository.queryOrganizationById(organizationId);
        applicationTemplateDTO.setOrganizationId(organization.getId());
        applicationTemplateDTO.setSynchro(false);
        applicationTemplateDTO.setFailed(false);
        DevopsProjectVO devopsProjectE = gitlabRepository.queryGroupByName(
                organization.getCode() + "_" + TEMPLATE, TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        if (devopsProjectE == null) {
            DevopsProjectVO devopsProjectENew = new DevopsProjectVO();
            devopsProjectENew.initName(organization.getCode() + "_" + TEMPLATE);
            devopsProjectENew.initPath(organization.getCode() + "_" + TEMPLATE);
            devopsProjectENew.initVisibility(Visibility.PUBLIC);
            gitlabGroupId = TypeUtil.objToInteger(gitlabRepository.createGroup(
                    devopsProjectENew, TypeUtil.objToInteger(userAttrE.getGitlabUserId())).getDevopsAppGroupId());
        } else {
            gitlabGroupId = TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId());
        }
        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload();
        gitlabProjectPayload.setGroupId(gitlabGroupId);
        gitlabProjectPayload.setUserId(TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        gitlabProjectPayload.setPath(applicationTemplateVO.getCode());
        gitlabProjectPayload.setOrganizationId(organization.getId());
        gitlabProjectPayload.setType(TEMPLATE);

        String input = gson.toJson(gitlabProjectPayload);
        transactionalProducer.apply(
                StartSagaBuilder.newBuilder()
                        .withSagaCode("devops-create-gitlab-template-project")
                        .withJson(input)
                        .withLevel(ResourceLevel.ORGANIZATION)
                        .withSourceId(organizationId),
                builder -> {
                });

        return templateDtoToResponse(baseQueryByCode(organization.getId(), applicationTemplateDTO.getCode()));
    }

    @Override
    public ApplicationTemplateRepVO update(ApplicationTemplateUpdateDTO applicationTemplateUpdateDTO, Long organizationId) {
        ApplicationTemplateDTO templateDTO = new ApplicationTemplateDTO();
        BeanUtils.copyProperties(applicationTemplateUpdateDTO, templateDTO);
        return templateDtoToResponse(baseUpdate(templateDTO));
    }

    @Override
    public void delete(Long appTemplateId) {
        ApplicationTemplateDTO applicationTemplateDTO = baseQuery(appTemplateId);
        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        if (applicationTemplateDTO.getGitlabProjectId() != null) {
            gitlabRepository.deleteProject(
                    TypeUtil.objToInteger(applicationTemplateDTO.getGitlabProjectId()),
                    TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        }
        applicationTemplateMapper.deleteByPrimaryKey(appTemplateId);
    }

    @Override
    public ApplicationTemplateRepVO queryByTemplateId(Long appTemplateId) {
        ApplicationTemplateRepVO applicationTemplateRepDTO = templateDtoToResponse(baseQuery(appTemplateId));
        String repoUrl = applicationTemplateRepDTO.getRepoUrl();
        if (applicationTemplateRepDTO.getOrganizationId() != null) {
            repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;
            repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl;
        }
        applicationTemplateRepDTO.setRepoUrl(repoUrl);
        return applicationTemplateRepDTO;
    }

    @Override
    public PageInfo<ApplicationTemplateRepVO> listByOptions(PageRequest pageRequest, Long organizationId, String searchParam) {

        PageInfo<ApplicationTemplateRepVO> applicationTemplateRepDTOPage = ConvertUtils.convertPage(basePageByOptions(pageRequest, organizationId, searchParam), this::templateDtoToResponse);
        List<ApplicationTemplateRepVO> applicationTemplateRepDTOList = applicationTemplateRepDTOPage.getList();
        applicationTemplateRepDTOList.forEach(this::setAppTemplateRepoUrl);
        applicationTemplateRepDTOPage.setList(applicationTemplateRepDTOList);
        return applicationTemplateRepDTOPage;
    }

//    private void setAppTemplateRepoUrl(List<ApplicationTemplateRepVO> applicationTemplateRepDTOList) {
//        for (ApplicationTemplateRepVO applicationTemplateRepDTO : applicationTemplateRepDTOList) {
//            String repoUrl = applicationTemplateRepDTO.getRepoUrl();
//            if (applicationTemplateRepDTO.getOrganizationId() != null) {
//                repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;
//                repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl;
//            }
//            applicationTemplateRepDTO.setRepoUrl(repoUrl);
//        }
//    }

    private void setAppTemplateRepoUrl(ApplicationTemplateRepVO applicationTemplateRepDTO) {
        String repoUrl = applicationTemplateRepDTO.getRepoUrl();
        if (applicationTemplateRepDTO.getOrganizationId() != null) {
            repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;
            repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl;
        }
        applicationTemplateRepDTO.setRepoUrl(repoUrl);
    }


    @Override
    public void operationApplicationTemplate(GitlabProjectPayload gitlabProjectPayload) {

        ApplicationTemplateDTO applicationTemplateDTO = baseQueryByCode(
                gitlabProjectPayload.getOrganizationId(), gitlabProjectPayload.getPath());

        OrganizationVO organization = iamRepository.queryOrganizationById(gitlabProjectPayload.getOrganizationId());

        GitlabProjectDTO gitlabProjectDO = gitlabRepository.getProjectByName(organization.getCode() + "_template", applicationTemplateDTO.getCode(), gitlabProjectPayload.getUserId());

        if (gitlabProjectDO.getId() == null) {
            gitlabProjectDO = gitlabRepository.createProject(gitlabProjectPayload.getGroupId(),
                    gitlabProjectPayload.getPath(), gitlabProjectPayload.getUserId(), true);
        }

        gitlabProjectPayload.setGitlabProjectId(gitlabProjectDO.getId());

        applicationTemplateDTO.setGitlabProjectId(TypeUtil.objToLong(gitlabProjectPayload.getGitlabProjectId()));
        String applicationDir = gitlabProjectPayload.getType() + System.currentTimeMillis();
        if (applicationTemplateDTO.getCopyFrom() != null) {
            ApplicationTemplateRepVO templateRepDTO = templateDtoToResponse(baseQuery(applicationTemplateDTO.getCopyFrom()));
            //拉取模板
            String repoUrl = templateRepDTO.getRepoUrl();
            String type = templateRepDTO.getCode();
            if (templateRepDTO.getOrganizationId() != null) {
                repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;
                repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl;
                type = MASTER;
            }
            Git git = gitUtil.clone(applicationDir, type, repoUrl);

            UserAttrE userAttrE = userAttrRepository.baseQueryByGitlabUserId(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
            String accessToken = getToken(gitlabProjectPayload, applicationDir, userAttrE);

            UserDTO gitlabUserE = gitlabServiceClientOperator.queryUserById(gitlabProjectPayload.getUserId());
            repoUrl = applicationTemplateDTO.getRepoUrl();
            repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;

            BranchDTO branchDTO = gitlabServiceClientOperator.queryBranch(gitlabProjectDO.getId(), MASTER);
            if (branchDTO.getName() == null) {
                gitUtil.push(
                        git,
                        applicationDir,
                        !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl,
                        gitlabUserE.getUsername(),
                        accessToken);
            }
        } else {
            if (!gitlabRepository.getFile(gitlabProjectDO.getId(), MASTER, README)) {
                gitlabRepository.createFile(gitlabProjectPayload.getGitlabProjectId(),
                        README, README_CONTENT, "ADD README",
                        gitlabProjectPayload.getUserId());
            }
        }
        applicationTemplateDTO.setSynchro(true);
        baseUpdate(applicationTemplateDTO);
    }

    private String getToken(GitlabProjectPayload gitlabProjectPayload, String applicationDir, UserAttrE userAttrE) {
        String accessToken = userAttrE.getGitlabToken();
        if (accessToken == null) {
            accessToken = gitlabRepository.createToken(gitlabProjectPayload.getGitlabProjectId(),
                    applicationDir, gitlabProjectPayload.getUserId());
            userAttrE.setGitlabToken(accessToken);
            userAttrRepository.baseUpdate(userAttrE);
        }
        return accessToken;
    }

    @Override
    public List<ApplicationTemplateRepVO> listAllByOrganizationId(Long organizationId) {
        return baseListByOrganizationId(organizationId).stream()
                .map(this::templateDtoToResponse)
                .peek(this::setAppTemplateRepoUrl)
                .collect(Collectors.toList());
    }

    @Override
    public void checkName(Long organizationId, String name) {
        ApplicationTemplateDTO applicationTemplateDTO = new ApplicationTemplateDTO();
        applicationTemplateDTO.setOrganizationId(organizationId);
        applicationTemplateDTO.setName(name);
        baseCheckName(applicationTemplateDTO);
    }

    @Override
    public void checkCode(Long organizationId, String code) {
        ApplicationTemplateDTO applicationTemplateDTO = new ApplicationTemplateDTO();
        applicationTemplateDTO.setOrganizationId(organizationId);
        applicationTemplateDTO.setCode(code);
        baseCheckCode(applicationTemplateDTO);
    }

    @Override
    public ApplicationTemplateRepVO queryByCode(Long organizationId, String code) {
        return templateDtoToResponse(baseQueryByCode(organizationId, code));
    }


    @Override
    public Boolean applicationTemplateExist(String uuid) {
        return baseCheckTemplateExist(uuid);
    }

    @Override
    @Saga(code = "devops-set-appTemplate-err",
            description = "Devops设置创建应用模板状态失败", inputSchema = "{}")
    public void setAppTemplateErrStatus(String input, Long organizationId) {
        transactionalProducer.apply(
                StartSagaBuilder.newBuilder()
                        .withLevel(ResourceLevel.ORGANIZATION)
                        .withSourceId(organizationId)
                        .withJson(input)
                        .withSagaCode("devops-set-appTemplate-err"),
                builder -> {
                }
        );
    }


    public ApplicationTemplateDTO baseCreate(ApplicationTemplateDTO applicationTemplateDTO) {

        OrganizationDTO organizationDTO = iamServiceClientOperator.queryOrganizationById(applicationTemplateDTO.getOrganizationId());
        applicationTemplateDTO.setRepoUrl(
                organizationDTO.getCode() + "_template" + "/"
                        + applicationTemplateDTO.getCode() + ".git");

        if (applicationTemplateMapper.insert(applicationTemplateDTO) != 1) {
            throw new CommonException("error.insert.appTemplate");
        }
        return applicationTemplateDTO;
    }

    public ApplicationTemplateDTO baseUpdate(ApplicationTemplateDTO applicationTemplateDTO) {
        if (applicationTemplateDTO.getObjectVersionNumber() == null) {
            ApplicationTemplateDTO oldApplicationTemplateDTO = applicationTemplateMapper.selectByPrimaryKey(
                    applicationTemplateDTO.getId());
            applicationTemplateDTO.setObjectVersionNumber(oldApplicationTemplateDTO.getObjectVersionNumber());
        }
        if (applicationTemplateMapper.updateByPrimaryKeySelective(applicationTemplateDTO) != 1) {
            throw new CommonException("error.update.appTemplate");
        }
        return applicationTemplateDTO;
    }

    public void baseDelete(Long appTemplateId) {
        applicationTemplateMapper.deleteByPrimaryKey(appTemplateId);
    }

    public ApplicationTemplateDTO baseQuery(Long appTemplateId) {
        return applicationTemplateMapper.selectByPrimaryKey(appTemplateId);
    }

    public PageInfo<ApplicationTemplateDTO> basePageByOptions(PageRequest pageRequest, Long organizationId, String params) {
        PageInfo<ApplicationTemplateDTO> applicationTemplateDTOPageInfo;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> maps = json.deserialize(params, Map.class);
            if (StringUtils.isEmpty(maps.get(TypeUtil.SEARCH_PARAM))) {
                applicationTemplateDTOPageInfo = PageHelper.startPage(
                        pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationTemplateMapper.listByOrganizationId(
                        organizationId,
                        null,
                        TypeUtil.cast(maps.get(TypeUtil.PARAM))));
            } else {
                applicationTemplateDTOPageInfo = PageHelper.startPage(
                        pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationTemplateMapper.listByOrganizationId(
                        organizationId,
                        TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(maps.get(TypeUtil.PARAM))));
            }
        } else {
            applicationTemplateDTOPageInfo = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationTemplateMapper.listByOrganizationId(organizationId, null, null));
        }
        return applicationTemplateDTOPageInfo;
    }

    public ApplicationTemplateDTO baseQueryByCode(Long organizationId, String code) {
        return applicationTemplateMapper.queryByCode(organizationId, code);
    }

    public List<ApplicationTemplateDTO> baseListByOrganizationId(Long organizationId) {
        return applicationTemplateMapper.listByOrganizationId(
                organizationId, null, null);
    }

    public void baseCheckName(ApplicationTemplateDTO applicationTemplateDTO) {
        if (Arrays.asList(models).contains(applicationTemplateDTO.getName().toLowerCase())) {
            throw new CommonException("error.name.exist");
        }
        if (applicationTemplateMapper.selectOne(applicationTemplateDTO) != null) {
            throw new CommonException("error.name.exist");
        }
    }

    public void baseCheckCode(ApplicationTemplateDTO applicationTemplateDTO) {
        if (Arrays.asList(models).contains(applicationTemplateDTO.getCode().toLowerCase())) {
            throw new CommonException("error.code.exist");
        }
        if (!applicationTemplateMapper.select(applicationTemplateDTO).isEmpty()) {
            throw new CommonException("error.code.exist");
        }
    }

    public Boolean baseCheckTemplateExist(String uuid) {
        ApplicationTemplateDTO applicationTemplateDTO = new ApplicationTemplateDTO();
        applicationTemplateDTO.setUuid(uuid);
        return !applicationTemplateMapper.select(applicationTemplateDTO).isEmpty();
    }

    /**
     * convert VO to DTO
     *
     * @param applicationTemplateVO input vo
     * @return dto
     */
    private ApplicationTemplateDTO templateVoToDto(ApplicationTemplateVO applicationTemplateVO) {
        ApplicationTemplateDTO dto = new ApplicationTemplateDTO();
        BeanUtils.copyProperties(applicationTemplateVO, dto);
        return dto;
    }

    /**
     * convert DTO to response VO
     *
     * @param applicationTemplateDTO input dto
     * @return response vo
     */
    private ApplicationTemplateRepVO templateDtoToResponse(ApplicationTemplateDTO applicationTemplateDTO) {
        ApplicationTemplateRepVO vo = new ApplicationTemplateRepVO();
        BeanUtils.copyProperties(applicationTemplateDTO, vo);
        return vo;
    }
}
