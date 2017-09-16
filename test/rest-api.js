let chai = require('chai');
let chaiHttp = require('chai-http');
let server = require('../index.js');
let should = chai.should();

chai.use(chaiHttp);

//Our parent block
describe('Routes accesses', () => {
    beforeEach((done) => { // Block code executed before each test (nothing here)
        done();         
    });

  /*
  * Test GET / route
  */
  describe('GET /', () => {
      it('it should return a 200 HTTP code', (done) => {
        chai.request(server)
            .get('/')
            .end((err, res) => {
              res.should.have.status(200);
              done();
            });
      });
  });

  /*
  * Test GET /authentication.svg route, without URL parameter
  */
  describe('GET /authentication.svg', () => {
      it('it should return a 404 HTTP code', (done) => {
        chai.request(server)
            .get('/authentication.svg')
            .end((err, res) => {
              res.should.have.status(404);
              done();
            });
      });
  });

  /*
  * Test GET /authentication.svg route, with an URL paramater
  */
  describe('GET /authentication.svg?id=66f24b575f5936ddc2ac', () => {
      it('it should return a 200 HTTP code', (done) => {
        chai.request(server)
            .get('/authentication.svg?id=66f24b575f5936ddc2ac')
            .end((err, res) => {
              res.should.have.status(200);
              done();
            });
      });
  });

  /*
  * Test GET /authhandler route, without URL parameter
  */
  describe('GET /authhandler', () => {
      it('it should return a 404 HTTP code', (done) => {
        chai.request(server)
            .get('/authhandler')
            .end((err, res) => {
              res.should.have.status(404);
              done();
            });
      });
  });

  /*
  * Test GET /authhandler route, with an URL parameter
  */
  describe('GET /authhandler?id=myFakeID', () => {
      it('it should return a 404 HTTP code', (done) => {
        chai.request(server)
            .get('/authhandler?id=c5d2dd6385cc6a305ef5')
            .end((err, res) => {
              res.should.have.status(404);
              done();
            });
      });
  });

});
