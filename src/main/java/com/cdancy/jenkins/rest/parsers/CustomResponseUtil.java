package com.cdancy.jenkins.rest.parsers;

import com.cdancy.jenkins.rest.domain.common.Response;
import com.fasterxml.jackson.core.type.TypeReference;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/8 9:36
 */
public class CustomResponseUtil {

    public static <T> T parse(Response response, Class<T> responseType) {
        if (response.statusCode() >= 200 && response.statusCode() < 400) {
            return JsonHelper.unmarshalByJackson(response.value(), responseType);
        } else {
            throw new CommonException(response.error());
        }
    }

    public static <T> T parse(Response response, TypeReference<T> typeReference) {
        if (response.statusCode() >= 200 && response.statusCode() < 400) {
            return JsonHelper.unmarshalByJackson(response.value(), typeReference);
        } else {
            throw new CommonException(response.error());
        }
    }

}
