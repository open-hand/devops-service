import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action, Choerodon } from '@choerodon/boot';
import { Icon, Modal, Tooltip } from 'choerodon-ui/pro';
import AppName from '../../../../../components/appName';
import { handlePromptError } from '../../../../../utils';
import eventStopProp from '../../../../../utils/eventStopProp';
import { useResourceStore } from '../../../stores';
import { useTreeItemStore } from './stores';

const modalKey = Modal.key();

function AppItem({ name, record, intl: { formatMessage }, intlPrefix }) {
  const {
    treeDs,
    AppState: { currentMenuType: { id } },
  } = useResourceStore();
  const { treeItemStore } = useTreeItemStore();

  async function handleClick() {
    if (!record) return;

    const envId = record.get('parentId');
    const appId = record.get('id');
    try {
      const result = await treeItemStore.removeService(id, envId, [appId]);
      if (handlePromptError(result, false)) {
        treeDs.query();
      }
    } catch (error) {
      Choerodon.handleResponseError(error);
    }
  }

  function openModal() {
    Modal.open({
      movable: false,
      closable: false,
      key: modalKey,
      title: formatMessage({ id: `${intlPrefix}.modal.service.delete` }),
      children: formatMessage({ id: `${intlPrefix}.modal.service.delete.desc` }),
      okText: formatMessage({ id: 'delete' }),
      onOk: handleClick,
      okProps: {
        color: 'red',
      },
      cancelProps: {
        color: 'dark',
      },
    });
  }

  function getSuffix() {
    const actionData = [{
      service: ['choerodon.code.project.deploy.app-deployment.resource.ps.delete-contact'],
      text: formatMessage({ id: `${intlPrefix}.modal.service.delete` }),
      action: openModal,
    }];
    return <Action placement="bottomRight" data={actionData} onClick={eventStopProp} />;
  }
  function renderIcon() {
    const type = record.get('type');
    let iconType = 'widgets';
    let message = 'project';
    if (type === 'market_service') {
      iconType = 'application_market';
      message = 'market';
    } else if (type === 'share_service') {
      iconType = 'share';
      message = 'share';
    }
    return (
      <Tooltip title={formatMessage({ id: message })}>
        <Icon type={iconType} />
      </Tooltip>
    );
  }
  return <Fragment>
    {renderIcon()}
    {name}
    {getSuffix()}
  </Fragment>;
}

AppItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(observer(AppItem));
