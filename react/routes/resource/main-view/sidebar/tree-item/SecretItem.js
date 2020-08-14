import React, { Fragment, useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/boot';
import { Icon } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import { useResourceStore } from '../../../stores';
import { useMainStore } from '../../stores';
import KeyValueModal from '../../contents/application/modals/key-value';
import eventStopProp from '../../../../../utils/eventStopProp';
import openWarnModal from '../../../../../utils/openWarnModal';

const modalKey = Modal.key();
const modalStyle = {
  width: 'calc(100vw - 3.52rem)',
};

function ConfigItem({
  record,
  name,
  intlPrefix,
  intl: { formatMessage },
}) {
  const {
    treeDs,
    itemTypes: { CIPHER_GROUP, CIPHER_ITEM },
    resourceStore: { getSelectedMenu: { itemType, parentId }, setUpTarget, checkExist },
    AppState: { currentMenuType: { projectId } },
  } = useResourceStore();
  const {
    secretStore,
    mainStore: { openDeleteModal },
  } = useMainStore();

  function freshTree() {
    treeDs.query();
  }

  function freshMenu() {
    freshTree();
    const [envId] = record.get('parentId').split('**');
    if (itemType === CIPHER_GROUP && envId === parentId) {
      setUpTarget({
        type: itemType,
        id: parentId,
      });
    } else {
      setUpTarget({
        type: CIPHER_ITEM,
        id: record.get('id'),
      });
    }
  }

  function getEnvIsNotRunning() {
    const [envId] = record.get('parentId').split('**');
    const envRecord = treeDs.find((item) => item.get('key') === envId);
    const connect = envRecord.get('connect');
    return !connect;
  }

  function checkDataExist() {
    return checkExist({
      projectId,
      type: 'secret',
      envId: record.get('parentId').split('**')[0],
      id: record.get('id'),
    }).then((isExist) => {
      if (!isExist) {
        openWarnModal(freshTree, formatMessage);
      }
      return isExist;
    });
  }

  function openModal() {
    checkDataExist().then((query) => {
      if (query) {
        Modal.open({
          key: modalKey,
          style: modalStyle,
          drawer: true,
          title: formatMessage({ id: `${intlPrefix}.cipher.edit` }),
          children: <KeyValueModal
            id={record.get('id')}
            envId={record.get('parentId').split('**')[0]}
            intlPrefix={intlPrefix}
            title="cipher"
            store={secretStore}
            refresh={freshMenu}
          />,
          okText: formatMessage({ id: 'save' }),
        });
      }
    });
  }

  function getSuffix() {
    const id = record.get('id');
    const recordName = record.get('name');
    const [envId] = record.get('parentId').split('**');
    const status = record.get('status');
    const disabled = getEnvIsNotRunning() || status === 'operating';
    if (disabled) {
      return null;
    }
    const actionData = [{
      service: ['choerodon.code.project.deploy.app-deployment.resource.ps.edit-cipher'],
      text: formatMessage({ id: 'edit' }),
      action: openModal,
    }, {
      service: ['choerodon.code.project.deploy.app-deployment.resource.ps.delete-cipher'],
      text: formatMessage({ id: 'delete' }),
      action: () => openDeleteModal(envId, id, recordName, 'secret', freshMenu),
    }];
    return <Action placement="bottomRight" data={actionData} onClick={eventStopProp} />;
  }

  return <Fragment>
    <Icon type="vpn_key" />
    {name}
    {getSuffix()}
  </Fragment>;
}

ConfigItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(observer(ConfigItem));
