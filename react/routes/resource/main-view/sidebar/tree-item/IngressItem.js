import React, { Fragment, useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import { Icon } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';
import { useMainStore } from '../../stores';
import DomainModal from '../../contents/application/modals/domain';

function IngressItem({
  record,
  name,
  intlPrefix,
  intl: { formatMessage },
}) {
  const { treeDs } = useResourceStore();
  const { ingressStore } = useMainStore();

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
      service: ['devops-service.devops-ingress.delete'],
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
      <DomainModal
        envId={record.get('parentId').split('-')[0]}
        id={record.get('id')}
        visible={showModal}
        type="edit"
        store={ingressStore}
        onClose={closeModal}
      />
    )}
  </Fragment>;
}

IngressItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(observer(IngressItem));
