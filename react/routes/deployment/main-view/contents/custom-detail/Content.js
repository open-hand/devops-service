import React, { Fragment, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
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
  if (!record) return;

  function refresh() {
    detailDs.query();
  }

  return (
    <div className={`${prefixCls}-custom-detail`}>
      <Modals />
      <div className={`${prefixCls}-detail-content-title`}>
        <Icon type="filter_b_and_w" className={`${prefixCls}-detail-content-title-icon`} />
        <span>{record.get('name')}</span>
      </div>
      <div className={`${prefixCls}-detail-content-section-title`}>
        <span>Description</span>
      </div>
      <pre className="custom-detail-section-content">
        {record.get('description')}
      </pre>
    </div>
  );
});

export default Content;
