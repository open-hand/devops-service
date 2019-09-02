import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Form, Select } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Icon } from 'choerodon-ui';
import map from 'lodash/map';

import './index.less';

const { Option, OptGroup } = Select;

export default injectIntl(observer((props) => {
  const { modal, dataSet, record, AppStore, projectId, intl: { formatMessage }, intlPrefix, prefixCls } = props;
  const [isExpand, setIsExpand] = useState(false);

  useEffect(() => {
    record.get('appServiceSource') && AppStore.loadAppService(projectId, record.get('appServiceSource'));
  }, [record.get('appServiceSource')]);

  useEffect(() => {
    record.get('templateAppServiceId') && AppStore.loadVersion(projectId, record.get('templateAppServiceId'));
    record.get('templateAppServiceVersionId') && record.set('templateAppServiceVersionId', null);
  }, [record.get('templateAppServiceId')]);
  
  function handleExpand() {
    setIsExpand((pre) => !pre);
  }

  return (
    <div className={`${prefixCls}-create-wrap-template`}>
      <div className={`${prefixCls}-create-wrap-template-title`}>
        <span>{formatMessage({ id: `${intlPrefix}.template` })}</span>
        <Icon
          type={isExpand ? 'expand_less' : 'expand_more'}
          onClick={handleExpand}
          className={`${prefixCls}-create-wrap-template-icon`}
        />
      </div>
      {isExpand && (
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
          <Select name="templateAppServiceVersionId" colSpan={3}>
            {map(AppStore.getVersion, ({ id, version }) => (
              <Option value={id}>{version}</Option>
            ))}
          </Select>
        </Form>
      )}
    </div>
  );
}));
