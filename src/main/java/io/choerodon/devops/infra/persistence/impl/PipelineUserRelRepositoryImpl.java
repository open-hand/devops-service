package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.PipelineUserRelE;
import io.choerodon.devops.domain.application.repository.PipelineUserRelRepository;
import io.choerodon.devops.infra.dataobject.PipelineUserRelDO;
import io.choerodon.devops.infra.mapper.PipelineUserRelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:10 2019/4/8
 * Description:
 */
@Component
public class PipelineUserRelRepositoryImpl implements PipelineUserRelRepository {
    @Autowired
    private PipelineUserRelMapper userRelMapper;

    @Override
    public void create(PipelineUserRelE pipelineUserRelE) {
        PipelineUserRelDO pipelineUserRelDO = ConvertHelper.convert(pipelineUserRelE, PipelineUserRelDO.class);
        if (userRelMapper.insert(pipelineUserRelDO) != 1) {
            throw new CommonException("error.insert.pipeline.user");
        }
    }

    @Override
    public List<PipelineUserRelE> listByOptions(Long pipelineId, Long stageId, Long taskId) {
        PipelineUserRelDO pipelineUserRelDO = new PipelineUserRelDO(pipelineId, stageId, taskId);
        return ConvertHelper.convertList(userRelMapper.select(pipelineUserRelDO), PipelineUserRelE.class);
    }

    @Override
    public void delete(Long userRelId) {
        PipelineUserRelDO pipelineUserRelDO = new PipelineUserRelDO();
        pipelineUserRelDO.setId(userRelId);
        userRelMapper.deleteByPrimaryKey(pipelineUserRelDO);
    }
}
