import { map } from 'lodash';

export default ({ formatMessage }) => {
  /**
   * 验证端口号
   * @param value
   * @param type
   * @param record
   */
  function checkPort(value, type, record) {
    const p = /^([1-9]\d*|0)$/;
    const isRepeat = record.dataSet.findIndex((r) => record.id !== r.id && r.get(type) === value) !== -1;

    const data = {
      typeMsg: '',
      min: 0,
      max: 65535,
      failedMsg: 'network.port.check.failed',
    };

    data.typeMsg = 'network.tport.check.repeat';

    if (value) {
      if (
        p.test(value)
        && parseInt(value, 10) >= data.min
        && parseInt(value, 10) <= data.max
      ) {
        if (!isRepeat) {
          return true;
        } else {
          return formatMessage({ id: data.typeMsg });
        }
      } else {
        return formatMessage({ id: data.failedMsg });
      }
    } 
  }


  return {
    fields: [
      {
        name: 'targetPort',
        type: 'string', 
        label: formatMessage({ id: 'network.config.targetPort' }),
        dynamicProps: {
          required: ({ dataSet, record, name }) => {
            if (!dataSet.parent.current) return false;
            return dataSet.parent.current.get('target') === 'endPoints';
          },
        },
        validator: checkPort,
      },
    ],
    events: {
      update: updateEventHandler,
    },
  };
};

function updateEventHandler({ dataSet, record, name, value, oldValue }) {
  if (!value) return;
  checkOtherRecords(record, name);
}

function checkOtherRecords(record, type) {
  record.dataSet.forEach((r) => {
    if (r.id !== record.id) {
      // 此处只对重复性做校验，不对空值做校验
      if (r.get(type)) {
        r.getField(type).checkValidity();
      }
    }
  });
}
