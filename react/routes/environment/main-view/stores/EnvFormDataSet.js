import { axios } from '@choerodon/master';

export default ({ formatMessage, intlPrefix, projectId }) => ({
  autoCreate: true,
  fields: [
    { name: 'cluster', type: 'number', label: '选择集群', required: true },
    { name: 'code', type: 'string', label: '环境编码', required: true },
    { name: 'name', type: 'string', label: '环境名称', required: true },
    { name: 'description', type: 'string', label: '环境描述' },
    { name: 'group', type: 'id', label: '选择分组' },
  ],
});
