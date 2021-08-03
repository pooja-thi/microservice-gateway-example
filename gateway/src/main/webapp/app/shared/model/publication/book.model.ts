import dayjs from 'dayjs';
import { ICategory } from 'app/shared/model/publication/category.model';

export interface IBook {
  id?: number;
  title?: string;
  author?: string | null;
  keywords?: string | null;
  description?: string | null;
  rating?: number | null;
  dateAdded?: string | null;
  dateModified?: string | null;
  imageContentType?: string | null;
  image?: string | null;
  categories?: ICategory[] | null;
}

export const defaultValue: Readonly<IBook> = {};
