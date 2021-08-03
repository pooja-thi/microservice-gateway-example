import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, openFile, byteSize, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './book.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IBookDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const BookDetail = (props: IBookDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { bookEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="bookDetailsHeading">
          <Translate contentKey="gatewayApp.publicationBook.detail.title">Book</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{bookEntity.id}</dd>
          <dt>
            <span id="title">
              <Translate contentKey="gatewayApp.publicationBook.title">Title</Translate>
            </span>
          </dt>
          <dd>{bookEntity.title}</dd>
          <dt>
            <span id="author">
              <Translate contentKey="gatewayApp.publicationBook.author">Author</Translate>
            </span>
          </dt>
          <dd>{bookEntity.author}</dd>
          <dt>
            <span id="keywords">
              <Translate contentKey="gatewayApp.publicationBook.keywords">Keywords</Translate>
            </span>
          </dt>
          <dd>{bookEntity.keywords}</dd>
          <dt>
            <span id="description">
              <Translate contentKey="gatewayApp.publicationBook.description">Description</Translate>
            </span>
          </dt>
          <dd>{bookEntity.description}</dd>
          <dt>
            <span id="rating">
              <Translate contentKey="gatewayApp.publicationBook.rating">Rating</Translate>
            </span>
          </dt>
          <dd>{bookEntity.rating}</dd>
          <dt>
            <span id="dateAdded">
              <Translate contentKey="gatewayApp.publicationBook.dateAdded">Date Added</Translate>
            </span>
          </dt>
          <dd>{bookEntity.dateAdded ? <TextFormat value={bookEntity.dateAdded} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="dateModified">
              <Translate contentKey="gatewayApp.publicationBook.dateModified">Date Modified</Translate>
            </span>
          </dt>
          <dd>
            {bookEntity.dateModified ? <TextFormat value={bookEntity.dateModified} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="image">
              <Translate contentKey="gatewayApp.publicationBook.image">Image</Translate>
            </span>
          </dt>
          <dd>
            {bookEntity.image ? (
              <div>
                {bookEntity.imageContentType ? (
                  <a onClick={openFile(bookEntity.imageContentType, bookEntity.image)}>
                    <img src={`data:${bookEntity.imageContentType};base64,${bookEntity.image}`} style={{ maxHeight: '30px' }} />
                  </a>
                ) : null}
                <span>
                  {bookEntity.imageContentType}, {byteSize(bookEntity.image)}
                </span>
              </div>
            ) : null}
          </dd>
        </dl>
        <Button tag={Link} to="/book" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/book/${bookEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ book }: IRootState) => ({
  bookEntity: book.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(BookDetail);
