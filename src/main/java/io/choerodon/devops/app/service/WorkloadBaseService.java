package io.choerodon.devops.app.service;

import static io.choerodon.devops.infra.constant.MiscConstants.CREATE_TYPE;
import static io.choerodon.devops.infra.constant.MiscConstants.UPDATE_TYPE;

import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.ObjectType;

public interface WorkloadBaseService<T, E> {
    T baseQueryByEnvIdAndName(Long envId, String name);

    T selectByPrimaryKey(Long id);

    void checkExist(Long envId, String name);

    Long baseCreate(T t);

    void baseUpdate(T t);

    void baseDelete(Long id);

    E createOrUpdateByGitOps(E t, Long userId, String content);

    void deleteByGitOps(Long id);

    default DevopsEnvCommandDTO initDevopsEnvCommandDTO(String type) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        if (type.equals(CREATE_TYPE)) {
            devopsEnvCommandDTO.setCommandType(CommandType.CREATE.getType());
        } else if (type.equals(UPDATE_TYPE)) {
            devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        } else {
            devopsEnvCommandDTO.setCommandType(CommandType.DELETE.getType());
        }
        devopsEnvCommandDTO.setObject(ObjectType.CONFIGMAP.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }
}
