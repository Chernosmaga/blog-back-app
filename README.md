# Blog Backend Application

## Описание проекта

Бэкенд-приложение для системы блогов, разработанное на базе Spring Framework (без Spring Boot). Приложение предоставляет REST API для управления постами, комментариями и пользователями блога.

## Технологический стек

### Основные технологии
- **Java 21** - язык программирования
- **Spring Framework 6.2.1** - фреймворк для построения веб-приложений
- **Spring MVC** - модуль для обработки HTTP-запросов и реализации паттерна MVC
- **Jakarta Servlet API 4.0.2** - спецификация для сервлетов
- **Apache Tomcat** - servlet container для развертывания приложения

### Работа с данными
- **Spring Data JDBC 3.4.1** - упрощенная работа с реляционными базами данных через JDBC
- **PostgreSQL Driver 42.7.3** - драйвер для подключения к базе данных PostgreSQL

### Сериализация/Десериализация
- **Jackson Databind 2.17.0** - преобразование Java объектов в JSON и обратно
- **Jackson Core 2.17.0** - базовая функциональность для работы с JSON
- **Jackson Annotations 2.17.0** - аннотации для настройки сериализации

### Тестирование
- **JUnit 5.7.0** - фреймворк для модульного тестирования

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
- Maven 3.6+

## Конфигурация базы данных

Приложение использует PostgreSQL. Необходимо создать базу данных и настроить подключение в конфигурационном файле:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/blog_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## Сборка и запуск
### Сборка проекта

```
mvn clean package
```

### Развертывание
#### Собранный WAR-файл необходимо разместить в директории webapps сервера Tomcat:

```
cp target/blog-backend.war $CATALINA_HOME/webapps/
```

### Запуск Tomcat

```
%CATALINA_HOME%\bin\startup.bat
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
mvn test
```