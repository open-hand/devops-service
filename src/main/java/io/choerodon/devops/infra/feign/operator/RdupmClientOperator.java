package io.choerodon.devops.infra.feign.operator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

import org.hzero.core.util.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.harbor.HarborCustomRepo;
import io.choerodon.devops.api.vo.hrds.HarborC7nRepoImageTagVo;
import io.choerodon.devops.api.vo.hrds.HarborC7nRepoVo;
import io.choerodon.devops.api.vo.rdupm.NexusRepositoryVO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusRepoDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.feign.RdupmClient;

/**
 * @author zmf
 * @since 2020/6/12
 */
@Component
public class RdupmClientOperator {
    @Autowired
    private RdupmClient rdupmClient;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    /**
     * CI-流水线-获取项目下仓库列表-包含用户信息
     *
     * @param organizationId 组织id
     * @param projectId      项目id
     * @param repositoryIds  仓库id集合
     * @return 仓库信息
     */
    public List<NexusMavenRepoDTO> getRepoUserByProject(@Nullable Long organizationId, Long projectId, Set<Long> repositoryIds) {
        if (CollectionUtils.isEmpty(repositoryIds)) {
            return Collections.emptyList();
        }
        if (organizationId == null) {
            organizationId = baseServiceClientOperator.queryIamProjectBasicInfoById(Objects.requireNonNull(projectId))
                    .getOrganizationId();
        }
        ResponseEntity<List<NexusMavenRepoDTO>> response = rdupmClient.getRepoUserByProject(
                Objects.requireNonNull(organizationId), projectId, repositoryIds);
        if (response == null || response.getBody() == null) {
            throw new CommonException("devops.query.nexus.repo.user.list", projectId, repositoryIds);
        }
        return response.getBody();
    }

    public NexusRepositoryVO queryRepoWithDefaultUserInfo(Long projectId, Long repoId) {

        ResponseEntity<String> response = rdupmClient.queryRepoWithDefaultUserInfo(projectId, repoId);

        return ResponseUtils.getResponse(response, NexusRepositoryVO.class);
    }

    /**
     * 根据项目id查询镜像仓库列表
     *
     * @param projectId
     * @return
     */
    public List<HarborC7nRepoVo> listImageRepo(@Nullable Long projectId) {

        ResponseEntity<List<HarborC7nRepoVo>> response = rdupmClient.listImageRepo(Objects.requireNonNull(projectId));
        if (response == null || response.getBody() == null) {
            throw new CommonException("devops.query.nexus.repo.list", projectId);
        }
        return response.getBody();
    }


    /**
     * 根据仓库类型和仓库id 镜像名
     * 查询所有镜像
     *
     * @param repoType
     * @param repoId
     * @param imageName
     * @return
     */
    public HarborC7nRepoImageTagVo listImageTag(String repoType, @Nullable Long repoId, String imageName, @Nullable String tagName) {
        ResponseEntity<HarborC7nRepoImageTagVo> response = rdupmClient.listImageTag(repoType, repoId, imageName, tagName);
        if (response == null || response.getBody() == null) {
            throw new CommonException("devops.query.nexus.repo.list.tag");
        }
        return response.getBody();
    }

    public HarborCustomRepo queryCustomRepoById(Long projectId, Long repoId) {
        ResponseEntity<HarborCustomRepo> response = rdupmClient.queryCustomRepoById(projectId, repoId);
        if (response == null || response.getBody() == null) {
            throw new CommonException("devops.query.nexus.repo.list.tag");
        }
        return response.getBody();
    }

    /**
     * mvn 仓库下的包列表
     *
     * @param organizationId
     * @param projectId
     * @param repositoryId
     * @param groupId
     * @param artifactId
     * @param versionRegular
     * @return
     */
    public List<C7nNexusComponentDTO> listMavenComponents(@Nullable Long organizationId,
                                                          @Nullable Long projectId,
                                                          @Nullable Long repositoryId,
                                                          String groupId,
                                                          String artifactId,
                                                          String versionRegular) {
        ResponseEntity<List<C7nNexusComponentDTO>> response = rdupmClient.listMavenComponents(organizationId, projectId, repositoryId, groupId, artifactId, versionRegular);
        if (response == null || response.getBody() == null) {
            throw new CommonException("devops.query.nexus.maven.list");
        }
        return response.getBody();
    }

    public HarborRepoDTO queryHarborRepoConfigById(Long projectId, Long harborConfigId, String repoType) {
        ResponseEntity<HarborRepoDTO> response = rdupmClient.queryHarborRepoConfigById(projectId, harborConfigId, repoType);
        if (response == null || response.getBody() == null || response.getBody().getHarborRepoConfig() == null) {
            throw new CommonException("devops.query.harbor.repo");
        }
        return response.getBody();
    }

    public C7nNexusRepoDTO getMavenRepo(Long organizationId, Long projectId, Long repositoryId) {
        return rdupmClient.getMavenRepo(organizationId, projectId, repositoryId).getBody();
    }


}
