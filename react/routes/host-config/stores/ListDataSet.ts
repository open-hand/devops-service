import { DataSetProps } from 'choerodon-ui/pro/lib/data-set/DataSet';

interface ListProps {
  projectId: number,
}

export default ({ projectId }: ListProps): DataSetProps => ({
  autoCreate: false,
  selection: false,
});
