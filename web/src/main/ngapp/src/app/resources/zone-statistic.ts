export class ZoneStatistic{
  zoneId: number;
  zoneName: string;
  averagePersonCount: number;
  averageSittingCount: number;
  averageStandingCount: number;
  fromTimeStamp: number;
  toTimeStamp: number;
  totalOutgoing: number;
  totalIncoming: number;
  outgoingMap: {[id:number] : number};
  incomingMap: {[id:number] : number};
}
