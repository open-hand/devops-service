package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.devops.domain.application.entity.DevopsCheckLogE;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.repository.DevopsCheckLogRepository;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.domain.application.valueobject.CheckLog;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.ApplicationDO;
import io.choerodon.devops.infra.mapper.ApplicationMapper;

@Service
public class DevopsCheckLogServiceImpl implements DevopsCheckLogService {

    private static final Integer ADMIN =1 ;

    @Value("${services.gateway.url}")
    private String gatewayUrl;

    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private DevopsCheckLogRepository devopsCheckLogRepository;

    @Override
    @Async
    public void checkLog(String version) {
        if (version.equals("0.7")) {
            List<ApplicationDO> applications = applicationMapper.selectAll();
            DevopsCheckLogE devopsCheckLogE = new DevopsCheckLogE();
            devopsCheckLogE.setBeginCheckDate(new Date());
            List<CheckLog> logs = new ArrayList<>();
            applications.parallelStream().filter(applicationDO -> applicationDO.getGitlabProjectId() != null && applicationDO.getHookId() == null).forEach(applicationDO -> {
                        CheckLog checkLog = new CheckLog();
                        checkLog.setContent("app: " + applicationDO.getName() + " create gitlab webhook");
                        try {
                            syncWebHook(applicationDO);
                            checkLog.setResult("success");
                        } catch (Exception e) {
                            checkLog.setResult("failed: " + e.getMessage());
                        }
                        logs.add(checkLog);
                    }
            );
            devopsCheckLogE.setLog(JSON.toJSONString(logs));
            devopsCheckLogE.setEndCheckDate(new Date());
            devopsCheckLogRepository.create(devopsCheckLogE);
        }

    }


    private void syncWebHook(ApplicationDO applicationDO) {
        ProjectHook projectHook = ProjectHook.allHook();
        projectHook.setEnableSslVerification(true);
        projectHook.setProjectId(applicationDO.getGitlabProjectId());
        projectHook.setToken(applicationDO.getToken());
        String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
        uri += "devops/webhook";
        projectHook.setUrl(uri);
        applicationDO.setHookId(TypeUtil.objToLong(gitlabRepository.createWebHook(applicationDO.getGitlabProjectId(), ADMIN, projectHook).getId()));
        applicationMapper.updateByPrimaryKey(applicationDO);
    }

}
