import React, { useEffect } from 'react';
import { Form, Select, TextField, SelectBox, Password } from 'choerodon-ui/pro';
import { Icon } from 'choerodon-ui';
import { observer } from 'mobx-react-lite';
import { useAddCDTaskStore } from './stores';
import YamlEditor from '../../../../../../components/yamlEditor';

import './index.less';

const { Option } = Select;

export default observer(() => {
  const {
    ADDCDTaskDataSet,
    appServiceId,
    PipelineCreateFormDataSet,
  } = useAddCDTaskStore();

  useEffect(() => {
    ADDCDTaskDataSet.current.set('glyyfw', appServiceId || PipelineCreateFormDataSet.getField('appServiceId').getText(PipelineCreateFormDataSet.current.get('appServiceId')));
  }, []);

  const getOtherConfig = () => {
    function getModeDom() {
      const result = {
        customize: (
          <YamlEditor
            colSpan={6}
            newLine
            readOnly
            value="123"
          />
        ),
        image: [
          <Select newLine colSpan={3} name="imageRepo" />,
          <Select colSpan={3} name="image" />,
          <Select colSpan={3} name="triggerType" />,
          <TextField colSpan={3} name="versionMatch" />,
          <YamlEditor
            colSpan={6}
            newLine
            readOnly
            value="123"
          />,
        ],
        jar: [
          <Select newLine colSpan={3} name="nexus" />,
          <Select colSpan={3} name="product" />,
          <Select colSpan={3} name="groupId" />,
          <TextField colSpan={3} name="artifactId" />,
          <TextField colSpan={6} name="jarMatch" />,
          <YamlEditor
            colSpan={6}
            newLine
            readOnly
            value="123"
          />,
        ],
      };
      return result[ADDCDTaskDataSet?.current?.get('mode')];
    }
    const obj = {
      cdDeploy: [
        <div className="addcdTask-divided" />,
        <p className="addcdTask-title">配置信息</p>,
        <Form className="addcdTask-form2" columns={3} dataSet={ADDCDTaskDataSet}>
          <Select colSpan={2} name="valueId" />
          <YamlEditor
            colSpan={3}
            newLine
            readOnly
            value="123"
          />
        </Form>,
      ],
      cdHost: [
        <div className="addcdTask-divided" />,
        <p className="addcdTask-title">主机设置</p>,
        <Form columns={2} dataSet={ADDCDTaskDataSet}>
          <TextField colSpan={1} name="ip" />
          <TextField colSpan={1} name="port" />
          <SelectBox colSpan={1} name="accountConfig">
            <Option value="accountPassword">用户名与密码</Option>
            <Option value="accountKey">用户名与密钥</Option>
          </SelectBox>
          <TextField colSpan={1} newLine name="account" />
          {
            ADDCDTaskDataSet?.current?.get('accountConfig') === 'accountPassword' ? (
              <Password colSpan={1} name="accountPassword" />
            ) : (
              <Password colSpan={1} name="accountKey" />
            )
          }
        </Form>,
        <div className="addcdTask-divided" />,
        <p className="addcdTask-title">主机部署</p>,
        <Form style={{ marginTop: 20 }} columns={6} dataSet={ADDCDTaskDataSet}>
          <SelectBox className="addcdTask-mainMode" colSpan={5} name="mode">
            <Option value="customize">自定义命令</Option>
            <Option value="image">镜像部署</Option>
            <Option value="jar">jar包部署</Option>
          </SelectBox>,
          {
            getModeDom()
          }
        </Form>,
      ],
    };
    return obj[ADDCDTaskDataSet?.current?.get('type')];
  };

  return (
    <div className="addcdTask">
      <Form columns={3} dataSet={ADDCDTaskDataSet}>
        <Select colSpan={1} name="type">
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
          >
            <Option value="refs">分支类型匹配</Option>
            <Option value="regex">正则匹配</Option>
            <Option value="exact_match">精确匹配</Option>
            <Option value="exact_exclude">精确排除</Option>
          </Select>
          <TextField className="addcdTask-triggerValue" name="triggerValue" />
        </div>
        {
          ADDCDTaskDataSet?.current?.get('type') === 'cdDeploy' && [
            <Select colSpan={1} name="envId" />,
            <SelectBox className="addcdTask-mode" newLine colSpan={1} name="bsms">
              <Option value="new">新建实例</Option>
              <Option value="update">替换实例</Option>
            </SelectBox>,
            <p className="addcdTask-text" colSpan={2}><Icon style={{ color: '#F44336' }} type="error" />替换实例会更新该实例的镜像及配置信息，请确认要替换的实例选择无误。</p>,
            ADDCDTaskDataSet?.current?.get('bsms') === 'new' ? (
              <TextField newLine colSpan={2} name="instanceName" />
            ) : (
              <Select newLine colSpan={2} name="instanceId" />
            ),
          ]
        }
        {
          ADDCDTaskDataSet?.current?.get('type') === 'cdAudit' && (
            <div colSpan={3} style={{ display: 'flex' }}>
              <div style={{ width: '51.1%', marginRight: 8 }}>
                <Select style={{ width: '100%' }} name="shry" />
              </div>
              <div style={{ width: 'calc(100% - 51.1% - 8px)' }}>
                <Select style={{ width: '100%' }} name="shms" />
              </div>
            </div>
          )
        }
      </Form>
      {getOtherConfig()}
    </div>
  );
});
