import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { ICustomer } from 'app/shared/model/user/customer.model';
import { getEntities as getCustomers } from 'app/entities/user/customer/customer.reducer';
import { getEntity, updateEntity, createEntity, reset } from './address.reducer';
import { IAddress } from 'app/shared/model/user/address.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IAddressUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const AddressUpdate = (props: IAddressUpdateProps) => {
  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const { addressEntity, customers, loading, updating } = props;

  const handleClose = () => {
    props.history.push('/address' + props.location.search);
  };

  useEffect(() => {
    if (isNew) {
      props.reset();
    } else {
      props.getEntity(props.match.params.id);
    }

    props.getCustomers();
  }, []);

  useEffect(() => {
    if (props.updateSuccess) {
      handleClose();
    }
  }, [props.updateSuccess]);

  const saveEntity = (event, errors, values) => {
    if (errors.length === 0) {
      const entity = {
        ...addressEntity,
        ...values,
        customer: customers.find(it => it.id.toString() === values.customerId.toString()),
      };

      if (isNew) {
        props.createEntity(entity);
      } else {
        props.updateEntity(entity);
      }
    }
  };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="gatewayApp.userAddress.home.createOrEditLabel" data-cy="AddressCreateUpdateHeading">
            <Translate contentKey="gatewayApp.userAddress.home.createOrEditLabel">Create or edit a Address</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : addressEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="address-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="address-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="address1Label" for="address-address1">
                  <Translate contentKey="gatewayApp.userAddress.address1">Address 1</Translate>
                </Label>
                <AvField id="address-address1" data-cy="address1" type="text" name="address1" />
              </AvGroup>
              <AvGroup>
                <Label id="address2Label" for="address-address2">
                  <Translate contentKey="gatewayApp.userAddress.address2">Address 2</Translate>
                </Label>
                <AvField id="address-address2" data-cy="address2" type="text" name="address2" />
              </AvGroup>
              <AvGroup>
                <Label id="cityLabel" for="address-city">
                  <Translate contentKey="gatewayApp.userAddress.city">City</Translate>
                </Label>
                <AvField id="address-city" data-cy="city" type="text" name="city" />
              </AvGroup>
              <AvGroup>
                <Label id="postcodeLabel" for="address-postcode">
                  <Translate contentKey="gatewayApp.userAddress.postcode">Postcode</Translate>
                </Label>
                <AvField
                  id="address-postcode"
                  data-cy="postcode"
                  type="text"
                  name="postcode"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                    maxLength: { value: 10, errorMessage: translate('entity.validation.maxlength', { max: 10 }) },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="countryLabel" for="address-country">
                  <Translate contentKey="gatewayApp.userAddress.country">Country</Translate>
                </Label>
                <AvField
                  id="address-country"
                  data-cy="country"
                  type="text"
                  name="country"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                    maxLength: { value: 2, errorMessage: translate('entity.validation.maxlength', { max: 2 }) },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label for="address-customer">
                  <Translate contentKey="gatewayApp.userAddress.customer">Customer</Translate>
                </Label>
                <AvInput id="address-customer" data-cy="customer" type="select" className="form-control" name="customerId">
                  <option value="" key="0" />
                  {customers
                    ? customers.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.id}
                        </option>
                      ))
                    : null}
                </AvInput>
              </AvGroup>
              <Button tag={Link} id="cancel-save" to="/address" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </AvForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

const mapStateToProps = (storeState: IRootState) => ({
  customers: storeState.customer.entities,
  addressEntity: storeState.address.entity,
  loading: storeState.address.loading,
  updating: storeState.address.updating,
  updateSuccess: storeState.address.updateSuccess,
});

const mapDispatchToProps = {
  getCustomers,
  getEntity,
  updateEntity,
  createEntity,
  reset,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(AddressUpdate);
