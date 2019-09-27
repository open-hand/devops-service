import React from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import { Spin } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';
import { useConfigDetailStore } from './stores';
import Modals from './modals';
import ResourceTitle from '../../components/resource-title';

import './index.less';

const Content = observer(() => {
  const { prefixCls, intlPrefix } = useResourceStore();
  const { detailDs } = useConfigDetailStore();

  function getContent() {
    const record = detailDs.current;
    return record ? map(record.get('value'), (value, key) => (
      <div className="configMap-detail-section">
        <div className="configMap-detail-section-title">
          <span>{key}</span>
        </div>
        <div className="configMap-detail-section-content">
          <pre className="configMap-detail-section-content-pre">{value}</pre>
        </div>
      </div>
    )) : '暂无数据';
  }

  return (
    <div className={`${prefixCls}-configMap-detail`}>
      <ResourceTitle
        iconType="compare_arrows"
        record={detailDs.current}
        statusKey="commandStatus"
      />
      <div className={`${prefixCls}-detail-content-section-title`}>
        <FormattedMessage id={`${intlPrefix}.key.value`} />
      </div>
      <Spin spinning={detailDs.status === 'loading'}>
        {getContent()}
      </Spin>
      <Modals />
    </div>
  );
});

export default Content;
