import React, { Fragment, useMemo } from 'react';
import { useClusterMainStore } from '../../../stores';
import { useClusterContentStore } from '../stores';
import { useClusterStore } from '../../../../stores';
import Loading from '../../../../../../components/loading';
import EmptyPage from '../../../../../../components/empty-page';

import './index.less';

export default (props) => {
  const {
    clusterStore: {
      getSelectedMenu: { code },
    },
  } = useClusterStore();
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
  } = useClusterContentStore();

  function refresh() {

  }

  function LinkToComponent() {
    setTabKey(COMPONENT_TAB);
  }

  function getContent() {
    if (getGrafanaUrl) {
      return (
        <iframe
          height={700}
          width="100%"
          src={`${getGrafanaUrl}&kiosk=tv&var-cluster=${code}`}
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
};
