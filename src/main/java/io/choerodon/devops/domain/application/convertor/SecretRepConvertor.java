package io.choerodon.devops.domain.application.convertor;

import java.util.HashMap;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.SecretRepDTO;
import io.choerodon.devops.domain.application.entity.DevopsSecretE;
import io.choerodon.devops.infra.common.util.Base64Util;
import io.choerodon.devops.infra.dataobject.DevopsSecretDO;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午10:18
 * Description:
 */
@Component
public class SecretRepConvertor implements ConvertorI<DevopsSecretE, DevopsSecretDO, SecretRepDTO> {

    private static final Gson gson = new Gson();

    @Override
    public SecretRepDTO doToDto(DevopsSecretDO dataObject) {
        SecretRepDTO secretRepDTO = new SecretRepDTO();
        BeanUtils.copyProperties(dataObject, secretRepDTO);
        Map<String, String> secretMaps = gson
                .fromJson(dataObject.getSecretMaps(), new TypeToken<Map<String, String>>() {
                }.getType());
        secretRepDTO.setSecretMaps(secretMaps);
        return secretRepDTO;
    }

    @Override
    public SecretRepDTO entityToDto(DevopsSecretE entity) {
        SecretRepDTO secretRepDTO = new SecretRepDTO();
        BeanUtils.copyProperties(entity, secretRepDTO);
        Map<String, String> secretMaps = new HashMap<>();
        for (Map.Entry<String, String> e : secretRepDTO.getSecretMaps().entrySet()) {
            secretMaps.put(e.getKey(), Base64Util.getBase64DecodedString(e.getValue()));
        }
        secretRepDTO.setSecretMaps(secretMaps);
        return secretRepDTO;
    }
}
