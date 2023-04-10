package io.choerodon.devops.api.validator;

import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.K8sUtil;

/**
 * Created by Zenger on 2018/4/26.
 */
public class DevopsIngressValidator {

    // ingress subdomain
    private static final String SUB_PATH_PATTERN = "^/(\\S)*$";

    private static final String DEVOPS_INGRESS_ANNOTATION_KEY_EMPTY = "devops.ingress.annotation.key.empty";
    private static final String DEVOPS_INGRESS_ANNOTATION_VALUE_EMPTY = "devops.ingress.annotation.value.empty";
    private static final String DEVOPS_INGRESS_ANNOTATION_KEY_TOO_MANY_SLASHES = "devops.ingress.annotation.key.too.many.slashes";
    private static final String DEVOPS_INGRESS_ANNOTATION_KEY_SUB_DOMAIN_PART_TOO_LONG = "devops.ingress.annotation.key.sub.domain.part.too.long";
    private static final String DEVOPS_INGRESS_ANNOTATION_KEY_SUB_DOMAIN_PART_INVALID = "devops.ingress.annotation.key.sub.domain.part.invalid";
    private static final String DEVOPS_INGRESS_ANNOTATION_KEY_NAME_PART_TOO_LONG = "devops.ingress.annotation.key.name.part.too.long";
    private static final String DEVOPS_INGRESS_ANNOTATION_KEY_NAME_PART_INVALID = "devops.ingress.annotation.key.name.part.invalid";

    private DevopsIngressValidator() {
    }

    /**
     * 参数校验
     */
    public static void checkIngressName(String name) {
        if (!K8sUtil.NAME_PATTERN.matcher(name).matches()) {
            throw new CommonException("devops.ingress.name.notMatch");
        }
    }

    /**
     * 参数校验
     */
    public static void checkPath(String path) {
        if (!Pattern.matches(SUB_PATH_PATTERN, path)) {
            throw new CommonException("devops.ingress.subPath.notMatch");
        }
    }

    public static void checkHost(String host) {
        if (StringUtils.isEmpty(host)
                || !K8sUtil.HOST_PATTERN.matcher(host).matches()) {
            throw new CommonException("devops.ingress.host.format");
        }
    }

    public static void checkVOForBatchDeployment(DevopsIngressVO devopsIngressVO) {
        checkIngressName(devopsIngressVO.getName());
        checkHost(devopsIngressVO.getDomain());
        if (devopsIngressVO.getEnvId() == null) {
            throw new CommonException("devops.env.id.null");
        }
        if (CollectionUtils.isEmpty(devopsIngressVO.getPathList())) {
            throw new CommonException("devops.ingress.path.empty");
        }
        checkAnnotations(devopsIngressVO.getAnnotations());
    }

    /**
     * 校验Annotation字段是否合法, 这里不校验Annotations所有键值对的总长度，由后续处理逻辑校验
     * 校验规则可参考(https://kubernetes.io/docs/concepts/overview/working-with-objects/annotations/#syntax-and-character-set)
     *
     * @param annotations 注解map，可为空
     */
    public static void checkAnnotations(@Nullable Map<String, String> annotations) {
        if (CollectionUtils.isEmpty(annotations)) {
            return;
        }

        annotations.forEach((key, value) -> {
            if (StringUtils.isEmpty(key)) {
                throw new CommonException(DEVOPS_INGRESS_ANNOTATION_KEY_EMPTY);
            }
            if (StringUtils.isEmpty(value)) {
                throw new CommonException(DEVOPS_INGRESS_ANNOTATION_VALUE_EMPTY);
            }
            String[] parts = key.split("/");
            if (parts.length > 2) {
                throw new CommonException(DEVOPS_INGRESS_ANNOTATION_KEY_TOO_MANY_SLASHES, key);
            } else if (parts.length == 2) {
                // 有两段的情况
                if (parts[0].length() > 253) {
                    throw new CommonException(DEVOPS_INGRESS_ANNOTATION_KEY_SUB_DOMAIN_PART_TOO_LONG, key);
                }
                if (!K8sUtil.SUB_DOMAIN_PATTERN.matcher(parts[0]).matches()) {
                    throw new CommonException(DEVOPS_INGRESS_ANNOTATION_KEY_SUB_DOMAIN_PART_INVALID, key);
                }
                if (parts[1].length() > 63) {
                    throw new CommonException(DEVOPS_INGRESS_ANNOTATION_KEY_NAME_PART_TOO_LONG, key);
                }
                if (!K8sUtil.ANNOTATION_NAME_PATTERN.matcher(parts[1]).matches()) {
                    throw new CommonException(DEVOPS_INGRESS_ANNOTATION_KEY_NAME_PART_INVALID, key);
                }
            } else {
                // 只有一段的情况
                if (key.length() > 63) {
                    throw new CommonException(DEVOPS_INGRESS_ANNOTATION_KEY_NAME_PART_TOO_LONG, key);
                }
                if (!K8sUtil.ANNOTATION_NAME_PATTERN.matcher(key).matches()) {
                    throw new CommonException(DEVOPS_INGRESS_ANNOTATION_KEY_NAME_PART_INVALID, key);
                }
            }
        });
    }


    /**
     * 校验Annotation字段是否合法, 这里不校验Annotations所有键值对的总长度，由后续处理逻辑校验
     * 校验规则可参考(https://kubernetes.io/docs/concepts/overview/working-with-objects/annotations/#syntax-and-character-set)
     *
     * @param annotations 注解map，可为空
     * @param filePath    文件路径
     */
    public static void checkAnnotationsForGitOps(@Nullable Map<String, String> annotations, String filePath) {
        if (CollectionUtils.isEmpty(annotations)) {
            return;
        }

        annotations.forEach((key, value) -> {
            if (StringUtils.isEmpty(key)) {
                throw new GitOpsExplainException(DEVOPS_INGRESS_ANNOTATION_KEY_EMPTY, filePath);
            }
            if (StringUtils.isEmpty(value)) {
                throw new GitOpsExplainException(DEVOPS_INGRESS_ANNOTATION_VALUE_EMPTY, filePath);
            }
            String[] parts = key.split("/");
            if (parts.length > 2) {
                throw new GitOpsExplainException(DEVOPS_INGRESS_ANNOTATION_KEY_TOO_MANY_SLASHES, filePath, new Object[]{key});
            } else if (parts.length == 2) {
                if (parts[0].length() > 253) {
                    throw new GitOpsExplainException(DEVOPS_INGRESS_ANNOTATION_KEY_SUB_DOMAIN_PART_TOO_LONG, filePath, new Object[]{key});
                }
                if (!K8sUtil.SUB_DOMAIN_PATTERN.matcher(parts[0]).matches()) {
                    throw new GitOpsExplainException(DEVOPS_INGRESS_ANNOTATION_KEY_SUB_DOMAIN_PART_INVALID, filePath, new Object[]{key});
                }
                if (parts[1].length() > 63) {
                    throw new GitOpsExplainException(DEVOPS_INGRESS_ANNOTATION_KEY_NAME_PART_TOO_LONG, filePath, new Object[]{key});
                }
                if (!K8sUtil.ANNOTATION_NAME_PATTERN.matcher(parts[1]).matches()) {
                    throw new GitOpsExplainException(DEVOPS_INGRESS_ANNOTATION_KEY_NAME_PART_INVALID, filePath, new Object[]{key});
                }
            }
        });
    }
}
