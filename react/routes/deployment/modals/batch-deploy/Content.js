import React, { Fragment, useEffect, useState, useMemo } from 'react';
import { Button, Form, Icon, Select, SelectBox, TextField } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import forEach from 'lodash/forEach';
import YamlEditor from '../../../../components/yamlEditor';
import StatusDot from '../../../../components/status-dot';
import Tips from '../../../../components/new-tips';
import NetworkForm from './NetworkForm';
import DomainForm from './DomainForm';
import { useBatchDeployStore } from './stores';

import './index.less';

const { Option, OptGroup } = Select;

const BatchDeployModal = injectIntl(observer(() => {
  const {
    batchDeployDs,
    deployStore,
    refresh,
    intlPrefix,
    prefixCls,
    modal,
    intl: { formatMessage },
    envId,
    isInstanceView,
  } = useBatchDeployStore();

  const record = useMemo(() => batchDeployDs.current, [batchDeployDs.current]);

  const [hasYamlFailed, setHasYamlFailed] = useState(false);
  const [resourceIsExpand, setResourceIsExpand] = useState(false);
  const [netIsExpand, setNetIsExpand] = useState(false);
  const [ingressIsExpand, setIngressIsExpand] = useState(false);
  
  useEffect(() => {
    if (envId) {
      record.init('environmentId', envId);
    }
  }, [envId]);

  useEffect(() => {
    ChangeConfigValue(deployStore.getConfigValue);
  }, [deployStore.getConfigValue]);
  

  modal.handleOk(async () => {
    if (hasYamlFailed) return false;
    try {
      const environmentId = batchDeployDs.current.get('environmentId');
      const res = await batchDeployDs.submit();
      if (res !== false) {
        refresh({ envId: environmentId }, 'resource');
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  });

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

  function handleRemoveForm(formRecord) {
    batchDeployDs.remove(formRecord);
  }

  function handleAddForm(appServiceType) {
    batchDeployDs.create();
    batchDeployDs.current.init('appServiceSource', appServiceType);
    deployStore.setConfigValue('');
  }

  function handleClickAppService(formRecord) {
    batchDeployDs.current = formRecord;
  }

  return (
    <div className={`${prefixCls}-batch-deploy`}>
      {!envId && <Form record={batchDeployDs.current} columns={2}>
        <Select
          name="environmentId"
          searchable
          clearButton={false}
          optionRenderer={renderEnvOption}
          notFoundContent={<FormattedMessage id={`${intlPrefix}.env.empty`} />}
          onOption={renderOptionProperty}
        />
        <span colSpan={1} />
      </Form>}
      <div className={`${prefixCls}-batch-deploy-content`}>
        <div className={`${prefixCls}-batch-deploy-content-app`}>
          {map(batchDeployDs.data, (formRecord) => {
            if (formRecord.get('appServiceSource') === 'normal_service') {
              return (
                <div
                  className={batchDeployDs.current === formRecord ? `${prefixCls}-batch-deploy-content-app-active` : ''}
                >
                  <Form record={formRecord} columns={5}>
                    <Select
                      name="appServiceId"
                      searchable
                      colSpan={4}
                      notFoundContent={<FormattedMessage id={`${intlPrefix}.app.empty`} />}
                      onClick={() => handleClickAppService(formRecord)}
                    >
                      {map(deployStore.getAppService[0] && deployStore.getAppService[0].appServiceList, ({ id, name, code }) => (
                        <Option value={`${id}__${code}`} key={id}>{name}</Option>
                      ))}
                    </Select>
                    {batchDeployDs.data.length > 1 ? (
                      <Button
                        funcType="flat"
                        icon="delete"
                        colSpan={1}
                        className="appService-delete-btn"
                        onClick={() => handleRemoveForm(formRecord)}
                      />
                    ) : <span colSpan={1} />}
                  </Form>
                </div>
              );
            }
          })}
          <Button
            funcType="flat"
            color="primary"
            icon="add"
            className="appService-add-btn"
            onClick={() => handleAddForm('normal_service')}
          >
            {formatMessage({ id: `${intlPrefix}.add.appService.normal` })}
          </Button>
          {map(batchDeployDs.data, (formRecord) => {
            if (formRecord.get('appServiceSource') === 'share_service') {
              return (
                <div
                  className={batchDeployDs.current === formRecord ? `${prefixCls}-batch-deploy-content-app-active` : ''}
                >
                  <Form record={formRecord} columns={5}>
                    <Select
                      name="appServiceId"
                      searchable
                      colSpan={4}
                      notFoundContent={<FormattedMessage id={`${intlPrefix}.app.empty`} />}
                      onClick={() => handleClickAppService(formRecord)}
                    >
                      {map(deployStore.getShareAppService, ({ id: groupId, name: groupName, appServiceList }) => (
                        <OptGroup label={groupName} key={groupId}>
                          {map(appServiceList, ({ id, name, code }) => (
                            <Option value={`${id}__${code}`} key={id}>{name}</Option>
                          ))}
                        </OptGroup>
                      ))}
                    </Select>
                    {batchDeployDs.data.length > 1 ? (
                      <Button
                        funcType="flat"
                        icon="delete"
                        colSpan={1}
                        className="appService-delete-btn"
                        onClick={() => handleRemoveForm(formRecord)}
                      />
                    ) : <span colSpan={1} />}
                  </Form>
                </div>
              );
            }
          })}
          <Button
            funcType="flat"
            color="primary"
            icon="add"
            className="appService-add-btn"
            onClick={() => handleAddForm('share_service')}
          >
            {formatMessage({ id: `${intlPrefix}.add.appService.share` })}
          </Button>
        </div>
        <div className={`${prefixCls}-batch-deploy-content-form`}>
          <Form record={batchDeployDs.current} columns={3}>
            <Select
              name="appServiceVersionId"
              searchable
              searchMatcher="version"
              disabled={record && !record.get('appServiceId')}
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
              disabled={record && (!record.get('appServiceId') || !record.get('environmentId'))}
              addonAfter={<Tips helpText={formatMessage({ id: `${intlPrefix}.config.tips` })} />}
              notFoundContent={<FormattedMessage id={`${intlPrefix}.config.empty`} />}
            />
            <YamlEditor
              colSpan={3}
              newLine
              readOnly={false}
              originValue={deployStore.getConfigValue}
              value={(record ? record.get('values') : '') || deployStore.getConfigValue}
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
                <NetworkForm />
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
                <DomainForm />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}));

export default BatchDeployModal;
