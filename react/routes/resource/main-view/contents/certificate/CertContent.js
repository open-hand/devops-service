import React, { Fragment, useMemo, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/master';
import { Popover } from 'choerodon-ui';
import { Table } from 'choerodon-ui/pro';
import map from 'lodash/map';
import findIndex from 'lodash/findIndex';
import find from 'lodash/find';
import filter from 'lodash/filter';
import StatusIcon from '../../../../../components/StatusIcon';
import { useResourceStore } from '../../../stores';
import { useCertificateStore } from './stores';
import Modals from './modals';
import MouserOverWrapper from '../../../../../components/MouseOverWrapper';
import { getTimeLeft, handlePromptError } from '../../../../../utils';
import StatusTags from '../../../../../components/status-tag';
import DeleteModal from '../../components/delete-modal';


import './index.less';
import { useMainStore } from '../../stores';

const { Column } = Table;

const CertContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    treeDs,
    resourceStore,
  } = useResourceStore();
  const {
    certificateDs,
    intl: { formatMessage },
    AppState: { currentMenuType: { projectId } },
  } = useCertificateStore();
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
    map(getDeleteArr, ({ name, display, deleteId }) => (<DeleteModal
      key={deleteId}
      envId={parentId.split('-')[0]}
      store={resourceStore}
      title={`${formatMessage({ id: 'certificate.delete' })}“${name}”`}
      visible={display}
      objectId={deleteId}
      loading={deleteLoading}
      objectType="certificate"
      onClose={closeDeleteModal}
      onOk={handleDelete}
    />))
  ), [getDeleteArr]);


  function refresh() {
    treeDs.query();
    certificateDs.query();
  }

  function renderName({ value, record }) {
    const commandStatus = record.get('commandStatus');
    return (
      <div className="c7n-network-service">
        <StatusTags
          name={formatMessage({ id: commandStatus || 'null' })}
          colorCode={commandStatus || 'success'}
          style={{ minWidth: 40, marginRight: '0.08rem', height: '0.16rem', lineHeight: '0.16rem' }}
        />
        <StatusIcon
          name={value}
          status={record.get('commandStatus') || ''}
          error={record.get('error') || ''}
        />
      </div>
    );
  }

  function renderDomains({ value }) {
    return (
      <MouserOverWrapper text={value[0] || ''} width={0.25}>
        {value[0]}
      </MouserOverWrapper>
    );
  }


  function renderValid({ record }) {
    const validFrom = record.get('validFrom');
    const validUntil = record.get('validUntil');
    const commandStatus = record.get('commandStatus');
    let msg = null;
    let content = null;
    if (!(validFrom && validUntil && commandStatus === 'success')) return content;

    content = (
      <Fragment>
        <FormattedMessage id="timeFrom" />：{validFrom}
        <br />
        <FormattedMessage id="timeUntil" />：{validUntil}
      </Fragment>
    );
    const start = new Date(validFrom.replace(/-/g, '/')).getTime();
    const end = new Date(validUntil.replace(/-/g, '/')).getTime();
    const now = Date.now();

    if (now < start) {
      msg = <FormattedMessage id="notActive" />;
    } else if (now > end) {
      msg = <FormattedMessage id="expired" />;
    } else {
      msg = getTimeLeft(now, end);
    }
    return (
      <Popover
        content={content}
        getPopupContainer={(triggerNode) => triggerNode.parentNode}
        trigger="hover"
        placement="top"
      >
        <span>{msg}</span>
      </Popover>
    );
  }

  function renderAction({ record }) {
    const commandStatus = record.get('commandStatus');
    const id = certificateDs.current.get('id');
    const name = certificateDs.current.get('certName');
    if (commandStatus === 'operating') {
      return null;
    }
    const buttons = [
      {
        service: ['devops-service.certification.delete'],
        text: formatMessage({ id: 'delete' }),
        action: () => openDeleteModal(id, name),
      },
    ];

    return (<Action data={buttons} />);
  }

  async function handleDelete(id, callback) {
    setDeleteLoading(true);
    try {
      const res = await certStore.deleteData(projectId, id);
      if (handlePromptError(res)) {
        removeDeleteModal(id);
        refresh();
      }
      setDeleteLoading(false);
    } catch (e) {
      setDeleteLoading(false);
      callback && callback();
      Choerodon.handleResponseError(e);
    }
  }

  return (
    <div className={`${prefixCls}-ingress-table`}>
      <Modals />
      <Table
        dataSet={certificateDs}
        border={false}
        queryBar="bar"
      >
        <Column name="certName" renderer={renderName} />
        <Column renderer={renderAction} width="0.7rem" />
        <Column name="domains" renderer={renderDomains} />
        <Column renderer={renderValid} header={formatMessage({ id: 'validDate' })} />
      </Table>
      {deleteModals}
    </div>
  );
});

export default CertContent;
