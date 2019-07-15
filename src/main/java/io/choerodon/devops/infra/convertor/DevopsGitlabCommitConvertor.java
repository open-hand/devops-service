package io.choerodon.devops.infra.convertor;


import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsGitlabCommitVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsGitlabCommitE;
import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsGitlabCommitConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsGitlabCommitDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsGitlabCommitE;
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsGitlabCommitConvertor.java
>>>>>>> [IMP] 重构Repository
import io.choerodon.devops.infra.dataobject.DevopsGitlabCommitDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsGitlabCommitConvertor.java

@Component
public class DevopsGitlabCommitConvertor implements ConvertorI<DevopsGitlabCommitE, DevopsGitlabCommitDTO, DevopsGitlabCommitVO> {

    @Override
    public DevopsGitlabCommitE doToEntity(DevopsGitlabCommitDTO devopsGitlabCommitDO) {
        DevopsGitlabCommitE devopsGitlabCommitE = new DevopsGitlabCommitE();
        BeanUtils.copyProperties(devopsGitlabCommitDO, devopsGitlabCommitE);
        return devopsGitlabCommitE;
    }

    @Override
    public DevopsGitlabCommitDTO entityToDo(DevopsGitlabCommitE devopsGitlabCommitE) {
        DevopsGitlabCommitDTO devopsGitlabCommitDO = new DevopsGitlabCommitDTO();
        BeanUtils.copyProperties(devopsGitlabCommitE, devopsGitlabCommitDO);
        return devopsGitlabCommitDO;
    }

    @Override
    public DevopsGitlabCommitVO entityToDto(DevopsGitlabCommitE devopsGitlabCommitE) {
        DevopsGitlabCommitVO devopsGitlabCommitDTO = new DevopsGitlabCommitVO();
        BeanUtils.copyProperties(devopsGitlabCommitE, devopsGitlabCommitDTO);
        return devopsGitlabCommitDTO;
    }
}
