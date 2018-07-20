package io.choerodon.devops.infra.common.util;

import java.util.Date;
import java.util.TimeZone;

/**
 * Creator: Runge
 * Date: 2018/7/12
 * Time: 17:51
 * Description:
 */
public class DateUtil {

    /**
     * 获取更改时区后的日期
     *
     * @param date    日期
     * @param oldZone 旧时区对象
     * @param newZone 新时区对象
     * @return 日期
     */
    public static Date changeTimeZone(Date date, TimeZone oldZone, TimeZone newZone) {
        Date dateTmp = null;
        if (date != null) {
            int timeOffset = oldZone.getRawOffset() - newZone.getRawOffset();
            dateTmp = new Date(date.getTime() - timeOffset);
        }
        return dateTmp;
    }
}
