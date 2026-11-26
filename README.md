# Blog Backend Application

## Описание проекта

Бэкенд-приложение для системы блогов, разработанное на базе Spring Boot. Приложение предоставляет REST API для управления постами, комментариями и фотографиями блога.

## Технологический стек

### Основные технологии
- **Java 25** - язык программирования
- **Spring Boot 4.2.0** - фреймворк для построения веб-приложений
- **Spring Boot Starter WebMvc** - стартер для обработки HTTP-запросов и реализации паттерна MVC
- **Spring Boot Starter Validation** - валидация входящих данных

### Работа с данными
- **Spring Boot Starter Data JPA** - стартер для работы с JPA репозиториями
- **PostgreSQL Driver** - драйвер для подключения к базе данных PostgreSQL
- **Mapstruct** - библиотека для генерации маппингов сущностей
- **Lombok** - библиотека для генерации конструкторов, сеттеров, геттеров и т.д.

### Тестирование
- **JUnit** - фреймворк для модульного тестирования
- **H2 Database** - база данных для тестового стенда

## Архитектура приложения

Приложение построено на классической архитектуре Spring MVC:
- **Controllers** - обработка HTTP-запросов
- **Services** - бизнес-логика
- **Repositories** - работа с базой данных через Spring Data JDBC
- **Models/Entities** - модели данных

## Требования для запуска

- Java 17 или выше
- Apache Tomcat 9.x или выше
- PostgreSQL 12 или выше
- Gradle 9+ или выше

## Конфигурация базы данных

Приложение использует PostgreSQL. Необходимо создать базу данных и настроить подключение в конфигурационном файле:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
```

## Сборка и запуск
### Сборка проекта

#### macOS

```
./gradlew clean build
```

#### windows

```
gradlew.bat clean build
```

### Запуск проекта

#### macOS

```
./gradlew bootRun
```

#### windows

```
gradlew.bat bootRun
```

## API Endpoints

#### Посты:

```
GET /api/posts?search={text}&pageNumber={pageNumber}&pageSize={pageSize} - получение списка всех постов
GET /api/posts/{postId} - получение поста по идентификатору
POST /api/posts - создание нового поста
PUT /api/posts/{postId} - обновление поста по идентификатору
DELETE /api/posts/{postId} - удаление поста по идентификатору
```

#### Лайки:

```
POST /api/posts/{postId}/likes - инкремент числа лайков поста
```

#### Картинки:

```
PUT /api/posts/{postId}/image - обновление картинки поста при добавлении или редактировании
GET /api/posts/{postId}/image - получение картинки поста
```

#### Комментарии:

```
GET /api/posts/{postId}/comments - получение комментариев к посту
GET /api/posts/{postId}/comments/{commentId} - получение комментария поста
PUT /api/posts/{postId}/comments/{commentId} - редактирование комментария поста
POST /api/posts/{postId}/comments - добавление комментария к посту
DELETE /api/posts/{postId}/comments/{id} - удаление комментария
```

## Тестирование
### Запуск тестов:

```
./gradlew test
```