/* eslint-disable import/no-anonymous-default-export */
import { DataSetProps } from 'choerodon-ui/pro/lib/data-set/DataSet';
import { FieldType } from 'choerodon-ui/pro/lib/data-set/enum';
import { DataSet } from 'choerodon-ui/pro';
import HostConfigApis from '@/routes/host-config/apis';
import { CustomValidator } from 'choerodon-ui/pro/lib/validator/Validator';

interface FormProps {
  formatMessage(arg0: object, arg1?: object): string,
  intlPrefix: string,
  projectId: number,
  typeDs: DataSet,
  accountDs: DataSet,
}

export default ({
  formatMessage,
  intlPrefix,
  projectId,
  typeDs,
  accountDs,
}: FormProps): DataSetProps => {
  async function checkName(value: any, name: any, record: any) {
    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && value === record.getPristineValue(name)) {
      return true;
    }
    if (value && pa.test(value)) {
      try {
        const res = await HostConfigApis.axiosCheckName(projectId, value);
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

  function checkIP(value: any) {
    const p = /^((\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])\.){3}(\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])$/;
    if (value && !p.test(value)) {
      return formatMessage({ id: 'network.ip.check.failed' });
    }
    return true;
  }

  return ({
    autoCreate: false,
    selection: false,
    // @ts-ignore
    fields: [
      {
        name: 'type',
        type: 'string' as FieldType,
        textField: 'text',
        valueField: 'value',
        defaultValue: 'test',
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
        name: 'ip',
        type: 'string' as FieldType,
        required: true,
        validator: checkIP,
        label: 'IP',
      },
      {
        name: 'port',
        required: true,
        label: formatMessage({ id: `${intlPrefix}.port` }),
      },
      {
        name: 'userName',
        type: 'string' as FieldType,
        required: true,
        label: formatMessage({ id: 'userName' }),
      },
      {
        name: 'password',
        type: 'string' as FieldType,
        required: true,
        dynamicProps: {
          required: ({ record }) => record.get('account') === 'password',
        },
        label: formatMessage({ id: 'password' }),
      },
      {
        name: 'token',
        type: 'string' as FieldType,
        required: true,
        dynamicProps: {
          required: ({ record }) => record.get('account') === 'token',
        },
        label: formatMessage({ id: `${intlPrefix}.token` }),
      },
      {
        name: 'account',
        type: 'string' as FieldType,
        textField: 'text',
        valueField: 'value',
        required: true,
        defaultValue: 'password',
        options: accountDs,
        // label: formatMessage({ id: `${intlPrefix}.account` }),
      },
      {
        name: 'jmeterPort',
        type: 'string' as FieldType,
        dynamicProps: {
          required: ({ record }) => record.get('type') === 'test',
        },
        label: formatMessage({ id: `${intlPrefix}.jmeter.port` }),
      },
      {
        name: 'jmeterPath',
        type: 'string' as FieldType,
        dynamicProps: {
          required: ({ record }) => record.get('type') === 'test',
        },
        label: formatMessage({ id: `${intlPrefix}.jmeter.path` }),
      },
    ],
  });
};
