import React, { Fragment, useEffect, useState } from 'react';
import { Form, Icon, Select, TextField } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import find from 'lodash/find';
import uuidV1 from 'uuid/v1';
import YamlEditor from '../../../../components/yamlEditor';
import NetworkForm from './network-form';
import DomainForm from '../../../resource/main-view/contents/application/modals/domain/domainForm';

import './index.less';

const { Option } = Select;

export default injectIntl(observer(({ record, store, projectId, networkStore, ingressStore, intlPrefix, prefixCls, modal }) => {
  const [resourceIsExpand, setResourceIsExpand] = useState(false);
  const [netIsExpand, setNetIsExpand] = useState(false);
  const [ingressIsExpand, setIngressIsExpand] = useState(false);
  const [netFormRef, setNetFormRef] = useState();
  const [ingressFormRef, setIngressFormRef] = useState();

  useEffect(() => {
    store.loadAppService(projectId);
    store.loadEnv(projectId);
  }, []);

  useEffect(() => {
    if (record.get('appServiceId')) {
      store.loadVersion(projectId, record.get('appServiceId'));
      const data = find(store.getAppService, ['id', record.get('appServiceId')]) || {};
      record.set('instanceName', getRandomName(data.code));
    }
    record.set('versionId', null);
  }, [record.get('appServiceId')]);

  useEffect(() => {
    if (record.get('envId') && record.get('appServiceId')) {
      store.loadConfig(projectId, record.get('envId'), record.get('appServiceId'));
      networkStore.loadPorts(projectId, record.get('envId'), record.get('appServiceId'));
    }
    record.get('configId', null);
  }, [record.get('envId'), record.get('appServiceId')]);

  useEffect(() => {
    record.get('configId') && store.loadConfigValue(projectId, record.get('configId'));
  }, [record.get('configId')]);

  useEffect(() => {
    ChangeConfigValue(store.getConfigValue.value);
  }, [store.getConfigValue.value]);

  modal.handleOk(async () => {
    // as
  });

  function getRandomName(prefix) {
    const randomString = uuidV1();

    return prefix
      ? `${prefix.substring(0, 24)}-${randomString.substring(0, 5)}`
      : randomString.substring(0, 30);
  }

  function ChangeConfigValue(value) {
    record.set('configValue', value);
  }

  function handleExpand(Operating) {
    Operating((pre) => !pre);
  }

  return (
    <div className={`${prefixCls}-manual-deploy`}>
      <Form record={record} columns={3}>
        <Select name="appServiceId" searchable>
          {map(store.getAppService, ({ id, name }) => (
            <Option value={id}>{name}</Option>
          ))}
        </Select>
        <Select name="versionId" searchable>
          {map(store.getVersion, ({ id, version }) => (
            <Option value={id}>{version}</Option>
          ))}
        </Select>
        <Select name="envId" searchable newLine>
          {map(store.getEnv, ({ id, name }) => (
            <Option value={id}>{name}</Option>
          ))}
        </Select>
        <TextField name="instanceName" />
        <Select name="configId" searchable colSpan={2} newLine>
          {map(store.getConfig, ({ id, name }) => (
            <Option value={id}>{name}</Option>
          ))}
        </Select>
        <YamlEditor
          colSpan={3}
          newLine
          readOnly={false}
          originValue={store.getConfigValue.value}
          value={record.get('configValue') || store.getConfigValue.value}
          onValueChange={ChangeConfigValue}
        />
      </Form>
      <div className={`${prefixCls}-resource-config`}>
        <div
          className={`${prefixCls}-resource-config-title`}
          onClick={() => handleExpand(setResourceIsExpand)}
        >
          <FormattedMessage id={`${intlPrefix}.resource`} />
          <Icon
            type={resourceIsExpand ? 'expand_less' : 'expand_more'}
            className={`${prefixCls}-resource-config-icon`}
          />
        </div>
        <div className={resourceIsExpand ? '' : `${prefixCls}-resource-display`}>
          <div
            className={`${prefixCls}-resource-config-network`}
            onClick={() => handleExpand(setNetIsExpand)}
          >
            <Icon
              type={netIsExpand ? 'expand_less' : 'expand_more'}
              className={`${prefixCls}-resource-config-icon`}
            />
            <FormattedMessage id={`${intlPrefix}.network`} />
          </div>
          <div className={netIsExpand ? '' : `${prefixCls}-resource-display`}>
            <NetworkForm
              wrappedComponentRef={(form) => setNetFormRef(form)}
              store={networkStore}
              record={record}
            />
          </div>
          <div
            className={`${prefixCls}-resource-config-network`}
            onClick={() => handleExpand(setIngressIsExpand)}
          >
            <Icon
              type={ingressIsExpand ? 'expand_less' : 'expand_more'}
              className={`${prefixCls}-resource-config-icon`}
            />
            <FormattedMessage id={`${intlPrefix}.ingress`} />
          </div>
          <div className={ingressIsExpand ? '' : `${prefixCls}-resource-display`}>
            <DomainForm
              wrappedComponentRef={(form) => setIngressFormRef(form)}
              type="create"
              envId={record.get('envId')}
              DomainStore={ingressStore}
            />
          </div>
        </div>
      </div>
    </div>
  );
}));
