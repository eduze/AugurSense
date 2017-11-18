export class ZoneStatistic{
  zoneId: number;
  zoneName: string;
  averagePersonCount: number;
  averageSittingCount: number;
  averageStandingCount: number;
  fromTimestamp: number;
  toTimestamp: number;
  totalOutgoing: number;
  totalIncoming: number;
  outgoingMap: {[id:number] : number};
  incomingMap: {[id:number] : number};

  totalCountVariation : {[id:number] : number};

  totalStandingCountVariation : {[id:number] : number};
  totalSittingCountVariation : {[id:number] : number};

}
