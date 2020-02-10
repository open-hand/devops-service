import { DataSet } from 'choerodon-ui/pro';

function handleUpdate({ record, name }) {
  if (name !== 'protocol') {
    checkOtherRecords(record, name);
  }
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

export default ({ formatMessage }) => {
  const protocolDs = new DataSet({
    data: [
      {
        value: 'TCP',
      },
      {
        value: 'UDP',
      },
    ],
    selection: 'single',
  });

  /**
   * 验证端口号
   * @param value
   * @param type
   * @param record
   */
  function checkPort(value, type, record) {
    const p = /^([1-9]\d*|0)$/;
    const isRepeat = record.dataSet.some((r) => record.id !== r.id && r.get(type) === value);

    const data = {
      typeMsg: '',
      min: 0,
      max: 65535,
      failedMsg: 'network.port.check.failed',
    };

    switch (type) {
      case 'targetPort':
        data.typeMsg = 'network.tport.check.repeat';
        break;
      case 'nodePort':
        data.typeMsg = 'network.nport.check.repeat';
        data.min = 30000;
        data.max = 32767;
        data.failedMsg = 'network.nport.check.failed';
        break;
      default:
        data.typeMsg = 'network.port.check.repeat';
    }
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

  function isRequired({ dataSet, record }) {
    const name = record.cascadeParent.get('name');
    return dataSet.dirty || !!name;
  }

  return {
    autoCreate: true,
    fields: [
      {
        name: 'nodePort',
        type: 'string', 
        label: formatMessage({ id: 'network.config.nodePort' }),
        validator: checkPort,
        maxLength: 5,
      },
      {
        name: 'port',
        type: 'string', 
        label: formatMessage({ id: 'network.config.port' }),
        validator: checkPort,
        dynamicProps: {
          required: isRequired,
        },
        maxLength: 5,
      },
      {
        name: 'targetPort',
        type: 'string',
        label: formatMessage({ id: 'network.config.targetPort' }),
        required: true,
        validator: checkPort,
        dynamicProps: {
          required: isRequired,
        },
        maxLength: 5,
      },
      {
        name: 'protocol',
        type: 'string', 
        label: formatMessage({ id: 'ist.deploy.ports.protocol' }),
        dynamicProps: {
          required: ({ dataSet, record }) => isRequired({ dataSet, record }) && record.cascadeParent.get('type') === 'NodePort',
        },
        options: protocolDs,
      },
    ],
    events: {
      update: handleUpdate,
    },
  };
};
