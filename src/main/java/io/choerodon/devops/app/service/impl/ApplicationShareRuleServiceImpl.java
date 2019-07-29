package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.kubernetes.client.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Response;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.ApplicationDTO;
import io.choerodon.devops.infra.dto.ApplicationShareRuleDTO;
import io.choerodon.devops.infra.dto.ApplicationVersionDTO;
import io.choerodon.devops.infra.dto.DevopsMarketConnectInfoDTO;
import io.choerodon.devops.infra.feign.AppShareClient;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.ApplicationShareRuleMapper;
import io.choerodon.devops.infra.mapper.ApplicationVersionReadmeMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;


/**
 * Created by ernst on 2018/5/12.
 */
@Service
public class ApplicationShareRuleServiceImpl implements ApplicationShareRuleService {
    private static final String CHARTS = "charts";
    private static final String CHART = "chart";
    private static final String ORGANIZATION = "organization";
    private static final String PROJECTS = "projects";
    private static final String IMAGES = "images";
    private static final String PUSH_IAMGES = "push_image.sh";
    private static final String JSON_FILE = ".json";

    private static final String FILE_SEPARATOR = "/";
    private static final Logger logger = LoggerFactory.getLogger(ApplicationShareRuleServiceImpl.class);

    private static Gson gson = new Gson();
    private JSON json = new JSON();

    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${services.helm.url}")
    private String helmUrl;

    @Autowired
    private IamService iamService;
    @Autowired
    private ApplicationVersionReadmeMapper applicationVersionReadmeMapper;
    @Autowired
    private MarketConnectInfoService marketConnectInfoService;
    @Autowired
    private ApplicationShareRuleMapper applicationShareRuleMapper;
    @Autowired
    private ApplicationVersionService applicationVersionService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private DevopsProjectConfigService devopsProjectConfigService;

    @Override
    @Transactional
    public ApplicationShareRuleVO createOrUpdate(Long projectId, ApplicationShareRuleVO applicationShareRuleVO) {
        ApplicationShareRuleDTO applicationShareRuleDTO = ConvertUtils.convertObject(applicationShareRuleVO, ApplicationShareRuleDTO.class);
        applicationShareRuleDTO.setOrganizationId(iamService.queryIamProject(projectId).getOrganizationId());
        if (applicationShareRuleDTO.getId() == null) {
            if (applicationShareRuleMapper.insert(applicationShareRuleDTO) != 1) {
                throw new CommonException("error.insert.application.share.rule.insert");
            }
        } else {
            if (applicationShareRuleMapper.updateByPrimaryKeySelective(applicationShareRuleDTO) != 1) {
                throw new CommonException("error.insert.application.share.rule.update");
            }
        }
        return ConvertUtils.convertObject(applicationShareRuleDTO, ApplicationShareRuleVO.class);
    }

    @Override
    public PageInfo<ApplicationShareRuleVO> pageByOptions(Long projectId, PageRequest pageRequest, String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        PageInfo<ApplicationShareRuleDTO> devopsProjectConfigDTOPageInfo = PageHelper.startPage(
                pageRequest.getPage(),
                pageRequest.getSize(),
                PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
                () -> applicationShareRuleMapper.listByOptions(projectId,
                        (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                        (String) mapParams.get(TypeUtil.PARAM)));
        PageInfo<ApplicationShareRuleVO> shareRuleVOPageInfo = ConvertUtils.convertPage(devopsProjectConfigDTOPageInfo, ApplicationShareRuleVO.class);
        List<ApplicationShareRuleVO> applicationShareRuleVOS = shareRuleVOPageInfo.getList().stream().peek(t -> t.setProjectName(iamService.queryIamProject(t.getProjectId()).getName())).collect(Collectors.toList());
        shareRuleVOPageInfo.setList(applicationShareRuleVOS);
        return shareRuleVOPageInfo;
    }

    @Override
    public ApplicationShareRuleVO query(Long projectId, Long ruleId) {
        ApplicationShareRuleVO applicationShareRuleVO = ConvertUtils.convertObject(applicationShareRuleMapper.selectByPrimaryKey(ruleId), ApplicationShareRuleVO.class);
        applicationShareRuleVO.setProjectName(iamService.queryIamProject(applicationShareRuleVO.getProjectId()).getName());
        return applicationShareRuleVO;
    }


    @Override
    public AppVersionAndValueVO getValuesAndChart(Long versionId) {
        AppVersionAndValueVO appVersionAndValueVO = new AppVersionAndValueVO();
        String versionValue = FileUtil.checkValueFormat(applicationVersionService.baseQueryValue(versionId));
        ApplicationVersionRemoteVO versionRemoteDTO = new ApplicationVersionRemoteVO();
        versionRemoteDTO.setValues(versionValue);
        ApplicationVersionDTO applicationVersionDTO = applicationVersionService.baseQuery(versionId);
        if (applicationVersionDTO != null) {
            versionRemoteDTO.setId(versionId);
            versionRemoteDTO.setRepository(applicationVersionDTO.getRepository());
            versionRemoteDTO.setVersion(applicationVersionDTO.getVersion());
            versionRemoteDTO.setImage(applicationVersionDTO.getImage());
            versionRemoteDTO.setReadMeValue(applicationVersionReadmeMapper.selectByPrimaryKey(applicationVersionDTO.getReadmeValueId()).getReadme());
            ApplicationDTO applicationDTO = applicationService.baseQuery(applicationVersionDTO.getAppId());
            if (applicationDTO.getHarborConfigId() == null) {
                appVersionAndValueVO.setHarbor(gson.fromJson(devopsProjectConfigService.baseQueryByName(null, "harbor_default").getConfig(), ProjectConfigVO.class));
                appVersionAndValueVO.setChart(gson.fromJson(devopsProjectConfigService.baseQueryByName(null, "chart_default").getConfig(), ProjectConfigVO.class));
            } else {
                appVersionAndValueVO.setHarbor(gson.fromJson(devopsProjectConfigService.baseQuery(applicationDTO.getHarborConfigId()).getConfig(), ProjectConfigVO.class));
                appVersionAndValueVO.setChart(gson.fromJson(devopsProjectConfigService.baseQuery(applicationDTO.getChartConfigId()).getConfig(), ProjectConfigVO.class));
            }
            appVersionAndValueVO.setVersionRemoteDTO(versionRemoteDTO);
        }
        return appVersionAndValueVO;
    }

    @Override
    public AccessTokenCheckResultVO checkToken(AccessTokenVO tokenDTO) {
        AppShareClient appShareClient = RetrofitHandler.getAppShareClient(tokenDTO.getSaasMarketUrl());
        Response<AccessTokenCheckResultVO> tokenDTOResponse = null;

        try {
            tokenDTOResponse = appShareClient.checkTokenExist(tokenDTO.getAccessToken()).execute();
            if (!tokenDTOResponse.isSuccessful()) {
                throw new CommonException("error.check.token");
            }
        } catch (IOException e) {
            throw new CommonException("error.check.token");
        }
        return tokenDTOResponse.body();
    }

    @Override
    public void saveToken(AccessTokenVO tokenDTO) {
        DevopsMarketConnectInfoDTO connectInfoDO = new DevopsMarketConnectInfoDTO();
        BeanUtils.copyProperties(tokenDTO, connectInfoDO);
        marketConnectInfoService.baseCreateOrUpdate(connectInfoDO);
    }

}
