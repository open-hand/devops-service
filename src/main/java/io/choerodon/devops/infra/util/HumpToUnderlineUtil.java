package io.choerodon.devops.infra.util;

import org.springframework.util.StringUtils;

/**
 * @author: 25499
 * @date: 2019/9/4 9:35
 * @description:
 */
public class HumpToUnderlineUtil {
    public static String toUnderLine(String str) {
        if(str==null||str.isEmpty()){
            return null;
        }
        StringBuffer newString = new StringBuffer();
        String[] split = str.split("\\s+");
        String splitString = split[0];
        for (int i = 0; i < splitString.length(); i++) {
            if (splitString.charAt(i) >= 'A' && splitString.charAt(i) <= 'Z') {
                char a = Character.toLowerCase(splitString.charAt(i));
                String s = "_" + String.valueOf(a);
                newString.append(s);
            } else {
                String s = String.valueOf(splitString.charAt(i));
                newString.append(s);
            }
        }
        newString.append(" ").append(split[1]);
        return String.valueOf(newString);
    }

}
