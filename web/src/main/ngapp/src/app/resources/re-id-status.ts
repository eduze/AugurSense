import {PersonImage} from "./person-image";

export class ReIdStatus {
  results: PersonImage[];
  pending: boolean;
  completed: boolean;
  invalid: boolean;
}
