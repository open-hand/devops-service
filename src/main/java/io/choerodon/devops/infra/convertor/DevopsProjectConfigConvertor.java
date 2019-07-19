package io.choerodon.devops.infra.convertor;

import com.google.gson.Gson;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsProjectConfigVO;
import io.choerodon.devops.api.vo.ProjectConfigVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectConfigE;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsProjectConfigConvertor.java
import io.choerodon.devops.infra.dto.DevopsProjectConfigDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsProjectConfigDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsProjectConfigConvertor.java
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsProjectConfigConvertor.java
import io.choerodon.devops.infra.dataobject.DevopsProjectConfigDO;
=======
import io.choerodon.devops.infra.dto.DevopsProjectConfigDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/DevopsProjectConfigConvertor.java
>>>>>>> [IMP]重构后端断码
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
=======
import io.choerodon.devops.infra.dto.DevopsProjectConfigDTO;
>>>>>>> [IMP] refactor AplicationControler


/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
@Component
public class DevopsProjectConfigConvertor implements ConvertorI<DevopsProjectConfigE, DevopsProjectConfigDTO, DevopsProjectConfigVO> {

    private static final Gson gson = new Gson();

    @Override
    public DevopsProjectConfigE dtoToEntity(DevopsProjectConfigVO devopsProjectConfigVO) {
        DevopsProjectConfigE devopsProjectConfigE = new DevopsProjectConfigE();
        BeanUtils.copyProperties(devopsProjectConfigVO, devopsProjectConfigE);
        return devopsProjectConfigE;
    }

    @Override
    public DevopsProjectConfigVO entityToDto(DevopsProjectConfigE devopsProjectConfigE) {
        DevopsProjectConfigVO devopsProjectConfigVO = new DevopsProjectConfigVO();
        BeanUtils.copyProperties(devopsProjectConfigE, devopsProjectConfigVO);
        return devopsProjectConfigVO;
    }
}
