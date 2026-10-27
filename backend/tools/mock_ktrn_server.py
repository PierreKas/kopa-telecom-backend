#!/usr/bin/env python3
"""
Dev-only stand-in for the KTRN (RBSS-CCBS) gateway - NOT part of the middleware/microservice
code, and never used by them by default. Lets you validate the full Client -> Microservices ->
Middleware -> "KTRN" -> back chain end-to-end while the real gateway at www.4glte.rw is
unreachable (see chat history: DNS resolves to 197.243.46.13 even from inside Kopa's network,
but port 80 there currently refuses connections - an availability/access question for Kopa
Telecom, not something this mock tries to diagnose).

Speaks the exact s2.1.1 contract: reads the REQ_XML form parameter, logs it, and replies with a
canned success XML response (msgcode 1001, REMAIN_AMT). Point kopa.middleware-ktrn at it with:

    KTRN_BASE_URL=http://localhost:9090/api_access.jsp ./mvnw spring-boot:run

Run this script first: python3 mock_ktrn_server.py [port, default 9090]
"""
import sys
from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import parse_qs

RESPONSE_XML = """<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
<response>
  <header>
    <svccode>HP00001</svccode>
    <opcode>HPOP001</opcode>
    <chcode>A</chcode>
    <msgcode>1001</msgcode>
    <message>Successfully processed.</message>
  </header>
  <body>
    <REMAIN_AMT>15000</REMAIN_AMT>
  </body>
</response>
"""


class MockKtrnHandler(BaseHTTPRequestHandler):

    def do_POST(self):
        length = int(self.headers.get("Content-Length", 0))
        raw_body = self.rfile.read(length).decode("utf-8")
        form = parse_qs(raw_body)
        req_xml = form.get("REQ_XML", ["<none>"])[0]

        print("---- received REQ_XML ----")
        print(req_xml)
        print("---------------------------")

        body = RESPONSE_XML.encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/xml")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, format, *args):
        print("[mock-ktrn] " + (format % args))


if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 9090
    server = HTTPServer(("0.0.0.0", port), MockKtrnHandler)
    print(f"Mock KTRN gateway listening on http://localhost:{port}/api_access.jsp")
    print("Point kopa.middleware-ktrn at it with:")
    print(f"  KTRN_BASE_URL=http://localhost:{port}/api_access.jsp ./mvnw spring-boot:run")
    server.serve_forever()
