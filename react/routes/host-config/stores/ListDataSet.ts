/* eslint-disable import/no-anonymous-default-export */
import { DataSetProps } from 'choerodon-ui/pro/lib/data-set/DataSet';
import apis from '../apis';

interface ListProps {
  projectId: number,
}

export default ({ projectId }: ListProps): DataSetProps => ({
  autoCreate: false,
  autoQuery: true,
  selection: false,
  paging: true,
  pageSize: 10,
  transport: {
    read: ({ data, params, dataSet }) => ({
      url: apis.getLoadHostsDetailsUrl(projectId),
      method: 'post',
      data: {
        searchParam: {
          type: 'deploy',
        },
        params: [],
      },
    }),
    destroy: ({ data: [data] }) => ({
      url: apis.getDeleteHostUrl(projectId, data.id),
      method: 'delete',
    }),
  },
});
