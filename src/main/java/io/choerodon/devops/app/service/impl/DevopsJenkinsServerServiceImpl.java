package io.choerodon.devops.app.service.impl;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import com.cdancy.jenkins.rest.JenkinsClient;
import com.cdancy.jenkins.rest.domain.plugins.Plugin;
import com.cdancy.jenkins.rest.domain.plugins.Plugins;
import com.cdancy.jenkins.rest.domain.statistics.OverallLoad;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsJenkinsServerStatusCheckResponseVO;
import io.choerodon.devops.api.vo.DevopsJenkinsServerVO;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.api.vo.jenkins.JenkinsPluginInfo;
import io.choerodon.devops.app.service.DevopsJenkinsServerService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DevopsJenkinsServerDTO;
import io.choerodon.devops.infra.enums.DevopsJenkinsServerStatusEnum;
import io.choerodon.devops.infra.enums.jenkins.JenkinsPluginStatusEnum;
import io.choerodon.devops.infra.mapper.DevopsJenkinsServerMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JenkinsClientUtil;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsJenkinsServerServiceImpl implements DevopsJenkinsServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsJenkinsServerServiceImpl.class);

    @Value("${devops.jenkins.plugin.version}")
    private String version;
    @Autowired
    private DevopsJenkinsServerMapper devopsJenkinsServerMapper;
    @Autowired
    private JenkinsClientUtil jenkinsClientUtil;


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
            OverallLoad overallLoad = client.api().statisticsApi().overallLoad();
            if (overallLoad != null) {
                devopsJenkinsServerStatusCheckResponseVO.setSuccess(true);
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
        Page<DevopsJenkinsServerVO> page = PageHelper.doPageAndSort(pageable, () -> devopsJenkinsServerMapper.page(projectId, searchVO));
        if (CollectionUtils.isEmpty(page.getContent())) {
            return page;
        }
        for (DevopsJenkinsServerVO devopsJenkinsServerVO : page.getContent()) {
            try {
                JenkinsClient jenkinsClient = jenkinsClientUtil.getClientByServerId(devopsJenkinsServerVO.getId());
                Plugins plugins = jenkinsClient.api().pluginManagerApi().plugins(3, null);
                if (!CollectionUtils.isEmpty(plugins.plugins())) {
                    Optional<Plugin> optionalPlugin = plugins.plugins().stream().filter(p -> p.shortName().equals("choerodon-integration")).findFirst();
                    JenkinsPluginInfo jenkinsPluginInfo = new JenkinsPluginInfo();
                    if (optionalPlugin.isPresent()) {
                        Plugin plugin = optionalPlugin.get();
                        jenkinsPluginInfo.setVersion(plugin.version());
                        jenkinsPluginInfo.setLastedVersion(version);
                        if (Boolean.TRUE.equals(plugin.active())) {
                            jenkinsPluginInfo.setStatus(StringUtils.compare(plugin.version(), version) < 0
                                    ? JenkinsPluginStatusEnum.UPGRADEABLE.value() : JenkinsPluginStatusEnum.INSTALLED.value());
                        } else {
                            jenkinsPluginInfo.setStatus(JenkinsPluginStatusEnum.DISABLED.value());
                        }
                    } else {
                        jenkinsPluginInfo.setStatus(JenkinsPluginStatusEnum.UNINSTALL.value());
                    }
                    devopsJenkinsServerVO.setJenkinsPluginInfo(jenkinsPluginInfo);
                }
            } catch (Exception e) {
                LOGGER.error("Query jenkins plugin info failed.", e);
            }
        }
        return page;
    }

    @Override
    public DevopsJenkinsServerVO query(Long projectId, Long jenkinsServerId) {
        return ConvertUtils.convertObject(devopsJenkinsServerMapper.selectByPrimaryKey(jenkinsServerId), DevopsJenkinsServerVO.class);
    }

    @Override
    public Boolean checkNameExists(Long projectId, Long jenkinsServerId, String serverName) {
        return devopsJenkinsServerMapper.checkNameExist(projectId, jenkinsServerId, serverName);
    }

    @Override
    public List<DevopsJenkinsServerDTO> listByProjectId(Long projectId) {
        Assert.notNull(projectId, ResourceCheckConstant.DEVOPS_PROJECT_ID_IS_NULL);

        DevopsJenkinsServerDTO devopsJenkinsServerDTO = new DevopsJenkinsServerDTO();
        devopsJenkinsServerDTO.setProjectId(projectId);
        return devopsJenkinsServerMapper.select(devopsJenkinsServerDTO);
    }

    @Override
    public DevopsJenkinsServerDTO queryById(Long id) {
        return devopsJenkinsServerMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<DevopsJenkinsServerDTO> listAll(Long projectId, String status) {
        return devopsJenkinsServerMapper.listAll(projectId, status);
    }

    @Override
    public ResponseEntity<Resource> downloadPlugin() {
        String filename = "choerodon-integration-" + version + ".hpi";
        String fileFolder = "jenkins-plugin";

        HttpHeaders headers = new HttpHeaders();
        headers.add("charset", "utf-8");
        //设置下载文件名
        headers.add("Content-Disposition", "attachment;filename=\"" + filename + "\"");

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileFolder + "/" + filename);
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(inputStream));
    }
}
