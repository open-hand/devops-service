package io.choerodon.devops.infra.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.eventhandler.payload.AppServiceVersionDownloadVO;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.ChartClient;
import io.choerodon.devops.infra.handler.RetrofitHandler;

/**
 * Created by Sheep on 2019/4/22.
 */

@Component
public class ChartUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(ChartUtil.class);
    private static final String CHART = "chart";
    private static final String FILE_SEPARATOR = "/";
    @Value("${services.helm.url}")
    private String helmUrl;


    public void uploadChart(String organizationCode, String projectCode, File file) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setType(CHART);
        configurationProperties.setBaseUrl(helmUrl);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        file = new File(file.getAbsolutePath());
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData(CHART, file.getName(), requestFile);
        ChartClient chartClient = retrofit.create(ChartClient.class);
        Call<Object> uploadTaz = chartClient.uploadTaz(organizationCode, projectCode, body);
        try {
            uploadTaz.execute();
        } catch (IOException e) {
            throw new CommonException(e);
        }
    }

    public void downloadChart(AppServiceVersionDTO appServiceVersionDTO, OrganizationDTO organizationDTO, ProjectDTO projectDTO, AppServiceDTO applicationDTO, String destpath) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setType(CHART);
        configurationProperties.setBaseUrl(appServiceVersionDTO.getRepository().split(organizationDTO.getCode() + "/" + projectDTO.getCode())[0]);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        ChartClient chartClient = retrofit.create(ChartClient.class);
        Call<ResponseBody> getTaz = chartClient.downloadTaz(organizationDTO.getCode(), projectDTO.getCode(), applicationDTO.getCode(), appServiceVersionDTO.getVersion());
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

    public String downloadChartForAppMarket(AppServiceVersionDownloadVO appServiceVersionPayload, String appServiceCode, String destpath) {
        String repository = appServiceVersionPayload.getImage();
        repository = repository.replace("http://", "");
        String[] repositoryArray = repository.split("/");

        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setType(CHART);
        configurationProperties.setBaseUrl("http://" + repositoryArray[0]);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        ChartClient chartClient = retrofit.create(ChartClient.class);
        Call<ResponseBody> getTaz = chartClient.downloadTaz(repositoryArray[1], repositoryArray[2], appServiceCode, appServiceVersionPayload.getVersion());
        String fileName = String.format("%s%s%s-%s.tgz",
                destpath,
                FILE_SEPARATOR,
                appServiceCode,
                appServiceVersionPayload.getVersion());
        try {
            Response<ResponseBody> response = getTaz.execute();
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                if (response.body() != null) {
                    InputStream is = response.body().byteStream();
                    byte[] buffer = new byte[4096];
                    int r = 0;
                    while ((r = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, r);
                    }
                    is.close();
                }
            }
        } catch (IOException e) {
            throw new CommonException(e.getMessage(), e);
        }
        return fileName;
    }
}
