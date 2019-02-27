package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsAutoDeployRecordDTO;
import io.choerodon.devops.domain.application.entity.DevopsAutoDeployRecordE;
import io.choerodon.devops.infra.dataobject.DevopsAutoDeployRecordDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:52 2019/2/27
 * Description:
 */
@Component
public class DevopsAutoDeployRecordConvertor implements ConvertorI<DevopsAutoDeployRecordE, DevopsAutoDeployRecordDO, DevopsAutoDeployRecordDTO> {
    @Override
    public DevopsAutoDeployRecordE doToEntity(DevopsAutoDeployRecordDO devopsAutoDeployRecordDO) {
        DevopsAutoDeployRecordE devopsAutoDeployRecordE = new DevopsAutoDeployRecordE();
        BeanUtils.copyProperties(devopsAutoDeployRecordDO, devopsAutoDeployRecordE);
        devopsAutoDeployRecordE.setEnvStatus(devopsAutoDeployRecordDO.getEnvStatus() == 1);
        return devopsAutoDeployRecordE;
    }

    @Override
    public DevopsAutoDeployRecordDTO entityToDto(DevopsAutoDeployRecordE devopsAutoDeployRecordE) {
        DevopsAutoDeployRecordDTO devopsAutoDeployRecordDTO = new DevopsAutoDeployRecordDTO();
        BeanUtils.copyProperties(devopsAutoDeployRecordE, devopsAutoDeployRecordDTO);
        return devopsAutoDeployRecordDTO;
    }

}
