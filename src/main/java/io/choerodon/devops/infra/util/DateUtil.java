package io.choerodon.devops.infra.util;

import java.util.Calendar;
import java.util.Date;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/9/3
 * @Modified By:
 */
public class DateUtil {

    /**
     * 加减日期
     *
     * @param date
     * @param day
     * @return
     */
    public static Date subOrAddDay(Date date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, day);
        return calendar.getTime();
    }
}
