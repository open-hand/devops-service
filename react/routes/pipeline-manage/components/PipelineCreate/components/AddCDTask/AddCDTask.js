/* eslint-disable no-template-curly-in-string */
import React, { useEffect, useState, useCallback } from 'react';
import { Form, Select, TextField, SelectBox, Password, Tooltip, Button } from 'choerodon-ui/pro';
import { Icon, Spin } from 'choerodon-ui';
import { axios } from '@choerodon/boot';
import { Base64 } from 'js-base64';
import { observer } from 'mobx-react-lite';
import { useAddCDTaskStore } from './stores';
import YamlEditor from '../../../../../../components/yamlEditor';

import './index.less';

let currentSize = 10;

const originBranchs = [{
  value: 'master',
  name: 'master',
}, {
  value: 'feature',
  name: 'feature',
}, {
  value: 'bugfix',
  name: 'bugfix',
}, {
  value: 'hotfix',
  name: 'hotfix',
}, {
  value: 'release',
  name: 'release',
}, {
  value: 'tag',
  name: 'tag',
}];

const { Option } = Select;

export default observer(() => {
  const {
    ADDCDTaskDataSet,
    appServiceId,
    PipelineCreateFormDataSet,
    AppState: { currentMenuType: { projectId } },
    modal,
    ADDCDTaskUseStore,
    handleOk,
    jobDetail,
  } = useAddCDTaskStore();

  const [branchsList, setBranchsList] = useState([]);
  const [valueIdValues, setValueIdValues] = useState('');
  const [customValues, setCustomValues] = useState('');
  const [imageDeployValues, setImageDeployValues] = useState('docker run --name=${containerName} -d ${imageName}');
  const [jarValues, setJarValues] = useState('# java -jar指令\n# 不可删除${jar}\n# java -jar 后台运行参数会自动添加 不需要在重复添加\n# 其余参数可参考可根据需要添加java -jar ${jar} ');
  const [testStatus, setTestStatus] = useState('');

  const handleAdd = async () => {
    const result = await ADDCDTaskDataSet.validate();
    if (result) {
      const ds = JSON.parse(JSON.stringify(ADDCDTaskDataSet.toData()[0]));
      if (ds.type === 'cdHost') {
        if (!await handleTestConnect()) {
          return false;
        }
        // if (!await handleTestValue()) {
        //   return false;
        // }
      }
      const data = {
        ...ds,
        triggerValue: typeof ds.triggerValue === 'object' ? ds.triggerValue?.join(',') : ds.triggerValue,
        metadata: (function () {
          if (ds.type === 'cdDeploy') {
            ds.value = Base64.encode(valueIdValues);
          }
          if (ds.type === 'cdHost') {
            ds.hostConnectionVO = {
              hostIp: ds.hostIp,
              hostPort: ds.hostPort,
              accountType: ds.accountType,
              userName: ds.userName,
              password: ds.password,
              accountKey: ds.accountKey,
            };
            if (ds.hostDeployType === 'customize') {
              ds.customize = Base64.encode(customValues);
            } else if (ds.hostDeployType === 'image') {
              ds.imageDeploy = {
                repoId: ds.repoId,
                repoName: ADDCDTaskUseStore.getRepoList?.find(i => String(i.repoId) === String(ds.repoId))?.repoName,
                repoType: ADDCDTaskUseStore.getRepoList?.find(i => String(i.repoId) === String(ds.repoId))?.repoType,
                imageId: ds.imageId,
                imageName: ADDCDTaskUseStore.getImageList?.find(i => String(i.imageId) === String(ds.imageId))?.imageName,
                matchType: ds.matchType,
                matchContent: ds.matchContent,
                containerName: ds.containerName,
                value: Base64.encode(imageDeployValues),
              };
            } else if (ds.hostDeployType === 'jar') {
              ds.jarDeploy = {
                serverName: ds.serverName,
                repositoryId: ds.repositoryId,
                groupId: ds.groupId,
                artifactId: ds.artifactId,
                versionRegular: ds.versionRegular,
                value: Base64.encode(jarValues),
              };
            }
          }
          ds.appServiceId = PipelineCreateFormDataSet.current.get('appServiceId');
          return JSON.stringify(ds).replace(/"/g, "'");
        }()),
      };
      handleOk(data);
      return true;
    }
    return false;
  };

  modal.handleOk(handleAdd);

  useEffect(() => {
    if (jobDetail) {
      let newCdAuditUserIds;
      if (jobDetail.type === 'cdDeploy') {
        const { value } = JSON.parse(jobDetail.metadata.replace(/'/g, '"'));
        value && setValueIdValues(Base64.decode(value));
      } else if (jobDetail.type === 'cdHost') {
        const metadata = JSON.parse(jobDetail.metadata.replace(/'/g, '"'));
        const { hostDeployType } = metadata;
        if (hostDeployType === 'customize') {
          setCustomValues(Base64.decode(metadata.customize));
        } else if (hostDeployType === 'image') {
          setImageDeployValues(Base64.decode(metadata.imageDeploy.value));
        } else if (hostDeployType === 'jar') {
          setJarValues(Base64.decode(metadata.jarDeploy.value));
        }
      } else if (jobDetail.type === 'cdAudit') {
        const { cdAuditUserIds } = JSON.parse(jobDetail.metadata.replace(/'/g, '"'));
        newCdAuditUserIds = cdAuditUserIds;
      }

      const newJobDetail = {
        ...jobDetail,
        ...JSON.parse(jobDetail.metadata.replace(/'/g, '"')),
        cdAuditUserIds: newCdAuditUserIds,
        triggerValue: jobDetail.triggerType === 'regex' ? jobDetail.triggerValue : jobDetail.triggerValue?.split(','),
      };
      delete newJobDetail.metadata;
      ADDCDTaskDataSet.loadData([newJobDetail]);
    }
    ADDCDTaskDataSet.current.set('glyyfw', appServiceId || PipelineCreateFormDataSet.getField('appServiceId').getText(PipelineCreateFormDataSet.current.get('appServiceId')));
  }, []);

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
  }, [ADDCDTaskDataSet.current.get('triggerType')]);

  const getTestDom = () => {
    const res = {
      loading: (
        <div className="testConnectCD">
          正在进行连接测试<Spin />
        </div>
      ),
      success: (
        <div style={{ background: 'rgba(0,191,165,0.04)', borderColor: 'rgba(0,191,165,1)' }} className="testConnectCD">
          <span style={{ color: '#3A345F' }}>测试连接：</span><span style={{ color: '#00BFA5' }}><Icon style={{ border: '1px solid rgb(0, 191, 165)', borderRadius: '50%', marginRight: 2, fontSize: '9px' }} type="done" />成功</span>
        </div>
      ),
      error: (
        <div style={{ background: 'rgba(247,122,112,0.04)', borderColor: 'rgba(247,122,112,1)' }} className="testConnectCD">
          <span style={{ color: '#3A345F' }}>测试连接：</span><span style={{ color: '#F77A70' }}><Icon style={{ border: '1px solid #F77A70', borderRadius: '50%', marginRight: 2, fontSize: '9px' }} type="close" />失败</span>
        </div>
      ),
    };
    return res[testStatus];
  };

  useEffect(() => {
    const type = ADDCDTaskDataSet.current.get('type');
    modal.update({
      okProps: {
        disabled: (function () {
          if (type === 'cdHost') {
            return testStatus !== 'success';
          } else {
            return false;
          }
        }()),
      },
    });
  }, [testStatus, ADDCDTaskDataSet.current.get('type')]);

  // const handleTestValue = async () => new Promise((resolve) => {
  //   const hostDeployType = ADDCDTaskDataSet.current.get('hostDeployType');
  //   axios.post('/devops/v1/cd_pipeline/check/instruction', {
  //     type: hostDeployType,
  //     instruction: hostDeployType === 'image' ? imageDeployValues : jarValues,
  //   }).then((res) => {
  //     resolve(res);
  //   }).catch(() => {
  //     resolve(false);
  //   });
  // });

  const handleTestConnect = async () => new Promise((resolve) => {
    const { hostIp, hostPort, userName, password, accountType } = ADDCDTaskDataSet.toData()[0];
    axios.post(`/devops/v1/projects/${projectId}/cicd_pipelines/test_connection`, {
      hostIp,
      hostPort,
      userName,
      password,
      accountType,
    }).then((res) => {
      setTestStatus(res ? 'success' : 'error');
      resolve(res);
    }).catch(() => {
      setTestStatus('error');
      resolve(false);
    });
  });

  const getOtherConfig = () => {
    function getModeDom() {
      const result = {
        customize: (
          <YamlEditor
            colSpan={6}
            newLine
            readOnly={false}
            value={customValues}
            onValueChange={(data) => setCustomValues(data)}
          />
        ),
        image: [
          <Select newLine colSpan={3} name="repoId" />,
          <Select colSpan={3} name="imageId" />,
          <Select colSpan={3} name="matchType">
            <Option value="refs">模糊匹配</Option>
            <Option value="regex">正则匹配</Option>
            <Option value="exact_match">精确匹配</Option>
            <Option value="exact_exclude">精确排除</Option>
          </Select>,
          <TextField colSpan={3} name="matchContent" />,
          <TextField colSpan={3} name="containerName" />,
          <YamlEditor
            colSpan={6}
            newLine
            readOnly={false}
            value={imageDeployValues}
            onValueChange={(data) => setImageDeployValues(data)}
          />,
        ],
        jar: [
          <Select newLine colSpan={3} name="serverName" />,
          <Select colSpan={3} name="repositoryId" />,
          <Select colSpan={3} name="groupId" />,
          <Select colSpan={3} name="artifactId" />,
          <TextField colSpan={6} name="versionRegular" />,
          <YamlEditor
            colSpan={6}
            newLine
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
        <Form className="addcdTask-form2" columns={3} dataSet={ADDCDTaskDataSet}>
          <Select
            colSpan={2}
            name="valueId"
            onChange={(data) => {
              const origin = ADDCDTaskUseStore.getValueIdList;
              setValueIdValues(origin.find(i => i.id === data).value);
            }}
          />
          <YamlEditor
            colSpan={3}
            newLine
            readOnly={false}
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
        <Form columns={2} dataSet={ADDCDTaskDataSet}>
          <TextField colSpan={1} name="hostIp" />
          <TextField colSpan={1} name="hostPort" />
          <SelectBox colSpan={1} name="accountType">
            <Option value="accountPassword">用户名与密码</Option>
            <Option value="accountKey">用户名与密钥</Option>
          </SelectBox>
          <TextField colSpan={1} newLine name="userName" />
          {
            ADDCDTaskDataSet?.current?.get('accountType') === 'accountPassword' ? (
              <Password colSpan={1} name="password" />
            ) : (
              <Password colSpan={1} name="accountKey" />
            )
          }
          <div colSpan={2} style={{ display: 'flex', alignItems: 'center' }}>
            <Button
              disabled={!ADDCDTaskDataSet.current.get('hostIp') || !ADDCDTaskDataSet.current.get('hostPort') || !ADDCDTaskDataSet.current.get('userName')}
              onClick={handleTestConnect}
              style={{ marginRight: 20 }}
              color="primary"
              funcType="raised"
            >测试连接</Button>
            {getTestDom()}
          </div>
        </Form>,
        <div className="addcdTask-divided" />,
        <p className="addcdTask-title">主机部署</p>,
        <Form style={{ marginTop: 20 }} columns={6} dataSet={ADDCDTaskDataSet}>
          <SelectBox className="addcdTask-mainMode" colSpan={5} name="hostDeployType">
            <Option value="image">镜像部署</Option>
            <Option value="jar">jar包部署</Option>
            <Option value="customize">自定义命令</Option>
          </SelectBox>,
          {
            getModeDom()
          }
        </Form>,
      ],
    };
    return obj[ADDCDTaskDataSet?.current?.get('type')];
  };

  const getBranchsList = useCallback(async () => {
    const url = `devops/v1/projects/${projectId}/app_service/${PipelineCreateFormDataSet.current.get('appServiceId')}/git/page_branch_by_options?page=1&size=${currentSize}`;
    const res = await axios.post(url);
    if (res.content.length % 10 === 0 && res.content.length !== 0) {
      res.content.push({
        name: '加载更多',
        value: 'more',
      });
    }
    setBranchsList(res.content.map((c) => {
      if (c.branchName) {
        c.name = c.branchName;
        c.value = c.branchName;
      }
      return c;
    }));
  }, [currentSize]);

  const renderderBranchs = ({ text }) => (text === '加载更多' ? (
    <a
      onClick={(e) => {
        e.preventDefault();
        e.stopPropagation();
        currentSize += 10;
        getBranchsList();
      }}
    >{text}</a>
  ) : text);

  return (
    <div className="addcdTask">
      <Form columns={3} dataSet={ADDCDTaskDataSet}>
        <Select
          onChange={(data) => ADDCDTaskDataSet.loadData([{
            type: data,
            glyyfw: appServiceId || PipelineCreateFormDataSet.getField('appServiceId').getText(PipelineCreateFormDataSet.current.get('appServiceId')),
            triggerType: 'refs',
            deployType: 'create',
            accountType: 'accountPassword',
            hostDeployType: 'image',
          }])}
          colSpan={1}
          name="type"
        >
          <Option value="cdDeploy">部署</Option>
          <Option value="cdHost">主机部署</Option>
          <Option value="cdAudit">人工卡点</Option>
        </Select>
        <TextField colSpan={2} name="name" />
        <TextField colSpan={1} name="glyyfw" />
        <div className="addcdTask-wrap" colSpan={2}>
          <Select
            name="triggerType"
            className="addcdTask-triggerType"
            onChange={() => ADDCDTaskDataSet.current.set('triggerValue', undefined)}
            colSpan={1}
          >
            <Option value="refs">分支类型匹配</Option>
            <Option value="regex">正则匹配</Option>
            <Option value="exact_match">精确匹配</Option>
            <Option value="exact_exclude">精确排除</Option>
          </Select>
          {
            ADDCDTaskDataSet.current.get('triggerType') === 'regex' ? (
              <TextField className="addcdTask-triggerValue" name="triggerValue" />
            ) : (
              <Select
                combo
                searchable
                multiple
                className="addcdTask-triggerValue"
                name="triggerValue"
                showHelp={ADDCDTaskDataSet.current.get('triggerType') !== 'exact_exclude' ? 'tooltip' : 'none'}
                help="您可以在此输入或选择触发该任务的分支类型，若不填写，则默认为所有分支或tag"
                searchMatcher="branchName"
                optionRenderer={({ text }) => renderderBranchs({ text })}
                maxTagCount={2}
                maxTagPlaceholder={(omittedValues) => <Tooltip title={omittedValues.join(',')}>
                  {`+${omittedValues.length}`}
                </Tooltip>}
                renderer={renderderBranchs}
                colSpan={2}
              >
                {branchsList.map(b => (
                  <Option value={b.value}>{b.name}</Option>
                ))}
              </Select>
            )
          }
        </div>
        {
          ADDCDTaskDataSet?.current?.get('type') === 'cdDeploy' && [
            <Select colSpan={1} name="envId" />,
            <SelectBox className="addcdTask-mode" newLine colSpan={1} name="deployType">
              <Option value="create">新建实例</Option>
              <Option value="update">替换实例</Option>
            </SelectBox>,
            <p className="addcdTask-text" colSpan={2}><Icon style={{ color: '#F44336' }} type="error" />替换实例会更新该实例的镜像及配置信息，请确认要替换的实例选择无误。</p>,
            ADDCDTaskDataSet?.current?.get('deployType') === 'create' ? (
              <TextField newLine colSpan={2} name="instanceName" />
            ) : (
              <Select newLine colSpan={2} name="instanceId" />
            ),
          ]
        }
        {
          ADDCDTaskDataSet?.current?.get('type') === 'cdAudit' && (
            <div colSpan={3} style={{ display: 'flex' }}>
              <div style={{ width: '47.5%', marginRight: 8 }} colSpan={2}>
                <Select style={{ width: '100%' }} name="cdAuditUserIds" />
              </div>
              {
                ADDCDTaskDataSet?.current?.get('cdAuditUserIds')?.length > 1 && (
                  <div style={{ width: 'calc(100% - 47.5% - 8px)' }} colSpan={1}>
                    <Select style={{ width: '100%' }} name="countersigned">
                      <Option value={1}>会签</Option>
                      <Option value={0}>或签</Option>
                    </Select>
                  </div>
                )
              }
            </div>
          )
        }
      </Form>
      {getOtherConfig()}
    </div>
  );
});
