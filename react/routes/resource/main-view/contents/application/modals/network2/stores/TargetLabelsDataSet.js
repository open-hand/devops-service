import _ from 'lodash';
import { includes } from 'lodash/includes';

export default ({ formatMessage, keyOptionsDs }) => {
  /**
   * 关键字检查
   * @param rule
   * @param value
   * @param callback
   */
  function checkKeywords(value, name, record) {
    // 必须由字母数字字符，' - '，'_'或'.'组成，并且必须以字母数字开头和结尾
    // 并且包括可选的DNS子域前缀(包括一级、二级域名)和'/'（例如'example.com/MyName'）
    const p = /^((?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\/)*([A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9]$/;
    const isRepeat = record.dataSet.findIndex((r) => record.id !== r.id && r.get(name) === value) !== -1;
    if (value) {
      if (p.test(value)) {
        if (isRepeat) {
          return formatMessage({ id: 'network.label.check.repeat' });
        }
      } else {
        return formatMessage({ id: 'network.label.check.failed' });
      }
    }
  }

  function checkValue(value, name, record) {
    const p = /^(([A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9])?$/;
    if (value) {
      if (!p.test(value)) {
        return formatMessage({ id: 'network.label.check.failed' });
      }
    }
  }

  const dynamicProps = {
    required: ({ dataSet, record, name }) => {
      if (!dataSet.parent.current) return false;
      return dataSet.parent.current.get('target') === 'param';
    },
  };


  return {
    autoCreate: true,
    fields: [
      {
        name: 'keyword',
        type: 'string', 
        label: formatMessage({ id: 'network.config.keyword' }),
        options: keyOptionsDs,
        validator: checkKeywords,
        textField: 'key',
        valueField: 'meaning',
        dynamicProps,
      },
      {
        name: 'value',
        type: 'string', 
        label: formatMessage({ id: 'network.config.value' }),
        options: keyOptionsDs,
        validator: checkValue,
        textField: 'value',
        valueField: 'meaning',
        dynamicProps,
      },
    ],
    events: {
      update: ({ dataSet, record, name, value, oldValue }) => {
        if (!value || !value.includes(':')) return;
        const splitkv = value.split(':');
        record.set('keyword', splitkv[0]);
        record.set('value', splitkv[1]);
      },
    },
  };
};
