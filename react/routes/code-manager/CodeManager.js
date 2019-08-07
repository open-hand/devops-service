import React from 'react';
import { Page, Header } from '@choerodon/boot';
import { Button } from 'choerodon-ui';

import CodeManagerToolBar from './tool-bar';
import MainView from './main-view';

export default function CodeManager() {
  return <Page>
    {/* <CodeManagerToolBar /> */}
    {/* <CodeManagerHeader /> */}
    <MainView />
  </Page>;
}
