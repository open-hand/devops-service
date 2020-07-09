package io.choerodon.devops.infra.util;

import static io.choerodon.devops.infra.constant.GitOpsConstants.COMMA;
import static org.hzero.core.util.StringPool.EMPTY;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.hzero.starter.keyencrypt.core.IEncryptionService;
import org.springframework.util.Assert;

import io.choerodon.core.convertor.ApplicationContextHelper;

/**
 * @author zmf
 * @since 2020/7/9
 */
public final class KeyDecryptHelper {
    private static final Gson GSON = new Gson();

    private KeyDecryptHelper() {
    }

    /**
     * 用于解密逗号分隔的加密id (用于加密时未提供key的解密过程)
     *
     * @param commaSeparatedEncryptedIds 逗号分隔的加密id
     * @return 逗号分隔的原值id
     */
    @Nullable
    public static String decryptCommaSeparatedIds(@Nullable String commaSeparatedEncryptedIds) {
        if (commaSeparatedEncryptedIds == null) {
            return null;
        }
        IEncryptionService iEncryptionService = ApplicationContextHelper.getContext().getBean(IEncryptionService.class);
        String[] encryptedIds = commaSeparatedEncryptedIds.split(COMMA);
        for (int i = 0; i < encryptedIds.length; i++) {
            encryptedIds[i] = iEncryptionService.decrypt(encryptedIds[i], EMPTY);
        }
        return Joiner.on(COMMA).join(encryptedIds);
    }

    /**
     * 用于解密 加密id json 数组 (用于加密时未提供key的解密过程)
     *
     * @param idJsonArray 加密id json 数组
     * @return 原值id 数组 json
     */
    @Nullable
    public static String decryptJsonIds(@Nullable String idJsonArray) {
        if (idJsonArray == null) {
            return null;
        }
        List<String> idStringList = GSON.fromJson(idJsonArray, new TypeToken<List<String>>() {
        }.getType());
        IEncryptionService iEncryptionService = ApplicationContextHelper.getContext().getBean(IEncryptionService.class);
        List<Long> result = new ArrayList<>(idStringList.size());
        for (String s : idStringList) {
            result.add(Long.valueOf(iEncryptionService.decrypt(s, EMPTY)));
        }
        return GSON.toJson(result);
    }

    /**
     * 解密加密id字符串的数组为Long数组
     *
     * @param ids 字符串数组
     * @return Long数据
     */
    @Nullable
    public static Long[] decryptIdArray(@Nullable String[] ids) {
        if (ids == null || ids.length == 0) {
            return null;
        }
        IEncryptionService iEncryptionService = ApplicationContextHelper.getContext().getBean(IEncryptionService.class);
        Long[] result = new Long[ids.length];
        for (int i = 0; i < ids.length; i++) {
            result[i] = Long.parseLong(iEncryptionService.decrypt(ids[i], EMPTY));
        }
        return result;
    }
}
