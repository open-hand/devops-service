package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsEnvFileErrorDTO;
import io.choerodon.devops.app.service.DevopsEnvFileService;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileErrorE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileErrorRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/8/10
 * Time: 11:03
 * Description:
 */
@Service
public class DevopsEnvFileServiceImpl implements DevopsEnvFileService {

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Autowired
    private DevopsEnvFileErrorRepository devopsEnvFileErrorRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private IamRepository iamRepository;

    @Override
    public List<DevopsEnvFileErrorDTO> listByEnvId(Long envId) {
        final String gitlabProjectPath = getGitlabUrl(envId);
        List<DevopsEnvFileErrorE> envFileErrorES = devopsEnvFileErrorRepository.listByEnvId(envId);
        envFileErrorES.stream().forEach(t -> setCommitAndFileUrl(t, gitlabProjectPath));
        return ConvertHelper.convertList(envFileErrorES, DevopsEnvFileErrorDTO.class);
    }

    @Override
    public Page<DevopsEnvFileErrorDTO> pageByEnvId(Long envId, PageRequest pageRequest) {
        final String gitlabProjectPath = getGitlabUrl(envId);
        Page<DevopsEnvFileErrorE> envFileErrorES = devopsEnvFileErrorRepository.pageByEnvId(envId, pageRequest);
        envFileErrorES.getContent().stream().forEach(t -> setCommitAndFileUrl(t, gitlabProjectPath));
        return ConvertPageHelper.convertPage(envFileErrorES, DevopsEnvFileErrorDTO.class);
    }

    private String getGitlabUrl(Long envId) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        return String.format("%s%s%s-%s-gitops/%s/tree/",
                gitlabUrl, urlSlash, organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode());
    }

    private void setCommitAndFileUrl(DevopsEnvFileErrorE devopsEnvFileErrorE, String gitlabProjectPath) {
        String commitUrl = gitlabProjectPath + devopsEnvFileErrorE.getCommit();
        String fileUrl = commitUrl + "/" + devopsEnvFileErrorE.getFilePath();
        devopsEnvFileErrorE.setCommitUrl(commitUrl);
        devopsEnvFileErrorE.setFileUrl(fileUrl);
    }
}
