package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsEnvFileErrorVO;
import io.choerodon.devops.app.service.DevopsEnvFileErrorService;
import io.choerodon.devops.app.service.DevopsEnvFileService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.infra.dto.DevopsEnvFileDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsEnvFileMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
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
    private DevopsEnvFileMapper devopsEnvFileMapper;
    @Autowired
    private DevopsEnvFileErrorService devopsEnvFileErrorService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Override
    public List<DevopsEnvFileErrorVO> listByEnvId(Long envId) {
        String gitlabProjectPath = getGitlabUrl(envId);
        if ("".equals(gitlabProjectPath)) {
            return new ArrayList<>();
        }
        List<DevopsEnvFileErrorDTO> devopsEnvFileErrorDTOS = devopsEnvFileErrorService.baseListByEnvId(envId);
        List<DevopsEnvFileErrorVO> devopsEnvFileErrorVOS = ConvertUtils.convertList(devopsEnvFileErrorDTOS, this::dtoToVo);
        devopsEnvFileErrorVOS.forEach(devopsEnvFileErrorVO -> setCommitAndFileUrl(devopsEnvFileErrorVO, gitlabProjectPath));
        return devopsEnvFileErrorVOS;
    }

    @Override
    public Page<DevopsEnvFileErrorVO> pageByEnvId(Long envId, PageRequest pageable) {
        String gitlabProjectPath = getGitlabUrl(envId);
        if ("".equals(gitlabProjectPath)) {
            return new Page<>();
        }
        Page<DevopsEnvFileErrorDTO> devopsEnvFileErrorDTOPageInfo = devopsEnvFileErrorService.basePageByEnvId(envId, pageable);
        Page<DevopsEnvFileErrorVO> devopsEnvFileErrorVOPageInfo = ConvertUtils.convertPage(devopsEnvFileErrorDTOPageInfo, this::dtoToVo);
        devopsEnvFileErrorVOPageInfo.getContent().forEach(devopsEnvFileErrorVO -> setCommitAndFileUrl(devopsEnvFileErrorVO, gitlabProjectPath));
        return devopsEnvFileErrorVOPageInfo;
    }

    private DevopsEnvFileErrorVO dtoToVo(DevopsEnvFileErrorDTO devopsEnvFileErrorDTO) {
        DevopsEnvFileErrorVO devopsEnvFileErrorVO = new DevopsEnvFileErrorVO();
        BeanUtils.copyProperties(devopsEnvFileErrorDTO, devopsEnvFileErrorVO);
        devopsEnvFileErrorVO.setLastUpdateDate(devopsEnvFileErrorDTO.getLastUpdateDate());
        return devopsEnvFileErrorVO;
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
        DevopsEnvFileDTO devopsEnvFileDTO = new DevopsEnvFileDTO();
        devopsEnvFileDTO.setEnvId(envId);
        return devopsEnvFileMapper.select(devopsEnvFileDTO);
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

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void deleteByEnvId(Long envId) {
        DevopsEnvFileDTO devopsEnvFileDTO = new DevopsEnvFileDTO();
        devopsEnvFileDTO.setEnvId(Objects.requireNonNull(envId));
        devopsEnvFileMapper.delete(devopsEnvFileDTO);
    }


    private String getGitlabUrl(Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        if (devopsEnvironmentDTO == null) {
            return "";
        }
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        return String.format("%s%s%s-%s-gitops/%s/tree/",
                gitlabUrl, urlSlash, organizationDTO.getTenantNum(), projectDTO.getCode(), devopsEnvironmentDTO.getCode());
    }

    private void setCommitAndFileUrl(DevopsEnvFileErrorVO devopsEnvFileErrorVO, String gitlabProjectPath) {
        String commitUrl = gitlabProjectPath + devopsEnvFileErrorVO.getCommit();
        String fileUrl = commitUrl + "/" + devopsEnvFileErrorVO.getFilePath();
        devopsEnvFileErrorVO.setCommitUrl(commitUrl);
        devopsEnvFileErrorVO.setFileUrl(fileUrl);
    }
}
