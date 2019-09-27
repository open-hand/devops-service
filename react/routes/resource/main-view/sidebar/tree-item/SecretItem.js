import React, { Fragment, useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import { Icon } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';
import { useMainStore } from '../../stores';
import KeyValueModal from '../../contents/application/modals/key-value';
import eventStopProp from '../../../../../utils/eventStopProp';
import openWarnModal from '../../../../../utils/openWarnModal';

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

  const [showModal, setShowModal] = useState(false);

  function freshTree() {
    treeDs.query();
  }

  function freshMenu() {
    freshTree();
    const [envId] = record.get('parentId').split('-');
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
    const [envId] = record.get('parentId').split('-');
    const envRecord = treeDs.find((item) => item.get('key') === envId);
    const connect = envRecord.get('connect');
    return !connect;
  }

  function checkDataExist() {
    return checkExist({
      projectId,
      type: 'secret',
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
    const recordName = record.get('name');
    const [envId] = record.get('parentId').split('-');
    const status = record.get('commandStatus');
    const disabled = getEnvIsNotRunning() || status === 'operating';
    if (disabled) {
      return null;
    }
    const actionData = [{
      service: ['devops-service.devops-secret.update'],
      text: formatMessage({ id: 'edit' }),
      action: openModal,
    }, {
      service: ['devops-service.devops-secret.deleteSecret'],
      text: formatMessage({ id: 'delete' }),
      action: () => openDeleteModal(envId, id, recordName, 'secret', freshMenu),
    }];
    return <Action placement="bottomRight" data={actionData} onClick={eventStopProp} />;
  }

  return <Fragment>
    <Icon type="vpn_key" />
    {name}
    {getSuffix()}
    {showModal && <KeyValueModal
      visible={showModal}
      id={record.get('id')}
      envId={record.get('parentId').split('-')[0]}
      onClose={closeModal}
      intlPrefix={intlPrefix}
      modeSwitch={false}
      title="cipher"
      store={secretStore}
    />}
  </Fragment>;
}

ConfigItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(observer(ConfigItem));
