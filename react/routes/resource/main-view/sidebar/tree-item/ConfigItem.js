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
    itemTypes: { MAP_ITEM, CIPHER_GROUP, CERT_GROUP },
    resourceStore: { getSelectedMenu: { itemType, parentId }, setUpTarget },
  } = useResourceStore();
  const {
    configMapStore,
    secretStore,
    mainStore: { openDeleteModal },
  } = useMainStore();
  const [showModal, setShowModal] = useState(false);

  function freshMenu() {
    treeDs.query();
    const [envId] = record.get('parentId').split('-');
    if ((itemType === CERT_GROUP || itemType === CIPHER_GROUP) && envId === parentId) {
      setUpTarget({
        type: itemType,
        id: parentId,
      });
    } else {
      setUpTarget({
        type: itemType,
        id: record.get('id'),
      });
    }
  }

  function openModal() {
    setShowModal(true);
  }

  function closeModal(isLoad) {
    setShowModal(false);
    isLoad && freshMenu();
  }

  function getParam() {
    const isConfigPage = record.get('itemType') === MAP_ITEM;
    return ({
      modeSwitch: isConfigPage,
      title: isConfigPage ? 'configMap' : 'cipher',
      store: isConfigPage ? configMapStore : secretStore,
    });
  }

  function getSuffix() {
    const id = record.get('id');
    const recordName = record.get('name');
    const type = record.get('itemType') === MAP_ITEM ? 'configMap' : 'secret';
    const [envId] = record.get('parentId').split('-');
    const actionData = [{
      service: [],
      text: formatMessage({ id: 'edit' }),
      action: openModal,
    }, {
      service: ['devops-service.devops-service.delete'],
      text: formatMessage({ id: 'delete' }),
      action: () => openDeleteModal(envId, id, recordName, type, freshMenu),
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
      {...getParam()}
    />}
  </Fragment>;
}

ConfigItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(observer(ConfigItem));
