package io.choerodon.devops.infra.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.dto.ApplicationServiceDTO;
import io.choerodon.devops.infra.dto.ApplicationVersionDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.feign.ChartClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Sheep on 2019/4/22.
 */

@Component
public class ChartUtil {

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

    public void downloadChart(ApplicationVersionDTO applicationVersionDTO, OrganizationDTO organizationDTO, ProjectDTO projectDTO, ApplicationServiceDTO applicationDTO, String destpath) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setType(CHART);
        configurationProperties.setBaseUrl(applicationVersionDTO.getRepository().split(organizationDTO.getCode() + "/" + projectDTO.getCode())[0]);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        ChartClient chartClient = retrofit.create(ChartClient.class);
        Call<ResponseBody> getTaz = chartClient.downloadTaz(organizationDTO.getCode(), projectDTO.getCode(), applicationDTO.getCode(), applicationVersionDTO.getVersion());
        try {
            Response<ResponseBody> response = getTaz.execute();
            try (FileOutputStream fos = new FileOutputStream(String.format("%s%s%s-%s.tgz",
                    destpath,
                    FILE_SEPARATOR,
                    applicationDTO.getCode(),
                    applicationVersionDTO.getVersion()))) {
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
    }

}
