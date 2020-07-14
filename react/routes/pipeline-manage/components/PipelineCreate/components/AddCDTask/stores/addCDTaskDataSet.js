export default (projectId, PipelineCreateFormDataSet, organizationId, useStore) => ({
  autoCreate: true,
  fields: [{
    name: 'type',
    type: 'string',
    label: '任务类型',
    required: true,
    defaultValue: 'cdDeploy',
  }, {
    name: 'name',
    type: 'string',
    label: '任务名称',
    required: true,
  }, {
    name: 'glyyfw',
    type: 'string',
    label: '关联应用服务',
    required: true,
    disabled: true,
  }, {
    name: 'triggerType',
    type: 'string',
    label: '匹配类型',
    required: true,
    defaultValue: 'refs',
  }, {
    name: 'triggerValue',
    type: 'string',
    label: '触发分支',
  }, {
    name: 'envId',
    type: 'string',
    label: '环境名称',
    required: true,
    textField: 'name',
    valueField: 'id',
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdDeploy',
    },
    lookupAxiosConfig: () => ({
      method: 'get',
      url: `/devops/v1/projects/${projectId}/envs/list_by_active?active=true`,
    }),
  }, {
    // name: 'bsms',
    name: 'deployType',
    type: 'string',
    label: '部署模式',
    defaultValue: 'create',
  }, {
    name: 'instanceName',
    type: 'string',
    label: '实例名称',
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdDeploy' && record.get('deployType') === 'create',
    },
  }, {
    name: 'instanceId',
    type: 'string',
    label: '选择要替换的实例',
    textField: 'code',
    valueField: 'id',
    dynamicProps: {
      required: ({ record }) => record.get('deployType') === 'update',
      disabled: ({ record }) => !record.get('envId'),
      lookupAxiosConfig: ({ record }) => ({
        method: 'get',
        url: `/devops/v1/projects/${projectId}/app_service_instances/list_running_and_failed?app_service_id=${PipelineCreateFormDataSet.current.get('appServiceId')}&env_id=${record.get('envId')}`,
      }),
    },
  }, {
    name: 'valueId',
    type: 'string',
    label: '部署配置',
    textField: 'name',
    valueField: 'id',
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdDeploy',
      disabled: ({ record }) => !record.get('envId'),
      lookupAxiosConfig: ({ record }) => ({
        method: 'get',
        url: `/devops/v1/projects/${projectId}/deploy_value/list_by_env_and_app?app_service_id=${PipelineCreateFormDataSet.current.get('appServiceId')}&env_id=${record.get('envId')}`,
        transformResponse: (res) => {
          let newRes = res;
          try {
            newRes = JSON.parse(res);
            useStore.setValueIdList(newRes);
            return newRes;
          } catch (e) {
            useStore.setValueIdList(newRes);
            return newRes;
          }
        },
      }),
    },
  }, {
    name: 'hostIp',
    type: 'string',
    label: 'IP',
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdHost',
    },
  }, {
    name: 'hostPort',
    type: 'string',
    label: '端口',
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdHost',
    },
  }, {
    name: 'accountType',
    type: 'string',
    label: '账号配置',
    defaultValue: 'accountPassword',
  }, {
    name: 'userName',
    type: 'string',
    label: '用户名',
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdHost',
    },
  }, {
    name: 'password',
    type: 'string',
    label: '密码',
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdHost' && record.get('accountType') === 'accountPassword',
    },
  }, {
    name: 'accountKey',
    type: 'string',
    label: '密钥',
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdHost' && record.get('accountType') === 'accountKey',
    },
  }, {
    name: 'hostDeployType',
    type: 'string',
    label: '部署模式',
    defaultValue: 'image',
  }, {
    name: 'repoId',
    type: 'string',
    label: '项目镜像仓库',
    textField: 'repoName',
    valueField: 'repoId',
    lookupAxiosConfig: () => ({
      method: 'get',
      url: `/rdupm/v1/harbor-choerodon-repos/listImageRepo?projectId=${projectId}`,
      transformResponse: (data) => {
        let newData = data;
        try {
          newData = JSON.parse(newData);
        } finally {
          useStore.setRepoList(newData);
        }
        return newData;
      },
    }),
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdHost' && record.get('hostDeployType') === 'image',
    },
  }, {
    name: 'imageId',
    type: 'string',
    label: '镜像',
    textField: 'imageName',
    valueField: 'imageId',
    dynamicProps: {
      disabled: ({ record }) => !record.get('repoId'),
      required: ({ record }) => record.get('type') === 'cdHost' && record.get('hostDeployType') === 'image',
      lookupAxiosConfig: ({ record }) => ({
        method: 'get',
        url: `rdupm/v1/harbor-choerodon-repos/listHarborImage?repoId=${record.get('repoId')}&repoType=${(function () {
          const lookup = record.getField('repoId').lookup;
          return lookup?.find(l => String(l.repoId) === String(record.get('repoId')))?.repoType;
        }())}`,
        transformResponse: (data) => {
          let newData = data;
          try {
            newData = JSON.parse(newData);
          } finally {
            useStore.setImageList(newData);
          }
          return newData;
        },
      }),
    },

  }, {
    name: 'matchType',
    type: 'string',
    label: '匹配类型',
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdHost' && record.get('hostDeployType') === 'image',
    },
  }, {
    name: 'matchContent',
    type: 'string',
    label: '镜像版本匹配',
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdHost' && record.get('hostDeployType') === 'image',
    },
  }, {
    name: 'containerName',
    type: 'string',
    label: '容器名称',
    required: true,
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdHost' && record.get('hostDeployType') === 'image',
    },
  }, {
    name: 'serverName',
    type: 'string',
    label: 'Nexus服务',
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdHost' && record.get('hostDeployType') === 'jar',
    },
    textField: 'serverName',
    valueField: 'configId',
    lookupAxiosConfig: () => ({
      method: 'get',
      url: `/devops/v1/nexus/choerodon/${organizationId}/project/${projectId}/nexus/server/list`,
    }),
  }, {
    name: 'repositoryId',
    type: 'string',
    label: '项目制品库',
    textField: 'repositoryId',
    valueField: 'repositoryId',
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdHost' && record.get('hostDeployType') === 'jar',
      disabled: ({ record }) => !record.get('serverName'),
      lookupAxiosConfig: ({ record }) => ({
        method: 'get',
        url: `rdupm/v1/nexus-repositorys/choerodon/${organizationId}/project/${projectId}/repo/maven/list?configId=${record.get('serverName')}`,
      }),
    },
  }, {
    name: 'groupId',
    type: 'string',
    label: 'groupID',
    textField: 'name',
    valueField: 'value',
    dynamicProps: {
      disabled: ({ record }) => !record.get('repositoryId'),
      lookupAxiosConfig: ({ record }) => ({
        method: 'get',
        url: `/rdupm/v1/nexus-repositorys/choerodon/${organizationId}/project/${projectId}/repo/maven/groupId?repositoryId=${record.get('repositoryId')}`,
        transformResponse: (data) => {
          try {
            const array = JSON.parse(data);

            return array.map((i) => ({
              value: i,
              name: i,
            }));
          } catch (e) {
            return data;
          }
        },
      }),
      required: ({ record }) => record.get('type') === 'cdHost' && record.get('hostDeployType') === 'jar',
    },
  }, {
    name: 'artifactId',
    type: 'string',
    label: 'artifactID',
    textField: 'name',
    valueField: 'value',
    dynamicProps: {
      disabled: ({ record }) => !record.get('repositoryId'),
      required: ({ record }) => record.get('type') === 'cdHost' && record.get('hostDeployType') === 'jar',
      lookupAxiosConfig: ({ record }) => ({
        method: 'get',
        url: `/rdupm/v1/nexus-repositorys/choerodon/${organizationId}/project/${projectId}/repo/maven/artifactId?repositoryId=${record.get('repositoryId')}`,
        transformResponse: (data) => {
          try {
            const array = JSON.parse(data);

            return array.map((i) => ({
              value: i,
              name: i,
            }));
          } catch (e) {
            return data;
          }
        },
      }),
    },
  }, {
    name: 'versionRegular',
    type: 'string',
    label: 'jar包版本正则匹配',
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdHost' && record.get('hostDeployType') === 'jar',
    },
  }, {
    name: 'cdAuditUserIds',
    type: 'number',
    label: '审核人员',
    textField: 'realName',
    multiple: true,
    valueField: 'id',
    lookupAxiosConfig: () => ({
      method: 'post',
      url: `/devops/v1/projects/${projectId}/users/list_users?page=0&size=20`,
    }),
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdAudit',
    },
  }, {
    name: 'countersigned',
    type: 'number',
    label: '审核模式',
    dynamicProps: {
      required: ({ record }) => record.get('type') === 'cdAudit' && record.get('cdAuditUserIds')?.length > 1,
    },
  }],
});
