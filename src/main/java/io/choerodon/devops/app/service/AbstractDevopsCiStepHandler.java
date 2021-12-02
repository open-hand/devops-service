package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import groovy.lang.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitlabCiUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 16:18
 */
public abstract class AbstractDevopsCiStepHandler {

    @Autowired
    @Lazy
    protected DevopsCiStepService devopsCiStepService;

    @Transactional
    public void save(Long projectId, Long devopsCiJobId, DevopsCiStepVO devopsCiStepVO) {
        // 保存步骤
        DevopsCiStepDTO devopsCiStepDTO = ConvertUtils.convertObject(devopsCiStepVO, DevopsCiStepDTO.class);
        devopsCiStepDTO.setId(null);
        devopsCiStepDTO.setProjectId(projectId);
        devopsCiStepDTO.setDevopsCiJobId(devopsCiJobId);
        devopsCiStepService.baseCreate(devopsCiStepDTO);
    }

    /**
     * 根据配置信息构建gitlab-ci脚本
     * @param devopsCiStepDTO
     * @return
     */
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        // 保存步骤
        return GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(devopsCiStepDTO.getScript()), true, true);
    }

    /**
     * 子类如果有关联的配置，则需要重写
     * @param devopsCiStepDTOS
     */
    @Transactional
    public void batchDeleteCascade(List<DevopsCiStepDTO> devopsCiStepDTOS) {
        Set<Long> ids = devopsCiStepDTOS.stream().map(DevopsCiStepDTO::getId).collect(Collectors.toSet());
        devopsCiStepService.batchDeleteByIds(ids);
    }

    public abstract String getType();
}
