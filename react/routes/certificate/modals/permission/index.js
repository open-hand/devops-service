import React, { useCallback, Fragment, useEffect } from 'react';
import { Action } from '@choerodon/master';
import { Table, Modal, SelectBox, Form } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import AddProject from './AddProject';

import './index.less';

const { Column } = Table;
const { Option } = SelectBox;
const modalKey = Modal.key();
const modalStyle = {
  width: 380,
};

export default injectIntl(observer(({
  record,
  dataSet,
  allProjectDs,
  permissionProjectDs,
  optionsDs,
  projectId,
  intlPrefix,
  prefixCls,
  intl: { formatMessage },
  modal,
  refresh,
}) => {
  useEffect(() => {
    permissionProjectDs.transport.read.url = `/devops/v1/projects/${projectId}/certs/${record.get('id')}/permission/page_related`;
    optionsDs.transport.read.url = `/devops/v1/projects/${projectId}/certs/${record.get('id')}/permission/list_non_related`;
    allProjectDs.query();
    permissionProjectDs.query();
  }, []);

  modal.handleOk(async () => {
    try {
      if (await dataSet.submit() !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  });

  function openModal() {
    Modal.open({
      key: modalKey,
      style: modalStyle,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.project.add` }),
      children: <AddProject
        dataSet={permissionProjectDs}
        optionsDs={optionsDs}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      okText: formatMessage({ id: 'add' }),
      onCancel: handleCancel,
    });
  }
  
  function handleCancel() {
    permissionProjectDs.reset();
  }

  function handleDelete() {
    permissionProjectDs.delete(permissionProjectDs.current);
  }

  function renderAction() {
    const actionData = [{
      service: ['devops-service.project-certification.deletePermissionOfProject'],
      text: formatMessage({ id: `${intlPrefix}.permission.delete` }),
      action: handleDelete,
    }];
    return <Action data={actionData} />;
  }

  return (
    <div className={`${prefixCls}-permission-wrap`}>
      <Form record={record}>
        <SelectBox name="skipCheckProjectPermission">
          <Option value>{formatMessage({ id: `${intlPrefix}.project.all` })}</Option>
          <Option value={false}>{formatMessage({ id: `${intlPrefix}.project.some` })}</Option>
        </SelectBox>
      </Form>
      {!record.get('skipCheckProjectPermission') ? (
        <Fragment>
          <Button
            type="primary"
            icon="add"
            onClick={openModal}
            className={`${prefixCls}-permission-wrap-button`}
          >
            <FormattedMessage id={`${intlPrefix}.project.add`} />
          </Button>
          <Table dataSet={permissionProjectDs}>
            <Column name="name" sortable />
            <Column renderer={renderAction} />
            <Column name="code" sortable />
          </Table>
        </Fragment>
      ) : (
        <Table dataSet={allProjectDs}>
          <Column name="name" sortable />
          <Column name="code" sortable />
        </Table>
      )}
    </div>
  );
}));
