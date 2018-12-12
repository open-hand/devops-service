package io.choerodon.devops.infra.common.util;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.DateCodec;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * 处理格式为 '2018-12-10 13:23:40 UTC' 的时间反序列化，可用于fastjson
 * @author zmf
 */
public class FastJsonDateUTCDeserializer extends DateCodec {

    private FastJsonDateUTCDeserializer() {}

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        Pattern utc = Pattern.compile("UTC");
        String value = parser.getLexer().stringVal().trim();
        if (utc.matcher(value).find()) {
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format2.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                return (T) format2.parse(value);
            } catch (ParseException e) {
                return super.deserialze(parser, type, fieldName);
            }
        }
        return super.deserialze(parser, type, fieldName);
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }

    /**
     * 获取用于fastjson解析json的反序列化配置
     * 用法：<code>
     *     MyDate date = JSON.parseObject(jsonString, MyDate.class, parserConfig);
     * </code>
     * @return 配置
     */
    public static ParserConfig getParserConfig() {
        ParserConfig parserConfig = new ParserConfig();
        parserConfig.putDeserializer(Date.class, new FastJsonDateUTCDeserializer());
        return parserConfig;
    }
}