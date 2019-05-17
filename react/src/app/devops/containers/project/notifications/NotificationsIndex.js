/**
 * @author ale0720@163.com
 * @date 2019-05-13 13:22
 */
import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const Notifications = asyncRouter(
  () => import('./notificationsHome'),
  () => import('../../../stores/project/notifications'),
);

const NotificationsIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={Notifications} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default NotificationsIndex;
