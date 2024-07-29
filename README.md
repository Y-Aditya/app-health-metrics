Health Metrics Service
----------------------

Overview
---------
The Health Metrics Service is a spring boot application that will expose a REST API about the history of the application’s health.
The application should be able to capture the below Disk Usage, and Open file information in configured intervals. 
It provides a REST API to fetch historical data stored in a PostgreSQL database and is secured using OAuth2 tokens with Keycloak as the Identity Provider (IdP).


High-Level Design
--------------------
Metrics capturing involves the Application's cpu, disk usage and open files info. Used Scheduler to capture the data.
The scheduler captures the data currently for every minute and saves in the database.

The application acts as Resource Server and the end points related to metrics retrieval and scheduler stop/start are protected by Keycloak OAuth2 ROLEs mechanism.
Tested this feature with the help of the PostMan tool.
We need to create realms on KeyCloak and clients' and user's along with the roles USER and ADMIN as required.
I have tested for both Client Credentials and Authorization Code grant types with the help of PostMan.

**Softwares/Tools used**
------------------------
```
Spring Boot: Framework for application development and configuration.
Spring Data JPA: ORM for database interactions.
PostgreSQL: Database for storing historical metrics data.
Keycloak: OAuth2 IdP for access and refresh token management.
Spring Security: For securing API endpoints.
Spring AOP: For logging, monitoring, and security aspects.
Springdoc OpenAPI: For generating Swagger documentation.
```

Key Components:
---------------
```
Metrics Collection: Captures and stores disk usage, cpu usage and open file information of the Spring Boot Application (for now I gave the process id of the same application).
REST API: Provides endpoints for fetching historical metrics data and for scheduler stopping/starting. 
OAuth2 Security: Secures API endpoints using Keycloak.
Caching: Optimizes performance by caching frequently accessed data.
Logging and Monitoring: Uses Spring AOP to manage logs and monitor application performance.
```

Compilation and Running
--------------------------
**Prerequisites**
```
JDK 17 or later
PostgreSQL
Keycloak instance
Maven
```

Folder Structure:
-----------------
```
health-metrics-service/
│
└───src
    ├───main
    │   ├───java
    │   │   └───com
    │   │       └───crc
    │   │           └───healthmetrics
    │   │               │   HealthMetricsServiceApplication.java
    │   │               │
    │   │               ├───config
    │   │               │       CacheConfig.java
    │   │               │
    │   │               ├───controller
    │   │               │       HealthMetricsController.java
    │   │               │       PageableValidator.java
    │   │               │
    │   │               ├───entity
    │   │               │       HealthMetrics.java
    │   │               │
    │   │               ├───exception
    │   │               │       CPUUsageComputeException.java
    │   │               │       DiskUsageComputeException.java
    │   │               │       ErrorResponse.java
    │   │               │       GlobalExceptionHandler.java
    │   │               │       MetricsCaptureException.java
    │   │               │       OpenFilesException.java
    │   │               │
    │   │               ├───logger
    │   │               │       AppConfig.java
    │   │               │       LoggingAspect.java
    │   │               │
    │   │               ├───repository
    │   │               │       HealthMetricsRepository.java
    │   │               │
    │   │               ├───scheduler
    │   │               │       ISystemMetrics.java
    │   │               │       LinuxSystemMetrics.java
    │   │               │       MetricsCollector.java
    │   │               │       SchedulerConfig.java
    │   │               │       WindowsSystemMetrics.java
    │   │               │
    │   │               ├───security
    │   │               │       KeycloakRoleConverter.java
    │   │               │       SecurityConfig.java
    │   │               │
    │   │               ├───service
    │   │               │       HealthMetricsService.java
    │   │               │
    │   │               └───util
    │   │                       ResourceExtractor.java
    │   │
    │   └───resources
    │       │   application.properties
    │       │   data.sql
    │       │   schema.sql
    │       │
    │       ├───bin
    │       │       handle.exe
    │       │
    │       ├───static
    │       └───templates
    └───test
        └───java
            └───com
                └───crc
                    └───healthmetrics
                        │   HealthMetricsServiceApplicationTests.java
                        │
                        └───cachetest
                                HealthMetricsServiceTest.java

```
Note: data.sql and schema.sql are just for reference

Compilation Steps:
-----------------------
1. Clone the Repository
```
git clone git@github.com:Y-Aditya/crc_assignment.git
cd health-metrics-service
```

2. Update Application Properties
Edit src/main/resources/application.properties to include your PostgreSQL and Keycloak configurations.
Hardcoded server port as 8081 in properties as KeyCloak is running on 8080.

3. Build the Project
```
.\mvnw clean package
```
4. Run the Application
```
.\mvnw spring-boot:run 
```
5. API Documentation
Access Swagger UI for API documentation 
``` 
http://localhost:8081/swagger-ui.html
```
