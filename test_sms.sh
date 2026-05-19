#!/bin/bash
curl -i -d "{\"code\":\"0000000001\",\"sms\":\"0988888888\"}" \
  -H "Content-Type: application/json" \
  -H "Authorization: dGhhbmhkdEB0b2N0aWVubHRkLnZuOjEyMzQ1Ng==" \
  -X PUT http://toctienltd.vn/cm-portlet/api/customer/sms
echo ""
curl -i -d "{\"code\":\"0000000001\",\"sms\":\"0988888888\"}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic dGhhbmhkdEB0b2N0aWVubHRkLnZuOjEyMzQ1Ng==" \
  -X PUT http://toctienltd.vn/cm-portlet/api/customer/sms
