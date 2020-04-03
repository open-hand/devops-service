package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCiContentService;
import io.choerodon.devops.infra.dto.DevopsCiContentDTO;
import io.choerodon.devops.infra.mapper.DevopsCiContentMapper;
import org.springframework.stereotype.Service;

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
}
