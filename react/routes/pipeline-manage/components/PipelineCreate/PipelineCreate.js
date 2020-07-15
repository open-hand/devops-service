import { axios } from '@choerodon/boot';
import React, { useEffect, useState, Fragment, useRef } from 'react';
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
        projectId,
      },
    },
    refreshTree,
    dataSource,
    mainStore,
  } = usePipelineCreateStore();

  const [expandIf, setExpandIf] = useState(false);

  useEffect(() => {
    if (dataSource) {
      const { name, appServiceId, image, stageList } = dataSource;
      PipelineCreateFormDataSet.loadData([{
        name,
        appServiceId,
        image,
        selectImage: '1',
      }]);
      editBlockStore.setStepData(stageList, true);
    }
    const init = async () => {
      const res = await createUseStore.axiosGetDefaultImage();
      createUseStore.setDefaultImage(res);
      if (!dataSource) {
        PipelineCreateFormDataSet.current.set('image', res);
      }
    };
    init();
  }, []);

  const handleCreate = async () => {
    const result = await PipelineCreateFormDataSet.validate();
    if (result) {
      const origin = PipelineCreateFormDataSet.toData()[0];
      const data = {
        ...dataSource,
        ...origin,
        image: origin.selectImage === '1' ? origin.image : null,
        devopsCiStageVOS: editBlockStore.getStepData2.filter(s => s.type === 'CI'),
        devopsCdStageVOS: editBlockStore.getStepData2.filter(s => s.type === 'CD'),
      };
      if (data.devopsCiStageVOS.some(s => s.jobList.length === 0)
        || data.devopsCdStageVOS.some(s => s.jobList.length === 0)
      ) {
        message.error(`CI流水线中存在空阶段，无法${modal.props.title.includes('创建') ? '创建' : '保存'}`);
        return false;
      }
      if (dataSource) {
        await axios.put(`/devops/v1/projects/${projectId}/cicd_pipelines/${dataSource.id}`, data);
        editBlockStore.loadData(projectId, dataSource.id);
        refreshTree();
      } else {
        return createUseStore.axiosCreatePipeline(data, id).then((res) => {
          if (res.failed) {
            message.error(res.message);
            return false;
          } else {
            res.id && mainStore.setSelectedMenu({ key: String(res.id) });
            refreshTree();
            return true;
          }
        });
      }
    } else {
      return false;
    }
  };

  const handelCancel = () => {
    refreshTree();
  };

  modal.handleOk(handleCreate);

  modal.handleCancel(handelCancel);

  // const handleChangeImage = (data) => {
  //   if (data === '0') {
  //     PipelineCreateFormDataSet.current.set('image', createUseStore.getDefaultImage);
  //   } else {
  //     PipelineCreateFormDataSet.current.set('image', '');
  //   }
  // };

  const handleChangeSelectImage = (data) => {
    if (data === createUseStore.getDefaultImage) {
      PipelineCreateFormDataSet.current.set('selectImage', '0');
    } else {
      PipelineCreateFormDataSet.current.set('selectImage', '1');
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

  const handleClickMore = async (e) => {
    e.stopPropagation();
    const pageSize = PipelineCreateFormDataSet.current.get('pageSize') + 20;
    const result = await axios.post(`/devops/v1/projects/${projectId}/app_service/page_app_services_without_ci?page=0&size=${pageSize}`);
    if (result.length % 20 === 0) {
      result.push({
        appServiceId: 'more',
        appServiceName: '加载更多',
      });
    }
    PipelineCreateFormDataSet.current.set('pageSize', pageSize);
    PipelineCreateFormDataSet.getField('appServiceId').props.lookup = result;
  };

  const renderer = ({ text }) => (text === '加载更多' ? (
    <a onClick={handleClickMore}>{text}</a>
  ) : text);

  const optionRenderer = ({ text }) => renderer({ text });

  function getAppServiceCode() {
    const appServiceData = PipelineCreateFormDataSet.getField('appServiceId').getLookupData(PipelineCreateFormDataSet.current.get('appServiceId'));
    return appServiceData?.appServiceCode || '';
  }

  return (
    <div>
      <Form columns={3} dataSet={PipelineCreateFormDataSet}>
        <TextField
          name="name"
          disabled={dataSource}
        />
        {/* 应用服务只能选择目前没有关联流水线的应用服务 */}
        <Select
          disabled={dataSource}
          name="appServiceId"
          searchable
          searchMatcher="appServiceName"
          showHelp="tooltip"
          optionRenderer={optionRenderer}
          renderer={renderer}
          help="此处仅能看到您有开发权限的启用状态的应用服务，并要求该应用服务必须有master分支，且尚未有关联的CI流水线"
        />
        <TextField style={{ display: 'none' }} />
        <div className="advanced_text" style={{ cursor: 'pointer' }} onClick={() => setExpandIf(!expandIf)}>
          高级设置<Icon style={{ fontSize: 18 }} type={expandIf ? 'expand_less' : 'expand_more'} />
        </div>
        {
          expandIf ? (
            <Select
              // disabled={
              //   !!(PipelineCreateFormDataSet.current && PipelineCreateFormDataSet.current.get('selectImage') === '0')
              // }
              combo
              newLine
              colSpan={2}
              name="image"
              onChange={handleChangeSelectImage}
              showHelp="tooltip"
              help="CI流程Runner镜像是该条流水线中所有CI任务默认的执行环境。您可直接使用此处给出的默认Runner镜像，或是输入自定义的CI流程Runner镜像"
            >
              <Option value={createUseStore.getDefaultImage}>{createUseStore.getDefaultImage}</Option>
            </Select>
          ) : ''
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
        appServiceId={PipelineCreateFormDataSet.current.get('appServiceId')}
        appServiceCode={getAppServiceCode() || editBlockStore.getMainData?.appServiceCode}
      />
      <p className="pipeline_createInfo"><Icon style={{ color: 'red', verticalAlign: 'text-bottom' }} type="error" />此页面定义了CI阶段或其中的任务后，GitLab仓库中的.gitlab-ci.yml文件也会同步修改。</p>
    </div>
  );
});

export default PipelineCreate;
