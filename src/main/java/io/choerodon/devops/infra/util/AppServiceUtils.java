package io.choerodon.devops.infra.util;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ResourceLimitVO;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;

/**
 * Created by wangxiang on 2021/3/3
 */
@Component
public class AppServiceUtils {
    private static final String ERROR_PROJECT_APP_SVC_NUM_MAX = "error.project.app.svc.num.max";

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    private AppServiceMapper appServiceMapper;

    /**
     * 校验项目下是否还能创建应用服务
     *
     * @param projectId 项目id
     * @param appSize   服务的个数
     */
    public void checkEnableCreateAppSvcOrThrowE(Long projectId, int appSize) {
        if (Boolean.FALSE.equals(checkEnableCreateAppSvcWithSize(projectId, appSize))) {
            throw new CommonException(ERROR_PROJECT_APP_SVC_NUM_MAX);
        }
    }

    /**
     * 校验服务的名称的唯一性
     *
     * @param projectId 项目id
     * @param name      服务名称
     */
    public void checkName(Long projectId, String name) {
        if (!isNameUnique(projectId, name)) {
            throw new CommonException("error.name.exist");
        }
    }

    /**
     * 校验服务code 唯一性
     *
     * @param projectId 项目id
     * @param code      服务code
     */
    public void checkCode(Long projectId, String code) {
        if (!isCodeUnique(projectId, code)) {
            throw new CommonException("error.code.exist");
        }
    }

    public boolean isCodeUnique(Long projectId, String code) {
        AppServiceDTO appServiceDTO = new AppServiceDTO();
        appServiceDTO.setProjectId(projectId);
        appServiceDTO.setCode(code);
        return appServiceMapper.selectCount(appServiceDTO) == 0;
    }

    public boolean isNameUnique(Long projectId, String name) {
        AppServiceDTO appServiceDTO = new AppServiceDTO();
        appServiceDTO.setProjectId(projectId);
        appServiceDTO.setName(name);
        return appServiceMapper.selectCount(appServiceDTO) == 0;
    }

    public Boolean checkEnableCreateAppSvcWithSize(Long projectId, int appSize) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId, false, false, false);
        ResourceLimitVO resourceLimitVO = baseServiceClientOperator.queryResourceLimit(projectDTO.getOrganizationId());
        if (resourceLimitVO != null && !Objects.isNull(resourceLimitVO.getAppSvcMaxNumber())) {
            AppServiceDTO example = new AppServiceDTO();
            example.setProjectId(projectId);
            int num = appServiceMapper.selectCount(example);
            return num + appSize <= resourceLimitVO.getAppSvcMaxNumber();
        }
        return true;
    }

    public void checkCodeExist(String appCode) {
        if (StringUtils.isEmpty(appCode)) {
            throw new CommonException("error.code.is.null");
        }
    }
}
