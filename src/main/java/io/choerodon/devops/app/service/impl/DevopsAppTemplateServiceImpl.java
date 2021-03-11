package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.DevopsAppTemplateCreateVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTaskCodeConstants;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsAppTemplateService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.DevopsAppTemplateDTO;
import io.choerodon.devops.infra.dto.DevopsAppTemplatePermissionDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.DevopsAppTemplateCreateTypeEnum;
import io.choerodon.devops.infra.enums.DevopsAppTemplateStatusEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsAppTemplateMapper;
import io.choerodon.devops.infra.mapper.DevopsAppTemplatePermissionMapper;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/3/9
 * @Modified By:
 */
@Service
public class DevopsAppTemplateServiceImpl implements DevopsAppTemplateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsAppTemplateServiceImpl.class);

    private static final String GITLAB_GROUP_CODE = "choerodon-%s-app-template";
    private static final String GITLAB_GROUP_NAME = "choerodon-%s应用模板库";
    private static final String GIT = ".git";
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Autowired
    private DevopsAppTemplateMapper devopsAppTemplateMapper;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private TransactionalProducer transactionalProducer;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsAppTemplatePermissionMapper appTemplatePermissionMapper;

    @Override
    public Page<DevopsAppTemplateDTO> pageAppTemplate(Long sourceId, String sourceType, String params, PageRequest pageRequest) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        Map<String, Object> searchParamMap = TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM));
        List<String> paramList = TypeUtil.cast(mapParams.get(TypeUtil.PARAMS));
        return PageHelper.doPageAndSort(pageRequest, () -> devopsAppTemplateMapper.pageAppTemplate(sourceId, sourceType, paramList, searchParamMap));
    }

    @Override
    @Transactional
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_APP_TEMPLATE,
            description = "Devops创建应用模板", inputSchema = "{}")
    public void createTemplate(Long sourceId, String sourceType, DevopsAppTemplateCreateVO appTemplateCreateVO) {
        DevopsAppTemplateDTO queryDTO = new DevopsAppTemplateDTO(sourceId, sourceType, appTemplateCreateVO.getCode());
        queryDTO.setName(appTemplateCreateVO.getName());
        if (!checkNameAndCode(queryDTO, "name")) {
            throw new CommonException("app.template.name.already.exists");
        }
        if (!checkNameAndCode(queryDTO, "code")) {
            throw new CommonException("app.template.code.already.exists");
        }
        // 创建模板 状态为创建中
        DevopsAppTemplateDTO devopsAppTemplateDTO = new DevopsAppTemplateDTO();
        BeanUtils.copyProperties(appTemplateCreateVO, devopsAppTemplateDTO);
        devopsAppTemplateDTO.setSourceId(0L);
        devopsAppTemplateDTO.setSourceType(ResourceLevel.SITE.value());
        devopsAppTemplateDTO.setStatus(DevopsAppTemplateStatusEnum.CREATING.getType());
        if (devopsAppTemplateMapper.insertSelective(devopsAppTemplateDTO) != 1) {
            throw new CommonException("error.insert.app.template");
        }

        appTemplateCreateVO.setAppTemplateId(devopsAppTemplateDTO.getId());
        transactionalProducer.apply(
                StartSagaBuilder.newBuilder()
                        .withRefType("devopsAppTemplate")
                        .withRefId(devopsAppTemplateDTO.getId().toString())
                        .withSagaCode(SagaTaskCodeConstants.DEVOPS_CREATE_APP_TEMPLATE)
                        .withLevel(ResourceLevel.valueOf(sourceType))
                        .withSourceId(sourceId)
                        .withPayloadAndSerialize(appTemplateCreateVO),
                builder -> {
                });

    }

    @Override
    @Transactional
    public void createTemplateSagaTask(DevopsAppTemplateCreateVO appTemplateCreateVO) {
        DevopsAppTemplateDTO devopsAppTemplateDTO = devopsAppTemplateMapper.selectByPrimaryKey(appTemplateCreateVO.getAppTemplateId());
        if (devopsAppTemplateDTO == null) {
            throw new CommonException("error.get.app.template");
        }
        // 获取平台层 gitlab group
        GroupDTO group = new GroupDTO();
        String groupPath;
        String groupName;
        ResourceLevel level = ResourceLevel.valueOf(devopsAppTemplateDTO.getSourceType());
        switch (level) {
            case SITE: {
                groupName = String.format(GITLAB_GROUP_NAME, "平台层");
                groupPath = String.format(GITLAB_GROUP_CODE, "site");
                break;
            }
            case ORGANIZATION: {
                Tenant tenant = baseServiceClientOperator.queryOrganizationById(devopsAppTemplateDTO.getSourceId());
                groupName = String.format(GITLAB_GROUP_NAME, tenant.getTenantName());
                groupPath = String.format(GITLAB_GROUP_CODE, tenant.getTenantNum());
                break;
            }
            case PROJECT: {
                ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsAppTemplateDTO.getId());
                Tenant tenant = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
                groupPath = String.format("%s-%s", tenant.getTenantNum(), projectDTO.getCode());
                groupName = String.format("%s-%s", tenant.getTenantName(), projectDTO.getName());
                break;
            }
            default:
                throw new CommonException("error.get.resource.level");
        }

        group.setName(groupName);
        group.setPath(groupPath);
        LOGGER.info("groupPath:{},adminId:{}", group.getPath(), GitUserNameUtil.getAdminId());
        GroupDTO groupDTO = gitlabServiceClientOperator.queryGroupByName(group.getPath(), GitUserNameUtil.getAdminId());
        if (groupDTO == null) {
            groupDTO = gitlabServiceClientOperator.createGroup(group, GitUserNameUtil.getAdminId());
        }
        //创建gitlab 应用 为创建分配developer角色
        GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.queryProjectByName(
                groupPath,
                devopsAppTemplateDTO.getCode(),
                GitUserNameUtil.getAdminId());
        if (gitlabProjectDTO.getId() == null) {
            gitlabProjectDTO = gitlabServiceClientOperator.createProject(
                    groupDTO.getId(),
                    devopsAppTemplateDTO.getCode(),
                    GitUserNameUtil.getAdminId(), false);
        }
        UserAttrDTO createByUser = userAttrService.baseQueryById(DetailsHelper.getUserDetails().getUserId());
        gitlabServiceClientOperator.createProjectMember(gitlabProjectDTO.getId(), new MemberDTO(TypeUtil.objToInteger(createByUser.getGitlabUserId()), 30, ""));
        // 导入模板
        String workingDirectory = gitUtil.getWorkingDirectory("app-template-import" + File.separator + groupPath + File.separator + devopsAppTemplateDTO.getCode());
        File localPathFile = new File(workingDirectory);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(TypeUtil.objToLong(GitUserNameUtil.getAdminId()));
        String pushToken = appServiceService.getToken(gitlabProjectDTO.getId(), workingDirectory, userAttrDTO);
        Git git;
        if (appTemplateCreateVO.getCreateType().equals(DevopsAppTemplateCreateTypeEnum.TEMPLATE.getType())) {
            DevopsAppTemplateDTO templateDTO = devopsAppTemplateMapper.selectByPrimaryKey(appTemplateCreateVO.getSelectedTemplateId());
            if (templateDTO.getType().equals("P")) {
                ClassPathResource cpr = new ClassPathResource(String.format("/app-template/%s", templateDTO.getCode()));
                File file = null;
                try {
                    file = cpr.getFile();
                    cpr.getInputStream();
                } catch (IOException e) {
                    throw new CommonException(e.getMessage());
                }
                FileUtil.copyDir(file, localPathFile);
                replaceParams(appTemplateCreateVO.getCode(), groupPath, workingDirectory, templateDTO.getCode(), groupPath, false);
                git = gitUtil.initGit(new File(workingDirectory));
            } else {
                git = gitUtil.cloneRepository(workingDirectory, templateDTO.getGitlabUrl(), pushToken);
            }

        } else {
            git = gitUtil.cloneRepository(workingDirectory, appTemplateCreateVO.getRepoUrl(), appTemplateCreateVO.getToken());
        }
        //获取admin的token
        //push 到远程仓库
        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        String repositoryUrl = repoUrl + groupPath + "/" + appTemplateCreateVO.getCode() + GIT;
        gitUtil.push(git, workingDirectory, "init the template", repositoryUrl, "admin", pushToken);

        FileUtil.deleteDirectory(localPathFile);
        // 回写模板状态
        devopsAppTemplateDTO.setStatus(DevopsAppTemplateStatusEnum.SUCCESS.getType());
        devopsAppTemplateDTO.setGitlabProjectId(TypeUtil.objToLong(gitlabProjectDTO.getId()));
        devopsAppTemplateDTO.setGitlabUrl(repositoryUrl);
        if (devopsAppTemplateMapper.updateByPrimaryKey(devopsAppTemplateDTO) != 1) {
            throw new CommonException("error.update.app.template");
        }
    }

    public void replaceParams(String newServiceCode,
                              String newGroupName,
                              String applicationDir,
                              String oldServiceCode,
                              String oldGroupName,
                              Boolean isGetWorkingDirectory) {
        try {
            File file = isGetWorkingDirectory ? new File(gitUtil.getWorkingDirectory(applicationDir)) : new File(applicationDir);
            Map<String, String> params = new HashMap<>();
            params.put("{{group.name}}", newGroupName);
            params.put("{{service.code}}", newServiceCode);
            params.put("the-oldService-name", oldServiceCode);
            params.put(oldGroupName, newGroupName);
            params.put(oldServiceCode, newServiceCode);
            FileUtil.replaceReturnFile(file, params);
        } catch (Exception e) {
            //删除模板
            gitUtil.deleteWorkingDirectory(applicationDir);
            throw new CommonException(e.getMessage(), e);
        }
    }

    @Override
    public Boolean checkNameAndCode(DevopsAppTemplateDTO appTemplateDTO, String type) {
        Long appTemplateId = appTemplateDTO.getId();
        appTemplateDTO.setId(null);
        if (type.equals("name")) {
            appTemplateDTO.setCode(null);
        } else {
            appTemplateDTO.setName(null);
        }
        DevopsAppTemplateDTO resultDTO = devopsAppTemplateMapper.selectOne(appTemplateDTO);
        if (resultDTO == null) {
            return true;
        } else {
            if (appTemplateId == null) {
                return false;
            } else {
                return appTemplateId.equals(resultDTO.getId());
            }
        }
    }

    @Override
    public void modifyName(Long appTemplateId, String name, Long sourceId, String sourceType) {
        DevopsAppTemplateDTO devopsAppTemplateDTO = new DevopsAppTemplateDTO(appTemplateId, sourceId, sourceType);
        devopsAppTemplateDTO.setName(name);
        if (!checkNameAndCode(devopsAppTemplateDTO, "name")) {
            throw new CommonException("app.template.name.already.exists");
        }
        DevopsAppTemplateDTO resultDTO = devopsAppTemplateMapper.selectByPrimaryKey(appTemplateId);
        resultDTO.setName(name);
        devopsAppTemplateMapper.updateByPrimaryKeySelective(resultDTO);
    }

    @Override
    public void addPermission(Long appTemplateId) {
        DevopsAppTemplatePermissionDTO permissionDTO = new DevopsAppTemplatePermissionDTO(appTemplateId, DetailsHelper.getUserDetails().getUserId());
        if (appTemplatePermissionMapper.selectOne(permissionDTO) == null) {
            if (appTemplatePermissionMapper.insert(permissionDTO) != 1) {
                throw new CommonException("error.insert.app.template.permission");
            }
        }
    }

    @Override
    public void enableAppTemplate(Long appTemplateId) {
        updateStatus(appTemplateId, true);
    }

    @Override
    public void disableAppTemplate(Long appTemplateId) {
        updateStatus(appTemplateId, false);
    }

    @Override
    public void deleteAppTemplate(Long appTemplateId) {
        devopsAppTemplateMapper.deleteByPrimaryKey(appTemplateId);
    }

    private void updateStatus(Long appTemplateId, Boolean enable) {
        DevopsAppTemplateDTO devopsAppTemplateDTO = devopsAppTemplateMapper.selectByPrimaryKey(appTemplateId);
        devopsAppTemplateDTO.setEnable(enable);
        if (devopsAppTemplateMapper.updateByPrimaryKey(devopsAppTemplateDTO) != 1) {
            throw new CommonException("error.update.app.template");
        }
    }
}
