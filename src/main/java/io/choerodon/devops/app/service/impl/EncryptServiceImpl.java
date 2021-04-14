package io.choerodon.devops.app.service.impl;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.hzero.starter.keyencrypt.core.EncryptContext;
import org.hzero.starter.keyencrypt.core.EncryptProperties;
import org.hzero.starter.keyencrypt.core.EncryptionService;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.EncryptService;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/1/26
 * @Modified By:
 */
@Service
public class EncryptServiceImpl implements EncryptService {
    public static final String BLANK_KEY = "";
    protected static EncryptionService encryptionService = new EncryptionService(new EncryptProperties());

    @Override
    public Map<String, String> encryptIds(List<String> ids) {
        Map<String, String> map = new HashMap<>();
        boolean isEncrypt = EncryptContext.isEncrypt();
        if (!CollectionUtils.isEmpty(ids)) {
            ids.forEach(t -> {
                if (!isEncrypt) {
                    map.put(t, t);
                } else {
                    map.put(t, encryptionService.encrypt(t, BLANK_KEY));
                }
            });
        }
        return map;
    }

    @Override
    public Set<Object> encryptIds(Set<Long> ids) {
        boolean isEncrypt = EncryptContext.isEncrypt();
        Set<Object> result = new HashSet<>();
        if (!CollectionUtils.isEmpty(ids)) {
            ids.forEach(i -> {
                if (!isEncrypt) {
                    result.add(i);
                } else {
                    result.add(encryptionService.encrypt(i.toString(), BLANK_KEY));
                }
            });
        }
        return result;
    }
}
