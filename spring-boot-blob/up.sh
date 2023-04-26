#!/bin/sh

curl http://localhost:8090/attachment/form \
-H "Content-Type: multipart/form-data" \
-v \
-F "content=@test.jpg" \
-F "fileName=test.jpg" \
-F "description=test.jpg"
