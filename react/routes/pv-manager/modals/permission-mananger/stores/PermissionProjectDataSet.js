import map from 'lodash/map';
import compact from 'lodash/compact';
import getTablePostData from '../../../../../utils/getTablePostData';

export default ((intlPrefix, formatMessage, projectId, pvId, optionsDs, DetailDs) => ({
  autoCreate: false,
  autoQuery: false,
  selection: false,
  paging: true,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);
      return ({
        url: `/devops/v1/projects/${projectId}/pvs/${pvId}/page_related`,
        method: 'post',
        data: postData,
      });
    },
    create: ({ data }) => {
      const res = {
        objectVersionNumber: DetailDs.current.get('objectVersionNumber'),
        skipCheckProjectPermission: false,
        projectIds: compact(map(data, 'projectId') || []),
        pvId,
      };
      return ({
        url: `/devops/v1/projects/${projectId}/pvs/${pvId}/permission`,
        method: 'post',
        data: res,
      });
    },
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/pvs/${pvId}/permission?related_project_id=${data.id}`,
      method: 'delete',
    }),
  },
  fields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.code` }) },
    { name: 'projectId', type: 'number', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.project` }), options: optionsDs, required: true },
  ],
  queryFields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.code` }) },
  ],
}));
