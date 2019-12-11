package io.choerodon.devops.infra.util;

import java.util.Objects;

import javax.annotation.Nonnull;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.common.Mapper;

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
     * @param <T>                 纪录类型
     * @return 插入纪录
     */
    @Nonnull
    public static <T> T resultJudgedInsert(Mapper<T> mapper, T recordToInsert, String commonExceptionCode) {
        if (mapper.insert(Objects.requireNonNull(recordToInsert)) != 1) {
            throw new CommonException(commonExceptionCode);
        }
        return recordToInsert;
    }

    /**
     * 对结果进行校验的 mapper.insertSelective
     *
     * @param mapper              mapper
     * @param recordToInsert      待插入的纪录
     * @param commonExceptionCode 插入异常时的异常消息Code
     * @param <T>                 纪录类型
     * @return 插入纪录
     */
    @Nonnull
    public static <T> T resultJudgedInsertSelective(Mapper<T> mapper, T recordToInsert, String commonExceptionCode) {
        if (mapper.insertSelective(Objects.requireNonNull(recordToInsert)) != 1) {
            throw new CommonException(commonExceptionCode);
        }
        return recordToInsert;
    }

    /**
     * 对结果进行校验的 mapper.updateByPrimaryKey
     *
     * @param mapper              mapper
     * @param recordToUpdate      待更新的纪录
     * @param commonExceptionCode 更新异常时的异常消息Code
     * @param <T>                 纪录类型
     */
    public static <T> void resultJudgedUpdateByPrimaryKey(Mapper<T> mapper, T recordToUpdate, String commonExceptionCode) {
        if (mapper.updateByPrimaryKey(Objects.requireNonNull(recordToUpdate)) != 1) {
            throw new CommonException(commonExceptionCode);
        }
    }

    /**
     * 对结果进行校验的 mapper.updateByPrimaryKey
     *
     * @param mapper              mapper
     * @param recordToUpdate      待更新的纪录
     * @param commonExceptionCode 更新异常时的异常消息Code
     * @param <T>                 纪录类型
     */
    public static <T> void resultJudgedUpdateByPrimaryKeySelective(Mapper<T> mapper, T recordToUpdate, String commonExceptionCode) {
        if (mapper.updateByPrimaryKeySelective(Objects.requireNonNull(recordToUpdate)) != 1) {
            throw new CommonException(commonExceptionCode);
        }
    }
}
