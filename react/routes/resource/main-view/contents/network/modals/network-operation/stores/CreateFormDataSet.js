import { map, forOwn, isEmpty, forEach } from 'lodash';
import uuidv1 from 'uuid/v1';

export default ({ formatMessage, portDs, endPointsDs, targetLabelsDs, appInstanceOptionsDs, networkStore, projectId, envId, networkEdit }) => {
  const { networkInfoDs, networkId, initTargetLabel, initPorts, initEndPoints } = networkEdit;

  /**
  * 检查名字的唯一性
  * @param value
  * @param name
  * @param record
  */
  async function checkName(value, name, record) {
    if (networkId) return;

    const pattern = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && !pattern.test(value)) {
      return formatMessage({ id: 'network.name.check.failed' });
    } else if (value && pattern.test(value)) {
      const res = await networkStore.checkNetWorkName(projectId, envId, value);
      if (!res) {
        return formatMessage({ id: 'network.name.check.exist' });
      }
    }
  }

  /**
   * 验证ip
   * @param value
   * @param name
   * @param record
   */
  function checkIP(value, name, record) {
    const p = /^((\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])\.){3}(\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])$/;
    let errorMsg;
    if (value) {
      if (!p.test(value)) {
        errorMsg = formatMessage({ id: 'network.ip.check.failed' });
      }
      return errorMsg;
    }
  }

  function checkInstance(value, name, record) {
    if (!networkId) return;
    let msg;
    if (value) {
      const data = value.split(',');
      forEach(data, (item) => {
        const instance = appInstanceOptionsDs.find((r) => r.get('code') === item);
        const status = instance.get('status');
        if (instance && status && status !== 'running' && !msg) {
          msg = formatMessage({ id: 'network.instance.check.failed' });
        }
      });
      if (data[1] && !msg) {
        msg = formatMessage({ id: 'network.instance.check.failed.more' });
      }
    }
    if (msg) {
      return msg;
    }
  }

  return {
    autoCreate: true,
    children: {
      portDs,
      endPointsDs,
      targetLabelsDs,
    },
    fields: [
      {
        name: 'name',
        type: 'string',
        label: formatMessage({ id: 'network.form.name' }),
        required: true,
        validator: checkName,
      },
      {
        name: 'appServiceId',
        type: 'string',
        label: formatMessage({ id: 'network.form.app' }),
        required: true,
        valueField: 'id',
        textField: 'name',
        lookupAxiosConfig: {
          method: 'get',
          url: `devops/v1/projects/${projectId}/app_service/list_all`,
        },
        dynamicProps: {
          required: ({ dataSet, record, name }) => record.get('target') === 'instance',
        },
      },
      {
        name: 'target',
        type: 'string',
        defaultValue: 'instance',
      },
      {
        name: 'type',
        type: 'string',
        defaultValue: 'ClusterIP',
      },
      {
        name: 'appInstance',
        type: 'string',
        label: formatMessage({ id: 'network.target.instance' }),
        required: true,
        options: appInstanceOptionsDs,
        textField: 'code',
        valueField: 'code',
        validator: checkInstance,
        dynamicProps: {
          required: ({ dataSet, record, name }) => record.get('target') === 'instance',
        },
      },
      {
        name: 'externalIps',
        label: formatMessage({ id: 'network.config.ip' }),
        multiple: true,
        validator: checkIP,
      },
      {
        name: 'targetIps',
        label: formatMessage({ id: 'network.target.ip' }),
        multiple: true,
        validator: checkIP,
        dynamicProps: {
          required: ({ dataSet, record, name }) => record.get('target') === 'endPoints',
        },
      },
    ],
    transport: {
      create: ({ data: [data] }) => {
        const postData = transFormData(data, formatMessage, envId);
        return {
          method: 'post',
          url: `/devops/v1/projects/${projectId}/service`,
          data: postData,
        };
      },
    },
    events: {
      update({ dataSet, record, name, value, oldValue }) {
        switch (name) {
          case 'target':
            networkId
              ? record.init('instance', initTargetLabel({ targetLabelsDs, type: value, record, networkInfoDs, formatMessage }))
              && record.init('targetIps', initEndPoints({ endPointsDs, targetLabelsDs, record: dataSet.current, networkInfoDs }))
              : handleTargetChange({ targetLabelsDs, endPointsDs, value, record, dataSet });
            break;
          case 'type':
            networkId
              ? record.init('externalIps', initPorts({ portDs, type: value, networkInfoDs }))
              : handleTypeChange({ portDs, value, record });
            break;
          case 'appServiceId':
            !networkId && handleAppServiceIdChange({ dataSet, record, name, value });
            if (value) {
              record.get('appInstance') && record.set('appInstance', null);
              appInstanceOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service_instances/list_running_instance?env_id=${envId}&app_service_id=${value}`;
              appInstanceOptionsDs.query();
            }
            break;
          default:
            break;
        }
      },
    },
  };
};

function handleTargetChange({ targetLabelsDs, endPointsDs, value, record }) {
  if (value !== 'instance') {
    record.set('appInstance', null);
    record.set('appServiceId', null);
  }
  if (value !== 'param') {
    targetLabelsDs.reset();
    targetLabelsDs.create();
  }
  if (value !== 'endPoints') {
    record.set('targetIps', null);
    endPointsDs.reset();
    endPointsDs.create();
  }
}

function handleTypeChange({ portDs, value, record }) {
  portDs.reset();
  portDs.create();
  if (value !== 'ClusterIP') {
    record.set('externalIps', null);
  }
}

function handleAppServiceIdChange({ dataSet, record, name, value }) {
  if (!value) return;
  const opt = dataSet.getField(name).getLookupData(value);
  if (opt) {
    const networkName = createNetworkName(opt);
    record.set('name', networkName);
  }
}

/**
 * 生成网络名
 * @param opt
 * @returns {string}
 */
function createNetworkName(opt) {
  let initName = opt.code;
  if (initName.length > 23) {
    // 初始网络名长度限制
    initName = initName.slice(0, 23);
  }
  initName = `${initName}-${uuidv1().slice(0, 6)}`;
  return initName;
}

export function transFormData(data, formatMessage, envId) {
  const { portDs: portData, endPointsDs: endPointsData, targetLabelsDs: targetLabelsData, target, appInstance, name, type, externalIps, targetIps, appServiceId } = data;

  // NOTE: 转换port的数据，过滤掉不用的数据
  const ports = map(portData, (value) => ({
    port: value.port,
    targetPort: value.targetPort,
    nodePort: value.nodePort,
    protocol: value.protocol,
  }));

  let targetAppServiceId;
  let targetInstanceCode;
  let selectors = null;
  let endPoints = null;
  // 目标对象是实例还是选择器
  if (target === 'instance') {
    /**
     * NOTE: 处理所有实例和单个实例的问题
     * 所有实例直接与AppService关联所以此处赋值给targetAppServiceId
     * 单个实例直接与AppInstnace关联所以此处赋值给targetInstanceCode
     */
    if (appInstance === formatMessage({ id: 'all_instance' })) {
      targetAppServiceId = appServiceId;
    } else {
      targetInstanceCode = appInstance;
    }
  } else if (target === 'param') {
    // NOTE: 处理selectors,将targetLabels的数组转换成key，value的对象
    selectors = {};
    forOwn(targetLabelsData, (item, key) => {
      if (item && item.keyword && item.value) {
        selectors[item.keyword] = item.value;
      }
    });
    if (isEmpty(selectors)) {
      selectors = null;
    }
  } else if (targetIps) {
    const ips = targetIps.join(',');
    const ipsPort = map(endPointsData, (item) => ({
      name: null,
      port: item.targetPort,
    }));
    if (ips && ipsPort && ipsPort.length > 0) {
      endPoints = {
        [ips]: ipsPort,
      };
    }
  }

  let externalIp;
  if (externalIps) {
    const ips = externalIps.join(',');
    externalIp = ips || null;
  }

  return {
    appServiceId,
    envId,
    targetInstanceCode,
    targetAppServiceId,
    name,
    externalIp,
    type,
    ports,
    selectors,
    endPoints,
  };
}
