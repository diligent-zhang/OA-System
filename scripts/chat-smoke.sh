#!/bin/sh
# 即时通讯 + 消息红标 冒烟测试（针对 8081 端口的新代码实例，请求体全 ASCII 以避免 GBK 重编码）
BASE=http://localhost:8081/api

echo "== waiting for backend on 8081 =="
i=0
code=""
while [ $i -lt 60 ]; do
  code=$(curl -s -o /dev/null -w '%{http_code}' "$BASE/messages/unread-count")
  if [ "$code" = "200" ]; then echo "backend up"; break; fi
  sleep 2
  i=$((i + 1))
done
if [ "$code" != "200" ]; then echo "backend NOT up (last http=$code)"; exit 1; fi

echo "== login =="
LOGIN=$(curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' -d '{"username":"admin","password":"123456"}')
echo "$LOGIN"
TOKEN=$(printf '%s' "$LOGIN" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
MYID=$(printf '%s' "$LOGIN" | sed -n 's/.*"userId":\([0-9]*\).*/\1/p')
echo "token.len=${#TOKEN} myId=$MYID"
AUTH="Authorization: Bearer $TOKEN"

echo "== conversations (expect empty list) =="
curl -s "$BASE/chat/conversations" -H "$AUTH"; echo

echo "== pick peer from contacts =="
CONTACTS=$(curl -s "$BASE/contacts/query" -H "$AUTH")
PEER=$(printf '%s' "$CONTACTS" | grep -o '"userId":[0-9]*' | sed 's/"userId"://' | grep -v "^$MYID$" | head -1)
echo "peerUserId=$PEER"

echo "== open SINGLE conversation =="
OPEN=$(curl -s -X POST "$BASE/chat/conversations" -H "$AUTH" -H 'Content-Type: application/json' -d "{\"type\":\"SINGLE\",\"memberUserIds\":[$PEER]}")
echo "$OPEN"
CID=$(printf '%s' "$OPEN" | sed -n 's/.*"data":\([0-9]*\).*/\1/p')
echo "conversationId=$CID"

echo "== send message =="
curl -s -X POST "$BASE/chat/messages" -H "$AUTH" -H 'Content-Type: application/json' -d "{\"conversationId\":$CID,\"content\":\"hello-smoke\"}"; echo

echo "== list conversations (expect lastContent + peer name) =="
curl -s "$BASE/chat/conversations" -H "$AUTH"; echo

echo "== conversation messages =="
curl -s "$BASE/chat/conversations/$CID/messages?pageNum=1&pageSize=10" -H "$AUTH"; echo

echo "== mark conversation read =="
curl -s -X PUT "$BASE/chat/conversations/$CID/read" -H "$AUTH"; echo

echo "== messages read-all then unread-count (expect 0) =="
curl -s -X PUT "$BASE/messages/read-all" -H "$AUTH"; echo
curl -s "$BASE/messages/unread-count" -H "$AUTH"; echo

echo "== open GROUP conversation =="
P1=$(printf '%s' "$CONTACTS" | grep -o '"userId":[0-9]*' | sed 's/"userId"://' | grep -v "^$MYID$" | head -1)
P2=$(printf '%s' "$CONTACTS" | grep -o '"userId":[0-9]*' | sed 's/"userId"://' | grep -v "^$MYID$" | head -2 | tail -1)
if [ -n "$P2" ] && [ "$P1" != "$P2" ]; then
  curl -s -X POST "$BASE/chat/conversations" -H "$AUTH" -H 'Content-Type: application/json' -d "{\"type\":\"GROUP\",\"memberUserIds\":[$P1,$P2],\"groupName\":\"smoke-group\"}"; echo
else
  echo "not enough peers for group test (p1=$P1 p2=$P2)"
fi

echo "== done =="
