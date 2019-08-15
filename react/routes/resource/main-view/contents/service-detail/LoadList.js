import React, { Fragment } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import { useResourceStore } from '../../../stores';
import { useNetworkDetailStore } from './stores';

const PortsList = observer(() => {
  const {
    prefixCls,
    intlPrefix,
  } = useResourceStore();
  const {
    baseInfoDs,
    intl: { formatMessage },
  } = useNetworkDetailStore();

  const record = baseInfoDs.current;

  return (
    record.get('target').instances.length && (
      <Fragment>
        <div className={`${prefixCls}-detail-content-section-title`}>
          <FormattedMessage id={`${intlPrefix}.load`} />
        </div>
        <div className="detail-content-section-detail">
          {map(record.get('target').instances, ({ code, status, podCount, podRunningCount, lastUpdateDate, objectVersionNumber }) => (
            <ul className="service-detail-load-list">
              <li className="service-detail-load-item">
                <span className="service-detail-load-item-code">{code}</span>
                <span className="service-detail-load-item-key">
                  {formatMessage({ id: 'deployment' })}
                </span>
              </li>
              <li className="service-detail-load-item">
                <div>
                  <span className="service-detail-load-item-key">
                    {formatMessage({ id: 'status' })}:
                  </span>
                  <div className="service-detail-load-item-status">
                    <span className={`service-detail-load-item-status-dot service-detail-load-item-status-dot-${status}`} />
                    <FormattedMessage id={status} />
                    <span>({podRunningCount}/{podCount})</span>
                  </div>
                </div>
                <div>
                  <span className="service-detail-load-item-key">
                    {formatMessage({ id: 'updateDate' })}:
                  </span>
                  <span>{lastUpdateDate || '-'}</span>
                </div>
              </li>
              <li className="service-detail-load-item">
                <span className="service-detail-load-item-key">
                  {formatMessage({ id: `${intlPrefix}.change.number` })}:
                </span>
                <span>{objectVersionNumber || '-'}</span>
              </li>
            </ul>
          ))}
        </div>
      </Fragment>
    )
  );
});

export default PortsList;
