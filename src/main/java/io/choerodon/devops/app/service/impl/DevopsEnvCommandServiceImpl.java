package io.choerodon.devops.app.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.kubernetes.Command;
import io.choerodon.devops.app.service.DevopsCommandEventService;
import io.choerodon.devops.app.service.DevopsEnvCommandLogService;
import io.choerodon.devops.app.service.DevopsEnvCommandService;
import io.choerodon.devops.app.service.DevopsEnvCommandValueService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandMapper;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:37 2019/7/12
 * Description:
 */
@Service
public class DevopsEnvCommandServiceImpl implements DevopsEnvCommandService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsEnvCommandServiceImpl.class);

    @Autowired
    DevopsEnvCommandValueService devopsEnvCommandValueService;
    @Autowired
    DevopsEnvCommandLogService devopsEnvCommandLogService;
    @Autowired
    DevopsCommandEventService devopsCommandEventService;
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper;


    @Override
    public DevopsEnvCommandDTO baseCreate(DevopsEnvCommandDTO devopsEnvCommandDTO) {
        if (devopsEnvCommandMapper.insert(devopsEnvCommandDTO) != 1) {
            throw new CommonException("devops.env.command.insert");
        }
        return devopsEnvCommandMapper.selectByPrimaryKey(devopsEnvCommandDTO);
    }

    @Override
    public DevopsEnvCommandDTO baseQueryByObject(String objectType, Long objectId) {
        return devopsEnvCommandMapper.queryByObject(objectType, objectId);
    }

    @Override
    public DevopsEnvCommandDTO baseUpdate(DevopsEnvCommandDTO devopsEnvCommandDTO) {
        DevopsEnvCommandDTO oldDevopsEnvCommandDO = devopsEnvCommandMapper
                .selectByPrimaryKey(devopsEnvCommandDTO.getId());
        devopsEnvCommandDTO.setObjectVersionNumber(oldDevopsEnvCommandDO.getObjectVersionNumber());
        if (devopsEnvCommandMapper.updateByPrimaryKeySelective(devopsEnvCommandDTO) != 1) {
            throw new CommonException("devops.env.command.update");
        }

        return devopsEnvCommandDTO;
    }

    @Override
    public void baseUpdateSha(Long commandId, String sha) {
        CommonExAssertUtil.assertNotNull(sha, "devops.commit.sha.null");
        devopsEnvCommandMapper.updateSha(commandId, sha);
    }

    @Override
    public DevopsEnvCommandDTO baseQuery(Long id) {
        return devopsEnvCommandMapper.selectByPrimaryKey(id);
    }


    @Override
    public List<DevopsEnvCommandDTO> baseListInstanceCommand(String objectType, Long objectId) {
        return devopsEnvCommandMapper.listInstanceCommand(objectType, objectId);
    }

    @Override
    public List<DevopsEnvCommandDTO> baseListByEnvId(Long envId) {
        DevopsEnvCommandDTO devopsEnvCommandDO = new DevopsEnvCommandDTO();
        return ConvertUtils.convertList(devopsEnvCommandMapper.select(devopsEnvCommandDO), DevopsEnvCommandDTO.class);
    }

    @Override
    public Page<DevopsEnvCommandDTO> basePageByObject(PageRequest pageable, String objectType, Long objectId, Date startTime, Date endTime) {
        return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () ->
                devopsEnvCommandMapper.listByObject(objectType, objectId, startTime == null ? null : new java.sql.Date(startTime.getTime()), endTime == null ? null : new java.sql.Date(endTime.getTime())));
    }

    @Override
    public void baseDelete(Long commandId) {
        DevopsEnvCommandDTO devopsEnvCommandDO = new DevopsEnvCommandDTO();
        devopsEnvCommandDO.setId(commandId);
        devopsEnvCommandMapper.deleteByPrimaryKey(devopsEnvCommandDO);
    }

    @Override
    public List<DevopsEnvCommandDTO> baseListByObject(String objectType, Long objectId) {
        DevopsEnvCommandDTO devopsEnvCommandDO = new DevopsEnvCommandDTO();
        devopsEnvCommandDO.setObjectId(objectId);
        devopsEnvCommandDO.setObject(objectType);
        return devopsEnvCommandMapper.select(devopsEnvCommandDO);
    }

    @Override
    public void baseDeleteByEnvCommandId(DevopsEnvCommandDTO devopsEnvCommandDTO) {
        if (devopsEnvCommandDTO.getValueId() != null) {
            devopsEnvCommandValueService.baseDeleteById(devopsEnvCommandDTO.getValueId());
        }
        devopsEnvCommandLogService.baseDeleteByCommandId(devopsEnvCommandDTO.getId());
        devopsCommandEventService.baseDeleteByCommandId(devopsEnvCommandDTO.getId());
        devopsEnvCommandMapper.deleteByPrimaryKey(devopsEnvCommandDTO.getId());
    }

    @Override
    public List<Command> listCommandsToSync(Long envId, String beforeDate) {
        return devopsEnvCommandMapper.listCommandsToSync(envId, beforeDate);
    }

    @Nullable
    @Override
    public DevopsEnvCommandDTO queryByInstanceIdAndCommitSha(Long instanceId, String sha) {
        List<DevopsEnvCommandDTO> devopsEnvCommandDTOS = devopsEnvCommandMapper.listByInstanceIdAndCommitSha(instanceId, sha);
        if (CollectionUtils.isEmpty(devopsEnvCommandDTOS)) {
            return null;
        } else {
            if (devopsEnvCommandDTOS.size() > 1) {
                LOGGER.info("Unexpected multi-record for commands with instanceId {} and sha {}", instanceId, sha);
            }
            return devopsEnvCommandDTOS.get(devopsEnvCommandDTOS.size() - 1);
        }
    }

    @Override
    public DevopsEnvCommandDTO queryByWorkloadTypeAndObjectIdAndCommitSha(String type, Long objectId, String sha) {
        DevopsEnvCommandDTO condition = new DevopsEnvCommandDTO();
        condition.setObjectId(Objects.requireNonNull(objectId));
        condition.setSha(Objects.requireNonNull(sha));
        condition.setObject(type.toLowerCase());
        List<DevopsEnvCommandDTO> devopsEnvCommandDTOS = devopsEnvCommandMapper.select(condition);
        if (CollectionUtils.isEmpty(devopsEnvCommandDTOS)) {
            return null;
        } else {
            if (devopsEnvCommandDTOS.size() > 1) {
                LOGGER.info("Unexpected multi-record for commands with workload {} and instanceId {} and sha {}", type, objectId, sha);
            }
            return devopsEnvCommandDTOS.get(0);
        }
    }

    @Override
    public void updateOperatingToSuccessBeforeDate(ObjectType objectType, Long objectId, Date beforeDate) {
        CommonExAssertUtil.assertNotNull(objectType, "devops.object.type.null");
        CommonExAssertUtil.assertNotNull(objectId, "devops.object.id.null");
        CommonExAssertUtil.assertNotNull(beforeDate, "devops.before.date.null");
        devopsEnvCommandMapper.updateOperatingToSuccessBeforeDate(objectType.getType(), objectId, beforeDate);
    }

    @Override
    public Long queryWorkloadEffectCommandId(String workloadType, Long workloadId) {
        return devopsEnvCommandMapper.queryWorkloadEffectCommandId(workloadType, workloadId);
    }

    @Override
    @Transactional
    public void deleteByInstanceId(Long instanceId) {
        Assert.notNull(instanceId, ResourceCheckConstant.DEVOPS_INSTANCE_ID_IS_NULL);
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        devopsEnvCommandDTO.setObjectId(instanceId);
        devopsEnvCommandDTO.setObject(ObjectType.INSTANCE.getType());
        devopsEnvCommandMapper.delete(devopsEnvCommandDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cascadeDeleteByInstanceId(Long instanceId) {
        List<DevopsEnvCommandDTO> devopsEnvCommandDTOS = baseListByObject(ObjectType.INSTANCE.getType(), instanceId);
        if (!CollectionUtils.isEmpty(devopsEnvCommandDTOS)) {
            Set<Long> commandIds = devopsEnvCommandDTOS.stream().map(DevopsEnvCommandDTO::getId).collect(Collectors.toSet());
            Set<Long> valueIds = devopsEnvCommandDTOS.stream().map(DevopsEnvCommandDTO::getValueId).collect(Collectors.toSet());

            devopsCommandEventService.batchDeleteByCommandIds(commandIds);
            devopsEnvCommandLogService.batchDeleteByCommandIds(commandIds);
            devopsEnvCommandValueService.batchDeleteByIds(valueIds);
            deleteByInstanceId(instanceId);
        }

    }
}
