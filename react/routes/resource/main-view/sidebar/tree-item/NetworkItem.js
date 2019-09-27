import React, { Fragment, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import { Icon } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';
import EditNetwork from '../../contents/network/modals/network-edit';
import { useMainStore } from '../../stores';
import eventStopProp from '../../../../../utils/eventStopProp';
import openWarnModal from '../../../../../utils/openWarnModal';

function NetworkItem({
  record,
  name,
  intlPrefix,
  intl: { formatMessage },
}) {
  const {
    treeDs,
    resourceStore: { getSelectedMenu: { itemType, parentId }, setUpTarget, checkExist },
    itemTypes: { SERVICES_GROUP, SERVICES_ITEM },
    AppState: { currentMenuType: { projectId } },
  } = useResourceStore();
  const {
    networkStore,
    mainStore: { openDeleteModal },
  } = useMainStore();

  const [showModal, setShowModal] = useState(false);

  function freshTree() {
    treeDs.query();
  }

  function freshMenu() {
    freshTree();
    const [envId] = record.get('parentId').split('-');
    if (itemType === SERVICES_GROUP && envId === parentId) {
      setUpTarget({
        type: SERVICES_GROUP,
        id: parentId,
      });
    } else {
      setUpTarget({
        type: SERVICES_ITEM,
        id: record.get('id'),
      });
    }
  }

  function getEnvIsNotRunning() {
    const [envId] = record.get('parentId').split('-');
    const envRecord = treeDs.find((item) => item.get('key') === envId);
    const connect = envRecord.get('connect');
    return !connect;
  }

  function checkDataExist() {
    return checkExist({
      projectId,
      type: 'service',
      envId: record.get('parentId').split('-')[0],
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
        setShowModal(true);
      }
    });
  }

  function closeModal(isLoad) {
    setShowModal(false);
    isLoad && freshMenu();
  }

  function getSuffix() {
    const id = record.get('id');
    const netName = record.get('name');
    const [envId] = record.get('parentId').split('-');
    const status = record.get('status');
    const disabled = getEnvIsNotRunning() || status === 'operating';
    if (disabled) {
      return null;
    }
    const actionData = [{
      service: ['devops-service.devops-service.update'],
      text: formatMessage({ id: 'edit' }),
      action: openModal,
    }, {
      service: ['devops-service.devops-service.delete'],
      text: formatMessage({ id: 'delete' }),
      action: () => openDeleteModal(envId, id, netName, 'service', freshMenu),
    }];
    return <Action placement="bottomRight" data={actionData} onClick={eventStopProp} />;
  }

  return <Fragment>
    <Icon type="router" />
    {name}
    {getSuffix()}
    {showModal && (
      <EditNetwork
        netId={record.get('id')}
        envId={record.get('parentId').split('-')[0]}
        visible={showModal}
        store={networkStore}
        onClose={closeModal}
      />
    )}
  </Fragment>;
}

NetworkItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(observer(NetworkItem));
