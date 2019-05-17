import React, { Component, Fragment } from 'react';
import { FormattedMessage } from 'react-intl';
import { Avatar, Pagination, Spin, Tooltip } from 'choerodon-ui';
import TimePopover from '../../../../components/timePopover';
import './Submission.scss';

export default function CommitHistory(props) {
  const { dataSource: { content, totalElements, number }, onPageChange, loading } = props;
  let list = [];
  if (content && content.length) {
    list = content.map((item) => {
      const { userName, url, commitContent, commitDate, imgUrl, appName, commitSHA } = item;
      return (
        <div className="c7n-report-history-item" key={`${commitSHA}-${commitDate}`}>
          {imgUrl
            ? <Avatar size="small" src={imgUrl} />
            : <Avatar size="small">{userName ? userName.toString().slice(0, 1).toUpperCase() : '?'}</Avatar>}
          <div className="c7n-report-history-info">
            <div className="c7n-report-history-content">
              <a
                className="c7n-report-history-link"
                href={url}
                rel="nofollow me noopener noreferrer"
                target="_blank"
              >{commitContent}</a>
            </div>
            <div className="c7n-report-history-date">
              <span className="c7n-report-history-name">{userName || (<Tooltip placement="top" title={<FormattedMessage id="report.unknown.user" />}>Unknown</Tooltip>)}</span><span>（{appName}）</span>
              <FormattedMessage id="report.commit.by" /> <TimePopover style={{ display: 'inline-block' }} content={commitDate} />
            </div>
          </div>
        </div>
      );
    });
  } else {
    list = [<span key="no.commits" className="c7n-report-history-list-none"><FormattedMessage id="report.commit.none" /></span>];
  }
  return (<Fragment>
    <h3 className="c7n-report-history-title"><FormattedMessage id="report.commit.history" /></h3>
    <Spin spinning={loading}>
      <div className="c7n-report-history-list">{list}</div>
    </Spin>
    <div className="c7n-report-history-page">
      {totalElements ? (<Pagination
        tiny
        size="small"
        total={totalElements || 0}
        current={number + 1 || 1}
        pageSize={5}
        showSizeChanger={false}
        onChange={onPageChange}
      />) : null}
    </div>
  </Fragment>);
}
