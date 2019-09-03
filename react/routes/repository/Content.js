import React, { useCallback, Fragment, useEffect } from 'react';
import { TabPage, Content, Header, Breadcrumb, Permission } from '@choerodon/master';
import { Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { useRepositoryStore } from './stores';
import WarehouseForm from './repository-form';

import './index.less';

const modalKey = Modal.key();
const modalStyle = {
  width: 380,
};

const Repository = withRouter(observer((props) => {
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { id } },
    intlPrefix,
    prefixCls,
    homeDs,
    detailDs,
  } = useRepositoryStore();

  function refresh() {
  }

  async function openModal() {
    await detailDs.query();
    Modal.open({
      key: modalKey,
      style: modalStyle,
      drawer: true,
      title: formatMessage({ id: intlPrefix }),
      children: <WarehouseForm
        record={detailDs.current}
        dataSet={detailDs}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      okText: formatMessage({ id: 'save' }),
    });
  }

  return (
    <TabPage
      service={[]}
    >
      <Header>
        <Permission
          service={[]}
        >
          <Button
            icon="mode_edit"
            onClick={openModal}
          >
            <FormattedMessage id={intlPrefix} />
          </Button>
        </Permission>
      </Header>
      <Breadcrumb />
      <Content>
        <div className={`${prefixCls}-home-item`}>
          <span className={`${prefixCls}-home-item-text`}>
            {formatMessage({ id: `${intlPrefix}.harbor` })}:
          </span>
          <span>
            {homeDs.current && homeDs.current.get('harborConfigUrl')
              ? homeDs.current.get('harborConfigUrl')
              : formatMessage({ id: `${intlPrefix}.harbor.default` })}
          </span>
        </div>
        <div>
          <span className={`${prefixCls}-home-item-text`}>
            {formatMessage({ id: `${intlPrefix}.chart` })}:
          </span>
          <span>
            {homeDs.current && homeDs.current.get('chartConfigUrl')
              ? homeDs.current.get('chartConfigUrl')
              : formatMessage({ id: `${intlPrefix}.chart.default` })}
          </span>
        </div>
      </Content>
    </TabPage>
  );
}));

export default Repository;
