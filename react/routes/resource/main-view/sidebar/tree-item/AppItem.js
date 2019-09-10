import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Permission } from '@choerodon/master';
import { Icon, Modal } from 'choerodon-ui/pro';
import Action from '../../../../../components/action';
import { handlePromptError } from '../../../../../utils';
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

  function openModal(e) {
    e.domEvent.stopPropagation();
    Modal.open({
      movable: false,
      closable: false,
      key: modalKey,
      title: formatMessage({ id: `${intlPrefix}.modal.service.delete` }),
      children: formatMessage({ id: `${intlPrefix}.modal.service.delete.desc` }),
      okText: formatMessage({ id: 'delete' }),
      onOk: handleClick,
    });
  }

  function getSuffix() {
    const actionData = [{
      key: 'delete',
      text: formatMessage({ id: `${intlPrefix}.modal.service.delete` }),
    }];
    return <Action placement="bottomRight" items={actionData} menuClick={openModal} />;
  }

  return <Fragment>
    <Icon type="widgets" />
    {name}
    <Permission service={['devops-service.devops-env-app-service.delete']}>
      {getSuffix()}
    </Permission>
  </Fragment>;
}

AppItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(observer(AppItem));
