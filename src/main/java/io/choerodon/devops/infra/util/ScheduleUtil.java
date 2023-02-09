package io.choerodon.devops.infra.util;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CommonScheduleVO;
import io.choerodon.devops.infra.enums.CiPipelineScheduleTriggerTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/29 16:35
 */
public class ScheduleUtil {
    private static final String DEVOPS_TRIGGER_TYPE_INVALID = "devops.trigger.type.invalid";
    private static final String DEVOPS_START_HOUR_OF_DAY_IS_NULL = "devops.start.hour.of.day.is.null";
    private static final String DEVOPS_END_HOUR_OF_DAY_IS_NULL = "devops.end.hour.of.day.is.null";
    private static final String DEVOPS_PERIOD_IS_NULL = "devops.period.is.null";
    private static final String DEVOPS_EXECUTE_TIME_IS_NULL = "devops.executeTime.is.null";

    public static Map<String, String> quartzWeekNumberMap = new HashedMap();

    static {
        quartzWeekNumberMap.put("1", "2");
        quartzWeekNumberMap.put("2", "3");
        quartzWeekNumberMap.put("3", "4");
        quartzWeekNumberMap.put("4", "5");
        quartzWeekNumberMap.put("5", "6");
        quartzWeekNumberMap.put("6", "7");
        quartzWeekNumberMap.put("7", "1");
    }

    public static void validate(CommonScheduleVO commonScheduleVO) {
        if (CiPipelineScheduleTriggerTypeEnum.PERIOD.value().equals(commonScheduleVO.getTriggerType())) {
            if (commonScheduleVO.getStartHourOfDay() == null) {
                throw new CommonException(DEVOPS_START_HOUR_OF_DAY_IS_NULL);
            }
            if (commonScheduleVO.getEndHourOfDay() == null) {
                throw new CommonException(DEVOPS_END_HOUR_OF_DAY_IS_NULL);
            }
            if (commonScheduleVO.getPeriod() == null) {
                throw new CommonException(DEVOPS_PERIOD_IS_NULL);
            }
        } else if (CiPipelineScheduleTriggerTypeEnum.SINGLE.value().equals(commonScheduleVO.getTriggerType())) {
            if (commonScheduleVO.getExecuteTime() == null) {
                throw new CommonException(DEVOPS_EXECUTE_TIME_IS_NULL);
            }
        } else {
            throw new CommonException(DEVOPS_TRIGGER_TYPE_INVALID);
        }
    }

    /**
     * 计算gitlab-ci cron表达式(gitlab-ci的cron没有秒只有5位)
     *
     * @param commonScheduleVO
     * @return
     */
    public static String calculateGitlabCiCron(CommonScheduleVO commonScheduleVO) {
        String cronTemplate = "%s %s * * %s";
        return calculateCron(cronTemplate, commonScheduleVO, commonScheduleVO.getWeekNumber());
    }

    public static String calculateCron(String cronTemplate, CommonScheduleVO commonScheduleVO, String weekNumber) {
        String minute = "";
        String hour = "";
        if (CiPipelineScheduleTriggerTypeEnum.PERIOD.value().equals(commonScheduleVO.getTriggerType())) {
            if (commonScheduleVO.getPeriod() >= 60) {
                minute = "0";
                hour = commonScheduleVO.getStartHourOfDay() + "-" + commonScheduleVO.getEndHourOfDay() + "/" + (commonScheduleVO.getPeriod() / 60);
            } else {
                minute = "0-59/" + commonScheduleVO.getPeriod();
                hour = commonScheduleVO.getStartHourOfDay() + "-" + commonScheduleVO.getEndHourOfDay();
            }
        } else if (CiPipelineScheduleTriggerTypeEnum.SINGLE.value().equals(commonScheduleVO.getTriggerType())) {
            String[] split = commonScheduleVO.getExecuteTime().split(":");
            minute = split[1];
            hour = split[0];
        } else {
            throw new CommonException(DEVOPS_TRIGGER_TYPE_INVALID);
        }


        return String.format(cronTemplate, minute, hour, weekNumber);
    }

    public static String calculateQuartzCron(CommonScheduleVO commonScheduleVO) {
        String cronTemplate = "0 %s %s ? * %s";
        // Quartz在设置周时星期一、星期二、星期三、星期四、星期五、星期六、星期日分别对应数字2、3、4、5、6、7、1或者对应英文的简写，而不是1、2、3、4、5、6、7
        String weekNumber;
        if (StringUtils.isBlank(commonScheduleVO.getWeekNumber())) {
            weekNumber = "*";
        } else {
            weekNumber = Arrays
                    .stream(commonScheduleVO.getWeekNumber().split(","))
                    .map(week -> quartzWeekNumberMap.get(week)).collect(Collectors.joining(","));
        }
        return calculateCron(cronTemplate, commonScheduleVO, weekNumber);
    }
}
