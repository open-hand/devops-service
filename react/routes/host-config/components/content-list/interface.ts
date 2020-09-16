export type statusKinds = 'success' | 'operating' | 'failed' | 'default';

export type statusObj = {
  text:string,
  hoverText:string,
  bgColor:string,
  fontColor:string,
}

export type statusKindsMap = Record<statusKinds, statusObj>

export interface StatusTagOutLineProps {
  fontSize?: number | string,
  status: statusKinds,
}
