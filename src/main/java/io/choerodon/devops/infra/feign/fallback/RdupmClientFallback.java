package io.choerodon.devops.infra.feign.fallback;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.NexusServerConfig;
import io.choerodon.devops.api.vo.harbor.HarborCustomRepo;
import io.choerodon.devops.api.vo.harbor.HarborImageTagVo;
import io.choerodon.devops.api.vo.hrds.HarborC7nRepoImageTagVo;
import io.choerodon.devops.api.vo.hrds.HarborC7nRepoVo;
import io.choerodon.devops.infra.dto.harbor.HarborAllRepoDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusRepoDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusServerDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.feign.RdupmClient;

/**
 * User: Mr.Wang
 * Date: 2020/6/11
 */
@Component
public class RdupmClientFallback implements RdupmClient {
    @Override
    public ResponseEntity<List<HarborCustomRepo>> listAllCustomRepoByProject(Long projectId) {
        throw new CommonException("error.query.repo.list");
    }

    @Override
    public ResponseEntity<HarborCustomRepo> listRelatedCustomRepoByService(Long projectId, Long appServiceId) {
        throw new CommonException("error.query.repo.list.by.appServiceId");
    }

    @Override
    public ResponseEntity saveRelationByService(Long projectId, Long appServiceId, Long customRepoId) {
        throw new CommonException("error.save.repo.rel");
    }

    @Override
    public ResponseEntity deleteRelationByService(Long projectId, Long appServiceId, Long customRepoId) {
        throw new CommonException("error.delete.repo");
    }

    @Override
    public ResponseEntity<HarborRepoDTO> queryHarborRepoConfig(Long projectId, Long appServiceId) {
        throw new CommonException("error.query.harbor.config");
    }

    @Override
    public ResponseEntity<List<NexusMavenRepoDTO>> getRepoByProject(Long organizationId, Long projectId, String repoType, String type) {
        throw new CommonException("error.query.nexus.repo.list", projectId, repoType, type);
    }

    @Override
    public ResponseEntity<List<NexusMavenRepoDTO>> getRepoUserByProject(Long organizationId, Long projectId, Set<Long> repositoryIds) {
        throw new CommonException("error.query.nexus.repo.user.list", projectId, repositoryIds);
    }

    @Override
    public ResponseEntity<HarborRepoDTO> queryHarborRepoConfigById(Long projectId, Long repoId, String repoType) {
        throw new CommonException("error.query.config.by.configId");
    }

    @Override
    public ResponseEntity<HarborAllRepoDTO> queryAllHarborRepoConfig(Long projectId) {
        throw new CommonException("error.query.all.config");
    }

    @Override
    public ResponseEntity<List<C7nNexusServerDTO>> getNexusServerByProject(Long organizationId, Long projectId) {
        throw new CommonException("error.query.nexus.service.by.project");
    }

    @Override
    public ResponseEntity<List<C7nNexusComponentDTO>> listMavenComponents(Long organizationId, Long projectId, Long repositoryId, String groupId, String artifactId, String versionRegular) {
        throw new CommonException("error.query.maven.components");
    }

    @Override
    public ResponseEntity<List<C7nNexusRepoDTO>> getMavenRepoByConfig(Long organizationId, Long projectId, Long configId, String type) {
        throw new CommonException("error.query.nexus.repo.components");
    }

    @Override
    public ResponseEntity<List<HarborC7nRepoVo>> listImageRepo(Long projectId) {
        throw new CommonException("error.query.nexus.repo.list");
    }

    @Override
    public ResponseEntity<HarborC7nRepoImageTagVo> listImageTag(String repoType, Long repoId, String imageName, String tagName) {
        throw new CommonException("error.query.nexus.repo.list.tag");
    }

    @Override
    public ResponseEntity deleteImageTag(String repoName, String tagName) {
        throw new CommonException("error.delete.image.tag");
    }

    @Override
    public ResponseEntity<Page<HarborImageTagVo>> pagingImageTag(Long projectId, String repoName, String tagName) {
        throw new CommonException("error.paging.image.tag");
    }

    @Override
    public ResponseEntity<C7nNexusRepoDTO> getMavenRepo(Long organizationId, Long projectId, Long repositoryId) {
        throw new CommonException("error.query.repo.by.id");
    }

    @Override
    public ResponseEntity deleteAllRelationByService(Long projectId, Long appServiceId) {
        throw new CommonException("error.delete.all.repo.relation");
    }

    @Override
    public ResponseEntity<NexusServerConfig> getDefaultMavenRepo(Long organizationId) {
        throw new CommonException("error.query.nexus.service.config");
    }

    @Override
    public ResponseEntity<List<HarborRepoDTO>> queryHarborReposByIds(Set<Long> harborConfigIds) {
        throw new CommonException("error.query.harbor.config.by.ids");
    }

    @Override
    public ResponseEntity<HarborCustomRepo> queryCustomRepoById(Long projectId, Long repoId) {
        throw new CommonException("error.query.harbor.custom.repo");
    }
}
