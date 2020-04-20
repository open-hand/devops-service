import React, { useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Select, TextField, Modal, SelectBox, Button, Password } from 'choerodon-ui/pro';
import { Icon, Spin } from 'choerodon-ui';
import YamlEditor from '../../../../../../components/yamlEditor';
import emptyImg from '../../../../../../components/empty-page/image/owner.png';
import DependRepo from './DependRepo';
import { useAddTaskStore } from './stores';

import './index.less';

const { Option } = Select;

const obj = {
  Maven: 'Maven构建',
  npm: 'Npm构建',
  upload: '上传软件包至存储库',
  docker: 'Docker构建',
  chart: 'Chart构建',
};

const AddTask = observer(() => {
  const {
    AddTaskFormDataSet,
    AddTaskStepFormDataSet,
    DependRepoDataSet,
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
  const [ConnectLoading, setConnectLoading] = useState(false);
  const [customYaml, setCustomYaml] = useState(useStore.getYaml.custom);

  useEffect(() => {
    if (steps.length > 0) {
      if (steps.find(s => s.checked).repo) {
        const item = [...(steps.find(s => s.checked).repo.publicRepo || []), ...(steps.find(s => s.checked).repo.privateRepo || [])];
        AddTaskFormDataSet.current.set('private', item.map(i => String(i.privateIf)));
      }
      AddTaskFormDataSet.getField('uploadFilePattern').set('required', steps.some(s => s.type === 'upload'));
      AddTaskFormDataSet.getField('dockerContextDir').set('required', steps.some(s => s.type === 'docker'));
      AddTaskFormDataSet.getField('dockerFilePath').set('required', steps.some(s => s.type === 'docker'));

      // else {
      //   AddTaskFormDataSet.current.set('private', '');
      // }
    }
  }, [steps.find(s => s.checked)]);

  useEffect(() => {
    if (jobDetail) {
      if (jobDetail.type !== 'custom') {
        const { config, authType, username, token, password, sonarUrl } = JSON.parse(jobDetail.metadata.replace(/'/g, '"'));
        let uploadFilePattern;
        let dockerContextDir;
        let dockerFilePath;
        let uploadArtifactFileName;
        let dockerArtifactFileName;
        config.forEach((c) => {
          if (c.type === 'upload') {
            uploadFilePattern = c.uploadFilePattern;
            uploadArtifactFileName = c.artifactFileName;
          } else if (c.type === 'docker') {
            dockerContextDir = c.dockerContextDir;
            dockerFilePath = c.dockerFilePath;
            dockerArtifactFileName = c.artifactFileName;
          }
        });
        const newSteps = config || [];
        const data = {
          ...jobDetail,
          uploadFilePattern,
          dockerContextDir,
          dockerFilePath,
          uploadArtifactFileName,
          dockerArtifactFileName,
          triggerRefs: jobDetail.triggerRefs.split(','),
          glyyfw: appServiceId || PipelineCreateFormDataSet.getField('appServiceId').getText(PipelineCreateFormDataSet.current.get('appServiceId')),
          bzmc: newSteps.find(s => s.checked) ? newSteps.find(s => s.checked).name : '',
          authType,
          username,
          token,
          password,
          sonarUrl,
          private: newSteps.find(s => s.checked).repos ? newSteps.find(s => s.checked).repos.map(r => String(r.privateIf)) : '',
        };
        AddTaskFormDataSet.loadData([data]);
        setSteps(newSteps);
      } else {
        AddTaskFormDataSet.loadData([jobDetail]);
        setCustomYaml(jobDetail.metadata);
      }
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
        metadata: (function () {
          if (data.type === 'build') {
            return JSON.stringify({
              config: steps.map((s, sIndex) => {
                s.sequence = sIndex;
                s.script = s.yaml;
                if (s.repo) {
                  s.repos = [...(s.repo.publicRepo || []).map(p => {
                    p.private = p.privateIf;
                    return p;
                  }), ...(s.repo.privateRepo || []).map(p => {
                    p.private = p.privateIf;
                    return p;
                  })];
                }
                if (s.type === 'upload') {
                  s.uploadFilePattern = data.uploadFilePattern;
                  if (data.uploadArtifactFileName) {
                    s.artifactFileName = data.uploadArtifactFileName;
                  }
                }
                if (s.type === 'docker') {
                  s.dockerContextDir = data.dockerContextDir;
                  s.dockerFilePath = data.dockerFilePath;
                  if (data.dockerArtifactFileName) {
                    s.artifactFileName = data.dockerArtifactFileName;
                  }
                }
                return s;
              }),
            }).replace(/"/g, "'");
          } else if (data.type === 'sonar') {
            return JSON.stringify({
              ...data,
              triggerRefs: data.triggerRefs.join(','),
              metadata: '',
            }).replace(/"/g, "'");
          } else if (data.type === 'custom') {
            return customYaml;
          }
        }()),
      };
      handleOk(data);
      return true;
    } else {
      return false;
    }
  };

  useEffect(() => {
    if (AddTaskFormDataSet.current.get('type') === 'sonar') {
      if (AddTaskFormDataSet.current.get('authType') === 'username') {
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
    } else {
      modal.update({
        okProps: {
          disabled: false,
        },
      });
    }
    if (AddTaskFormDataSet.current.get('type') !== 'build') {
      AddTaskFormDataSet.getField('uploadFilePattern').set('required', false);
      AddTaskFormDataSet.getField('dockerContextDir').set('required', false);
      AddTaskFormDataSet.getField('dockerFilePath').set('required', false);
    }
  }, [testConnect, AddTaskFormDataSet.current.get('type'), AddTaskFormDataSet.current.get('authType')]);

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

  const handleAddStepItem = (index) => {
    Modal.open({
      key: Modal.key(),
      title: '添加步骤',
      style: {
        width: 380,
      },
      children: (
        <Form dataSet={AddTaskStepFormDataSet}>
          <Select
            onOption={({ record }) => ({
              disabled: steps.map(s => s.type).includes(record.get('value')),
            })}
            name="kybz"
          >
            <Option value="Maven">Maven构建</Option>
            <Option value="npm">Npm构建</Option>
            <Option value="upload">上传软件包至存储库</Option>
            <Option value="docker">Docker构建</Option>
            <Option value="chart">Chart构建</Option>
          </Select>
        </Form>
      ),
      drawer: true,
      okText: '添加',
      onOk: async () => {
        const result = await AddTaskStepFormDataSet.validate();
        if (!result) {
          return false;
        }
        if (AddTaskStepFormDataSet.current && AddTaskStepFormDataSet.current.get('kybz')) {
          const value = AddTaskStepFormDataSet.current.get('kybz');
          if (value) {
            const newSteps = steps;
            newSteps.splice(index, 0, {
              name: obj[value],
              type: value,
              checked: true,
              yaml: useStore.getYaml[value] || '',
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
    setConnectLoading(true);
    const data = AddTaskFormDataSet.current.toData();
    useStore.axiosConnectTest(data, id).then((res) => {
      setTestConnect(res);
      setConnectLoading(false);
      resolve(res);
    });
  });

  const handleChangeBuildTemple = (value) => {
    if (value) {
      AddTaskFormDataSet.current.set('bzmc', obj[value]);
      const origin = [{
        name: obj[value],
        type: value,
        checked: true,
        yaml: useStore.getYaml[value],
      }];
      let extra = [];
      if (value === 'Maven') {
        extra = [{
          name: '上传软件包至存储库',
          type: 'upload',
          checked: false,
        }, {
          name: 'Docker构建',
          type: 'docker',
          checked: false,
        }, {
          name: 'Chart构建',
          type: 'chart',
          checked: false,
        }];
      } else if (value === 'npm') {
        extra = [{
          name: '上传软件包至存储库',
          type: 'upload',
          checked: false,
        }];
      }
      setSteps([...origin, ...extra]);
    } else {
      setSteps([]);
    }
  };

  const renderTestConnect = () => {
    function renderDom() {
      if (String(testConnect)) {
        if (testConnect) {
          return (
            <React.Fragment>
              <div className="addTask_testConnect_success"><i className="success" /></div>
              <p className="addTask_testConnect_havnot">测试连接: <span>成功</span></p>
              <p style={{ color: 'rgba(58,52,95,0.65)' }}>(重新进行连接测试: <Button onClick={handleTestConnect} funcType="flat" style={{ width: 'auto', color: '#3F51B5' }}>测试连接</Button>)</p>
            </React.Fragment>
          );
        } else {
          return (
            <React.Fragment>
              <div style={{ borderColor: 'rgb(247, 122, 112)' }} className="addTask_testConnect_success"><i className="failure">X</i></div>
              <p className="addTask_testConnect_havnot">测试连接: <span style={{ color: 'rgb(247, 122, 112)' }}>失败</span></p>
              <p style={{ color: 'rgba(58,52,95,0.65)' }}>(重新进行连接测试: <Button onClick={handleTestConnect} funcType="flat" style={{ width: 'auto', color: '#3F51B5' }}>测试连接</Button>)</p>
            </React.Fragment>
          );
        }
      } else {
        return (
          <React.Fragment>
            <img style={{ width: 121, marginRight: 39 }} src={emptyImg} alt="none" />
            <div>
              <p className="addTask_testConnect_havnot">未进行过连接</p>
              <Button onClick={handleTestConnect} funcType="raised" style={{ width: 'auto', color: '#3F51B5' }} newLine>测试连接</Button>
            </div>
          </React.Fragment>
        );
      }
    }
    if ((AddTaskFormDataSet.current.get('authType') === 'username')) {
      return (
        <div
          colSpan={2}
          newLine
          className="addTask_testConnect_container"
          style={{
            background: ConnectLoading ? 'rgba(117,137,242,0.06)' : (function () {
              if (String(testConnect)) {
                return 'rgba(0,191,165,0.06)';
              } else {
                return 'white';
              }
            }()),
            flexDirection: !String(testConnect) ? 'row' : 'column',
            textAlign: 'center',
          }}
        >
          {
            ConnectLoading ? (
              <React.Fragment>
                <Spin size="large" />
                <p className="addTask_testConnect_havnot">正在进行连接测试</p>
              </React.Fragment>
            ) : renderDom()
          }
        </div>
      );
    }
  };

  const handleAddRepo = (data, privateIf) => {
    if (data.length === 0) {
      const old = AddTaskFormDataSet.current.get('private');
      const newData = old.filter(o => o !== (privateIf ? 'true' : 'false'));
      AddTaskFormDataSet.current.set('private', newData);
    }
    setSteps(steps.map(s => {
      if (s.checked) {
        let newRepo = {};
        if (privateIf) {
          newRepo = {
            privateRepo: data,
          };
        } else {
          newRepo = {
            publicRepo: data,
          };
        }
        s.repo = {
          ...s.repo,
          ...newRepo,
        };
      }
      return s;
    }));
  };

  const handleOpenRepo = (privateIf) => {
    Modal.open({
      key: Modal.key(),
      title: `修改${privateIf === 'privateRepo' ? '私有' : '公有'}依赖仓库`,
      style: {
        width: 380,
      },
      children: <DependRepo handleParentCancel={handleCancel} dsData={steps.find(s => s.checked).repo[privateIf]} handleAdd={handleAddRepo} ds={DependRepoDataSet} privateIf={privateIf === 'privateRepo'} />,
      drawer: true,
      okText: '添加',
    });
  };

  const handleCancel = (privateIf) => {
    const old = AddTaskFormDataSet.current.get('private');
    const newData = old.filter(o => o !== (privateIf ? 'true' : 'false'));
    AddTaskFormDataSet.current.set('private', newData);
  };

  const handleChangePrivate = (newV, oldV) => {
    newV = newV || [];
    oldV = oldV || [];
    function minus(a, b) {
      return [...a.filter(item => !b.includes(item)), ...b.filter(item => !a.includes(item))];
    }
    const extra = minus(newV, oldV)[0];
    if (newV.length > oldV.length) {
      //  打钩
      Modal.open({
        key: Modal.key(),
        title: `修改${extra === 'true' ? '私有' : '公有'}依赖仓库`,
        style: {
          width: 380,
        },
        children: <DependRepo handleParentCancel={handleCancel} handleAdd={handleAddRepo} ds={DependRepoDataSet} privateIf={extra === 'true'} />,
        drawer: true,
        okText: '添加',
      });
    } else {
      Modal.confirm({
        title: `禁用${extra === 'true' ? '私有' : '公有'}依赖库`,
        children: `确定禁用并清空已有的${extra === 'true' ? '私有' : '公有'}依赖库吗？`,
      }).then(button => {
        if (button === 'ok') {
          setSteps(steps.map(s => {
            if (s.checked) {
              s.repo[`${extra === 'true' ? 'privateRepo' : 'publicRepo'}`] = [];
            }
            return s;
          }));
        } else {
          AddTaskFormDataSet.current.set('private', extra === 'true' ? 'true' : 'false');
        }
      });
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
          <Option value="npm">Npm模板</Option>
        </Select>,
        <div newLine colSpan={2} style={{ display: 'flex', flexDirection: 'column' }} className="AddTask_stepContent">
          {generateSteps()}
          <div
            className="stepformContent"
          >
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
              // newLine
              name="bzmc"
            />
            {
              steps.find(s => s.checked) && steps.find(s => s.checked).type === 'Maven' ? (
                <SelectBox style={{ marginTop: 30 }} onChange={handleChangePrivate} name="private">
                  <Option value="false">
                    <span style={{ display: 'inline-flex', alignItems: 'center' }}>
                      公有依赖库
                      <Button onClick={() => handleOpenRepo('publicRepo')} style={{ marginLeft: 8 }}>
                        <Icon
                          style={{
                            display: steps.find(s => s.checked).repo && steps.find(s => s.checked).repo.publicRepo && steps.find(s => s.checked).repo.publicRepo.length > 0 ? 'inline-block' : 'none',
                            color: '#3F51B5',
                          }}
                          type="mode_edit"
                        />
                      </Button>
                    </span>
                  </Option>
                  <Option value="true">
                    <span style={{ display: 'inline-flex', alignItems: 'center' }}>
                      私有依赖库
                      <Button onClick={() => handleOpenRepo('privateRepo')} style={{ marginLeft: 8 }}>
                        <Icon
                          style={{
                            display: steps.find(s => s.checked).repo && steps.find(s => s.checked).repo.privateRepo && steps.find(s => s.checked).repo.privateRepo.length > 0 ? 'inline-block' : 'none',
                            color: '#3F51B5',
                          }}
                          type="mode_edit"
                        />
                      </Button>
                    </span>
                  </Option>
                </SelectBox>
              ) : ''
            }
          </div>
          {
            (function () {
              if (steps.length > 0) {
                const type = steps.find(s => s.checked).type;
                if (type && ['Maven', 'npm'].includes(type)) {
                  return (
                    <div>
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
                  );
                } else if (type === 'upload') {
                  return [
                    <TextField style={{ width: 339, marginBottom: 20 }} name="uploadFilePattern" />,
                    <TextField style={{ width: 339, marginBottom: 20 }} name="uploadArtifactFileName" />,
                  ];
                } else if (type === 'docker') {
                  return [
                    <TextField style={{ width: 339, marginBottom: 20 }} name="dockerContextDir" />,
                    <TextField style={{ width: 339, marginBottom: 20 }} name="dockerFilePath" />,
                    <TextField style={{ width: 339, marginBottom: 20 }} name="dockerArtifactFileName" />,
                  ];
                }
              }
              return '';
            }())
          }
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
        renderTestConnect(),
      ];
    }
  };

  return (
    <React.Fragment>
      <Form dataSet={AddTaskFormDataSet} columns={2}>
        <Select name="type">
          <Option value="build">构建</Option>
          <Option value="sonar">代码检查</Option>
          <Option value="custom">自定义</Option>
        </Select>
        <TextField name="name" />
        {
          AddTaskFormDataSet.current.get('type') !== 'custom' ? [
            <TextField name="glyyfw" />,
            <Select combo searchable name="triggerRefs">
              <Option value="master">master</Option>
              <Option value="feature">feature</Option>
              <Option value="bugfix">bugfix</Option>
              <Option value="hotfix">hotfix</Option>
              <Option value="release">release</Option>
              <Option value="tag">tag</Option>
            </Select>,
            getMissionOther(),
          ] : (
            <YamlEditor
              readOnly={false}
              colSpan={2}
              newLine
              value={customYaml}
              onValueChange={(valueYaml) => setCustomYaml(valueYaml)}
              modeChange={false}
              showError={false}
            />
          )
        }
      </Form>
    </React.Fragment>
  );
});

export default AddTask;
