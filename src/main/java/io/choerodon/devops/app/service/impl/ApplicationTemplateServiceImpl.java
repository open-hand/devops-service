package io.choerodon.devops.app.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.ApplicationTemplateValidator;
import io.choerodon.devops.api.vo.ApplicationTemplateRepVO;
import io.choerodon.devops.api.vo.ApplicationTemplateUpdateDTO;
import io.choerodon.devops.api.vo.iam.entity.ApplicationTemplateE;
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectVO;
import io.choerodon.devops.api.vo.iam.entity.UserAttrE;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabUserE;
import io.choerodon.devops.app.eventhandler.payload.GitlabProjectPayload;
import io.choerodon.devops.app.service.ApplicationTemplateService;
import io.choerodon.devops.domain.application.repository.ApplicationTemplateRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.ApplicationTemplateDTO;
import io.choerodon.devops.infra.dto.gitlab.BranchDO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.enums.Visibility;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.mapper.ApplicationTemplateMapper;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


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
    private IamRepository iamRepository;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private ApplicationTemplateRepository applicationTemplateRepository;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private GitlabUserRepository gitlabUserRepository;
    @Autowired
    private DevopsGitRepository devopsGitRepository;
    @Autowired
    private ApplicationTemplateMapper applicationTemplateMapper;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;

    @Autowired
    private SagaClient sagaClient;


    @Override
    @Saga(code = "devops-create-gitlab-template-project",
            description = "devops创建gitlab模板项目", inputSchema = "{}")
    public ApplicationTemplateRepVO create(io.choerodon.devops.api.vo.ApplicationTemplateDTO applicationTemplateDTO, Long organizationId) {
        ApplicationTemplateValidator.checkApplicationTemplate(applicationTemplateDTO);
        ApplicationTemplateE applicationTemplateE = ConvertHelper.convert(
                applicationTemplateDTO, ApplicationTemplateE.class);
        applicationTemplateRepository.baseCheckCode(applicationTemplateE);
        applicationTemplateRepository.baseCheckName(applicationTemplateE);
        Integer gitlabGroupId;
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        OrganizationVO organization = iamRepository.queryOrganizationById(organizationId);
        applicationTemplateE.initOrganization(organization.getId());
        applicationTemplateE.setSynchro(false);
        applicationTemplateE.setFailed(false);
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
        gitlabProjectPayload.setPath(applicationTemplateDTO.getCode());
        gitlabProjectPayload.setOrganizationId(organization.getId());
        gitlabProjectPayload.setType(TEMPLATE);

        if (applicationTemplateRepository.baseCreate(applicationTemplateE) == null) {
            throw new CommonException("error.applicationTemplate.insert");
        }
        String input = gson.toJson(gitlabProjectPayload);
        sagaClient.startSaga("devops-create-gitlab-template-project", new StartInstanceDTO(input, "", "", ResourceLevel.ORGANIZATION.value(), organizationId));

        return ConvertHelper.convert(applicationTemplateRepository.baseQueryByCode(organization.getId(),
                applicationTemplateDTO.getCode()), ApplicationTemplateRepVO.class);
    }

    @Override
    public ApplicationTemplateRepVO update(ApplicationTemplateUpdateDTO applicationTemplateUpdateDTO, Long organizationId) {
        ApplicationTemplateE applicationTemplateE = ConvertHelper.convert(
                applicationTemplateUpdateDTO, ApplicationTemplateE.class);
        applicationTemplateE.initOrganization(organizationId);
        return ConvertHelper.convert(applicationTemplateRepository.baseUpdate(applicationTemplateE),
                ApplicationTemplateRepVO.class);
    }

    @Override
    public void delete(Long appTemplateId) {
        ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.baseQuery(appTemplateId);
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        if (applicationTemplateE.getGitlabProjectE() != null) {
            gitlabRepository.deleteProject(
                    applicationTemplateE.getGitlabProjectE().getId(),
                    TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        }
        applicationTemplateRepository.baseDelete(appTemplateId);
    }

    @Override
    public ApplicationTemplateRepVO query(Long appTemplateId) {
        ApplicationTemplateRepVO applicationTemplateRepDTO = ConvertHelper.convert(applicationTemplateRepository
                .baseQuery(appTemplateId), ApplicationTemplateRepVO.class);
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
        PageInfo<ApplicationTemplateRepVO> applicationTemplateRepDTOPage = ConvertPageHelper
                .convertPageInfo(applicationTemplateRepository.baseListByOptions(
                        pageRequest, organizationId, searchParam),
                        ApplicationTemplateRepVO.class);
        List<ApplicationTemplateRepVO> applicationTemplateRepDTOList = applicationTemplateRepDTOPage.getList();
        setAppTemplateRepoUrl(applicationTemplateRepDTOList);
        applicationTemplateRepDTOPage.setList(applicationTemplateRepDTOList);
        return applicationTemplateRepDTOPage;
    }

    private void setAppTemplateRepoUrl(List<ApplicationTemplateRepVO> applicationTemplateRepDTOList) {
        for (ApplicationTemplateRepVO applicationTemplateRepDTO : applicationTemplateRepDTOList) {
            String repoUrl = applicationTemplateRepDTO.getRepoUrl();
            if (applicationTemplateRepDTO.getOrganizationId() != null) {
                repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;
                repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl;
            }
            applicationTemplateRepDTO.setRepoUrl(repoUrl);
        }
    }

    @Override
    public void operationApplicationTemplate(GitlabProjectPayload gitlabProjectPayload) {

        ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.baseQueryByCode(
                gitlabProjectPayload.getOrganizationId(), gitlabProjectPayload.getPath());

        OrganizationVO organization = iamRepository.queryOrganizationById(gitlabProjectPayload.getOrganizationId());

        GitlabProjectDTO gitlabProjectDO = gitlabRepository.getProjectByName(organization.getCode() + "_template", applicationTemplateE.getCode(), gitlabProjectPayload.getUserId());

        if (gitlabProjectDO.getId() == null) {
            gitlabProjectDO = gitlabRepository.createProject(gitlabProjectPayload.getGroupId(),
                    gitlabProjectPayload.getPath(), gitlabProjectPayload.getUserId(), true);
        }

        gitlabProjectPayload.setGitlabProjectId(gitlabProjectDO.getId());

        applicationTemplateE.initGitlabProjectE(
                TypeUtil.objToInteger(gitlabProjectPayload.getGitlabProjectId()));
        String applicationDir = gitlabProjectPayload.getType() + System.currentTimeMillis();
        if (applicationTemplateE.getCopyFrom() != null) {
            ApplicationTemplateRepVO templateRepDTO = ConvertHelper.convert(applicationTemplateRepository
                    .baseQuery(applicationTemplateE.getCopyFrom()), ApplicationTemplateRepVO.class);
            //拉取模板
            String repoUrl = templateRepDTO.getRepoUrl();
            String type = templateRepDTO.getCode();
            if (templateRepDTO.getOrganizationId() != null) {
                repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;
                repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl;
                type = MASTER;
            }
            Git git = gitUtil.clone(applicationDir, type, repoUrl);

            UserAttrE userAttrE = userAttrRepository.queryByGitlabUserId(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
            String accessToken = getToken(gitlabProjectPayload, applicationDir, userAttrE);

            GitlabUserE gitlabUserE = gitlabUserRepository.getGitlabUserByUserId(gitlabProjectPayload.getUserId());
            repoUrl = applicationTemplateE.getRepoUrl();
            repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;

            BranchDO branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), MASTER);
            if (branchDO.getName() == null) {
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
        applicationTemplateE.setSynchro(true);
        applicationTemplateRepository.baseUpdate(applicationTemplateE);
    }

    private String getToken(GitlabProjectPayload gitlabProjectPayload, String applicationDir, UserAttrE userAttrE) {
        String accessToken = userAttrE.getGitlabToken();
        if (accessToken == null) {
            accessToken = gitlabRepository.createToken(gitlabProjectPayload.getGitlabProjectId(),
                    applicationDir, gitlabProjectPayload.getUserId());
            userAttrE.setGitlabToken(accessToken);
            userAttrRepository.update(userAttrE);
        }
        return accessToken;
    }

    @Override
    public List<ApplicationTemplateRepVO> list(Long organizationId) {
        List<ApplicationTemplateRepVO> applicationTemplateRepDTOList = ConvertHelper.convertList(
                applicationTemplateRepository.baseListByOrganizationId(organizationId),
                ApplicationTemplateRepVO.class);
        setAppTemplateRepoUrl(applicationTemplateRepDTOList);
        return applicationTemplateRepDTOList;
    }

    @Override
    public void checkName(Long organizationId, String name) {
        ApplicationTemplateE applicationTemplateE = ApplicationTemplateFactory.createApplicationTemplateE();
        applicationTemplateE.initOrganization(organizationId);
        applicationTemplateE.setName(name);
        applicationTemplateRepository.baseCheckName(applicationTemplateE);
    }

    @Override
    public void checkCode(Long organizationId, String code) {
        ApplicationTemplateE applicationTemplateE = ApplicationTemplateFactory.createApplicationTemplateE();
        applicationTemplateE.initOrganization(organizationId);
        applicationTemplateE.setCode(code);
        applicationTemplateRepository.baseCheckCode(applicationTemplateE);
    }

    @Override
    public ApplicationTemplateRepVO queryByCode(Long organizationId, String code) {
        return ConvertHelper.convert(applicationTemplateRepository.baseQueryByCode(organizationId, code), ApplicationTemplateRepVO.class);
    }


    @Override
    public Boolean applicationTemplateExist(String uuid) {
        return applicationTemplateRepository.baseCheckTeamplateExist(uuid);
    }

    @Override
    @Saga(code = "devops-set-appTemplate-err",
            description = "Devops设置创建应用模板状态失败", inputSchema = "{}")
    public void setAppTemplateErrStatus(String input, Long organizationId) {
        sagaClient.startSaga("devops-set-appTemplate-err", new StartInstanceDTO(input, "", "", ResourceLevel.ORGANIZATION.value(), organizationId));
    }


    public ApplicationTemplateDTO baseCreate(ApplicationTemplateDTO applicationTemplateDTO) {

        OrganizationDTO organizationDTO = iamServiceClientOperator.queryOrganizationById(applicationTemplateDTO.getOrganizationId())
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
            if (maps.get(TypeUtil.SEARCH_PARAM).equals("")) {
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

    public Boolean baseCheckTeamplateExist(String uuid) {
        ApplicationTemplateDTO applicationTemplateDTO = new ApplicationTemplateDTO();
        applicationTemplateDTO.setUuid(uuid);
        return !applicationTemplateMapper.select(applicationTemplateDTO).isEmpty();
    }
}
