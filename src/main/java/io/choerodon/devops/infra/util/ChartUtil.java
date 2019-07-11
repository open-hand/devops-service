package io.choerodon.devops.infra.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.ApplicationE;
import io.choerodon.devops.api.vo.iam.entity.ApplicationVersionE;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.RetrofitHandler;
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

    public void downloadChart(ApplicationVersionE applicationVersionE, OrganizationVO organization, ProjectVO projectE, ApplicationE applicationE, String destpath) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setType(CHART);
        configurationProperties.setBaseUrl(applicationVersionE.getRepository().split(organization.getCode() + "/" + projectE.getCode())[0]);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        ChartClient chartClient = retrofit.create(ChartClient.class);
        Call<ResponseBody> getTaz = chartClient.downloadTaz(organization.getCode(), projectE.getCode(), applicationE.getCode(), applicationVersionE.getVersion());
        try {
            Response<ResponseBody> response = getTaz.execute();
            try (FileOutputStream fos = new FileOutputStream(String.format("%s%s%s-%s.tgz",
                    destpath,
                    FILE_SEPARATOR,
                    applicationE.getCode(),
                    applicationVersionE.getVersion()))) {
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
