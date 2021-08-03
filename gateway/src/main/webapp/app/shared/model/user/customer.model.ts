import { IAddress } from 'app/shared/model/user/address.model';

export interface ICustomer {
  id?: number;
  firstName?: string | null;
  lastName?: string | null;
  email?: string | null;
  telephone?: string | null;
  addresses?: IAddress[] | null;
}

export const defaultValue: Readonly<ICustomer> = {};
