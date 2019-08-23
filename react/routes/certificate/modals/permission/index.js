import React, { useCallback, Fragment, useEffect } from 'react';
import { Action } from '@choerodon/master';
import { Table, Modal, SelectBox, Form } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
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

  useEffect(() => {
    permissionProjectDs.transport.create = ({ data }) => {
      const res = {
        objectVersionNumber: record.get('objectVersionNumber'),
        certificationId: record.get('id'),
        skipCheckProjectPermission: false,
        projectIds: map(data, 'project'),
      };

      return ({
        url: `/devops/v1/projects/${projectId}/certs/${record.get('id')}/permission`,
        method: 'post',
        data: res,
      });
    };
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
    permissionProjectDs.current.delete();
  }

  function renderAction() {
    const actionData = [{
      service: [],
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
            <Column name="name" />
            <Column renderer={renderAction} />
            <Column name="code" />
          </Table>
        </Fragment>
      ) : (
        <Table dataSet={allProjectDs}>
          <Column name="name" />
          <Column name="code" />
        </Table>
      )}
    </div>
  );
}));
