import React, { useState, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { Spin } from 'choerodon-ui';
import YamlEditor from '../../../../../../../components/yamlEditor';
import InterceptMask from '../../../../../../../components/interceptMask/InterceptMask';
import { handlePromptError } from '../../../../../../../utils';

import './index.less';

const ValueModalContent = observer((
  {
    modal,
    store,
    formatMessage,
    intlPrefix,
    prefixCls,
    vo,
  },
) => {
  const [value, setValue] = useState('');
  const [isDisabled, setIsDisabled] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const { yaml, name } = store.getUpgradeValue;

  modal.handleOk(handleOk);

  async function handleOk() {
    if (isDisabled) return false;
    setIsLoading(true);
    const { id, parentId, projectId, appServiceVersionId } = vo;
    const [envId, appId] = parentId.split('-');

    const data = {
      values: value || yaml || '',
      instanceId: id,
      type: 'update',
      environmentId: Number(envId),
      appServiceId: Number(appId),
      appServiceVersionId,
    };

    try {
      const result = await store.upgrade(projectId, data);
      if (handlePromptError(result)) {
        Choerodon.prompt('修改成功.');
      } else {
        Choerodon.prompt('修改失败.');
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
    }
  }

  function toggleOkDisabled(flag) {
    modal.update({ okProps: { disabled: flag } });
  }

  function handleChange(nextValue) {
    setValue(nextValue);
  }

  function handleEnableNext(flag) {
    setIsDisabled(flag);
    toggleOkDisabled(flag);
  }

  return (<Fragment>
    <div className={`${prefixCls}-configValue-text`}>
      <span>{formatMessage({ id: `${intlPrefix}.modal.config` })}：</span>
      <span className={`${prefixCls}-configValue-name`}>
        {name || formatMessage({ id: `${intlPrefix}.modal.config.empty` })}
      </span>
    </div>
    <Spin spinning={store.getValueLoading}>
      <YamlEditor
        readOnly={false}
        value={value || yaml || ''}
        originValue={yaml}
        onValueChange={handleChange}
        handleEnableNext={handleEnableNext}
      />
    </Spin>
    <InterceptMask visible={isLoading} />
  </Fragment>);
});

export default ValueModalContent;
