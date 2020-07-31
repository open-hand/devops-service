import { DataSet } from 'choerodon-ui/pro';
import jsonBigint from 'json-bigint';

export default (projectId) => ({
  autoCreate: true,
  fields: [{
    name: 'type',
    type: 'string',
    label: '阶段属性',
    required: true,
    textField: 'text',
    valueField: 'value',
    options: new DataSet({
      data: [{
        value: 'CI',
        text: 'CI阶段',
      }, {
        value: 'CD',
        text: 'CD阶段',
      }],
    }),
  }, {
    name: 'step',
    type: 'string',
    label: '阶段名称',
    required: true,
    maxLength: 15,
  }, {
    name: 'parallel',
    type: 'number',
    label: '任务设置',
    required: true,
    disabled: true,
  }, {
    name: 'triggerType',
    type: 'string',
    label: '流转至此阶段',
    defaultValue: 'auto',
  },
  {
    name: 'pageSize',
    defaultValue: 20,
    type: 'number',
  },
  {
    name: 'cdAuditUserIds',
    type: 'string',
    label: '审核人员',
    dynamicProps: {
      required: ({ record }) => record.get('triggerType') === 'manual',
    },
    textField: 'realName',
    multiple: true,
    valueField: 'id',
    lookupAxiosConfig: (data) => ({
      method: 'post',
      url: `/devops/v1/projects/${projectId}/users/list_users?page=0&size=20`,
      data: {
        param: [],
        searchParam: {
          realName: data.params.realName || '',
        },
      },
      transformResponse: (res) => {
        let newRes;
        try {
          newRes = JSON.parse(res);
          // debugger;
          // if (data.params.appServiceName) {
          //   createUseStore.setSearchAppServiceData(newRes);
          // }
          if (newRes.totalElements % 20 !== 0 && newRes.content.length !== 0) {
            newRes.content.push({
              id: 'more',
              realName: '加载更多',
            });
          }
          return newRes;
        } catch (e) {
          return res;
        }
      },
    }),
  }],
});
