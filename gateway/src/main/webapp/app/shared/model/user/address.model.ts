import { ICustomer } from 'app/shared/model/user/customer.model';

export interface IAddress {
  id?: number;
  address1?: string | null;
  address2?: string | null;
  city?: string | null;
  postcode?: string;
  country?: string;
  customer?: ICustomer | null;
}

export const defaultValue: Readonly<IAddress> = {};
