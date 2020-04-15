import React, { useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Select, TextField, Modal, SelectBox, Button, Password } from 'choerodon-ui/pro';
import { Icon } from 'choerodon-ui';
import YamlEditor from '../../../../../../components/yamlEditor';
import { useAddTaskStore } from './stores';

import './index.less';

const { Option } = Select;

const obj = {
  Maven: 'Maven构建',
  npm: 'Npm构建',
};

const AddTask = observer(() => {
  const {
    AddTaskFormDataSet,
    AddTaskStepFormDataSet,
    modal,
    handleOk,
    useStore,
    AppState: {
      currentMenuType: {
        id,
      },
    },
    jobDetail,
    appServiceId,
    PipelineCreateFormDataSet,
  } = useAddTaskStore();

  const [steps, setSteps] = useState([]);
  const [testConnect, setTestConnect] = useState('');

  useEffect(() => {
    if (jobDetail) {
      const { mavenbuildTemplateVOList, authType, username, token, password, sonarUrl } = JSON.parse(jobDetail.metadata.replace(/'/g, '"'));
      const newSteps = mavenbuildTemplateVOList || [];
      const data = {
        ...jobDetail,
        triggerRefs: jobDetail.triggerRefs.split(','),
        glyyfw: appServiceId || PipelineCreateFormDataSet.getField('appServiceId').getText(PipelineCreateFormDataSet.current.get('appServiceId')),
        bzmc: newSteps.find(s => s.checked) ? newSteps.find(s => s.checked).name : '',
        authType,
        username,
        token,
        password,
        sonarUrl,
      };
      AddTaskFormDataSet.loadData([data]);
      setSteps(newSteps);
    } else {
      AddTaskFormDataSet.current.set('glyyfw', appServiceId || PipelineCreateFormDataSet.getField('appServiceId').getText(PipelineCreateFormDataSet.current.get('appServiceId')));
    }
  }, []);

  const handleAdd = async () => {
    const result = await AddTaskFormDataSet.validate();
    if (result) {
      if (AddTaskFormDataSet.current.get('type') === 'sonar') {
        const connet = await handleTestConnect();
        if (!connet) {
          return false;
        }
      }
      let data = AddTaskFormDataSet.toData()[0];
      data = {
        ...data,
        triggerRefs: data.triggerRefs.join(','),
        metadata: data.type === 'build' ? JSON.stringify({
          mavenbuildTemplateVOList: steps.map((s, sIndex) => {
            s.sequence = sIndex;
            s.script = s.yaml;
            return s;
          }),
        }).replace(/"/g, "'") : JSON.stringify({
          ...data,
          triggerRefs: data.triggerRefs.join(','),
          metadata: '',
        }).replace(/"/g, "'"),
      };
      handleOk(data);
      return true;
    } else {
      return false;
    }
  };

  useEffect(() => {
    if (AddTaskFormDataSet.current.get('type') === 'sonar') {
      modal.update({
        okProps: {
          disabled: !testConnect,
        },
      });
    } else {
      modal.update({
        okProps: {
          disabled: false,
        },
      });
    }
  }, [testConnect, AddTaskFormDataSet.current.get('type')]);

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
            <Option value="npm">Npm构建</Option>
          </Select>
        </Form>
      ),
      drawer: true,
      okText: '添加',
      onOk: () => {
        if (AddTaskStepFormDataSet.current && AddTaskStepFormDataSet.current.get('kybz')) {
          const value = AddTaskStepFormDataSet.current.get('kybz');
          if (value) {
            const newSteps = steps;
            newSteps.splice(index, 0, {
              name: obj[value],
              value,
              checked: true,
              yaml: useStore.getYaml[value],
              // children: (
              //   <div
              //     style={{
              //       marginTop: 20,
              //     }}
              //   >
              //     <YamlEditor
              //       readOnly={false}
              //       colSpan={2}
              //       newLine
              //       value={steps[0].yaml}
              //       modeChange={false}
              //       onValueChange={(valueYaml) => handleChangeValue(valueYaml, index)}
              //     />
              //   </div>
              // ),
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

  const handleChangeValue = (value, index) => {
    setSteps(steps.map((s, sIndex) => {
      if (s.checked) {
        s.yaml = value;
      }
      return s;
    }));
  };

  const handleTestConnect = () => new Promise((resolve) => {
    const data = AddTaskFormDataSet.current.toData();
    useStore.axiosConnectTest(data, id).then((res) => {
      setTestConnect(res);
      resolve(res);
    });
  });

  const handleChangeBuildTemple = (value) => {
    if (value) {
      AddTaskFormDataSet.current.set('bzmc', obj[value]);
      setSteps([{
        name: obj[value],
        value,
        checked: true,
        yaml: useStore.getYaml[value],
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
    if (AddTaskFormDataSet.current.get('type') === 'build') {
      return [
        <div colSpan={2} className="AddTask_configStep">
          <p>配置步骤</p>
        </div>,
        <Select onChange={handleChangeBuildTemple} name="gjmb">
          <Option value="Maven">Maven模板</Option>
          <Option value="npm">Npm构建</Option>
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
            style={{
              width: 339,
              marginTop: 30,
              marginBottom: 20,
              display: steps.length === 0 ? 'none' : 'block',
            }}
            newLine
            name="bzmc"
          />
          <div
            style={{
              visibility: steps.length === 0 ? 'hidden' : 'visible',
            }}
          >
            <YamlEditor
              readOnly={false}
              colSpan={2}
              newLine
              value={steps.length > 0 ? steps.find(s => s.checked).yaml : ''}
              onValueChange={(valueYaml) => handleChangeValue(valueYaml)}
              modeChange={false}
              showError={false}
            />
          </div>
        </div>,
      ];
    } else {
      let extra;
      if (AddTaskFormDataSet.current.get('authType') === 'username') {
        extra = [
          <TextField newLine name="username" />,
          <Password name="password" />,
          <TextField name="sonarUrl" />,
        ];
      } else {
        extra = [
          <TextField newLine name="token" />,
          <TextField name="sonarUrl" />,
        ];
      }
      return [
        <SelectBox name="authType">
          <Option value="username">用户名与密码</Option>
          <Option value="token">Token</Option>
        </SelectBox>,
        ...extra,
        <Button onClick={handleTestConnect} funcType="raised" style={{ width: 'auto', color: '#3F51B5' }} newLine>测试连接</Button>,
        <div className="addTask_testConnect" newLine>
          测试连接：{handleShowTestConnectResult()}
        </div>,
      ];
    }
  };

  return (
    <React.Fragment>
      <Form dataSet={AddTaskFormDataSet} columns={2}>
        <Select name="type">
          <Option value="build">构建</Option>
          <Option value="sonar">代码检查</Option>
        </Select>
        <TextField name="name" />
        <TextField name="glyyfw" />
        <Select combo searchable name="triggerRefs">
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
