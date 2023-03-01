package io.choerodon.devops.app;

import com.cdancy.jenkins.rest.JenkinsClient;
import com.cdancy.jenkins.rest.domain.system.SystemInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsJenkinsServerStatusCheckResponseVO;
import io.choerodon.devops.api.vo.DevopsJenkinsServerVO;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.infra.dto.DevopsJenkinsServerDTO;
import io.choerodon.devops.infra.enums.DevopsJenkinsServerStatusEnum;
import io.choerodon.devops.infra.mapper.DevopsJenkinsServerMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsJenkinsServerServiceImpl implements DevopsJenkinsServerService {
    @Autowired
    private DevopsJenkinsServerMapper devopsJenkinsServerMapper;

    @Transactional
    @Override
    public DevopsJenkinsServerVO create(Long projectId, DevopsJenkinsServerVO devopsJenkinsServerVO) {
        // 检查名称项目下唯一
        if (checkNameExists(projectId, null, devopsJenkinsServerVO.getName())) {
            throw new CommonException("error.devops.jenkins.server.name.exists");
        }
        devopsJenkinsServerVO.setProjectId(projectId);
        DevopsJenkinsServerDTO devopsJenkinsServerDTO = MapperUtil.resultJudgedInsert(devopsJenkinsServerMapper, ConvertUtils.convertObject(devopsJenkinsServerVO, DevopsJenkinsServerDTO.class), "error.devops.jenkins.server.create");
        devopsJenkinsServerVO.setId(devopsJenkinsServerDTO.getId());
        return devopsJenkinsServerVO;
    }

    @Transactional
    @Override
    public void update(Long projectId, DevopsJenkinsServerVO devopsJenkinsServerVO) {
        // 检查名称项目下唯一
        if (checkNameExists(projectId, devopsJenkinsServerVO.getId(), devopsJenkinsServerVO.getName())) {
            throw new CommonException("error.devops.jenkins.server.name.exists");
        }
        devopsJenkinsServerVO.setProjectId(projectId);
        MapperUtil.resultJudgedUpdateByPrimaryKey(devopsJenkinsServerMapper, ConvertUtils.convertObject(devopsJenkinsServerVO, DevopsJenkinsServerDTO.class), "error.devops.jenkins.server.update");
    }

    @Override
    public DevopsJenkinsServerStatusCheckResponseVO checkConnection(Long projectId, DevopsJenkinsServerVO devopsJenkinsServerVO) {
        DevopsJenkinsServerStatusCheckResponseVO devopsJenkinsServerStatusCheckResponseVO = new DevopsJenkinsServerStatusCheckResponseVO();
        devopsJenkinsServerStatusCheckResponseVO.setSuccess(false);
        try {
            JenkinsClient client = JenkinsClient.builder().endPoint(devopsJenkinsServerVO.getUrl()).credentials(String.format("%s:%s", devopsJenkinsServerVO.getUsername(), devopsJenkinsServerVO.getPassword())).build();
            SystemInfo systemInfo = client.api().systemApi().systemInfo();
            if (systemInfo != null) {
                if (systemInfo.errors().size() == 0) {
                    devopsJenkinsServerStatusCheckResponseVO.setSuccess(true);
                } else {
                    devopsJenkinsServerStatusCheckResponseVO.setMessage(systemInfo.errors().get(0).exceptionName());
                }
            }
        } catch (Exception e) {
            devopsJenkinsServerStatusCheckResponseVO.setMessage(e.getMessage());
            devopsJenkinsServerStatusCheckResponseVO.setSuccess(false);
        }
        return devopsJenkinsServerStatusCheckResponseVO;
    }

    @Transactional
    @Override
    public void enable(Long projectId, Long jenkinsId) {
        DevopsJenkinsServerDTO devopsJenkinsServerDTO = devopsJenkinsServerMapper.selectByPrimaryKey(jenkinsId);
        devopsJenkinsServerDTO.setStatus(DevopsJenkinsServerStatusEnum.ENABLED.getStatus());
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsJenkinsServerMapper, devopsJenkinsServerDTO, "error.devops.jenkins.server.update");
    }

    @Transactional
    @Override
    public void disable(Long projectId, Long jenkinsId) {
        DevopsJenkinsServerDTO devopsJenkinsServerDTO = devopsJenkinsServerMapper.selectByPrimaryKey(jenkinsId);
        devopsJenkinsServerDTO.setStatus(DevopsJenkinsServerStatusEnum.DISABLE.getStatus());
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsJenkinsServerMapper, devopsJenkinsServerDTO, "error.devops.jenkins.server.update");
    }

    @Transactional
    @Override
    public void delete(Long projectId, Long jenkinsId) {
        devopsJenkinsServerMapper.deleteByPrimaryKey(jenkinsId);
    }

    @Override
    public Page<DevopsJenkinsServerVO> pageServer(Long projectId, PageRequest pageable, SearchVO searchVO) {
        return PageHelper.doPageAndSort(pageable, () -> devopsJenkinsServerMapper.page(projectId, searchVO));
    }

    @Override
    public DevopsJenkinsServerVO query(Long projectId, Long jenkinsServerId) {
        return ConvertUtils.convertObject(devopsJenkinsServerMapper.selectByPrimaryKey(jenkinsServerId), DevopsJenkinsServerVO.class);
    }

    @Override
    public Boolean checkNameExists(Long projectId, Long jenkinsServerId, String serverName) {
        return devopsJenkinsServerMapper.checkNameExist(projectId, jenkinsServerId, serverName);
    }
}
