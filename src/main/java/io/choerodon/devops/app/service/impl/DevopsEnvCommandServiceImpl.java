package io.choerodon.devops.app.service.impl;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCommandEventService;
import io.choerodon.devops.app.service.DevopsEnvCommandLogService;
import io.choerodon.devops.app.service.DevopsEnvCommandService;
import io.choerodon.devops.app.service.DevopsEnvCommandValueService;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandMapper;
import io.choerodon.devops.infra.util.PageRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:37 2019/7/12
 * Description:
 */
@Service
public class DevopsEnvCommandServiceImpl implements DevopsEnvCommandService {

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
        return devopsEnvCommandDTO;
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
        return ConvertHelper.convertList(devopsEnvCommandMapper.select(devopsEnvCommandDO), DevopsEnvCommandDTO.class);
    }

    @Override
    public PageInfo<DevopsEnvCommandDTO> basePageByObject(PageRequest pageRequest, String objectType, Long objectId, Date startTime, Date endTime) {
        return PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
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
}
