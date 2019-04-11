package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.PipelineUserRecordRelE;
import io.choerodon.devops.domain.application.repository.PipelineUserRelRecordRepository;
import io.choerodon.devops.infra.dataobject.PipelineUserRecordRelDO;
import io.choerodon.devops.infra.mapper.PipelineUserRecordRelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:26 2019/4/9
 * Description:
 */
@Component
public class PipelineUserRelRecordRepositoryImpl implements PipelineUserRelRecordRepository {
    @Autowired
    private PipelineUserRecordRelMapper recordRelMapper;

    @Override
    public PipelineUserRecordRelE create(PipelineUserRecordRelE recordRelE) {
        PipelineUserRecordRelDO recordRelDO = ConvertHelper.convert(recordRelE, PipelineUserRecordRelDO.class);
        if (recordRelMapper.insert(recordRelDO) != 1) {
            throw new CommonException("error.insert.pipeline.user.record");
        }
        return ConvertHelper.convert(recordRelMapper.selectOne(recordRelDO), PipelineUserRecordRelE.class);
    }
}
