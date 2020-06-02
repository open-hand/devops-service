import { DataSet } from 'choerodon-ui/pro';

const optionDs = new DataSet({
  selection: 'single',
  data: [{
    name: 'Maven构建',
    value: 'Maven',
  }, {
    name: 'Npm构建',
    value: 'npm',
  }, {
    name: '上传软件包至存储库',
    value: 'upload',
  }, {
    name: 'Docker构建',
    value: 'docker',
  }],
});

export default () => ({
  autoCreate: true,
  fields: [{
    name: 'kybz',
    type: 'string',
    label: '可用步骤',
    textField: 'name',
    valueField: 'value',
    required: true,
    options: optionDs,
  }],
});
