package io.choerodon.devops.infra.util;

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
     * 计算cron表达式
     *
     * @param commonScheduleVO
     * @return
     */
    public static String calculateCron(CommonScheduleVO commonScheduleVO) {
        String cronTemplate = "%s %s * * %s";
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


        String week = commonScheduleVO.getWeekNumber();
        return String.format(cronTemplate, minute, hour, week);
    }
}
