import React, { useEffect } from 'react';
import { Page, Content, Header, Breadcrumb, Permission } from '@choerodon/boot';
import { Modal, Button, Spin } from 'choerodon-ui/pro';
import { FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { useRepositoryStore } from './stores';
import RepositoryForm from './repository-form';

import './index.less';

const modalKey = Modal.key();
const modalStyle = {
  width: 380,
};

const Repository = withRouter(observer((props) => {
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { organizationId } },
    intlPrefix,
    prefixCls,
    permissions,
    homeDs,
    detailDs,
    repositoryStore,
  } = useRepositoryStore();

  function refresh() {
    homeDs.query();
  }

  useEffect(() => {
    detailDs.query();
  }, []);

  return (
    <Page
      service={permissions}
    >
      <Breadcrumb />
      <Content className={`${prefixCls}-home`}>
        {detailDs.current ? <RepositoryForm
          record={detailDs.current}
          dataSet={detailDs}
          store={repositoryStore}
          id={organizationId}
          intlPrefix={intlPrefix}
          prefixCls={prefixCls}
          refresh={refresh}
        /> : <Spin />}
      </Content>
    </Page>
  );
}));

export default Repository;
