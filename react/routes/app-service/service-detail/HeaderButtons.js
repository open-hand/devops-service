import React, { memo, useMemo } from 'react';
import { FormattedMessage } from 'react-intl';
import { Header, Permission, Choerodon } from '@choerodon/boot';
import { Button, Tooltip } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import { useAppTopStore } from '../stores';
import { useServiceDetailStore } from './stores';
import Detail from './modals/detail';
import CreateForm from '../modals/creat-form';
import EditForm from '../modals/edit-form';


const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const editModalKey = Modal.key();
const modalStyle = {
  width: 380,
};


const HeaderButtons = observer(({ children }) => {
  const {
    intlPrefix,
    prefixCls,
    appServiceStore,
  } = useAppTopStore();
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { id } },
    detailDs,
  } = useServiceDetailStore();

  const serviceActive = useMemo(() => detailDs.current && detailDs.current.get('active'), [detailDs.current]);

  function refresh() {
    detailDs.query();
  }

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
    const appServiceId = detailDs.current.get('id');

    Modal.open({
      key: editModalKey,
      drawer: true,
      style: modalStyle,
      title: formatMessage({ id: `${intlPrefix}.edit` }),
      children: <EditForm
        refresh={refresh}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        appServiceId={appServiceId}
      />,
      okText: formatMessage({ id: 'save' }),
    });
  }

  function getActiveText() {
    const active = serviceActive ? 'disable' : 'enable';
    return <FormattedMessage id={`${intlPrefix}.${active}`} />;
  }

  async function changeActive() {
    const { current } = detailDs;
    if (current.get('active')) {
      Modal.open({
        key: modalKey2,
        title: formatMessage({ id: `${intlPrefix}.stop` }, { name: current.get('name') }),
        children: <FormattedMessage id={`${intlPrefix}.stop.tips`} />,
        onOk: () => handleChangeActive(false),
        okText: formatMessage({ id: 'stop' }),
      });
    } else {
      handleChangeActive(true);
    }
  }

  async function handleChangeActive(active) {
    const { current } = detailDs;
    try {
      if (await appServiceStore.changeActive(id, current.get('id'), active) !== false) {
        detailDs.query();
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  }


  return (
    <Header>
      <Permission
        service={['devops-service.app-service.update']}
      >
        <Tooltip
          title={!serviceActive ? <FormattedMessage id={`${intlPrefix}.button.disabled`} /> : ''}
          placement="bottom"
        >
          <Button
            icon="mode_edit "
            onClick={openEdit}
            disabled={!serviceActive}
          >
            <FormattedMessage id={`${intlPrefix}.edit`} />
          </Button>
        </Tooltip>
      </Permission>
      <Permission
        service={['devops-service.app-service.updateActive']}
      >
        <Button
          icon={serviceActive ? 'remove_circle_outline' : 'finished'}
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
