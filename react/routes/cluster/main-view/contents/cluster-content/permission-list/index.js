import React from 'react';
import { Permission, Action } from '@choerodon/boot';
import { Table } from 'choerodon-ui/pro';
import { useClusterContentStore } from '../stores';


const { Column } = Table;

export default () => {
  const { formatMessage,
    PermissionDs, ClusterDetailDs } = useClusterContentStore();
  const cluster = ClusterDetailDs.current;
  function renderActions({ record }) {
    const actionData = [
      {
        service: ['devops-service.devops-cluster.deleteCluster'],
        text: formatMessage({ id: 'delete' }),
        action: () => {
          PermissionDs.transport.destroy.params = {
            delete_project_id: record.get('id'),
          };
          const modalProps = {
            title: formatMessage({ id: 'c7ncd.deployment.permission.delete.title' }),
            children: formatMessage({ id: 'c7ncd.deployment.permission.project.delete.des' }),
            okText: formatMessage({ id: 'delete' }),
            okProps: { color: 'red' },
            cancelProps: { color: 'dark' },
          };
          PermissionDs.delete(record, modalProps);
        },
      },
    ];
    return (<Action data={actionData} />);
  }

  return (
    <Table
      dataSet={PermissionDs}
      border={false}
    >
      <Column name="name" width={200} />
      {cluster.get('skipCheckProjectPermission') ? null : <Column renderer={renderActions} />}
      <Column name="code" />
    </Table>
  );
};
