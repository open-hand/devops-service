package io.choerodon.devops.infra.util;

import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;

import static io.choerodon.devops.infra.constant.GitOpsConstants.NEW_LINE;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.app.service.CiTemplateJobBusService;
import io.choerodon.devops.app.service.CiTemplateStepBusService;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;

/**
 * 帮助构建用户的错误信息
 *
 * @author zmf
 * @since 2021/1/21
 */
public class UserSyncErrorBuilder {
    private StringBuilder stringBuilder;

    public UserSyncErrorBuilder() {
        buildHeader();
    }

    private void buildHeader() {
        stringBuilder = new StringBuilder();
        stringBuilder.append("userId").append(BaseConstants.Symbol.COMMA).append("realName").append(BaseConstants.Symbol.COMMA).append("loginName").append(BaseConstants.Symbol.COMMA).append("errorMessage").append(NEW_LINE);
    }

    public UserSyncErrorBuilder addErrorUser(Long userId, String userRealName, String loginName, String errorMessage) {
        stringBuilder.append(userId)
                .append(BaseConstants.Symbol.COMMA)
                .append(userRealName)
                .append(BaseConstants.Symbol.COMMA)
                .append(loginName)
                .append(BaseConstants.Symbol.COMMA)
                .append(errorMessage)
                .append(NEW_LINE);
        return this;
    }

    public String build() {
        return stringBuilder.toString();
    }

    @Component
    public static class PipelineTemplateUtils {

        public static final String JOB = "job";
        public static final String STEP = "step";
        public static ThreadLocal threadLocal = new ThreadLocal();


        @Autowired
        private BaseServiceClientOperator baseServiceClientOperator;

        @Autowired
        private CiTemplateJobBusService ciTemplateJobBusService;

        @Autowired
        private CiTemplateStepBusService ciTemplateStepBusService;


        @Nullable
        public Long getOrganizationId(Long sourceId, String sourceType) {
            Long organizationId = null;
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(sourceType, ResourceLevel.PROJECT.value())) {
                ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(sourceId);
                organizationId = projectDTO != null ? projectDTO.getOrganizationId() : null;
            }
            return organizationId;
        }

        public String generateRandomName(String type, Long projectId, String name) {
            String newName = null;
            do {
                newName = name + BaseConstants.Symbol.LOWER_LINE + ThreadLocalRandom.current().nextInt(10000, 99999);
            } while (checkName(type, projectId, newName));
            return newName;
        }

        private boolean checkName(String type, Long projectId, String newName) {
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(type, JOB)) {
                return ciTemplateJobBusService.checkName(projectId, newName);
            } else if (StringUtils.equalsIgnoreCase(type, STEP)) {
                return ciTemplateStepBusService.checkName(projectId, newName);
            } else {
                throw new CommonException("error.check.name");
            }
        }

        public void checkAccess(Long sourceId, String sourceType) {
            if (baseServiceClientOperator.isRoot(DetailsHelper.getUserDetails().getUserId())) {
                return;
            }
            // 如果sourceId为0，校验用户是有有平台管理员角色
            CustomUserDetails userDetails = DetailsHelper.getUserDetails();
            if (Boolean.TRUE.equals(userDetails.getAdmin())) {
                return;
            }
            if (sourceId == 0) {
                if (!baseServiceClientOperator.checkSiteAccess(userDetails.getUserId())) {
                    throw new CommonException("error.no.permission.to.do.operation");
                }
            } else {
                // 如果sourceId不为0，校验用户是否有resourceId对应的组织管理权限
                switch (ResourceLevel.valueOf(sourceType.toUpperCase())) {
                    case ORGANIZATION:
                        if (!baseServiceClientOperator.isOrganzationRoot(userDetails.getUserId(), sourceId)) {
                            throw new CommonException("error.no.permission.to.do.operation");
                        }
                        break;
                    case PROJECT:
                        if (!baseServiceClientOperator.isOrganzationRoot(userDetails.getUserId(), sourceId)
                                && !baseServiceClientOperator.isProjectOwner(userDetails.getUserId(), sourceId)) {
                            throw new CommonException("error.no.permission.to.do.operation");
                        }
                        break;
                    default:
                        break;
                }

            }
        }
    }

    public static final class TemplateJobTypeUtils {
        private static final String CHART = "chart";
        private static final String API_TEST = "apiTest";
        private static final String AUDIT = "audit";
        private static final String DEPLOYMENT = "deployment";
        private static final String HOST = "host";
        private static final String PIPELINE_TRIGGER = "pipeline_trigger";
        public static Map<String, String> stringStringMap = new HashMap<>(5);

        static {
            stringStringMap.put(CiJobTypeEnum.CHART_DEPLOY.value(), CHART);
            stringStringMap.put(CiJobTypeEnum.API_TEST.value(), API_TEST);
            stringStringMap.put(CiJobTypeEnum.AUDIT.value(), AUDIT);
            stringStringMap.put(CiJobTypeEnum.DEPLOYMENT_DEPLOY.value(), DEPLOYMENT);
            stringStringMap.put(CiJobTypeEnum.HOST_DEPLOY.value(), HOST);
            stringStringMap.put(CiJobTypeEnum.PIPELINE_TRIGGER.value(), PIPELINE_TRIGGER);
        }
    }
}
