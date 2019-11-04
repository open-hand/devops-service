import React, { Fragment, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { useClusterMainStore } from '../../../stores';
import { useClusterContentStore } from '../stores';
import Loading from '../../../../../../components/loading';
import EmptyPage from '../../../../../../components/empty-page';

import './index.less';

export default observer((props) => {
  const {
    intlPrefix,
    prefixCls,
  } = useClusterMainStore();
  const {
    contentStore: {
      getGrafanaUrl,
      setTabKey,
    },
    formatMessage,
    tabs: {
      COMPONENT_TAB,
    },
    ClusterDetailDs,
  } = useClusterContentStore();

  function refresh() {

  }

  function LinkToComponent() {
    setTabKey(COMPONENT_TAB);
  }

  function getContent() {
    if (getGrafanaUrl) {
      const code = ClusterDetailDs.current ? ClusterDetailDs.current.get('code') : '';
      return (
        <iframe
          height={700}
          width="100%"
          src={`${getGrafanaUrl}?kiosk=tv${code ? `&var-cluster=${code}` : ''}`}
          title="grafana"
          frameBorder={0}
          sandbox
        />
      );
    } else {
      return (
        <EmptyPage
          title={formatMessage({ id: `${intlPrefix}.monitor.install` })}
          describe={formatMessage({ id: `${intlPrefix}.monitor.empty.des` })}
          btnText={formatMessage({ id: `${intlPrefix}.monitor.link` })}
          onClick={LinkToComponent}
          access
        />
      );
    }
  }

  return (
    <div className={`${prefixCls}-monitor-wrap`}>
      {getContent()}
    </div>
  );
});
