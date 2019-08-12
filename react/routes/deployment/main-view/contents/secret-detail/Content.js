import React, { Fragment, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import { Icon } from 'choerodon-ui';
import { useDeploymentStore } from '../../../stores';
import { useCustomDetailStore } from './stores';
import Modals from './modals';

import './index.less';

const Content = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    deploymentStore: { getSelectedMenu: { menuId, parentId } },
  } = useDeploymentStore();
  const {
    detailDs,
    intl: { formatMessage },
  } = useCustomDetailStore();

  const record = detailDs.current;
  if (!record) return <span>loading</span>;

  function refresh() {
    detailDs.query();
  }

  return (
    <div className={`${prefixCls}-secret-detail`}>
      <Modals />
      <div className="detail-content-title">
        <Icon type="vpn_key" className="detail-content-title-icon" />
        <span>{record.get('name')}</span>
      </div>
      <div className="detail-content-section-title">
        <FormattedMessage id={`${intlPrefix}.key.value`} />
      </div>
      {map(record.get('value'), (value, key) => (
        <div className="secret-detail-section">
          <div className="secret-detail-section-title">
            <span>{key}</span>
          </div>
          <div className="secret-detail-section-content">
            <span>{value}</span>
          </div>
        </div>
      ))}
    </div>
  );
});

export default Content;
