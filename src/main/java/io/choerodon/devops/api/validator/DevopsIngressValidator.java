package io.choerodon.devops.api.validator;

import java.util.regex.Pattern;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsIngressPathVO;
import io.choerodon.devops.api.vo.DevopsIngressVO;

/**
 * Created by Zenger on 2018/4/26.
 */
public class DevopsIngressValidator {

    //ingress name
    private static final String NAME_PATTERN = "[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*";
    // ingress subdomain
    private static final String SUB_PATH_PATTERN = "^\\/(\\S)*$";
    /**
     * Kubernetes Host的正则
     * // TODO 是不是支持 * 开头
     */
    private static final Pattern HOST_PATTERN = Pattern.compile("[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*");

    private DevopsIngressValidator() {
    }

    /**
     * 参数校验
     */
    public static void checkIngressName(String name) {
        if (!Pattern.matches(NAME_PATTERN, name)) {
            throw new CommonException("error.ingress.name.notMatch");
        }
    }

    /**
     * 参数校验
     */
    public static void checkPath(String path) {
        if (!Pattern.matches(SUB_PATH_PATTERN, path)) {
            throw new CommonException("error.ingress.subPath.notMatch");
        }
    }

    public static void checkVOForBatchDeployment(DevopsIngressVO devopsIngressVO) {
        checkIngressName(devopsIngressVO.getName());
        if (StringUtils.isEmpty(devopsIngressVO.getDomain())
            || !HOST_PATTERN.matcher(devopsIngressVO.getDomain()).matches()) {
            throw new CommonException("error.ingress.host.format");
        }
        if (devopsIngressVO.getEnvId() == null) {
            throw new CommonException("error.env.id.null");
        }
        if (CollectionUtils.isEmpty(devopsIngressVO.getPathList())) {
            throw new CommonException("error.ingress.path.empty");
        }

    }

    public static void checkPathVO(DevopsIngressPathVO path) {
        if (StringUtils.isEmpty(path.getServiceName())) {
            throw new CommonException("error.service.name.in.path.null");
        }
        checkPath(path.getPath());
        if (!checkPort(path.getServicePort())) {
            throw new CommonException("error.port.illegal");
        }
    }

    private static Boolean checkPort(Long port) {
        return port >= 0 && port <= 65535;
    }
}
