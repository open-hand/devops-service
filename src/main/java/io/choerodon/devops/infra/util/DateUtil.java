package io.choerodon.devops.infra.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(calendar.getTime());
        ParsePosition pos = new ParsePosition(8);
        return formatter.parse(dateString, pos);
    }
}
