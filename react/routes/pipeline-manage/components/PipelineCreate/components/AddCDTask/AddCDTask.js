/* eslint-disable jsx-a11y/no-static-element-interactions */
/* eslint-disable no-param-reassign */
/* eslint-disable no-template-curly-in-string */
import React, { useEffect, useState, useCallback } from 'react';
import {
  Form,
  Select,
  TextField,
  SelectBox,
  Password,
  Tooltip,
  Button,
  Modal,
} from 'choerodon-ui/pro';
import { Icon, Spin } from 'choerodon-ui';
import { axios } from '@choerodon/boot';
import { Base64 } from 'js-base64';
import { observer } from 'mobx-react-lite';
import DeployConfig from '@/components/deploy-config';
import JSONbig from 'json-bigint';
import addCDTaskDataSetMap from './stores/addCDTaskDataSetMap';
import { useAddCDTaskStore } from './stores';
import YamlEditor from '../../../../../../components/yamlEditor';
import Tips from '../../../../../../components/new-tips';
import './index.less';

let currentSize = 10;

const originBranchs = [
  {
    value: 'master',
    name: 'master',
  },
  {
    value: 'feature',
    name: 'feature',
  },
  {
    value: 'bugfix',
    name: 'bugfix',
  },
  {
    value: 'hotfix',
    name: 'hotfix',
  },
  {
    value: 'release',
    name: 'release',
  },
  {
    value: 'tag',
    name: 'tag',
  },
];

const { Option } = Select;

export default observer(() => {
  const {
    ADDCDTaskDataSet,
    appServiceId,
    PipelineCreateFormDataSet,
    AppState: {
      currentMenuType: { projectId },
    },
    modal,
    ADDCDTaskUseStore,
    handleOk,
    jobDetail,
    pipelineStageMainSource,
  } = useAddCDTaskStore();

  const [branchsList, setBranchsList] = useState([]);
  const [valueIdValues, setValueIdValues] = useState('');
  const [customValues, setCustomValues] = useState('# 自定义ssh指令\n# 比如部署镜像\n# 需要包括部署镜像指令以及二次触发部署的停用删除逻辑\ndocker stop mynginx & docker rm mynginx & docker run --name mynginx -d nginx:latest');
  const [imageDeployValues, setImageDeployValues] = useState('# docker run指令\n# 不可删除${containerName}和${imageName}占位符\n# 不可删除 -d: 后台运行容器\n# 其余参数可参考可根据需要添加\ndocker run --name=${containerName} -d ${imageName}');
  const [jarValues, setJarValues] = useState('# java -jar指令\n# 不可删除${jar}\n# java -jar 后台运行参数会自动添加 不需要在重复添加\n# 其余参数可参考可根据需要添加\njava -jar ${jar}\n# jar包下载存放目录为：/temp-jar/xxx.jar 日志存放目录为：/temp-log/xxx.log\n# 请确保用户有该目录操作权限 ');
  const [testStatus, setTestStatus] = useState('');
  const [accountKeyValue, setAccountKeyValue] = useState('');

  useEffect(() => {
    ADDCDTaskUseStore.setValueIdRandom(Math.random());
  }, []);

  function getMetadata(ds) {
    if (ds.type === 'cdDeploy') {
      ds.value = Base64.encode(valueIdValues);
      // 如果部署模式是新建 则删掉多余的实例id
      if (ds.deployType && ds.deployType === 'create') {
        delete ds.instanceId;
      } else {
        // 如果是替换 则除了传id 还需要传对应的name
        const instanceName = ADDCDTaskUseStore.getInstanceList
          ?.find((i) => i.id === ds.instanceId)?.code;
        ds.instanceName = instanceName;
      }
    }
    if (ds.type === addCDTaskDataSetMap.apiTest) {
      ds.apiTestTaskName = ADDCDTaskUseStore.getApiTestArray
        .find((i) => i.id == ADDCDTaskDataSet.current.get(addCDTaskDataSetMap.apiTestMission)).name;
    }
    if (ds.type === 'cdHost') {
      ds.hostConnectionVO = {
        hostIp: ds.hostIp,
        hostPort: ds.hostPort,
        accountType: ds.accountType,
        userName: ds.userName,
        password: ds.password,
        accountKey: accountKeyValue && Base64.encode(accountKeyValue),
      };
      const currentObj = {
        deploySource: ds.deploySource,
      };
      if (ds.hostDeployType === 'customize') {
        ds.customize = {
          values: Base64.encode(customValues),
        };
      } else if (ds.hostDeployType === 'image') {
        if (ds.deploySource === 'matchDeploy') {
          const repo = ADDCDTaskUseStore.getRepoList?.find(
            (i) => String(i.repoId) === String(ds.repoId)
          );
          const img = ADDCDTaskUseStore.getImageList?.find(
            (i) => String(i.imageId) === String(ds.imageId)
          );
          ds.imageDeploy = {
            ...currentObj,
            repoId: ds.repoId,
            repoName: repo?.repoName,
            repoType: repo?.repoType,
            imageId: ds.imageId,
            imageName: img?.imageName,
            matchType: ds.matchType,
            matchContent: ds.matchContent,
            containerName: ds.containerName,
            value: Base64.encode(imageDeployValues),
          };
        } else if (ds.deploySource === 'pipelineDeploy') {
          ds.imageDeploy = {
            ...currentObj,
            pipelineTask: ds.pipelineTask,
            containerName: ds.containerName,
            value: Base64.encode(imageDeployValues),
          };
        }
      } else if (ds.hostDeployType === 'jar') {
        if (ds.deploySource === 'matchDeploy') {
          ds.jarDeploy = {
            ...currentObj,
            serverName: ds.serverName,
            repositoryId: ds.repositoryId,
            groupId: ds.groupId,
            artifactId: ds.artifactId,
            versionRegular: ds.versionRegular,
            value: Base64.encode(jarValues),
          };
        } else if (ds.deploySource === 'pipelineDeploy') {
          ds.jarDeploy = {
            ...currentObj,
            pipelineTask: ds.pipelineTask,
            value: Base64.encode(jarValues),
          };
        }
        ds.jarDeploy.workingPath = ds.workingPath;
      }
    }

    ds.appServiceId = PipelineCreateFormDataSet.current.get('appServiceId');
    return JSON.stringify(ds).replace(/"/g, "'");
  }

  const handleAdd = async () => {
    const result = await ADDCDTaskDataSet.current.validate(true);
    if (result) {
      const ds = JSON.parse(JSON.stringify(ADDCDTaskDataSet.toData()[0]));
      if (ds.type === 'cdHost') {
        if (!(await handleTestConnect())) {
          return false;
        }
      }
      const cdAuditUserIds = ds.cdAuditUserIds.map((x) => (typeof x === 'object' ? x.id : x));
      const data = {
        ...ds,
        cdAuditUserIds,
        triggerValue:
          typeof ds.triggerValue === 'object'
            ? ds.triggerValue?.join(',')
            : ds.triggerValue,
      };
      if (ds.type !== 'cdAudit') {
        data.metadata = getMetadata(ds);
      }
      handleOk(data);
      return true;
    }
    return false;
  };

  modal.handleOk(handleAdd);

  useEffect(() => {
    if (jobDetail) {
      let newCdAuditUserIds = jobDetail?.cdAuditUserIds;
      let extra = {};
      if (jobDetail.type === 'cdDeploy') {
        const { value } = JSON.parse(jobDetail.metadata.replace(/'/g, '"'));
        value && setValueIdValues(Base64.decode(value));
      } else if (jobDetail.type === 'cdHost') {
        const metadata = JSON.parse(jobDetail.metadata.replace(/'/g, '"'));
        extra = {
          ...metadata?.hostConnectionVO,
          ...metadata?.jarDeploy,
          ...metadata?.imageDeploy,
        };
        if (extra?.accountKey) {
          setAccountKeyValue(Base64.decode(extra.accountKey));
        }
        const { hostDeployType } = metadata;
        if (hostDeployType === 'customize') {
          setCustomValues(Base64.decode(metadata.customize?.values));
        } else if (hostDeployType === 'image') {
          setImageDeployValues(Base64.decode(metadata.imageDeploy.value));
        } else if (hostDeployType === 'jar') {
          setJarValues(Base64.decode(metadata.jarDeploy.value));
        }
      } else if (jobDetail.type === 'cdAudit') {
        if (jobDetail.metadata) {
          const { cdAuditUserIds } = JSON.parse(
            jobDetail.metadata.replace(/'/g, '"'),
          );
          newCdAuditUserIds = cdAuditUserIds && [...cdAuditUserIds];
        }
      }

      const newJobDetail = {
        ...jobDetail,
        ...extra,
        ...(jobDetail.metadata ? JSONbig.parse(jobDetail.metadata.replace(/'/g, '"')) : {}),
        cdAuditUserIds: newCdAuditUserIds && [...newCdAuditUserIds],
        triggerValue:
          jobDetail.triggerType === 'regex'
            ? jobDetail.triggerValue
            : jobDetail.triggerValue?.split(','),
      };
      delete newJobDetail.metadata;
      ADDCDTaskDataSet.loadData([newJobDetail]);
    }
    ADDCDTaskDataSet.current.set(
      'glyyfw',
      appServiceId
        || PipelineCreateFormDataSet.getField('appServiceId').getText(
          PipelineCreateFormDataSet.current.get('appServiceId'),
        ),
    );
    handleClickMore();
  }, [ADDCDTaskDataSet, PipelineCreateFormDataSet, appServiceId, jobDetail]);

  useEffect(() => {
    async function initBranchs() {
      const value = ADDCDTaskDataSet.current.get('triggerType');
      if (value && !value.includes('exact')) {
        setBranchsList(originBranchs);
      } else {
        getBranchsList();
      }
    }
    initBranchs();
  }, [ADDCDTaskDataSet, getBranchsList]);

  const getTestDom = () => {
    const res = {
      loading: (
        <div className="testConnectCD">
          正在进行连接测试
          <Spin />
        </div>
      ),
      success: (
        <div
          style={{
            background: 'rgba(0,191,165,0.04)',
            borderColor: 'rgba(0,191,165,1)',
          }}
          className="testConnectCD"
        >
          <span style={{ color: '#3A345F' }}>测试连接：</span>
          <span style={{ color: '#00BFA5' }}>
            <Icon
              style={{
                border: '1px solid rgb(0, 191, 165)',
                borderRadius: '50%',
                marginRight: 2,
                fontSize: '9px',
              }}
              type="done"
            />
            成功
          </span>
        </div>
      ),
      error: (
        <div
          style={{
            background: 'rgba(247,122,112,0.04)',
            borderColor: 'rgba(247,122,112,1)',
          }}
          className="testConnectCD"
        >
          <span style={{ color: '#3A345F' }}>测试连接：</span>
          <span style={{ color: '#F77A70' }}>
            <Icon
              style={{
                border: '1px solid #F77A70',
                borderRadius: '50%',
                marginRight: 2,
                fontSize: '9px',
              }}
              type="close"
            />
            失败
          </span>
        </div>
      ),
    };
    return res[testStatus];
  };

  const handleTestConnect = async () => new Promise((resolve) => {
    const {
      hostIp,
      hostPort,
      userName,
      password,
      accountType,
    } = ADDCDTaskDataSet.toData()[0];
    axios
      .post(
        `/devops/v1/projects/${projectId}/cicd_pipelines/test_connection`,
        {
          hostIp,
          hostPort,
          userName,
          password,
          accountType,
          accountKey: accountKeyValue && Base64.encode(accountKeyValue),
        },
      )
      .then((res) => {
        setTestStatus(res ? 'success' : 'error');
        resolve(res);
      })
      .catch(() => {
        setTestStatus('error');
        resolve(false);
      });
  });

  const renderRelatedJobOpts = () => {
    const currentHostDeployType = ADDCDTaskDataSet?.current?.get(
      'hostDeployType'
    );
    const tempArr = pipelineStageMainSource
      && pipelineStageMainSource.length > 0
      && pipelineStageMainSource.map((item) => item?.jobList.slice());
    const jobArr = tempArr
      ? tempArr.length > 0 && [].concat.apply(...tempArr)
      : [];
    let filterArr;
    if (jobArr && currentHostDeployType && currentHostDeployType === 'image') {
      filterArr = jobArr.filter(
        (x) => x.configJobTypes?.includes('docker') && x.type === 'build',
      );
    } else if (currentHostDeployType === 'jar') {
      filterArr = jobArr.filter(
        (x) => (x.configJobTypes?.includes('maven_deploy')
            || x.configJobTypes?.includes('upload_jar'))
          && x.type === 'build',
      );
    }
    if (filterArr && filterArr.length > 0) {
      if (typeof filterArr[0] === 'object') {
        ADDCDTaskDataSet.current.set('pipelineTask', filterArr[0].name);
      }
    }
    return (
      filterArr
      && filterArr.length > 0
      && filterArr.map((item) => <Option value={item.name}>{item.name}</Option>)
    );
  };

  function searchMatcher({ record, text }) {
    return record.get('pipelineTask').indexOf(text) !== -1;
  }

  const handleClickCreateValue = (e) => {
    e.preventDefault();
    e.stopPropagation();
    Modal.open({
      key: Modal.key(),
      drawer: true,
      style: {
        width: '740px',
      },
      children: <DeployConfig
        envId={ADDCDTaskDataSet.current.get('envId')}
        appServiceId={PipelineCreateFormDataSet.current.get('appServiceId')}
        appServiceName={appServiceId}
        refresh={({ valueId, value }) => {
          ADDCDTaskUseStore.setValueIdRandom(Math.random());
          ADDCDTaskDataSet.current.set('valueId', valueId);
          // const origin = ADDCDTaskUseStore.getValueIdList;
          setValueIdValues(value);
        }}
      />,
      title: '创建部署配置',
    });
  };

  const rendererValueId = ({ value, text, record }) => (text === '创建部署配置' ? (
    <a style={{ width: '100%', display: 'inline-block' }} role="none" onClick={(e) => handleClickCreateValue(e)}>{text}</a>) : text);

  const optionRenderValueId = ({ value, text, record }) => rendererValueId({ text });

  const renderHostSetting = () => {
    const value = ADDCDTaskDataSet.current.get(addCDTaskDataSetMap.hostSource);
    if (value === addCDTaskDataSetMap.alreadyhost) {
      return [
        <div
          colSpan={2}
          style={{
            display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', position: 'relative',
          }}
          newLine
        >
          <Select style={{ flex: 1 }} name={addCDTaskDataSetMap.host} />
          <div style={{ flex: 1, marginLeft: 16 }}>
            <TextField style={{ width: '100%' }} name="hostIp" />
          </div>
          <div style={{ flex: 1, marginLeft: 16 }}>
            <TextField style={{ width: '100%' }} name="hostPort" />
          </div>
        </div>,
      ];
    }
    return [
      <TextField newLine colSpan={1} name="hostIp" />,
      <TextField colSpan={1} name="hostPort" />,
      <SelectBox colSpan={1} name="accountType" className="addcdTask-mode">
        <Option value="accountPassword">用户名与密码</Option>
        <Option value="accountKey">用户名与密钥</Option>
      </SelectBox>,
      <TextField colSpan={1} newLine name="userName" />,
        ADDCDTaskDataSet?.current?.get('accountType')
        === 'accountPassword' ? (
          <Password colSpan={1} name="password" />
          ) : (
            [
              <p newLine colSpan={1} className="addcdTask-accountKeyP">
                密钥
              </p>,
              <YamlEditor
                colSpan={2}
                newLine
                readOnly={false}
                value={accountKeyValue}
                modeChange={false}
                onValueChange={(data) => setAccountKeyValue(data)}
              />,
            ]
          ),
    ];
  };

  const getOtherConfig = () => {
    function getModeDom() {
      const currentDepoySource = ADDCDTaskDataSet?.current?.get('deploySource');
      const result = {
        customize: (
          <YamlEditor
            colSpan={6}
            newLine
            className="addcdTask-yamleditor"
            readOnly={false}
            value={customValues}
            onValueChange={(data) => setCustomValues(data)}
          />
        ),
        image: [
          <Select
            newLine
            colSpan={3}
            name="deploySource"
            clearButton={false}
            addonAfter={
              <Tips helpText="流水线制品部署表示直接使用所选关联构建任务中产生的镜像进行部署；匹配制品部署则表示可自主选择项目镜像仓库中的镜像，并配置镜像版本的匹配规则，后续部署的镜像版本便会遵循此规则。" />
            }
          >
            <Option value="pipelineDeploy">流水线制品部署</Option>
            <Option value="matchDeploy">匹配制品部署</Option>
          </Select>,
          currentDepoySource === 'pipelineDeploy' && (
            <Select
              colSpan={3}
              name="pipelineTask"
              searchable
              addonAfter={
                <Tips helpText="此处的关联构建任务，仅会查询出该条流水线中存在'Docker构建'步骤的“构建类型”任务。若所选任务中存在多个“Docker构建”步骤，则只会部署第一个“Docker构建”步骤产生的镜像" />
              }
              searchMatcher={searchMatcher}
            >
              {renderRelatedJobOpts()}
            </Select>
          ),
          currentDepoySource === 'matchDeploy' && (
            <Select colSpan={3} name="repoId" />
          ),
          currentDepoySource === 'matchDeploy' && (
            <Select colSpan={3} name="imageId" />
          ),
          currentDepoySource === 'matchDeploy' && (
            <Select colSpan={3} name="matchType">
              <Option value="refs">模糊匹配</Option>
              <Option value="regex">正则匹配</Option>
              <Option value="exact_match">精确匹配</Option>
              <Option value="exact_exclude">精确排除</Option>
            </Select>
          ),
          currentDepoySource === 'matchDeploy' && (
            <TextField colSpan={3} name="matchContent" />
          ),
          <TextField colSpan={3} name="containerName" />,
          <YamlEditor
            colSpan={6}
            className="addcdTask-yamleditor"
            newLine
            readOnly={false}
            value={imageDeployValues}
            onValueChange={(data) => setImageDeployValues(data)}
          />,
        ],
        jar: [
          <Select
            newLine
            colSpan={3}
            name="deploySource"
            clearButton={false}
            addonAfter={
              <Tips helpText="流水线制品部署表示直接使用所选关联构建任务中生成的jar包进行部署；匹配制品部署则表示可自主选择项目下制品库中的jar包，并需配置jar包版本的正则匹配规则，后续部署的jar包版本便会遵循此规则。" />
            }
          >
            <Option value="pipelineDeploy">流水线制品部署</Option>
            <Option value="matchDeploy">匹配制品部署</Option>
          </Select>,
          currentDepoySource === 'pipelineDeploy' && (
            <Select
              colSpan={3}
              name="pipelineTask"
              searchable
              addonAfter={
                <Tips helpText="此处的关联构建任务，仅会查询出该条流水线中存在'上传jar包至制品库'或“Maven发布”步骤的“构建类型”任务。若所选任务中存在多个满足条件的步骤，则只会部署所选任务中第一个满足条件的步骤产生的jar包；" />
              }
              searchMatcher={searchMatcher}
            >
              {renderRelatedJobOpts()}
            </Select>
          ),
          currentDepoySource === 'matchDeploy' && (
            <Select colSpan={3} name="serverName" />
          ),
          currentDepoySource === 'matchDeploy' && (
            <Select colSpan={3} name="repositoryId" />
          ),
          currentDepoySource === 'matchDeploy' && (
            <Select colSpan={3} name="groupId" />
          ),
          currentDepoySource === 'matchDeploy' && (
            <Select colSpan={3} name="artifactId" />
          ),
          currentDepoySource === 'matchDeploy' && (
            <TextField colSpan={6} name="versionRegular" />
          ),
          <TextField
            addonAfter={(
              <Tips helpText="默认工作目录为：/temp，而默认的jar包下载存放目录为：/temp/jar/xxx.jar，默认日志存放目录为：/temp/log/xxx.log
若此处填写了自定义工作目录，即表示，jar包下载存放目录为：工作目录/jar/xxx.jar 日志存放目录为：工作目录/log/xxx.log"
              />
            )}
            colSpan={3}
            name="workingPath"
          />,
          <YamlEditor
            colSpan={6}
            newLine
            className="addcdTask-yamleditor"
            readOnly={false}
            value={jarValues}
            onValueChange={(data) => setJarValues(data)}
          />,
        ],
      };
      return result[ADDCDTaskDataSet?.current?.get('hostDeployType')];
    }
    const obj = {
      cdDeploy: [
        <div className="addcdTask-divided" />,
        <p className="addcdTask-title">配置信息</p>,
        <Form
          className="addcdTask-form2"
          columns={3}
          dataSet={ADDCDTaskDataSet}
        >
          <Select
            colSpan={2}
            name="valueId"
            onChange={(data) => {
              const origin = ADDCDTaskUseStore.getValueIdList;
              setValueIdValues(origin.find((i) => i.id === data).value);
            }}
            optionRenderer={optionRenderValueId}
            renderer={rendererValueId}
          />
          <YamlEditor
            colSpan={3}
            newLine
            readOnly={false}
            className="addcdTask-yamleditor"
            onValueChange={(data) => {
              setValueIdValues(data);
            }}
            value={valueIdValues}
          />
        </Form>,
      ],
      cdHost: [
        <div className="addcdTask-divided" />,
        <p className="addcdTask-title">主机设置</p>,
        <Form
          className="addcdTask-cdHost"
          columns={2}
          dataSet={ADDCDTaskDataSet}
        >
          <SelectBox style={{ top: '10px' }} colSpan={1} name={addCDTaskDataSetMap.hostSource}>
            <Option value={addCDTaskDataSetMap.alreadyhost}>已有主机</Option>
            <Option value={addCDTaskDataSetMap.customhost}>自定义主机</Option>
          </SelectBox>
          {renderHostSetting()}
          <div newLine colSpan={2} style={{ display: 'flex', alignItems: 'center' }}>
            <Button
              disabled={
                ADDCDTaskDataSet.current.get(
                  addCDTaskDataSetMap.hostSource,
                ) === addCDTaskDataSetMap.customhost
                  ? (!ADDCDTaskDataSet.current.get('hostIp')
                || !ADDCDTaskDataSet.current.get('hostPort')
                || !ADDCDTaskDataSet.current.get('userName')) : !ADDCDTaskDataSet.current.get(addCDTaskDataSetMap.host)
              }
              onClick={handleTestConnect}
              style={{ marginRight: 20 }}
              color="primary"
              funcType="raised"
            >
              测试连接
            </Button>
            {getTestDom()}
          </div>
        </Form>,
        <div className="addcdTask-divided" />,
        <p className="addcdTask-title">主机部署</p>,
        <Form style={{ marginTop: 20 }} columns={6} dataSet={ADDCDTaskDataSet}>
          <SelectBox
            className="addcdTask-mainMode"
            colSpan={5}
            name="hostDeployType"
            onChange={(data) => {
              ADDCDTaskDataSet.current.set('hostDeployType', data);
              if (data !== 'customize') {
                ADDCDTaskDataSet.current.set('deploySource', 'pipelineDeploy');
                ADDCDTaskDataSet.current.set('pipelineTask', null);
              }
            }}
          >
            <Option value="image">镜像部署</Option>
            <Option value="jar">jar包部署</Option>
            <Option value="customize">自定义命令</Option>
          </SelectBox>
          ,
          {getModeDom()}
        </Form>,
      ],
      [addCDTaskDataSetMap.apiTest]: [
        <div className="addcdTask-divided" />,
        <p className="addcdTask-title">执行设置</p>,
        <Form style={{ marginTop: 20 }} columns={2} dataSet={ADDCDTaskDataSet}>
          <SelectBox name={addCDTaskDataSetMap.whetherBlock}>
            <Option value>是</Option>
            <Option value={false}>否</Option>
          </SelectBox>
        </Form>,
      ],
    };
    return obj[ADDCDTaskDataSet?.current?.get('type')];
  };

  async function handleClickMore(e, realName) {
    e && e.stopPropagation();
    const pageSize = !e
      ? ADDCDTaskDataSet.current.get('pageSize')
      : ADDCDTaskDataSet.current.get('pageSize') + 20;
    const url = `/devops/v1/projects/${projectId}/users/list_users?page=0&size=${pageSize ?? 20}`;
    const cdAuditsUserIds = [];
    jobDetail?.cdAuditUserIds
      && jobDetail.cdAuditUserIds.forEach((obj) => {
        if (typeof obj === 'string') {
          cdAuditsUserIds.push(obj);
        } else if (typeof obj === 'object') {
          cdAuditsUserIds.push(obj?.id);
        }
      });
    const res = await axios.post(url, {
      param: [],
      searchParam: {
        realName: realName || '',
      },
      ids: cdAuditsUserIds || [],
    });
    if (res.content.length % 20 === 0 && res.content.length !== 0) {
      res.content.push({
        realName: '加载更多',
        id: 'more',
      });
    }
    ADDCDTaskDataSet.current.set('pageSize', pageSize);
    if (realName) {
      ADDCDTaskDataSet.getField('cdAuditUserIds').props.lookup = [
        ...res.content,
        ...ADDCDTaskDataSet.getField('cdAuditUserIds').props.lookup,
      ];
    } else {
      ADDCDTaskDataSet.getField('cdAuditUserIds').props.lookup = res.content;
    }
  }

  const renderderAuditUsersList = ({ text, record }) => (text === '加载更多' ? (
    // eslint-disable-next-line jsx-a11y/click-events-have-key-events
    <a
      style={{ display: 'block', width: '100%', height: '100%' }}
      onClick={handleClickMore}
    >
      {text}
    </a>
  ) : (
    `${text}(${record.get('loginName')})`
  ));

  const getBranchsList = useCallback(async () => {
    const url = `devops/v1/projects/${projectId}/app_service/${PipelineCreateFormDataSet.current.get(
      'appServiceId',
    )}/git/page_branch_by_options?page=1&size=${currentSize}`;
    const res = await axios.post(url);
    if (res.content.length % 10 === 0 && res.content.length !== 0) {
      res.content.push({
        name: '加载更多',
        value: 'more',
      });
    }
    setBranchsList(
      res.content.map((c) => {
        if (c.branchName) {
          c.name = c.branchName;
          c.value = c.branchName;
        }
        return c;
      }),
    );
  }, [PipelineCreateFormDataSet, projectId]);

  const renderderBranchs = ({ text }) => (text === '加载更多' ? (
  // eslint-disable-next-line jsx-a11y/click-events-have-key-events
    <a
      style={{ width: '100%', height: '100%', display: 'block' }}
      onClick={(e) => {
        e.preventDefault();
        e.stopPropagation();
        currentSize += 10;
        getBranchsList();
      }}
    >
      {text}
    </a>
  ) : (
    text
  ));

  function renderTriggerTypeTips() {
    const type = ADDCDTaskDataSet.current.get('triggerType');
    switch (type) {
      case 'refs':
        return '您可在此选择或输入触发该任务的分支类型；支持多填多选；若不填写，则默认为所有分支和tag';
      case 'exact_match':
        return '您可在此精确选择或输入触发该任务的具体分支名称；支持多填多选；若不填写，则默认为所有分支和tag';
      default:
        return '您可在此选择或输入某几个具体的分支名称以此来精确排除；此处支持多填多选；若不填写，则默认为没有需要排除的分支或tag';
    }
  }

  const renderer = ({ record, text }) => (
    <span>
      {text && (
        <i
          style={{
            display: 'inline-block',
            marginRight: 3,
            width: '0.08rem',
            height: '0.08rem',
            borderRadius: '50%',
            backgroundColor: record.get('connected')
              ? 'rgb(0, 191, 165)'
              : '#ff9915',
          }}
        />
      )}
      {text}
    </span>
  );

  const optionRenderer = ({ record, text, value }) => renderer({ record, text, value });

  return (
    <div className="addcdTask">
      <Form columns={3} dataSet={ADDCDTaskDataSet}>
        <Select
          onChange={(data) => ADDCDTaskDataSet.loadData([
            {
              type: data,
              glyyfw:
                  appServiceId
                  || PipelineCreateFormDataSet.getField('appServiceId').getText(
                    PipelineCreateFormDataSet.current.get('appServiceId'),
                  ),
              triggerType: 'refs',
              deployType: 'create',
              accountType: 'accountPassword',
              hostDeployType: 'image',
              deploySource: 'pipelineDeploy',
              [addCDTaskDataSetMap.hostSource]: addCDTaskDataSetMap.alreadyhost,
              workingPath: '/temp',
              name: ADDCDTaskDataSet.current.get('name') || undefined,
            },
          ])}
          colSpan={1}
          name="type"
        >
          <Option value="cdDeploy">部署</Option>
          <Option value="cdHost">主机部署</Option>
          <Option value="cdAudit">人工卡点</Option>
          <Option value={addCDTaskDataSetMap.apiTest}>API测试</Option>
        </Select>
        <TextField colSpan={2} name="name" />
        <TextField colSpan={1} name="glyyfw" />
        <div className="addcdTask-wrap" colSpan={2}>
          <Select
            name="triggerType"
            className="addcdTask-triggerType"
            onChange={() => ADDCDTaskDataSet.current.set('triggerValue', undefined)}
            colSpan={1}
            clearButton={false}
          >
            <Option value="refs">分支类型匹配</Option>
            <Option value="regex">正则匹配</Option>
            <Option value="exact_match">精确匹配</Option>
            <Option value="exact_exclude">精确排除</Option>
          </Select>
          {ADDCDTaskDataSet.current.get('triggerType') === 'regex' ? (
            <TextField
              className="addcdTask-triggerValue"
              name="triggerValue"
              addonAfter={
                <Tips helpText="您可在此输入正则表达式来配置触发分支；例：若想匹配以 feature 开头的分支，可以输入 ^feature.*。更多表达式，详见用户手册。若不填写，则默认为所有分支和tag" />
              }
            />
          ) : (
            <Select
              combo
              searchable
              multiple
              className="addcdTask-triggerValue"
              name="triggerValue"
              addonAfter={<Tips helpText={renderTriggerTypeTips()} />}
              searchMatcher="branchName"
              optionRenderer={({ text }) => renderderBranchs({ text })}
              maxTagCount={2}
              maxTagPlaceholder={(omittedValues) => (
                <Tooltip title={omittedValues.join(',')}>
                  {`+${omittedValues.length}`}
                </Tooltip>
              )}
              renderer={renderderBranchs}
              colSpan={2}
            >
              {branchsList.map((b) => (
                <Option value={b.value}>{b.name}</Option>
              ))}
            </Select>
          )}
        </div>
        {
          ADDCDTaskDataSet.current.get('type') === addCDTaskDataSetMap.apiTest && (
            <Select newLine colSpan={1} name={addCDTaskDataSetMap.apiTestMission} />
          )
        }
        {ADDCDTaskDataSet?.current?.get('type') === 'cdDeploy' && [
          <Select
            colSpan={1}
            name="envId"
            optionRenderer={optionRenderer}
            // renderer={renderer}
            onOption={({ record }) => ({
              disabled: !record.get('connected'),
            })}
          />,
          <SelectBox
            className="addcdTask-mode"
            newLine
            colSpan={1}
            name="deployType"
          >
            <Option value="create">新建实例</Option>
            <Option value="update">替换实例</Option>
          </SelectBox>,
          <p className="addcdTask-text" colSpan={2}>
            <Icon style={{ color: '#F44336' }} type="error" />
            替换实例会更新该实例的镜像及配置信息，请确认要替换的实例选择无误。
          </p>,
          ADDCDTaskDataSet?.current?.get('deployType') === 'create' ? (
            <TextField newLine colSpan={2} name="instanceName" />
          ) : (
            <Select newLine colSpan={2} name="instanceId" />
          ),
        ]}
        {ADDCDTaskDataSet?.current?.get('type') === 'cdAudit' && (
          <div colSpan={3} style={{ display: 'flex' }}>
            <div style={{ width: '47.5%', marginRight: 8 }} colSpan={2}>
              <Select
                popupCls="addcdTask-auditUsers"
                searchable
                style={{ width: '100%' }}
                name="cdAuditUserIds"
                maxTagCount={3}
                searchMatcher="realName"
                onOption={({ record }) => ({
                  disabled: record.get('id') === 'more',
                })}
                onChange={() => {
                  handleClickMore(null);
                }}
                optionRenderer={renderderAuditUsersList}
                renderer={({ text, value }) => (
                  <Tooltip title={`${text}(${value.loginName})`}>
                    {text}
                  </Tooltip>
                )}
              />
            </div>
            {ADDCDTaskDataSet?.current?.get('cdAuditUserIds')?.length > 1 && (
              <div style={{ width: 'calc(100% - 47.5% - 8px)' }} colSpan={1}>
                <Select
                  style={{ width: '100%' }}
                  name="countersigned"
                  addonAfter={
                    <Tips helpText="会签模式中，需要所有审核人员都审核通过才能通过，审核人员中任一人点击终止，则流水线终止；或签模式中，仅需任一审核人员审核即可，即第一个审核的人点击通过则通过，点击终止则终止" />
                  }
                >
                  <Option value={1}>会签</Option>
                  <Option value={0}>或签</Option>
                </Select>
              </div>
            )}
          </div>
        )}
      </Form>
      {getOtherConfig()}
    </div>
  );
});
