export type statusKinds = 'success' | 'pending' | 'failed' | 'default';

export type statusObj = {
  text:string,
  bgColor:string,
  fontColor:string,
}

export type statusKindsMap = Record<statusKinds, statusObj>

export interface StatusTagOutLineProps {
  fontSize?: number | string,
  status: statusKinds,
}
