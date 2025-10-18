**Сборка**
```
./gradlew clean build
```

**Запуск**
```
./gradlew bootRun
```
Проверка: GET /actuator/health → UP

**Форматирование кода**

В проекте включён плагин для проверки код стайла. Если получили ошибку при сборке, запустите сборку еще раз. В случае, когда автоматическое изменение невозможно, исправьте код вручную.

**Миграции бд**

Liquibase применяет миграции автоматически при старте.
Для этого в application.yaml нужно указать
```
liquibase:
    enabled: true
```
и прописать креды.

Скрипты sql должны располагаться в папке
```
dbscripts/liquibase/memorycards/changelog
```
Формат changeSet:
```
  - changeSet:
      id: v_1_0_0
      author: "Marco Villarreal"
      comment: "Initial dummy library database schema"
      sqlFile:
        encoding: utf8
        relativeToChangelogFile: true
        stripComments: true
        path: "v_1_0_0/main-changelog.sql"
```
