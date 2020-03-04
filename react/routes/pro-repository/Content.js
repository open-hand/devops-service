import React, { useCallback, Fragment, useEffect } from 'react';
import { Page, Content, Header, Breadcrumb, Permission } from '@choerodon/boot';
import { Modal, Button, Spin } from 'choerodon-ui/pro';
import { FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { Prompt } from 'react-router-dom';
import { useRepositoryStore } from './stores';
import RepositoryForm from '../repository/repository-form';

import './index.less';

const ProRepository = withRouter(observer((props) => {
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { id } },
    intlPrefix,
    prefixCls,
    permissions,
    detailDs,
    repositoryStore,
    promptMsg,
  } = useRepositoryStore();

  function refresh() {
    detailDs.query();
  }

  return (
    <Page
      service={permissions}
    >
      <Breadcrumb />
      <Prompt message={promptMsg} when={detailDs.current ? detailDs.current.dirty : false} />
      <Content className={`${prefixCls}-home`}>
        {detailDs.current ? <RepositoryForm
          record={detailDs.current}
          dataSet={detailDs}
          store={repositoryStore}
          id={id}
          isProject
          intlPrefix={intlPrefix}
          prefixCls={prefixCls}
          refresh={refresh}
        /> : <Spin />}
      </Content>
    </Page>
  );
}));

export default ProRepository;
