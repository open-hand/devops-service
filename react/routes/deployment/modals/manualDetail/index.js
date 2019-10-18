import React from 'react';
import { injectIntl } from 'react-intl';
import { withRouter } from 'react-router-dom';
import MouserOverWrapper from '../../../../components/MouseOverWrapper';

import './index.less';

export default withRouter(injectIntl(({
  record,
  intlPrefix,
  prefixCls,
  intl: { formatMessage },
  history,
  location: { search },
}) => {
  function linkToInstance() {
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
    <ul className={`${prefixCls}-detail-manual`}>
      <li className={`${prefixCls}-detail-manual-item`}>
        <span className={`${prefixCls}-detail-manual-text`}>
          {formatMessage({ id: `${intlPrefix}.service` })}
        </span>
        <span>{record.get('appServiceName')}</span>
      </li>
      <li className={`${prefixCls}-detail-manual-item ${prefixCls}-detail-manual-item-flex`}>
        <span className={`${prefixCls}-detail-manual-text`}>
          {formatMessage({ id: `${intlPrefix}.version` })}
        </span>
        <MouserOverWrapper text={record.get('appServiceVersion')} width="230px">
          <span>{record.get('appServiceVersion')}</span>
        </MouserOverWrapper>
      </li>
      <li className={`${prefixCls}-detail-manual-item`}>
        <span className={`${prefixCls}-detail-manual-text`}>
          {formatMessage({ id: `${intlPrefix}.env` })}
        </span>
        <span>{record.get('envName')}</span>
      </li>
      <li className={`${prefixCls}-detail-manual-item`}>
        <span className={`${prefixCls}-detail-manual-text`}>
          {formatMessage({ id: `${intlPrefix}.instance` })}
        </span>
        <span
          onClick={linkToInstance}
          className={`${prefixCls}-detail-manual-instance`}
        >
          {record.get('instanceName')}
        </span>
      </li>
    </ul>
  );
}));
