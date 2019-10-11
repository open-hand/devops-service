import React, { Fragment, useEffect, useState } from 'react';
import { Button, Form, Icon, Select, SelectBox, TextField } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import classnames from 'classnames';
import YamlEditor from '../../../../components/yamlEditor';
import StatusDot from '../../../../components/status-dot';
import Tips from '../../../../components/new-tips';

import './index.less';

const { Option, OptGroup } = Select;

const DeployModal = injectIntl(observer(({ record, dataSet, store, projectId, refresh, intlPrefix, prefixCls, modal, intl: { formatMessage } }) => {
  const [hasYamlFailed, setHasYamlFailed] = useState(false);

  useEffect(() => {
    ChangeConfigValue(store.getConfigValue);
  }, [store.getConfigValue]);

  modal.handleOk(async () => {
    if (hasYamlFailed) return false;
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

  function ChangeConfigValue(value) {
    record.set('values', value);
  }

  function handleEnableNext(flag) {
    setHasYamlFailed(flag);
  }

  function renderEnvOption({ record: envRecord, text, value }) {
    const isAvailable = envRecord.get('connect') && envRecord.get('synchro') && envRecord.get('permission');
    const envClass = classnames({
      [`${prefixCls}-manual-deploy-available`]: isAvailable,
      [`${prefixCls}-manual-deploy-disabled`]: !isAvailable,
    });
    return (
      <div className={envClass}>
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
          <Option value="market_service">
            <span className={`${prefixCls}-manual-deploy-radio`}>
              {formatMessage({ id: `${intlPrefix}.source.market` })}
            </span>
          </Option>
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
        <Select name="appServiceVersionId" searchable disabled={!record.get('appServiceId')} />
        <Select
          name="environmentId"
          searchable
          newLine
          optionRenderer={renderEnvOption}
          popupCls={`${prefixCls}-manual-deploy`}
          dropdownMenuStyle={{ cursor: 'not-allowed' }}
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
    </div>
  );
}));

export default DeployModal;
