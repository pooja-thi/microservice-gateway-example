import dayjs from 'dayjs';
import { IBook } from 'app/shared/model/publication/book.model';
import { CategoryStatus } from 'app/shared/model/enumerations/category-status.model';

export interface ICategory {
  id?: number;
  description?: string;
  sortOrder?: number | null;
  dateAdded?: string | null;
  dateModified?: string | null;
  status?: CategoryStatus | null;
  parent?: ICategory | null;
  books?: IBook[] | null;
}

export const defaultValue: Readonly<ICategory> = {};
