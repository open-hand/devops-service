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
 * 提供用于fastjson解析json的反序列化配置（目前用于补充解析UTC格式的时间）
 *
 * @author zmf
 */
public class FastjsonParserConfigProvider {
    private static final ParserConfig PARSER_CONFIG;

    static {
        PARSER_CONFIG = new ParserConfig();
        PARSER_CONFIG.putDeserializer(Date.class, FastJsonDateUTCDeserializer.getInstance());
    }

    private FastjsonParserConfigProvider() {
    }

    /**
     * 获取用于fastjson解析json的反序列化配置，获取的实例请谨慎进行改变，改变将会保存，
     * 影响之后的获取实例，否则修改此方法实现为每次返回新实例。
     * 用法：<code>
     * MyDate date = JSON.parseObject(jsonString, MyDate.class, FastjsonParserConfigProvider.getParserConfig());
     * </code>
     *
     * @return 配置
     */
    public static ParserConfig getParserConfig() {
        return PARSER_CONFIG;
    }


    /**
     * 处理格式为 '2018-12-10 13:23:40 UTC' 的时间反序列化，可用于fastjson
     *
     * @author zmf
     */
    private static class FastJsonDateUTCDeserializer extends DateCodec {
        private static final FastJsonDateUTCDeserializer FAST_JSON_DATE_UTC_DESERIALIZER = new FastJsonDateUTCDeserializer();
        private Pattern utc = Pattern.compile("UTC");

        private FastJsonDateUTCDeserializer() {
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
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

        public static FastJsonDateUTCDeserializer getInstance() {
            return FAST_JSON_DATE_UTC_DESERIALIZER;
        }
    }
}
