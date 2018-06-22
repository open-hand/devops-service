package io.choerodon.devops.app.service.impl;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.AppMarketDownloadDTO;
import io.choerodon.devops.api.dto.AppMarketTgzDTO;
import io.choerodon.devops.api.dto.AppMarketVersionDTO;
import io.choerodon.devops.api.dto.ApplicationReleasingDTO;
import io.choerodon.devops.app.service.ApplicationMarketService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.factory.ApplicationMarketFactory;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.devops.infra.common.util.GenerateUUID;
import io.choerodon.devops.infra.common.util.HttpClientUtil;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketDO;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketVersionDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by ernst on 2018/5/12.
 */
@Service
public class ApplicationMarketServiceImpl implements ApplicationMarketService {
    private static final String ORGANIZATION = "organization";
    private static final String PUBLIC = "public";
    private static final String CHARTS = "charts";
    private static final String IMAGES = "images";
    private static final Logger logger = LoggerFactory.getLogger(ApplicationMarketServiceImpl.class);
    private static final String FILE_SEPARATOR = "file.separator";
    private static Gson gson = new Gson();
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${services.helm.url}")
    private String helmUrl;

    @Autowired
    private ApplicationVersionRepository applicationVersionRepository;
    @Autowired
    private ApplicationMarketRepository applicationMarketRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;
    @Autowired
    private ApplicationVersionValueRepository applicationVersionValueRepository;

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
        ApplicationMarketE applicationMarketE =
                applicationMarketRepository.getMarket(projectId, appMarketId);
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
                applicationMarketRepository.getMarket(null, appMarketId);
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

        Date latestUpdateDate = appMarketVersionDTOList.isEmpty()
                ? getLaterDate(applicationE.getLastUpdateDate(), applicationMarketE.getMarketUpdatedDate())
                : getLatestDate(
                appMarketVersionDTOList.get(0).getUpdatedDate(),
                applicationE.getLastUpdateDate(),
                applicationMarketE.getMarketUpdatedDate());
        applicationReleasingDTO.setLastUpdatedDate(latestUpdateDate);

        Boolean versionExist = appMarketVersionDTOList.parallelStream().anyMatch(t -> t.getId().equals(versionId));
        Long latestVersionId = versionId;
        if (!versionExist) {
            Optional<AppMarketVersionDTO> optional = appMarketVersionDTOList.parallelStream()
                    .max(this::compareAppMarketVersionDTO);
            latestVersionId = optional.isPresent()
                    ? optional.get().getId()
                    : versionId;
        }
        String readme = applicationVersionRepository.getReadme(latestVersionId);

        applicationReleasingDTO.setReadme(readme);

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

    @Override
    public String getMarketAppVersionReadme(Long appMarketId, Long versionId) {
        applicationMarketRepository.checkMarketVersion(appMarketId, versionId);
        return applicationVersionRepository.getReadme(versionId);
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

    @Override
    public AppMarketTgzDTO getMarketAppListInFile(Long projectId, MultipartFile file) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String fileSeparator = File.separator;
        String classPath = String.format(
                "tmp%s%s%s%s",
                fileSeparator,
                organization.getCode(),
                fileSeparator,
                projectE.getCode());
        String path = FileUtil.multipartFileToFile(classPath, file);
        String destPath = String.format("%s%s%s", classPath, fileSeparator, "new");
        FileUtil.unZipFiles(new File(path), destPath);
        FileUtil.deleteFile(path);
        File zipDirectory = new File(destPath);
        AppMarketTgzDTO appMarketTgzDTO = new AppMarketTgzDTO();
        String fileCode;
        if (zipDirectory.exists() && zipDirectory.isDirectory()) {
            File[] chartsDirectory = zipDirectory.listFiles();
            if (chartsDirectory != null
                    && chartsDirectory.length == 1
                    && chartsDirectory[0].getName().equals(CHARTS)) {
                File[] appFiles = chartsDirectory[0].listFiles();
                if (appFiles == null || appFiles.length == 0) {
                    FileUtil.deleteDirectory(zipDirectory);
                    throw new CommonException("error.file.empty");
                }
                List<File> images = Arrays.stream(appFiles)
                        .filter(t -> t.getName().equals("images")).collect(Collectors.toCollection(ArrayList::new));
                // do sth with images[0]
                fileCode = hashImages(images);

                List<File> appFileList = Arrays.stream(appFiles)
                        .filter(File::isDirectory).collect(Collectors.toCollection(ArrayList::new));
                // do sth with appFileList
                analyzeAppFile(appMarketTgzDTO.getAppMarketList(), appFileList, false);
            } else {
                FileUtil.deleteDirectory(zipDirectory);
                throw new CommonException("error.zip.illegal");
            }
        } else {
            FileUtil.deleteDirectory(zipDirectory);
            throw new CommonException("error.zip.empty");
        }
        appMarketTgzDTO.setFileCode(
                zipDirectory.renameTo(new File(String.format("%s%s%s", classPath, fileSeparator, fileCode)))
                        ? fileCode : "new");
        return appMarketTgzDTO;
    }

    @Override
    public void importApps(Long projectId, String fileName, Boolean isPublic) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String fileSeparator = File.separator;
        String destPath = String.format(
                "tmp%s%s%s%s%s%s",
                fileSeparator,
                organization.getCode(),
                fileSeparator,
                projectE.getCode(),
                fileSeparator,
                fileName);
        File zipDirectory = new File(destPath);

        if (zipDirectory.exists() && zipDirectory.isDirectory()) {
            File[] chartsDirectory = zipDirectory.listFiles();
            if (chartsDirectory != null
                    && chartsDirectory.length == 1
                    && chartsDirectory[0].getName().equals(CHARTS)) {
                File[] appFiles = chartsDirectory[0].listFiles();
                if (appFiles == null || appFiles.length == 0) {
                    FileUtil.deleteDirectory(zipDirectory);
                    throw new CommonException("error.file.empty");
                }
                List<File> appFileList = Arrays.stream(appFiles)
                        .filter(File::isDirectory).collect(Collectors.toCollection(ArrayList::new));
                importAppFile(projectId, appFileList, isPublic);
            } else {
                throw new CommonException("error.zip.illegal");
            }
        } else {
            throw new CommonException("error.zip.notFound");
        }
        FileUtil.deleteDirectory(zipDirectory);
    }

    @Override
    public void deleteZip(Long projectId, String fileName) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String fileSeparator = File.separator;
        String destPath = String.format(
                "tmp%s%s%s%s%s%s",
                fileSeparator,
                organization.getCode(),
                fileSeparator,
                projectE.getCode(),
                fileSeparator,
                fileName);
        File zipDirectory = new File(destPath);
        FileUtil.deleteDirectory(zipDirectory);
    }

    private String hashImages(List<File> images) {
        if (images != null && !images.isEmpty() && images.size() == 1) {
            File image = images.get(0);
            String imagePath = image.getAbsolutePath();
            try {
                return FileUtil.md5HashCode(imagePath);
            } catch (FileNotFoundException e) {
                logger.info(e.getMessage());
                throw new CommonException("error.image.read");
            }
        } else {
            throw new CommonException("error.images.illegal");
        }
    }

    private void analyzeAppFile(List<ApplicationReleasingDTO> appMarketVersionDTOS,
                                List<File> appFileList,
                                Boolean del) {
        appFileList.parallelStream().forEach(t -> {
            String appName = t.getName();
            File[] appFiles = t.listFiles();
            if (appFiles != null && !appFileList.isEmpty()) {
                String appFileName = String.format("%s%s", appName, ".json");
                List<File> appMarkets = Arrays.stream(appFiles).parallel()
                        .filter(k -> k.getName().equals(appFileName))
                        .collect(Collectors.toCollection(ArrayList::new));
                if (appMarkets != null && !appMarkets.isEmpty() && appMarkets.size() == 1) {
                    File appMarket = appMarkets.get(0);
                    String appMarketJson = FileUtil.getFileContent(appMarket);
                    ApplicationReleasingDTO appMarketVersionDTO =
                            gson.fromJson(appMarketJson, ApplicationReleasingDTO.class);
                    appMarketVersionDTOS.add(appMarketVersionDTO);
                    if (del) {
                        FileUtil.deleteFile(appMarket);
                    }
                }
            }
        });
    }

    private void importAppFile(Long projectId, List<File> appFileList, Boolean isPublic) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        appFileList.parallelStream().forEach(t -> {
            String appName = t.getName();
            File[] appFiles = t.listFiles();
            if (appFiles != null && !appFileList.isEmpty()) {
                String appFileName = String.format("%s%s", appName, ".json");
                List<File> appMarkets = Arrays.stream(appFiles).parallel()
                        .filter(k -> k.getName().equals(appFileName))
                        .collect(Collectors.toCollection(ArrayList::new));
                if (appMarkets != null && !appMarkets.isEmpty() && appMarkets.size() == 1) {
                    File appMarket = appMarkets.get(0);
                    String appMarketJson = FileUtil.getFileContent(appMarket);
                    ApplicationReleasingDTO applicationReleasingDTO =
                            gson.fromJson(appMarketJson, ApplicationReleasingDTO.class);
                    ApplicationE applicationE = new ApplicationE();
                    applicationE.setCode(applicationReleasingDTO.getCode());
                    applicationE.setName(applicationReleasingDTO.getName());
                    applicationE.initProjectE(projectId);
                    applicationE.setActive(true);
                    applicationE.setSynchro(true);
                    applicationE.setToken(GenerateUUID.generateUUID());
                    Long appId = applicationRepository.create(applicationE).getId();
                    applicationReleasingDTO.getAppVersions().parallelStream()
                            .forEach(appVersion -> createVersionAndApp(
                                    appVersion, organization, projectE, applicationE, appId, appFiles
                            ));
                    // 发布应用
                    releaseApp(isPublic, applicationReleasingDTO);
                }
            }
        });
    }

    /**
     * 导出应用市场应用 zip
     *
     * @param appMarkets 应用市场应用信息
     */
    public void export(List<AppMarketDownloadDTO> appMarkets) {
        List<String> images = new ArrayList<>();
        for (AppMarketDownloadDTO appMarketDownloadDTO : appMarkets) {
            ApplicationReleasingDTO applicationReleasingDTO = getMarketApp(appMarketDownloadDTO.getAppMarketId(), null);
            String destpath = String.format("charts%s%s",
                    System.getProperty(FILE_SEPARATOR),
                    applicationReleasingDTO.getCode());
            ApplicationE applicationE = applicationRepository.query(applicationReleasingDTO.getAppId());
            ProjectE projectE = iamRepository.queryIamProject(applicationE.getProjectE().getId());
            Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
            String appMarketJson = gson.toJson(applicationReleasingDTO);
            FileUtil.saveDataToFile(destpath, applicationReleasingDTO.getCode() + ".json", appMarketJson);
            appMarketDownloadDTO.getAppVersionIds().parallelStream().forEach(appVersionId -> {
                ApplicationVersionE applicationVersionE = applicationVersionRepository.query(appVersionId);
                images.add(applicationVersionE.getImage());
                String repoUrl = String.format("%s%s%s%s%s%s%s%s%s%s%s%s", helmUrl,
                        System.getProperty(FILE_SEPARATOR),
                        organization.getCode(),
                        System.getProperty(FILE_SEPARATOR),
                        projectE.getCode(),
                        System.getProperty(FILE_SEPARATOR),
                        CHARTS,
                        System.getProperty(FILE_SEPARATOR),
                        applicationE.getCode(),
                        "-",
                        applicationVersionE.getVersion(),
                        ".tgz");

                HttpClientUtil.getTgz(repoUrl,
                        String.format("%s%s%s-%s.tgz",
                                destpath,
                                System.getProperty(FILE_SEPARATOR),
                                applicationE.getCode(),
                                applicationVersionE.getVersion()));

            });
            StringBuilder stringBuilder = new StringBuilder();
            for (String image : images) {
                stringBuilder.append(image);
                stringBuilder.append(System.getProperty("line.separator"));
            }
            FileUtil.saveDataToFile(CHARTS, IMAGES, stringBuilder.toString());
        }
        try (FileOutputStream outputStream = new FileOutputStream(CHARTS + ".zip")) {
            FileUtil.toZip(CHARTS, outputStream, true);
            FileUtil.deleteDirectory(new File(CHARTS));
        } catch (IOException e) {
            throw new CommonException(e.getMessage());
        }
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


    private void createVersionAndApp(AppMarketVersionDTO appVersion,
                                     Organization organization,
                                     ProjectE projectE,
                                     ApplicationE applicationE,
                                     Long appId,
                                     File[] appFiles) {
        ApplicationVersionE applicationVersionE = new ApplicationVersionE();
        String image = String.format("%s%s%s%s%s%s%s%s%s", harborConfigurationProperties.getBaseUrl(),
                System.getProperty(FILE_SEPARATOR),
                organization.getCode(),
                "-",
                projectE.getCode(),
                System.getProperty(FILE_SEPARATOR),
                applicationE.getCode(),
                ":",
                appVersion.getVersion()
        );
        applicationVersionE.setImage(image);
        applicationVersionE.setRepository(String.format("%s%s%s%s%s",
                System.getProperty(FILE_SEPARATOR),
                organization.getCode(),
                System.getProperty(FILE_SEPARATOR),
                projectE.getCode(),
                System.getProperty(FILE_SEPARATOR)));
        applicationVersionE.setVersion(appVersion.getVersion());
        applicationVersionE.initApplicationEById(appId);
        String tazName = String.format("%s%s%s%s",
                applicationE.getCode(),
                "-",
                appVersion.getVersion(),
                ".tgz"
        );
        List<File> tgzVersions = Arrays.stream(appFiles).parallel()
                .filter(k -> k.getName().equals(tazName))
                .collect(Collectors.toCollection(ArrayList::new));
        if (!tgzVersions.isEmpty()) {
            ApplicationVersionValueE applicationVersionValueE = new ApplicationVersionValueE();
            try {
                FileUtil.unTarGZ(tgzVersions.get(0).getAbsolutePath(), applicationE.getCode());
                applicationVersionValueE.setValue(
                        FileUtil.replaceReturnString(new FileInputStream(new File(FileUtil.queryFileFromFiles(
                                new File(applicationE.getCode()), "values.yaml").getAbsolutePath())), null));

                applicationVersionE.initApplicationVersionValueE(applicationVersionValueRepository
                        .create(applicationVersionValueE).getId());
            } catch (Exception e) {
                throw new CommonException("error.version.insert");
            }
            applicationVersionE.initApplicationVersionReadmeV(FileUtil.getReadme(applicationE.getCode()));
            applicationVersionRepository.create(applicationVersionE);
            String classPath = String.format("Charts%s%s%s%s",
                    System.getProperty(FILE_SEPARATOR),
                    organization.getCode(),
                    System.getProperty(FILE_SEPARATOR),
                    projectE.getCode());
            FileUtil.moveFiles(tgzVersions.get(0).getAbsolutePath(), classPath);
            FileUtil.deleteDirectory(new File(applicationE.getCode()));
        }
    }

    private void releaseApp(Boolean isPublic, ApplicationReleasingDTO applicationReleasingDTO) {
        if (isPublic != null) {
            ApplicationMarketE applicationMarketE = new ApplicationMarketE();
            applicationMarketE.initApplicationEById(applicationReleasingDTO.getAppId());
            applicationMarketE.setPublishLevel(isPublic ? PUBLIC : ORGANIZATION);
            applicationMarketE.setActive(true);
            applicationMarketE.setContributor(applicationReleasingDTO.getContributor());
            applicationMarketE.setDescription(applicationReleasingDTO.getDescription());
            applicationMarketE.setCategory(applicationReleasingDTO.getCategory());
            applicationMarketRepository.create(applicationMarketE);
            Long appMarketId =
                    applicationMarketRepository.getMarketIdByAppId(applicationReleasingDTO.getAppId());
            applicationMarketRepository.updateVersion(appMarketId, null, true);
        }
    }
}
