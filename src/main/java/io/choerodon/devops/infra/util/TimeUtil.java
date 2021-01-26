package io.choerodon.devops.infra.util;

import java.util.Date;

public class TimeUtil {
    /**
     * 获取两个时间之差
     *
     * @param time1 时间戳 秒
     * @param time2 时间戳 秒
     * @return
     */
    public static Long[] getStageTime(Date time1, Date time2) {
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        long diff;
        long ttime1 = time1.getTime();
        long ttime2 = time2.getTime();
        if (ttime1 < ttime2) {
            diff = ttime2 - ttime1;
        } else {
            diff = ttime1 - ttime2;
        }
        day = diff / (24 * 60 * 60 * 1000);
        hour = (diff / (60 * 60 * 1000) - day * 24);
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        return new Long[]{day, hour, min, sec};
    }

    public static String getStageTimeInStr(Date time1, Date time2) {
        Long[] stageTime = getStageTime(time1, time2);
        StringBuilder sb = new StringBuilder();
        if (stageTime[0] != 0) {
            sb.append(stageTime[0]).append("天");
        }
        if (stageTime[1] != 0) {
            sb.append(stageTime[1]).append("小时");
        }
        if (stageTime[2] != 0) {
            sb.append(stageTime[2]).append("分");
        }
        if (stageTime[3] != 0) {
            sb.append(stageTime[3]).append("秒");
        }
        return sb.toString();
    }
}
