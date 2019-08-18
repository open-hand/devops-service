package io.choerodon.devops.infra.feign;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  12:06 2019/8/18
 * Description:
 */
public interface MarketServiceClient {

    @POST("v1/market_applications/url")
    Call<Void> uploadFile(@QueryMap Map<String, String> map);

}