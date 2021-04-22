package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCiContentService;
import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
import io.choerodon.devops.infra.dto.DevopsCiContentDTO;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.mapper.DevopsCiCdPipelineMapper;
import io.choerodon.devops.infra.mapper.DevopsCiContentMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:25
 */
@Service
public class DevopsCiContentServiceImpl implements DevopsCiContentService {
    private static final String ERROR_PIPELINE_TOKEN_MISMATCH = "error.pipeline.token.mismatch";
    private static final String CREATE_CI_CONTENT_FAILED = "create.ci.content.failed";
    private static final String ERROR_PIPELINE_ID_IS_NULL = "error.pipeline.id.is.null";

    private static final String DEFAULT_EMPTY_GITLAB_CI_FILE_PATH = "/component/empty-gitlabci-config.yml";
    /**
     * 默认的空gitlab-ci文件内容
     */
    private static final String DEFAULT_EMPTY_GITLAB_CI_FILE_CONTENT;
    private DevopsCiContentMapper devopsCiContentMapper;
    private DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;


    static {
        try (InputStream inputStream = DevopsClusterServiceImpl.class.getResourceAsStream(DEFAULT_EMPTY_GITLAB_CI_FILE_PATH)) {
            DEFAULT_EMPTY_GITLAB_CI_FILE_CONTENT = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException("error.load.default.empty.gitlab.ci.file");
        }
    }

    public DevopsCiContentServiceImpl(DevopsCiContentMapper devopsCiContentMapper, DevopsCiCdPipelineMapper devopsCiCdPipelineMapper) {
        this.devopsCiContentMapper = devopsCiContentMapper;
        this.devopsCiCdPipelineMapper = devopsCiCdPipelineMapper;
    }

    @Override
    public String queryLatestContent(String pipelineToken) {
        CiCdPipelineDTO devopsCiPipelineDTO = devopsCiCdPipelineMapper.queryByToken(pipelineToken);
        if (devopsCiPipelineDTO == null) {
            throw new DevopsCiInvalidException(ERROR_PIPELINE_TOKEN_MISMATCH);
        }
        if (Boolean.FALSE.equals(devopsCiPipelineDTO.getEnabled())) {
            return DEFAULT_EMPTY_GITLAB_CI_FILE_CONTENT;
        }
        return devopsCiContentMapper.queryLatestContent(devopsCiPipelineDTO.getId());
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
