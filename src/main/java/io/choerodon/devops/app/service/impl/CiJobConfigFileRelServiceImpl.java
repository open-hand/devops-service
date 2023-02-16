package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.app.service.CiJobConfigFileRelService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiJobConfigFileRelDTO;
import io.choerodon.devops.infra.mapper.CiJobConfigFileRelMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * CI配置文件关联表(CiJobConfigFileRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-16 15:50:03
 */
@Service
public class CiJobConfigFileRelServiceImpl implements CiJobConfigFileRelService {

    private static final String DEVOPS_SAVE_JOB_CONFIG_REL_FAILED = "devops.save.job.config.rel.failed";

    @Autowired
    private CiJobConfigFileRelMapper ciJobConfigFileRelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiJobConfigFileRelDTO ciJobConfigFileRelDTO) {
        MapperUtil.resultJudgedInsertSelective(ciJobConfigFileRelMapper, ciJobConfigFileRelDTO, DEVOPS_SAVE_JOB_CONFIG_REL_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByJobIds(List<Long> jobIds) {
        if (!CollectionUtils.isEmpty(jobIds)) {
            ciJobConfigFileRelMapper.deleteByJobIds(jobIds);
        }
    }

    @Override
    public List<CiJobConfigFileRelDTO> listByJobId(Long jobId) {
        Assert.notNull(jobId, PipelineCheckConstant.DEVOPS_JOB_ID_IS_NULL);

        CiJobConfigFileRelDTO ciJobConfigFileRelDTO = new CiJobConfigFileRelDTO();
        ciJobConfigFileRelDTO.setCiJobId(jobId);

        return ciJobConfigFileRelMapper.select(ciJobConfigFileRelDTO);
    }
}

