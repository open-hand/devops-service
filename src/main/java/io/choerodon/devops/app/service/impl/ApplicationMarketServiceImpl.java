package io.choerodon.devops.app.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.AppMarketVersionDTO;
import io.choerodon.devops.api.dto.ApplicationReleasingDTO;
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
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketDO;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketVersionDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by ernst on 2018/5/12.
 */
@Service
public class ApplicationMarketServiceImpl implements ApplicationMarketService {
    private static final String ORGANIZATION = "organization";
    private static final String PUBLIC = "public";
    private static final String DESTPATH = "devops";


    private static final Logger logger = LoggerFactory.getLogger(ApplicationMarketServiceImpl.class);

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    private ApplicationVersionRepository applicationVersionRepository;
    private ApplicationMarketRepository applicationMarketRepository;
    private IamRepository iamRepository;
    private ApplicationRepository applicationRepository;
    private GitlabServiceClient gitlabServiceClient;

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
        List<Long> ids;
        if (applicationReleasingDTO != null) {
            String publishLevel = applicationReleasingDTO.getPublishLevel();
            if (!ORGANIZATION.equals(publishLevel) && !PUBLIC.equals(publishLevel)) {
                throw new CommonException("error.publishLevel");
            }
            List<AppMarketVersionDTO> appVersions = applicationReleasingDTO.getAppVersions();
            ids = appVersions.parallelStream().map(AppMarketVersionDTO::getId)
                    .collect(Collectors.toCollection(ArrayList::new));
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
        return getReleasingDTOs(projectId, applicationMarketEPage);
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
            return getReleasingDTOs(projectId, applicationMarketEPage);
        }
        return null;
    }

    @Override
    public ApplicationReleasingDTO getMarketAppInProject(Long projectId, Long appMarketId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Long organizationId = projectE.getOrganization().getId();
        List<ProjectE> projectEList = iamRepository.listIamProjectByOrgId(organizationId);
        List<Long> projectIds = projectEList.parallelStream().map(ProjectE::getId)
                .collect(Collectors.toCollection(ArrayList::new));
        ApplicationMarketE applicationMarketE =
                applicationMarketRepository.getMarket(projectId, appMarketId, projectIds);
        List<DevopsAppMarketVersionDO> versionDOList = applicationMarketRepository
                .getVersions(projectId, appMarketId, true);
        List<AppMarketVersionDTO> appMarketVersionDTOList = ConvertHelper
                .convertList(versionDOList, AppMarketVersionDTO.class);
        ApplicationReleasingDTO applicationReleasingDTO =
                ConvertHelper.convert(applicationMarketE, ApplicationReleasingDTO.class);
        applicationReleasingDTO.setAppVersions(appMarketVersionDTOList);

        return applicationReleasingDTO;
    }

    @Override
    public ApplicationReleasingDTO getMarketApp(Long appMarketId, Long versionId) {
        ApplicationMarketE applicationMarketE =
                applicationMarketRepository.getMarket(null, appMarketId, null);
        ApplicationE applicationE = applicationMarketE.getApplicationE();
        List<DevopsAppMarketVersionDO> versionDOList = applicationMarketRepository
                .getVersions(null, appMarketId, true);
        List<AppMarketVersionDTO> appMarketVersionDTOList = ConvertHelper
                .convertList(versionDOList, AppMarketVersionDTO.class)
                .parallelStream()
                .sorted(this::compareAppMarketVersionDTO)
                .collect(Collectors.toCollection(ArrayList::new));
        ApplicationReleasingDTO applicationReleasingDTO =
                ConvertHelper.convert(applicationMarketE, ApplicationReleasingDTO.class);
        applicationReleasingDTO.setAppVersions(appMarketVersionDTOList);

        Long applicationId = applicationE.getId();
        applicationE = applicationRepository.query(applicationId);
        ProjectE projectE = iamRepository.queryIamProject(applicationE.getProjectE().getId());
        Long organizationId = projectE.getOrganization().getId();
        Organization organization = iamRepository.queryOrganizationById(organizationId);

        Date latestUpdateDate = appMarketVersionDTOList.isEmpty()
                ? getLaterDate(applicationE.getLastUpdateDate(), applicationMarketE.getMarketUpdatedDate())
                : getLatestDate(
                appMarketVersionDTOList.get(0).getUpdatedDate(),
                applicationE.getLastUpdateDate(),
                applicationMarketE.getMarketUpdatedDate());
        applicationReleasingDTO.setLastUpdatedDate(latestUpdateDate);

        String readme = "";
        String latestVersionCommit;
        Boolean versionExist = appMarketVersionDTOList.parallelStream().anyMatch(t -> t.getId().equals(versionId));
        ApplicationVersionE versionE;
        Long latestVersionId = versionId;
        if (!versionExist) {
            Optional<AppMarketVersionDTO> optional = appMarketVersionDTOList.parallelStream()
                    .max(this::compareAppMarketVersionDTO);
            latestVersionId = optional.isPresent()
                    ? optional.get().getId()
                    : versionId;
        }

        versionE = applicationVersionRepository.query(latestVersionId);
        latestVersionCommit = versionE.getCommit();
        String fileSeparator = System.getProperty("file.separator");
        String classPath = String.format("Charts%s%s%s%s%s%s%s%s%s",
                fileSeparator,
                organization.getCode(),
                fileSeparator,
                projectE.getCode(),
                fileSeparator,
                applicationE.getCode(),
                "-",
                versionE.getVersion(),
                ".tgz");
        FileUtil.unTarGZ(classPath, DESTPATH);
        File readmeFile = null;
        try {
            readmeFile = FileUtil.queryFileFromFiles(new File(DESTPATH), "README.md");
        } catch (Exception e) {
            logger.info("file not found");
            readme = "# 暂无。";
        }
        if (readme.isEmpty()) {
            readme = readmeFile == null
                    ? getReadme(applicationE.getGitlabProjectE().getId(), latestVersionCommit)
                    : getFileContent(readmeFile);
        }
        applicationReleasingDTO.setReadme(readme);
        FileUtil.deleteFile(new File(DESTPATH));
        return applicationReleasingDTO;
    }

    private Date getLatestDate(Date a, Date b, Date c) {
        if (a.after(b)) {
            return getLaterDate(a, c);
        } else {
            return getLaterDate(b, c);
        }
    }

    private Date getLaterDate(Date a, Date b) {
        return a.after(b) ? a : b;
    }

    private Integer compareAppMarketVersionDTO(AppMarketVersionDTO s, AppMarketVersionDTO t) {
        if (s.getUpdatedDate().before(t.getUpdatedDate())) {
            return 1;
        } else {
            if (s.getUpdatedDate().after(t.getUpdatedDate())) {
                return -1;
            } else {
                if (s.getCreationDate().before(t.getCreationDate())) {
                    return 1;
                } else {
                    return s.getCreationDate().after(t.getCreationDate()) ? -1 : 0;
                }
            }
        }
    }

    private String getFileContent(File file) {
        StringBuilder content = new StringBuilder();
        try {
            try (FileReader fileReader = new FileReader(file)) {
                try (BufferedReader reader = new BufferedReader(fileReader)) {
                    String lineTxt;
                    while ((lineTxt = reader.readLine()) != null) {
                        content.append(lineTxt).append("\n");
                    }
                }
            }
        } catch (IOException e) {
            throw new CommonException("error.file.read");
        }
        return content.toString();
    }


    private String getReadme(Integer gitlabProjectId, String commit) {
        String readme = gitlabServiceClient.getReadme(gitlabProjectId, commit).getBody();
        return !"{\"failed\":true,\"message\":\"error.file.get\"}".equals(readme)
                && !"error.readme.get".equals(readme)
                ? readme : "# 暂无";
    }

    @Override
    public String getMarketAppVersionReadme(Long appMarketId, Long versionId) {
        return getMarketApp(appMarketId, versionId).getReadme();
    }

    @Override
    public void unpublish(Long projectId, Long appMarketId) {
        applicationMarketRepository.checkProject(projectId, appMarketId);
        applicationMarketRepository.checkDeployed(projectId, appMarketId, null, null);
        applicationMarketRepository.unpublishApplication(appMarketId);
    }

    @Override
    public void unpublish(Long projectId, Long appMarketId, Long versionId) {
        applicationMarketRepository.checkProject(projectId, appMarketId);
        applicationMarketRepository.checkDeployed(projectId, appMarketId, versionId, null);
        applicationMarketRepository.unpublishVersion(appMarketId, versionId);

    }

    @Override
    public void update(Long projectId, Long appMarketId, ApplicationReleasingDTO applicationRelease) {
        if (applicationRelease != null) {
            String publishLevel = applicationRelease.getPublishLevel();
            if (publishLevel != null
                    && !ORGANIZATION.equals(publishLevel)
                    && !PUBLIC.equals(publishLevel)) {
                throw new CommonException("error.publishLevel");
            }
        } else {
            throw new CommonException("error.app.check");
        }
        if (applicationRelease.getId() != null
                && !appMarketId.equals(applicationRelease.getId())) {
            throw new CommonException("error.id.notMatch");
        }
        applicationMarketRepository.checkProject(projectId, appMarketId);
        ApplicationReleasingDTO applicationReleasingDTO = getMarketAppInProject(projectId, appMarketId);
        if (applicationRelease.getAppId() != null
                && !applicationReleasingDTO.getAppId().equals(applicationRelease.getAppId())) {
            throw new CommonException("error.app.cannot.change");
        }
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        if (projectE == null || projectE.getOrganization() == null) {
            throw new CommonException("error.project.query");
        }
        if (applicationRelease.getPublishLevel() != null
                && !applicationRelease.getPublishLevel().equals(applicationReleasingDTO.getPublishLevel())) {
            throw new CommonException("error.publishLevel.cannot.change");
        }
        DevopsAppMarketDO devopsAppMarketDO = ConvertHelper.convert(applicationRelease, DevopsAppMarketDO.class);
        if (!ConvertHelper.convert(applicationReleasingDTO, DevopsAppMarketDO.class).equals(devopsAppMarketDO)) {
            applicationMarketRepository.update(devopsAppMarketDO);
        }
    }

    @Override
    public void update(Long projectId, Long appMarketId, List<AppMarketVersionDTO> versionDTOList) {
        applicationMarketRepository.checkProject(projectId, appMarketId);

        ApplicationReleasingDTO applicationReleasingDTO = getMarketAppInProject(projectId, appMarketId);

        List<Long> ids = versionDTOList.parallelStream()
                .map(AppMarketVersionDTO::getId).collect(Collectors.toCollection(ArrayList::new));

        applicationVersionRepository.checkAppAndVersion(applicationReleasingDTO.getAppId(), ids);
        applicationVersionRepository.updatePublishLevelByIds(ids, 1L);
    }

    @Override
    public List<AppMarketVersionDTO> getAppVersions(Long projectId, Long appMarketId, Boolean isPublish) {
        return ConvertHelper.convertList(applicationMarketRepository.getVersions(projectId, appMarketId, isPublish),
                AppMarketVersionDTO.class);
    }

    @Override
    public Page<AppMarketVersionDTO> getAppVersions(Long projectId, Long appMarketId, Boolean isPublish,
                                                    PageRequest pageRequest, String searchParam) {
        return ConvertPageHelper.convertPage(
                applicationMarketRepository.getVersions(projectId, appMarketId, isPublish, pageRequest, searchParam),
                AppMarketVersionDTO.class);
    }

    private Page<ApplicationReleasingDTO> getReleasingDTOs(Long projectId,
                                                           Page<ApplicationMarketE> applicationMarketEPage) {
        Page<ApplicationReleasingDTO> applicationReleasingDTOPage = ConvertPageHelper.convertPage(
                applicationMarketEPage,
                ApplicationReleasingDTO.class);
        List<ApplicationReleasingDTO> applicationReleasingDTOList = applicationReleasingDTOPage.getContent();
        for (ApplicationReleasingDTO applicationReleasingDTO : applicationReleasingDTOList) {
            Long appMarketId = applicationReleasingDTO.getId();
            List<DevopsAppMarketVersionDO> marketVersionDOS = applicationMarketRepository
                    .getVersions(projectId, appMarketId, true);
            List<AppMarketVersionDTO> marketVersionDTOList = ConvertHelper
                    .convertList(marketVersionDOS, AppMarketVersionDTO.class);
            applicationReleasingDTO.setAppVersions(marketVersionDTOList);
        }
        return applicationReleasingDTOPage;
    }
}
