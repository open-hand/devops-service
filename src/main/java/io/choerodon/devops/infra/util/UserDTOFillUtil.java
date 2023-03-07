package io.choerodon.devops.infra.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;

/**
 * 〈功能简述〉
 * 〈填充用户DTO工具类〉
 *
 * @author wanghao
 * @Date 2021/7/3 14:37
 */
public class UserDTOFillUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDTOFillUtil.class);

    private UserDTOFillUtil() {
    }

    /**
     * 填充IamUserDTO
     * @param sourceList 源集合
     * @param filed userId字段
     * @param destFiled 目标DTO字段
     */
    public static void fillUserInfo(List<?> sourceList, String filed, String destFiled) {
        if (CollectionUtils.isEmpty(sourceList)) {
            return;
        }
        List<Long> userIds = sourceList.stream().map(v -> {
            Class<?> aClass = v.getClass();
            try {
                Field userIdFiled = getFiled(aClass, filed);
                userIdFiled.setAccessible(true);
                return (Long) userIdFiled.get(v);
            } catch (Exception e) {
                LOGGER.info("read user id failed", e.fillInStackTrace());
            }
            return null;
        }).collect(Collectors.toList());

        List<IamUserDTO> iamUserDTOS = ApplicationContextHelper.getContext().getBean(BaseServiceClientOperator.class).listUsersByIds(userIds);
        if(iamUserDTOS != null) {
            Map<Long, IamUserDTO> iamUserDTOMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, Function.identity()));
            sourceList.forEach(source -> {
                Class<?> aClass = source.getClass();
                try {
                    Field userIdFiled = getFiled(aClass, filed);
                    userIdFiled.setAccessible(true);
                    Long userId = (Long) userIdFiled.get(source);

                    IamUserDTO iamUserDTO = iamUserDTOMap.get(userId);

                    Field userDTOFiled = getFiled(aClass, destFiled);
                    userDTOFiled.setAccessible(true);
                    userDTOFiled.set(source, iamUserDTO);
                } catch (Exception e) {
                    LOGGER.info("fill user id failed", e.fillInStackTrace());
                }
            });
        }
    }

    private static Field getFiled(Class<?> aClass, String filed) throws NoSuchFieldException {

        Field[] declaredFields = aClass.getDeclaredFields();

        for (Field declaredField : declaredFields) {
            if (declaredField.getName().equals(filed)) {
                return declaredField;
            }
        }

        if (aClass.getSuperclass() != null) {
            return getFiled(aClass.getSuperclass(), filed);
        }

        throw new NoSuchFieldException(filed);

    }
}
