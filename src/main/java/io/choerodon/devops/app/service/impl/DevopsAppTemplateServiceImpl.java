package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.xml.soap.Detail;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.DevopsAppTemplateCreateVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsAppTemplateService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.GitLabUserDTO;
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.DevopsAppTemplateStatusEnum;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsAppTemplateMapper;
import io.choerodon.devops.infra.util.*;
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

    @Override
    public Page<DevopsAppTemplateDTO> pageAppTemplate(Long sourceId, String sourceType, String params, PageRequest pageRequest) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        Map<String, Object> searchParamMap = TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM));
        List<String> paramList = TypeUtil.cast(mapParams.get(TypeUtil.PARAMS));
        return PageHelper.doPageAndSort(pageRequest, () -> devopsAppTemplateMapper.pageAppTemplate(sourceId, sourceType, paramList, searchParamMap));
    }

    @Override
    @Transactional
    public void createTemplateOnSite(Long sourceId, String sourceType, DevopsAppTemplateCreateVO appTemplateCreateVO) {
        // 获取平台层 gitlab项目
        GroupDTO group = new GroupDTO();
        String groupName = String.format(GITLAB_GROUP_NAME, "平台层");
        String groupPath = String.format(GITLAB_GROUP_CODE, "site");
        group.setName(groupName);
        group.setPath(groupPath);
//
        LOGGER.info("groupPath:{},adminId:{}", group.getPath(), GitUserNameUtil.getAdminId());
        GroupDTO groupDTO = gitlabServiceClientOperator.queryGroupByName(group.getPath(), GitUserNameUtil.getAdminId());
        if (groupDTO == null) {
            groupDTO = gitlabServiceClientOperator.createGroup(group, GitUserNameUtil.getAdminId());
        }
//        // 创建模板 状态为创建中
        DevopsAppTemplateDTO devopsAppTemplateDTO = new DevopsAppTemplateDTO();
        BeanUtils.copyProperties(appTemplateCreateVO, devopsAppTemplateDTO);
        devopsAppTemplateDTO.setSourceId(0L);
        devopsAppTemplateDTO.setSourceType(ResourceLevel.SITE.value());
        devopsAppTemplateDTO.setStatus(DevopsAppTemplateStatusEnum.CREATING.getType());
        devopsAppTemplateMapper.insertSelective(devopsAppTemplateDTO);
//        // 创建应用服务
        GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.queryProjectByName(
                groupPath,
                devopsAppTemplateDTO.getCode(),
                GitUserNameUtil.getAdminId());
        //创建gitlab 应用 为创建分配developer角色
        if (gitlabProjectDTO.getId() == null) {
            gitlabProjectDTO = gitlabServiceClientOperator.createProject(
                    groupDTO.getId(),
                    devopsAppTemplateDTO.getCode(),
                    GitUserNameUtil.getAdminId(), false);
        }
        UserAttrDTO createByUser = userAttrService.baseQueryById(DetailsHelper.getUserDetails().getUserId());
        gitlabServiceClientOperator.createProjectMember(gitlabProjectDTO.getId(), new MemberDTO(TypeUtil.objToInteger(createByUser.getGitlabUserId()), 30, ""));
        // 导入模板
        ClassPathResource cpr = new ClassPathResource("/app-template/choerodon-golang-template");
        File file = null;
        try {
            file = cpr.getFile();
        } catch (IOException e) {
            throw new CommonException(e.getMessage());
        }

//        InputStream inputStream = this.getClass().getResourceAsStream("/app-template/choerodon-golang-template.zip");
        String workingDirectory = gitUtil.getWorkingDirectory("app-template-import" + File.separator + "site" + File.separator + devopsAppTemplateDTO.getCode());
        File localPathFile = new File(workingDirectory);
        FileUtil.copyDir(file, localPathFile);
        //获取admin的token
        replaceParams(appTemplateCreateVO.getCode(), groupPath, workingDirectory, appTemplateCreateVO.getSelectedTemplateCode(), groupPath, false);
        Git git = gitUtil.initGit(new File(workingDirectory));
        UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(TypeUtil.objToLong(GitUserNameUtil.getAdminId()));
        String pushToken = appServiceService.getToken(gitlabProjectDTO.getId(), workingDirectory, userAttrDTO);
        //push 到远程仓库
        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        String repositoryUrl = repoUrl + groupPath + "/" + appTemplateCreateVO.getCode() + GIT;
        gitUtil.push(git, workingDirectory, "init the template", repositoryUrl, "admin", pushToken);

        FileUtil.deleteDirectory(localPathFile);
        // 回写模板状态
        devopsAppTemplateDTO.setStatus(DevopsAppTemplateStatusEnum.SUCCESS.getType());
        devopsAppTemplateDTO.setGitlabProjectId(TypeUtil.objToLong(gitlabProjectDTO.getId()));
        devopsAppTemplateDTO.setGitlabUrl(repositoryUrl);
        devopsAppTemplateMapper.updateByPrimaryKey(devopsAppTemplateDTO);
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

    public Boolean checkNameAndCode(DevopsAppTemplateDTO appTemplateDTO) {
        Long appTemplateId = appTemplateDTO.getId();
        appTemplateDTO.setId(null);
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
}
