/**
 * @author ale0720@163.com
 * @date 2019-06-27 13:42
 */
import JsYaml from 'js-yaml';

const SexyYamlType = new JsYaml.Type('!sexy', {
  kind: 'sequence',
  construct(data) {
    return data.map((string) => {
      return `sexy ${string}`;
    });
  },
});
const SEXY_SCHEMA = JsYaml.Schema.create([SexyYamlType]);

export function convert(value) {
  return JsYaml.load(value, { schema: SEXY_SCHEMA });
}

/**
 * YAML 格式校验
 * @param values
 * @returns {Array}
 */
export function checkFormat(value) {
  const result = [];

  try {
    convert(value);
  } catch (e) {
    result.push(e);
  }

  return result;
}


/**
 * 非注释改动检测
 * @param old
 * @param value
 * @param callback
 * @returns {boolean}
 */
export function changedValue(old, value, callback) {
  let hasChanged = true;
  try {
    const oldValue = convert(old || '');
    const newValue = convert(value);
    // 转为对象格式，可以将注释去除
    if (JSON.stringify(oldValue) === JSON.stringify(newValue)) {
      hasChanged = false;
    }
  } catch (e) {
    callback(true);
    throw new Error(`格式错误：${e}`);
  }
  return hasChanged;
}
