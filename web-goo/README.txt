Using an immediate Future:

snert:web-goo hrupp$ curl -i http://localhost:8080/web-goo/test.jsonw?jsonp=jsonp\&immediate=true
HTTP/1.1 200 OK
Connection: keep-alive
X-Powered-By: Undertow/1
Server: WildFly/8
Content-Type: application/vnd.rhq.wrapped+json
Content-Length: 0
Date: Sun, 08 Jun 2014 19:22:43 GMT


=> Content-Type is wrong and no wrapping => onComplete never called
------------------

Using a "transformed" Future:


snert:web-goo hrupp$ curl -i http://localhost:8080/web-goo/test.jsonw?jsonp=jsonp\&immediate=false
HTTP/1.1 200 OK
Connection: keep-alive
X-Powered-By: Undertow/1
Server: WildFly/8
Content-Type: application/javascript;charset=utf-8
Content-Length: 25
Date: Sun, 08 Jun 2014 19:22:58 GMT

jsonp(["Hello","World"]);

=> Correct Content-Type and body => onComplete called