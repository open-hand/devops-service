import React from 'react';
import { PageWrap, PageTab, Page } from '@choerodon/master';
import { observer } from 'mobx-react-lite';
import { useAppTopStore } from '../stores';
import { useServiceDetailStore } from './stores';
import Version from './Version';
import Allocation from './Allocation';
import Share from './Share';

const DetailContent = observer(() => {
  const {
    intlPrefix,
    detailPermissions,
    appServiceStore,
  } = useAppTopStore();
  const {
    intl: { formatMessage },
    detailDs,
  } = useServiceDetailStore();

  // function getContent() {
  //   const { getHasApp } = appServiceStore;
  //   return getHasApp ?  : <Fragment>
  //     <Breadcrumb />
  //     <Content>
  //       <EmptyPage
  //         title={formatMessage({ id: 'empty.title.app' })}
  //         describe={formatMessage({ id: 'empty.tips.app.owner' })}
  //       />
  //     </Content>
  //   </Fragment>;
  // }

  return (<Page
    service={detailPermissions}
  >
    <PageWrap noHeader={[]} cache>
      <PageTab
        title={formatMessage({ id: `${intlPrefix}.version` })}
        tabKey="Version"
        component={Version}
        alwaysShow
      />
      <PageTab
        title={formatMessage({ id: `${intlPrefix}.permission` })}
        tabKey="Allocation"
        component={Allocation}
        alwaysShow={appServiceStore.getProjectRole === 'owner'}
      />
      <PageTab
        title={formatMessage({ id: `${intlPrefix}.share` })}
        tabKey="Share"
        component={Share}
        alwaysShow={appServiceStore.getProjectRole === 'owner' && detailDs.current && detailDs.current.get('type') === 'normal'}
      />
    </PageWrap>
  </Page>);
});

export default DetailContent;
