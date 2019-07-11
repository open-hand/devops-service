package io.choerodon.devops.domain.application.convertor;

import io.choerodon.devops.domain.application.entity.DevopsProjectE;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.gitlab.GroupDO;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitlabGroupConvertor implements ConvertorI<DevopsProjectE, GroupDO, Object> {

    @Override
    public DevopsProjectE doToEntity(GroupDO groupDO) {
        DevopsProjectE devopsProjectE = new DevopsProjectE();
        BeanUtils.copyProperties(groupDO, devopsProjectE);
        devopsProjectE.setDevopsAppGroupId(TypeUtil.objToLong(groupDO.getId()));
        return devopsProjectE;
    }

    @Override
    public GroupDO entityToDo(DevopsProjectE devopsProjectE) {
        GroupDO groupDO = new GroupDO();
        BeanUtils.copyProperties(devopsProjectE, groupDO);
        return groupDO;
    }

}
