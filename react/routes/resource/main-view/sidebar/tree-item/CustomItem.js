import React, { Fragment, useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import { Icon } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';
import { useMainStore } from '../../stores';
import CustomForm from '../../contents/custom/modals/form-view';
import eventStopProp from '../../../../../utils/eventStopProp';

function CustomItem({
  record,
  name,
  intlPrefix,
  intl: { formatMessage },
}) {
  const {
    treeDs,
    resourceStore: { getSelectedMenu: { itemType, parentId }, setUpTarget },
    itemTypes: { CUSTOM_GROUP, CUSTOM_ITEM },
  } = useResourceStore();
  const { customStore } = useMainStore();

  const [showModal, setShowModal] = useState(false);

  function freshMenu() {
    treeDs.query();
    const [envId] = record.get('parentId').split('-');
    if (itemType === CUSTOM_GROUP && envId === parentId) {
      setUpTarget({
        type: CUSTOM_GROUP,
        id: parentId,
      });
    } else {
      setUpTarget({
        type: CUSTOM_ITEM,
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

  function getSuffix() {
    const status = record.get('commandStatus');
    const disabled = getEnvIsNotRunning() || status === 'operating';
    if (disabled) {
      return null;
    }
    const actionData = [{
      service: ['devops-service.devops-customize-resource.createResource'],
      text: formatMessage({ id: 'edit' }),
      action: openModal,
    }, {
      service: ['devops-service.devops-customize-resource.deleteResource'],
      text: formatMessage({ id: 'delete' }),
      action: deleteItem,
    }];
    return <Action
      placement="bottomRight"
      data={actionData}
      onClick={eventStopProp}
    />;
  }

  return <Fragment>
    <Icon type="filter_b_and_w" />
    {name}
    {getSuffix()}
    {showModal && <CustomForm
      id={record.get('id')}
      envId={record.get('parentId').split('-')[0]}
      type="edit"
      store={customStore}
      visible={showModal}
      onClose={closeModal}
    />}
  </Fragment>;
}

CustomItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(observer(CustomItem));
