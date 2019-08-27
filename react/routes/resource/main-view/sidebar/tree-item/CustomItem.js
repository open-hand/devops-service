import React, { Fragment, useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import { Icon } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';
import { useMainStore } from '../../stores';
import CustomForm from '../../contents/custom/modals/form-view';

function CustomItem({
  record,
  name,
  intlPrefix,
  intl: { formatMessage },
}) {
  const { treeDs } = useResourceStore();
  const { customStore } = useMainStore();

  const [showModal, setShowModal] = useState(false);

  function freshMenu() {
    treeDs.query();
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
    const actionData = [{
      service: [],
      text: formatMessage({ id: 'edit' }),
      action: openModal,
    }, {
      service: ['devops-service.devops-customize-resource.deleteResource'],
      text: formatMessage({ id: 'delete' }),
      action: deleteItem,
    }];
    return <Action placement="bottomRight" data={actionData} />;
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
