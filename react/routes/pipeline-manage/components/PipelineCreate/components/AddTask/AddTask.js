import React, { useState, useEffect } from 'react';
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
    AddTaskStepFormDataSet,
    modal,
    handleOk,
  } = useAddTaskStore();

  const [steps, setSteps] = useState([]);
  const [testConnect, setTestConnect] = useState('');

  const handleAdd = () => {
    const data = 'fuck you';
    handleOk(data);
    window.console.log(testConnect);
  };

  useEffect(() => {
    if (AddTaskFormDataSet.current.get('rwlx') === 'dmjc') {
      modal.update({
        okProps: {
          disabled: !testConnect,
        },
      });
    }
  }, [testConnect, AddTaskFormDataSet.current.get('rwlx')]);

  modal.handleOk(handleAdd);

  const handleClickStepItem = (index) => {
    setSteps(steps.map((s, sIndex) => {
      if (sIndex === index) {
        s.checked = true;
        AddTaskFormDataSet.current.set('bzmc', s.name);
      } else {
        s.checked = false;
      }
      return s;
    }));
  };

  const handleShowTestConnectResult = () => {
    if (typeof testConnect === 'boolean') {
      if (testConnect) {
        return (
          <span className="addTask_testConnect_success">成功 <i /></span>
        );
      } else {
        return (
          <span className="addTask_testConnect_failure">失败 <Icon type="close" /></span>
        );
      }
    }
    return '';
  };

  const handleAddStepItem = (index) => {
    Modal.open({
      key: Modal.key(),
      title: '添加步骤',
      style: {
        width: 380,
      },
      children: (
        <Form dataSet={AddTaskStepFormDataSet}>
          <Select name="kybz">
            <Option value="Maven">Maven构建</Option>
          </Select>
        </Form>
      ),
      drawer: true,
      okText: '添加',
      onOk: () => {
        if (AddTaskStepFormDataSet.current && AddTaskStepFormDataSet.current.get('kybz')) {
          if (AddTaskStepFormDataSet.current.get('kybz') === 'Maven') {
            const newSteps = steps;
            newSteps.splice(index, 0, {
              name: 'Maven构建',
              value: 'Maven',
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
            });
            setSteps(newSteps.map((s, sIndex) => {
              if (sIndex === index) {
                s.checked = true;
                AddTaskFormDataSet.current.set('bzmc', s.name);
              } else {
                s.checked = false;
              }
              return s;
            }));
          }
        }
        AddTaskStepFormDataSet.reset();
      },
      onCancel: () => {
        AddTaskStepFormDataSet.reset();
      },
    });
  };

  const handleDeleteStep = (index, e) => {
    e.stopPropagation();
    const newSteps = steps;
    newSteps.splice(index, 1);
    const newSteps2 = newSteps.map((s, sIndex) => {
      if (index === 0) {
        if (sIndex === 0) {
          s.checked = true;
          AddTaskFormDataSet.current.set('bzmc', s.name);
        } else {
          s.checked = false;
        }
      } else if (sIndex === (index - 1)) {
        s.checked = true;
        AddTaskFormDataSet.current.set('bzmc', s.name);
      } else {
        s.checked = false;
      }
      return s;
    });
    setSteps(newSteps2);
  };

  const generateSteps = () => (
    <div className="AddTask_stepItemsContainer">
      {
          steps.length > 0 ? steps.map((s, index) => (
            <div className="AddTask_stepMapContent">
              <div style={{ display: index === 0 ? 'flex' : 'none' }} className="AddTask_stepAdd">
                <span onClick={() => handleAddStepItem(index)} style={{ fontSize: 20 }}>+</span>
              </div>
              <div className="AddTask_addLine" />
              <div onClick={() => handleClickStepItem(index)} className={s.checked ? 'AddTask_stepItem AddTask_stepItemChecked' : 'AddTask_stepItem'}>
                {s.name}
                <Icon onClick={(e) => handleDeleteStep(index, e)} style={{ position: 'relative', bottom: '1px' }} type="delete_forever" />
              </div>
              <div className="AddTask_addLine" />
              <div className="AddTask_stepAdd">
                <span onClick={() => handleAddStepItem(index + 1)} style={{ fontSize: 20 }}>+</span>
              </div>
            </div>
          )) : (
            <div className="AddTask_stepMapContent">
              <div className="AddTask_stepAdd">
                <span onClick={() => handleAddStepItem(0)} style={{ fontSize: 20 }}>+</span>
              </div>
            </div>
          )
        }
    </div>
  );

  const handleChangeBuildTemple = (value) => {
    if (value === 'Maven') {
      AddTaskFormDataSet.current.set('bzmc', 'Maven构建');
      setSteps([{
        name: 'Maven构建',
        value: 'Maven',
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
      },
      // , {
      //   name: '上传软件包至发布库',
      //   checked: false,
      //   children: (
      //     <React.Fragment>
      //       <TextField style={{ width: 339, marginTop: 27 }} newLine name="yhm" />
      //       <TextField style={{ width: 339, marginTop: 27 }} newLine name="mm" />
      //       <TextField style={{ width: 339, marginTop: 27 }} newLine name="gjblj" />
      //     </React.Fragment>
      //   ),
      // }
      ]);
    } else {
      setSteps([]);
    }
  };

  const getMissionOther = () => {
    if (AddTaskFormDataSet.current.get('rwlx') === 'gj') {
      return [
        <div colSpan={2} className="AddTask_configStep">
          <p>配置步骤</p>
        </div>,
        <Select onChange={handleChangeBuildTemple} name="gjmb">
          <Option value="Maven">Maven模板</Option>
        </Select>,
        <div newLine colSpan={2} style={{ display: 'flex', flexDirection: 'column' }} className="AddTask_stepContent">
          {generateSteps()}
          <TextField
            onChange={(value) => {
              setSteps(steps.map(s => {
                if (s.checked) {
                  s.name = value;
                }
                return s;
              }));
            }}
            style={{ width: 339, marginTop: 30 }}
            newLine
            name="bzmc"
          />
          {
            steps.find(s => s.checked) && steps.find(s => s.checked).children
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
        <Button onClick={() => setTestConnect(!testConnect)} funcType="raised" style={{ width: 76, color: '#3F51B5' }} newLine>测试连接</Button>,
        <div className="addTask_testConnect" newLine>
          测试连接：{handleShowTestConnectResult()}
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
        <Select combo searchable name="cffzlx">
          <Option value="master">master</Option>
          <Option value="feature">feature</Option>
          <Option value="bugfix">bugfix</Option>
          <Option value="hotfix">hotfix</Option>
          <Option value="release">release</Option>
          <Option value="tag">tag</Option>
        </Select>
        {
          getMissionOther()
        }
      </Form>
    </React.Fragment>
  );
});

export default AddTask;
