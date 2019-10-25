import React, { Fragment, useEffect, useState } from 'react';
import { Button, Form, Icon, Select, SelectBox, TextField } from 'choerodon-ui/pro';
import { Form as OldForm } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import forEach from 'lodash/forEach';
import classnames from 'classnames';
import YamlEditor from '../../../../components/yamlEditor';
import StatusDot from '../../../../components/status-dot';
import Tips from '../../../../components/new-tips';
import NetworkForm from './network-form';
import DomainForm from './domain-form';

import './index.less';

const { Option, OptGroup } = Select;

const DeployModal = injectIntl(observer(({ record, dataSet, store, projectId, refresh, intlPrefix, prefixCls, modal, intl: { formatMessage }, form }) => {
  const [hasYamlFailed, setHasYamlFailed] = useState(false);
  const [resourceIsExpand, setResourceIsExpand] = useState(false);
  const [netIsExpand, setNetIsExpand] = useState(false);
  const [ingressIsExpand, setIngressIsExpand] = useState(false);

  useEffect(() => {
    ChangeConfigValue(store.getConfigValue);
  }, [store.getConfigValue]);

  modal.handleOk(async () => {
    if (hasYamlFailed || await record.validate() === false) return false;
    let result = true;
    let hasDomain = false;
    let hasNet = false;
    const { getFieldValue, validateFieldsAndScroll } = form;
    const fieldNames = [];
    if (getFieldValue('networkName') || getFieldValue('externalIps') || getFieldValue('port').join('') || getFieldValue('tport').join('') || (getFieldValue('nport') && getFieldValue('nport').join('')) || (getFieldValue('protocol') && getFieldValue('protocol').join(''))) {
      fieldNames.push('networkName', 'externalIps', 'portKeys', 'config', 'port', 'tport', 'nport', 'protocol');
      hasNet = true;
    }
    if (getFieldValue('domain') || getFieldValue('domainName') || getFieldValue('netPort').join('') || getFieldValue('path').join('') !== '/') {
      fieldNames.push('domain', 'domainName', 'paths', 'netPort', 'path', 'network', 'type');
      hasDomain = true;
    }
    if (fieldNames.length) {
      result = await formValidate(fieldNames, hasNet, hasDomain);
    }
    if (!result) {
      return false;
    }
    try {
      if (await dataSet.submit() !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  });

  function formValidate(fieldNames, hasNet, hasDomain) {
    const { getFieldValue, validateFieldsAndScroll } = form;
    return new Promise((resolve) => {
      validateFieldsAndScroll(fieldNames, (err, data) => {
        if (!err) {
          const {
            networkName,
            externalIps,
            portKeys,
            port,
            tport,
            nport,
            protocol,
            config,
            domain,
            domainName,
            certId,
            paths,
            path,
            network,
            netPort,
          } = data;
          if (hasNet) {
            const ports = [];
            if (portKeys) {
              forEach(portKeys, (item) => {
                if (item || item === 0) {
                  const node = {
                    port: Number(port[item]),
                    targetPort: Number(tport[item]),
                    nodePort: nport ? Number(nport[item]) : null,
                  };
                  config === 'NodePort' && (node.protocol = protocol[item]);
                  ports.push(node);
                }
              });
            }

            const networkData = {
              name: networkName,
              appServiceId: Number(record.get('appServiceId').split('__')[0]),
              instances: [record.get('instanceName')],
              envId: record.get('environmentId'),
              externalIp: externalIps && externalIps.length ? externalIps.join(',') : null,
              ports,
              type: config,
            };
            record.set('devopsServiceReqVO', networkData);
          }
          if (hasDomain) {
            const pathList = [];
            forEach(paths, (item) => {
              const pt = path[item];
              const servicePort = Number(netPort[item]);
              const serviceName = network[item];
              pathList.push({
                path: pt,
                servicePort,
                serviceName,
              });
            });

            const ingress = {
              domain,
              name: domainName,
              certId,
              appServiceId: Number(record.get('appServiceId').split('__')[0]),
              envId: record.get('environmentId'),
              pathList,
            };
            record.set('devopsIngressVO', ingress);
          }
          resolve(true);
        }
        resolve(false);
      });
    });
  }

  function ChangeConfigValue(value) {
    record.set('values', value);
  }

  function handleEnableNext(flag) {
    setHasYamlFailed(flag);
  }

  function renderEnvOption({ record: envRecord, text, value }) {
    return (
      <Fragment>
        {value && (<StatusDot
          connect={envRecord.get('connect')}
          synchronize={envRecord.get('synchro')}
          active={envRecord.get('active')}
          size="small"
        />)}
        <span className={`${prefixCls}-select-option-text`}>{text}</span>
      </Fragment>
    );
  }

  function renderOptionProperty({ record: envRecord }) {
    const isAvailable = envRecord.get('connect') && envRecord.get('synchro') && envRecord.get('permission');
    return ({
      disabled: !isAvailable,
    });
  }

  function handleExpand(Operating) {
    Operating((pre) => !pre);
  }

  return (
    <div className={`${prefixCls}-manual-deploy`}>
      <Tips
        helpText={formatMessage({ id: `${intlPrefix}.source.tips` })}
        title={formatMessage({ id: `${intlPrefix}.source` })}
      />
      <Form record={record} columns={3}>
        <SelectBox name="appServiceSource" colSpan={3}>
          <Option value="normal_service">
            <span className={`${prefixCls}-manual-deploy-radio`}>
              {formatMessage({ id: `${intlPrefix}.source.project` })}
            </span>
          </Option>
          <Option value="share_service">
            <span className={`${prefixCls}-manual-deploy-radio`}>
              {formatMessage({ id: `${intlPrefix}.source.organization` })}
            </span>
          </Option>
        </SelectBox>
        <Select
          name="appServiceId"
          searchable
          newLine
          notFoundContent={<FormattedMessage id={`${intlPrefix}.app.empty`} />}
        >
          {record.get('appServiceSource') === 'normal_service' ? (
            map(store.getAppService[0] && store.getAppService[0].appServiceList, ({ id, name, code }) => (
              <Option value={`${id}__${code}`} key={id}>{name}</Option>
            ))
          ) : (
            map(store.getAppService, ({ id: groupId, name: groupName, appServiceList }) => (
              <OptGroup label={groupName} key={groupId}>
                {map(appServiceList, ({ id, name, code }) => (
                  <Option value={`${id}__${code}`} key={id}>{name}</Option>
                ))}
              </OptGroup>
            ))
          )}
        </Select>
        <Select
          name="appServiceVersionId"
          searchable
          searchMatcher="version"
          disabled={!record.get('appServiceId')}
        />
        <Select
          name="environmentId"
          searchable
          newLine
          optionRenderer={renderEnvOption}
          popupCls={`${prefixCls}-manual-deploy`}
          dropdownMenuStyle={{ cursor: 'not-allowed' }}
          notFoundContent={<FormattedMessage id={`${intlPrefix}.env.empty`} />}
          onOption={renderOptionProperty}
        />
        <TextField
          name="instanceName"
          addonAfter={<Tips helpText={formatMessage({ id: `${intlPrefix}.instance.tips` })} />}
        />
        <Select
          name="valueId"
          searchable
          colSpan={2}
          newLine
          addonAfter={<Tips helpText={formatMessage({ id: `${intlPrefix}.config.tips` })} />}
          notFoundContent={<FormattedMessage id={`${intlPrefix}.config.empty`} />}
        />
        <YamlEditor
          colSpan={3}
          newLine
          readOnly={false}
          originValue={store.getConfigValue}
          value={record.get('values') || store.getConfigValue}
          onValueChange={ChangeConfigValue}
          handleEnableNext={handleEnableNext}
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
              className={`${prefixCls}-resource-config-network-icon`}
            />
            <FormattedMessage id={`${intlPrefix}.network`} />
          </div>
          <div className={netIsExpand ? `${prefixCls}-resource-content` : `${prefixCls}-resource-display`}>
            <NetworkForm
              form={form}
              store={store}
              envId={record.get('environmentId')}
            />
          </div>
          <div
            className={`${prefixCls}-resource-config-network`}
            onClick={() => handleExpand(setIngressIsExpand)}
          >
            <Icon
              type={ingressIsExpand ? 'expand_less' : 'expand_more'}
              className={`${prefixCls}-resource-config-network-icon`}
            />
            <FormattedMessage id={`${intlPrefix}.ingress`} />
          </div>
          <div className={ingressIsExpand ? `${prefixCls}-resource-content` : `${prefixCls}-resource-display`}>
            <DomainForm
              form={form}
              type="create"
              envId={record.get('environmentId')}
              DomainStore={store}
            />
          </div>
        </div>
      </div>
    </div>
  );
}));

export default OldForm.create()(DeployModal);
