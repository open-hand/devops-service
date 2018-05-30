package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.ApplicationReleasingDTO;
import io.choerodon.devops.api.dto.ApplicationVersionRepDTO;
import io.choerodon.devops.app.service.ApplicationMarketService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.ApplicationMarketE;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.factory.ApplicationMarketFactory;
import io.choerodon.devops.domain.application.repository.ApplicationMarketRepository;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.ApplicationVersionRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by ernst on 2018/5/12.
 */
@Service
public class ApplicationMarketServiceImpl implements ApplicationMarketService {
    private static final String ORGANIZATION = "organization";
    private static final String PUBLIC = "public";

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    private ApplicationVersionRepository applicationVersionRepository;
    private ApplicationMarketRepository applicationMarketRepository;
    private IamRepository iamRepository;
    private ApplicationRepository applicationRepository;
    private GitlabServiceClient gitlabServiceClient;

    /**
     * 构造函数
     */
    @Autowired
    public ApplicationMarketServiceImpl(ApplicationVersionRepository applicationVersionRepository,
                                        ApplicationMarketRepository applicationMarketRepository,
                                        IamRepository iamRepository,
                                        ApplicationRepository applicationRepository,
                                        GitlabServiceClient gitlabServiceClient) {
        this.applicationVersionRepository = applicationVersionRepository;
        this.applicationMarketRepository = applicationMarketRepository;
        this.iamRepository = iamRepository;
        this.gitlabServiceClient = gitlabServiceClient;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public Long release(Long projectId, ApplicationReleasingDTO applicationReleasingDTO) {
        List<Long> ids = new ArrayList<>();
        if (applicationReleasingDTO != null) {
            String publishLevel = applicationReleasingDTO.getPublishLevel();
            if (!ORGANIZATION.equals(publishLevel) && !PUBLIC.equals(publishLevel)) {
                throw new CommonException("error.publishLevel");
            }
            List<ApplicationVersionRepDTO> appVersions = applicationReleasingDTO.getAppVersions();
            if (appVersions != null && !appVersions.isEmpty()) {
                for (ApplicationVersionRepDTO appVersion : appVersions) {
                    ids.add(appVersion.getId());
                }
            }
        } else {
            throw new CommonException("error.app.check");
        }
        applicationMarketRepository.checkCanPub(applicationReleasingDTO.getAppId());
        //校验应用和版本
        applicationVersionRepository.checkAppAndVersion(applicationReleasingDTO.getAppId(), ids);
        applicationVersionRepository.updatePublishLevelByIds(ids, 1L);

        ApplicationMarketE applicationMarketE = ApplicationMarketFactory.create();
        applicationMarketE.initApplicationEById(applicationReleasingDTO.getAppId());
        applicationMarketE.setPublishLevel(applicationReleasingDTO.getPublishLevel());
        applicationMarketE.setActive(true);
        applicationMarketE.setContributor(applicationReleasingDTO.getContributor());
        applicationMarketE.setDescription(applicationReleasingDTO.getDescription());
        applicationMarketE.setCategory(applicationReleasingDTO.getCategory());
        applicationMarketE.setImgUrl(applicationReleasingDTO.getImgUrl());
        applicationMarketRepository.create(applicationMarketE);
        return applicationMarketRepository.getMarketIdByAppId(applicationReleasingDTO.getAppId());
    }

    @Override
    public Page<ApplicationReleasingDTO> listMarketAppsByProjectId(Long projectId, PageRequest pageRequest, String searchParam) {
        Page<ApplicationMarketE> applicationMarketEPage = applicationMarketRepository.listMarketAppsByProjectId(
                projectId, pageRequest, searchParam);
        return getReleasingDTOs(applicationMarketEPage);
    }

    @Override
    public Page<ApplicationReleasingDTO> listMarketApps(Long projectId, PageRequest pageRequest, String searchParam) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        if (projectE != null && projectE.getOrganization() != null) {
            Long organizationId = projectE.getOrganization().getId();
            List<ProjectE> projectEList = iamRepository.listIamProjectByOrgId(organizationId);
            List<Long> projectIds = new ArrayList<>();
            if (projectEList != null) {
                for (ProjectE project : projectEList) {
                    projectIds.add(project.getId());
                }
            }
            Page<ApplicationMarketE> applicationMarketEPage = applicationMarketRepository.listMarketApps(
                    projectIds, pageRequest, searchParam);
            return getReleasingDTOs(applicationMarketEPage);
        }
        return null;
    }

    @Override
    public ApplicationReleasingDTO getMarketApp(Long projectId, Long appMarketId, Long versionId) {
        ApplicationMarketE applicationMarketE = applicationMarketRepository.getMarket(projectId, appMarketId);
        ApplicationE applicationE = applicationMarketE.getApplicationE();
        Long applicationId = applicationE.getId();
        List<ApplicationVersionE> applicationVersionEList = applicationVersionRepository
                .listAllPublishedVersion(applicationId);
        List<ApplicationVersionRepDTO> applicationVersionDTOList = ConvertHelper
                .convertList(applicationVersionEList, ApplicationVersionRepDTO.class);
        ApplicationReleasingDTO applicationReleasingDTO =
                ConvertHelper.convert(applicationMarketE, ApplicationReleasingDTO.class);
        applicationReleasingDTO.setAppVersions(applicationVersionDTOList);
        applicationE = applicationRepository.query(applicationId);

        String latestVersionCommit;
        if (versionId == null) {
            Optional<ApplicationVersionRepDTO> optional = applicationVersionDTOList.parallelStream()
                    .max((t, k) -> {
                        if (t.getId() > k.getId()) {
                            return 1;
                        } else {
                            return t.getId().equals(k.getId()) ? 0 : -1;
                        }
                    });
            latestVersionCommit = optional.isPresent()
                    ? applicationVersionRepository.query(optional.get().getId()).getCommit()
                    : null;
        } else {
            Long versionExist = applicationVersionDTOList.parallelStream()
                    .filter(t -> t.getId().equals(versionId)).count();
            latestVersionCommit = versionExist > 0 ? applicationVersionRepository.query(versionId).getCommit() : null;
        }
        String readme = getReadme(applicationE.getGitlabProjectE().getId(), latestVersionCommit);
        applicationReleasingDTO.setReadme(readme);
        return applicationReleasingDTO;
    }

    private String getReadme(Integer gitlabProjectId, String commit) {
        String readme = gitlabServiceClient.getReadme(gitlabProjectId, commit).getBody();
        return !"{\"failed\":true,\"message\":\"error.file.get\"}".equals(readme) ? readme : "# 暂无";
    }

    @Override
    public void unpublish(Long projectId, Long appMarketId) {
        applicationMarketRepository.checkProject(projectId, appMarketId);
        applicationMarketRepository.checkDeployed(projectId, appMarketId, null);
        applicationMarketRepository.unpublishApplication(appMarketId);
    }

    @Override
    public void unpublish(Long projectId, Long appMarketId, Long versionId) {
        applicationMarketRepository.checkProject(projectId, appMarketId);
        applicationMarketRepository.checkDeployed(projectId, appMarketId, versionId);
        applicationMarketRepository.unpublishVersion(appMarketId, versionId);

    }

    @Override
    public void update(Long projectId, Long appMarketId, ApplicationReleasingDTO applicationRelease) {
        if (applicationRelease != null) {
            String publishLevel = applicationRelease.getPublishLevel();
            if (!ORGANIZATION.equals(publishLevel) && !PUBLIC.equals(publishLevel)) {
                throw new CommonException("error.publishLevel");
            }
        } else {
            throw new CommonException("error.app.check");
        }
        if (!appMarketId.equals(applicationRelease.getId())) {
            throw new CommonException("error.id.notMatch");
        }
        applicationMarketRepository.checkProject(projectId, appMarketId);
        ApplicationReleasingDTO applicationReleasingDTO = getMarketApp(projectId, appMarketId, null);
        if (!applicationReleasingDTO.getAppId().equals(applicationRelease.getAppId())) {
            throw new CommonException("error.app.cannot.change");
        }
        if (ORGANIZATION.equals(applicationRelease.getPublishLevel())
                && PUBLIC.equals(applicationReleasingDTO.getPublishLevel())) {
            applicationMarketRepository.checkDeployed(projectId, appMarketId, null);

        }
        DevopsAppMarketDO devopsAppMarketDO = ConvertHelper.convert(applicationRelease, DevopsAppMarketDO.class);
        if (!ConvertHelper.convert(applicationReleasingDTO, DevopsAppMarketDO.class).equals(devopsAppMarketDO)) {
            applicationMarketRepository.update(devopsAppMarketDO);
        }
        List<Long> ids = applicationRelease.getAppVersions().parallelStream()
                .map(ApplicationVersionRepDTO::getId).collect(Collectors.toCollection(ArrayList::new));

        if (!ids.equals(applicationReleasingDTO.getAppVersions().parallelStream()
                .map(ApplicationVersionRepDTO::getId).collect(Collectors.toCollection(ArrayList::new)))) {
            applicationVersionRepository.checkAppAndVersion(applicationRelease.getAppId(), ids);
            applicationMarketRepository.unpublishVersion(appMarketId, null);
            applicationVersionRepository.updatePublishLevelByIds(ids, 1L);
        }
    }

    private Page<ApplicationReleasingDTO> getReleasingDTOs(Page<ApplicationMarketE> applicationMarketEPage) {
        Page<ApplicationReleasingDTO> applicationReleasingDTOPage = ConvertPageHelper.convertPage(
                applicationMarketEPage,
                ApplicationReleasingDTO.class);
        List<ApplicationReleasingDTO> applicationReleasingDTOList = applicationReleasingDTOPage.getContent();
        for (ApplicationReleasingDTO applicationReleasingDTO : applicationReleasingDTOList) {
            Long applicationId = applicationReleasingDTO.getAppId();
            List<ApplicationVersionE> applicationVersionEList = applicationVersionRepository
                    .listAllPublishedVersion(applicationId);
            List<ApplicationVersionRepDTO> applicationVersionDTOList = ConvertHelper
                    .convertList(applicationVersionEList, ApplicationVersionRepDTO.class);
            applicationReleasingDTO.setAppVersions(applicationVersionDTOList);
        }
        return applicationReleasingDTOPage;
    }
}
