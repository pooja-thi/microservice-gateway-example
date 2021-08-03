import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
import { setFileData, openFile, byteSize, Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { ICategory } from 'app/shared/model/publication/category.model';
import { getEntities as getCategories } from 'app/entities/publication/category/category.reducer';
import { getEntity, updateEntity, createEntity, setBlob, reset } from './book.reducer';
import { IBook } from 'app/shared/model/publication/book.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IBookUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const BookUpdate = (props: IBookUpdateProps) => {
  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const { bookEntity, categories, loading, updating } = props;

  const { description, image, imageContentType } = bookEntity;

  const handleClose = () => {
    props.history.push('/book');
  };

  useEffect(() => {
    if (!isNew) {
      props.getEntity(props.match.params.id);
    }

    props.getCategories();
  }, []);

  const onBlobChange = (isAnImage, name) => event => {
    setFileData(event, (contentType, data) => props.setBlob(name, data, contentType), isAnImage);
  };

  const clearBlob = name => () => {
    props.setBlob(name, undefined, undefined);
  };

  useEffect(() => {
    if (props.updateSuccess) {
      handleClose();
    }
  }, [props.updateSuccess]);

  const saveEntity = (event, errors, values) => {
    if (errors.length === 0) {
      const entity = {
        ...bookEntity,
        ...values,
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
          <h2 id="gatewayApp.publicationBook.home.createOrEditLabel" data-cy="BookCreateUpdateHeading">
            <Translate contentKey="gatewayApp.publicationBook.home.createOrEditLabel">Create or edit a Book</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : bookEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="book-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="book-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="titleLabel" for="book-title">
                  <Translate contentKey="gatewayApp.publicationBook.title">Title</Translate>
                </Label>
                <AvField
                  id="book-title"
                  data-cy="title"
                  type="text"
                  name="title"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="authorLabel" for="book-author">
                  <Translate contentKey="gatewayApp.publicationBook.author">Author</Translate>
                </Label>
                <AvField id="book-author" data-cy="author" type="text" name="author" />
              </AvGroup>
              <AvGroup>
                <Label id="keywordsLabel" for="book-keywords">
                  <Translate contentKey="gatewayApp.publicationBook.keywords">Keywords</Translate>
                </Label>
                <AvField id="book-keywords" data-cy="keywords" type="text" name="keywords" />
              </AvGroup>
              <AvGroup>
                <Label id="descriptionLabel" for="book-description">
                  <Translate contentKey="gatewayApp.publicationBook.description">Description</Translate>
                </Label>
                <AvInput id="book-description" data-cy="description" type="textarea" name="description" />
              </AvGroup>
              <AvGroup>
                <Label id="ratingLabel" for="book-rating">
                  <Translate contentKey="gatewayApp.publicationBook.rating">Rating</Translate>
                </Label>
                <AvField id="book-rating" data-cy="rating" type="string" className="form-control" name="rating" />
              </AvGroup>
              <AvGroup>
                <Label id="dateAddedLabel" for="book-dateAdded">
                  <Translate contentKey="gatewayApp.publicationBook.dateAdded">Date Added</Translate>
                </Label>
                <AvField id="book-dateAdded" data-cy="dateAdded" type="date" className="form-control" name="dateAdded" />
              </AvGroup>
              <AvGroup>
                <Label id="dateModifiedLabel" for="book-dateModified">
                  <Translate contentKey="gatewayApp.publicationBook.dateModified">Date Modified</Translate>
                </Label>
                <AvField id="book-dateModified" data-cy="dateModified" type="date" className="form-control" name="dateModified" />
              </AvGroup>
              <AvGroup>
                <AvGroup>
                  <Label id="imageLabel" for="image">
                    <Translate contentKey="gatewayApp.publicationBook.image">Image</Translate>
                  </Label>
                  <br />
                  {image ? (
                    <div>
                      {imageContentType ? (
                        <a onClick={openFile(imageContentType, image)}>
                          <img src={`data:${imageContentType};base64,${image}`} style={{ maxHeight: '100px' }} />
                        </a>
                      ) : null}
                      <br />
                      <Row>
                        <Col md="11">
                          <span>
                            {imageContentType}, {byteSize(image)}
                          </span>
                        </Col>
                        <Col md="1">
                          <Button color="danger" onClick={clearBlob('image')}>
                            <FontAwesomeIcon icon="times-circle" />
                          </Button>
                        </Col>
                      </Row>
                    </div>
                  ) : null}
                  <input id="file_image" data-cy="image" type="file" onChange={onBlobChange(true, 'image')} accept="image/*" />
                  <AvInput type="hidden" name="image" value={image} />
                </AvGroup>
              </AvGroup>
              <Button tag={Link} id="cancel-save" to="/book" replace color="info">
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
  bookEntity: storeState.book.entity,
  loading: storeState.book.loading,
  updating: storeState.book.updating,
  updateSuccess: storeState.book.updateSuccess,
});

const mapDispatchToProps = {
  getCategories,
  getEntity,
  updateEntity,
  setBlob,
  createEntity,
  reset,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(BookUpdate);
