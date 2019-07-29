import React, { Fragment, useContext, useMemo, useState, lazy, Suspense, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { DataSet } from 'choerodon-ui/pro';
import { Tabs, Icon } from 'choerodon-ui';
import BaseInfoDataSet from './stores/BaseInfoDataSet';
import Store from '../../../stores';

import './index.less';

const { TabPane } = Tabs;
const NET_TAB = 'net';
const MAPPING_TAB = 'mapping';
const CIPHER_TAB = 'cipher';

const CipherContent = lazy(() => import('./cipher'));
const MappingContent = lazy(() => import('./mapping'));
const NetContent = lazy(() => import('./net'));

const AppContent = observer(() => {
  const {
    selectedMenu: { menuId },
    intl: { formatMessage },
    prefixCls,
    intlPrefix,
    AppState: { currentMenuType: { id } },
  } = useContext(Store);
  const [activeKey, setActiveKey] = useState(NET_TAB);
  const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet(id, menuId)), [id, menuId]);
  const baseInfo = baseInfoDs.data;

  const getTitle = useMemo(() => {
    if (baseInfo.length) {
      const record = baseInfo[0];
      const name = record.get('name');

      return <Fragment>
        <Icon type="widgets" />
        <span className={`${prefixCls}-environment-title`}>{name}</span>
      </Fragment>;
    }
    return null;
  }, [baseInfo, prefixCls]);
  const handleChange = useCallback((key) => {
    setActiveKey(key);
  }, []);

  return (
    <div className={`${prefixCls}-environment`}>
      <div className={`${prefixCls}-environment-info`}>
        {getTitle}
      </div>
      <Tabs
        className={`${prefixCls}-environment-tabs`}
        animated={false}
        activeKey={activeKey}
        onChange={handleChange}
      >
        <TabPane
          key={NET_TAB}
          tab={formatMessage({ id: `${intlPrefix}.instance.tabs.cases` })}
        >
          <Suspense fallback={<div>loading</div>}>
            <NetContent />
          </Suspense>
        </TabPane>
        <TabPane
          key={MAPPING_TAB}
          tab={formatMessage({ id: `${intlPrefix}.instance.tabs.cases` })}
        >
          <Suspense fallback={<div>loading</div>}>
            <MappingContent />
          </Suspense>
        </TabPane>
        <TabPane
          key={CIPHER_TAB}
          tab={formatMessage({ id: `${intlPrefix}.instance.tabs.cases` })}
        >
          <Suspense fallback={<div>loading</div>}>
            <CipherContent />
          </Suspense>
        </TabPane>
      </Tabs>
    </div>
  );
});

export default AppContent;
