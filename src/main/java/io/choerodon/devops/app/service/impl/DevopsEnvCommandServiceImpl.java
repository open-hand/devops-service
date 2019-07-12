package io.choerodon.devops.app.service.impl;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandVO;
import io.choerodon.devops.app.service.DevopsEnvCommandService;
import io.choerodon.devops.domain.application.repository.DevopsCommandEventRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandLogRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandValueRepository;
import io.choerodon.devops.infra.dto.ApplicationInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandMapper;
import io.choerodon.devops.infra.util.PageRequestUtil;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:37 2019/7/12
 * Description:
 */
public class DevopsEnvCommandServiceImpl implements DevopsEnvCommandService {

    @Autowired
    DevopsEnvCommandValueRepository devopsEnvCommandValueRepository;
    @Autowired
    DevopsEnvCommandLogRepository devopsEnvCommandLogRepository;
    @Autowired
    DevopsCommandEventRepository devopsCommandEventRepository;
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper;


    @Override
    public DevopsEnvCommandVO baseCreate(DevopsEnvCommandVO devopsEnvCommandE) {
        DevopsEnvCommandDTO devopsEnvCommandDO = ConvertHelper.convert(devopsEnvCommandE, DevopsEnvCommandDTO.class);
        if (devopsEnvCommandMapper.insert(devopsEnvCommandDO) != 1) {
            throw new CommonException("error.env.command.insert");
        }
        return ConvertHelper.convert(devopsEnvCommandDO, DevopsEnvCommandVO.class);
    }

    @Override
    public DevopsEnvCommandVO baseQueryByObject(String objectType, Long objectId) {
        return ConvertHelper.convert(
                devopsEnvCommandMapper.queryByObject(objectType, objectId), DevopsEnvCommandVO.class);
    }

    @Override
    public DevopsEnvCommandVO baseUpdate(DevopsEnvCommandVO devopsEnvCommandVO) {
        DevopsEnvCommandDTO devopsEnvCommandDO = ConvertHelper.convert(devopsEnvCommandVO, DevopsEnvCommandDTO.class);
        DevopsEnvCommandDTO newDevopsEnvCommandDO = devopsEnvCommandMapper
                .selectByPrimaryKey(devopsEnvCommandDO.getId());
        devopsEnvCommandDO.setObjectVersionNumber(newDevopsEnvCommandDO.getObjectVersionNumber());
        if (devopsEnvCommandMapper.updateByPrimaryKeySelective(devopsEnvCommandDO) != 1) {
            throw new CommonException("error.env.command.update");
        }
        return ConvertHelper.convert(devopsEnvCommandDO, DevopsEnvCommandVO.class);
    }

    @Override
    public DevopsEnvCommandVO baseQuery(Long id) {
        DevopsEnvCommandDTO devopsEnvCommandDO = devopsEnvCommandMapper.selectByPrimaryKey(id);
        return ConvertHelper.convert(devopsEnvCommandDO, DevopsEnvCommandVO.class);
    }

    @Override
    public List<DevopsEnvCommandVO> baseListByEnvId(Long envId) {
        DevopsEnvCommandDTO devopsEnvCommandDO = new DevopsEnvCommandDTO();
        return ConvertHelper.convertList(devopsEnvCommandMapper.select(devopsEnvCommandDO), DevopsEnvCommandVO.class);
    }

    @Override
    public List<DevopsEnvCommandVO> baseQueryInstanceCommand(String objectType, Long objectId) {
        return ConvertHelper.convertList(devopsEnvCommandMapper.queryInstanceCommand(objectType, objectId), DevopsEnvCommandVO.class);
    }

    @Override
    public PageInfo<DevopsEnvCommandVO> baseListByObject(PageRequest pageRequest, String objectType, Long objectId, Date startTime, Date endTime) {
        PageInfo<ApplicationInstanceDTO> applicationInstanceDOPage = PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                devopsEnvCommandMapper.listByObject(objectType, objectId, startTime == null ? null : new java.sql.Date(startTime.getTime()), endTime == null ? null : new java.sql.Date(endTime.getTime())));
        return ConvertPageHelper.convertPageInfo(applicationInstanceDOPage, DevopsEnvCommandVO.class);
    }

    @Override
    public void baseDelete(Long commandId) {
        DevopsEnvCommandDTO devopsEnvCommandDO = new DevopsEnvCommandDTO();
        devopsEnvCommandDO.setId(commandId);
        devopsEnvCommandMapper.deleteByPrimaryKey(devopsEnvCommandDO);
    }

    @Override
    public List<DevopsEnvCommandVO> baseListByObjectAll(String objectType, Long objectId) {
        DevopsEnvCommandDTO devopsEnvCommandDO = new DevopsEnvCommandDTO();
        devopsEnvCommandDO.setObjectId(objectId);
        devopsEnvCommandDO.setObject(objectType);
        return ConvertHelper.convertList(devopsEnvCommandMapper.select(devopsEnvCommandDO), DevopsEnvCommandVO.class);
    }

    @Override
    public void baseDeleteCommandById(DevopsEnvCommandVO devopsEnvCommandVO) {
        if (devopsEnvCommandVO.getDevopsEnvCommandValueDTO() != null) {
            devopsEnvCommandValueRepository.baseDeleteById(devopsEnvCommandVO.getDevopsEnvCommandValueDTO().getId());
        }
        devopsEnvCommandLogRepository.baseDeleteByCommandId(devopsEnvCommandVO.getId());
        devopsCommandEventRepository.deleteByCommandId(devopsEnvCommandVO.getId());
        devopsEnvCommandMapper.deleteByPrimaryKey(devopsEnvCommandVO.getId());
    }
}
