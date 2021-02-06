package io.choerodon.devops.infra.util;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.BaseDomain;
import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;
import io.choerodon.devops.api.vo.DevopsCdPipelineRecordVO;
import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.enums.PipelineStatus;

public class CiCdPipelineUtils {
    private static final Integer VIEWID_DIGIT = 6;

    private static final Pattern JOB_NAME_REGEX_PATTERN = Pattern.compile("(.*) \\d/\\d");

    public static <T extends BaseDomain> void recordListSort(List<T> list) {
        Collections.sort(list, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    if (o1.getCreatedDate().getTime() > o2.getCreatedDate().getTime()) {
                        return -1;
                    } else if (o1.getCreatedDate().getTime() < o2.getCreatedDate().getTime()) {
                        return 1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }


    public static void calculateStatus(CiCdPipelineRecordVO ciCdPipelineRecordVO, DevopsCiPipelineRecordVO devopsCiPipelineRecordVO, DevopsCdPipelineRecordVO devopsCdPipelineRecordVO) {
        if (!Objects.isNull(devopsCiPipelineRecordVO) && !Objects.isNull(devopsCdPipelineRecordVO)) {
            //计算记录的状态
            if (!PipelineStatus.SUCCESS.toValue().equals(devopsCiPipelineRecordVO.getStatus())) {
                ciCdPipelineRecordVO.setStatus(devopsCiPipelineRecordVO.getStatus());
                return;
            }
            if (PipelineStatus.SUCCESS.toValue().equals(devopsCiPipelineRecordVO.getStatus())
                    && PipelineStatus.SUCCESS.toValue().equals(devopsCdPipelineRecordVO.getStatus())) {
                ciCdPipelineRecordVO.setStatus(PipelineStatus.SUCCESS.toValue());
                return;
            }
            //如果ci状态成功cd是未执行，则状态为执行中
            if (PipelineStatus.SUCCESS.toValue().equals(devopsCiPipelineRecordVO.getStatus()) &&
                    PipelineStatus.CREATED.toValue().equals(devopsCdPipelineRecordVO.getStatus())) {
                ciCdPipelineRecordVO.setStatus(PipelineStatus.RUNNING.toValue());
                return;
            } else {
                ciCdPipelineRecordVO.setStatus(devopsCdPipelineRecordVO.getStatus());
            }
        }
        if (!Objects.isNull(devopsCiPipelineRecordVO) && Objects.isNull(devopsCdPipelineRecordVO)) {
            ciCdPipelineRecordVO.setStatus(devopsCiPipelineRecordVO.getStatus());
        }
        if (Objects.isNull(devopsCiPipelineRecordVO) && !Objects.isNull(devopsCdPipelineRecordVO)) {
            ciCdPipelineRecordVO.setStatus(devopsCdPipelineRecordVO.getStatus());
        }
    }

    public static void fillViewId(List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS) {
        if (CollectionUtils.isEmpty(ciCdPipelineRecordVOS)) {
            return;
        }
        ciCdPipelineRecordVOS.forEach(ciCdPipelineRecordVO -> {
            String handleId = handleId(ciCdPipelineRecordVO.getDevopsPipelineRecordRelId());
            ciCdPipelineRecordVO.setViewId(handleId);
        });
    }

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
