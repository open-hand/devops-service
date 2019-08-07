import React from 'react';
import { observer, inject } from 'mobx-react';
import ExpandRow from '../components/ExpandRow';
import InstanceStore from './stores/InstancesStore';

const IST_ID = 8693;

@inject('AppState')
@observer
export default class Instances extends React.Component {
  state = {
    record: [],
  };

  componentDidMount() {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    InstanceStore.loadResource(projectId, IST_ID).then((data) => {
      this.setState({ record: data });
    });
  }

  render() {
    return <ExpandRow record={this.state.record} />;
  }
}
