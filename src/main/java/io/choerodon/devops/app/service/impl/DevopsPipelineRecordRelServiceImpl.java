package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsPipelineRecordRelService;
import io.choerodon.devops.infra.constant.PipelineConstants;
import io.choerodon.devops.infra.dto.DevopsPipelineRecordRelDTO;
import io.choerodon.devops.infra.mapper.DevopsPipelineRecordRelMapper;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/14 20:52
 */
@Service
public class DevopsPipelineRecordRelServiceImpl implements DevopsPipelineRecordRelService {


    private static final String ERROR_UPDATE_PIPELINE_RECORD_REL = "error.update.pipeline.record.rel";
    private static final String ERROR_PIPELINE_RECORD_REL_ID_IS_NULL = "error.pipeline.record.rel.id.is.null";
    @Autowired
    private DevopsPipelineRecordRelMapper devopsPipelineRecordRelMapper;


    @Override
    @Transactional
    public void save(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO) {
        if (devopsPipelineRecordRelMapper.insertSelective(devopsPipelineRecordRelDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_PIPELINE_RECORD_REL);
        }
    }

    @Override
    public DevopsPipelineRecordRelDTO queryByPipelineIdAndCiPipelineRecordId(Long pipelineId, Long ciPipelineRecordId) {
        DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = new DevopsPipelineRecordRelDTO();
        devopsPipelineRecordRelDTO.setPipelineId(pipelineId);
        devopsPipelineRecordRelDTO.setCiPipelineRecordId(ciPipelineRecordId);
        devopsPipelineRecordRelDTO.setCdPipelineRecordId(PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID);
        return devopsPipelineRecordRelMapper.selectOne(devopsPipelineRecordRelDTO);
    }

    @Override
    @Transactional
    public void update(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO) {
        if (devopsPipelineRecordRelMapper.updateByPrimaryKeySelective(devopsPipelineRecordRelDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_PIPELINE_RECORD_REL);
        }
    }

    @Override
    public Page<DevopsPipelineRecordRelDTO> pagingPipelineRel(Long pipelineId, PageRequest cicdPipelineRel) {
        DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = new DevopsPipelineRecordRelDTO();
        devopsPipelineRecordRelDTO.setPipelineId(pipelineId);
        Page<DevopsPipelineRecordRelDTO> devopsPipelineRecordRelDTOS = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(cicdPipelineRel), () -> devopsPipelineRecordRelMapper.select(devopsPipelineRecordRelDTO));
        return devopsPipelineRecordRelDTOS;
    }

    @Override
    public DevopsPipelineRecordRelDTO queryById(Long pipelineRecordRelId) {
        Assert.notNull(pipelineRecordRelId, ERROR_PIPELINE_RECORD_REL_ID_IS_NULL);
        return devopsPipelineRecordRelMapper.selectByPrimaryKey(pipelineRecordRelId);
    }
}
