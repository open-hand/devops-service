import React, { useEffect } from 'react';
import { Page } from '@choerodon/master';
import DevPipelineStore from './stores/DevPipelineStore';
import MainView from './main-view';

export default function CodeManager() {
  useEffect(() => {
    DevPipelineStore.setAppData([]);
    DevPipelineStore.setRecentApp([]);
    DevPipelineStore.setSelectApp(null);
  }, []);
  return <Page>
    <MainView />
  </Page>;
}
