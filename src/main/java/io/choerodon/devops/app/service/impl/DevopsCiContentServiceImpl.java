package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCdStageService;
import io.choerodon.devops.app.service.DevopsCiContentService;
import io.choerodon.devops.app.service.DevopsCiPipelineService;
import io.choerodon.devops.app.service.DevopsCiStageService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
import io.choerodon.devops.infra.dto.DevopsCdStageDTO;
import io.choerodon.devops.infra.dto.DevopsCiContentDTO;
import io.choerodon.devops.infra.dto.DevopsCiStageDTO;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.mapper.DevopsCiCdPipelineMapper;
import io.choerodon.devops.infra.mapper.DevopsCiContentMapper;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:25
 */
@Service
public class DevopsCiContentServiceImpl implements DevopsCiContentService {
    private static final String DEVOPS_PIPELINE_TOKEN_MISMATCH = "devops.pipeline.token.mismatch";
    private static final String DEVOPS_CREATE_CI_CONTENT_FAILED = "devops.create.ci.content.failed";
    private static final String DEVOPS_SAVE_CONTENT_FAILED = "devops.save.content.failed";
    private static final String DEVOPS_UPDATE_CONTENT_FAILED = "devops.update.content.failed";
    private static final String DEVOPS_LOAD_DEFAULT_EMPTY_GITLAB_CI_FILE = "devops.load.default.empty.gitlab.ci.file";

    private static final String DEFAULT_EMPTY_GITLAB_CI_FILE_PATH = "/component/empty-gitlabci-config.yml";
    private static final String DEFAULT_EMPTY_GITLAB_CI_FILE_FOR_CD_PATH = "/component/empty-gitlabci-config-for-cd.yml";
    /**
     * 默认的空gitlab-ci文件内容
     */
    private static final String DEFAULT_EMPTY_GITLAB_CI_FILE_CONTENT;
    /**
     * 默认的空gitlab-ci文件内容
     */
    private static final String DEFAULT_EMPTY_GITLAB_CI_FILE_CONTENT_FOR_CD;
    private DevopsCiContentMapper devopsCiContentMapper;
    private DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;
    private DevopsCiPipelineService devopsCiPipelineService;

    static {
        try (InputStream inputStream = DevopsClusterServiceImpl.class.getResourceAsStream(DEFAULT_EMPTY_GITLAB_CI_FILE_PATH)) {
            DEFAULT_EMPTY_GITLAB_CI_FILE_CONTENT = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException(DEVOPS_LOAD_DEFAULT_EMPTY_GITLAB_CI_FILE);
        }
        try (InputStream inputStream = DevopsClusterServiceImpl.class.getResourceAsStream(DEFAULT_EMPTY_GITLAB_CI_FILE_FOR_CD_PATH)) {
            DEFAULT_EMPTY_GITLAB_CI_FILE_CONTENT_FOR_CD = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException(DEVOPS_LOAD_DEFAULT_EMPTY_GITLAB_CI_FILE);
        }
    }

    @Autowired
    private DevopsCiStageService devopsCiStageService;

    @Value("${services.gateway.url}")
    private String gatewayUrl;
    @Value("${devops.ci.default.rule-number}")
    private Long defaultRuleNumber;
    @Autowired
    private DevopsCdStageService devopsCdStageService;

    public DevopsCiContentServiceImpl(DevopsCiContentMapper devopsCiContentMapper,
                                      DevopsCiCdPipelineMapper devopsCiCdPipelineMapper,
                                      @Lazy DevopsCiPipelineService devopsCiPipelineService) {
        this.devopsCiContentMapper = devopsCiContentMapper;
        this.devopsCiCdPipelineMapper = devopsCiCdPipelineMapper;
        this.devopsCiPipelineService = devopsCiPipelineService;
    }

    @Override
    public String queryLatestContent(String pipelineToken) {
        CiCdPipelineDTO devopsCiPipelineDTO = devopsCiCdPipelineMapper.queryByToken(pipelineToken);
        return queryLatestContent(devopsCiPipelineDTO);
    }

    @Override
    public String queryLatestContent(CiCdPipelineDTO devopsCiPipelineDTO) {
        if (devopsCiPipelineDTO == null) {
            throw new DevopsCiInvalidException(DEVOPS_PIPELINE_TOKEN_MISMATCH);
        }
        if (Boolean.FALSE.equals(devopsCiPipelineDTO.getEnabled())) {
            return DEFAULT_EMPTY_GITLAB_CI_FILE_CONTENT;
        }
        // 只有纯cd流水线返回一个
        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(devopsCiPipelineDTO.getId());
        List<DevopsCdStageDTO> devopsCdStageDTOList = devopsCdStageService.queryByPipelineId(devopsCiPipelineDTO.getId());
        if (CollectionUtils.isEmpty(devopsCiStageDTOList) && !CollectionUtils.isEmpty(devopsCdStageDTOList)) {
            return DEFAULT_EMPTY_GITLAB_CI_FILE_CONTENT_FOR_CD;
        }

        DevopsCiContentDTO devopsCiContentDTO = devopsCiContentMapper.queryLatestContent(devopsCiPipelineDTO.getId());

        String ciContent;
        // 需要重新生成yaml的情况有两种
        // 1. 流水线有修改
        // 2. devops的渲染规则有变动
        if (devopsCiContentDTO == null) {
            ciContent = devopsCiPipelineService.generateGitlabCiYaml(devopsCiPipelineDTO);

            // 缓存配置
            devopsCiContentDTO = new DevopsCiContentDTO();
            devopsCiContentDTO.setCiContentFile(ciContent);
            devopsCiContentDTO.setDevopsDefaultRuleNumber(defaultRuleNumber);
            devopsCiContentDTO.setPipelineVersionNumber(devopsCiPipelineDTO.getObjectVersionNumber());
            MapperUtil.resultJudgedInsertSelective(devopsCiContentMapper,
                    devopsCiContentDTO,
                    DEVOPS_SAVE_CONTENT_FAILED);
        } else if (devopsCiContentDTO.getPipelineVersionNumber() < devopsCiPipelineDTO.getObjectVersionNumber()
                || devopsCiContentDTO.getDevopsDefaultRuleNumber() < defaultRuleNumber) {
            ciContent = devopsCiPipelineService.generateGitlabCiYaml(devopsCiPipelineDTO);

            devopsCiContentDTO.setCiContentFile(ciContent);
            devopsCiContentDTO.setDevopsDefaultRuleNumber(defaultRuleNumber);
            devopsCiContentDTO.setPipelineVersionNumber(devopsCiPipelineDTO.getObjectVersionNumber());
            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiContentMapper,
                    devopsCiContentDTO,
                    DEVOPS_UPDATE_CONTENT_FAILED);
        } else {
            ciContent = devopsCiContentDTO.getCiContentFile();
        }

        Map<String, String> params = new HashMap<>();
        gatewayUrl = gatewayUrl.endsWith("/") ? gatewayUrl.substring(0, gatewayUrl.length() - 1) : gatewayUrl;
        params.put("${CHOERODON_URL}", gatewayUrl);
        return FileUtil.replaceReturnString(ciContent, params);
    }

    @Override
    public void create(DevopsCiContentDTO devopsCiContentDTO) {
        if (devopsCiContentMapper.insertSelective(devopsCiContentDTO) != 1) {
            throw new CommonException(DEVOPS_CREATE_CI_CONTENT_FAILED);
        }
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);
        }
        DevopsCiContentDTO devopsCiContentDTO = new DevopsCiContentDTO();
        devopsCiContentDTO.setCiPipelineId(ciPipelineId);
        devopsCiContentMapper.delete(devopsCiContentDTO);
    }
}
