import React, { useEffect, useState } from 'react';
import { Form, TextField, Select, SelectBox, Modal, Button, DataSet } from 'choerodon-ui/pro';
import { message, Icon } from 'choerodon-ui';
import { observer } from 'mobx-react-lite';
import { usePipelineCreateStore } from './stores';
import AddTask from './components/AddTask';
import { usePipelineManageStore } from '../../stores';
import StageEditBlock from '../PipelineFlow/components/stageEditBlock';

import './pipelineCreate.less';

const { Option } = Select;

const PipelineCreate = observer(() => {
  const {
    PipelineCreateFormDataSet,
    modal,
    editBlockStore,
    createUseStore,
    AppState: {
      currentMenuType: {
        id,
      },
    },
    refreshTree,
  } = usePipelineCreateStore();

  const [expandIf, setExpandIf] = useState(false);

  useEffect(() => {
    const init = async () => {
      const res = await createUseStore.axiosGetDefaultImage();
      createUseStore.setDefaultImage(res);
      PipelineCreateFormDataSet.current.set('image', res);
    };
    init();
  }, []);

  const handleCreate = async () => {
    const result = await PipelineCreateFormDataSet.validate();
    if (result) {
      const origin = PipelineCreateFormDataSet.toData()[0];
      const data = {
        ...origin,
        image: origin.selectImage === '1' ? origin.image : null,
        stageList: editBlockStore.getStepData2,
      };
      return createUseStore.axiosCreatePipeline(data, id).then((res) => {
        if (res.failed) {
          message.error(res.message);
          return false;
        } else {
          refreshTree();
          return true;
        }
      });
    } else {
      return false;
    }
  };

  modal.handleOk(handleCreate);

  const handleChangeImage = (data) => {
    if (data === '0') {
      PipelineCreateFormDataSet.current.set('image', createUseStore.getDefaultImage);
    } else {
      PipelineCreateFormDataSet.current.set('image', '');
    }
  };

  // const handleAddMission = () => {
  //   Modal.open({
  //     key: Modal.key(),
  //     title: '添加任务',
  //     style: {
  //       width: '740px',
  //     },
  //     children: <AddTask />,
  //     drawer: true,
  //     okText: '添加',
  //   });
  // };

  return (
    <div>
      <Form columns={3} dataSet={PipelineCreateFormDataSet}>
        <TextField name="name" />
        {/* 应用服务只能选择目前没有关联流水线的应用服务 */}
        <Select
          name="appServiceId"
          searchable
          searchMatcher="appServiceName"
        />
        <TextField style={{ display: 'none' }} />
        <div style={{ cursor: 'pointer' }} onClick={() => setExpandIf(!expandIf)}>
          <Icon type={expandIf ? 'expand_less' : 'expand_more'} />高级设置
        </div>
        {
          expandIf ? [
            <SelectBox onChange={handleChangeImage} colSpan={2} newLine name="selectImage">
              <Option value="0">默认Runner镜像</Option>
              <Option value="1">自定义Runner镜像</Option>
            </SelectBox>,
            <TextField
              disabled={
                !!(PipelineCreateFormDataSet.current && PipelineCreateFormDataSet.current.get('selectImage') === '0')
              }
              newLine
              colSpan={2}
              name="image"
            />,
          ] : ''
        }
        {/* <SelectBox name="triggerType"> */}
        {/*  <Option value="auto">自动触发</Option> */}
        {/*  <Option disabled value="F">手动触发</Option> */}
        {/* </SelectBox> */}
      </Form>
      <StageEditBlock
        editBlockStore={editBlockStore}
        edit
        image={PipelineCreateFormDataSet.current.get('image')}
      />
      <p className="pipeline_createInfo"><Icon style={{ color: 'red', verticalAlign: 'text-bottom' }} type="error" />此页面定义了阶段与任务后，GitLab仓库中的.gitlab-ci.yml文件也会同步修改。</p>
    </div>
  );
});

export default PipelineCreate;
