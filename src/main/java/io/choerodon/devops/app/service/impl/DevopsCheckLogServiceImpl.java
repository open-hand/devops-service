package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.zaxxer.hikari.util.UtilityElf;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.AccessLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.kubernetes.CheckLog;
import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.DevopsCheckLogDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCheckLogMapper;
import io.choerodon.devops.infra.util.TypeUtil;


@Service
public class DevopsCheckLogServiceImpl implements DevopsCheckLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCheckLogServiceImpl.class);
    private static final String SUCCESS = "success";
    private static final String FAILED = "failed";
    private static final ExecutorService executorService = new ThreadPoolExecutor(0, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new UtilityElf.DefaultThreadFactory("devops-upgrade", false));

    @Autowired
    private DevopsCheckLogMapper devopsCheckLogMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsProjectService devopsProjectService;

    @Override
    public void checkLog(String version) {
        LOGGER.info("start upgrade task");
        executorService.execute(new UpgradeTask(version));
    }

    private static void printRetryNotice() {
        LOGGER.error("======================================================================================");
        LOGGER.error("Please retry data migration later in choerodon interface after cheorodon-front upgrade");
        LOGGER.error("======================================================================================");
    }


    class UpgradeTask implements Runnable {
        private String version;
        private Long env;

        UpgradeTask(String version) {
            this.version = version;
        }

        UpgradeTask(String version, Long env) {
            this.version = version;
            this.env = env;
        }

        @Override
        public void run() {
            try {
                DevopsCheckLogDTO devopsCheckLogDTO = new DevopsCheckLogDTO();
                List<CheckLog> logs = new ArrayList<>();
                devopsCheckLogDTO.setBeginCheckDate(new Date());
                if ("0.21.0".equals(version)) {
                    LOGGER.info("修复数据开始");
                    syncRoot();
                    syncOrgRoot();
                    LOGGER.info("修复数据完成");
                } else {
                    LOGGER.info("version not matched");
                }

                devopsCheckLogDTO.setLog(JSON.toJSONString(logs));
                devopsCheckLogDTO.setEndCheckDate(new Date());

                devopsCheckLogMapper.insert(devopsCheckLogDTO);
            } catch (Throwable ex) {
                printRetryNotice();
                LOGGER.warn("Exception occurred when applying data migration. The ex is: {}", ex);
            }
        }

        private void syncRoot() {
            LOGGER.info("Start to sync root users to gitlab");
            List<IamUserDTO> rootUsers = baseServiceClientOperator.queryAllRootUsers();

            if (CollectionUtils.isEmpty(rootUsers)) {
                LOGGER.warn("Root users got is null. Please check whether it is right later in cheorodon-front.");
                LOGGER.info("Root user migration is Skipped...");
                return;
            }

            rootUsers.forEach(user -> {
                // 跳过停用的用户
                if (!Boolean.TRUE.equals(user.getEnabled())) {
                    LOGGER.info("Skip disabled user with id {} and name {}.", user.getId(), user.getRealName());
                    return;
                }

                // 跳过创建时同步失败的用户
                UserAttrDTO userAttrDTO = userAttrService.baseQueryById(user.getId());
                if (userAttrDTO == null || userAttrDTO.getGitlabUserId() == null) {
                    LOGGER.warn("User with iamUserId {} is not created successfully, therefore not sync", user.getId());
                    return;
                }

                gitlabServiceClientOperator.assignAdmin(userAttrDTO.getIamUserId(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
                userAttrService.updateAdmin(user.getId(), Boolean.TRUE);
                LOGGER.info("Successfully sync the user with id {} and name {} to the gitlab as an admin.", user.getId(), user.getRealName());
            });

            LOGGER.info("Finish syncing root users to gitlab");
        }

        //同步之前已经是组织管理员的用户的gitLab权限
        private void syncOrgRoot() {
            LOGGER.info("Start to sync organization root users to gitlab");
            //所有组织管理员用户
            List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.queryAllOrgRoot();
            if (CollectionUtils.isEmpty(iamUserDTOS)) {
                LOGGER.warn("Organization Root users got is null. Please check whether it is right later in cheorodon-front.");
                LOGGER.info("Organization Root user migration is Skipped...");
                return;
            }

            Map<Long, List<IamUserDTO>> listMap = iamUserDTOS.stream().collect(Collectors.groupingBy(IamUserDTO::getOrganizationId));
            listMap.forEach((organizationId, iamUserDTOS1) -> {
                LOGGER.info("Start synchronizing organization administrators for project with organization Id :{} ", organizationId);
                synOrgRootByOrgId(organizationId, iamUserDTOS1);
                //还要同步到devops_user表。
                LOGGER.info("Finished synchronizing organization administrators for project with organization Id :{} ", organizationId);
            });
            LOGGER.info("Finish syncing organization root users to gitlab");
        }

        private void synOrgRootByOrgId(Long organizationId, List<IamUserDTO> iamUserDTOS1) {
            List<ProjectDTO> projectDTOS = baseServiceClientOperator.listIamProjectByOrgId(organizationId);
            if (!CollectionUtils.isEmpty(projectDTOS)) {
                projectDTOS.stream().forEach(projectDTO -> {
                    iamUserDTOS1.stream().forEach(iamUserDTO -> {
                        assignGitLabGroupMemeberForOwner(projectDTO, iamUserDTO);
                    });
                });
            }
        }

        private void assignGitLabGroupMemeberForOwner(ProjectDTO projectDTO, IamUserDTO iamUserDTO) {
            UserAttrDTO userAttrDTO = userAttrService.baseQueryById(iamUserDTO.getId());
            DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectDTO.getId());
            if (!Objects.isNull(devopsProjectDTO) && !Objects.isNull(userAttrDTO)) {
                MemberDTO memberDTO = new MemberDTO((TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()))
                        , AccessLevel.OWNER.toValue(), "");
                //添加应用服务的gitlab的owner权限
                gitlabServiceClientOperator.createGroupMember(TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()), memberDTO);
                //添加环境的owner权限
                gitlabServiceClientOperator.createGroupMember(TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()), memberDTO);
                //添加集群配置库的owner,一些旧项目的集群group是采用懒加载的方式创建的,组可能为null
                if (!Objects.isNull(devopsProjectDTO.getDevopsClusterEnvGroupId())) {
                    gitlabServiceClientOperator.createGroupMember(TypeUtil.objToInteger(devopsProjectDTO.getDevopsClusterEnvGroupId()), memberDTO);
                }
            }
        }
    }
}
