package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandLogRepository;
import io.choerodon.devops.infra.dto.DevopsEnvCommandLogDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandLogMapper;
import org.springframework.stereotype.Service;

/**
 * Created by younger on 2018/4/24.
 */
@Service
public class DevopsEnvCommandLogRepositoryImpl implements DevopsEnvCommandLogRepository {

    private DevopsEnvCommandLogMapper devopsEnvCommandLogMapper;

    public DevopsEnvCommandLogRepositoryImpl(DevopsEnvCommandLogMapper devopsEnvCommandLogMapper) {
        this.devopsEnvCommandLogMapper = devopsEnvCommandLogMapper;
    }


    @Override
    public DevopsEnvCommandLogVO baseCreate(DevopsEnvCommandLogVO devopsEnvCommandLogE) {
        DevopsEnvCommandLogDTO devopsEnvCommandLogDTO =
                ConvertHelper.convert(devopsEnvCommandLogE, DevopsEnvCommandLogDTO.class);
        if (devopsEnvCommandLogMapper.insert(devopsEnvCommandLogDTO) != 1) {
            throw new CommonException("error.log.insert");
        }
        return ConvertHelper.convert(devopsEnvCommandLogDTO, DevopsEnvCommandLogVO.class);
    }

    @Override
    public DevopsEnvCommandLogVO baseQuery(Long logId) {
        return ConvertHelper.convert(devopsEnvCommandLogMapper.selectByPrimaryKey(logId),
                DevopsEnvCommandLogVO.class);
    }

    @Override
    public List<DevopsEnvCommandLogVO> baseListByDeployId(Long commandId) {
        DevopsEnvCommandLogDTO devopsEnvCommandLogDTO = new DevopsEnvCommandLogDTO();
        devopsEnvCommandLogDTO.setCommandId(commandId);
        List<DevopsEnvCommandLogDTO> devopsEnvCommandLogDTOS = devopsEnvCommandLogMapper.select(devopsEnvCommandLogDTO);
        return ConvertHelper.convertList(devopsEnvCommandLogDTOS, DevopsEnvCommandLogVO.class);
    }

    @Override
    public void baseDeleteByInstanceId(Long instanceId) {
        devopsEnvCommandLogMapper.deletePreInstanceCommandLog(instanceId);
    }

    @Override
    public void baseDeleteByCommandId(Long commandId) {
        DevopsEnvCommandLogDTO devopsEnvCommandLogDTO = new DevopsEnvCommandLogDTO();
        devopsEnvCommandLogDTO.setCommandId(commandId);
        devopsEnvCommandLogMapper.delete(devopsEnvCommandLogDTO);
    }
}
