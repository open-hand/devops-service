import { DataSetProps } from 'choerodon-ui/pro/lib/data-set/DataSet';
import { FieldType } from 'choerodon-ui/pro/lib/data-set/enum';

export default (projectId: number): DataSetProps => ({
  autoQuery: false,
  selection: false,
  paging: false,
  fields: [
    { name: 'appServiceId', type: 'string' as FieldType },
    { name: 'appServiceName', type: 'string' as FieldType },
  ],
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/app_service/list_app_services_having_versions`,
      method: 'get',
    },
  },
});
