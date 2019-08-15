import React, { Fragment, lazy, Suspense, useCallback, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Row, Col, Card, Icon } from 'choerodon-ui';
import { useEnvironmentStore } from './stores';
import { useResourceStore } from '../../../stores';
import StatusDot from '../../components/status-dot';
import PrefixTitle from '../../components/prefix-title';
// import Modals from './modals';
import './index.less';


const color = {
  running: {
    bfcolor: 'rgba(0, 191, 165, 1)',
    bgcolor: 'rgba(0, 191, 165, 0.2)',
    status: 'running',
  },
  operating: {
    bfcolor: 'rgba(77,144,254,1)',
    bgcolor: 'rgba(77,144,254,0.2)',
    status: 'operating',
  },
  stopped: {
    bfcolor: 'rgba(0,0,0,0.36)',
    bgcolor: 'rgba(0,0,0,0.04)',
    status: 'stopped',
  },
  failed: {
    bfcolor: 'rgba(244,67,54,1)',
    bgcolor: 'rgba(244,67,54, 0.2)',
    status: 'failed',
  },
};

let formatMessagefun;

const EnvContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    intl: { formatMessage },
  } = useResourceStore();
  formatMessagefun = formatMessage;
  const {
    baseInfoDs,
    resourceCountDs,
  } = useEnvironmentStore();

  const baseInfo = baseInfoDs.data;
  const countData = resourceCountDs.data;
  const title = useMemo(() => {
    if (baseInfo.length) {
      const record = baseInfo[0];
      const name = record.get('name');
      const connect = record.get('connect');
      const synchronize = record.get('synchronize');

      return <Fragment>
        <StatusDot
          connect={connect}
          synchronize={synchronize}
        />
        <span className={`${prefixCls}-title-text`}>{name}</span>
      </Fragment>;
    }
    return null;
  }, [baseInfo, prefixCls, countData]);

  const { resourceCount, statusCount } = useMemo(() => {
    let resource = null;
    let status = null;
    if (!countData.length) {
      return {
        resourceCount: resource, statusCount: status,
      };
    }
    const record = countData[0];
    resource = (
      <div className="card-content">
        <ItemNumberByResource count={record.get('instanceCount')} name={formatMessage({ id: 'instance' })} />
        <ItemNumberByResource count={record.get('serviceCount')} name={formatMessage({ id: 'network.header.title' })} />
        <ItemNumberByResource count={record.get('ingressCount')} name={formatMessage({ id: 'ingress' })} />
        <ItemNumberByResource count={record.get('certificationCount')} name={formatMessage({ id: 'ctf.head' })} />
        <ItemNumberByResource count={record.get('configMapCount')} name={formatMessage({ id: 'c7ncd.deployment.application.tabs.mapping' })} />
        <ItemNumberByResource count={record.get('secretCount')} name={formatMessage({ id: 'c7ncd.deployment.application.tabs.cipher' })} />
      </div>
    );
    status = (
      <div className="card-content card-content-right">
        <ItemNumberByStatus color={color.running} count={record.get('runningInstanceCount')} />
        <ItemNumberByStatus color={color.operating} count={record.get('operatingInstanceCount')} />
        <ItemNumberByStatus color={color.stopped} count={record.get('stoppedInstanceCount')} />
        <ItemNumberByStatus color={color.failed} count={record.get('failedInstanceCount')} />
      </div>
    );
    return {
      resourceCount: resource, statusCount: status,
    };
  });

  return (
    <div className={`${prefixCls}-resource-environment`}>
      {/* <Modals /> */}
      <PrefixTitle
        prefixCls={prefixCls}
        fallback={!title}
      >
        {title}
      </PrefixTitle>
      <Row gutter={16}>
        <Col span={14}>
          <div className="card">
            <div className="card-title">部署资源</div>
            {resourceCount}
          </div>
        </Col>
        <Col span={10}>
          <div className="card">
            <div className="card-title">实例状态</div>
            {statusCount}
          </div>
        </Col>
      </Row>
    </div>
  );
});


function ItemNumberByResource(props) {
  const { count, name } = props;
  let countstr = count;
  if (count < 10) {
    countstr = `${count}`;
  }
  return (
    <div className="item">
      <div className="icon">
        <Icon type="project_filled" />
      </div>
      <div className="number">{countstr}</div>
      <div className="name">{name}</div>
    </div>
  );
}

function ItemNumberByStatus(props) {
  const { color: { bfcolor, bgcolor, status }, count } = props;

  return (
    <div className="item item-box">
      <div className="top" style={{ borderColor: bfcolor, background: bgcolor }}>
        <span style={{ color: bfcolor }}>{count}</span>
      </div>
      <div className="bottom">
        <span>{formatMessagefun({ id: status })}</span>
      </div>
    </div>
  );
}

export default EnvContent;
