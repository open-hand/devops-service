import React, { Fragment, useEffect, useState } from 'react';
import { Button, Form, Icon, Select, SelectBox, TextField } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import find from 'lodash/find';
import forEach from 'lodash/forEach';
import uuidV1 from 'uuid/v1';
import YamlEditor from '../../../../components/yamlEditor';
import NetworkForm from './network-form';
import DomainForm from '../../../../components/domain-form';
import StatusDot from '../../../../components/status-dot';

import './index.less';

const { Option, OptGroup } = Select;

export default injectIntl(observer(({ record, dataSet, store, projectId, networkStore, ingressStore, refresh, intlPrefix, prefixCls, modal, intl: { formatMessage } }) => {
  const [resourceIsExpand, setResourceIsExpand] = useState(false);
  const [netIsExpand, setNetIsExpand] = useState(false);
  const [ingressIsExpand, setIngressIsExpand] = useState(false);
  const [netFormRef, setNetFormRef] = useState();
  const [ingressFormRef, setIngressFormRef] = useState();
  const [hasYamlFailed, setHasYamlFailed] = useState(false);

  useEffect(() => {
    if (record.get('environmentId') && record.get('appServiceId')) {
      networkStore.loadPorts(projectId, record.get('environmentId'), record.get('appServiceId').split('__')[0]);
    }
  }, [record.get('environmentId'), record.get('appServiceId')]);

  useEffect(() => {
    ChangeConfigValue(store.getConfigValue);
  }, [store.getConfigValue]);

  modal.handleOk(async () => {
    if (hasYamlFailed) return false;

    let hasFailed = false;
    const netForm = netFormRef.props.form;
    const ingressForm = ingressFormRef.props.form;
    if (netForm.getFieldValue('name') || netForm.getFieldValue('externalIps') || netForm.getFieldValue('port').join('') || netForm.getFieldValue('tport').join('') || (netForm.getFieldValue('nport') && netForm.getFieldValue('nport').join('')) || (netForm.getFieldValue('protocol') && netForm.getFieldValue('protocol').join(''))) {
      netFormRef.props.form.validateFieldsAndScroll((err, data) => {
        if (!err) {
          const {
            name,
            externalIps,
            portKeys,
            port,
            tport,
            nport,
            protocol,
            config,
          } = data;

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

          const network = {
            name,
            appServiceId: record.get('appServiceId').split('__')[0],
            instances: [record.get('instanceName')],
            envId: record.get('environmentId'),
            externalIp: externalIps && externalIps.length ? externalIps.join(',') : null,
            ports,
            type: config,
          };
          record.set('devopsServiceReqVO', network);
        } else {
          hasFailed = true;
        }
      });
    }
    if (ingressForm.getFieldValue('domain') || ingressForm.getFieldValue('name') || ingressForm.getFieldValue('network').join('') || ingressForm.getFieldValue('port').join('') || ingressForm.getFieldValue('path').join('') !== '/') {
      ingressFormRef.props.form.validateFieldsAndScroll((err, data) => {
        if (!err) {
          const {
            domain,
            name,
            certId,
            paths,
            path,
            network,
            port,
          } = data;

          const pathList = [];
          const networkList = ingressStore.getNetwork;
          forEach(paths, (item) => {
            const pt = path[item];
            const serviceId = network[item];
            const servicePort = port[item];
            const serviceName = find(networkList, ['id', serviceId])[0].name;
            pathList.push({
              path: pt,
              serviceId,
              servicePort,
              serviceName,
            });
          });

          const ingress = {
            domain,
            name,
            certId,
            appServiceId: record.get('appServiceId').split('__')[0],
            envId: record.get('environmentId'),
            pathList,
          };
          record.set('devopsIngressVO', ingress);
        } else {
          hasFailed = true;
        }
      });
    }
    if (!hasFailed && await dataSet.submit() !== false) {
      refresh();
    } else {
      return false;
    }
  });

  function getRandomName(prefix) {
    const randomString = uuidV1();

    return prefix
      ? `${prefix.substring(0, 24)}-${randomString.substring(0, 5)}`
      : randomString.substring(0, 30);
  }

  function ChangeConfigValue(value) {
    record.set('values', value);
  }

  function handleExpand(Operating) {
    Operating((pre) => !pre);
  }

  function handleEnableNext(flag) {
    setHasYamlFailed(flag);
  }

  function renderEnvOption({ record: envRecord, text, value }) {
    return (
      <div disabled={!envRecord.get('connect') || !envRecord.get('synchro')}>
        {value && (<StatusDot
          connect={envRecord.get('connect')}
          synchronize={envRecord.get('synchro')}
          active={envRecord.get('active')}
          size="small"
        />)}
        <span className={`${prefixCls}-select-option-text`}>{text}</span>
      </div>
    );
  }

  return (
    <div className={`${prefixCls}-manual-deploy`}>
      <Form record={record} columns={3}>
        <SelectBox name="appServiceSource" colSpan={3}>
          <Option value="normal_service">{formatMessage({ id: `${intlPrefix}.source.project` })}</Option>
          <Option value="share_service">{formatMessage({ id: `${intlPrefix}.source.organization` })}</Option>
          <Option value="market_service">{formatMessage({ id: `${intlPrefix}.source.market` })}</Option>
        </SelectBox>
        <Select name="appServiceId" searchable newLine>
          {record.get('appServiceSource') === 'normal_service' ? (
            map(store.getAppService[0] && store.getAppService[0].appServiceList, ({ id, name, code }) => (
              <Option value={`${id}__${code}`}>{name}</Option>
            ))
          ) : (
            map(store.getAppService, ({ id: groupId, name: groupName, appServiceList }) => (
              <OptGroup label={groupName} key={groupId}>
                {map(appServiceList, ({ id, name, code }) => (
                  <Option value={`${id}__${code}`}>{name}</Option>
                ))}
              </OptGroup>
            ))
          )}
        </Select>
        <Select name="appServiceVersionId" searchable />
        <Select name="environmentId" searchable newLine optionRenderer={renderEnvOption} />
        <TextField name="instanceName" />
        <Select name="valueId" searchable colSpan={2} newLine />
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
              wrappedComponentRef={(form) => setNetFormRef(form)}
              store={networkStore}
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
              wrappedComponentRef={(form) => setIngressFormRef(form)}
              type="create"
              envId={record.get('environmentId')}
              DomainStore={ingressStore}
              isDeployPage
            />
          </div>
        </div>
      </div>
    </div>
  );
}));
