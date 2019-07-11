package io.choerodon.devops.infra.convertor;

<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsProjectConvertor.java
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectE;
import io.choerodon.devops.infra.dataobject.DevopsProjectDTO;
=======
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectVO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsProjectConvertor.java
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Created by younger on 2018/4/2.
 */
@Component
public class DevopsProjectConvertor implements ConvertorI<DevopsProjectVO, DevopsProjectDTO, Object> {

    @Override
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsProjectConvertor.java
    public DevopsProjectE doToEntity(DevopsProjectDTO devopsProjectDO) {
        DevopsProjectE devopsProjectE = new DevopsProjectE();
        BeanUtils.copyProperties(devopsProjectDO, devopsProjectE);
=======
    public DevopsProjectVO doToEntity(DevopsProjectDTO devopsProjectDO) {
        DevopsProjectVO devopsProjectE = new DevopsProjectVO();
        BeanUtils.copyProperties(devopsProjectDO,devopsProjectE);
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsProjectConvertor.java
        devopsProjectE.initProjectE(devopsProjectDO.getIamProjectId());
        return devopsProjectE;
    }

    @Override
    public DevopsProjectDTO entityToDo(DevopsProjectVO devopsProjectE) {
        DevopsProjectDTO devopsProjectDO = new DevopsProjectDTO();
        BeanUtils.copyProperties(devopsProjectE, devopsProjectDO);
        devopsProjectDO.setIamProjectId(devopsProjectE.getProjectE().getId());
        return devopsProjectDO;
    }
}
