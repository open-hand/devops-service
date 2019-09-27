import React, { Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import keys from 'lodash/keys';
import { Spin } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';
import { useNetworkDetailStore } from './stores';
import Modals from './modals';
import PodList from './PodList';
import ResourceTitle from '../../components/resource-title';

import './index.less';

const ServiceDetail = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
  } = useResourceStore();
  const { baseInfoDs } = useNetworkDetailStore();

  function getPort({ port, targetPort, protocol }) {
    return <ul className="service-detail-port-list" key={port}>
      <li className="service-detail-port-item">
        <span className="service-detail-port-item-text">{port}</span>
        {formatMessage({ id: 'port' })}
      </li>
      <li className="service-detail-port-item service-detail-arrow-item">
        <span className="service-detail-port-item-arrow">→</span>
      </li>
      <li className="service-detail-port-item">
        <span className="service-detail-port-item-text">{protocol}</span>
        {formatMessage({ id: 'protocol' })}
      </li>
      <li className="service-detail-port-item service-detail-arrow-item">
        <span className="service-detail-port-item-arrow">→</span>
      </li>
      <li className="service-detail-port-item">
        <span className="service-detail-port-item-text">{targetPort}</span>
        {formatMessage({ id: `${intlPrefix}.target.port` })}
      </li>
    </ul>;
  }

  function getBurden({ code, status, podCount, podRunningCount, lastUpdateDate, objectVersionNumber }) {
    return <ul className="service-detail-load-list" key={code}>
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
            {formatMessage({ id: status })}
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
    </ul>;
  }

  function getEndpoints(endPoints) {
    return (
      <div>
        <span className="service-detail-endpoints">{formatMessage({ id: `${intlPrefix}.target.ip` })}:</span>
        <span>{keys(endPoints).join()}</span>
      </div>
    );
  }

  function getPorts() {
    const record = baseInfoDs.current;
    let ports;
    let burden;
    let endPoints;

    if (record) {
      ports = record.get('config').ports;
      burden = record.get('target').instances;
      endPoints = record.get('target').endPoints;
    }

    return <div>
      <div className={`${prefixCls}-detail-content-section-title`}>
        {formatMessage({ id: 'port' })}
      </div>
      <div className="detail-content-section-detail">
        {ports && ports.length ? map(ports, getPort) : formatMessage({ id: 'nodata' })}
      </div>
      {burden && burden.length ? (<Fragment>
        <div className={`${prefixCls}-detail-content-section-title`}>
          {formatMessage({ id: `${intlPrefix}.load` })}
        </div>
        <div className="detail-content-section-detail">
          {map(burden, getBurden)}
        </div>
      </Fragment>) : null}
      {endPoints ? (<Fragment>
        <div className={`${prefixCls}-detail-content-section-title`}>
          Endpoints
        </div>
        <div className="detail-content-section-detail">
          {getEndpoints(endPoints)}
        </div>
      </Fragment>) : (record && <PodList />)}
    </div>;
  }

  return (
    <div className={`${prefixCls}-service-detail`}>
      <ResourceTitle
        record={baseInfoDs.current}
        iconType="router"
      />
      <div className={`${prefixCls}-service-detail-content`}>
        <Spin spinning={baseInfoDs.status === 'loading'}>
          {getPorts()}
        </Spin>
      </div>
      <Modals />
    </div>
  );
});

export default ServiceDetail;
