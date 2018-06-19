package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.ApplicationVersionRepDTO;
import io.choerodon.devops.app.service.ApplicationVersionService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;
import io.choerodon.devops.domain.application.entity.ApplicationVersionValueE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.factory.ApplicationVersionEFactory;
import io.choerodon.devops.domain.application.factory.ApplicationVersionValueFactory;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.ApplicationVersionRepository;
import io.choerodon.devops.domain.application.repository.ApplicationVersionValueRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/3.
 */
@Service
public class ApplicationVersionServiceImpl implements ApplicationVersionService {

    private static final String DESTPATH = "devops";

    private ApplicationVersionRepository applicationVersionRepository;
    private ApplicationRepository applicationRepository;
    private IamRepository iamRepository;
    private ApplicationVersionValueRepository applicationVersionValueRepository;

    @Value("${services.helm.url}")
    private String helmUrl;


    public ApplicationVersionServiceImpl(ApplicationVersionRepository applicationVersionRepository, ApplicationRepository applicationRepository, IamRepository iamRepository, ApplicationVersionValueRepository applicationVersionValueRepository) {
        this.applicationVersionRepository = applicationVersionRepository;
        this.applicationRepository = applicationRepository;
        this.iamRepository = iamRepository;
        this.applicationVersionValueRepository = applicationVersionValueRepository;
    }

    @Override
    public Page<ApplicationVersionRepDTO> listApplicationVersion(Long projectId, PageRequest pageRequest, String searchParam) {
        Page<ApplicationVersionE> applicationVersionEPage = applicationVersionRepository.listApplicationVersion(
                projectId, pageRequest, searchParam);
        return ConvertPageHelper.convertPage(applicationVersionEPage, ApplicationVersionRepDTO.class);
    }

    @Override
    public void create(String image, String token, String version, String commit, MultipartFile files) {
        ApplicationE applicationE = applicationRepository.queryByToken(token);

        ApplicationVersionValueE applicationVersionValueE = ApplicationVersionValueFactory.create();
        ApplicationVersionE applicationVersionE = ApplicationVersionEFactory.create();
        ProjectE projectE = iamRepository.queryIamProject(applicationE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        ApplicationVersionE newApplicationVersionE = applicationVersionRepository
                .queryByAppAndVersion(applicationE.getId(), version);
        applicationVersionE.initApplicationEById(applicationE.getId());
        applicationVersionE.setImage(image);
        applicationVersionE.setCommit(commit);
        applicationVersionE.setVersion(version);
        applicationVersionE.setRepository("/" + organization.getCode() + "/" + projectE.getCode() + "/");
        String classPath = String.format("Charts%s%s%s%s",
                System.getProperty("file.separator"),
                organization.getCode(),
                System.getProperty("file.separator"),
                projectE.getCode());
        String path = FileUtil.multipartFileToFile(classPath, files);
        if (newApplicationVersionE != null) {
            return;
        }
        try {
            FileUtil.unTarGZ(path, DESTPATH);
            applicationVersionValueE.setValue(
                    FileUtil.replaceReturnString(new FileInputStream(new File(FileUtil.queryFileFromFiles(
                            new File(DESTPATH), "values.yaml").getAbsolutePath())), null));

            applicationVersionE.initApplicationVersionValueE(applicationVersionValueRepository
                    .create(applicationVersionValueE).getId());
        } catch (Exception e) {
            throw new CommonException("error.version.insert");
        }
        applicationVersionE.initApplicationVersionReadmeV(FileUtil.getReadme(DESTPATH));
        applicationVersionRepository.create(applicationVersionE);
        FileUtil.deleteFile(new File(DESTPATH));
    }

    @Override
    public List<ApplicationVersionRepDTO> listByAppId(Long appId, Boolean isPublish) {
        return ConvertHelper.convertList(
                applicationVersionRepository.listByAppId(appId, isPublish), ApplicationVersionRepDTO.class);
    }

    @Override
    public List<ApplicationVersionRepDTO> listByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return ConvertHelper.convertList(
                applicationVersionRepository.listByAppIdAndEnvId(projectId, appId, envId),
                ApplicationVersionRepDTO.class);
    }

    @Override
    public Page<ApplicationVersionRepDTO> listApplicationVersionInApp(Long projectId, Long appId, PageRequest pageRequest, String searchParam) {
        Page<ApplicationVersionE> applicationVersionEPage = applicationVersionRepository.listApplicationVersionInApp(
                projectId, appId, pageRequest, searchParam);
        return ConvertPageHelper.convertPage(applicationVersionEPage, ApplicationVersionRepDTO.class);
    }
}
