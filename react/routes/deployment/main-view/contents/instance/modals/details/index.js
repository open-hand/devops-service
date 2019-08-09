import React, { Fragment } from 'react';

import './index.less';

export default function DetailsModal({ intlPrefix, record, prefixCls, formatMessage }) {
  const { versionName, appServiceVersionId, commandVersionId, commandVersion } = record;
  let status = record.status;
  if (status === 'failed') {
    status = appServiceVersionId ? 'failed.upgrade' : 'failed.deploy';
  }

  return <Fragment>
    <div className={`${prefixCls}-modals-row`}>
      <span
        className={`${prefixCls}-modals-key ${prefixCls}-modals-cell`}
      >{formatMessage({ id: `${intlPrefix}.instance.status` })}：</span>
      <span className={`${prefixCls}-modals-value ${prefixCls}-modals-cell`}>
        {formatMessage({ id: `${intlPrefix}.instance.status.${status}` })}
      </span>
    </div>
    <div className={`${prefixCls}-modals-row`}>
      <span className={`${prefixCls}-modals-key ${prefixCls}-modals-cell`}>{formatMessage({ id: 'version' })}：</span>
      <span className={`${prefixCls}-modals-value ${prefixCls}-modals-cell`}>
        {versionName}
      </span>
    </div>
  </Fragment>;
}
