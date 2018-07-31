package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.devops.domain.application.entity.DevopsBranchE;
import io.choerodon.devops.domain.application.entity.DevopsCheckLogE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.CheckLog;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.ApplicationDO;
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import io.choerodon.devops.infra.mapper.ApplicationMapper;

@Service
public class DevopsCheckLogServiceImpl implements DevopsCheckLogService {

    private static final Integer ADMIN = 1;

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
    @Autowired
    private GitlabServiceClient gitlabServiceClient;
    @Autowired
    private DevopsGitRepository devopsGitRepository;
    @Autowired
    private IamRepository iamRepository;

    @Override
    @Async
    public void checkLog(String version) {
        if (version.equals("0.8")) {
            List<ApplicationDO> applications = applicationMapper.selectAll();
            DevopsCheckLogE devopsCheckLogE = new DevopsCheckLogE();
            devopsCheckLogE.setBeginCheckDate(new Date());
            List<CheckLog> logs = new ArrayList<>();
            applications.parallelStream()
                    .filter(applicationDO ->
                            applicationDO.getGitlabProjectId() != null && applicationDO.getHookId() == null)
                    .forEach(applicationDO -> {
                        syncWebHook(applicationDO, logs);
                        syncBranches(applicationDO, logs);
                    });
            devopsCheckLogE.setLog(JSON.toJSONString(logs));
            devopsCheckLogE.setEndCheckDate(new Date());
            devopsCheckLogRepository.create(devopsCheckLogE);
        }

    }


    private void syncWebHook(ApplicationDO applicationDO, List<CheckLog> logs) {
        CheckLog checkLog = new CheckLog();
        checkLog.setContent("app: " + applicationDO.getName() + " create gitlab webhook");
        try {
            ProjectHook projectHook = ProjectHook.allHook();
            projectHook.setEnableSslVerification(true);
            projectHook.setProjectId(applicationDO.getGitlabProjectId());
            projectHook.setToken(applicationDO.getToken());
            String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
            uri += "devops/webhook";
            projectHook.setUrl(uri);
            applicationDO.setHookId(TypeUtil.objToLong(
                    gitlabRepository.createWebHook(applicationDO.getGitlabProjectId(), ADMIN, projectHook).getId()));
            applicationMapper.updateByPrimaryKey(applicationDO);
            checkLog.setResult("success");
        } catch (Exception e) {
            checkLog.setResult("failed: " + e.getMessage());
        }
        logs.add(checkLog);
    }


    private void syncBranches(ApplicationDO applicationDO, List<CheckLog> logs) {
        CheckLog checkLog = new CheckLog();
        checkLog.setContent("app: " + applicationDO.getName() + " sync branches");
        try {
            Optional<List<BranchDO>> branchDOS = Optional.ofNullable(
                    gitlabServiceClient.listBranches(applicationDO.getGitlabProjectId(), ADMIN).getBody());
            List<String> branchNames =
                    devopsGitRepository.listDevopsBranchesByAppId(applicationDO.getId()).parallelStream()
                            .map(DevopsBranchE::getBranchName).collect(Collectors.toList());
            branchDOS.ifPresent(branchDOS1 -> branchDOS1.parallelStream()
                    .filter(branchDO -> !branchNames.contains(branchDO.getName()))
                    .forEach(branchDO -> {
                        DevopsBranchE newDevopsBranchE = new DevopsBranchE();
                        newDevopsBranchE.initApplicationE(applicationDO.getId());
                        newDevopsBranchE.setLastCommitDate(branchDO.getCommit().getCommittedDate());
                        newDevopsBranchE.setLastCommit(branchDO.getCommit().getId());
                        newDevopsBranchE.setBranchName(branchDO.getName());
                        newDevopsBranchE.setCheckoutCommit(branchDO.getCommit().getId());
                        newDevopsBranchE.setCheckoutDate(branchDO.getCommit().getCommittedDate());
                        newDevopsBranchE.setLastCommitMsg(branchDO.getCommit().getMessage());
                        UserE userE = iamRepository.queryByLoginName(branchDO.getCommit().getAuthorName());
                        newDevopsBranchE.setLastCommitUser(userE.getId());
                        devopsGitRepository.createDevopsBranch(newDevopsBranchE);
                        checkLog.setResult("success");
                    }));
        } catch (Exception e) {
            checkLog.setResult("failed: " + e.getMessage());
        }
        logs.add(checkLog);
    }
}
