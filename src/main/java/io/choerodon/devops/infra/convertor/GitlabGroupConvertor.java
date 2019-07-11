package io.choerodon.devops.infra.convertor;

<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/GitlabGroupConvertor.java
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectE;
=======
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectVO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/GitlabGroupConvertor.java
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.dto.gitlab.GroupDO;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitlabGroupConvertor implements ConvertorI<DevopsProjectVO, GroupDO, Object> {

    @Override
    public DevopsProjectVO doToEntity(GroupDO groupDO) {
        DevopsProjectVO devopsProjectE = new DevopsProjectVO();
        BeanUtils.copyProperties(groupDO, devopsProjectE);
        devopsProjectE.setDevopsAppGroupId(TypeUtil.objToLong(groupDO.getId()));
        return devopsProjectE;
    }

    @Override
    public GroupDO entityToDo(DevopsProjectVO devopsProjectE) {
        GroupDO groupDO = new GroupDO();
        BeanUtils.copyProperties(devopsProjectE, groupDO);
        return groupDO;
    }

}
