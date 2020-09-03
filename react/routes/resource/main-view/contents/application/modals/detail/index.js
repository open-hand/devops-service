import React from 'react';
import { FormattedMessage } from 'react-intl';
import { Tooltip } from 'choerodon-ui';

import './index.less';

export default function ({
  intlPrefix, record, prefixCls, formatMessage,
}) {
  let status;
  if (record.get('fail')) {
    status = 'failed';
  } else if (record.get('synchro') && record.get('active')) {
    status = 'active';
  } else if (record.get('active')) {
    status = 'creating';
  } else {
    status = 'stop';
  }

  return (
    <ul className={`${prefixCls}-application-detail-modal`}>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: `${intlPrefix}.service.status` })}
        </span>
        <FormattedMessage id={status} />
      </li>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: `${intlPrefix}.service.code` })}
        </span>
        <span>{record.get('code')}</span>
      </li>
      <li className="detail-item detail-item-has-url">
        <span className="detail-item-text">
          {record.get('shareProjectName') ? '来源项目' : formatMessage({ id: `${intlPrefix}.service.url` })}
        </span>
        <Tooltip title={record.get('repoUrl')}>
          {
            record.get('shareProjectName') ? (
              <span>{record.get('shareProjectName')}</span>
            ) : (
              <a
                href={record.get('repoUrl')}
                className="detail-item-url"
                target="_blank"
                rel="nofollow me noopener noreferrer"
              >
                <span>{record.get('repoUrl')}</span>
              </a>
            )
          }
        </Tooltip>
      </li>
    </ul>
  );
}
