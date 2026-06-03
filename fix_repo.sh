#!/bin/bash

# AuthRepositoryImpl.kt
sed -i '' 's/response.status.code == "success"/response.status?.code == "success" || response.status == null/g' composeApp/src/commonMain/kotlin/com/example/appghichiso/data/repository/AuthRepositoryImpl.kt
sed -i '' 's/response.status.message/response.status?.message ?: "Lỗi không xác định"/g' composeApp/src/commonMain/kotlin/com/example/appghichiso/data/repository/AuthRepositoryImpl.kt

# CustomerRepositoryImpl.kt
sed -i '' 's/response.status.code/response.status?.code/g' composeApp/src/commonMain/kotlin/com/example/appghichiso/data/repository/CustomerRepositoryImpl.kt
sed -i '' 's/response.status.message/response.status?.message ?: ""/g' composeApp/src/commonMain/kotlin/com/example/appghichiso/data/repository/CustomerRepositoryImpl.kt

# MeterReadingRepositoryImpl.kt
sed -i '' 's/response.status.code == "success"/response.status?.code == "success" || response.status == null/g' composeApp/src/commonMain/kotlin/com/example/appghichiso/data/repository/MeterReadingRepositoryImpl.kt
sed -i '' 's/response.status.message/response.status?.message ?: "Lỗi cập nhật"/g' composeApp/src/commonMain/kotlin/com/example/appghichiso/data/repository/MeterReadingRepositoryImpl.kt

# RoadRepositoryImpl.kt
sed -i '' 's/response.status.code == "success"/response.status?.code == "success" || response.status == null/g' composeApp/src/commonMain/kotlin/com/example/appghichiso/data/repository/RoadRepositoryImpl.kt
sed -i '' 's/response.status.message/response.status?.message ?: "Lỗi tải tuyến"/g' composeApp/src/commonMain/kotlin/com/example/appghichiso/data/repository/RoadRepositoryImpl.kt

