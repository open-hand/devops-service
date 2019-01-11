package io.choerodon.devops

import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

/**
 * 解决导出应用返回二进制流的http响应转换问题
 * Created by n!Ck
 * Date: 18-11-30
 * Time: 上午10:01
 * Description:
 */
class ExportOctetStream2HttpMessageConverter extends MappingJackson2HttpMessageConverter {
    ExportOctetStream2HttpMessageConverter() {
        List<MediaType> mediaTypes = new ArrayList<>()
        mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM)
        setSupportedMediaTypes(mediaTypes)
    }
}
