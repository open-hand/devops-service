/* eslint-disable import/no-anonymous-default-export */
import { DataSetProps } from 'choerodon-ui/pro/lib/data-set/DataSet';
import { FieldType } from 'choerodon-ui/pro/lib/data-set/enum';
import { DataSet } from 'choerodon-ui/pro';
import HostConfigApis from '@/routes/host-config/apis';
import omit from 'lodash/omit';

interface FormProps {
  formatMessage(arg0: object, arg1?: object): string,
  intlPrefix: string,
  projectId: number,
  typeDs: DataSet,
  accountDs: DataSet,
  hostId: string,
}

function handleUpdate({ name, record }: { name: string, record: any }) {
  if (name === 'hostIp') {
    record.get('sshPort') && record.getField('sshPort').checkValidity();
    if (record.get('type') === 'distribute_test' && record.get('jmeterPort')) {
      record.getField('jmeterPort').checkValidity();
    }
  }
}

export default ({
  formatMessage,
  intlPrefix,
  projectId,
  typeDs,
  accountDs,
  hostId,
}: FormProps): DataSetProps => {
  async function checkName(value: any, name: any, record: any) {
    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && value === record.getPristineValue(name)) {
      return true;
    }
    if (value && pa.test(value)) {
      try {
        const res = await HostConfigApis.checkName(projectId, value);
        if ((res && res.failed) || !res) {
          return formatMessage({ id: 'checkNameExist' });
        }
        return true;
      } catch (err) {
        return formatMessage({ id: 'checkNameFail' });
      }
    } else {
      return formatMessage({ id: 'checkCodeReg' });
    }
  }

  async function checkPortUnique(ip: string, port: any, type: string) {
    try {
      const control = type === 'sshPort' ? HostConfigApis.checkSshPort : HostConfigApis.checkJmeterPort;
      const res = await control(projectId, ip, port);
      return res;
    } catch (e) {
      return false;
    }
  }

  function checkIP(value: any) {
    const p = /^((\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])\.){3}(\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])$/;
    if (value && !p.test(value)) {
      return formatMessage({ id: 'network.ip.check.failed' });
    }
    return true;
  }

  async function checkPort(value: any, name: any, record: any) {
    const p = /^([1-9]\d*|0)$/;
    const data = {
      typeMsg: '',
      min: 1,
      max: 65535,
      failedMsg: `${intlPrefix}.port.check.failed`,
    };

    if (value) {
      if (
        p.test(value)
        && parseInt(value, 10) >= data.min
        && parseInt(value, 10) <= data.max
      ) {
        if (record.get('hostIp')) {
          if (await checkPortUnique(record.get('hostIp'), value, name) === false) {
            return formatMessage({ id: `${intlPrefix}.port.unique.failed.${name}` });
          }
        }
        return true;
      }
      return formatMessage({ id: data.failedMsg });
    }
    return true;
  }

  function checkPath(value: any) {
    const p = /^([\/] [\w-]+)*$/;
    if (value && !p.test(value)) {
      return formatMessage({ id: 'network.ip.check.failed' });
    }
    return true;
  }

  return ({
    autoCreate: false,
    selection: false,
    transport: {
      read: {
        url: HostConfigApis.getHostDetail(projectId, hostId),
        method: 'get',
      },
      create: ({ data: [data] }) => {
        const postData = omit(data, ['__status', '__id', 'status', 'jmeterStatus', 'sshStatus']);
        return ({
          url: HostConfigApis.createHost(projectId),
          method: 'post',
          data: postData,
        });
      },
      update: ({ data: [data] }) => {
        const postData = omit(data, ['__status', '__id', 'status', 'jmeterStatus', 'sshStatus']);
        return ({
          url: HostConfigApis.editHost(projectId, hostId),
          method: 'put',
          data: postData,
        });
      },
    },
    // @ts-ignore
    fields: [
      {
        name: 'type',
        type: 'string' as FieldType,
        textField: 'text',
        valueField: 'value',
        defaultValue: 'distribute_test',
        options: typeDs,
        label: formatMessage({ id: `${intlPrefix}.type` }),
      },
      {
        name: 'name',
        type: 'string' as FieldType,
        maxLength: 30,
        required: true,
        validator: checkName,
        label: '主机名称',
      },
      {
        name: 'hostIp',
        type: 'string' as FieldType,
        required: true,
        validator: checkIP,
        label: 'IP',
      },
      {
        name: 'sshPort',
        required: true,
        validator: checkPort,
        label: formatMessage({ id: `${intlPrefix}.port` }),
      },
      {
        name: 'username',
        type: 'string' as FieldType,
        required: true,
        label: formatMessage({ id: 'userName' }),
      },
      {
        name: 'password',
        type: 'string' as FieldType,
        required: true,
        dynamicProps: {
          label: ({ record }) => formatMessage({ id: record.get('authType') === 'accountPassword' ? 'password' : `${intlPrefix}.token` }),
        },
      },
      {
        name: 'authType',
        type: 'string' as FieldType,
        textField: 'text',
        valueField: 'value',
        required: true,
        defaultValue: 'accountPassword',
        options: accountDs,
      },
      {
        name: 'jmeterPort',
        type: 'string' as FieldType,
        validator: checkPort,
        dynamicProps: {
          required: ({ record }) => record.get('type') === 'distribute_test',
        },
        label: formatMessage({ id: `${intlPrefix}.jmeter.port` }),
      },
      {
        name: 'jmeterPath',
        type: 'string' as FieldType,
        // validator: checkPath,
        dynamicProps: {
          required: ({ record }) => record.get('type') === 'distribute_test',
        },
        label: formatMessage({ id: `${intlPrefix}.jmeter.path` }),
      },
    ],
    events: {
      update: handleUpdate,
    },
  });
};
