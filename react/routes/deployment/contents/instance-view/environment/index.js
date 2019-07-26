import React, { useContext } from 'react';
import { Tabs } from 'choerodon-ui';
import _ from 'lodash';
import SyncSituation from './sync-situation';
import AssignPermissions from './assign-permissions';
import Store from '../../../stores';
import StatusDot from '../../../components/status-dot';

import './index.less';

const { TabPane } = Tabs;

export default function EnvPage() {
  const {
    store: {
      getPreviewData: {
        connect,
        synchronize,
        name,
      },
    },
    intl: { formatMessage },
    prefixCls,
    intlPrefix,
  } = useContext(Store);
  const tabData = [
    {
      key: 'sync',
      tab: formatMessage({ id: `${intlPrefix}.environment.tabs.sync` }),
      components: <SyncSituation />,
    },
    {
      key: 'assignPermissions',
      tab: formatMessage({ id: `${intlPrefix}.environment.tabs.assignPermissions` }),
      components: <AssignPermissions />,
    },
  ];

  return (
    <div className={`${prefixCls}-environment`}>
      <div className={`${prefixCls}-environment-info`}>
        <StatusDot connect={connect} synchronize={synchronize} width="0.12rem" />
        <span className={`${prefixCls}-environment-title`}>{name}</span>
      </div>
      <Tabs className={`${prefixCls}-environment-tabs`}>
        {
          _.map(tabData, ({ key, tab, components }) => (
            <TabPane tab={tab} key={key}>
              {components}
            </TabPane>
          ))
        }
      </Tabs>,
    </div>
  );
}
