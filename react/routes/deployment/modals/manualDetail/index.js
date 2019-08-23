import React from 'react';
import { injectIntl } from 'react-intl';

import './index.less';

export default injectIntl(({ record, intlPrefix, prefixCls, intl: { formatMessage } }) => (
  <ul className={`${prefixCls}-detail-manual`}>
    <li className={`${prefixCls}-detail-manual-item`}>
      <span className={`${prefixCls}-detail-manual-text`}>
        {formatMessage({ id: `${intlPrefix}.service` })}:
      </span>
      <span>{record.get('appServiceName')}</span>
    </li>
    <li className={`${prefixCls}-detail-manual-item`}>
      <span className={`${prefixCls}-detail-manual-text`}>
        {formatMessage({ id: `${intlPrefix}.version` })}:
      </span>
      <span>{record.get('appServiceVersion')}</span>
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
