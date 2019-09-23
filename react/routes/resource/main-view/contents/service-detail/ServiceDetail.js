import React from 'react';
import { observer } from 'mobx-react-lite';
import { useResourceStore } from '../../../stores';
import { useNetworkDetailStore } from './stores';
import Modals from './modals';
import PortsList from './PortsList';
import LoadList from './LoadList';
import PodList from './PodList';
import ResourceTitle from '../../components/resource-title';

import './index.less';

const ServiceDetail = observer(() => {
  const { prefixCls } = useResourceStore();
  const { baseInfoDs } = useNetworkDetailStore();

  const record = baseInfoDs.current;
  if (!record) return <span>loading</span>;

  return (
    <div className={`${prefixCls}-service-detail`}>
      <ResourceTitle iconType="router" record={record} />
      <ul className={`${prefixCls}-service-detail-content`}>
        <li><PortsList /></li>
        <li><LoadList /></li>
        <li><PodList /></li>
      </ul>
      <Modals />
    </div>
  );
});

export default ServiceDetail;
