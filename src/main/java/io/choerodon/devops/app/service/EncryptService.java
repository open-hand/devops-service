package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/1/26
 * @Modified By:
 */
public interface EncryptService {

    Map<String, String> encryptIds(List<String> ids);
}
