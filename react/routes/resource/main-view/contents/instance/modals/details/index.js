import React, { Fragment } from 'react';

import './index.less';

const UPDATE_FAILED = 'failed.upgrade';
const DEPLOY_FAILED = 'failed.deploy';

export default function DetailsModal({ intlPrefix, record, prefixCls, formatMessage }) {
  function getStatusCode() {
    let status;
    if (record) {
      status = record.get('status');

      if (!status) {
        return '';
      } else if (status === 'failed') {
        const appServiceVersionId = record.get('appServiceVersionId');
        const commandVersionId = record.get('commandVersionId');
        if (appServiceVersionId) {
          return UPDATE_FAILED;
        } else if (appServiceVersionId !== commandVersionId) {
          return DEPLOY_FAILED;
        }
      } else {
        return status;
      }
    }
    return '';
  }

  function getStatus() {
    const statusCode = getStatusCode();
    const commandVersion = record.get('commandVersion');
    if (statusCode === UPDATE_FAILED) {
      return formatMessage({ id: `${intlPrefix}.instance.status.failed.upgrade.describe` }, { version: commandVersion });
    } else if (statusCode === DEPLOY_FAILED) {
      return formatMessage({ id: `${intlPrefix}.instance.status.failed.deploy.describe` }, { version: commandVersion });
    } else {
      return statusCode && <span className={`${prefixCls}-modals-value ${prefixCls}-modals-cell`}>
        {formatMessage({ id: `${intlPrefix}.instance.status.${statusCode}` })}
      </span>;
    }
  }

  function getVersion() {
    const statusCode = getStatusCode();
    if (!statusCode) return '';
    const versionName = record.get('versionName');
    return versionName || '-';
  }

  return <Fragment>
    <div className={`${prefixCls}-modals-row`}>
      <span className={`${prefixCls}-modals-key ${prefixCls}-modals-cell`}>
        {formatMessage({ id: `${intlPrefix}.instance.status` })}
      </span>
      {getStatus()}
    </div>
    <div className={`${prefixCls}-modals-row`}>
      <span className={`${prefixCls}-modals-key ${prefixCls}-modals-cell`}>
        {formatMessage({ id: 'version' })}
      </span>
      <span className={`${prefixCls}-modals-value ${prefixCls}-modals-cell`}>
        {getVersion()}
      </span>
    </div>
  </Fragment>;
}
