import { combineReducers } from 'redux';
import { loadingBarReducer as loadingBar } from 'react-redux-loading-bar';

import locale, { LocaleState } from './locale';
import authentication, { AuthenticationState } from './authentication';
import applicationProfile, { ApplicationProfileState } from './application-profile';

import administration, { AdministrationState } from 'app/modules/administration/administration.reducer';
import userManagement, { UserManagementState } from './user-management';
// prettier-ignore
import address, {
  AddressState
} from 'app/entities/user/address/address.reducer';
// prettier-ignore
import book, {
  BookState
} from 'app/entities/publication/book/book.reducer';
// prettier-ignore
import category, {
  CategoryState
} from 'app/entities/publication/category/category.reducer';
// prettier-ignore
import customer, {
  CustomerState
} from 'app/entities/user/customer/customer.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

export interface IRootState {
  readonly authentication: AuthenticationState;
  readonly locale: LocaleState;
  readonly applicationProfile: ApplicationProfileState;
  readonly administration: AdministrationState;
  readonly userManagement: UserManagementState;
  readonly address: AddressState;
  readonly book: BookState;
  readonly category: CategoryState;
  readonly customer: CustomerState;
  /* jhipster-needle-add-reducer-type - JHipster will add reducer type here */
  readonly loadingBar: any;
}

const rootReducer = combineReducers<IRootState>({
  authentication,
  locale,
  applicationProfile,
  administration,
  userManagement,
  address,
  book,
  category,
  customer,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
  loadingBar,
});

export default rootReducer;
