import React, { Fragment, useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import { Icon } from 'choerodon-ui';
import map from 'lodash/map';
import { useResourceStore } from '../../../stores';
import eventStopProp from '../../../../../utils/eventStopProp';
import DeleteModal from '../../components/delete-modal';
import { handlePromptError } from '../../../../../utils';
import { useMainStore } from '../../stores';

function CertItem({
  record,
  name,
  intlPrefix,
  intl: { formatMessage },
}) {
  const {
    treeDs,
    resourceStore,
    AppState: { currentMenuType: { projectId } },
  } = useResourceStore();
  const { certStore } = useMainStore();

  const {
    getSelectedMenu: { parentId },
    getDeleteArr,
    openDeleteModal,
    closeDeleteModal,
    removeDeleteModal,
  } = resourceStore;

  const [deleteLoading, setDeleteLoading] = useState(false);

  const deleteModals = useMemo(() => (
    map(getDeleteArr, ({ name: deleteName, display, deleteId }) => (<DeleteModal
      key={deleteId}
      envId={parentId.split('-')[0]}
      store={resourceStore}
      title={`${formatMessage({ id: 'certificate.delete' })}“${deleteName}”`}
      visible={display}
      objectId={deleteId}
      loading={deleteLoading}
      objectType="certificate"
      onClose={closeDeleteModal}
      onOk={handleDelete}
    />))
  ), [getDeleteArr]);

  function freshMenu() {
    treeDs.query();
  }

  async function handleDelete(id, callback) {
    setDeleteLoading(true);
    try {
      const res = await certStore.deleteData(projectId, id);
      if (handlePromptError(res)) {
        removeDeleteModal(id);
        freshMenu();
      }
      setDeleteLoading(false);
    } catch (e) {
      setDeleteLoading(false);
      callback && callback();
      Choerodon.handleResponseError(e);
    }
  }

  function getSuffix() {
    const actionData = [{
      service: ['devops-service.certification.delete'],
      text: formatMessage({ id: 'delete' }),
      action: () => openDeleteModal(record.get('id'), record.get('certName')),
    }];
    return <Action placement="bottomRight" data={actionData} onClick={eventStopProp} />;
  }

  return <Fragment>
    <Icon type="class" />
    {name}
    {getSuffix()}
    {deleteModals}
  </Fragment>;
}

CertItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(observer(CertItem));
