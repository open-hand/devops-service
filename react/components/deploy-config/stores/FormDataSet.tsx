import { DataSetProps } from 'choerodon-ui/pro/lib/data-set/DataSet';
import { FieldIgnore, FieldType } from 'choerodon-ui/pro/lib/data-set/enum';
import Record from 'choerodon-ui/pro/lib/data-set/Record';
import { axios } from '@choerodon/boot';
import { StoreProps } from '@/components/deploy-config/stores/useStore';
import { DataSet } from 'choerodon-ui/pro';

interface FormProps {
  intlPrefix: string,
  formatMessage(arg0: object): string,
  projectId: number,
  envId: string,
  configId?: string,
  store: StoreProps,
  appOptionDs: DataSet,
  appServiceId?: string,
  appServiceName?: string,
}

interface UpdateProps {
  name: string,
  value: any,
  record: Record,
}

export default ({
                  formatMessage,
                  intlPrefix,
                  projectId,
                  envId,
                  configId,
                  store,
                  appOptionDs,
                  appServiceId,
                  appServiceName,
                }: FormProps): DataSetProps => {
  const handleUpdate = async ({ name, value, record }: UpdateProps) => {
    if (name === 'appServiceId' && value) {
      if (value) {
        try {
          const res = await store.loadValue(projectId, value);
          record.set('value', res);
        } catch (e) {
          record.set('value', '');
        }
      } else {
        record.set('value', '');
      }
    }
  };
  // @ts-ignore
  const nameValidator = async (value: any, name?: string, record: Record) => {
    const id = record.get('id');
    const oldName = record.getPristineValue('name');
    if (id && oldName === value) {
      return true;
    }
    try {
      const res = await axios.get(`/devops/v1/projects/${projectId}/deploy_value/check_name?name=${encodeURIComponent(value)}&env_id=${envId}`);
      if ((res && res.failed) || !res) {
        return '名称已存在';
      }
      return true;
    } catch (err) {
      return '名称校验失败，请稍后再试';
    }
  };

  return {
    autoCreate: false,
    autoQuery: false,
    paging: false,
    autoQueryAfterSubmit: false,
    fields: [{
      name: 'name',
      type: 'string' as FieldType,
      label: '部署配置名称',
      required: true,
      maxLength: 30,
      validator: nameValidator,
    }, {
      name: 'description',
      type: 'string' as FieldType,
      required: true,
      label: '描述',
      maxLength: 200,
    }, {
      name: 'appServiceId',
      type: 'string' as FieldType,
      textField: 'appServiceName',
      valueField: 'appServiceId',
      label: '应用服务',
      required: true,
      options: appOptionDs,
      defaultValue: appServiceId,
    }, {
      name: 'appServiceName',
      type: 'string' as FieldType,
      label: '应用服务',
      readOnly: true,
      ignore: 'always' as FieldIgnore,
      defaultValue: appServiceName,
    }, {
      name: 'value',
      type: 'string' as FieldType,
    }, {
      name: 'envId',
      type: 'string' as FieldType,
      defaultValue: envId,
    }],
    transport: {
      read: {
        url: `/devops/v1/projects/${projectId}/deploy_value?value_id?value_id=${configId}`,
        method: 'get',
      },
      submit: ({ data: [data] }) => ({
        url: `/devops/v1/projects/${projectId}/deploy_value`,
        method: 'post',
        data,
      }),
    },
    events: {
      update: handleUpdate,
    },
  };
};
