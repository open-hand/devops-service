import _ from 'lodash';

export default ({ formatMessage, portDs, targetLabelsDs, appInstanceOptionsDs, networkStore, projectId, envId, appId }) => {
  /**
  * 检查名字的唯一性
  * @param value
  * @param name
  * @param record
  */
  async function checkName(value, name, record) {
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
  
  return {
    autoCreate: true,
    children: {
      portDs,
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
      },
      {
        name: 'externalIps',
        label: formatMessage({ id: 'network.config.ip' }),
        multiple: true,
        validator: checkIP,
      },
    ],
    transport: {
      create: ({ data: [data] }) => {
        /**
         * NOTE: 转换port的数据，过滤掉不用的数据
         */
        const ports = _.map(data.portDs, (value, key) => ({
          port: value.port,
          targetPort: value.port,
          nodeport: value.nodeport,
          protocol: value.protocol,
        }));

        
        let targetAppServiceId;
        let targetInstanceCode;
        const selectors = {};
        // 目标对象是实例还是选择器
        if (data.target === 'instance') {
          /**
           * NOTE: 处理所有实例和单个实例的问题 
           * 所有实例直接与AppService关联所以此处赋值给targetAppServiceId
           * 单个实例直接与AppInstnace关联所以此处赋值给targetInstanceCode
          */
          if (data.appInstance === formatMessage({ id: 'all_instance' })) {
            targetAppServiceId = appId;
          } else {
            targetInstanceCode = data.appInstance;
          }
        } else {
          /**
           * NOTE: 处理selectors,将targetLabels的数组转换成key，value的对象
          */
          _.forOwn(data.targetLabelsDs, (value, key) => {
            selectors[value.keyword] = value.value;
          });
        }
        
      
        const reqData = {
          appServiceId: appId,
          envId,
          targetInstanceCode,
          targetAppServiceId,
          name: data.name,
          externalIp: data.externalIps.join(','),
          type: data.type,
          ports,
          selectors,
          endPoints: null,
        };
        return {
          method: 'post',
          url: `/devops/v1/projects/${projectId}/service`,
          data: reqData,
        };
      },
    },
  };
};
