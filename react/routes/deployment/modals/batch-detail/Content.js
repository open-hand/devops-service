import React, { Fragment, useEffect } from 'react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import { Collapse } from 'choerodon-ui';
import { useBatchDetailStore } from './stores';
import MouserOverWrapper from '../../../../components/MouseOverWrapper';

import './index.less';

const { Panel } = Collapse;

export default injectIntl(observer((props) => {
  const {
    batchDetailDs,
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    location: { search },
    history,
  } = useBatchDetailStore();

  function getPanelHeader(record) {
    return (
      <div className={`${prefixCls}-detail-batch-collapse-header`}>
        <span className={`${prefixCls}-detail-batch-text`}>
          {formatMessage({ id: `${intlPrefix}.service` })}
        </span>
        <span>{record.get('appServiceName')}</span>
      </div>
    );
  }

  function linkToInstance(record) {
    if (record) {
      const instanceId = record.get('instanceId');
      const appServiceId = record.get('appServiceId');
      const envId = record.get('envId');
      history.push({
        pathname: '/devops/resource',
        search,
        state: {
          instanceId,
          appServiceId,
          envId,
        },
      });
    }
    history.push(`/devops/resource${search}`);
  }

  return (
    <div className={`${prefixCls}-detail-batch-content`}>
      <Collapse bordered={false}>
        {batchDetailDs.map((record) => (
          <Panel key={record.id} header={getPanelHeader(record)}>
            <ul className={`${prefixCls}-detail-batch`}>
              <li className={`${prefixCls}-detail-batch-item ${prefixCls}-detail-batch-item-flex`}>
                <span className={`${prefixCls}-detail-batch-text`}>
                  {formatMessage({ id: `${intlPrefix}.version` })}
                </span>
                <MouserOverWrapper text={record.get('appServiceVersion')} width="230px">
                  <span>{record.get('appServiceVersion')}</span>
                </MouserOverWrapper>
              </li>
              <li className={`${prefixCls}-detail-batch-item`}>
                <span className={`${prefixCls}-detail-batch-text`}>
                  {formatMessage({ id: `${intlPrefix}.env` })}
                </span>
                <span>{record.get('envName')}</span>
              </li>
              <li className={`${prefixCls}-detail-batch-item`}>
                <span className={`${prefixCls}-detail-batch-text`}>
                  {formatMessage({ id: `${intlPrefix}.instance` })}
                </span>
                {record.get('deleted') ? (<span>
                  {formatMessage({ id: 'deleted' })}
                </span>) : (<span
                  onClick={() => linkToInstance(record)}
                  className={`${prefixCls}-detail-batch-instance`}
                >
                  {record.get('instanceName')}
                </span>)}
              </li>
            </ul>
          </Panel>
        ))}
      </Collapse>
    </div>
  );
}));
