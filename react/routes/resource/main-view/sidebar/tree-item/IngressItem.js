import React, { Fragment, useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/boot';
import { Icon } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import { useResourceStore } from '../../../stores';
import { useMainStore } from '../../stores';
import DomainModal from '../../contents/ingress/modals/domain-create';
import eventStopProp from '../../../../../utils/eventStopProp';
import openWarnModal from '../../../../../utils/openWarnModal';

const modalKey = Modal.key();
const modalStyle = {
  width: 740,
};

function IngressItem({
  record,
  name,
  intlPrefix,
  intl: { formatMessage },
}) {
  const {
    treeDs,
    resourceStore: { getSelectedMenu: { itemType, parentId }, setUpTarget, checkExist },
    itemTypes: { INGRESS_GROUP, INGRESS_ITEM },
    AppState: { currentMenuType: { projectId } },
  } = useResourceStore();
  const {
    ingressStore,
    mainStore: { openDeleteModal },
  } = useMainStore();

  const [showModal, setShowModal] = useState(false);

  function freshTree() {
    treeDs.query();
  }

  function freshMenu() {
    freshTree();
    const [envId] = record.get('parentId').split('-');
    if (itemType === INGRESS_GROUP && envId === parentId) {
      setUpTarget({
        type: INGRESS_GROUP,
        id: parentId,
      });
    } else {
      setUpTarget({
        type: INGRESS_ITEM,
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
      type: 'ingress',
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
        Modal.open({
          key: modalKey,
          style: modalStyle,
          drawer: true,
          title: formatMessage({ id: 'domain.update.head' }),
          children: <DomainModal
            envId={record.get('parentId').split('-')[0]}
            id={record.get('id')}
            visible={showModal}
            type="edit"
            store={ingressStore}
            refresh={freshMenu}
          />,
          okText: formatMessage({ id: 'save' }),
        });
      }
    });
  }

  function closeModal(isLoad) {
    setShowModal(false);
    isLoad && freshMenu();
  }

  function getSuffix() {
    const id = record.get('id');
    const ingressName = record.get('name');
    const [envId] = record.get('parentId').split('-');
    const status = record.get('status');
    const disabled = getEnvIsNotRunning() || status === 'operating';
    if (disabled) {
      return null;
    }
    const actionData = [{
      service: ['devops-service.devops-ingress.update'],
      text: formatMessage({ id: 'edit' }),
      action: openModal,
    }, {
      service: ['devops-service.devops-ingress.delete'],
      text: formatMessage({ id: 'delete' }),
      action: () => openDeleteModal(envId, id, ingressName, 'ingress', freshMenu),
    }];
    return <Action placement="bottomRight" data={actionData} onClick={eventStopProp} />;
  }

  return <Fragment>
    <Icon type="language" />
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
