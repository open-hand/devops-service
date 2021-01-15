package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.kubernetes.Command;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:36 2019/7/12
 * Description:
 */
public interface DevopsEnvCommandService {
    DevopsEnvCommandDTO baseCreate(DevopsEnvCommandDTO devopsEnvCommandDTO);

    DevopsEnvCommandDTO baseQueryByObject(String objectType, Long objectId);

    DevopsEnvCommandDTO baseUpdate(DevopsEnvCommandDTO devopsEnvCommandDTO);

    void baseUpdateSha(Long commandId, String sha);

    DevopsEnvCommandDTO baseQuery(Long id);

    List<DevopsEnvCommandDTO> baseListByEnvId(Long envId);

    List<DevopsEnvCommandDTO> baseListInstanceCommand(String objectType, Long objectId);

    Page<DevopsEnvCommandDTO> basePageByObject(PageRequest pageable, String objectType, Long objectId, Date startTime, Date endTime);

    void baseDelete(Long commandId);

    List<DevopsEnvCommandDTO> baseListByObject(String objectType, Long objectId);

    void baseDeleteByEnvCommandId(DevopsEnvCommandDTO devopsEnvCommandDTO);

    /**
     * 列出三分钟以上还在处理中状态的各种资源相关的command
     *
     * @param envId      环境id
     * @param beforeDate 特定时间字符串，格式为：'yyyy-MM-dd HH:mm:ss'
     * @return commands
     */
    List<Command> listCommandsToSync(Long envId, String beforeDate);

    /**
     * 根据实例的id和commit sha值查询实例
     *
     * @param instanceId 实例id
     * @param sha        散列值
     * @return command
     */
    @Nullable
    DevopsEnvCommandDTO queryByInstanceIdAndCommitSha(Long instanceId, String sha);

    /**
     * 将之前的操作中的资源更新为成功
     *
     * @param objectType 对象类型
     * @param objectId   对象id
     * @param beforeDate 截止日期
     */
    void updateOperatingToSuccessBeforeDate(ObjectType objectType, Long objectId, Date beforeDate);
}
