package io.choerodon.devops.app.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.kubernetes.Command;
import io.choerodon.devops.app.service.DevopsCommandEventService;
import io.choerodon.devops.app.service.DevopsEnvCommandLogService;
import io.choerodon.devops.app.service.DevopsEnvCommandService;
import io.choerodon.devops.app.service.DevopsEnvCommandValueService;
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
            throw new CommonException("error.env.command.insert");
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
            throw new CommonException("error.env.command.update");
        }

        return devopsEnvCommandDTO;
    }

    @Override
    public void baseUpdateSha(Long commandId, String sha) {
        CommonExAssertUtil.assertNotNull(sha, "error.commit.sha.null");
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
        DevopsEnvCommandDTO condition = new DevopsEnvCommandDTO();
        condition.setObjectId(Objects.requireNonNull(instanceId));
        condition.setSha(Objects.requireNonNull(sha));
        condition.setObject(ObjectType.INSTANCE.getType());
        List<DevopsEnvCommandDTO> devopsEnvCommandDTOS = devopsEnvCommandMapper.select(condition);
        if (CollectionUtils.isEmpty(devopsEnvCommandDTOS)) {
            return null;
        } else {
            if (devopsEnvCommandDTOS.size() > 1) {
                LOGGER.info("Unexpected multi-record for commands with instanceId {} and sha {}", instanceId, sha);
            }
            return devopsEnvCommandDTOS.get(0);
        }
    }
}
