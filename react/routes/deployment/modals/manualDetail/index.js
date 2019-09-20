import React from 'react';
import { injectIntl } from 'react-intl';

import './index.less';
import MouserOverWrapper from '../../../../components/MouseOverWrapper';

export default injectIntl(({ record, intlPrefix, prefixCls, intl: { formatMessage } }) => (
  <ul className={`${prefixCls}-detail-manual`}>
    <li className={`${prefixCls}-detail-manual-item`}>
      <span className={`${prefixCls}-detail-manual-text`}>
        {formatMessage({ id: `${intlPrefix}.service` })}:
      </span>
      <span>{record.get('appServiceName')}</span>
    </li>
    <li className={`${prefixCls}-detail-manual-item ${prefixCls}-detail-manual-item-flex`}>
      <span className={`${prefixCls}-detail-manual-text`}>
        {formatMessage({ id: `${intlPrefix}.version` })}:
      </span>
      <MouserOverWrapper text={record.get('appServiceVersion')} width="230px">
        <span>{record.get('appServiceVersion')}</span>
      </MouserOverWrapper>
    </li>
    <li className={`${prefixCls}-detail-manual-item`}>
      <span className={`${prefixCls}-detail-manual-text`}>
        {formatMessage({ id: `${intlPrefix}.env` })}:
      </span>
      <span>{record.get('envName')}</span>
    </li>
    <li className={`${prefixCls}-detail-manual-item`}>
      <span className={`${prefixCls}-detail-manual-text`}>
        {formatMessage({ id: `${intlPrefix}.instance` })}:
      </span>
      <span>{record.get('instanceName')}</span>
    </li>
  </ul>
));
