package io.choerodon.devops.infra.util;

import io.choerodon.asgard.common.ApplicationContextHelper;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 〈功能简述〉
 * 〈填充用户DTO工具类〉
 *
 * @author wanghao
 * @Date 2021/7/3 14:37
 */
public class UserDTOFillUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDTOFillUtil.class);

    /**
     * 填充IamUserDTO
     * @param sourceList 源集合
     * @param filed userId字段
     * @param destFiled 目标DTO字段
     */
    public static void fillUserInfo(List<?> sourceList, String filed, String destFiled) {
        List<Long> userIds = sourceList.stream().map(v -> {
            Class<?> aClass = v.getClass();
            try {
                Field declaredField = aClass.getDeclaredField(filed);
                declaredField.setAccessible(true);
                return (Long) declaredField.get(v);
            } catch (Exception e) {
                LOGGER.info("read user id failed", e.fillInStackTrace());
            }
            return null;
        }).collect(Collectors.toList());

        List<IamUserDTO> iamUserDTOS = ApplicationContextHelper.getBean(BaseServiceClientOperator.class).listUsersByIds(userIds);
        if(iamUserDTOS != null) {
            Map<Long, IamUserDTO> iamUserDTOMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, Function.identity()));
            sourceList.forEach(source -> {
                Class<?> aClass = source.getClass();
                try {
                    Field userIdFiled = aClass.getDeclaredField(filed);
                    userIdFiled.setAccessible(true);
                    Long userId = (Long) userIdFiled.get(source);

                    IamUserDTO iamUserDTO = iamUserDTOMap.get(userId);
                    Field userDTOFiled = aClass.getDeclaredField(destFiled);
                    userDTOFiled.setAccessible(true);
                    userDTOFiled.set(source, iamUserDTO);
                } catch (Exception e) {
                    LOGGER.info("fill user id failed", e.fillInStackTrace());
                }
            });
        }
    }
}
