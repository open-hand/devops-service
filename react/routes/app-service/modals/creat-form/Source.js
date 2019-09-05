import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Form, Select } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Icon } from 'choerodon-ui';
import map from 'lodash/map';

import './index.less';

const { Option, OptGroup } = Select;

export default injectIntl(observer((props) => {
  const { record, AppStore, projectId, intl: { formatMessage }, intlPrefix, prefixCls } = props;

  useEffect(() => {
    if (record.get('appServiceSource')) {
      AppStore.loadAppService(projectId, record.get('appServiceSource'));
    } else {
      record.set('templateAppServiceId', null);
      AppStore.setAppService([]);
    }
  }, [record.get('appServiceSource')]);

  useEffect(() => {
    if (record.get('templateAppServiceId')) {
      AppStore.loadVersion(projectId, record.get('templateAppServiceId'));
    } else {
      AppStore.setVersion([]);
    }
    record.get('templateAppServiceVersionId') && record.set('templateAppServiceVersionId', null);
  }, [record.get('templateAppServiceId')]);

  return (
    <div className={`${prefixCls}-create-wrap-template`}>
      <div className={`${prefixCls}-create-wrap-template-title`}>
        <span>{formatMessage({ id: `${intlPrefix}.template` })}</span>
      </div>
      <Form record={record} columns={3}>
        <Select name="appServiceSource">
          <Option value="normal_service">{formatMessage({ id: `${intlPrefix}.source.project` })}</Option>
          <Option value="share_service">{formatMessage({ id: `${intlPrefix}.source.organization` })}</Option>
          <Option value="market_service">{formatMessage({ id: `${intlPrefix}.source.market` })}</Option>
        </Select>
        <Select name="templateAppServiceId" colSpan={2}>
          {record.get('appServiceSource') === 'normal_service' ? (
            map(AppStore.getAppService[0] && AppStore.getAppService[0].appServiceList, ({ id, name }) => (
              <Option value={id}>{name}</Option>
            ))
          ) : (
            map(AppStore.getAppService, ({ id: groupId, name: groupName, appServiceList }) => (
              <OptGroup label={groupName} key={groupId}>
                {map(appServiceList, ({ id, name }) => (
                  <Option value={id}>{name}</Option>
                ))}
              </OptGroup>
            ))
          )}
        </Select>
        <Select name="templateAppServiceVersionId" colSpan={3} searchable>
          {map(AppStore.getVersion, ({ id, version }) => (
            <Option value={id}>{version}</Option>
          ))}
        </Select>
      </Form>
    </div>
  );
}));
