import React, { Fragment, useState, useMemo } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Icon } from 'choerodon-ui';
import map from 'lodash/map';
import { useResourceStore } from '../../../stores';
import { useCertDetailStore } from './stores';
import Modals from './modals';

import './index.less';

const Content = observer(() => {
  const {
    prefixCls,
    intlPrefix,
  } = useResourceStore();
  const { detailDs } = useCertDetailStore();

  const record = detailDs.current;
  if (!record) return <span>loading</span>;

  return (
    <div className={`${prefixCls}-certificate-detail`}>
      <Modals />
      <div className={`${prefixCls}-detail-content-title`}>
        <Icon type="class" className={`${prefixCls}-detail-content-title-icon`} />
        <span>{record.get('name')}</span>
      </div>
      <div>
        <div className={`${prefixCls}-detail-content-section-title`}>
          <FormattedMessage id={`${intlPrefix}.domains`} />
        </div>
        <div className={`${prefixCls}-detail-content-section-name`}>
          <span>CommonName:&nbsp;</span>
          <span>{record.get('commonName')}</span>
        </div>
        <ul className={`${prefixCls}-detail-section-ul`}>
          {map(record.get('DNSNames'), (item) => (
            <li className={`${prefixCls}-detail-section-li`}>
              <span className="detail-section-li-text">DNSNames:&nbsp;</span>
              <span>{item}</span>
            </li>
          ))}
        </ul>
      </div>
      <div>
        <div className={`${prefixCls}-detail-content-section-title`}>
          <FormattedMessage id={`${intlPrefix}.current.domains`} />
        </div>
        <ul className={`${prefixCls}-detail-section-ul`}>
          {map(record.get('ingresses'), (item) => (
            <li className={`${prefixCls}-detail-section-li`}>
              <span>{item}</span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
});

export default Content;
