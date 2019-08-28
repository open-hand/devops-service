import React from 'react';
import { injectIntl } from 'react-intl';
import { getEnvStatus } from '../status-dot';
import StatusTag from '../status-tag';

import './index.less';

function EnvDetail({ record, intl: { formatMessage }, isRecord }) {
  let data = record;
  if (isRecord) {
    data = record ? record.toData() : {};
  }
  const { name,
    connect,
    synchronize,
    synchro,
    active,
    code,
    description,
    clusterName } = data;
  const status = getEnvStatus(connect, synchronize || synchro, active);

  return (
    <ul className="c7ncd-env-detail">
      <li className="c7ncd-env-detail-item">
        <span className="c7ncd-env-detail-item-text">
          {formatMessage({ id: 'c7ncd.env.name' })}:
        </span>
        <span className="c7ncd-env-detail-item-value">{name}</span>
      </li>
      <li className="c7ncd-env-detail-item">
        <span className="c7ncd-env-detail-item-text">
          {formatMessage({ id: 'c7ncd.env.status' })}:
        </span>
        <StatusTag name={formatMessage({ id: status })} colorCode={status} />
      </li>
      <li className="c7ncd-env-detail-item">
        <span className="c7ncd-env-detail-item-text">
          {formatMessage({ id: 'c7ncd.env.code' })}:
        </span>
        <span className="c7ncd-env-detail-item-value">{code}</span>
      </li>
      <li className="c7ncd-env-detail-item">
        <span className="c7ncd-env-detail-item-text">
          {formatMessage({ id: 'c7ncd.env.description' })}:
        </span>
        <span className="c7ncd-env-detail-item-value">{description}</span>
      </li>
      <li className="c7ncd-env-detail-item">
        <span className="c7ncd-env-detail-item-text">
          {formatMessage({ id: 'c7ncd.env.cluster' })}:
        </span>
        <span className="c7ncd-env-detail-item-value">{clusterName}</span>
      </li>
    </ul>
  );
}

EnvDetail.defaultProps = {
  isRecord: true,
};

export default injectIntl(EnvDetail);
