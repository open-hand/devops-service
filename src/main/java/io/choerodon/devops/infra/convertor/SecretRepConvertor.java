package io.choerodon.devops.infra.convertor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.SecretRepDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsSecretE;
import io.choerodon.devops.infra.util.Base64Util;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

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
            if (!e.getKey().equals(".dockerconfigjson")) {
                secretMaps.put(e.getKey(), Base64Util.getBase64DecodedString(e.getValue()));
            }else {
                secretMaps.put(e.getKey(),e.getValue());
            }
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
