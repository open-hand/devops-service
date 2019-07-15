package io.choerodon.devops.infra.convertor;

import java.util.Date;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsMergeRequestVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsMergeRequestE;
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsMergeRequestConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsMergeRequestDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsMergeRequestE;
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsMergeRequestConvertor.java
>>>>>>> [IMP]重构后端断码
import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsMergeRequestConvertor.java

@Component
public class DevopsMergeRequestConvertor implements ConvertorI<DevopsMergeRequestE, DevopsMergeRequestDTO, DevopsMergeRequestVO> {

    @Override
    public DevopsMergeRequestVO entityToDto(DevopsMergeRequestE entity) {
        DevopsMergeRequestVO devopsMergeRequestVO = new DevopsMergeRequestVO();


        BeanUtils.copyProperties(entity, devopsMergeRequestVO);

        return devopsMergeRequestVO;
    }


    @Override
    public DevopsMergeRequestE dtoToEntity(DevopsMergeRequestVO devopsMergeRequestVO) {
        DevopsMergeRequestE devopsMergeRequestE = new DevopsMergeRequestE();
        devopsMergeRequestE.setProjectId(devopsMergeRequestVO.getProject().getId());
        devopsMergeRequestE.setGitlabMergeRequestId(devopsMergeRequestVO.getObjectAttributes().getIid());
        String sourceBranch = devopsMergeRequestVO.getObjectAttributes().getSourceBranch();
        devopsMergeRequestE.setSourceBranch(sourceBranch);
        String targetBranch = devopsMergeRequestVO.getObjectAttributes().getTargetBranch();
        devopsMergeRequestE.setTargetBranch(targetBranch);
        Long authorId = devopsMergeRequestVO.getObjectAttributes().getAuthorId();
        devopsMergeRequestE.setAuthorId(authorId);
        devopsMergeRequestE.setAssigneeId(devopsMergeRequestVO.getObjectAttributes().getAssigneeId());
        String state = devopsMergeRequestVO.getObjectAttributes().getState();
        devopsMergeRequestE.setState(state);
        String title = devopsMergeRequestVO.getObjectAttributes().getTitle();
        devopsMergeRequestE.setTitle(title);
        Date createTime = devopsMergeRequestVO.getObjectAttributes().getCreatedAt();
        devopsMergeRequestE.setCreatedAt(createTime);
        Date updateTime = devopsMergeRequestVO.getObjectAttributes().getUpdatedAt();
        devopsMergeRequestE.setUpdatedAt(updateTime);
        return devopsMergeRequestE;
    }

    @Override
    public DevopsMergeRequestDTO entityToDo(DevopsMergeRequestE entity) {
        DevopsMergeRequestDTO devopsMergeRequestDTO = new DevopsMergeRequestDTO();
        BeanUtils.copyProperties(entity, devopsMergeRequestDTO);
        return devopsMergeRequestDTO;
    }

    @Override
    public DevopsMergeRequestE doToEntity(DevopsMergeRequestDTO dataObject) {
        DevopsMergeRequestE devopsMergeRequestE = new DevopsMergeRequestE();
        BeanUtils.copyProperties(dataObject, devopsMergeRequestE);
        return devopsMergeRequestE;
    }
}
