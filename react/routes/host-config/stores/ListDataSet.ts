/* eslint-disable import/no-anonymous-default-export */
import { DataSetProps } from 'choerodon-ui/pro/lib/data-set/DataSet';

interface ListProps {
  projectId: number,
}

export default ({ projectId }: ListProps): DataSetProps => ({
  autoCreate: false,
  selection: false,
  paging: true,
  pageSize: 10,
});
