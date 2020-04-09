package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCiContentService;
import io.choerodon.devops.infra.dto.DevopsCiContentDTO;
import io.choerodon.devops.infra.mapper.DevopsCiContentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:25
 */
@Service
public class DevopsCiContentServiceImpl implements DevopsCiContentService {

    private static final String CREATE_CI_CONTENT_FAILED = "create.ci.content.failed";
    private static final String ERROR_PIPELINE_ID_IS_NULL = "error.pipeline.id.is.null";
    private DevopsCiContentMapper devopsCiContentMapper;

    public DevopsCiContentServiceImpl(DevopsCiContentMapper devopsCiContentMapper) {
        this.devopsCiContentMapper = devopsCiContentMapper;
    }

    @Override
    public String queryLatestContent(Long pipelineId) {
        return devopsCiContentMapper.queryLatestContent(pipelineId);
    }

    @Override
    public void create(DevopsCiContentDTO devopsCiContentDTO) {
        if (devopsCiContentMapper.insertSelective(devopsCiContentDTO) != 1) {
            throw new CommonException(CREATE_CI_CONTENT_FAILED);
        }
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        DevopsCiContentDTO devopsCiContentDTO = new DevopsCiContentDTO();
        devopsCiContentDTO.setCiPipelineId(ciPipelineId);
        devopsCiContentMapper.delete(devopsCiContentDTO);
    }
}
