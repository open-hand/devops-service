import React, { Fragment, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/boot';
import {
  Tooltip,
  Icon,
  Popover,
} from 'choerodon-ui';
import { Table } from 'choerodon-ui/pro';
import _ from 'lodash';
import classnames from 'classnames';
import { useDeploymentStore } from '../../../stores';
import { useNetworkDetailStore } from './stores';
import Modals from './modals';

import './index.less';

const ServiceDetail = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    deploymentStore: { getSelectedMenu: { parentId } },
  } = useDeploymentStore();
  const {
    baseInfoDs,
    intl: { formatMessage },
  } = useNetworkDetailStore();

  const record = baseInfoDs.current;
  // if (!record) return <span>loading</span>;
 

  return (
    <div className={`${prefixCls}-service-detail`}>
      <Modals />
      <div className="detail-content-title">
        <Icon type="router" className="detail-content-title-icon" />
        <span>{record.get('name')}</span>
      </div>
      <ul className={`${prefixCls}-service-detail-content`}>
        <li>
          <div className="detail-content-section-title">
            <FormattedMessage id="port" />
          </div>
        </li>
        <li>
          <div className="detail-content-section-title">
            <FormattedMessage id={`${intlPrefix}.load`} />
          </div>
        </li>
        <li>
          <div className="detail-content-section-title">
            <FormattedMessage id={`${intlPrefix}.pods`} />
          </div>
        </li>
      </ul>
    </div>
  );
});

export default ServiceDetail;
