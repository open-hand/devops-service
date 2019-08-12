import React, { Fragment, useState, useMemo } from 'react';
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

  const record = useMemo(() => detailDs.current, []);

  function refresh() {
    detailDs.query();
  }

  return (
    <div className={`${prefixCls}-certificate-detail`}>
      <Modals />
      <div className="detail-content-title">
        <Icon type="edit" className="detail-content-title-icon" />
      </div>
    </div>
  );
});

export default Content;
