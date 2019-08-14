import React, { Fragment } from 'react';
import { Tooltip } from 'choerodon-ui/pro';

import './index.less';

export default function DetailsModal({ intlPrefix, record, prefixCls, formatMessage }) {
  const { versionName, appServiceVersionId, commandVersionId, commandVersion } = record;
  const status = record.status;
  let statusText = null;

  if (status === 'failed') {
    let code;
    let title = '';
    if (appServiceVersionId) {
      code = 'failed.upgrade';
      title = formatMessage({ id: `${intlPrefix}.instance.status.failed.upgrade.describe` }, { version: commandVersion });
    } else if (appServiceVersionId !== commandVersionId) {
      code = 'failed.deploy';
      title = formatMessage({ id: `${intlPrefix}.instance.status.failed.deploy.describe` }, { version: versionName });
    }
    statusText = <span className={`${prefixCls}-modals-value ${prefixCls}-modals-cell`}>
      <Tooltip placement="bottom" title={title}>
        <span>
          {formatMessage({ id: `${intlPrefix}.instance.status.${code}` })}
        </span>
      </Tooltip>
    </span>;
  } else {
    statusText = <span className={`${prefixCls}-modals-value ${prefixCls}-modals-cell`}>
      {formatMessage({ id: `${intlPrefix}.instance.status.${status}` })}
    </span>;
  }

  return <Fragment>
    <div className={`${prefixCls}-modals-row`}>
      <span
        className={`${prefixCls}-modals-key ${prefixCls}-modals-cell`}
      >{formatMessage({ id: `${intlPrefix}.instance.status` })}：</span>
      {statusText}
    </div>
    <div className={`${prefixCls}-modals-row`}>
      <span className={`${prefixCls}-modals-key ${prefixCls}-modals-cell`}>{formatMessage({ id: 'version' })}：</span>
      <span className={`${prefixCls}-modals-value ${prefixCls}-modals-cell`}>
        {versionName}
      </span>
    </div>
  </Fragment>;
}
