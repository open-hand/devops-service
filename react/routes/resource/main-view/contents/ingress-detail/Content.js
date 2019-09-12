import React from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Icon } from 'choerodon-ui';
import map from 'lodash/map';
import { Tooltip } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';
import { useCustomDetailStore } from './stores';
import Modals from './modals';
import StatusTags from '../../../../../components/status-tag';

import './index.less';


const statusTagsStyle = {
  minWidth: 40,
  marginRight: 8,
  height: '0.16rem',
  lineHeight: '0.16rem',
};

const Content = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    treeDs,
  } = useResourceStore();
  const {
    detailDs,
    intl: { formatMessage },
  } = useCustomDetailStore();

  const record = detailDs.current;
  if (!record) return <span>loading</span>;

  function refresh() {
    treeDs.query();
    detailDs.query();
  }

  return (
    <div className={`${prefixCls}-ingress-detail`}>
      <Modals />
      <div className={`${prefixCls}-detail-content-title`}>
        <Icon type="language" className={`${prefixCls}-detail-content-title-icon`} />
        <span>{record.get('name')}</span>
      </div>
      <div>
        <div className={`${prefixCls}-detail-content-section-title`}>
          <FormattedMessage id="routing" />
          <span className="detail-content-section-title-hover">(Rules)</span>
        </div>
        <div className={`${prefixCls}-detail-content-section-name`}>
          <span>{record.get('domain')}</span>
        </div>
        <ul className={`${prefixCls}-detail-section-ul`}>
          {map(record.get('pathList'), ({ path, serviceName, servicePort, serviceStatus }) => (
            <li className={`${prefixCls}-detail-section-li`}>
              <table className="detail-section-li-table">
                <tbody>
                  <td className="td-width-30">
                    <span className="detail-section-li-text">
                      {formatMessage({ id: 'path' })}:&nbsp;
                    </span>
                    <span>{path}</span>
                  </td>
                  <td className="detail-section-service">
                    <span className="detail-section-li-text">
                      {formatMessage({ id: 'network' })}:&nbsp;
                    </span>
                    <div className="detail-section-service">
                      <StatusTags
                        colorCode={serviceStatus}
                        name={formatMessage({ id: serviceStatus })}
                        style={statusTagsStyle}
                      />
                      <span>{serviceName}</span>
                    </div>
                  </td>
                  <td className="td-width-20">
                    <span className="detail-section-li-text">
                      {formatMessage({ id: 'port' })}:&nbsp;
                    </span>
                    <span>{servicePort}</span>
                  </td>
                  <td className="td-width-6px">
                    <a
                      rel="nofollow me noopener noreferrer"
                      target="_blank"
                      href={`http://${record.get('domain')}${path}`}
                    >
                      <FormattedMessage id={`${intlPrefix}.click.visit`} />
                    </a>
                  </td>
                </tbody>
              </table>
            </li>
          ))}
        </ul>
      </div>
      <div>
        <div className={`${prefixCls}-detail-content-section-title`}>
          <FormattedMessage id="annotation" />
          <span className="detail-content-section-title-hover">(Annotations)</span>
        </div>
        <ul className={`${prefixCls}-detail-section-ul`}>
          {map(record.get('annotations'), (value, key) => (
            <li className={`${prefixCls}-detail-section-li`}>
              <span className="ingress-detail-annotation">{key}</span>
              <Tooltip title={<div className={`${prefixCls}-detail-section-li-tooltip`}>{value}</div>} arrowPointAtCenter>
                <span className="ingress-detail-annotation-value">{value}</span>
              </Tooltip>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
});

export default Content;
