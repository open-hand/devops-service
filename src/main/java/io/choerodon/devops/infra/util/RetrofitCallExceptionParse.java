package io.choerodon.devops.infra.util;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import retrofit2.Call;
import retrofit2.Response;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.ExceptionResponse;

/**
 * @author zongw.lee@gmail.com
 * @date 2019/8/29
 */
public class RetrofitCallExceptionParse {

    private static final Logger logger = LoggerFactory.getLogger(RetrofitCallExceptionParse.class);
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd hh:mm:ss")
            .enableComplexMapKeySerialization().create();

    private RetrofitCallExceptionParse() {
    }

    /**
     * 执行请求并返回执行对象
     *
     * @param call             需要执行的call
     * @param exceptionMessage 报错信息
     * @param clazz            期望的返回类型
     * @return T
     */
    public static <T> T executeCall(Call<ResponseBody> call, String exceptionMessage, Class<T> clazz) {
        return executeCallWithTarget(call, exceptionMessage, clazz, null);
    }

    public static <T> T executeCallWithTarget(Call<ResponseBody> call, String exceptionMessage, Class<T> clazz, String target) {
        String bodyStr = parseException(call, exceptionMessage);
        try {
            if (ObjectUtils.isEmpty(bodyStr)) {
                return null;
            }
            if (clazz.getName().equals(Void.class.getName())) {
                return null;
            }
            if (target != null) {
                JsonNode jsonNode = JsonHelper.OBJECT_MAPPER.readTree(bodyStr);
                bodyStr = jsonNode.get(target).toString();
            }

            return gson.fromJson(bodyStr, clazz);
        } catch (Exception e) {
            parseCommonException(bodyStr, exceptionMessage);
            throw new CommonException(exceptionMessage);
        }
    }

    public static <T> T executeCallWithTarget(Call<ResponseBody> call, String exceptionMessage, TypeReference<T> typeReference, String target) {
        String bodyStr = parseException(call, exceptionMessage);
        try {
            if (ObjectUtils.isEmpty(bodyStr)) {
                return null;
            }

            if (target != null) {
                JsonNode jsonNode = JsonHelper.OBJECT_MAPPER.readTree(bodyStr);
                bodyStr = jsonNode.get(target).toString();
            }

            return JsonHelper.unmarshalByJackson(bodyStr, typeReference);
        } catch (Exception e) {
            parseCommonException(bodyStr, exceptionMessage);
            throw new CommonException(exceptionMessage);
        }
    }

    /**
     * 执行请求并返回执行对象
     *
     * @param call             需要执行的call
     * @param exceptionMessage 报错信息
     * @param clazz            期望的返回类型
     * @return List<T>
     */
    public static <T> List<T> executeCallForList(Call<ResponseBody> call, String exceptionMessage, Class<T> clazz) {
        return Arrays.asList(gson.fromJson(parseException(call, exceptionMessage), clazz));
    }

    /**
     * 执行请求并返回执行对象
     *
     * @param call             需要执行的call
     * @param exceptionMessage 报错信息
     * @param clazzKey         期望返回Map的key类型
     * @param clazzValue       期望返回Map的value类型
     * @return Map<K, T>
     */
    public static <K, T> Map<K, T> executeCallForMap(Call<ResponseBody> call, String exceptionMessage, Class<K> clazzKey, Class<T> clazzValue) {
        Map map = gson.fromJson(parseException(call, exceptionMessage), Map.class);
        Map<K, T> resMap = new HashMap<>();
        map.forEach((k, v) ->
                resMap.put(getLoadedInstanceWithGson(k, clazzKey), getLoadedInstanceWithGson(v, clazzValue))
        );
        return resMap;
    }

    private static <T> T getLoadedInstanceWithGson(Object object, Class<T> clazz) {
        return gson.fromJson(gson.toJson(object), clazz);
    }

    private static String parseException(Call<ResponseBody> call, String exceptionMessage) {
        String bodyStr;
        try {
            Response<ResponseBody> execute = call.execute();
            if (execute == null) {
                logger.info("::Retrofit::response is null");
                throw new CommonException("devops.retrofit.execute.response.is.empty");
            }
            if (execute.raw().code() == HttpStatus.NO_CONTENT.value()) {
                return null;
            }
            if (!execute.isSuccessful()) {
                logger.info("::Retrofit::unsuccessful");
                Optional.ofNullable(execute.errorBody()).ifPresent(v -> {
                    try {
                        logger.info("::Retrofit::error body:{}", v.string());
                    } catch (IOException e) {
                        throw new CommonException("devops.retrofit.execute.is.unsuccessful", e);
                    }
                });
                throw new CommonException("devops.retrofit.execute.is.unsuccessful");
            }
            if (ObjectUtils.isEmpty(execute.body())) {
                logger.info("::Retrofit::response body is null");
                throw new CommonException("devops.retrofit.execute.response.body.is.empty");
            }
            bodyStr = execute.body().string();
        } catch (IOException e) {
            logger.info("::Retrofit::An exception occurred during execution:{}", e);
            throw new CommonException("devops.retrofit.execute", e);
        }
        return bodyStr;
    }

    /**
     * 解析响应是否是CommonException
     */
    private static void parseCommonException(String responseStr, String exceptionMessage) {
        ExceptionResponse e = gson.fromJson(responseStr, ExceptionResponse.class);
        if (e.getFailed()) {
            logger.info("::Retrofit::The response is CommonException，code:{},message:{}", e.getCode(), e.getMessage());
        }
    }
}
