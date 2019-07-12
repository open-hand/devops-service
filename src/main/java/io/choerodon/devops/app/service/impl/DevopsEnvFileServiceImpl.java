package io.choerodon.devops.app.service.impl;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsEnvFileErrorVO;
import io.choerodon.devops.api.vo.DevopsEnvFileVO;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileErrorE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.app.service.DevopsEnvFileService;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileErrorRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
import io.choerodon.devops.infra.dto.DevopsEnvFileDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvFileMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Creator: Runge
 * Date: 2018/8/10
 * Time: 11:03
 * Description:
 */
@Service
public class DevopsEnvFileServiceImpl implements DevopsEnvFileService {

    @Autowired
    DevopsEnvFileMapper devopsEnvFileMapper;
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Autowired
    private DevopsEnvFileErrorRepository devopsEnvFileErrorRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private IamRepository iamRepository;

    @Override
    public List<DevopsEnvFileErrorVO> listByEnvId(Long envId) {
        String gitlabProjectPath = getGitlabUrl(envId);
        List<DevopsEnvFileErrorE> envFileErrorES = devopsEnvFileErrorRepository.baseListByEnvId(envId);
        envFileErrorES.forEach(t -> setCommitAndFileUrl(t, gitlabProjectPath));
        return ConvertHelper.convertList(envFileErrorES, DevopsEnvFileErrorVO.class);
    }

    @Override
    public PageInfo<DevopsEnvFileErrorVO> pageByEnvId(Long envId, PageRequest pageRequest) {
        String gitlabProjectPath = getGitlabUrl(envId);
        PageInfo<DevopsEnvFileErrorE> envFileErrorES = devopsEnvFileErrorRepository.basePageByEnvId(envId, pageRequest);
        envFileErrorES.getList().stream().forEach(t -> setCommitAndFileUrl(t, gitlabProjectPath));
        return ConvertPageHelper.convertPageInfo(envFileErrorES, DevopsEnvFileErrorVO.class);
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

    @Override
    public DevopsEnvFileDTO baseCreate(DevopsEnvFileDTO devopsEnvFileDTO) {
        if (devopsEnvFileMapper.insert(devopsEnvFileDTO) != 1) {
            throw new CommonException("error.env.file.create");
        }
        return devopsEnvFileDTO;
    }

    @Override
    public List<DevopsEnvFileDTO> baseListByEnvId(Long envId) {
        DevopsEnvFileDTO devopsEnvFileDO = new DevopsEnvFileDTO();
        devopsEnvFileDO.setEnvId(envId);
        return devopsEnvFileMapper.select(devopsEnvFileDO);
    }

    @Override
    public DevopsEnvFileDTO baseQueryByEnvAndPath(Long envId, String path) {
        DevopsEnvFileDTO devopsEnvFileDTO = new DevopsEnvFileDTO();
        devopsEnvFileDTO.setEnvId(envId);
        devopsEnvFileDTO.setFilePath(path);
        return devopsEnvFileMapper.selectOne(devopsEnvFileDTO);
    }

    @Override
    public DevopsEnvFileDTO baseQueryByEnvAndPathAndCommit(Long envId, String path, String commit) {
        DevopsEnvFileDTO devopsEnvFileDTO = new DevopsEnvFileDTO();
        devopsEnvFileDTO.setEnvId(envId);
        devopsEnvFileDTO.setFilePath(path);
        devopsEnvFileDTO.setDevopsCommit(commit);
        return devopsEnvFileMapper.selectOne(devopsEnvFileDTO);
    }

    @Override
    public DevopsEnvFileVO baseQueryByEnvAndPathAndCommits(Long envId, String path, List<String> commits) {
        DevopsEnvFileVO devopsEnvFileVO = new DevopsEnvFileVO();
        BeanUtils.copyProperties(devopsEnvFileMapper.queryByEnvAndPathAndCommits(envId, path, commits), devopsEnvFileVO);
        return devopsEnvFileVO;
    }

    @Override
    public void baseUpdate(DevopsEnvFileDTO devopsEnvFileDTO) {
        devopsEnvFileDTO = devopsEnvFileMapper.selectByPrimaryKey(devopsEnvFileDTO.getId());
        devopsEnvFileDTO.setDevopsCommit(devopsEnvFileDTO.getDevopsCommit());
        devopsEnvFileDTO.setAgentCommit(devopsEnvFileDTO.getAgentCommit());
        devopsEnvFileMapper.updateByPrimaryKeySelective(devopsEnvFileDTO);
    }

    @Override
    public void baseDelete(DevopsEnvFileDTO devopsEnvFileDTO) {
        devopsEnvFileMapper.delete(devopsEnvFileDTO);
    }

    @Override
    public List<DevopsEnvFileDTO> baseListByEnvIdAndPath(Long envId, String path) {
        DevopsEnvFileDTO devopsEnvFileDTO = new DevopsEnvFileDTO();
        devopsEnvFileDTO.setEnvId(envId);
        devopsEnvFileDTO.setFilePath(path);
        return devopsEnvFileMapper.select(devopsEnvFileDTO);
    }
}
