import React, { useState, useMemo, useCallback } from 'react';
import { Form, Progress, Select, Icon, TextField } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import { map, some, debounce } from 'lodash';
import { axios, Choerodon } from '@choerodon/boot';
import { useExecuteContentStore } from './stores';

import './index.less';

const { OptGroup, Option } = Select;

export default observer(() => {
  const {
    AppState: { currentMenuType: { projectId } },
    intl: { formatMessage },
    appServiceId,
    selectDs,
    store,
    modal,
    refresh,
    prefixCls,
  } = useExecuteContentStore();

  const [branchPage, setBranchPage] = useState(1);
  const [tagPage, setTagPage] = useState(1);
  const [moreTagLoading, setMoreTagLoading] = useState(false);
  const [moreBranchLoading, setMoreBranchLoading] = useState(false);
  const [selectCom, setSelectCom] = useState(null);
  const [searchValue, setSearchValue] = useState('');

  const {
    getBranchData,
    getTagData,
    getHasMoreBranch,
    getHasMoreTag,
  } = store;

  modal.handleOk(async () => {
    try {
      if (await selectDs.submit() !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  });

  async function loadMore(type, e) {
    e.stopPropagation();

    if (type === 'branch') {
      setMoreBranchLoading(true);
      await store.loadBranchData({ projectId, appServiceId, page: branchPage + 1, searchValue });
      setMoreBranchLoading(false);
      setBranchPage(pre => pre + 1);
    } else {
      setMoreTagLoading(true);
      await store.loadTagData({ projectId, appServiceId, page: tagPage + 1, searchValue });
      setMoreTagLoading(false);
      setTagPage(pre => pre + 1);
    }
  }

  const loadData = useMemo(() => debounce(async (text) => {
    setBranchPage(1);
    setTagPage(1);
    if (selectCom && selectCom.options) {
      selectCom.options.changeStatus('loading');
    }
    try {
      const [branchData, tagData] = await axios.all([store.loadBranchData({ projectId, appServiceId, page: 1, searchValue: text }), store.loadTagData({ projectId, appServiceId, page: 1, searchValue: text })]);
      const value = selectDs.current.get('branch');
      if (selectCom && selectCom.options) {
        selectCom.options.changeStatus('ready');
      }
      if (!branchData && !tagData) {
        return;
      }
      if (!searchValue
        && value
        && !(some(branchData.list, (item) => item.branchName === value.slice(0, -7))
          || some(tagData.list, (item) => item.release.tagName === value.slice(0, -7)))) {
        if (value.slice(-7) === '_type_b') {
          store.setBranchData([
            ...store.getBranchData,
            { branchName: value.slice(0, -7) },
          ]);
        } else {
          store.setTagData([
            ...store.getTagData,
            {
              release: {
                tagName: value.slice(0, -7),
              },
            },
          ]);
        }
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
    }
  }, 700), [projectId, appServiceId, selectCom]);

  function renderBranchOptionOrigin(args) {
    const { record, text } = args;
    // meaning是默认的textfiled 此处用于判断 是否是加载更多的按钮
    if (!record.get('meaning')) {
      // 根据value来判断是哪一个加载更多的按钮
      let progress = null;
      if (record.get('value') === 'tag') {
        progress = moreTagLoading ? <Progress type="loading" size="small" /> : null;
      } else {
        progress = moreBranchLoading ? <Progress type="loading" size="small" /> : null;
      }
      return (
        <div
          onClick={loadMore.bind(this, record.get('value'))}
          className={`${prefixCls}-popover`}
        >
          {progress}
          <span className={`${prefixCls}-popover-option-more`}>{formatMessage({ id: 'loadMore' })}</span>
        </div>);
    }

    return renderOption(record.get('value'));
  }

  // 用于渲染分支来源
  const renderBranchOrigin = (args) => {
    const { text, value } = args;
    if (text || value) {
      return null;
    }
    return renderOption(value);
  };

  function renderOption(text) {
    if (!text) return null;
    return (<span>
      <Icon
        type={text.slice(-7) === '_type_t' ? 'local_offer' : 'branch'}
        className={`${prefixCls}-popover-option-icon`}
      />
      {text && text.slice(0, -7)}
    </span>);
  }

  function searchMatcher() {
    return true;
  }

  function handleInput({ target: { value } }) {
    setSearchValue(value);
    loadData(value);
  }

  function handleBlur(e) {
    if (getBranchData.length === 0 && getTagData.length === 0) {
      selectDs.current.set('branch', null);
    }
    setSearchValue('');
    loadData('');
  }

  function changeRef(obj) {
    if (obj) {
      const fields = obj.fields;
      if (fields instanceof Array && fields.length) {
        const select = fields[0];
        if (select && !selectCom) {
          setSelectCom(select);
        }
      }
    }
  }

  return (
    <Form
      dataSet={selectDs}
      style={{ width: 340 }}
      ref={changeRef}
      columns={3}
    >
      <TextField name="appServiceName" disabled />
      <Select
        name="branch"
        searchable
        searchMatcher={searchMatcher}
        onInput={handleInput}
        onBlur={handleBlur}
        clearButton={false}
        optionRenderer={renderBranchOptionOrigin}
        renderer={renderBranchOrigin}
        colSpan={2}
      >
        <OptGroup
          label={formatMessage({ id: 'branch' })}
          key="proGroup"
        >
          {map(getBranchData, ({ branchName }) => (
            <Option value={`${branchName}_type_b`} key={branchName} title={branchName}>
              {branchName}
            </Option>
          ))}
          {getHasMoreBranch ? (
            <Option value="branch" />
          ) : null}
        </OptGroup>
        <OptGroup
          label={formatMessage({ id: 'tag' })}
          key="more"
        >
          {map(getTagData, ({ release }) => (release
            ? <Option value={`${release.tagName}_type_t`} key={release.tagName}>
              {release.tagName}
            </Option> : null))}
          {getHasMoreTag ? (
            <Option value="tag" />) : null }
        </OptGroup>
      </Select>
    </Form>
  );
});
