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

function ConfigItem({
  record,
  name,
  intlPrefix,
  intl: { formatMessage },
}) {
  const {
    treeDs,
    itemTypes: { MAP_ITEM, MAP_GROUP },
    resourceStore: { getSelectedMenu: { itemType, parentId }, setUpTarget },
  } = useResourceStore();
  const {
    configMapStore,
    mainStore: { openDeleteModal },
  } = useMainStore();

  const [showModal, setShowModal] = useState(false);

  function freshMenu() {
    treeDs.query();
    const [envId] = record.get('parentId').split('-');
    if (itemType === MAP_GROUP && envId === parentId) {
      setUpTarget({
        type: itemType,
        id: parentId,
      });
    } else {
      setUpTarget({
        type: MAP_ITEM,
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

  function openModal() {
    setShowModal(true);
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
      service: ['devops-service.devops-config-map.update'],
      text: formatMessage({ id: 'edit' }),
      action: openModal,
    }, {
      service: ['devops-service.devops-config-map.delete'],
      text: formatMessage({ id: 'delete' }),
      action: () => openDeleteModal(envId, id, recordName, 'configMap', freshMenu),
    }];
    return <Action placement="bottomRight" data={actionData} onClick={eventStopProp} />;
  }

  return <Fragment>
    <Icon type={record.get('itemType') === MAP_ITEM ? 'compare_arrows' : 'vpn_key'} />
    {name}
    {getSuffix()}
    {showModal && <KeyValueModal
      visible={showModal}
      id={record.get('id')}
      envId={record.get('parentId').split('-')[0]}
      onClose={closeModal}
      intlPrefix={intlPrefix}
      modeSwitch
      title="configMap"
      store={configMapStore}
    />}
  </Fragment>;
}

ConfigItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(observer(ConfigItem));
