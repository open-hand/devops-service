import React from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Icon } from 'choerodon-ui';
import { useDeploymentStore } from '../../../stores';
import { useNetworkDetailStore } from './stores';
import Modals from './modals';
import PortsList from './PortsList';
import LoadList from './LoadList';

import './index.less';

const ServiceDetail = observer(() => {
  const {
    prefixCls,
    intlPrefix,
  } = useDeploymentStore();
  const {
    baseInfoDs,
    intl: { formatMessage },
  } = useNetworkDetailStore();

  const record = baseInfoDs.current;
  if (!record) return <span>loading</span>;
 

  return (
    <div className={`${prefixCls}-service-detail`}>
      <Modals />
      <div className={`${prefixCls}-detail-content-title`}>
        <Icon type="router" className={`${prefixCls}-detail-content-title-icon`} />
        <span>{record && record.get('name')}</span>
      </div>
      <ul className={`${prefixCls}-service-detail-content`}>
        <li><PortsList /></li>
        <li><LoadList /></li>
        <li>
          <div className={`${prefixCls}-detail-content-section-title`}>
            <FormattedMessage id={`${intlPrefix}.pods`} />
          </div>
        </li>
      </ul>
    </div>
  );
});

export default ServiceDetail;
