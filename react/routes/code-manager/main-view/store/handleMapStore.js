import { observable, action, computed, set } from 'mobx';
import { axios, store, stores } from '@choerodon/master';

@store('HandleMapStore')
class HandleMapStore {
  @observable CodeQuality;

  @observable CodeManagerBranch;

  @observable CodeManagerMergeRequest;
  
  @observable CodeManagerAppTag;

  @observable CodeManagerCiPipelineManage;

  @action
  setCodeQuality(obj) {
    this.CodeQuality = obj;
  }

  @action 
  setCodeManagerBranch(obj) {
    this.CodeManagerBranch = obj;
  }

  @action
  setCodeManagerMergeRequest(obj) {
    this.CodeManagerMergeRequest = obj;
  }

  @action
  setCodeManagerAppTag(obj) {
    this.CodeManagerAppTag = obj;
  }

  @action
  setCodeManagerCiPipelineManage(obj) {
    this.CodeManagerCiPipelineManage = obj;
  }
}

const handleMapStore = new HandleMapStore();

export default handleMapStore;
