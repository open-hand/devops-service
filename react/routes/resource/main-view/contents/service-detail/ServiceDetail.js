import React from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Icon, Tooltip } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';
import { useNetworkDetailStore } from './stores';
import Modals from './modals';
import PortsList from './PortsList';
import LoadList from './LoadList';
import PodList from './PodList';
import ResourceTitle from '../../components/resource-title';

import './index.less';

const ServiceDetail = observer(() => {
  const {
    prefixCls,
    intlPrefix,
  } = useResourceStore();
  const {
    baseInfoDs,
    intl: { formatMessage },
  } = useNetworkDetailStore();

  const record = baseInfoDs.current;
  if (!record) return <span>loading</span>;


  return (
    <div className={`${prefixCls}-service-detail`}>
      <Modals />
      <ResourceTitle iconType="router" record={record} />
      <ul className={`${prefixCls}-service-detail-content`}>
        <li><PortsList /></li>
        <li><LoadList /></li>
        <li><PodList /></li>
      </ul>
    </div>
  );
});

export default ServiceDetail;
