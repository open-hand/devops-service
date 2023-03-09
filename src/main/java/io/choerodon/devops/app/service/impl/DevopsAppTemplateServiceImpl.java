package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.DevopsAppTemplateCreateTypeEnum;
import io.choerodon.devops.infra.enums.DevopsAppTemplateStatusEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsAppTemplateMapper;
import io.choerodon.devops.infra.mapper.DevopsAppTemplatePermissionMapper;
import io.choerodon.devops.infra.util.FileUtil;
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

    private static final String GITLAB_GROUP_CODE = "choerodon_%s-app-template";
    private static final String GITLAB_GROUP_NAME = "choerodon_%s应用模板库";
    private static final String DEVOPS_APP_TEMPLATE_NAME_ALREADY_EXISTS = "devops.app.template.name.already.exists";
    private static final String DEVOPS_APP_TEMPLATE_CODE_ALREADY_EXISTS = "devops.app.template.code.already.exists";
    private static final String DEVOPS_ERROR_INSERT_APP_TEMPLATE = "devops.devops.insert.app.template";
    private static final String DEVOPS_ERROR_INSERT_APP_TEMPLATE_PERMISSION = "devops.devops.insert.app.template.permission";
    private static final String DEVOPS_ERROR_GET_APP_TEMPLATE = "devops.get.app.template";
    private static final String DEVOPS_ERROR_GET_RESOURCE_LEVEL = "devops.get.resource.level";
    private static final String DEVOPS_ERROR_UPDATE_APP_TEMPLATE = "devops.update.app.template";
    private static final String DEVOPS_APP_TEMPLATE_IS_STATUS = "devops.app.template.is.status";

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
        return PageHelper.doPage(pageRequest, () -> devopsAppTemplateMapper.pageAppTemplate(sourceId, sourceType, DetailsHelper.getUserDetails().getUserId(), paramList, searchParamMap));
    }

    @Override
    public List<DevopsAppTemplateDTO> listAppTemplate(Long sourceId, String sourceType, String selectedLevel, String param) {
        if (selectedLevel.equals(ResourceLevel.SITE.value())) {
            return devopsAppTemplateMapper.listAppTemplate(0L, ResourceLevel.SITE.value(), param);
        } else {
            if (sourceType.equals(ResourceLevel.PROJECT.value())) {
                sourceType = ResourceLevel.ORGANIZATION.value();
                sourceId = baseServiceClientOperator.queryIamProjectBasicInfoById(sourceId).getOrganizationId();
            }
            return devopsAppTemplateMapper.listAppTemplate(sourceId, sourceType, param);
        }
    }

    @Override
    @Transactional
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_CREATE_APP_TEMPLATE,
            description = "Devops创建应用模板", inputSchema = "{}")
    public void createTemplate(Long sourceId, String sourceType, DevopsAppTemplateCreateVO appTemplateCreateVO) {
        DevopsAppTemplateDTO devopsAppTemplateDTO = new DevopsAppTemplateDTO();
        BeanUtils.copyProperties(appTemplateCreateVO, devopsAppTemplateDTO);
        devopsAppTemplateDTO.setSourceId(sourceId);
        devopsAppTemplateDTO.setSourceType(sourceType);
        if (!checkNameAndCode(devopsAppTemplateDTO, "name")) {
            throw new CommonException(DEVOPS_APP_TEMPLATE_NAME_ALREADY_EXISTS);
        }
        if (!checkNameAndCode(devopsAppTemplateDTO, "code")) {
            throw new CommonException(DEVOPS_APP_TEMPLATE_CODE_ALREADY_EXISTS);
        }
        // 创建模板 状态为创建中
        devopsAppTemplateDTO.setStatus(DevopsAppTemplateStatusEnum.CREATING.getType());
        devopsAppTemplateDTO.setType("C");
        if (devopsAppTemplateMapper.insertSelective(devopsAppTemplateDTO) != 1) {
            throw new CommonException(DEVOPS_ERROR_INSERT_APP_TEMPLATE);
        }

        // 更新权限
        DevopsAppTemplatePermissionDTO permissionDTO = new DevopsAppTemplatePermissionDTO(devopsAppTemplateDTO.getId(), DetailsHelper.getUserDetails().getUserId());
        if (appTemplatePermissionMapper.selectOne(permissionDTO) == null) {
            if (appTemplatePermissionMapper.insert(permissionDTO) != 1) {
                throw new CommonException(DEVOPS_ERROR_INSERT_APP_TEMPLATE_PERMISSION);
            }
        }

        appTemplateCreateVO.setAppTemplateId(devopsAppTemplateDTO.getId());
        appTemplateCreateVO.setCreatorId(DetailsHelper.getUserDetails().getUserId());
        transactionalProducer.apply(
                StartSagaBuilder.newBuilder()
                        .withRefType("devopsAppTemplate")
                        .withRefId(devopsAppTemplateDTO.getId().toString())
                        .withSagaCode(SagaTaskCodeConstants.DEVOPS_CREATE_APP_TEMPLATE)
                        .withLevel(ResourceLevel.valueOf(sourceType.toUpperCase()))
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
            throw new CommonException(DEVOPS_ERROR_GET_APP_TEMPLATE);
        }
        // 获取平台层 gitlab group
        GroupDTO group = new GroupDTO();
        String groupPath;
        String groupName;
        if (devopsAppTemplateDTO.getSourceType().equals(ResourceLevel.SITE.value())) {
            groupName = String.format(GITLAB_GROUP_NAME, "平台层");
            groupPath = String.format(GITLAB_GROUP_CODE, "site");
        } else if (devopsAppTemplateDTO.getSourceType().equals(ResourceLevel.ORGANIZATION.value())) {
            Tenant tenant = baseServiceClientOperator.queryOrganizationById(devopsAppTemplateDTO.getSourceId());
            groupName = String.format(GITLAB_GROUP_NAME, tenant.getTenantName());
            groupPath = String.format(GITLAB_GROUP_CODE, tenant.getTenantNum());
        } else {
            throw new CommonException(DEVOPS_ERROR_GET_RESOURCE_LEVEL);
        }
        group.setName(groupName);
        group.setPath(groupPath);
        UserAttrDTO adminUserAttrDTO = userAttrService.queryGitlabAdminByIamId();
        Integer gitlabAdminUserId = TypeUtil.objToInteger(adminUserAttrDTO.getGitlabUserId());
        LOGGER.info("groupPath:{},adminId:{}", group.getPath(), gitlabAdminUserId);
        GroupDTO groupDTO = gitlabServiceClientOperator.queryGroupByName(group.getPath(), gitlabAdminUserId);
        if (groupDTO == null) {
            groupDTO = gitlabServiceClientOperator.createGroup(group, gitlabAdminUserId);
        }
        //创建gitlab 应用 为创建分配developer角色
        GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.queryProjectByName(
                groupPath,
                devopsAppTemplateDTO.getCode(),
                gitlabAdminUserId,
                false);
        if (gitlabProjectDTO.getId() == null) {
            gitlabProjectDTO = gitlabServiceClientOperator.createProject(
                    groupDTO.getId(),
                    devopsAppTemplateDTO.getCode(),
                    gitlabAdminUserId, false);
        }
        UserAttrDTO createByUser = userAttrService.baseQueryById(appTemplateCreateVO.getCreatorId());
        gitlabServiceClientOperator.createProjectMember(gitlabProjectDTO.getId(), new MemberDTO(TypeUtil.objToInteger(createByUser.getGitlabUserId()), 30, ""));
        // 导入模板
        String workingDirectory = gitUtil.getWorkingDirectory("app-template-import" + File.separator + groupPath + File.separator + devopsAppTemplateDTO.getCode());
        File localPathFile = new File(workingDirectory);
        String pushToken = appServiceService.getToken(gitlabProjectDTO.getId(), workingDirectory, adminUserAttrDTO);
        Git git;
        if (appTemplateCreateVO.getCreateType().equals(DevopsAppTemplateCreateTypeEnum.TEMPLATE.getType())) {
            DevopsAppTemplateDTO templateDTO = devopsAppTemplateMapper.selectByPrimaryKey(appTemplateCreateVO.getSelectedTemplateId());
            if (templateDTO.getType().equals("P")) {
                ClassPathResource cpr = new ClassPathResource(String.format("/app-template/%s", templateDTO.getCode()) + ".zip");
                File zipFile = null;
                try {
                    InputStream inputStream = cpr.getInputStream();
                    zipFile = new File(workingDirectory + ".zip");
                    FileUtils.copyInputStreamToFile(cpr.getInputStream(), zipFile);
                    inputStream.close();
                    FileUtil.unpack(zipFile, localPathFile);
                } catch (IOException e) {
                    throw new CommonException(e.getMessage());
                } finally {
                    if (zipFile != null) {
                        FileUtil.deleteFile(zipFile);
                    }
                }
                git = gitUtil.initGit(new File(workingDirectory));
            } else {
                git = gitUtil.cloneRepository(localPathFile, templateDTO.getGitlabUrl(), pushToken);
            }
            appServiceService.replaceParams(appTemplateCreateVO.getCode(), groupPath, workingDirectory, templateDTO.getCode(), getTemplateGroupPath(appTemplateCreateVO.getSelectedTemplateId()), false);
        } else {
            git = gitUtil.cloneRepository(localPathFile, appTemplateCreateVO.getRepoUrl(), appTemplateCreateVO.getToken());
        }
        //push 到远程仓库
        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        String repositoryUrl = repoUrl + groupPath + "/" + appTemplateCreateVO.getCode() + GIT;
        gitUtil.push(git, workingDirectory, "init the template", repositoryUrl, "admin", pushToken,false);

        FileUtil.deleteDirectory(localPathFile);
        // 回写模板状态
        devopsAppTemplateDTO.setStatus(DevopsAppTemplateStatusEnum.SUCCESS.getType());
        devopsAppTemplateDTO.setGitlabProjectId(TypeUtil.objToLong(gitlabProjectDTO.getId()));
        devopsAppTemplateDTO.setGitlabUrl(repositoryUrl);
        if (devopsAppTemplateMapper.updateByPrimaryKey(devopsAppTemplateDTO) != 1) {
            throw new CommonException(DEVOPS_ERROR_UPDATE_APP_TEMPLATE);
        }
    }

    @Override
    public Boolean checkNameAndCode(DevopsAppTemplateDTO appTemplateDTO, String type) {
        DevopsAppTemplateDTO queryDTO = new DevopsAppTemplateDTO();
        BeanUtils.copyProperties(appTemplateDTO, queryDTO);
        Long appTemplateId = queryDTO.getId();
        queryDTO.setId(null);
        if (type.equals("name")) {
            queryDTO.setCode(null);
        } else {
            queryDTO.setName(null);
        }
        DevopsAppTemplateDTO resultDTO = devopsAppTemplateMapper.selectOne(queryDTO);
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
            throw new CommonException(DEVOPS_APP_TEMPLATE_NAME_ALREADY_EXISTS);
        }
        DevopsAppTemplateDTO resultDTO = devopsAppTemplateMapper.selectByPrimaryKey(appTemplateId);
        resultDTO.setName(name);
        devopsAppTemplateMapper.updateByPrimaryKeySelective(resultDTO);
    }

    @Override
    @Transactional
    public void addPermission(Long appTemplateId) {
        DevopsAppTemplateDTO appTemplateDTO = devopsAppTemplateMapper.selectByPrimaryKey(appTemplateId);
        DevopsAppTemplatePermissionDTO permissionDTO = new DevopsAppTemplatePermissionDTO(appTemplateId, DetailsHelper.getUserDetails().getUserId());
        if (appTemplatePermissionMapper.selectOne(permissionDTO) == null) {
            if (appTemplatePermissionMapper.insert(permissionDTO) != 1) {
                throw new CommonException(DEVOPS_ERROR_INSERT_APP_TEMPLATE_PERMISSION);
            }
        }
        UserAttrDTO createByUser = userAttrService.baseQueryById(DetailsHelper.getUserDetails().getUserId());
        gitlabServiceClientOperator.createProjectMember(TypeUtil.objToInteger(appTemplateDTO.getGitlabProjectId()), new MemberDTO(TypeUtil.objToInteger(createByUser.getGitlabUserId()), 30, ""));
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
    @Transactional
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_DELETE_APP_TEMPLATE,
            description = "Devops删除应用模板", inputSchema = "{}")
    public void deleteAppTemplate(Long sourceId, String sourceType, Long appTemplateId) {
        DevopsAppTemplateDTO devopsAppTemplateDTO = devopsAppTemplateMapper.selectByPrimaryKey(appTemplateId);
        if (devopsAppTemplateDTO.getEnable() && !devopsAppTemplateDTO.getStatus().equals(DevopsAppTemplateStatusEnum.FAILED.getType())) {
            throw new CommonException(DEVOPS_APP_TEMPLATE_IS_STATUS);
        }
        devopsAppTemplateDTO.setStatus(DevopsAppTemplateStatusEnum.CREATING.getType());
        devopsAppTemplateMapper.updateByPrimaryKeySelective(devopsAppTemplateDTO);
        transactionalProducer.apply(
                StartSagaBuilder.newBuilder()
                        .withRefType("devops-app-template")
                        .withRefId(appTemplateId.toString())
                        .withSagaCode(SagaTaskCodeConstants.DEVOPS_DELETE_APP_TEMPLATE)
                        .withLevel(ResourceLevel.valueOf(sourceType.toUpperCase()))
                        .withSourceId(sourceId)
                        .withPayloadAndSerialize(appTemplateId),
                builder -> {
                });
    }

    @Transactional
    public void deleteAppTemplateSagaTask(Long appTemplateId) {
        DevopsAppTemplateDTO devopsAppTemplateDTO = devopsAppTemplateMapper.selectByPrimaryKey(appTemplateId);
        devopsAppTemplateMapper.deleteByPrimaryKey(appTemplateId);
        appTemplatePermissionMapper.delete(new DevopsAppTemplatePermissionDTO(appTemplateId, null));
        UserAttrDTO userAttrDTO = userAttrService.queryGitlabAdminByIamId();
        if (devopsAppTemplateDTO != null && devopsAppTemplateDTO.getGitlabProjectId() != null) {
            gitlabServiceClientOperator.deleteProjectById(TypeUtil.objToInteger(devopsAppTemplateDTO.getGitlabProjectId()), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }
    }

    private void updateStatus(Long appTemplateId, Boolean enable) {
        DevopsAppTemplateDTO devopsAppTemplateDTO = devopsAppTemplateMapper.selectByPrimaryKey(appTemplateId);
        devopsAppTemplateDTO.setEnable(enable);
        if (devopsAppTemplateMapper.updateByPrimaryKey(devopsAppTemplateDTO) != 1) {
            throw new CommonException(DEVOPS_ERROR_UPDATE_APP_TEMPLATE);
        }
    }

    @Override
    public String getTemplateGroupPath(Long appTemplateId) {
        DevopsAppTemplateDTO devopsAppTemplateDTO = devopsAppTemplateMapper.selectByPrimaryKey(appTemplateId);
        if (devopsAppTemplateDTO == null) {
            throw new CommonException(DEVOPS_ERROR_GET_APP_TEMPLATE);
        }
        if (devopsAppTemplateDTO.getSourceType().equals(ResourceLevel.SITE.value())) {
            return String.format(GITLAB_GROUP_CODE, "site");
        } else if (devopsAppTemplateDTO.getSourceType().equals(ResourceLevel.ORGANIZATION.value())) {
            Tenant tenant = baseServiceClientOperator.queryOrganizationById(devopsAppTemplateDTO.getSourceId());
            return String.format(GITLAB_GROUP_CODE, tenant.getTenantNum());
        } else {
            throw new CommonException(DEVOPS_ERROR_GET_RESOURCE_LEVEL);
        }
    }

    @Override
    public DevopsAppTemplateDTO queryAppTemplateById(Long appTemplateId) {
        return devopsAppTemplateMapper.selectByPrimaryKey(appTemplateId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAppTemplateStatus(Long appTemplateId) {
        DevopsAppTemplateDTO appTemplateDTO = devopsAppTemplateMapper.selectByPrimaryKey(appTemplateId);
        appTemplateDTO.setStatus(DevopsAppTemplateStatusEnum.FAILED.getType());
        devopsAppTemplateMapper.updateByPrimaryKeySelective(appTemplateDTO);
    }

    @Override
    public void updateAppTemplate(Long appTemplateId, String name) {
        DevopsAppTemplateDTO appTemplateDTO = devopsAppTemplateMapper.selectByPrimaryKey(appTemplateId);
        appTemplateDTO.setName(name);
        if (!checkNameAndCode(appTemplateDTO, "name")) {
            throw new CommonException(DEVOPS_APP_TEMPLATE_NAME_ALREADY_EXISTS);
        }
        if (devopsAppTemplateMapper.updateByPrimaryKeySelective(appTemplateDTO) != 1) {
            throw new CommonException(DEVOPS_ERROR_UPDATE_APP_TEMPLATE);
        }
    }
}
