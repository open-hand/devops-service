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
    read: ({ data }) => {
      const { type, params, status } = data;
      return {
        url: apis.getLoadHostsDetailsUrl(projectId),
        method: 'post',
        data: {
          searchParam: {
            type: type || 'distribute_test',
            status,
          },
          params: params ? [params] : [],
        },
      };
    },
  },
});
