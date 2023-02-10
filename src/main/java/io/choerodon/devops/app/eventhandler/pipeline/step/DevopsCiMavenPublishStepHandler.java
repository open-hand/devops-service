package io.choerodon.devops.app.eventhandler.pipeline.step;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import io.choerodon.devops.api.vo.DevopsCiMavenPublishConfigVO;
import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.MavenRepoVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiTemplateMavenPublishService;
import io.choerodon.devops.app.service.DevopsCiMavenPublishConfigService;
import io.choerodon.devops.infra.config.ProxyProperties;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.CiTemplateMavenPublishDTO;
import io.choerodon.devops.infra.dto.DevopsCiMavenPublishConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiMavenSettingsDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.dto.maven.Proxy;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.enums.pipline.MavenGavSourceTypeEnum;
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

    private static final String ERROR_CI_MAVEN_SETTINGS_INSERT = "devops.maven.settings.insert";
    private static final String EXPORT_VAR_TPL = "export %s=%s";

    @Autowired
    private DevopsCiMavenPublishConfigService devopsCiMavenPublishConfigService;
    @Autowired
    private CiTemplateMavenPublishService ciTemplateMavenPublishService;

    @Autowired
    private DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper;

    @Autowired
    private RdupmClientOperator rdupmClientOperator;
    @Autowired
    private ProxyProperties proxyProperties;

    @Override
    @Transactional
    public void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
        if (devopsCiStepVO.getMavenPublishConfig() != null) {
            DevopsCiMavenPublishConfigDTO devopsCiMavenPublishConfigDTO = voToDto(devopsCiStepVO.getMavenPublishConfig());
            devopsCiMavenPublishConfigDTO.setId(null);
            devopsCiMavenPublishConfigDTO.setStepId(stepId);

            devopsCiMavenPublishConfigService.baseCreate(devopsCiMavenPublishConfigDTO);
        }
    }


    @Override
    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {
        CiTemplateMavenPublishDTO ciTemplateMavenPublishDTO = ciTemplateMavenPublishService.queryByStepId(ciTemplateStepVO.getId());
        if (ciTemplateMavenPublishDTO != null) {
            if (!ObjectUtils.isEmpty(ciTemplateMavenPublishDTO.getNexusMavenRepoIdStr())) {
                ciTemplateMavenPublishDTO.setNexusMavenRepoIds(JsonHelper.unmarshalByJackson(ciTemplateMavenPublishDTO.getNexusMavenRepoIdStr(), new TypeReference<Set<Long>>() {
                }));
            }
            if (!ObjectUtils.isEmpty(ciTemplateMavenPublishDTO.getTargetRepoStr())) {
                ciTemplateMavenPublishDTO.setTargetRepo(JsonHelper.unmarshalByJackson(ciTemplateMavenPublishDTO.getTargetRepoStr(), MavenRepoVO.class));
            }

            if (!ObjectUtils.isEmpty(ciTemplateMavenPublishDTO.getRepoStr())) {
                ciTemplateMavenPublishDTO.setRepos(JsonHelper.unmarshalByJackson(ciTemplateMavenPublishDTO.getRepoStr(), new TypeReference<List<MavenRepoVO>>() {
                }));
            }
            ciTemplateStepVO.setMavenPublishConfig(ciTemplateMavenPublishDTO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {
        if (ciTemplateStepVO.getMavenPublishConfig() != null) {
            CiTemplateMavenPublishDTO ciTemplateMavenPublishDTO = ciTemplateMavenPublishService.voToDto(ciTemplateStepVO.getMavenPublishConfig());
            ciTemplateMavenPublishService.baseCreate(ciTemplateStepVO.getId(), ciTemplateMavenPublishDTO);
        }
    }

    @Override
    public void deleteTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {
        ciTemplateMavenPublishService.deleteByTemplateId(ciTemplateStepVO.getId());
    }

    @Override
    public void fillTemplateStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        CiTemplateMavenPublishDTO ciTemplateMavenPublishDTO = ciTemplateMavenPublishService.queryByStepId(devopsCiStepVO.getId());
        if (ciTemplateMavenPublishDTO != null) {
            DevopsCiMavenPublishConfigDTO devopsCiMavenPublishConfigDTO = ConvertUtils.convertObject(ciTemplateMavenPublishDTO, DevopsCiMavenPublishConfigDTO.class);
            devopsCiStepVO.setMavenPublishConfig(dtoToVo(devopsCiMavenPublishConfigDTO));
        }

    }

    @Override
    public void fillStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        DevopsCiMavenPublishConfigDTO devopsCiMavenPublishConfigDTO = devopsCiMavenPublishConfigService.queryByStepId(devopsCiStepVO.getId());
        if (devopsCiMavenPublishConfigDTO != null) {
            devopsCiStepVO.setMavenPublishConfig(dtoToVo(devopsCiMavenPublishConfigDTO));
        }

    }

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        Long projectId = devopsCiStepDTO.getProjectId();
        Long devopsCiJobId = devopsCiStepDTO.getDevopsCiJobId();

        DevopsCiMavenPublishConfigDTO devopsCiMavenPublishConfigDTO = devopsCiMavenPublishConfigService.queryByStepId(devopsCiStepDTO.getId());
        DevopsCiMavenPublishConfigVO devopsCiMavenPublishConfigVO = dtoToVo(devopsCiMavenPublishConfigDTO);

        List<MavenRepoVO> targetRepos = new ArrayList<>();
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = buildAndSaveJarDeployMavenSettings(projectId,
                devopsCiJobId,
                devopsCiMavenPublishConfigVO,
                devopsCiStepDTO,
                targetRepos);
        return buildMavenJarDeployScripts(projectId,
                devopsCiMavenSettingsDTO,
                devopsCiMavenPublishConfigVO,
                devopsCiStepDTO,
                targetRepos);
    }

    @Override
    public void batchDeleteConfig(Set<Long> stepIds) {
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
    private DevopsCiMavenSettingsDTO buildAndSaveJarDeployMavenSettings(Long projectId,
                                                                        Long jobId,
                                                                        DevopsCiMavenPublishConfigVO devopsCiMavenPublishConfigVO,
                                                                        DevopsCiStepDTO devopsCiStepDTO,
                                                                        List<MavenRepoVO> targetRepoContainer) {
        Long sequence = devopsCiStepDTO.getSequence();
        Set<Long> dependencyRepoIds = devopsCiMavenPublishConfigVO.getNexusMavenRepoIds();
        List<MavenRepoVO> dependencyRepos = devopsCiMavenPublishConfigVO.getRepos();

        boolean targetRepoEmpty = devopsCiMavenPublishConfigVO.getNexusRepoId() == null && devopsCiMavenPublishConfigVO.getTargetRepo() == null;
        boolean dependencyRepoIdsEmpty = CollectionUtils.isEmpty(dependencyRepoIds);
        boolean dependencyRepoEmpty = CollectionUtils.isEmpty(dependencyRepos);
        boolean settingsEmpty = !StringUtils.hasText(devopsCiMavenPublishConfigVO.getMavenSettings());

        // 如果都为空, 不生成settings文件
        if (targetRepoEmpty && dependencyRepoIdsEmpty && dependencyRepoEmpty && settingsEmpty) {
            return null;
        }

        List<Proxy> proxies = new ArrayList<>();
        if (proxyProperties != null && Boolean.TRUE.equals(proxyProperties.getActive())) {
            proxies.add(ConvertUtils.convertObject(proxyProperties, Proxy.class));
        }

        // 查询制品库
        List<MavenRepoVO> mavenRepoVOS = new ArrayList<>();
        if (devopsCiMavenPublishConfigVO.getNexusRepoId() != null) {
            Set<Long> ids = new HashSet<>();
            ids.add(devopsCiMavenPublishConfigVO.getNexusRepoId());
            if (!CollectionUtils.isEmpty(dependencyRepoIds)) {
                ids.addAll(dependencyRepoIds);
            }
            List<NexusMavenRepoDTO> nexusMavenRepoDTOs = rdupmClientOperator.getRepoUserByProject(null,
                    projectId,
                    ids);
            // 如果填入的仓库信息和制品库查出的结果都为空, 不生成settings文件
            if (CollectionUtils.isEmpty(nexusMavenRepoDTOs) && dependencyRepoEmpty) {
                return null;
            }
            // 转化制品库信息, 并将目标仓库信息取出, 放入targetRepoContainer

            mavenRepoVOS = nexusMavenRepoDTOs.stream().map(r -> {
                MavenRepoVO result = convertRepo(r);
                // 目标仓库不为空, 并且目标仓库包含
                if (!targetRepoEmpty && devopsCiMavenPublishConfigVO.getNexusRepoId().equals(r.getRepositoryId())) {
                    targetRepoContainer.add(result);
                }
                return result;
            }).collect(Collectors.toList());
        } else {
            MavenRepoVO targetRepo = devopsCiMavenPublishConfigVO.getTargetRepo();
            mavenRepoVOS.add(targetRepo);
            targetRepoContainer.add(targetRepo);
        }

        // 将手动输入的仓库信息也放入列表
        if (!dependencyRepoEmpty) {
            mavenRepoVOS.addAll(dependencyRepos);
        }

        // 生成settings文件内容
        String settings;
        // 如果填了自定义的settings文件则直接使用用户填的
        if (settingsEmpty) {
            settings = MavenSettingsUtil.buildSettings(mavenRepoVOS, proxies);
        } else {
            settings = devopsCiMavenPublishConfigVO.getMavenSettings();
        }

        DevopsCiMavenSettingsDTO devopsCiMavenSettingsRecordDTO = new DevopsCiMavenSettingsDTO(jobId, sequence);
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = devopsCiMavenSettingsMapper.selectOne(devopsCiMavenSettingsRecordDTO);

        if (devopsCiMavenSettingsDTO == null) {
            return MapperUtil.resultJudgedInsert(devopsCiMavenSettingsMapper, new DevopsCiMavenSettingsDTO(jobId, sequence, settings), ERROR_CI_MAVEN_SETTINGS_INSERT);
        } else {
            devopsCiMavenSettingsDTO.setMavenSettings(settings);
            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiMavenSettingsMapper, devopsCiMavenSettingsDTO, ERROR_CI_MAVEN_SETTINGS_INSERT);
        }

        return devopsCiMavenSettingsDTO;
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
     * @param projectId                项目id
     * @param devopsCiMavenSettingsDTO settings配置
     * @param ciConfigTemplateVO       maven发布软件包阶段的信息
     * @param devopsCiStepDTO
     * @param targetMavenRepoVO        目标制品库仓库信息
     * @return 生成的shell脚本
     */
    private List<String> buildMavenJarDeployScripts(final Long projectId,
                                                    final DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO,
                                                    DevopsCiMavenPublishConfigVO ciConfigTemplateVO,
                                                    DevopsCiStepDTO devopsCiStepDTO,
                                                    List<MavenRepoVO> targetMavenRepoVO) {
        List<String> shells = new ArrayList<>();
        // 声明变量
        if (!CollectionUtils.isEmpty(targetMavenRepoVO)) {
            shells.add(String.format(EXPORT_VAR_TPL, GitOpsConstants.CHOERODON_MAVEN_REPO_ID, targetMavenRepoVO.get(0).getName()));
            shells.add(String.format(EXPORT_VAR_TPL, GitOpsConstants.CHOERODON_MAVEN_REPO_URL, targetMavenRepoVO.get(0).getUrl()));
        }

        if (MavenGavSourceTypeEnum.POM.value().equals(ciConfigTemplateVO.getGavSourceType())) {
            shells.add(String.format(EXPORT_VAR_TPL, "CHOERODON_MAVEN_POM_LOCATION", ciConfigTemplateVO.getPomLocation()));
        } else if (MavenGavSourceTypeEnum.CUSTOM.value().equals(ciConfigTemplateVO.getGavSourceType())) {
            shells.add(String.format(EXPORT_VAR_TPL, "CHOERODON_MAVEN_GROUP_ID", ciConfigTemplateVO.getGroupId()));
            shells.add(String.format(EXPORT_VAR_TPL, "CHOERODON_MAVEN_ARTIFACT_ID", ciConfigTemplateVO.getArtifactId()));
            shells.add(String.format(EXPORT_VAR_TPL, "CHOERODON_MAVEN_VERSION", ciConfigTemplateVO.getVersion()));
            shells.add(String.format(EXPORT_VAR_TPL, "CHOERODON_MAVEN_PACKAGING", ciConfigTemplateVO.getPackaging()));
        }

        // 这里这么写是为了考虑之后可能选了多个仓库, 如果是多个仓库的话, 变量替换不便
        // TODO 重构逻辑
//        List<String> templateShells = GitlabCiUtil
//                .filterLines(GitlabCiUtil.splitLinesForShell(devopsCiStepDTO.getScript()),
//                        true,
//                        true);
        // 如果有settings配置, 填入获取settings的指令
        if (devopsCiMavenSettingsDTO != null) {
            shells.add(GitlabCiUtil.downloadMavenSettings(projectId, devopsCiMavenSettingsDTO.getId()));
        }
        shells.addAll(GitlabCiUtil
                .filterLines(GitlabCiUtil.splitLinesForShell(devopsCiStepDTO.getScript()),
                        true,
                        true));
        // 保存jar包信息到猪齿鱼
        String cmd;
        if (ciConfigTemplateVO.getNexusRepoId() != null) {
            cmd = GitlabCiUtil.saveJarMetadata(ciConfigTemplateVO.getNexusRepoId(),
                    devopsCiMavenSettingsDTO.getId(),
                    devopsCiStepDTO.getSequence());
        } else {
            MavenRepoVO targetRepo = ciConfigTemplateVO.getTargetRepo();
            cmd = GitlabCiUtil.saveCustomJarMetadata(devopsCiMavenSettingsDTO.getId(),
                    devopsCiStepDTO.getSequence(),
                    targetRepo.getUrl(),
                    targetRepo.getUsername(),
                    targetRepo.getPassword());
        }
        shells.add(cmd);
//        // 根据目标仓库信息, 渲染发布jar包的指令
//        if (!CollectionUtils.isEmpty(targetMavenRepoVO)) {
//            // 插入shell指令将配置的settings文件下载到项目目录下
//
//            // 包含repoId锚点的字符串在templateShells中的索引号
//            int repoIdIndex = -1;
//            // 包含repoUrl锚点的字符串在templateShells中的索引号
//            int repoUrlIndex = -1;
//            // 寻找包含这两个锚点的字符串位置
//            for (int i = 0; i < templateShells.size(); i++) {
//                if (repoIdIndex == -1 && templateShells.get(i).contains(GitOpsConstants.CHOERODON_MAVEN_REPO_ID)) {
//                    repoIdIndex = i;
//                }
//                if (repoUrlIndex == -1 && templateShells.get(i).contains(GitOpsConstants.CHOERODON_MAVEN_REPO_URL)) {
//                    repoUrlIndex = i;
//                }
//                if (repoIdIndex != -1 && repoUrlIndex != -1) {
//                    // 没必要再找了
//                    break;
//                }
//            }
//
//            // 为每一个仓库都从模板的脚本中加一份生成的命令
//            for (MavenRepoVO repo : targetMavenRepoVO) {
//                // 将预定的变量(仓库名和地址)替换为settings.xml文件指定的
//                List<String> commands = new ArrayList<>(templateShells);
//                if (repoIdIndex != -1) {
//                    commands.set(repoIdIndex, commands.get(repoIdIndex).replace(GitOpsConstants.CHOERODON_MAVEN_REPO_ID, repo.getName()));
//                }
//                if (repoUrlIndex != -1) {
//                    commands.set(repoUrlIndex, commands.get(repoUrlIndex).replace(GitOpsConstants.CHOERODON_MAVEN_REPO_URL, repo.getUrl()));
//                }
//                shells.addAll(commands);
//            }
//
//            // 只生成一个jar包元数据上传指令用于CD阶段
//            //加上jobId  与sequence，用于查询jar包的时间戳
//            String cmd;
//            if (ciConfigTemplateVO.getNexusRepoId() != null) {
//
//                cmd = GitlabCiUtil.saveJarMetadata(ciConfigTemplateVO.getNexusRepoId(),
//                        devopsCiMavenSettingsDTO.getId(),
//                        devopsCiStepDTO.getSequence());
//            } else {
//                MavenRepoVO targetRepo = ciConfigTemplateVO.getTargetRepo();
//                cmd = GitlabCiUtil.saveCustomJarMetadata(devopsCiMavenSettingsDTO.getId(),
//                        devopsCiStepDTO.getSequence(),
//                        targetRepo.getUrl(),
//                        targetRepo.getUsername(),
//                        targetRepo.getPassword());
//            }
//
//            shells.add(cmd);
//        } else {
//            // 如果没有目标仓库信息, 则认为用户是自己填入好了maven发布jar的指令, 不需要渲染
//            shells.addAll(templateShells);
//        }
        return shells;
    }

    @Nullable
    private DevopsCiMavenPublishConfigVO dtoToVo(DevopsCiMavenPublishConfigDTO devopsCiMavenPublishConfigDTO) {
        DevopsCiMavenPublishConfigVO devopsCiMavenPublishConfigVO = ConvertUtils.convertObject(devopsCiMavenPublishConfigDTO, DevopsCiMavenPublishConfigVO.class);
        if (StringUtils.hasText(devopsCiMavenPublishConfigDTO.getNexusMavenRepoIdStr())) {
            devopsCiMavenPublishConfigVO.setNexusMavenRepoIds(JsonHelper.unmarshalByJackson(devopsCiMavenPublishConfigDTO.getNexusMavenRepoIdStr(), new TypeReference<Set<Long>>() {
            }));
        }
        if (StringUtils.hasText(devopsCiMavenPublishConfigDTO.getRepoStr())) {
            devopsCiMavenPublishConfigVO.setRepos(JsonHelper.unmarshalByJackson(devopsCiMavenPublishConfigDTO.getRepoStr(), new TypeReference<List<MavenRepoVO>>() {
            }));
        }
        if (StringUtils.hasText(devopsCiMavenPublishConfigDTO.getTargetRepoStr())) {
            devopsCiMavenPublishConfigVO.setTargetRepo(JsonHelper.unmarshalByJackson(devopsCiMavenPublishConfigDTO.getTargetRepoStr(), MavenRepoVO.class));
        }
        return devopsCiMavenPublishConfigVO;
    }


    @Nullable
    private DevopsCiMavenPublishConfigDTO voToDto(DevopsCiMavenPublishConfigVO mavenPublishConfig) {
        DevopsCiMavenPublishConfigDTO devopsCiMavenPublishConfigDTO = ConvertUtils.convertObject(mavenPublishConfig, DevopsCiMavenPublishConfigDTO.class);
        if (!CollectionUtils.isEmpty(mavenPublishConfig.getNexusMavenRepoIds())) {
            devopsCiMavenPublishConfigDTO.setNexusMavenRepoIdStr(JsonHelper.marshalByJackson(mavenPublishConfig.getNexusMavenRepoIds()));
        }
        if (!CollectionUtils.isEmpty(mavenPublishConfig.getRepos())) {
            devopsCiMavenPublishConfigDTO.setRepoStr(JsonHelper.marshalByJackson(mavenPublishConfig.getRepos()));
        }
        if (mavenPublishConfig.getTargetRepo() != null) {
            devopsCiMavenPublishConfigDTO.setTargetRepoStr(JsonHelper.marshalByJackson(mavenPublishConfig.getTargetRepo()));
        }
        return devopsCiMavenPublishConfigDTO;
    }

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.MAVEN_PUBLISH;
    }

    @Override
    protected Boolean isConfigComplete(DevopsCiStepVO ciTemplateStepVO) {
        DevopsCiMavenPublishConfigVO mavenPublishConfig = ciTemplateStepVO.getMavenPublishConfig();

        if (mavenPublishConfig == null) {
            return false;
        }
        if (mavenPublishConfig.getNexusRepoId() == null && mavenPublishConfig.getTargetRepo() == null) {
            return false;
        }
        return true;
    }
}
