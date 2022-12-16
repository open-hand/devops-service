package io.choerodon.devops.app.eventhandler.pipeline.step;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import groovy.lang.Lazy;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.DevopsCiStepService;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
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

    /**
     * 添加模板步骤的配置信息，含有配置的子类自行重写
     *
     * @param ciTemplateStepVO
     */
    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {

    }

    public void saveTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {

    }

    public void deleteTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {

    }

    /**
     * 添加模板步骤的配置信息，含有配置的子类自行重写
     *
     * @param devopsCiStepVO
     */
    public void fillTemplateStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {

    }

    /**
     * 添加项目下流水线的配置信息
     *
     * @param devopsCiStepVO
     */
    public void fillStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {

    }

    /**
     * 保存步骤信息，如果存在配置信息也一并保存
     *
     * @param projectId      项目id
     * @param devopsCiJobId  所属jobId
     * @param devopsCiStepVO 步骤相关信息
     */
    @Transactional
    public void save(Long projectId, Long devopsCiJobId, DevopsCiStepVO devopsCiStepVO) {
        // 保存步骤
        DevopsCiStepDTO devopsCiStepDTO = saveStep(projectId, devopsCiJobId, devopsCiStepVO);
        // 保存任务配置
        saveConfig(devopsCiStepDTO.getId(), devopsCiStepVO);
    }

    /**
     * 保存任务配置，如果需要的话，子类负责实现
     *
     * @param stepId         步骤id
     * @param devopsCiStepVO 步骤信息
     */
    public void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
        // do nothing
    }

    @NotNull
    private DevopsCiStepDTO saveStep(Long projectId, Long devopsCiJobId, DevopsCiStepVO devopsCiStepVO) {
        DevopsCiStepDTO devopsCiStepDTO = ConvertUtils.convertObject(devopsCiStepVO, DevopsCiStepDTO.class);
        devopsCiStepDTO.setId(null);
        devopsCiStepDTO.setProjectId(projectId);
        devopsCiStepDTO.setDevopsCiJobId(devopsCiJobId);
        devopsCiStepService.baseCreate(devopsCiStepDTO);
        return devopsCiStepDTO;
    }


    /**
     * 根据配置信息构建gitlab-ci脚本
     *
     * @param devopsCiStepDTO 步骤信息
     * @return gitlab-ci脚本
     */
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        // 保存步骤
        return GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(devopsCiStepDTO.getScript()), true, true);
    }

    /**
     * 批量删除步骤信息
     *
     * @param devopsCiStepDTOS 要删除的步骤列表
     */
    @Transactional
    public void batchDeleteCascade(List<DevopsCiStepDTO> devopsCiStepDTOS) {
        Set<Long> ids = devopsCiStepDTOS.stream().map(DevopsCiStepDTO::getId).collect(Collectors.toSet());
        devopsCiStepService.batchDeleteByIds(ids);

        // 删除关联的配置,如果有的话
        batchDeleteConfig(ids);
    }


    /**
     * 批量删除步骤关联的配置（如果有关联的配置，子类则实现相关删除逻辑，没有则不做任何处理）
     *
     * @param stepIds 步骤ids
     */
    public void batchDeleteConfig(Set<Long> stepIds) {

    }

    /**
     * 步骤类型
     *
     * @return 步骤类型枚举
     */
    public abstract DevopsCiStepTypeEnum getType();

    public Boolean isComplete(DevopsCiStepVO devopsCiStepVO) {
        if (!StringUtils.hasText(devopsCiStepVO.getName())) {
            return false;
        }
        if (!StringUtils.hasText(devopsCiStepVO.getType())) {
            return false;
        }
        return isConfigComplete(devopsCiStepVO);
    }

    protected Boolean isConfigComplete(DevopsCiStepVO devopsCiStepVO) {
        return true;
    }
}
