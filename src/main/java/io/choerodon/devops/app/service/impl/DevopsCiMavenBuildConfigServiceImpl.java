package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_STEP_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.PipelineConstants.DEVOPS_CI_MAVEN_REPOSITORY_TYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsCiMavenBuildConfigVO;
import io.choerodon.devops.api.vo.MavenRepoVO;
import io.choerodon.devops.app.service.DevopsCiMavenBuildConfigService;
import io.choerodon.devops.infra.config.ProxyProperties;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.DevopsCiMavenBuildConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiMavenSettingsDTO;
import io.choerodon.devops.infra.dto.maven.Proxy;
import io.choerodon.devops.infra.dto.maven.Repository;
import io.choerodon.devops.infra.dto.maven.RepositoryPolicy;
import io.choerodon.devops.infra.dto.maven.Server;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCiMavenBuildConfigMapper;
import io.choerodon.devops.infra.mapper.DevopsCiMavenSettingsMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.MavenSettingsUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 15:03
 */
@Service
public class DevopsCiMavenBuildConfigServiceImpl implements DevopsCiMavenBuildConfigService {

    public static final String DEVOPS_MAVEN_SETTINGS_INSERT = "devops.maven.settings.insert";
    private static final String DEVOPS_SAVE_MAVEN_BUILD_CONFIG_FAILED = "devops.save.maven.build.config.failed";

    @Autowired
    private DevopsCiMavenBuildConfigMapper devopsCiMavenBuildConfigMapper;
    @Autowired
    private RdupmClientOperator rdupmClientOperator;

    @Autowired
    private ProxyProperties proxyProperties;

    @Autowired
    private DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper;


    @Override
    public DevopsCiMavenBuildConfigDTO baseQuery(Long id) {
        return devopsCiMavenBuildConfigMapper.selectByPrimaryKey(id);
    }

    @Override
    public DevopsCiMavenBuildConfigVO queryUnmarshalByStepId(Long stepId) {
        DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = queryByStepId(stepId);
        if (devopsCiMavenBuildConfigDTO == null) {
            return null;
        }
        DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO = ConvertUtils.convertObject(devopsCiMavenBuildConfigDTO, DevopsCiMavenBuildConfigVO.class);
        if (StringUtils.isNoneBlank(devopsCiMavenBuildConfigVO.getNexusMavenRepoIdStr())) {
            devopsCiMavenBuildConfigVO
                    .setNexusMavenRepoIds(JsonHelper
                            .unmarshalByJackson(devopsCiMavenBuildConfigVO.getNexusMavenRepoIdStr(), new TypeReference<Set<Long>>() {
                            }));
        }

        if (StringUtils.isNoneBlank(devopsCiMavenBuildConfigVO.getRepoStr())) {
            devopsCiMavenBuildConfigVO
                    .setRepos(JsonHelper
                            .unmarshalByJackson(devopsCiMavenBuildConfigVO.getRepoStr(), new TypeReference<List<MavenRepoVO>>() {
                            }));
        }

        return devopsCiMavenBuildConfigVO;
    }

    @Override
    public DevopsCiMavenBuildConfigDTO queryByStepId(Long stepId) {
        Assert.notNull(stepId, DEVOPS_STEP_ID_IS_NULL);

        DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = new DevopsCiMavenBuildConfigDTO();
        devopsCiMavenBuildConfigDTO.setStepId(stepId);

        return devopsCiMavenBuildConfigMapper.selectOne(devopsCiMavenBuildConfigDTO);
    }

    @Override
    @Transactional
    public void baseCreate(DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiMavenBuildConfigMapper,
                devopsCiMavenBuildConfigDTO,
                DEVOPS_SAVE_MAVEN_BUILD_CONFIG_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(Long stepId, DevopsCiMavenBuildConfigVO mavenBuildConfig) {
        DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = voToDto(mavenBuildConfig);
        devopsCiMavenBuildConfigDTO.setId(null);
        devopsCiMavenBuildConfigDTO.setStepId(stepId);
        baseCreate(devopsCiMavenBuildConfigDTO);
    }

    @Override
    public void batchDeleteByStepIds(Set<Long> stepIds) {
        devopsCiMavenBuildConfigMapper.batchDeleteByStepIds(stepIds);
    }

    private static String buildSettings(List<MavenRepoVO> mavenRepoList, List<Proxy> proxies) {
        List<Server> servers = new ArrayList<>();
        List<Repository> repositories = new ArrayList<>();

        mavenRepoList.forEach(m -> {
            if (m.getType() != null) {
                String[] types = m.getType().split(GitOpsConstants.COMMA);
                if (types.length > 2) {
                    throw new CommonException(DEVOPS_CI_MAVEN_REPOSITORY_TYPE, m.getType());
                }
            }
            if (Boolean.TRUE.equals(m.getPrivateRepo())) {
                servers.add(new Server(Objects.requireNonNull(m.getName()), Objects.requireNonNull(m.getUsername()), Objects.requireNonNull(m.getPassword())));
            }
            repositories.add(new Repository(
                    Objects.requireNonNull(m.getName()),
                    Objects.requireNonNull(m.getName()),
                    Objects.requireNonNull(m.getUrl()),
                    m.getType() == null ? null : new RepositoryPolicy(m.getType().contains(GitOpsConstants.RELEASE)),
                    m.getType() == null ? null : new RepositoryPolicy(m.getType().contains(GitOpsConstants.SNAPSHOT))));
        });
        return MavenSettingsUtil.generateMavenSettings(servers, repositories, proxies);
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

    @Override
    public DevopsCiMavenBuildConfigDTO voToDto(DevopsCiMavenBuildConfigVO mavenBuildConfigVO) {
        DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = ConvertUtils.convertObject(mavenBuildConfigVO, DevopsCiMavenBuildConfigDTO.class);
        if (!CollectionUtils.isEmpty(mavenBuildConfigVO.getNexusMavenRepoIds())) {
            devopsCiMavenBuildConfigDTO.setNexusMavenRepoIdStr(JsonHelper.marshalByJackson(mavenBuildConfigVO.getNexusMavenRepoIds()));
        }
        if (!CollectionUtils.isEmpty(mavenBuildConfigVO.getRepos())) {
            devopsCiMavenBuildConfigDTO.setRepoStr(JsonHelper.marshalByJackson(mavenBuildConfigVO.getRepos()));
        }
        return devopsCiMavenBuildConfigDTO;
    }

    @Override
    public DevopsCiMavenBuildConfigVO dtoToVo(DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO) {
        DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO = ConvertUtils.convertObject(devopsCiMavenBuildConfigDTO, DevopsCiMavenBuildConfigVO.class);
        if (org.springframework.util.StringUtils.hasText(devopsCiMavenBuildConfigDTO.getNexusMavenRepoIdStr())) {
            devopsCiMavenBuildConfigVO.setNexusMavenRepoIds(JsonHelper.unmarshalByJackson(devopsCiMavenBuildConfigDTO.getNexusMavenRepoIdStr(), new TypeReference<Set<Long>>() {
            }));
        }
        if (org.springframework.util.StringUtils.hasText(devopsCiMavenBuildConfigDTO.getRepoStr())) {
            devopsCiMavenBuildConfigVO.setRepos(JsonHelper.unmarshalByJackson(devopsCiMavenBuildConfigDTO.getRepoStr(), new TypeReference<List<MavenRepoVO>>() {
            }));
        }
        return devopsCiMavenBuildConfigVO;
    }

    @Override
    public DevopsCiMavenSettingsDTO buildAndSaveMavenSettings(Long projectId, Long jobId, Long sequence, DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO) {
        // settings文件内容
        String settings;
        final List<MavenRepoVO> repos = new ArrayList<>();

        List<Proxy> proxies = new ArrayList<>();
        if (proxyProperties != null && Boolean.TRUE.equals(proxyProperties.getActive())) {
            proxies.add(ConvertUtils.convertObject(proxyProperties, Proxy.class));
        }

        // 是否有手动填写仓库表单
        final boolean hasManualRepos = !CollectionUtils.isEmpty(devopsCiMavenBuildConfigVO.getRepos());
        // 是否有选择已有的maven仓库
        final boolean hasNexusRepos = !CollectionUtils.isEmpty(devopsCiMavenBuildConfigVO.getNexusMavenRepoIds());

        if (!StringUtils.isEmpty(devopsCiMavenBuildConfigVO.getMavenSettings())) {
            // 使用用户提供的xml内容，不进行内容的校验
            settings = devopsCiMavenBuildConfigVO.getMavenSettings();
        } else if (hasManualRepos || hasNexusRepos) {
            if (hasNexusRepos) {
                // 用户选择的已有的maven仓库
                List<NexusMavenRepoDTO> nexusMavenRepoDTOs = rdupmClientOperator.getRepoUserByProject(null, projectId, devopsCiMavenBuildConfigVO.getNexusMavenRepoIds());
                repos.addAll(nexusMavenRepoDTOs.stream().map(DevopsCiMavenBuildConfigServiceImpl::convertRepo).collect(Collectors.toList()));
            }

            if (hasManualRepos) {
                // 由用户填写的表单构建xml文件内容
                repos.addAll(devopsCiMavenBuildConfigVO.getRepos());
            }

            // 构建settings文件
            settings = buildSettings(repos, proxies);
        } else {
            // 没有填关于settings的信息
            return null;
        }

        // 这里存储的ci setting文件内容是解密后的
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsRecordDTO = new DevopsCiMavenSettingsDTO(jobId, sequence);
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = devopsCiMavenSettingsMapper.selectOne(devopsCiMavenSettingsRecordDTO);
        if (devopsCiMavenSettingsDTO == null) {
            return MapperUtil.resultJudgedInsert(devopsCiMavenSettingsMapper, new DevopsCiMavenSettingsDTO(jobId, sequence, settings), DEVOPS_MAVEN_SETTINGS_INSERT);
        } else {
            devopsCiMavenSettingsDTO.setMavenSettings(settings);
            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiMavenSettingsMapper, devopsCiMavenSettingsDTO, DEVOPS_MAVEN_SETTINGS_INSERT);
        }
        return devopsCiMavenSettingsDTO;
    }
}
