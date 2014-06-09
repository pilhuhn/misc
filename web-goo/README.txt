== Running with a Servlet Filter doing work

Using an immediate Future:

$ curl -i http://localhost:8080/web-goo/test.jsonw?jsonp=jsonp\&immediate=true
HTTP/1.1 200 OK
Connection: keep-alive
X-Powered-By: Undertow/1
Server: WildFly/8
Content-Type: application/vnd.rhq.wrapped+json
Content-Length: 0
Date: Sun, 08 Jun 2014 19:22:43 GMT


=> Content-Type is wrong and no wrapping (and no body ) => onComplete never called
------------------

Using a "transformed" Future:


$ curl -i http://localhost:8080/web-goo/test.jsonw?jsonp=jsonp\&immediate=false
HTTP/1.1 200 OK
Connection: keep-alive
X-Powered-By: Undertow/1
Server: WildFly/8
Content-Type: application/javascript;charset=utf-8
Content-Length: 25
Date: Sun, 08 Jun 2014 19:22:58 GMT

jsonp(["Hello","World"]);

=> Correct Content-Type and body => onComplete called

== No Servlet Filter

Both times it "just works"

$ curl -i http://localhost:8080/web-goo/test.json?immediate=false
HTTP/1.1 200 OK
Connection: keep-alive
X-Powered-By: Undertow/1
Server: WildFly/8
Transfer-Encoding: chunked
Content-Type: application/json
Date: Mon, 09 Jun 2014 06:58:56 GMT

["Hello","World"]


$ curl -i http://localhost:8080/web-goo/test.json?immediate=true
HTTP/1.1 200 OK
Connection: keep-alive
X-Powered-By: Undertow/1
Server: WildFly/8
Transfer-Encoding: chunked
Content-Type: application/json
Date: Mon, 09 Jun 2014 06:59:00 GMT

["Hello","World"]