import React, { useState, useEffect } from 'react';
import { axios } from '@choerodon/boot';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { Form, TextField, Select } from 'choerodon-ui/pro';
import { Content } from '@choerodon/boot';
import { injectIntl } from 'react-intl';
import '../../../../main.less';
import './index.less';
import '../index.less';
import MouserOverWrapper from '../../../../../components/MouseOverWrapper';
import InterceptMask from '../../../../../components/intercept-mask';
import { useFormStore } from './store';
import { handlePromptError } from '../../../../../utils';

const { Option, OptGroup } = Select;

function BranchCreate(props) {
  const {
    modal,
    handleRefresh,
    contentStore,
    issueNameOptionDs,
    formDs,
    projectId,
    selectedApp,
  } = useFormStore();
  const { formatMessage } = props.intl;

  let brancgPageSize = 3;
  let tagPageSize = 3;
  const [submitting, setSubmitting] = useState(false);
  const [prefixData, setPrefixeData] = useState('');
  const [branchOringData, setBranchOringData] = useState([]);
  const [branchTagData, setBranchTagData] = useState([]);
  const [loadMoreBranch, setLoadMoreBranch] = useState(false);
  const [loadMoreTag, setLoadMoreTag] = useState(false);

  const recordData = issueNameOptionDs.toData();
  const optionsData = [];
  let issueTypeCode;
  let issueSummry;
  let issueIusseNum;
  let issueIssueId;
  recordData.forEach(item => {
    issueIssueId = item.issueId;
    issueTypeCode = item.typeCode;
    issueSummry = item.summary;
    issueIusseNum = item.issueNum;
    const data = {
      issueId: issueIssueId,
      typeCode: issueTypeCode,
      summary: issueSummry,
      issueNum: issueIusseNum,
    };
    optionsData.push(data);
  });

  useEffect(() => {
    loadBranchData(brancgPageSize);
    loadTagData(tagPageSize);
  }, [selectedApp]);

  /**
   * 加载分支数据
   * @param BranchPageSize
   */
  function loadBranchData(BranchPageSize) {
    axios.post(`devops/v1/projects/${projectId}/app_service/${selectedApp}/git/page_branch_by_options?page=1&size=${BranchPageSize}&sort=creation_date,asc`)
      .then((data) => {
        if (handlePromptError(data)) {
          setBranchOringData(data.list);
          if (data.total > data.size && data.size > 0) {
            setLoadMoreBranch(true);
          }
        }
      });
  }
  /**
   * 加载标记数据
   * @param TagPageSize
   */
  function loadTagData(TagPageSize) {
    axios.post(`/devops/v1/projects/${projectId}/app_service/${selectedApp}/git/page_tags_by_options?page=1&size=${TagPageSize}`)
      .then((data) => {
        if (handlePromptError(data)) {
          setBranchTagData(data.list);
          if (data.total > data.size && data.size > 0) {
            setLoadMoreTag(true);
          }
        }
      });
  }
  /**
   * 创建
   */
  async function handleOk() {
    try {
      setSubmitting(true);
      if ((await formDs.submit()) !== false) {
        handleRefresh();
        setSubmitting(false);
        return true;
      } else {
        setSubmitting(false);
        return false;
      }
    } catch (e) {
      setSubmitting(false);
      return false;
    }
  }
  modal.handleOk(() => handleOk());
  /**
   * 切换issue
   * @param value
   */
  const changeIssue = (value) => {
    let key = '';
    let type;
    let branchName;
    optionsData.forEach((item) => {
      if (item.issueId === value) {
        key = item.typeCode;
        branchName = item.issueNum;
      }
    });
    formDs.current.set('branchName', branchName);
    switch (key) {
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
      setPrefixeData(text);
      contentStore.setBranchPrefix(text);
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
  const issueNameRender = ({ text }) => {
    let renderTypeCode;
    let renderIssueNum;
    let renderSummary;
    optionsData.forEach(item => {
      if (item.issueId === Number(text)) {
        renderTypeCode = item.typeCode;
        renderIssueNum = item.issueNum;
        renderSummary = item.summary;
      }
    });
    return (
      text ? <span>
        {renderissueName(renderTypeCode, renderIssueNum, renderSummary)}
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
  // 用于渲染分支来源
  const renderBranchOrigin = ({ text }) => {
    if (!text) {
      return null;
    }
    if (typeof text !== 'object') {
      text = text.split(',');
    }
    return text.length === 1 ? <span>
      <i className="icon icon-branch c7n-branch-formItem-icon" />
      {text[0]}
    </span> : <span>
      <i className="icon icon-local_offer c7n-branch-formItem-icon" />
      {text[1]}
    </span>;
  };
  const loadMore = (type, e) => {
    e.stopPropagation();
    if (type === 'branch') {
      brancgPageSize += 10;
      loadBranchData(brancgPageSize);
      setLoadMoreBranch(false);
    } else {
      tagPageSize += 10;
      loadTagData(tagPageSize);
      setLoadMoreTag(false);
    }
  };
  const rednerBranchOptionOrigin = ({ text }) => {
    if (typeof text === 'object' && 'props' in text) {
      if (text.props.type === 'branch') {
        return (
          <div
            onClick={loadMore.bind(this, 'branch')}
            className="c7n-option-popover c7n-dom-more"
          >
            {formatMessage({ id: 'loadMore' })}
          </div>
        );
      } else {
        return (
          <div
            onClick={loadMore.bind(this, 'tag')}
            className="c7n-option-popover c7n-dom-more"
          >
            {formatMessage({ id: 'loadMore' })}
          </div>
        );
      }
    }
    return (
      <span>
        {renderBranchOrigin({ text })}
      </span>
    );
  };
  return (
    <Content className="sidebar-content c7n-createBranch">
      <div style={{ width: '75%' }}>
        <Form
          dataSet={formDs}
          columns={5}
        >
          <Select colSpan={5} onChange={changeIssue} optionRenderer={issueNameOptionRender} renderer={issueNameRender} name="issueName" />
          <Select
            colSpan={5}
            name="branchOrigin"
            optionRenderer={rednerBranchOptionOrigin}
            renderer={renderBranchOrigin}
          >
            <OptGroup
              label={formatMessage({ id: 'branch.branch' })}
              key="proGroup"
            >
              {branchOringData.map((s) => (
                <Option value={s.branchName} key={s.branchName} title={s.branchName}>
                  {s.branchName}
                </Option>
              ))}
              {loadMoreBranch ? (
                <Option value="more">
                  <span type="branch" />
                </Option>
              ) : null}
            </OptGroup>
            <OptGroup
              label={formatMessage({ id: 'branch.tag' })}
              key="more"
            >
              {branchTagData.map((s) => (s.release
                ? <Option value={s.release.tagName} key={s.release.tagName}>
                  <i style={{ display: 'none' }} className="icon icon-local_offer c7n-branch-formItem-icon" />
                  {s.release.tagName}
                </Option> : null))}
              {loadMoreTag ? (
                <Option value="more">
                  <span type="tag" />
                </Option>) : null }
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
          <TextField colSpan={3} addonBefore={prefixData} name="branchName" />
        </Form>
      </div>
      <InterceptMask visible={submitting} />
    </Content>
  );
}
export default withRouter(injectIntl(observer(BranchCreate)));
