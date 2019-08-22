import React, { Fragment, useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import { Icon } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';
import EditNetwork from '../../contents/network/modals/network-edit';
import { useMainStore } from '../../stores';

function NetworkItem({
  record,
  name,
  intlPrefix,
  intl: { formatMessage },
}) {
  const { treeDs } = useResourceStore();
  const { networkStore } = useMainStore();

  const [showModal, setShowModal] = useState(false);

  function freshMenu() {
    treeDs.query();
  }

  function deleteItem() {
    treeDs.remove(record);
  }

  function openModal() {
    setShowModal(true);
  }

  function closeModal(isLoad) {
    setShowModal(false);
    isLoad && freshMenu();
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
