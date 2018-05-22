package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.dto.ApplicationReleasingDTO;
import io.choerodon.devops.api.dto.ApplicationVersionRepDTO;
import io.choerodon.devops.app.service.ApplicationMarketService;
import io.choerodon.devops.domain.application.entity.ApplicationMarketE;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.factory.ApplicationMarketFactory;
import io.choerodon.devops.domain.application.repository.ApplicationMarketRepository;
import io.choerodon.devops.domain.application.repository.ApplicationVersionRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.infra.feign.FileFeignClient;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by ernst on 2018/5/12.
 */
@Service
public class ApplicationMarketServiceImpl implements ApplicationMarketService {

    private ApplicationVersionRepository applicationVersionRepository;
    private ApplicationMarketRepository applicationMarketRepository;
    private IamRepository iamRepository;
    private FileFeignClient fileFeignClient;

    private static final String USER_NOT_LOGIN_EXCEPTION = "error.user.not.login";
    private static final String USER_ID_NOT_EQUAL_EXCEPTION = "error.user.id.not.equals";

    /**
     * 构造函数
     */
    public ApplicationMarketServiceImpl(ApplicationVersionRepository applicationVersionRepository, ApplicationMarketRepository applicationMarketRepository,
                                        IamRepository iamRepository, FileFeignClient fileFeignClient) {
        this.applicationVersionRepository = applicationVersionRepository;
        this.applicationMarketRepository = applicationMarketRepository;
        this.iamRepository = iamRepository;
        this.fileFeignClient = fileFeignClient;
    }

    @Override
    public void release(Long projectId, ApplicationReleasingDTO applicationReleasingDTO,MultipartFile file) {
        if(applicationReleasingDTO!=null){
            List<ApplicationVersionRepDTO> appVersions = applicationReleasingDTO.getAppVersions();
            if (appVersions != null && !appVersions.isEmpty()) {
                List<Long> ids = new ArrayList<>();
                for (ApplicationVersionRepDTO appVersion : appVersions) {
                    ids.add(appVersion.getId());
                }
                applicationVersionRepository.updatePublishLevelByIds(ids, 1L);
            }
        }

        String imgUrl = "www.default.com";
        if(file!=null){
            Long organizationId = DetailsHelper.getUserDetails().getOrganizationId();
            String bakcetName = "devops-service";
            imgUrl = fileFeignClient.uploadFile(organizationId, bakcetName, file.getOriginalFilename(), file).getBody();
        }

        ApplicationMarketE applicationMarketE = ApplicationMarketFactory.create();
        applicationMarketE.initApplicationEById(applicationReleasingDTO.getAppId());
        applicationMarketE.setPublishLevel(applicationReleasingDTO.getPublishLevel());
        applicationMarketE.setImgUrl(imgUrl);
        applicationMarketE.setActive(true);
        applicationMarketE.setContributor(applicationReleasingDTO.getContributor());
        applicationMarketE.setDescription(applicationReleasingDTO.getDescription());
        applicationMarketE.setCategory(applicationReleasingDTO.getCategory());
        applicationMarketRepository.create(applicationMarketE);
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
            for (ProjectE project : projectEList) {
                projectIds.add(project.getId());
            }
            Page<ApplicationMarketE> applicationMarketEPage = applicationMarketRepository.listMarketApps(
                    projectIds, pageRequest, searchParam);
            return getReleasingDTOs(applicationMarketEPage);
        }
        return null;
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

    private void checkLoginUser(Long id) {
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        if (customUserDetails == null) {
            throw new CommonException(USER_NOT_LOGIN_EXCEPTION);
        }
        if (!id.equals(customUserDetails.getUserId())) {
            throw new CommonException(USER_ID_NOT_EQUAL_EXCEPTION);
        }
    }


}
