import uuidV1 from 'uuid/v1';
import { axios } from '@choerodon/boot';
import forEach from 'lodash/forEach';
import JSONbig from 'json-bigint';
import addCDTaskDataSetMap from './addCDTaskDataSetMap';

function getDefaultInstanceName(appServiceCode) {
  return appServiceCode
    ? `${appServiceCode.substring(0, 24)}-${uuidV1().substring(0, 5)}`
    : uuidV1().substring(0, 30);
}

async function checkName(value, projectId, record) {
  if (!record.get('envId')) {
    return true;
  }
  if (!(record.get('type') === 'cdDeploy' && record.get('deployType') === 'create')) {
    return true;
  }
  try {
    const res = await axios.get(
      `/devops/v1/projects/${projectId}/app_service_instances/check_name?env_id=${record.get(
        'envId',
      )}&instance_name=${value}`,
    );
    if ((res && res.failed) || !res) {
      return '格式有误';
    }
    return true;
  } catch (err) {
    return '校验失败';
  }
}

export default (
  projectId,
  PipelineCreateFormDataSet,
  organizationId,
  useStore,
  appServiceCode,
  random,
) => ({
  autoCreate: true,
  fields: [
    {
      name: 'type',
      type: 'string',
      label: '任务类型',
      required: true,
      defaultValue: 'cdDeploy',
    },
    {
      name: 'name',
      type: 'string',
      label: '任务名称',
      required: true,
    },
    {
      name: 'glyyfw',
      type: 'string',
      label: '关联应用服务',
      required: true,
      disabled: true,
    },
    {
      name: addCDTaskDataSetMap.apiTestMission,
      type: 'string',
      label: 'API测试任务',
      textField: 'name',
      valueField: 'id',
      required: true,
      lookupAxiosConfig: () => ({
        method: 'get',
        url: `/test/v1/projects/${projectId}/api_test/tasks/paging?random=${random}`,
        transformResponse: (res) => {
          let newRes = res;
          try {
            newRes = JSON.parse(res);
            useStore.setApiTestArray(newRes.content);
            return newRes;
          } catch (e) {
            return newRes;
          }
        },
      }),
    },
    {
      name: 'triggerType',
      type: 'string',
      label: '匹配类型',
      required: true,
      defaultValue: 'refs',
    },
    {
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
        transformResponse: (res) => {
          let newRes = res;
          try {
            newRes = JSONbig.parse(res);
            useStore.setValueIdList(newRes.filter((r) => r.permission));
            return newRes.filter((r) => r.permission);
          } catch (e) {
            return newRes;
          }
        },
      }),
    },
    {
      // name: 'bsms',
      name: 'deployType',
      type: 'string',
      label: '部署模式',
      defaultValue: 'create',
    },
    {
      name: 'instanceName',
      type: 'string',
      label: '实例名称',
      validator: (value, name, record) => checkName(value, projectId, record),
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'cdDeploy'
          && record.get('deployType') === 'create',
      },
      defaultValue: getDefaultInstanceName(appServiceCode),
    },
    {
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
          url:
            record.get('envId')
            && `/devops/v1/projects/${projectId}/app_service_instances/list_running_and_failed?app_service_id=${PipelineCreateFormDataSet.current.get(
              'appServiceId',
            )}&env_id=${record.get('envId')}`,
        }),
      },
    },
    {
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
          url:
            record.get('envId')
            && `/devops/v1/projects/${projectId}/deploy_value/list_by_env_and_app?app_service_id=${PipelineCreateFormDataSet.current.get(
              'appServiceId',
            )}&env_id=${record.get('envId')}&random=${random}&createValueRandom=${useStore.getValueIdRandom}`,
          transformResponse: (res) => {
            let newRes = res;
            try {
              newRes = JSON.parse(res);
              newRes.push({
                name: '创建部署配置',
                id: 'create',
              });
              useStore.setValueIdList(newRes);
              return newRes;
            } catch (e) {
              newRes.push({
                name: '创建部署配置',
                id: 'create',
              });
              useStore.setValueIdList(newRes);
              return newRes;
            }
          },
        }),
      },
    },
    {
      name: addCDTaskDataSetMap.hostSource,
      type: 'string',
      label: '主机来源',
      defaultValue: addCDTaskDataSetMap.alreadyhost,
    },
    {
      name: addCDTaskDataSetMap.host,
      type: 'string',
      label: '主机',
      lookupAxiosConfig: () => ({
        method: 'post',
        url: `/devops/v1/projects/${projectId}/hosts/page_by_options`,
        data: {
          type: 'deploy',
        },
      }),
    },
    {
      name: 'hostIp',
      type: 'string',
      label: 'IP',
      dynamicProps: {
        disabled: ({ record }) => record.get(addCDTaskDataSetMap.hostSource)
          === addCDTaskDataSetMap.alreadyhost,
        required: ({ record }) => record.get('type') === 'cdHost',
      },
    },
    {
      name: 'hostPort',
      type: 'string',
      label: '端口',
      dynamicProps: {
        disabled: ({ record }) => record.get(addCDTaskDataSetMap.hostSource)
          === addCDTaskDataSetMap.alreadyhost,
        required: ({ record }) => record.get('type') === 'cdHost',
      },
    },
    {
      name: 'accountType',
      type: 'string',
      label: '账号配置',
      defaultValue: 'accountPassword',
    },
    {
      name: 'userName',
      type: 'string',
      label: '用户名',
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'cdHost',
      },
    },
    {
      name: 'password',
      type: 'string',
      label: '密码',
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'cdHost'
          && record.get('accountType') === 'accountPassword',
      },
    },
    {
      name: 'hostDeployType',
      type: 'string',
      label: '部署模式',
      defaultValue: 'image',
    },
    {
      name: 'deploySource',
      type: 'string',
      label: '部署来源',
      defaultValue: 'matchDeploy',
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'cdHost'
          && (record.get('hostDeployType') === 'image'
            || record.get('hostDeployType') === 'jar'),
      },
    },
    {
      name: 'workingPath',
      type: 'string',
      label: '工作目录',
      defaultValue: '/temp',
    },
    {
      name: 'pipelineTask',
      type: 'string',
      label: '关联构建任务',
      textField: 'pipelineTask',
      valueField: 'pipelineTask',
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'cdHost'
          && ((record.get('hostDeployType') === 'image'
            && record.get('deploySource') === 'pipelineDeploy')
            || (record.get('hostDeployType') === 'jar'
              && record.get('deploySource') === 'pipelineDeploy')),
      },
    },
    {
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
            useStore.setRepoList(newData);
            return newData;
          } catch (e) {
            useStore.setRepoList(newData);
            return newData;
          }
        },
      }),
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'cdHost'
          && record.get('hostDeployType') === 'image'
          && record.get('deploySource') === 'matchDeploy',
      },
    },
    {
      name: 'imageId',
      type: 'string',
      label: '镜像',
      textField: 'imageName',
      valueField: 'imageId',
      dynamicProps: {
        disabled: ({ record }) => !record.get('repoId'),
        required: ({ record }) => record.get('type') === 'cdHost'
          && record.get('hostDeployType') === 'image'
          && record.get('deploySource') === 'matchDeploy',
        lookupAxiosConfig: ({ record }) => ({
          method: 'get',
          url:
            record.get('repoId')
            && `rdupm/v1/harbor-choerodon-repos/listHarborImage?repoId=${record.get(
              'repoId',
            )}&repoType=${(function () {
              const { lookup } = record.getField('repoId');
              return lookup?.find(
                (l) => String(l.repoId) === String(record.get('repoId'))
              )?.repoType;
            }())}`,
          transformResponse: (data) => {
            let newData = data;
            try {
              newData = JSON.parse(newData);
              useStore.setImageList(newData);
              return newData;
            } catch (e) {
              useStore.setImageList(newData);
              return newData;
            }
          },
        }),
      },
    },
    {
      name: 'matchType',
      type: 'string',
      label: '匹配类型',
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'cdHost'
          && record.get('hostDeployType') === 'image'
          && record.get('deploySource') === 'matchDeploy',
      },
    },
    {
      name: 'matchContent',
      type: 'string',
      label: '镜像版本匹配',
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'cdHost'
          && record.get('hostDeployType') === 'image'
          && record.get('deploySource') === 'matchDeploy',
      },
    },
    {
      name: 'containerName',
      type: 'string',
      label: '容器名称',
      required: true,
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'cdHost'
          && record.get('hostDeployType') === 'image'
          && (record.get('deploySource') === 'matchDeploy'
            || record.get('deploySource') === 'pipelineDeploy'),
      },
    },
    {
      name: 'serverName',
      type: 'string',
      label: 'Nexus服务',
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'cdHost'
          && record.get('hostDeployType') === 'jar'
          && record.get('deploySource') === 'matchDeploy',
      },
      textField: 'serverName',
      valueField: 'configId',
      lookupAxiosConfig: () => ({
        method: 'get',
        url: `/devops/v1/nexus/choerodon/${organizationId}/project/${projectId}/nexus/server/list`,
      }),
    },
    {
      name: 'repositoryId',
      type: 'string',
      label: '项目制品库',
      textField: 'neRepositoryName',
      valueField: 'repositoryId',
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'cdHost'
          && record.get('hostDeployType') === 'jar'
          && record.get('deploySource') === 'matchDeploy',
        disabled: ({ record }) => !record.get('serverName'),
        lookupAxiosConfig: ({ record }) => ({
          method: 'get',
          url:
            record.get('serverName')
            && `rdupm/v1/nexus-repositorys/choerodon/${organizationId}/project/${projectId}/repo/maven/list?configId=${record.get(
              'serverName',
            )}`,
        }),
      },
    },
    {
      name: 'groupId',
      type: 'string',
      label: 'groupID',
      textField: 'name',
      valueField: 'value',
      dynamicProps: {
        disabled: ({ record }) => !record.get('repositoryId'),
        lookupAxiosConfig: ({ record }) => ({
          method: 'get',
          url:
            record.get('repositoryId')
            && `/rdupm/v1/nexus-repositorys/choerodon/${organizationId}/project/${projectId}/repo/maven/groupId?repositoryId=${record.get(
              'repositoryId',
            )}`,
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
        required: ({ record }) => record.get('type') === 'cdHost'
          && record.get('hostDeployType') === 'jar'
          && record.get('deploySource') === 'matchDeploy',
      },
    },
    {
      name: 'artifactId',
      type: 'string',
      label: 'artifactID',
      textField: 'name',
      valueField: 'value',
      dynamicProps: {
        disabled: ({ record }) => !record.get('repositoryId'),
        required: ({ record }) => record.get('type') === 'cdHost'
          && record.get('hostDeployType') === 'jar'
          && record.get('deploySource') === 'matchDeploy',
        lookupAxiosConfig: ({ record }) => ({
          method: 'get',
          url:
            record.get('repositoryId')
            && `/rdupm/v1/nexus-repositorys/choerodon/${organizationId}/project/${projectId}/repo/maven/artifactId?repositoryId=${record.get(
              'repositoryId',
            )}`,
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
    },
    {
      name: 'versionRegular',
      type: 'string',
      label: 'jar包版本正则匹配',
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'cdHost'
          && record.get('hostDeployType') === 'jar'
          && record.get('deploySource') === 'matchDeploy',
      },
    },
    {
      name: 'triggerValue',
      type: 'string',
      label: '触发分支',
    },
    {
      name: 'pageSize',
      type: 'number',
      defaultValue: 20,
    },
    {
      name: 'cdAuditUserIds',
      type: 'object',
      label: '审核人员',
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'cdAudit',
      },
      textField: 'realName',
      multiple: true,
      valueField: 'id',
      lookupAxiosConfig: ({ params, dataSet }) => {
        const cdAuditIdsArrayObj = dataSet.current?.get('cdAuditUserIds');
        let cdAuditIds = [];
        forEach(cdAuditIdsArrayObj, (obj) => {
          if (typeof obj === 'string') {
            cdAuditIds.push(obj);
          } else if (typeof obj === 'object') {
            cdAuditIds.push(obj?.id);
          }
        });
        if (params.realName && params.id) {
          cdAuditIds = [...cdAuditIds, params.id];
        }
        return {
          method: 'post',
          url: `/devops/v1/projects/${projectId}/users/list_users?page=0&size=20`,
          data: {
            param: [],
            searchParam: {
              realName: params.realName || '',
            },
            ids: cdAuditIds || [],
          },
          transformResponse: (res) => {
            let newRes;
            try {
              newRes = JSON.parse(res);
              if (
                newRes.totalElements % 20 === 0
                && newRes.content.length !== 0
              ) {
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
        };
      },
    },
    {
      name: 'countersigned',
      type: 'number',
      label: '审核模式',
      dynamicProps: {
        required: ({ record }) => record.get('type') === 'cdAudit'
          && record.get('cdAuditUserIds')?.length > 1,
      },
    },
    {
      name: addCDTaskDataSetMap.whetherBlock,
      type: 'boolean',
      label: '是否阻塞后续阶段与任务',
      defaultValue: true,
    },
  ],
});
