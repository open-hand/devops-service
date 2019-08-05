import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const CertificateHome = asyncRouter(() => import('./certificateHome'), () => import('../../stores/project/certificate'));

const CertificateIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={CertificateHome} />
    <Route path={'*'} component={nomatch} />
  </Switch>
);

export default CertificateIndex;
