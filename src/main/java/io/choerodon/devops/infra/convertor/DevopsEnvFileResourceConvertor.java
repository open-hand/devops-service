package io.choerodon.devops.infra.convertor;

import java.io.File;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileResourceVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsEnvFileResourceConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.infra.dataobject.DevopsEnvFileResourceDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsEnvFileResourceConvertor.java
=======
>>>>>>> [IMP] 重构Repository

/**
 * Creator: Runge
 * Date: 2018/7/25
 * Time: 17:09
 * Description:
 */
@Component
public class DevopsEnvFileResourceConvertor implements ConvertorI<DevopsEnvFileResourceVO, DevopsEnvFileResourceDTO, Object> {

    @Override
    public DevopsEnvFileResourceVO doToEntity(DevopsEnvFileResourceDTO devopsEnvFileResourceDO) {
        DevopsEnvFileResourceVO devopsEnvFileResourceE = new DevopsEnvFileResourceVO();
        BeanUtils.copyProperties(devopsEnvFileResourceDO, devopsEnvFileResourceE);
        DevopsEnvironmentE environmentE = new DevopsEnvironmentE();
        environmentE.setId(devopsEnvFileResourceDO.getEnvId());
        devopsEnvFileResourceE.setEnvironment(environmentE);
        devopsEnvFileResourceE.setFile(new File(devopsEnvFileResourceDO.getFilePath()));
        return devopsEnvFileResourceE;
    }

    @Override
    public DevopsEnvFileResourceDTO entityToDo(DevopsEnvFileResourceVO devopsEnvFileResourceE) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDO = new DevopsEnvFileResourceDTO();
        BeanUtils.copyProperties(devopsEnvFileResourceE, devopsEnvFileResourceDO);
        if (devopsEnvFileResourceE.getEnvironment() != null) {
            devopsEnvFileResourceDO.setEnvId(devopsEnvFileResourceE.getEnvironment().getId());
        }
        return devopsEnvFileResourceDO;
    }
}
