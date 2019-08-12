import React, { Fragment, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Icon } from 'choerodon-ui';
import map from 'lodash/map';
import { useDeploymentStore } from '../../../stores';
import { useCustomDetailStore } from './stores';
import Modals from './modals';

import './index.less';
import StatusTags from '../../../../../components/StatusTags';

const Content = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    deploymentStore: { getSelectedMenu: { menuId, parentId } },
  } = useDeploymentStore();
  const {
    detailDs,
    intl: { formatMessage },
  } = useCustomDetailStore();

  const record = detailDs.current;
  if (!record) return <span>loading</span>;

  function refresh() {
    detailDs.query();
  }

  return (
    <div className={`${prefixCls}-ingress-detail`}>
      <Modals />
      <div className="detail-content-title">
        <Icon type="language" className="detail-content-title-icon" />
        <span>{record.get('name')}</span>
      </div>
      <div>
        <div className="detail-content-section-title">
          <FormattedMessage id="routing" />
          <span className="detail-content-section-title-hover">(Rules)</span>
        </div>
        <div className="detail-content-section-name">
          <span>{record.get('domain')}</span>
        </div>
        <ul className="detail-section-ul">
          {map(record.get('pathList'), ({ path, serviceName, servicePort, serviceStatus }) => (
            <li className="detail-section-li">
              <table>
                <tbody>
                  <td>
                    <span className="detail-section-li-text">
                      {formatMessage({ id: 'path' })}:&nbsp;
                    </span>
                    <span>{path}</span>
                  </td>
                  <td>
                    <span className="detail-section-li-text">
                      {formatMessage({ id: 'network' })}:&nbsp;
                    </span>
                    <div className="detail-section-service">
                      <StatusTags
                        colorCode={serviceStatus}
                        name={formatMessage({ id: serviceStatus })}
                        style={{
                          minWidth: 40,
                          marginRight: 8,
                        }}
                      />
                      <span>{serviceName}</span>
                    </div>
                  </td>
                  <td>
                    <span className="detail-section-li-text">
                      {formatMessage({ id: 'port' })}:&nbsp;
                    </span>
                    <span>{servicePort}</span>
                  </td>
                  <td>
                    <a rel="nofollow me noopener noreferrer" target="_blank">
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
        <div className="detail-content-section-title">
          <FormattedMessage id="annotation" />
          <span className="detail-content-section-title-hover">(Annotations)</span>
        </div>
        <ul className="detail-section-ul">
          {map(record.get('annotations'), (value, key) => (
            <li className="detail-section-li">
              <span>{key}</span>
              <span>{value}</span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
});

export default Content;
