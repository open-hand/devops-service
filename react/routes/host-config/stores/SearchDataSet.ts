/* eslint-disable import/no-anonymous-default-export */
import { DataSetProps } from 'choerodon-ui/pro/lib/data-set/DataSet';

interface ListProps {
  projectId: number,
}

export default ({ projectId }: ListProps): DataSetProps => ({
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
    },
  ],
});
