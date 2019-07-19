package io.choerodon.devops.infra.convertor;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsServiceVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsServiceE;
<<<<<<< HEAD
<<<<<<< HEAD
import io.choerodon.devops.api.vo.iam.entity.PortMapE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsServiceConvertor.java
import io.choerodon.devops.infra.dto.DevopsServiceDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsServiceDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsServiceConvertor.java
=======
=======
import io.choerodon.devops.infra.dto.PortMapDTO;
>>>>>>> [REF] finish refactoring DevopsIngressController.
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsServiceConvertor.java
import io.choerodon.devops.infra.dataobject.DevopsServiceDO;
=======
import io.choerodon.devops.infra.dto.DevopsServiceDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/DevopsServiceConvertor.java
>>>>>>> [IMP]重构后端断码
=======
import io.choerodon.devops.infra.dto.DevopsServiceDTO;
import io.choerodon.devops.infra.dto.PortMapVO;
>>>>>>> [IMP]修复后端结构
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Created by Zenger on 2018/4/18.
 */
@Component
public class DevopsServiceConvertor implements ConvertorI<DevopsServiceE, DevopsServiceDTO, DevopsServiceVO> {
    private Gson gson = new Gson();

    @Override
    public DevopsServiceDTO entityToDo(DevopsServiceE entity) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        BeanUtils.copyProperties(entity, devopsServiceDTO);
        devopsServiceDTO.setPorts(gson.toJson(entity.getPorts()));
        return devopsServiceDTO;
    }

    @Override
    public DevopsServiceE doToEntity(DevopsServiceDTO dataObject) {
        DevopsServiceE devopsServiceE = new DevopsServiceE();
        BeanUtils.copyProperties(dataObject, devopsServiceE);
        devopsServiceE.setPorts(gson.fromJson(dataObject.getPorts(), new TypeToken<ArrayList<PortMapVO>>() {
        }.getType()));
        return devopsServiceE;
    }

    @Override
    public DevopsServiceVO entityToDto(DevopsServiceE devopsServiceE) {
        DevopsServiceVO devopsServiceVO = new DevopsServiceVO();
        BeanUtils.copyProperties(devopsServiceVO, devopsServiceVO);
        return devopsServiceVO;
    }
}
