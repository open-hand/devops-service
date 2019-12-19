import React, { useCallback, Fragment, useEffect } from 'react';
import { Page, Content, Header, Breadcrumb, Permission } from '@choerodon/boot';
import { Modal, Button, Spin } from 'choerodon-ui/pro';
import { FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { useRepositoryStore } from './stores';
import RepositoryForm from '../repository/repository-form';

import './index.less';

const modalKey = Modal.key();
const modalStyle = {
  width: 380,
};

const ProRepository = withRouter(observer((props) => {
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { id } },
    intlPrefix,
    prefixCls,
    permissions,
    homeDs,
    detailDs,
    repositoryStore,
  } = useRepositoryStore();

  useEffect(() => {
    detailDs.query();
  }, []);

  function refresh() {
    homeDs.query();
  }

  async function openModal() {
    await detailDs.query();
    Modal.open({
      key: modalKey,
      style: modalStyle,
      drawer: true,
      title: formatMessage({ id: intlPrefix }),
      children: <RepositoryForm
        record={detailDs.current}
        dataSet={detailDs}
        store={repositoryStore}
        id={id}
        isProject
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        refresh={refresh}
      />,
      okText: formatMessage({ id: 'save' }),
    });
  }

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
