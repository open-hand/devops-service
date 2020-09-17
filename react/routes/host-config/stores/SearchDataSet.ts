import { DataSetProps } from 'choerodon-ui/pro/lib/data-set/DataSet';
import { DataSet } from 'choerodon-ui/pro';

interface ListProps {
  projectId: number,
  statusDs: DataSet,
}

export default ({ projectId, statusDs }: ListProps): DataSetProps => ({
  autoCreate: true,
  selection: false,
  fields: [
    {
      name: 'params',
    },
    {
      name: 'status',
      textField: 'text',
      valueField: 'value',
      options: statusDs,
      // label: '主机状态',
    },
  ],
});
