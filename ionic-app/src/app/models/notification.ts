export class Notification {

  private _id: string;
  private _title: string;
  private _message: string;
  private _timestamp: Date;
  private _seen: boolean;

  constructor(id: string, title: string, message: string, timestamp: Date, seen: boolean) {
    this._id = id;
    this._title = title;
    this._message = message;
    this._timestamp = timestamp;
    this._seen = seen;
  }

  get title(): string {
    return this._title;
  }

  set title(value: string) {
    this._title = value;
  }

  get message(): string {
    return this._message;
  }

  set message(value: string) {
    this._message = value;
  }

  get timestamp(): Date {
    return this._timestamp;
  }

  set timestamp(value: Date) {
    this._timestamp = value;
  }

  get id(): string {
    return this._id;
  }

  set id(value: string) {
    this._id = value;
  }

  get seen(): boolean {
    return this._seen;
  }

  set seen(value: boolean) {
    this._seen = value;
  }

  public static fromJSON(key: string, obj: any) {
    return new Notification(
      key,
      obj.title,
      obj.message,
      new Date(obj.timestamp),
      obj.seen ? obj.seen as boolean : false
    );
  }

  public toJSON() {
    return {
      title: this.title,
      message: this.message,
      timestamp: this.timestamp.getTime(),
      seen: this.seen
    }
  }
}
