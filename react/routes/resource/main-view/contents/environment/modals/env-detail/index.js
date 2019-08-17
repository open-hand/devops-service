import React from 'react';
import { FormattedMessage } from 'react-intl';

import './index.less';

const STATUS_OPERATING = 'operating';
const STATUS_RUNNING = 'running';
const STATUS_DISCONNECT = 'disconnect';


export default function ({ intlPrefix, record, prefixCls, formatMessage }) {
  let status;
  if (!record.get('synchronize')) {
    status = STATUS_OPERATING;
  } else if (record.get('connect')) {
    status = STATUS_RUNNING;
  } else {
    status = STATUS_DISCONNECT;
  }

  return (
    <ul className={`${prefixCls}-environment-detail-modal`}>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: `${intlPrefix}.environment.status` })}:
        </span>
        <FormattedMessage id={status} />
      </li>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: `${intlPrefix}.environment.code` })}:
        </span>
        <span className="detail-item-value">{record.get('code')}</span>
      </li>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: `${intlPrefix}.environment.description` })}:
        </span>
        <span className="detail-item-value">{record.get('description')}</span>
      </li>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: `${intlPrefix}.environment.cluster` })}:
        </span>
        <span className="detail-item-value">{record.get('clusterName')}</span>
      </li>
    </ul>
  );
}
