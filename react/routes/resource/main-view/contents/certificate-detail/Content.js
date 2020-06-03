import React, { Fragment } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import { Spin } from 'choerodon-ui';
import { Permission } from '@choerodon/boot';
import ResourceTitle from '../../components/resource-title';
import { useResourceStore } from '../../../stores';
import { useCertDetailStore } from './stores';
import Modals from './modals';

import './index.less';

const Content = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    intl: { formatMessage },
  } = useResourceStore();
  const { detailDs } = useCertDetailStore();

  function getContent() {
    const record = detailDs.current;
    let dnsNames;
    let ingresses;
    let commonName;
    if (record) {
      dnsNames = record.get('DNSNames');
      ingresses = record.get('ingresses');
      commonName = record.get('commonName');
    }

    const dnsNode = dnsNames ? map(dnsNames, (item) => (
      <li className={`${prefixCls}-detail-section-li`}>
        <span className="detail-section-li-text">DNSNames:&nbsp;</span>
        <span>{item}</span>
      </li>
    )) : <span style={{ color: 'rgba(0,0,0,.65)' }}>{formatMessage({ id: 'nodata' })}</span>;
    const ingressNode = ingresses && ingresses.length > 0 ? map(ingresses, (item) => (
      <li className={`${prefixCls}-detail-section-li`}>
        <span>{item}</span>
      </li>
    )) : <span style={{ color: 'rgba(0,0,0,.65)' }}>{formatMessage({ id: 'nodata' })}</span>;

    return <Fragment>
      <div>
        <div className={`${prefixCls}-detail-content-section-title`}>
          <FormattedMessage id={`${intlPrefix}.domains`} />
        </div>
        <div className={`${prefixCls}-detail-content-section-name`}>
          <span>CommonName:&nbsp;</span>
          <span>{commonName}</span>
        </div>
        <ul className={`${prefixCls}-detail-section-ul`}>
          {dnsNode}
        </ul>
      </div>
      <div>
        <div className={`${prefixCls}-detail-content-section-title`}>
          <FormattedMessage id={`${intlPrefix}.current.domains`} />
        </div>
        <ul className={`${prefixCls}-detail-section-ul`}>
          {ingressNode}
        </ul>
      </div>
    </Fragment>;
  }

  return (
    <Permission
      service={['choerodon.code.project.deploy.app-deployment.resource.ps.cert-detail']}
    >
      <div className={`${prefixCls}-certificate-detail`}>
        <ResourceTitle
          iconType="class"
          record={detailDs.current}
          statusKey="commandStatus"
        />
        <Spin spinning={detailDs.status === 'loading'}>
          {getContent()}
        </Spin>
        <Modals />
      </div>
    </Permission>
  );
});

export default Content;
