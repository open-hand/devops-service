package io.choerodon.devops.infra.util;

import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.AUTH_TYPE_PULL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.api.vo.chart.ChartDeleteResponseVO;
import io.choerodon.devops.api.vo.chart.ChartTagVO;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.feign.ChartClient;
import io.choerodon.devops.infra.handler.RetrofitHandler;

/**
 * Created by Sheep on 2019/4/22.
 */

@Component
public class ChartUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(ChartUtil.class);
    private static final String FILE_SEPARATOR = "/";
    private static final String APP_SERVICE = "appService";
    private static final String CHART = "chart";
    private static final String DEFAULT_ERROR_MESSAGE_FOR_UPLOADING = "error.upload.with.null.response";

    private static final Gson GSON = new Gson();

    @Autowired
    DevopsConfigService devopsConfigService;

    public static void uploadChart(String repository, String organizationCode, String projectCode, File file, @Nullable String username, @Nullable String password) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setType(CHART);
        repository = repository.endsWith("/") ? repository.substring(0, repository.length() - 1) : repository;
        configurationProperties.setBaseUrl(repository);
        configurationProperties.setUsername(username);
        configurationProperties.setPassword(password);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        file = new File(file.getAbsolutePath());

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData(CHART, file.getName(), requestFile);
        ChartClient chartClient = retrofit.create(ChartClient.class);
        Call<Object> uploadTaz = chartClient.uploadTaz(organizationCode, projectCode, body);

        Response<Object> response;
        try {
            response = uploadTaz.execute();
        } catch (Exception e) {
            throw new CommonException(e);
        }

        // 判断响应结果
        if (!response.isSuccessful()) {
            // 报409，可能是chart包已经存在，而chart museum又设置为不允许覆盖，这种情况认为是成功的
            // 报错信息形如:  {"error":"test/test/code-i-0.1.0.tgz already exists"}
            if (response.code() == 409) {
                LOGGER.info("409 for uploading chart: the repo: {}, orgCode {}, proCode {}, file name {}, username {}, password is null: {}", repository, organizationCode, projectCode, file.getName(), username, password == null);
                return;
            }

            // 读取错误信息
            String errorMessage = response.body() == null ? null : response.body().toString();
            if (errorMessage == null) {
                errorMessage = readErrorMessage(response, repository, organizationCode, projectCode, file, username, password);
            }
            throw new CommonException(errorMessage == null ? DEFAULT_ERROR_MESSAGE_FOR_UPLOADING : errorMessage);
        }
    }

    private static String readErrorMessage(Response<?> response, String repository, String organizationCode, String projectCode, File file, @Nullable String username, @Nullable String password) {
        ResponseBody errorBody = response.errorBody();
        if (errorBody != null) {
            try {
                return errorBody.string();
            } catch (Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Failed to read error response. the repo: {}, orgCode {}, proCode {}, file name {}, username {}, password is null: {}", repository, organizationCode, projectCode, file.getName(), username, password == null);
                    LOGGER.debug("And the ex is", e);
                }
            }
        }
        return null;
    }

    public void deleteChart(ChartTagVO chartTagVO) {
        DevopsConfigDTO devopsConfigDTO = devopsConfigService.queryRealConfig(chartTagVO.getAppServiceId(), APP_SERVICE, CHART, AUTH_TYPE_PULL);
        ConfigVO helmConfig = GSON.fromJson(devopsConfigDTO.getConfig(), ConfigVO.class);

        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setType(CHART);
        configurationProperties.setUsername(helmConfig.getUserName());
        configurationProperties.setPassword(helmConfig.getPassword());
        configurationProperties.setBaseUrl(chartTagVO.getRepository().split(chartTagVO.getOrgCode() + "/" + chartTagVO.getProjectCode())[0]);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        ChartClient chartClient = retrofit.create(ChartClient.class);
        Call<ChartDeleteResponseVO> call = chartClient.deleteChartVersion(chartTagVO.getOrgCode(), chartTagVO.getProjectCode(), chartTagVO.getChartName(), chartTagVO.getChartVersion());
        try {
            Response<ChartDeleteResponseVO> response = call.execute();
        } catch (Exception e) {
            LOGGER.error("error.delete.chart。repository: {}, chartName：{}，chartVersion：{} ", chartTagVO.getRepository(), chartTagVO.getChartName(), chartTagVO.getChartVersion());
        }
    }

    public void downloadChart(AppServiceVersionDTO appServiceVersionDTO, Tenant organizationDTO, ProjectDTO projectDTO, AppServiceDTO applicationDTO, String destpath) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setType(CHART);
        configurationProperties.setBaseUrl(appServiceVersionDTO.getRepository().split(organizationDTO.getTenantNum() + "/" + projectDTO.getCode())[0]);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        ChartClient chartClient = retrofit.create(ChartClient.class);
        Call<ResponseBody> getTaz = chartClient.downloadTaz(organizationDTO.getTenantNum(), projectDTO.getCode(), applicationDTO.getCode(), appServiceVersionDTO.getVersion());
        FileOutputStream fos = null;
        try {
            Response<ResponseBody> response = getTaz.execute();
            fos = new FileOutputStream(String.format("%s%s%s-%s.tgz",
                    destpath,
                    FILE_SEPARATOR,
                    applicationDTO.getCode(),
                    appServiceVersionDTO.getVersion()));
            if (response.body() != null) {
                InputStream is = response.body().byteStream();
                byte[] buffer = new byte[4096];
                int r = 0;
                while ((r = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, r);
                }
                is.close();
            }
            fos.close();
        } catch (IOException e) {
            IOUtils.closeQuietly(fos);
            FileUtil.deleteDirectory(new File(destpath).getParentFile());
            throw new CommonException("error.download.chart", e.getMessage());
        }
    }
}
