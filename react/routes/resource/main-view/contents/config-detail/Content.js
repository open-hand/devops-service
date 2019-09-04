import React from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Icon } from 'choerodon-ui';
import map from 'lodash/map';
import { useResourceStore } from '../../../stores';
import { useConfigDetailStore } from './stores';
import Modals from './modals';

import './index.less';

const Content = observer(() => {
  const {
    prefixCls,
    intlPrefix,
  } = useResourceStore();
  const {
    detailDs,
  } = useConfigDetailStore();

  const record = detailDs.current;
  if (!record) return;

  return (
    <div className={`${prefixCls}-configMap-detail`}>
      <Modals />
      <div className={`${prefixCls}-detail-content-title`}>
        <Icon type="compare_arrows" className={`${prefixCls}-detail-content-title-icon`} />
        <span>{record.get('name')}</span>
      </div>
      <div className={`${prefixCls}-detail-content-section-title`}>
        <FormattedMessage id={`${intlPrefix}.key.value`} />
      </div>
      {map(record.get('value'), (value, key) => (
        <div className="configMap-detail-section">
          <div className="configMap-detail-section-title">
            <span>{key}</span>
          </div>
          <div className="configMap-detail-section-content">
            <pre className="configMap-detail-section-content-pre">{value}</pre>
          </div>
        </div>
      ))}
    </div>
  );
});

export default Content;
