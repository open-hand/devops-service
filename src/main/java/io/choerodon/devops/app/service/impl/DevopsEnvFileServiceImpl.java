package io.choerodon.devops.app.service.impl;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.devops.api.vo.DevopsEnvFileErrorDTO;
import io.choerodon.devops.app.service.DevopsEnvFileService;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileErrorE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileErrorRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;

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
        String gitlabProjectPath = getGitlabUrl(envId);
        List<DevopsEnvFileErrorE> envFileErrorES = devopsEnvFileErrorRepository.listByEnvId(envId);
        envFileErrorES.forEach(t -> setCommitAndFileUrl(t, gitlabProjectPath));
        return ConvertHelper.convertList(envFileErrorES, DevopsEnvFileErrorDTO.class);
    }

    @Override
    public PageInfo<DevopsEnvFileErrorDTO> pageByEnvId(Long envId, PageRequest pageRequest) {
        String gitlabProjectPath = getGitlabUrl(envId);
        PageInfo<DevopsEnvFileErrorE> envFileErrorES = devopsEnvFileErrorRepository.pageByEnvId(envId, pageRequest);
        envFileErrorES.getList().stream().forEach(t -> setCommitAndFileUrl(t, gitlabProjectPath));
        return ConvertPageHelper.convertPageInfo(envFileErrorES, DevopsEnvFileErrorDTO.class);
    }

    private String getGitlabUrl(Long envId) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        ProjectVO projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
        OrganizationVO organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
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
