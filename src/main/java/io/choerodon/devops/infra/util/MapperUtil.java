package io.choerodon.devops.infra.util;

import java.util.Objects;
import javax.annotation.Nonnull;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 封装一些Mapper操作
 *
 * @author zmf
 * @since 12/11/19
 */
public class MapperUtil {
    private MapperUtil() {
    }

    /**
     * 对结果进行校验的 mapper.insert
     *
     * @param mapper              mapper
     * @param recordToInsert      待插入的纪录
     * @param commonExceptionCode 插入异常时的异常消息Code
     * @param messageParameters   渲染异常消息需要的参数
     * @param <T>                 纪录类型
     * @return 插入纪录
     */
    @Nonnull
    public static <T> T resultJudgedInsert(BaseMapper<T> mapper, T recordToInsert, String commonExceptionCode, Object... messageParameters) {
        if (mapper.insert(Objects.requireNonNull(recordToInsert)) != 1) {
            throw new CommonException(commonExceptionCode, messageParameters);
        }
        return recordToInsert;
    }

    /**
     * 对结果进行校验的 mapper.insertSelective
     *
     * @param mapper              mapper
     * @param recordToInsert      待插入的纪录
     * @param commonExceptionCode 插入异常时的异常消息Code
     * @param messageParameters   渲染异常消息需要的参数
     * @param <T>                 纪录类型
     * @return 插入纪录
     */
    @Nonnull
    public static <T> T resultJudgedInsertSelective(BaseMapper<T> mapper, T recordToInsert, String commonExceptionCode, Object... messageParameters) {
        if (mapper.insertSelective(Objects.requireNonNull(recordToInsert)) != 1) {
            throw new CommonException(commonExceptionCode, messageParameters);
        }
        return recordToInsert;
    }

    /**
     * 对结果进行校验的 mapper.updateByPrimaryKey
     *
     * @param mapper              mapper
     * @param recordToUpdate      待更新的纪录
     * @param commonExceptionCode 更新异常时的异常消息Code
     * @param messageParameters   渲染异常消息需要的参数
     * @param <T>                 纪录类型
     */
    public static <T> void resultJudgedUpdateByPrimaryKey(BaseMapper<T> mapper, T recordToUpdate, String commonExceptionCode, Object... messageParameters) {
        if (mapper.updateByPrimaryKey(Objects.requireNonNull(recordToUpdate)) != 1) {
            throw new CommonException(commonExceptionCode, messageParameters);
        }
    }

    /**
     * 对结果进行校验的 mapper.updateByPrimaryKey
     *
     * @param mapper              mapper
     * @param recordToUpdate      待更新的纪录
     * @param commonExceptionCode 更新异常时的异常消息Code
     * @param messageParameters   渲染异常消息需要的参数
     * @param <T>                 纪录类型
     */
    public static <T> void resultJudgedUpdateByPrimaryKeySelective(BaseMapper<T> mapper, T recordToUpdate, String commonExceptionCode, Object... messageParameters) {
        if (mapper.updateByPrimaryKeySelective(Objects.requireNonNull(recordToUpdate)) != 1) {
            throw new CommonException(commonExceptionCode, messageParameters);
        }
    }
}
