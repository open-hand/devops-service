import React, { useState, useEffect, useMemo } from 'react';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { Form, TextField, Select, Progress } from 'choerodon-ui/pro';
import { Content, axios } from '@choerodon/boot';
import { injectIntl } from 'react-intl';
import _ from 'lodash';
import MouserOverWrapper from '../../../../../components/MouseOverWrapper';
import { useFormStore } from './stores';
import { handlePromptError } from '../../../../../utils';

import '../../../../main.less';
import './index.less';
import '../index.less';

const { Option, OptGroup } = Select;

function BranchCreate(props) {
  const {
    modal,
    handleRefresh,
    contentStore,
    formDs,
    projectId,
    appServiceId,
  } = useFormStore();
  const { formatMessage } = props.intl;


  const [branchPageSize, setBranchPageSize] = useState(3);
  const [tagPageSize, setTagPageSize] = useState(3);
  const [prefixData, setPrefixeData] = useState('');
  const [branchOringData, setBranchOringData] = useState([]);
  const [branchTagData, setBranchTagData] = useState([]);
  const [loadMoreBranch, setLoadMoreBranch] = useState(false);
  const [loadMoreTag, setLoadMoreTag] = useState(false);
  const [moreTagLoading, setMoreTagLoading] = useState(false);
  const [moreBranchLoading, setMoreBranchLoading] = useState(false);
  const [selectCom, setSelectCom] = useState(null);
  const [isCallHandleInput, setIsCallHandleInput] = useState(false);
  const [isOPERATIONS, setIsOPERATIONS] = useState(false);

  useEffect(() => {
    const pattern = new URLSearchParams(window.location.hash);
    if (pattern.get('category') === 'OPERATIONS') {
      setIsOPERATIONS(true);
    }
  }, []);

  useEffect(() => {
    loadBranchData(branchPageSize);
    loadTagData(tagPageSize);
  }, [appServiceId]);

  const searchData = useMemo(() => _.debounce((text) => {
    if (selectCom && selectCom.options) {
      selectCom.options.changeStatus('loading');
    }
    axios.all([contentStore.loadBranchData(projectId, appServiceId, branchPageSize, text), contentStore.loadTagData(projectId, appServiceId, tagPageSize, text)])
      .then(axios.spread((branchs, tags) => {
        if (selectCom && selectCom.options) {
          selectCom.options.changeStatus('ready');
        }
        if (handlePromptError(branchs) || handlePromptError(tags)) {
          setLoadMoreBranch(judgeShowMore(branchs));
          setLoadMoreTag(judgeShowMore(tags));
          const value = formDs.current.get('branchOrigin');
          if (!text
            && value
            && !(_.findIndex(branchs.list, (item) => item.branchName === value.slice(0, -7)) !== -1
            || _.findIndex(tags.list, (item) => item.release.tagName === value.slice(0, -7)) !== -1)) {
            if (value.slice(-7) === '_type_b') {
              branchs.list.push({
                branchName: value.slice(0, -7),
              });
            } else {
              tags.list.push({
                release: {
                  tagName: value.slice(0, -7),
                },
              });
            }
          }
          setBranchOringData(branchs.list || []);
          setBranchTagData(tags.list || []);
        }
      }));
  }, 700), [selectCom, projectId, appServiceId, branchPageSize, tagPageSize]);


  /**
   * 加载分支数据
   * @param BranchPageSize
   */
  async function loadBranchData(BranchPageSize) {
    setMoreBranchLoading(true);
    const data = await contentStore.loadBranchData(projectId, appServiceId, BranchPageSize);
    setMoreBranchLoading(false);
    if (handlePromptError(data)) {
      setBranchOringData(data.list);
      setLoadMoreBranch(judgeShowMore(data));
    }
  }

  /**
   * 加载标记数据
   * @param TagPageSize
   */
  async function loadTagData(TagPageSize) {
    setMoreTagLoading(true);
    const data = await contentStore.loadTagData(projectId, appServiceId, TagPageSize);
    setMoreTagLoading(false);
    if (handlePromptError(data)) {
      setBranchTagData(data.list);
      setLoadMoreTag(judgeShowMore(data));
    }
  }
  /**
   * 创建
   */
  async function handleOk() {
    try {
      if ((await formDs.submit()) !== false) {
        handleRefresh();
        return true;
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }
  modal.handleOk(handleOk);

  /**
   * 切换issue
   * @param value
   */
  const changeIssue = (value) => {
    let type;
    const { typeCode, issueNum } = value || {};
    formDs.current.set('branchName', issueNum);
    switch (typeCode) {
      case 'story':
        type = 'feature';
        break;
      case 'bug':
        type = 'bugfix';
        break;
      case 'issue_epic':
        type = 'custom';
        break;
      case 'sub_task':
        type = 'feature';
        break;
      case 'task':
        type = 'feature';
        break;
      default:
        type = 'custom';
    }
    formDs.current.set('branchType', type);
  };

  /**
   * 获取列表的icon
   * @param type 分支类型
   * @returns {*}
   */
  const getIcon = (type) => {
    let icon;
    switch (type) {
      case 'feature':
        icon = <span className="c7n-branch-icon icon-feature">F</span>;
        break;
      case 'bugfix':
        icon = <span className="c7n-branch-icon icon-develop">B</span>;
        break;
      case 'hotfix':
        icon = <span className="c7n-branch-icon icon-hotfix">H</span>;
        break;
      case 'master':
        icon = <span className="c7n-branch-icon icon-master">M</span>;
        break;
      case 'release':
        icon = <span className="c7n-branch-icon icon-release">R</span>;
        break;
      default:
        icon = <span className="c7n-branch-icon icon-custom">C</span>;
    }
    return icon;
  };

  // 用于分支类型的渲染函数
  const renderBranchType = ({ text }) => {
    if (text !== 'custom') {
      setPrefixeData(text ? `${text}-` : '');
      contentStore.setBranchPrefix(text ? `${text}-` : '');
    } else {
      contentStore.setBranchPrefix(null);
      setPrefixeData(null);
    }
    return (
      text ? <div>
        <div style={{ width: '100%' }}>
          {getIcon(text)}
          <span className="c7n-branch-text">{text}</span>
        </div>
      </div> : null
    );
  };
  // 用于分支类型的渲染函数
  const renderOptionsBranchType = ({ text }) => (
    <div style={{ width: '100%' }}>
      {getIcon(text)}
      <span className="c7n-branch-text">{text}</span>
    </div>
  );

  // 用于问题名称的渲染函数
  const renderissueName = (typeCode, issueNum, summary) => {
    let mes = '';
    let icon = '';
    let color = '';
    switch (typeCode) {
      case 'story':
        mes = formatMessage({ id: 'branch.issue.story' });
        icon = 'agile_story';
        color = '#00bfa5';
        break;
      case 'bug':
        mes = formatMessage({ id: 'branch.issue.bug' });
        icon = 'agile_fault';
        color = '#f44336';
        break;
      case 'issue_epic':
        mes = formatMessage({ id: 'branch.issue.epic' });
        icon = 'agile_epic';
        color = '#743be7';
        break;
      case 'sub_task':
        mes = formatMessage({ id: 'branch.issue.subtask' });
        icon = 'agile_subtask';
        color = '#4d90fe';
        break;
      default:
        mes = formatMessage({ id: 'branch.issue.task' });
        icon = 'agile_task';
        color = '#4d90fe';
    }
    return (
      <span>
        <div style={{ color }} className="branch-issue">
          <i className={`icon icon-${icon}`} />
        </div>
        <span className="branch-issue-content">
          <span style={{ color: 'rgb(0,0,0,0.65)' }}>{issueNum}</span>
          <MouserOverWrapper
            style={{ display: 'inline-block', verticalAlign: 'sub' }}
            width="350px"
            text={summary}
          >
            {summary}
          </MouserOverWrapper>
        </span>
      </span>
    );
  };

  const issueNameRender = ({ text, value }) => {
    const { typeCode, issueNum, summary } = value || {};
    return (
      text ? <span>
        {renderissueName(typeCode, issueNum, summary)}
      </span> : null
    );
  };
  const issueNameOptionRender = ({ record }) => {
    const typeCode = record.get('typeCode');
    const issueNum = record.get('issueNum');
    const summary = record.get('summary');
    return (
      <span>
        {renderissueName(typeCode, issueNum, summary)}
      </span>
    );
  };


  const loadMore = (type, e) => {
    e.stopPropagation();
    if (type === 'branch') {
      const pageSize = branchPageSize + 10;
      setBranchPageSize(pageSize);
      loadBranchData(pageSize);
    } else {
      const pageSize = tagPageSize + 10;
      setTagPageSize(pageSize);
      loadTagData(pageSize);
    }
  };

  const rednerBranchOptionOrigin = (args) => {
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
          className="c7n-option-popover"
        >
          {progress}
          <span className="c7n-option-span">{formatMessage({ id: 'loadMore' })}</span>
        </div>);
    }

    return renderOption(record.get('value'));
  };

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
      <i className={`icon c7n-branch-formItem-icon ${text.slice(-7) === '_type_t' ? 'icon-local_offer' : 'icon-branch'}`} />
      {text && text.slice(0, -7)}
    </span>);
  }

  function searchMatcher() {
    return true;
  }

  function handleInput({ target: { value } }) {
    if (!isCallHandleInput) setIsCallHandleInput(true);
    searchData(value);
  }


  function changeRef(obj) {
    if (obj) {
      const fields = obj.fields;
      if (fields instanceof Array && fields.length > 0) {
        const select = fields[1];
        if (select && !selectCom) {
          setSelectCom(select);
        }
      }
    }
  }

  function handleBlur(e) {
    if (branchOringData.length === 0 && branchTagData.length === 0) {
      formDs.current.set('branchOrigin', null);
    }
    searchData('');
  }

  return (
    <Content className="sidebar-content c7n-createBranch">
      <div style={{ width: '75%' }}>
        <Form
          dataSet={formDs}
          columns={5}
          ref={changeRef}
        >
          {
            !isOPERATIONS
              && <Select
                name="issue"
                colSpan={5}
                onChange={changeIssue}
                optionRenderer={issueNameOptionRender}
                renderer={issueNameRender}
                searchable
                searchMatcher="content"
              />
          }
          <Select
            colSpan={5}
            name="branchOrigin"
            searchMatcher={searchMatcher}
            onInput={handleInput}
            onBlur={handleBlur}
            searchable
            optionRenderer={rednerBranchOptionOrigin}
            renderer={renderBranchOrigin}
          >
            <OptGroup
              label={formatMessage({ id: 'branch.branch' })}
              key="proGroup"
            >
              {branchOringData.map((s) => (
                <Option value={`${s.branchName}_type_b`} key={s.branchName} title={s.branchName}>
                  {s.branchName}
                </Option>
              ))}
              {loadMoreBranch ? (
                <Option value="branch" />
              ) : null}
            </OptGroup>
            <OptGroup
              label={formatMessage({ id: 'branch.tag' })}
              key="more"
            >
              {branchTagData.map((s) => (s.release
                ? <Option value={`${s.release.tagName}_type_t`} key={s.release.tagName}>
                  {s.release.tagName}
                </Option> : null))}
              {loadMoreTag ? (
                <Option value="tag" />) : null }
            </OptGroup>
          </Select>
          <Select colSpan={2} name="branchType" renderer={renderBranchType} optionRenderer={renderOptionsBranchType}>
            {['feature', 'bugfix', 'release', 'hotfix', 'custom'].map(
              (s) => (
                <Option value={s} key={s} title={s}>
                  {s}
                </Option>
              )
            )}
          </Select>
          <TextField colSpan={3} prefix={prefixData} name="branchName" />
        </Form>
      </div>
    </Content>
  );
}
export default withRouter(injectIntl(observer(BranchCreate)));


function judgeShowMore(data) {
  return data.total > data.size && data.size > 0;
}
