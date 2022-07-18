package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.CiPipelineAppVersionService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.CiPipelineAppVersionDTO;
import io.choerodon.devops.infra.mapper.CiPipelineAppVersionMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 发布应用服务版本步骤生成的流水线记录信息(CiPipelineAppVersion)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-14 16:01:30
 */
@Service
public class CiPipelineAppVersionServiceImpl implements CiPipelineAppVersionService {
    @Autowired
    private CiPipelineAppVersionMapper ciPipelineAppVersionMapper;

    @Override
    public CiPipelineAppVersionDTO queryByPipelineIdAndJobName(Long appServiceId, Long gitlabPipelineId, String jobName) {
        Assert.notNull(appServiceId, ResourceCheckConstant.ERROR_APP_SERVICE_ID_IS_NULL);
        Assert.notNull(gitlabPipelineId, ResourceCheckConstant.ERROR_GITLAB_PIPELINE_ID_IS_NULL);
        Assert.notNull(jobName, ResourceCheckConstant.ERROR_JOB_NAME_ID_IS_NULL);

        CiPipelineAppVersionDTO ciPipelineAppVersionDTO = new CiPipelineAppVersionDTO();
        ciPipelineAppVersionDTO.setAppServiceId(appServiceId);
        ciPipelineAppVersionDTO.setGitlabPipelineId(gitlabPipelineId);
        ciPipelineAppVersionDTO.setJobName(jobName);

        return ciPipelineAppVersionMapper.selectOne(ciPipelineAppVersionDTO);
    }

    @Override
    public void baseCreate(CiPipelineAppVersionDTO ciPipelineAppVersionDTO) {
        MapperUtil.resultJudgedInsertSelective(ciPipelineAppVersionMapper, ciPipelineAppVersionDTO, "error.save.app.version");
    }
}
