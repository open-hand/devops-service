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
import StatusIcon from '../../../../../components/StatusIcon';
import { useDeploymentStore } from '../../../stores';
import Modals from './modals';

import './index.less';

const { Column } = Table;

const ServiceDetail = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    deploymentStore: { getSelectedMenu: { parentId } },
  } = useDeploymentStore();
 

  return (
    <div>
      <Modals />
      <h1>网络详情页面</h1>
    </div>
  );
});

export default ServiceDetail;
