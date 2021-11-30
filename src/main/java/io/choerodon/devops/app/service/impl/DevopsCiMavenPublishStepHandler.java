package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.CiConfigTemplateVO;
import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.MavenDeployRepoSettings;
import io.choerodon.devops.api.vo.MavenRepoVO;
import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.infra.dto.DevopsCiMavenSettingsDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.util.ArrayUtil;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 16:32
 */
@Component
public class DevopsCiMavenPublishStepHandler extends AbstractDevopsCiStepHandler {
    protected DevopsCiStepTypeEnum type = DevopsCiStepTypeEnum.MAVEN_PUBLISH;

    @Override
    public void save(Long projectId, Long devopsCiJobId, DevopsCiStepVO devopsCiStepVO) {
        super.save(projectId, devopsCiJobId, devopsCiStepVO);
    }

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        Long projectId = devopsCiStepDTO.getProjectId();
        Long devopsCiJobId = devopsCiStepDTO.getDevopsCiJobId();



        List<MavenRepoVO> targetRepos = new ArrayList<>();
        boolean hasMavenSettings = buildAndSaveJarDeployMavenSettings(projectId, devopsCiJobId, config, targetRepos);
        buildMavenJarDeployScripts(projectId, jobId, hasMavenSettings, config, targetRepos);
    }

    @Override
    public void batchDeleteCascade(List<DevopsCiStepDTO> devopsCiStepDTOS) {
        super.batchDeleteCascade(devopsCiStepDTOS);
    }

    /**
     * 生成并存储maven settings到数据库
     *
     * @param projectId           项目id
     * @param jobId               job id
     * @param ciConfigTemplateVO  配置
     * @param targetRepoContainer 用来存放解析出的目标仓库信息
     * @return 返回true表示有settings信息
     */
    private boolean buildAndSaveJarDeployMavenSettings(Long projectId, Long jobId, CiConfigTemplateVO ciConfigTemplateVO, List<MavenRepoVO> targetRepoContainer) {
        MavenDeployRepoSettings mavenDeployRepoSettings = ciConfigTemplateVO.getMavenDeployRepoSettings();
        Long sequence = ciConfigTemplateVO.getSequence();
        Set<Long> dependencyRepoIds = ciConfigTemplateVO.getNexusMavenRepoIds();
        List<MavenRepoVO> dependencyRepos = ciConfigTemplateVO.getRepos();

        boolean targetRepoEmpty = mavenDeployRepoSettings.getNexusRepoIds() == null;
        boolean dependencyRepoIdsEmpty = CollectionUtils.isEmpty(dependencyRepoIds);
        boolean dependencyRepoEmpty = CollectionUtils.isEmpty(dependencyRepos);

        // 如果都为空, 不生成settings文件
        if (targetRepoEmpty && dependencyRepoIdsEmpty && dependencyRepoEmpty) {
            return false;
        }

        // 查询制品库
        List<NexusMavenRepoDTO> nexusMavenRepoDTOs = rdupmClientOperator.getRepoUserByProject(null, projectId, ArrayUtil.singleAsSet(mavenDeployRepoSettings.getNexusRepoIds()));

        // 如果填入的仓库信息和制品库查出的结果都为空, 不生成settings文件
        if (CollectionUtils.isEmpty(nexusMavenRepoDTOs) && dependencyRepoEmpty) {
            return false;
        }

        // 转化制品库信息, 并将目标仓库信息取出, 放入targetRepoContainer
        List<MavenRepoVO> mavenRepoVOS = nexusMavenRepoDTOs.stream().map(r -> {
            MavenRepoVO result = convertRepo(r);
            // 目标仓库不为空, 并且目标仓库包含
            if (!targetRepoEmpty && mavenDeployRepoSettings.getNexusRepoIds().equals(r.getRepositoryId())) {
                targetRepoContainer.add(result);
            }
            return result;
        }).collect(Collectors.toList());

        // 将手动输入的仓库信息也放入列表
        if (!dependencyRepoEmpty) {
            mavenRepoVOS.addAll(dependencyRepos);
        }

        // 生成settings文件内容
        String settings = buildSettings(mavenRepoVOS);
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = new DevopsCiMavenSettingsDTO(jobId, sequence, settings);
        MapperUtil.resultJudgedInsert(devopsCiMavenSettingsMapper, devopsCiMavenSettingsDTO, ERROR_CI_MAVEN_SETTINGS_INSERT);
        return true;
    }
}
