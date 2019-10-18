import React, { Fragment } from 'react';
import { Header, Content, Breadcrumb } from '@choerodon/boot';
import { Button } from 'choerodon-ui';
import EmptyPage from '../../../components/empty-page';
import Loading from '../../../components/loading';
import { useAppTopStore } from '../stores';

export function EmptyLoading({ formatMessage }) {
  return <Fragment>
    <Header>
      <Button
        icon="refresh"
        type="primary"
        funcType="flat"
      >
        {formatMessage({ id: 'refresh' })}
      </Button>
    </Header>
    <Breadcrumb />
    <Content>
      <Loading display />
    </Content>
  </Fragment>;
}

export default function EmptyShown() {
  const {
    intl: { formatMessage },
    AppState: {
      currentMenuType: {
        id: projectId,
      },
    },
    appServiceStore,
  } = useAppTopStore();

  function refresh() {
    appServiceStore.checkHasApp(projectId);
  }

  return <Fragment>
    <Header>
      <Button
        icon="refresh"
        type="primary"
        funcType="flat"
        onClick={refresh}
      >
        {formatMessage({ id: 'refresh' })}
      </Button>
    </Header>
    <Breadcrumb />
    <Content>
      <EmptyPage
        title={formatMessage({ id: 'empty.title.prohibited' })}
        describe={formatMessage({ id: 'empty.tips.app.member' })}
      />
    </Content>
  </Fragment>;
}
