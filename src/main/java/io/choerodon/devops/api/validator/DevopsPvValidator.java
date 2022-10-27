package io.choerodon.devops.api.validator;

import java.util.regex.Pattern;

import io.kubernetes.client.openapi.models.V1HostPathVolumeSource;
import io.kubernetes.client.openapi.models.V1NFSVolumeSource;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.kubernetes.LocalPvResource;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.VolumeTypeEnum;

public class DevopsPvValidator {

    private static final String SERVER_PATTERN = "^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}$";

    private static final String HOSTPATH_PATTERN = "^/([-\\w]+[.]*[-\\w]*/?)+";

    private DevopsPvValidator() {

    }

    //校验存储类型的值是否为空或者格式是否符合要求
    public static void checkConfigValue(Object object, VolumeTypeEnum type) {
        switch (type) {
            case HOSTPATH:
                V1HostPathVolumeSource hostPath = (V1HostPathVolumeSource) object;
                if (hostPath.getPath() == null) {
                    throw new CommonException(GitOpsObjectError.PERSISTENT_VOLUME_HOSTPATH_PATH_NOT_FOUND.getError());
                } else if (!Pattern.matches(HOSTPATH_PATTERN, hostPath.getPath())) {
                    throw new CommonException("devops.pv.hostpath.format.error");
                }
                break;
            case NFS:
                V1NFSVolumeSource nfs = (V1NFSVolumeSource) object;
                if (nfs.getPath() == null) {
                    throw new CommonException(GitOpsObjectError.PERSISTENT_VOLUME_NFS_PATH_NOT_FOUND.getError());
                } else if (!Pattern.matches(HOSTPATH_PATTERN, nfs.getPath())) {
                    throw new CommonException("devops.pv.nfs.path.format.error");
                } else if (nfs.getServer() == null) {
                    throw new CommonException(GitOpsObjectError.PERSISTENT_VOLUME_NFS_SERVER_NOT_IP.getError());
                } else if (!Pattern.matches(SERVER_PATTERN, nfs.getServer())) {
                    throw new CommonException("devops.pv.nfs.server.format.error");
                }
                break;
            case LOCALPV:
                LocalPvResource localPvResource = (LocalPvResource) object;
                if (localPvResource.getPath() == null) {
                    throw new CommonException(GitOpsObjectError.PERSISTENT_VOLUME_LOCAL_PATH_NOT_FOUND.getError());
                } else if (!Pattern.matches(HOSTPATH_PATTERN, localPvResource.getPath())) {
                    throw new CommonException("devops.pv.local.path.format.error");
                } else if (localPvResource.getNodeName() == null) {
                    throw new CommonException(GitOpsObjectError.PERSISTENT_VOLUME_LOCAL_PATH_NODE_AFFINITY_NOT_FOUND.getError());
                }
                break;
        }
    }

}
