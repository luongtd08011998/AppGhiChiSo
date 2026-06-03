#!/bin/bash
sed -i '' 's/val msg = response.status?.message ?: ""/val msg = response.status?.message ?: "Lỗi từ server (Backend Error)"/g' composeApp/src/commonMain/kotlin/com/example/appghichiso/data/repository/CustomerRepositoryImpl.kt
