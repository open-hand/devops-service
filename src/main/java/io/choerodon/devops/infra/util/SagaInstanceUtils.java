package io.choerodon.devops.infra.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.SagaInstanceDetails;

/**
 * Created by wangxiang on 2020/9/24
 */
public class SagaInstanceUtils {

    private static final String FAILED = "FAILED";

    public static Long fillInstanceId(Map<String, SagaInstanceDetails> map, String refId) {
        if (!MapUtils.isEmpty(map) && !Objects.isNull(map.get(refId)) && FAILED.equalsIgnoreCase(map.get(refId).getStatus().trim())) {
            return map.get(refId).getId();
        } else {
            return 0L;
        }
    }

    public static Map<String, SagaInstanceDetails> listToMap(List<SagaInstanceDetails> sagaInstanceDetails) {
        Map<String, SagaInstanceDetails> sagaInstanceDetailsMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(sagaInstanceDetails)) {
            sagaInstanceDetailsMap = sagaInstanceDetails.stream().collect(Collectors.toMap(SagaInstanceDetails::getRefId, Function.identity()));
        }
        return sagaInstanceDetailsMap;
    }
}
