import React, { useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Select, TextField, Modal, SelectBox, Button } from 'choerodon-ui/pro';
import { Icon } from 'choerodon-ui';
import YamlEditor from '../../../../../../components/yamlEditor';
import { useAddTaskStore } from './stores';

import './index.less';

const { Option } = Select;

const AddTask = observer(() => {
  const {
    AddTaskFormDataSet,
  } = useAddTaskStore();

  const [steps, setSteps] = useState([{
    name: 'Maven构建',
    checked: true,
    children: (
      <div
        style={{
          marginTop: 20,
        }}
      >
        <YamlEditor
          readOnly={false}
          colSpan={2}
          newLine
          modeChange={false}
        />
      </div>
    ),
  }, {
    name: '上传软件包至发布库',
    checked: false,
    children: (
      <React.Fragment>
        <TextField style={{ width: 339, marginTop: 27 }} newLine name="yhm" />
        <TextField style={{ width: 339, marginTop: 27 }} newLine name="mm" />
        <TextField style={{ width: 339, marginTop: 27 }} newLine name="gjblj" />
      </React.Fragment>
    ),
  }]);

  const handleClickStepItem = (index) => {
    setSteps(steps.map((s, sIndex) => {
      if (sIndex === index) {
        s.checked = true;
      } else {
        s.checked = false;
      }
      return s;
    }));
  };

  const handleAddStepItem = (index) => {
    Modal.open({
      key: Modal.key(),
      title: '添加步骤',
      style: {
        width: 380,
      },
      children: (
        <Form>
          <Select label="可用步骤" />
        </Form>
      ),
      drawer: true,
      okText: '添加',
    });
  };

  const generateSteps = () => (
    <div className="AddTask_stepItemsContainer">
      {
          steps.map((s, index) => (
            <div className="AddTask_stepMapContent">
              <div style={{ display: index === 0 ? 'flex' : 'none' }} className="AddTask_stepAdd">
                <span onClick={() => handleAddStepItem(index)} style={{ fontSize: 20 }}>+</span>
              </div>
              <div className="AddTask_addLine" />
              <div onClick={() => handleClickStepItem(index)} className={s.checked ? 'AddTask_stepItem AddTask_stepItemChecked' : 'AddTask_stepItem'}>
                {s.name}
                <Icon style={{ position: 'relative', bottom: '1px' }} type="delete_forever" />
              </div>
              <div className="AddTask_addLine" />
              <div className="AddTask_stepAdd">
                <span onClick={() => handleAddStepItem(index + 1)} style={{ fontSize: 20 }}>+</span>
              </div>
            </div>
          ))
        }
    </div>
  );

  const getMissionOther = () => {
    if (AddTaskFormDataSet.current.get('rwlx') === 'gj') {
      return [
        <div colSpan={2} className="AddTask_configStep">
          <p>配置步骤</p>
        </div>,
        <Select name="gjmb" />,
        <div newLine colSpan={2} style={{ display: 'flex', flexDirection: 'column' }} className="AddTask_stepContent">
          {generateSteps()}
          <TextField style={{ width: 339, marginTop: 30 }} newLine name="bzmc" />
          {
            steps.find(s => s.checked).children
          }
        </div>,
      ];
    } else {
      let extra;
      if (AddTaskFormDataSet.current.get('sonarQube') === 'M') {
        extra = [
          <TextField newLine name="sqyhm" />,
          <TextField name="sqmm" />,
          <TextField name="sqdz" />,
        ];
      } else {
        extra = [
          <TextField newLine name="token" />,
          <TextField name="sqdz" />,
        ];
      }
      return [
        <SelectBox name="sonarQube">
          <Option value="M">用户名与密码</Option>
          <Option value="F">Token</Option>
        </SelectBox>,
        ...extra,
        <Button funcType="raised" style={{ width: 76, color: '#3F51B5' }} newLine>测试连接</Button>,
        <div className="addTask_testConnect" newLine>
          测试连接：<span>成功 <i /></span>
        </div>,
      ];
    }
  };

  return (
    <React.Fragment>
      <Form dataSet={AddTaskFormDataSet} columns={2}>
        <Select name="rwlx">
          <Option value="gj">构建</Option>
          <Option value="dmjc">代码检查</Option>
        </Select>
        <TextField name="rwmc" />
        <Select name="glyyfw" />
        <Select name="cffzlx" />
        {
          getMissionOther()
        }
      </Form>
    </React.Fragment>
  );
});

export default AddTask;
