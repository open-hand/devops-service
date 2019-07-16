package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_CREATE_GITLAB_TEMPLATE_PROJECT;
import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_SET_APPLICATION_TEMPLATE_ERROR;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.ApplicationTemplateValidator;
import io.choerodon.devops.api.vo.ApplicationTemplateRespVO;
import io.choerodon.devops.api.vo.ApplicationTemplateUpdateDTO;
import io.choerodon.devops.api.vo.ApplicationTemplateVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectVO;
import io.choerodon.devops.app.eventhandler.payload.GitlabProjectPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.ApplicationTemplateDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
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
    private IamService iamService;
    @Autowired
    private GitLabService gitLabService;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private ApplicationTemplateMapper applicationTemplateMapper;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private TransactionalProducer transactionalProducer;


    @Override
    @Saga(code = DEVOPS_CREATE_GITLAB_TEMPLATE_PROJECT,
            description = "devops创建gitlab模板项目", inputSchema = "{}")
    public ApplicationTemplateRespVO create(ApplicationTemplateVO applicationTemplateVO, Long organizationId) {
        ApplicationTemplateValidator.checkApplicationTemplate(applicationTemplateVO);

        ApplicationTemplateDTO applicationTemplateDTO = ConvertUtils.convertObject(applicationTemplateVO, ApplicationTemplateDTO.class);
        baseCheckCode(applicationTemplateDTO);
        baseCheckName(applicationTemplateDTO);

        Integer gitlabGroupId;
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        OrganizationDTO organization = iamService.queryOrganizationById(organizationId);
        applicationTemplateDTO.setOrganizationId(organization.getId());
        applicationTemplateDTO.setSynchro(false);
        applicationTemplateDTO.setFailed(false);

        DevopsProjectVO devopsProjectVO = gitLabService.queryGroupByName(
                organization.getCode() + "_" + TEMPLATE, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (devopsProjectVO == null) {
            DevopsProjectVO devopsProjectVONew = new DevopsProjectVO();
            devopsProjectVONew.initName(organization.getCode() + "_" + TEMPLATE);
            devopsProjectVONew.initPath(organization.getCode() + "_" + TEMPLATE);
            devopsProjectVONew.initVisibility(Visibility.PUBLIC);
            gitlabGroupId = TypeUtil.objToInteger(gitLabService.createGroup(
                    devopsProjectVONew, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())).getDevopsAppGroupId());
        } else {
            gitlabGroupId = TypeUtil.objToInteger(devopsProjectVO.getDevopsAppGroupId());
        }

        baseCreate(applicationTemplateDTO);

        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload();
        gitlabProjectPayload.setGroupId(gitlabGroupId);
        gitlabProjectPayload.setUserId(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        gitlabProjectPayload.setPath(applicationTemplateVO.getCode());
        gitlabProjectPayload.setOrganizationId(organization.getId());
        gitlabProjectPayload.setType(TEMPLATE);

        String input = gson.toJson(gitlabProjectPayload);
        transactionalProducer.apply(
                StartSagaBuilder.newBuilder()
                        .withSagaCode(DEVOPS_CREATE_GITLAB_TEMPLATE_PROJECT)
                        .withJson(input)
                        .withLevel(ResourceLevel.ORGANIZATION)
                        .withSourceId(organizationId),
                builder -> {
                });

        return ConvertUtils.convertObject(applicationTemplateMapper.queryByCode(organization.getId(), applicationTemplateDTO.getCode()), ApplicationTemplateRespVO.class);
    }

    @Override
    public ApplicationTemplateRespVO update(ApplicationTemplateUpdateDTO applicationTemplateUpdateDTO, Long organizationId) {
        ApplicationTemplateDTO templateDTO = new ApplicationTemplateDTO();
        BeanUtils.copyProperties(applicationTemplateUpdateDTO, templateDTO);
        return ConvertUtils.convertObject(baseUpdate(templateDTO), ApplicationTemplateRespVO.class);
    }

    @Override
    public void delete(Long appTemplateId) {
        ApplicationTemplateDTO applicationTemplateDTO = applicationTemplateMapper.selectByPrimaryKey(appTemplateId);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        if (applicationTemplateDTO.getGitlabProjectId() != null) {
            gitLabService.deleteProject(
                    TypeUtil.objToInteger(applicationTemplateDTO.getGitlabProjectId()),
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }
        applicationTemplateMapper.deleteByPrimaryKey(appTemplateId);
    }

    @Override
    public ApplicationTemplateRespVO queryByTemplateId(Long appTemplateId) {
        ApplicationTemplateRespVO applicationTemplateRespVO = ConvertUtils.convertObject(applicationTemplateMapper.selectByPrimaryKey(appTemplateId), ApplicationTemplateRespVO.class);
        setAppTemplateRepoUrl(applicationTemplateRespVO);
        return applicationTemplateRespVO;
    }

    @Override
    public PageInfo<ApplicationTemplateRespVO> listByOptions(PageRequest pageRequest, Long organizationId, String searchParam) {
        PageInfo<ApplicationTemplateRespVO> applicationTemplateRepDTOPage = ConvertUtils.convertPage(basePageByOptions(pageRequest, organizationId, searchParam), ApplicationTemplateRespVO.class);
        applicationTemplateRepDTOPage.getList().forEach(this::setAppTemplateRepoUrl);
        return applicationTemplateRepDTOPage;
    }


    /**
     * 对模板的repoUrl进行操作
     *
     * @param applicationTemplateRespVO 包含repoUrl和gitlabUrl的模板数据
     */
    private void setAppTemplateRepoUrl(ApplicationTemplateRespVO applicationTemplateRespVO) {
        String repoUrl = applicationTemplateRespVO.getRepoUrl();
        // 通过组织id为空来判断是否是系统内置模板。系统内置模板存于数据库的组织id是空的但是存repoUrl是完整的，所以内置模板不需要拼接
        if (applicationTemplateRespVO.getOrganizationId() != null) {
            repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;
            repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl;
        }
        applicationTemplateRespVO.setRepoUrl(repoUrl);
    }


    @Override
    public void operationApplicationTemplate(GitlabProjectPayload gitlabProjectPayload) {

        ApplicationTemplateDTO applicationTemplateDTO = applicationTemplateMapper.queryByCode(
                gitlabProjectPayload.getOrganizationId(), gitlabProjectPayload.getPath());
        OrganizationDTO organization = iamService.queryOrganizationById(gitlabProjectPayload.getOrganizationId());
        GitlabProjectDTO gitlabProjectDTO = gitLabService.getProjectByName(organization.getCode() + "_template", applicationTemplateDTO.getCode(), gitlabProjectPayload.getUserId());

        if (gitlabProjectDTO.getId() == null) {
            gitlabProjectDTO = gitLabService.createProject(gitlabProjectPayload.getGroupId(),
                    gitlabProjectPayload.getPath(), gitlabProjectPayload.getUserId(), true);
        }

        applicationTemplateDTO.setGitlabProjectId(TypeUtil.objToLong(gitlabProjectDTO.getId()));
        String applicationDir = gitlabProjectPayload.getType() + System.currentTimeMillis();
        if (applicationTemplateDTO.getCopyFrom() != null) {
            ApplicationTemplateRespVO templateRepDTO = ConvertUtils.convertObject(applicationTemplateMapper.selectByPrimaryKey(applicationTemplateDTO.getCopyFrom()), ApplicationTemplateRespVO.class);
            //拉取模板
            String repoUrl = templateRepDTO.getRepoUrl();
            String type = templateRepDTO.getCode();
            if (templateRepDTO.getOrganizationId() != null) {
                repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;
                repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl;
                type = MASTER;
            }
            Git git = gitUtil.clone(applicationDir, type, repoUrl);

            UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
            String accessToken = getToken(gitlabProjectPayload, applicationDir, userAttrDTO);

            UserDTO gitlabUserE = gitlabServiceClientOperator.queryUserById(gitlabProjectPayload.getUserId());
            repoUrl = applicationTemplateDTO.getRepoUrl();
            repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;

            BranchDTO branchDTO = gitlabServiceClientOperator.queryBranch(gitlabProjectDTO.getId(), MASTER);
            if (branchDTO.getName() == null) {
                gitUtil.push(
                        git,
                        applicationDir,
                        !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl,
                        gitlabUserE.getUsername(),
                        accessToken);
            }
        } else {
            if (!gitLabService.getFile(gitlabProjectDTO.getId(), MASTER, README)) {
                gitLabService.createFile(gitlabProjectDTO.getId(),
                        README, README_CONTENT, "ADD README",
                        gitlabProjectPayload.getUserId());
            }
        }

        applicationTemplateDTO.setSynchro(true);
        baseUpdate(applicationTemplateDTO);
    }

    private String getToken(GitlabProjectPayload gitlabProjectPayload, String applicationDir, UserAttrDTO userAttrDTO) {
        String accessToken = userAttrDTO.getGitlabToken();
        if (accessToken == null) {
            accessToken = gitLabService.createToken(gitlabProjectPayload.getGitlabProjectId(),
                    applicationDir, gitlabProjectPayload.getUserId());
            userAttrDTO.setGitlabToken(accessToken);
            userAttrService.baseUpdate(userAttrDTO);
        }
        return accessToken;
    }

    @Override
    public List<ApplicationTemplateRespVO> listAllByOrganizationId(Long organizationId) {
        return applicationTemplateMapper.listByOrganizationId(organizationId, null, null)
                .stream()
                .map(a -> ConvertUtils.convertObject(a, ApplicationTemplateRespVO.class))
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
    public ApplicationTemplateRespVO queryByCode(Long organizationId, String code) {
        return ConvertUtils.convertObject(applicationTemplateMapper.queryByCode(organizationId, code), ApplicationTemplateRespVO.class);
    }


    @Override
    public Boolean applicationTemplateExist(String uuid) {
        return baseCheckTemplateExist(uuid);
    }

    @Override
    @Saga(code = DEVOPS_SET_APPLICATION_TEMPLATE_ERROR,
            description = "Devops设置创建应用模板状态失败", inputSchema = "{}")
    public void setAppTemplateErrStatus(String input, Long organizationId) {
        transactionalProducer.apply(
                StartSagaBuilder.newBuilder()
                        .withLevel(ResourceLevel.ORGANIZATION)
                        .withSourceId(organizationId)
                        .withJson(input)
                        .withSagaCode(DEVOPS_SET_APPLICATION_TEMPLATE_ERROR),
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
}
