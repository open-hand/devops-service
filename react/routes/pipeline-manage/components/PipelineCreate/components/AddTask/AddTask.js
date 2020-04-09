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
      AddTaskFormDataSet.loadData([{
        ...jobDetail,
        triggerRefs: jobDetail.triggerRefs.split(','),
        glyyfw: appServiceId || PipelineCreateFormDataSet.current.get('appServiceId'),
      }]);
      setSteps(JSON.parse(jobDetail.metadata.replace(/'/g, '"')).mavenbuildTemplateVOList);
    }
  }, []);

  const handleAdd = async () => {
    const result = await AddTaskFormDataSet.validate();
    if (result) {
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
              yaml: '',
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

  const handleTestConnect = () => {
    const data = AddTaskFormDataSet.current.toData();
    useStore.axiosConnectTest(data, id).then((res) => {
      setTestConnect(res);
    });
  };

  const handleChangeBuildTemple = (value) => {
    if (value === 'Maven') {
      AddTaskFormDataSet.current.set('bzmc', 'Maven构建');
      setSteps([{
        name: 'Maven构建',
        value: 'Maven',
        checked: true,
        yaml: `
        # 功能： 更新pom文件中指定的项目的版本号
# 说明： 此函数是猪齿鱼内置的shell函数，用于更新pom文件的版本号为对应commit的版本号,
#        (这个值在猪齿鱼内置变量 CI_COMMIT_TAG 中)
# update_pom_version


# 功能： 以jacoco为代理进行单元测试，可以分析单元测试覆盖率
# 参数说明：
#  -Dmaven.test.skip=false：不跳过单元测试
#  -U：每次构建检查依赖更新，可避免缓存中快照版本依赖不更新问题，但会牺牲部分性能
#  -e -X ：打印调试信息，定位疑难构建问题时建议使用此参数构建
#  -B：以batch模式运行，可避免日志打印时出现ArrayIndexOutOfBoundsException异常
# 更多帮助信息请执行此命令进行查看：mvn org.jacoco:jacoco-maven-plugin:help
# mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent test -Dmaven.test.failure.ignore=true -DskipTests=false -U -e -X -B


# 功能： springboot项目打包
# repackage可以将已经存在的jar和war格式的文件重新打包
# 打出的jar包将可以在命令行使用java -jar命令执行。
# 更多帮助信息请执行此命令进行查看：mvn spring-boot:help
# mvn package spring-boot:repackage


# 功能：  打包
# 参数说明：
#  -Dmaven.test.skip=true：跳过单元测试，不建议
#  -U：每次构建检查依赖更新，可避免缓存中快照版本依赖不更新问题，但会牺牲部分性能
#  -e -X ：打印调试信息，定位疑难构建问题时建议使用此参数构建
#  -B：以batch模式运行，可避免日志打印时出现ArrayIndexOutOfBoundsException异常
# 使用场景： 打包项目且不需要执行单元测试时使用
# 更多帮助信息请执行此命令进行查看：mvn help:describe -Dcmd=package
mvn package -Dmaven.test.skip=true -U -e -X -B`,
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
            />
          </div>
        </div>,
      ];
    } else {
      let extra;
      if (AddTaskFormDataSet.current.get('authType') === 'username') {
        extra = [
          <TextField newLine name="username" />,
          <TextField name="password" />,
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
        <Button onClick={handleTestConnect} funcType="raised" style={{ width: 76, color: '#3F51B5' }} newLine>测试连接</Button>,
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
        <Select name="glyyfw" />
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
