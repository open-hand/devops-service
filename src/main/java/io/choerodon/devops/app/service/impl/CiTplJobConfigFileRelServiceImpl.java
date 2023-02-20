package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.pipeline.ConfigFileRelVO;
import io.choerodon.devops.app.service.CiTplJobConfigFileRelService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiTplJobConfigFileRelDTO;
import io.choerodon.devops.infra.mapper.CiTplJobConfigFileRelMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * CI任务模板配置文件关联表(CiTplJobConfigFileRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-16 15:50:17
 */
@Service
public class CiTplJobConfigFileRelServiceImpl implements CiTplJobConfigFileRelService {

    private static final String DEVOPS_SAVE_TPL_JOB_CONFIG_FILE_REL_FAILED = "devops.save.tpl.job.config.file.rel.failed";

    @Autowired
    private CiTplJobConfigFileRelMapper ciTplJobConfigFileRelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiTplJobConfigFileRelDTO ciTplJobConfigFileRelDTO) {
        MapperUtil.resultJudgedInsertSelective(ciTplJobConfigFileRelMapper,
                ciTplJobConfigFileRelDTO,
                DEVOPS_SAVE_TPL_JOB_CONFIG_FILE_REL_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByJobId(Long jobId) {
        Assert.notNull(jobId, PipelineCheckConstant.DEVOPS_JOB_ID_IS_NULL);

        CiTplJobConfigFileRelDTO ciTplJobConfigFileRelDTO = new CiTplJobConfigFileRelDTO();
        ciTplJobConfigFileRelDTO.setCiTemplateJobId(jobId);
        ciTplJobConfigFileRelMapper.delete(ciTplJobConfigFileRelDTO);
    }

    @Override
    public List<CiTplJobConfigFileRelDTO> listByJobId(Long jobId) {
        Assert.notNull(jobId, PipelineCheckConstant.DEVOPS_JOB_ID_IS_NULL);

        CiTplJobConfigFileRelDTO ciTplJobConfigFileRelDTO = new CiTplJobConfigFileRelDTO();
        ciTplJobConfigFileRelDTO.setCiTemplateJobId(jobId);
        return ciTplJobConfigFileRelMapper.select(ciTplJobConfigFileRelDTO);
    }

    @Override
    public List<ConfigFileRelVO> listVOByJobId(Long jobId) {
        List<CiTplJobConfigFileRelDTO> ciTplJobConfigFileRelDTOS = listByJobId(jobId);
        if (CollectionUtils.isEmpty(ciTplJobConfigFileRelDTOS)) {
            return new ArrayList<>();
        }
        return ConvertUtils.convertList(ciTplJobConfigFileRelDTOS, ConfigFileRelVO.class);
    }
}

