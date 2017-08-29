# Синопсис

Это попытка реализовать car-sharing приложение, которое позволяет экономить на путешествиях, 
присоединяясь к поездкам других людей или создавая поездки, к которым могут присоединиться.
Была поставлена задача разработать приложение, которое позволяет создать пользователя, 
от его лица создать поездку, сделать поиск по поездкам с некоторыми параметрами, присоединиться к выбранной.
Поиск должен работать быстро, создание пользователей и поездок, присоединение к поездкам может быть более медленным.
Весь функционал приложения можно получить по /help в телеграм боте.

Проект состоит из четырех модулей.

# alicecarservice

Это серверная часть, 
которая позволяет создавать пользователей и поездки, хранить всех созданных пользоваетелей и их поездки и 
добавляться в поездки.
С некоторой периодичностью обновления поездок шлются на поисковый сервер
searchservice.

Пакет com.alice.dbclasses предназначен для работы с поездками и пользователями и хранением их базах данных,
как серверной, так и поисковой.<br />
Пакет com.alice.kafkaclasses – для пересылки обновленных поездок с помощью кафки на поисковый сервер.<br />
Пакет com.alice.rest – для работы с сервером по сети.<br />
Пакет com.alice.services содержит в себе сервисы сервера.<br />

Тесты в данном модуле уже неактуальны.

#api

Модуль, который предоставляет необходимое апи для других модулей.

#bot

Тестовый модуль, который работает в разных режимах: тестирует корректность работы серверной части, 
создавая какое-то количество пользователей и поездок и добавляясь в них; симулирует нагрузку на сервер;
телеграм-бот, с помощью которого можно работать напрямую с приложением.

#searchservice

Поисковый сервер, который позволяет искать актуальные (то есть непросроченные) поездки по временным рамкам.
В нем есть кэш, который хранит в себе поездки для следующих 10 дней (было решено из-за того, 
что предполагается, что клиенты будут искать чаще поездки, которые должны состояться скоро).<br />
Сервер позволяет искать по заданным датам, отдавая список id поездок. <br />
Используется hibernate для удобства поиска по параметрам поездки.

Пакет com.test.cache содержит кэш, информация в котором обновляется при обновлении базы данных.  
Также обновляется автоматически в полночь. <br />
Пакет com.test.db позволяет работать с базой данных, которая содержит в себе поездки в виде, удобном для поиска. <br />
Пакет com.test.dbclasses содержит классы базы данных. <br />
Пакет com.test.kafka позволяет получать обновленные поездки.<br />
SearchService ищет поездки с заданными параметрами.<br />