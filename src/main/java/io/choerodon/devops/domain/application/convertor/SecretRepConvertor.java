package io.choerodon.devops.domain.application.convertor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class SecretRepConvertor implements ConvertorI<DevopsSecretE, Object, SecretRepDTO> {

    private static final Gson gson = new Gson();

//    @Override
//    public SecretRepDTO doToDto(DevopsSecretDO dataObject) {
//        SecretRepDTO secretRepDTO = new SecretRepDTO();
//        BeanUtils.copyProperties(dataObject, secretRepDTO);
//        Map<String, String> secretMaps = gson
//                .fromJson(dataObject.getValue(), new TypeToken<Map<String, String>>() {
//                }.getType());
//        List<String> key = new ArrayList<>();
//        secretMaps.forEach((key1, value) -> key.add(key1));
//        secretRepDTO.setKey(key);
//        secretRepDTO.setCommandStatus(dataObject.getStatus());
//        secretRepDTO.setLastUpdateDate(dataObject.getLastUpdateDate());
//        secretRepDTO.setValue(secretMaps);
//        return secretRepDTO;
//    }

    @Override
    public SecretRepDTO entityToDto(DevopsSecretE entity) {
        SecretRepDTO secretRepDTO = new SecretRepDTO();
        BeanUtils.copyProperties(entity, secretRepDTO);
        Map<String, String> secretMaps = new HashMap<>();
        for (Map.Entry<String, String> e : secretRepDTO.getValue().entrySet()) {
            secretMaps.put(e.getKey(), Base64Util.getBase64DecodedString(e.getValue()));
        }
        List<String> key = new ArrayList<>();
        secretMaps.forEach((key1, value) -> key.add(key1));
        secretRepDTO.setKey(key);
        secretRepDTO.setCommandStatus(entity.getStatus());
        secretRepDTO.setLastUpdateDate(entity.getLastUpdateDate());
        secretRepDTO.setValue(secretMaps);
        return secretRepDTO;
    }
}
