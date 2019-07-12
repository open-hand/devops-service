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
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitlabGroupConvertor implements ConvertorI<DevopsProjectVO, GroupDTO, Object> {

    @Override
    public DevopsProjectVO doToEntity(GroupDTO groupDTO) {
        DevopsProjectVO devopsProjectE = new DevopsProjectVO();
        BeanUtils.copyProperties(groupDTO, devopsProjectE);
        devopsProjectE.setDevopsAppGroupId(TypeUtil.objToLong(groupDTO.getId()));
        return devopsProjectE;
    }

    @Override
    public GroupDTO entityToDo(DevopsProjectVO devopsProjectE) {
        GroupDTO groupDTO = new GroupDTO();
        BeanUtils.copyProperties(devopsProjectE, groupDTO);
        return groupDTO;
    }

}
