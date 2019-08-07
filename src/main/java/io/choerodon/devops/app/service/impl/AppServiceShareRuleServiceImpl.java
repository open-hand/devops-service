package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.devops.infra.dto.AppServiceShareRuleDTO;
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
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.DevopsMarketConnectInfoDTO;
import io.choerodon.devops.infra.feign.AppShareClient;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.AppServiceShareRuleMapper;
import io.choerodon.devops.infra.mapper.AppServiceVersionReadmeMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;


/**
 * Created by ernst on 2018/5/12.
 */
@Service
public class AppServiceShareRuleServiceImpl implements AppServiceShareRuleService {
    private static final String CHARTS = "charts";
    private static final String CHART = "chart";
    private static final String ORGANIZATION = "organization";
    private static final String PROJECTS = "projects";
    private static final String IMAGES = "images";
    private static final String PUSH_IAMGES = "push_image.sh";
    private static final String JSON_FILE = ".json";

    private static final String FILE_SEPARATOR = "/";
    private static final Logger logger = LoggerFactory.getLogger(AppServiceShareRuleServiceImpl.class);

    private static Gson gson = new Gson();
    private JSON json = new JSON();

    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${services.helm.url}")
    private String helmUrl;

    @Autowired
    private IamService iamService;
    @Autowired
    private AppServiceVersionReadmeMapper appServiceVersionReadmeMapper;
    @Autowired
    private MarketConnectInfoService marketConnectInfoService;
    @Autowired
    private AppServiceShareRuleMapper appServiceShareRuleMapper;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private AppSevriceService applicationService;
    @Autowired
    private DevopsProjectConfigService devopsProjectConfigService;

    @Override
    @Transactional
    public AppServiceShareRuleVO createOrUpdate(Long projectId, AppServiceShareRuleVO appServiceShareRuleVO) {
        AppServiceShareRuleDTO appServiceShareRuleDTO = ConvertUtils.convertObject(appServiceShareRuleVO, AppServiceShareRuleDTO.class);
        if (appServiceShareRuleDTO.getId() == null) {
            if (appServiceShareRuleMapper.insert(appServiceShareRuleDTO) != 1) {
                throw new CommonException("error.insert.application.share.rule.insert");
            }
        } else {
            if (appServiceShareRuleMapper.updateByPrimaryKeySelective(appServiceShareRuleDTO) != 1) {
                throw new CommonException("error.insert.application.share.rule.update");
            }
        }
        return ConvertUtils.convertObject(appServiceShareRuleDTO, AppServiceShareRuleVO.class);
    }

    @Override
    public PageInfo<AppServiceShareRuleVO> pageByOptions(Long appServiceId, PageRequest pageRequest, String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        PageInfo<AppServiceShareRuleDTO> devopsProjectConfigDTOPageInfo = PageHelper.startPage(
                pageRequest.getPage(),
                pageRequest.getSize(),
                PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
                () -> appServiceShareRuleMapper.listByOptions(appServiceId,
                        (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                        (String) mapParams.get(TypeUtil.PARAM)));
        PageInfo<AppServiceShareRuleVO> shareRuleVOPageInfo = ConvertUtils.convertPage(devopsProjectConfigDTOPageInfo, AppServiceShareRuleVO.class);
        List<AppServiceShareRuleVO> appServiceShareRuleVOS = shareRuleVOPageInfo.getList().stream().peek(t -> t.setProjectName(iamService.queryIamProject(t.getAppId()).getName())).collect(Collectors.toList());
        shareRuleVOPageInfo.setList(appServiceShareRuleVOS);
        return shareRuleVOPageInfo;
    }

    @Override
    public AppServiceShareRuleVO query(Long projectId, Long ruleId) {
        AppServiceShareRuleVO appServiceShareRuleVO = ConvertUtils.convertObject(appServiceShareRuleMapper.selectByPrimaryKey(ruleId), AppServiceShareRuleVO.class);
        appServiceShareRuleVO.setProjectName(iamService.queryIamProject(appServiceShareRuleVO.getAppId()).getName());
        return appServiceShareRuleVO;
    }


    @Override
    public AppServiceVersionAndValueVO getValuesAndChart(Long versionId) {
        AppServiceVersionAndValueVO appServiceVersionAndValueVO = new AppServiceVersionAndValueVO();
        String versionValue = FileUtil.checkValueFormat(appServiceVersionService.baseQueryValue(versionId));
        AppServiceVersionRemoteVO versionRemoteDTO = new AppServiceVersionRemoteVO();
        versionRemoteDTO.setValues(versionValue);
        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(versionId);
        if (appServiceVersionDTO != null) {
            versionRemoteDTO.setId(versionId);
            versionRemoteDTO.setRepository(appServiceVersionDTO.getRepository());
            versionRemoteDTO.setVersion(appServiceVersionDTO.getVersion());
            versionRemoteDTO.setImage(appServiceVersionDTO.getImage());
            versionRemoteDTO.setReadMeValue(appServiceVersionReadmeMapper.selectByPrimaryKey(appServiceVersionDTO.getReadmeValueId()).getReadme());
            AppServiceDTO applicationDTO = applicationService.baseQuery(appServiceVersionDTO.getAppServiceId());
            if (applicationDTO.getHarborConfigId() == null) {
                appServiceVersionAndValueVO.setHarbor(gson.fromJson(devopsProjectConfigService.baseQueryByName(null, "harbor_default").getConfig(), ProjectConfigVO.class));
                appServiceVersionAndValueVO.setChart(gson.fromJson(devopsProjectConfigService.baseQueryByName(null, "chart_default").getConfig(), ProjectConfigVO.class));
            } else {
                appServiceVersionAndValueVO.setHarbor(gson.fromJson(devopsProjectConfigService.baseQuery(applicationDTO.getHarborConfigId()).getConfig(), ProjectConfigVO.class));
                appServiceVersionAndValueVO.setChart(gson.fromJson(devopsProjectConfigService.baseQuery(applicationDTO.getChartConfigId()).getConfig(), ProjectConfigVO.class));
            }
            appServiceVersionAndValueVO.setVersionRemoteDTO(versionRemoteDTO);
        }
        return appServiceVersionAndValueVO;
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
