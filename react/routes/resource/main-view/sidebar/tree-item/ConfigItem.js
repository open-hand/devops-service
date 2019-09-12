import React, { Fragment, useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import { Icon } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';
import { useMainStore } from '../../stores';
import KeyValueModal from '../../contents/application/modals/key-value';

function ConfigItem({
  record,
  name,
  intlPrefix,
  intl: { formatMessage },
}) {
  const { treeDs, itemType: { MAP_ITEM } } = useResourceStore();
  const { configMapStore, secretStore, childrenStore, testStore } = useMainStore();
  const [showModal, setShowModal] = useState(false);

  function freshMenu() {
    treeDs.query();
    childrenStore.getDetailDs.query();
  }

  function deleteItem() {
    treeDs.delete(record);
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
      title: isConfigPage ? 'configMap' : 'secret',
      store: isConfigPage ? configMapStore : secretStore,
    });
  }

  function getSuffix() {
    const actionData = [{
      service: [],
      text: formatMessage({ id: 'edit' }),
      action: openModal,
    }, {
      service: ['devops-service.devops-service.delete'],
      text: formatMessage({ id: 'delete' }),
      action: deleteItem,
    }];
    return <Action placement="bottomRight" data={actionData} />;
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
      {...getParam()}
    />}
  </Fragment>;
}

ConfigItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(observer(ConfigItem));
