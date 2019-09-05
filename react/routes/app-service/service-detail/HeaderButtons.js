import React, { memo } from 'react';
import { FormattedMessage } from 'react-intl';
import { Header, Permission } from '@choerodon/master';
import { Button, Tooltip } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import { useServiceDetailStore } from './stores';
import Detail from './modals/detail';
import CreateForm from '../modals/creat-form';


const modalKey1 = Modal.key();
const modalStyle = {
  width: 380,
};


const HeaderButtons = observer(({ children }) => {
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { id } },
    intlPrefix,
    prefixCls,
    detailDs,
    AppStore,
  } = useServiceDetailStore();

  function openDetail() {
    const detailModal = Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.detail` }),
      children: <Detail record={detailDs.current} intlPrefix={intlPrefix} prefixCls={prefixCls} formatMessage={formatMessage} />,
      drawer: true,
      style: modalStyle,
      footer: (
        <Button funcType="raised" type="primary" onClick={() => detailModal.close()}>
          <FormattedMessage id="close" />
        </Button>
      ),
    });
  }

  function openEdit() {
    Modal.open({
      key: modalKey1,
      drawer: true,
      style: modalStyle,
      title: <FormattedMessage id={`${intlPrefix}.edit`} />,
      children: <CreateForm
        dataSet={detailDs}
        record={detailDs.current}
        AppStore={AppStore}
        projectId={id}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        isDetailPage
      />,
      onCancel: () => handleCancel(),
    });
  }

  function handleCancel() {
    detailDs.current.reset();
  }

  function getActiveText() {
    const active = detailDs.current && detailDs.current.get('active') ? 'disable' : 'enable';
    return <FormattedMessage id={`${intlPrefix}.${active}`} />;
  }

  async function changeActive() {
    const { current } = detailDs;
    if (await AppStore.changeActive(id, current.get('id'), !current.get('active')) !== false) {
      detailDs.query();
    }
  }


  return (
    <Header>
      <Permission
        service={['devops-service.app-service.update']}
      >
        <Tooltip
          title={detailDs.current && !detailDs.current.get('active') ? <FormattedMessage id={`${intlPrefix}.button.disabled`} /> : ''}
          placement="bottom"
        >
          <Button
            icon="mode_edit "
            onClick={openEdit}
            disabled={detailDs.current && !detailDs.current.get('active')}
          >
            <FormattedMessage id={`${intlPrefix}.edit`} />
          </Button>
        </Tooltip>
      </Permission>
      <Permission
        service={['devops-service.app-service.updateActive']}
      >
        <Button
          icon="remove_circle_outline"
          onClick={changeActive}
        >
          {getActiveText()}
        </Button>
      </Permission>
      <Permission
        service={['devops-service.app-service.query']}
      >
        <Button
          icon="find_in_page"
          onClick={openDetail}
        >
          <FormattedMessage id={`${intlPrefix}.detail`} />
        </Button>
      </Permission>
      {children}
    </Header>
  );
});

export default HeaderButtons;
