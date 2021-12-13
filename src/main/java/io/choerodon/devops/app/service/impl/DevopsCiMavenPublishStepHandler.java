package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.DevopsCiMavenPublishConfigVO;
import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.MavenRepoVO;
import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.DevopsCiMavenPublishConfigService;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.DevopsCiMavenPublishConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiMavenSettingsDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCiMavenSettingsMapper;
import io.choerodon.devops.infra.util.*;

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

    private static final String ERROR_CI_MAVEN_SETTINGS_INSERT = "error.maven.settings.insert";

    @Autowired
    private DevopsCiMavenPublishConfigService devopsCiMavenPublishConfigService;

    @Autowired
    private DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper;

    @Autowired
    private RdupmClientOperator rdupmClientOperator;

    @Override
    protected void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
        DevopsCiMavenPublishConfigDTO devopsCiMavenPublishConfigDTO = devopsCiStepVO.getDevopsCiMavenPublishConfigDTO();
        devopsCiMavenPublishConfigDTO.setStepId(stepId);
        devopsCiMavenPublishConfigService.baseCreate(devopsCiMavenPublishConfigDTO);
    }

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        Long projectId = devopsCiStepDTO.getProjectId();
        Long devopsCiJobId = devopsCiStepDTO.getDevopsCiJobId();

        DevopsCiMavenPublishConfigDTO devopsCiMavenPublishConfigDTO = devopsCiMavenPublishConfigService.queryByStepId(devopsCiStepDTO.getId());
        DevopsCiMavenPublishConfigVO devopsCiMavenPublishConfigVO = ConvertUtils.convertObject(devopsCiMavenPublishConfigDTO, DevopsCiMavenPublishConfigVO.class);

        List<MavenRepoVO> targetRepos = new ArrayList<>();
        boolean hasMavenSettings = buildAndSaveJarDeployMavenSettings(projectId,
                devopsCiJobId,
                devopsCiMavenPublishConfigVO,
                devopsCiStepDTO,
                targetRepos);
        return buildMavenJarDeployScripts(projectId,
                devopsCiJobId,
                hasMavenSettings,
                devopsCiMavenPublishConfigVO,
                devopsCiStepDTO,
                targetRepos);
    }

    @Override
    protected void batchDeleteConfig(Set<Long> stepIds) {
        devopsCiMavenPublishConfigService.batchDeleteByStepIds(stepIds);
    }

    /**
     * 生成并存储maven settings到数据库
     *
     * @param projectId                    项目id
     * @param jobId                        job id
     * @param devopsCiMavenPublishConfigVO 配置
     * @param devopsCiStepDTO
     * @param targetRepoContainer          用来存放解析出的目标仓库信息
     * @return 返回true表示有settings信息
     */
    private boolean buildAndSaveJarDeployMavenSettings(Long projectId,
                                                       Long jobId,
                                                       DevopsCiMavenPublishConfigVO devopsCiMavenPublishConfigVO,
                                                       DevopsCiStepDTO devopsCiStepDTO,
                                                       List<MavenRepoVO> targetRepoContainer) {
//        MavenDeployRepoSettings mavenDeployRepoSettings = devopsCiMavenPublishConfigVO.getMavenDeployRepoSettings();
        Long sequence = devopsCiStepDTO.getSequence();
        Set<Long> dependencyRepoIds = devopsCiMavenPublishConfigVO.getNexusMavenRepoIds();
        List<MavenRepoVO> dependencyRepos = devopsCiMavenPublishConfigVO.getRepos();

        boolean targetRepoEmpty = devopsCiMavenPublishConfigVO.getNexusRepoIds() == null;
        boolean dependencyRepoIdsEmpty = CollectionUtils.isEmpty(dependencyRepoIds);
        boolean dependencyRepoEmpty = CollectionUtils.isEmpty(dependencyRepos);

        // 如果都为空, 不生成settings文件
        if (targetRepoEmpty && dependencyRepoIdsEmpty && dependencyRepoEmpty) {
            return false;
        }

        // 查询制品库
        List<NexusMavenRepoDTO> nexusMavenRepoDTOs = rdupmClientOperator.getRepoUserByProject(null,
                projectId,
                ArrayUtil.singleAsSet(devopsCiMavenPublishConfigVO.getNexusRepoIds()));

        // 如果填入的仓库信息和制品库查出的结果都为空, 不生成settings文件
        if (CollectionUtils.isEmpty(nexusMavenRepoDTOs) && dependencyRepoEmpty) {
            return false;
        }

        // 转化制品库信息, 并将目标仓库信息取出, 放入targetRepoContainer
        List<MavenRepoVO> mavenRepoVOS = nexusMavenRepoDTOs.stream().map(r -> {
            MavenRepoVO result = convertRepo(r);
            // 目标仓库不为空, 并且目标仓库包含
            if (!targetRepoEmpty && devopsCiMavenPublishConfigVO.getNexusRepoIds().equals(r.getRepositoryId())) {
                targetRepoContainer.add(result);
            }
            return result;
        }).collect(Collectors.toList());

        // 将手动输入的仓库信息也放入列表
        if (!dependencyRepoEmpty) {
            mavenRepoVOS.addAll(dependencyRepos);
        }

        // 生成settings文件内容
        String settings = MavenSettingsUtil.buildSettings(mavenRepoVOS);
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = new DevopsCiMavenSettingsDTO(jobId, sequence, settings);
        MapperUtil.resultJudgedInsert(devopsCiMavenSettingsMapper, devopsCiMavenSettingsDTO, ERROR_CI_MAVEN_SETTINGS_INSERT);
        return true;
    }

    private static MavenRepoVO convertRepo(NexusMavenRepoDTO nexusMavenRepoDTO) {
        MavenRepoVO mavenRepoVO = new MavenRepoVO();
        mavenRepoVO.setName(nexusMavenRepoDTO.getName());
        mavenRepoVO.setPrivateRepo(Boolean.TRUE);
        if ("MIXED".equals(nexusMavenRepoDTO.getVersionPolicy())) {
            mavenRepoVO.setType(GitOpsConstants.SNAPSHOT + "," + GitOpsConstants.RELEASE);
        } else {
            // group 类型的仓库没有版本类型
            mavenRepoVO.setType(nexusMavenRepoDTO.getVersionPolicy() == null ? null : nexusMavenRepoDTO.getVersionPolicy().toLowerCase());
        }
        mavenRepoVO.setUrl(nexusMavenRepoDTO.getUrl());
        mavenRepoVO.setUsername(nexusMavenRepoDTO.getNeUserId());
        mavenRepoVO.setPassword(nexusMavenRepoDTO.getNeUserPassword());
        return mavenRepoVO;
    }


    /**
     * 生成jar包发布相关的脚本
     *
     * @param projectId          项目id
     * @param jobId              job id
     * @param hasSettings        是否有settings配置
     * @param ciConfigTemplateVO maven发布软件包阶段的信息
     * @param devopsCiStepDTO
     * @param targetMavenRepoVO  目标制品库仓库信息
     * @return 生成的shell脚本
     */
    private List<String> buildMavenJarDeployScripts(final Long projectId,
                                                    final Long jobId,
                                                    final boolean hasSettings,
                                                    DevopsCiMavenPublishConfigVO ciConfigTemplateVO,
                                                    DevopsCiStepDTO devopsCiStepDTO,
                                                    List<MavenRepoVO> targetMavenRepoVO) {
        List<String> shells = new ArrayList<>();
        // 这里这么写是为了考虑之后可能选了多个仓库, 如果是多个仓库的话, 变量替换不便
        // TODO 重构逻辑
        List<String> templateShells = GitlabCiUtil
                .filterLines(GitlabCiUtil.splitLinesForShell(devopsCiStepDTO.getScript()),
                        true,
                        true);
        // 如果有settings配置, 填入获取settings的指令
        if (hasSettings) {
            shells.add(GitlabCiUtil.downloadMavenSettings(projectId, jobId, devopsCiStepDTO.getSequence()));
        }
        // 根据目标仓库信息, 渲染发布jar包的指令
        if (!CollectionUtils.isEmpty(targetMavenRepoVO)) {
            // 插入shell指令将配置的settings文件下载到项目目录下

            // 包含repoId锚点的字符串在templateShells中的索引号
            int repoIdIndex = -1;
            // 包含repoUrl锚点的字符串在templateShells中的索引号
            int repoUrlIndex = -1;
            // 寻找包含这两个锚点的字符串位置
            for (int i = 0; i < templateShells.size(); i++) {
                if (repoIdIndex == -1 && templateShells.get(i).contains(GitOpsConstants.CHOERODON_MAVEN_REPO_ID)) {
                    repoIdIndex = i;
                }
                if (repoUrlIndex == -1 && templateShells.get(i).contains(GitOpsConstants.CHOERODON_MAVEN_REPO_URL)) {
                    repoUrlIndex = i;
                }
                if (repoIdIndex != -1 && repoUrlIndex != -1) {
                    // 没必要再找了
                    break;
                }
            }

            // 为每一个仓库都从模板的脚本中加一份生成的命令
            for (MavenRepoVO repo : targetMavenRepoVO) {
                // 将预定的变量(仓库名和地址)替换为settings.xml文件指定的
                List<String> commands = new ArrayList<>(templateShells);
                if (repoIdIndex != -1) {
                    commands.set(repoIdIndex, commands.get(repoIdIndex).replace(GitOpsConstants.CHOERODON_MAVEN_REPO_ID, repo.getName()));
                }
                if (repoUrlIndex != -1) {
                    commands.set(repoUrlIndex, commands.get(repoUrlIndex).replace(GitOpsConstants.CHOERODON_MAVEN_REPO_URL, repo.getUrl()));
                }
                shells.addAll(commands);
            }

            // 只生成一个jar包元数据上传指令用于CD阶段
            //加上jobId  与sequence，用于查询jar包的时间戳
            shells.add(GitlabCiUtil.saveJarMetadata(ciConfigTemplateVO.getNexusRepoIds(),
                    jobId,
                    devopsCiStepDTO.getSequence()));
        } else {
            // 如果没有目标仓库信息, 则认为用户是自己填入好了maven发布jar的指令, 不需要渲染
            shells.addAll(templateShells);
        }
        return shells;
    }

    @Override
    public String getType() {
        return type.value();
    }
}
