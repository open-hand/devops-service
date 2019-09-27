import React from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import { Spin } from 'choerodon-ui';
import ResourceTitle from '../../components/resource-title';
import { useResourceStore } from '../../../stores';
import { useMainStore } from '../../stores';
import { useSecretDetailStore } from './stores';
import Modals from './modals';

import './index.less';

const Content = observer(() => {
  const {
    prefixCls,
    intlPrefix,
  } = useResourceStore();
  const { detailDs } = useSecretDetailStore();
  const { childrenStore } = useMainStore();

  childrenStore.setDetailDs(detailDs);

  function renderSecret(value, key) {
    return <div className="secret-detail-section">
      <div className="secret-detail-section-title">
        <span>{key}</span>
      </div>
      <div className="secret-detail-section-content">
        <span>{value}</span>
      </div>
    </div>;
  }

  function getContent() {
    const record = detailDs.current;
    return record ? map(record.get('value'), renderSecret) : '暂无数据';
  }

  return (
    <div className={`${prefixCls}-secret-detail`}>
      <ResourceTitle
        record={detailDs.current}
        iconType="vpn_key"
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
