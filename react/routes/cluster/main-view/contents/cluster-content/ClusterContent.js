import React, { Fragment, lazy, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs, Spin } from 'choerodon-ui';
import { useClusterContentStore } from './stores';
import Modals from './modals';
import PageTitle from '../../../../../components/page-title';
import StatusDot from '../../../../../components/status-dot';


const { TabPane } = Tabs;

const NodeList = lazy(() => import('./node-list'));
const PermissionList = lazy(() => import('./permission-list'));


export default observer((props) => {
  const {
    intlPrefix,
    intl: { formatMessage },
    tabs: {
      NODE_TAB,
      ASSIGN_TAB,
    },
    contentStore,
    ClusterDetailDs,
  } = useClusterContentStore();
  const handleChange = (key) => {
    contentStore.setTabKey(key);
  };
  
  const title = useMemo(() => {
    const record = ClusterDetailDs.current;
    if (record) {
      const name = record.get('name');
      const getStatus = () => {
        const connect = record.get('connect');
        const upgrade = record.get('upgrade');
        if (upgrade) {
          return ['disconnect'];
        } else if (connect) {
          return ['running', 'connect'];
        }
        return ['disconnect'];
      };

      return <Fragment>
        <StatusDot
          getStatus={getStatus}
        />
        <span className="c7ncd-page-title-text">{name}</span>
      </Fragment>;
    }
    return null;
  }, [ClusterDetailDs.current]);
  

  return (
    <Fragment>
      <Modals />
      <PageTitle>
        {title}
      </PageTitle>
      <Tabs
        animated={false}
        activeKey={contentStore.getTabKey}
        onChange={handleChange}
      >
        <TabPane
          key={NODE_TAB}
          tab={formatMessage({ id: `${intlPrefix}.node.list` })}
        >
          <Suspense fallback={<Spin />}>
            <NodeList />
          </Suspense>
        </TabPane>
        <TabPane
          key={ASSIGN_TAB}
          tab={formatMessage({ id: `${intlPrefix}.permission.assign` })}
        >
          <Suspense fallback={<Spin />}>
            <PermissionList />
          </Suspense>
        </TabPane>
      </Tabs>
    </Fragment>);
});
