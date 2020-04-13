package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCiContentService;
import io.choerodon.devops.infra.dto.DevopsCiContentDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineDTO;
import io.choerodon.devops.infra.mapper.DevopsCiContentMapper;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

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

    private static final String DEFAULT_EMPTY_GITLAB_CI_FILE_PATH = "/component/empty-gitlabci-config.yml";
    private DevopsCiContentMapper devopsCiContentMapper;
    private DevopsCiPipelineMapper devopsCiPipelineMapper;

    public DevopsCiContentServiceImpl(DevopsCiContentMapper devopsCiContentMapper, DevopsCiPipelineMapper devopsCiPipelineMapper) {
        this.devopsCiContentMapper = devopsCiContentMapper;
        this.devopsCiPipelineMapper = devopsCiPipelineMapper;
    }

    @Override
    public String queryLatestContent(Long pipelineId) {
        DevopsCiPipelineDTO devopsCiPipelineDTO = devopsCiPipelineMapper.selectByPrimaryKey(pipelineId);
        if (Boolean.FALSE.equals(devopsCiPipelineDTO.getEnabled())) {
            String content = "";
            try {
                content = StreamUtils.copyToString(this.getClass().getClassLoader().getResourceAsStream(DEFAULT_EMPTY_GITLAB_CI_FILE_PATH), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content;
        }
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
