package io.choerodon.devops.infra.utils;

import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.app.service.CiTemplateJobBusService;
import io.choerodon.devops.app.service.CiTemplateStepBusService;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;

@Component
public class PipelineTemplateUtils {

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
        if (StringUtils.equalsIgnoreCase(sourceType, ResourceLevel.PROJECT.value())) {
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
        if (StringUtils.equalsIgnoreCase(type, JOB)) {
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
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(sourceId, false, false, false, false, false);
                    if (!baseServiceClientOperator.isOrganzationRoot(userDetails.getUserId(), projectDTO.getOrganizationId()) && !baseServiceClientOperator.isProjectOwner(userDetails.getUserId(), sourceId)) {
                        throw new CommonException("error.no.permission.to.do.operation");
                    }
                    break;
                default:
                    break;
            }

        }
    }
}
