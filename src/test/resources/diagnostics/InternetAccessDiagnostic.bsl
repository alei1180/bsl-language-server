Процедура Тест1()
    FTPСоединение = Новый FTPСоединение(Сервер, Порт, Пользователь, Пароль); // ошибка

    Определения = Новый WSОпределения("http://localhost/test.asmx?WSDL"); // ошибка

    ПроксиДва = Новый WSПрокси(Определения, "http://localhost/", "test", "test"); // ошибка

    Определения =
        Новый WSОпределения("http://localhost/test.asmx?WSDL", "Пользователь", "Пароль", Неопределено, Таймаут); // ошибка

КонецПроцедуры

Процедура HTTP()
    HTTPСоединение = Новый HTTPСоединение("zabbix.localhost", 80); // ошибка
    HTTPЗапрос = Новый HTTPЗапрос(); // ошибка
    HTTPЗапрос = Новый HTTPЗапрос("zabbix", 80); // ошибка
    HTTPЗапрос = Новый HTTPЗапрос("zabbix"); // ошибка
    ИнтернетПрокси = Новый ИнтернетПрокси("zabbix"); // ошибка
КонецПроцедуры

Процедура HTTP()
    HTTPСоединение = Новый HTTPСоединение("zabbix.localhost", 80); // ошибка
    HTTPЗапрос = Новый HTTPЗапрос(); // ошибка
    HTTPЗапрос = Новый HTTPЗапрос("zabbix", 80); // ошибка
    HTTPЗапрос = Новый HTTPЗапрос("zabbix"); // ошибка
    ИнтернетПрокси = Новый ИнтернетПрокси("zabbix"); // ошибка
КонецПроцедуры

Функция НовыйИнтернетПочтовыйПрофильБезТаймАута()
    Профиль = Новый ИнтернетПочтовыйПрофиль; // ошибка
    Профиль.Пользователь = "admin";
    Возврат Профиль;
КонецФункции

Функция InternetMail()
    Профиль = Новый InternetMail; // ошибка
КонецФункции

Профиль = Новый Почта; // ошибка