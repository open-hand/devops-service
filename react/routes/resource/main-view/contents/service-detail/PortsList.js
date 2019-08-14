import React, { Fragment } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import { useDeploymentStore } from '../../../stores';
import { useNetworkDetailStore } from './stores';

const PortsList = observer(() => {
  const {
    prefixCls,
    intlPrefix,
  } = useDeploymentStore();
  const {
    baseInfoDs,
  } = useNetworkDetailStore();

  const record = baseInfoDs.current;

  return (
    <Fragment>
      <div className={`${prefixCls}-detail-content-section-title`}>
        <FormattedMessage id="port" />
      </div>
      <div className="detail-content-section-detail">
        {map(record.get('config').ports, ({ port, targetPort, protocol }) => (
          <ul className="service-detail-port-list">
            <li className="service-detail-port-item">
              <span className="service-detail-port-item-text">{port}</span>
              <FormattedMessage id="port" />
            </li>
            <li className="service-detail-port-item service-detail-arrow-item">
              <span className="service-detail-port-item-arrow">→</span>
            </li>
            <li className="service-detail-port-item">
              <span className="service-detail-port-item-text">{protocol}</span>
              <FormattedMessage id="protocol" />
            </li>
            <li className="service-detail-port-item service-detail-arrow-item">
              <span className="service-detail-port-item-arrow">→</span>
            </li>
            <li className="service-detail-port-item">
              <span className="service-detail-port-item-text">{targetPort}</span>
              <FormattedMessage id={`${intlPrefix}.target.port`} />
            </li>
          </ul>
        ))}
      </div>
    </Fragment>
  );
});

export default PortsList;
