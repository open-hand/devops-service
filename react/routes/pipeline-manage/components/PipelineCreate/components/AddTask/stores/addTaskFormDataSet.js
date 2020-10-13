export default (
  PipelineCreateFormDataSet,
  AppServiceOptionsDs,
  appServiceId,
  projectId,
  AddTaskUseStore,
  organizationId,
  ZpkOptionsDs,
) => {
  function checkImage(value, name, record) {
    const pa = /^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z0-9][a-z0-9-]{0,61}(\/.+)*:.+$/;
    if (value && pa.test(value)) {
      return true;
    }
    return '请输入格式正确的image镜像';
  }
  return ({
    autoCreate: true,
    fields: [{
      name: 'type',
      type: 'string',
      label: '任务类型',
      required: true,
      defaultValue: 'build',
    }, {
      name: 'name',
      type: 'string',
      label: '任务名称',
      dynamicProps: {
        required: ({ record }) => record.get('type') !== 'custom',
        maxLength: ({ record }) => (record.get('type') !== 'custom' ? 15 : undefined),
      },
    }, {
      name: 'glyyfw',
      type: 'string',
      label: '关联应用服务',
      required: true,
      disabled: true,
      // textField: 'appServiceName',
      // valueField: 'appServiceId',
      // defaultValue: appServiceId || PipelineCreateFormDataSet.current.get('appServiceId'),
      // options: AppServiceOptionsDs,
      // lookupAxiosConfig: (data) => ({
      //   method: 'post',
      //   url: `/devops/v1/projects/${projectId}/app_service/list_app_services_without_ci`,
      //   data: {
      //     param: [],
      //     searchParam: {
      //       code: data.params.appServiceName || '',
      //     },
      //   },
      //
      // }),
    }, {
      // name: 'pplx',
      name: 'triggerType',
      type: 'string',
      label: '匹配类型',
      required: true,
      defaultValue: 'refs',
    }, {
      // name: 'branchs',
      name: 'triggerValue',
      type: 'string',
      label: '触发分支',
    }, {
      name: 'gjmb',
      type: 'string',
      label: '构建模板',
    }, {
      name: 'nexusMavenRepoIds',
      type: 'string',
      label: '项目依赖仓库',
      textField: 'name',
      multiple: true,
      valueField: 'repositoryId',
      lookupAxiosConfig: ({ params }) => ({
        method: 'get',
        url: `/rdupm/v1/nexus-repositorys/${organizationId}/project/${projectId}/ci/repo/list?repoType=MAVEN`,
      }),
    }, {
      name: 'private',
      type: 'string',
      // label: 'Setting配置',
      multiple: true,
      // dynamicProps: {
      //   required: ({ record }) => record.get('gjmb') === 'Maven',
      // },
    }, {
      name: 'bzmc',
      type: 'string',
      label: '步骤名称',
      dynamicProps: ({ record, name }) => ({
        required: record.get('type') === 'build',
      }),
    },
    //   {
    //   name: 'yhm',
    //   type: 'string',
    //   label: '用户名',
    //   required: true,
    // }
    {
      name: 'mm',
      type: 'string',
      label: '密码',
    }, {
      name: 'uploadFilePattern',
      type: 'string',
      label: '构建包路径',
      required: true,
    },
    {
      name: 'uploadArtifactFileName',
      type: 'string',
      label: '构建包名称',
      required: true,
    },
    {
      name: 'dockerArtifactFileName',
      type: 'string',
      label: '存储库构建包名称',
    },
    {
      name: 'dockerContextDir',
      type: 'string',
      label: '镜像构建上下文',
      required: true,
    },
    {
      name: 'dockerFilePath',
      type: 'string',
      label: 'Dockerfile文件路径',
      required: true,
    },
    {
      name: 'skipDockerTlsVerify',
      type: 'boolean',
      label: '是否启用TLS校验',
      defaultValue: false,
    },
    {
      name: 'zpk',
      type: 'string',
      label: '目标制品库',
      textField: 'name',
      valueField: 'repositoryId',
      required: true,
      options: ZpkOptionsDs,
    },
    {
      name: 'jar_zpk',
      type: 'string',
      label: '目标制品库',
      textField: 'name',
      valueField: 'repositoryId',
      required: true,
      options: ZpkOptionsDs,
    },
    {
      name: 'scannerType',
      type: 'string',
      label: '检查类型',
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'sonar',
      },
    }, {
      name: 'sources',
      type: 'string',
      label: '扫描路径',
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'sonar' && record.get('scannerType') === 'SonarScanner',
      },
    },
    {
      name: 'skipTests',
      type: 'boolean',
      label: '是否执行Maven单测',
      defaultValue: false,
    },
    {
      name: 'configType',
      type: 'string',
      label: 'SonarQube配置方式',
      defaultValue: 'default',
    },
    {
      name: 'authType',
      type: 'string',
      label: 'SonarQube账号配置',
      defaultValue: 'username',
      dynamicProps: ({ record, name }) => ({
        required: record.get('type') === 'sonar' && record.get('configType') === 'custom',
      }),
    }, {
      name: 'username',
      type: 'string',
      label: 'SonarQube用户名',
      dynamicProps: ({ record, name }) => ({
        required: record.get('type') === 'sonar' && record.get('configType') === 'custom' && record.get('authType') === 'username',
      }),
    }, {
      name: 'password',
      type: 'string',
      label: '密码',
      dynamicProps: ({ record, name }) => ({
        required: record.get('type') === 'sonar' && record.get('configType') === 'custom' && record.get('authType') === 'username',
      }),
    }, {
      name: 'sonarUrl',
      type: 'string',
      label: 'SonarQube地址',
      dynamicProps: ({ record, name }) => ({
        required: record.get('type') === 'sonar' && record.get('configType') === 'custom',
      }),
    }, {
      name: 'token',
      type: 'string',
      label: 'Token',
      dynamicProps: ({ record, name }) => ({
        required: record.get('type') === 'sonar' && record.get('authType') === 'token',
      }),
    }, {
      name: 'selectImage',
      type: 'string',
      label: '',
      defaultValue: '0',
    }, {
      name: 'image',
      type: 'string',
      label: 'CI任务Runner镜像',
      dynamicProps: ({ record, name }) => ({
        required: record.get('type') !== 'custom',
      }),
      validator: checkImage,
      defaultValue: AddTaskUseStore.getDefaultImage,
    }, {
      name: 'share',
      type: 'string',
      multiple: true,
    }],
  });
};
