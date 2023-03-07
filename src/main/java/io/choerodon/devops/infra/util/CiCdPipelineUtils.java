package io.choerodon.devops.infra.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import io.choerodon.devops.infra.dto.DevopsCiJobDTO;

public class CiCdPipelineUtils {
    private static final Integer VIEWID_DIGIT = 6;

    private CiCdPipelineUtils() {

    }
    private static final Pattern JOB_NAME_REGEX_PATTERN = Pattern.compile("(.*) [1-9]\\d*/[1-9]\\d*");



    public static String handleId(Long id) {
        String relId = String.valueOf(id);
        if (relId.length() >= VIEWID_DIGIT) {
            return StringUtils.reverse(StringUtils.substring(StringUtils.reverse(relId), 0, VIEWID_DIGIT));
        }
        return relId;
    }

    /**
     * 判断jobMap中是否有指定名称的job(包含了parallel类型job情况)。存在返回job对象，不存在返回null
     */
    public static DevopsCiJobDTO judgeAndGetJob(String jobName, Map<String, DevopsCiJobDTO> jobDTOMap) {
        DevopsCiJobDTO devopsCiJobDTO = jobDTOMap.get(jobName);
        if (devopsCiJobDTO != null) {
            return devopsCiJobDTO;
        }
        Matcher matcher = JOB_NAME_REGEX_PATTERN.matcher(jobName);
        if (matcher.matches()) {
            jobName = matcher.group(1);
            return jobDTOMap.get(jobName);
        } else {
            return null;
        }
    }
}
