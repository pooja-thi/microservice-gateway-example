import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { getEntities as getCategories } from 'app/entities/publication/category/category.reducer';
import { IBook } from 'app/shared/model/publication/book.model';
import { getEntities as getBooks } from 'app/entities/publication/book/book.reducer';
import { getEntity, updateEntity, createEntity, reset } from './category.reducer';
import { ICategory } from 'app/shared/model/publication/category.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface ICategoryUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const CategoryUpdate = (props: ICategoryUpdateProps) => {
  const [idsbook, setIdsbook] = useState([]);
  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const { categoryEntity, categories, books, loading, updating } = props;

  const handleClose = () => {
    props.history.push('/category' + props.location.search);
  };

  useEffect(() => {
    if (isNew) {
      props.reset();
    } else {
      props.getEntity(props.match.params.id);
    }

    props.getCategories();
    props.getBooks();
  }, []);

  useEffect(() => {
    if (props.updateSuccess) {
      handleClose();
    }
  }, [props.updateSuccess]);

  const saveEntity = (event, errors, values) => {
    if (errors.length === 0) {
      const entity = {
        ...categoryEntity,
        ...values,
        books: mapIdList(values.books),
        parent: categories.find(it => it.id.toString() === values.parentId.toString()),
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
          <h2 id="gatewayApp.publicationCategory.home.createOrEditLabel" data-cy="CategoryCreateUpdateHeading">
            <Translate contentKey="gatewayApp.publicationCategory.home.createOrEditLabel">Create or edit a Category</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : categoryEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="category-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="category-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="descriptionLabel" for="category-description">
                  <Translate contentKey="gatewayApp.publicationCategory.description">Description</Translate>
                </Label>
                <AvField
                  id="category-description"
                  data-cy="description"
                  type="text"
                  name="description"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="sortOrderLabel" for="category-sortOrder">
                  <Translate contentKey="gatewayApp.publicationCategory.sortOrder">Sort Order</Translate>
                </Label>
                <AvField id="category-sortOrder" data-cy="sortOrder" type="string" className="form-control" name="sortOrder" />
              </AvGroup>
              <AvGroup>
                <Label id="dateAddedLabel" for="category-dateAdded">
                  <Translate contentKey="gatewayApp.publicationCategory.dateAdded">Date Added</Translate>
                </Label>
                <AvField id="category-dateAdded" data-cy="dateAdded" type="date" className="form-control" name="dateAdded" />
              </AvGroup>
              <AvGroup>
                <Label id="dateModifiedLabel" for="category-dateModified">
                  <Translate contentKey="gatewayApp.publicationCategory.dateModified">Date Modified</Translate>
                </Label>
                <AvField id="category-dateModified" data-cy="dateModified" type="date" className="form-control" name="dateModified" />
              </AvGroup>
              <AvGroup>
                <Label id="statusLabel" for="category-status">
                  <Translate contentKey="gatewayApp.publicationCategory.status">Status</Translate>
                </Label>
                <AvInput
                  id="category-status"
                  data-cy="status"
                  type="select"
                  className="form-control"
                  name="status"
                  value={(!isNew && categoryEntity.status) || 'AVAILABLE'}
                >
                  <option value="AVAILABLE">{translate('gatewayApp.CategoryStatus.AVAILABLE')}</option>
                  <option value="BORROWED">{translate('gatewayApp.CategoryStatus.BORROWED')}</option>
                  <option value="DISABLED">{translate('gatewayApp.CategoryStatus.DISABLED')}</option>
                </AvInput>
              </AvGroup>
              <AvGroup>
                <Label for="category-parent">
                  <Translate contentKey="gatewayApp.publicationCategory.parent">Parent</Translate>
                </Label>
                <AvInput id="category-parent" data-cy="parent" type="select" className="form-control" name="parentId">
                  <option value="" key="0" />
                  {categories
                    ? categories.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.id}
                        </option>
                      ))
                    : null}
                </AvInput>
              </AvGroup>
              <AvGroup>
                <Label for="category-book">
                  <Translate contentKey="gatewayApp.publicationCategory.book">Book</Translate>
                </Label>
                <AvInput
                  id="category-book"
                  data-cy="book"
                  type="select"
                  multiple
                  className="form-control"
                  name="books"
                  value={!isNew && categoryEntity.books && categoryEntity.books.map(e => e.id)}
                >
                  <option value="" key="0" />
                  {books
                    ? books.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.title}
                        </option>
                      ))
                    : null}
                </AvInput>
              </AvGroup>
              <Button tag={Link} id="cancel-save" to="/category" replace color="info">
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
  categories: storeState.category.entities,
  books: storeState.book.entities,
  categoryEntity: storeState.category.entity,
  loading: storeState.category.loading,
  updating: storeState.category.updating,
  updateSuccess: storeState.category.updateSuccess,
});

const mapDispatchToProps = {
  getCategories,
  getBooks,
  getEntity,
  updateEntity,
  createEntity,
  reset,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(CategoryUpdate);
